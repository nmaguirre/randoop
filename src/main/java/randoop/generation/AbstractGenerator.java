package randoop.generation;

import plume.Option;
import plume.OptionGroup;
import plume.Unpublicized;
import randoop.*;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.test.TestCheckGenerator;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.Log;
import randoop.util.ProgressDisplay;
import randoop.util.Randomness;
import randoop.util.ReflectionExecutor;
import randoop.util.SimpleList;
import randoop.util.Timer;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizerLog;
import randoop.util.heapcanonicalization.ExtendExtensionsResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.RandomPerm;
import randoop.util.heapcanonicalization.Tuple;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensions;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStrings;
import randoop.util.obsdetection.OperationClassifier;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Algorithm template for implementing a test generator.
 *
 * The main generation loop is defined in method <code>explore()</code>, which
 * repeatedly generates a new sequence, determines if it a failing sequence, and
 * stops the process when the time or sequence limit expires. The process of
 * generating a new sequences is left abstract.
 *
 * @see ForwardGenerator
 */
public abstract class AbstractGenerator {
	
	
	public static HeapCanonicalizer newCanonicalizer;
	public static CanonicalStore store;
	public static FieldExtensions globalExtensions;

	public void initNewCanonicalizer(Collection<String> classNames) {
		store = new CanonicalStore(classNames, fbg_field_distance);
		newCanonicalizer = new HeapCanonicalizer(store, fbg_max_objects, fbg_max_array_objs, fbg_bfs_depth);
		globalExtensions = new FieldExtensionsStrings();
	}
	
	
	
	  public enum FieldBasedGenType {
          /** Field based generation is disabled */
          DISABLED,
          // Field based generation
          EXTENSIONS,
          // Hashes as proxy for object equality
          HASHES
	  }
	
	  @OptionGroup("Field based generation")
	  @Option("Select a generation approach. "
	                + "DISABLED: Original randoop. "
	                + "EXTENSIONS: Field based generation. "
	                + "HASHES: Use hashes as a proxy for object equality")
	  public static FieldBasedGenType field_based_gen = FieldBasedGenType.EXTENSIONS;

	  @Option("Do not canonicalize structures having more than this number of objects of a single reference type.")
	  public static int fbg_max_objects = 100;
	  
	  @Option("Only canonicalize up to this number of elements in arrays.")
	  public static int fbg_max_array_objs = 100;
	  
	  @Option("Only canonicalize objects reachable by this number of field traversals from the structure's root.")
	  public static int fbg_bfs_depth = Integer.MAX_VALUE;
	  
	  @Option("Only canonicalize objects reachable by this number of field traversals from the structure's root.")
	  public static int fbg_field_distance = 2;
	  
	  @Option("Detect observers during field based generation and use them to extend tests.")
	  public static boolean fbg_observer_detection = true;  
	  
	  @Option("Only save up to this number of characters of a string in extensions")
	  public static int fbg_max_strlen_in_extensions = 1000;
	  
	  @Option("Allows field based generation to detect precisely which objects enlarge the extensions."
		  		+ " This may negatively affect runtime performance.")
	  public static boolean fbg_precise_extension_detection = true;
	  
	  @Option("Repeat observer operation during the second phase to generate better oracles")
	  public static int fbg_repeat_observers = 1;
	  
	  @Option("Disable randoop's collections and arrays generation heuristic")
	  public static boolean disable_collections_generation_heuristic = false;

	  @Option("Disable randoop's collections and arrays generation heuristic")
	  public static boolean disable_contracts = true;

	  @Option("Allow methods from abstract classes for generation")
	  public static boolean allow_abstract_classes_methods = true;
	  
	  @Option("Separate simple observers from observers in the second phase.")
	  public static boolean fbg_2nd_phase_simple_obs_detection = false; 
	  
	  @Option("Extend subsumed tests during the second phase.")
	  public static boolean field_based_gen_extend_subsumed = false;

	  @Option("Max number of observers to be added to each test.")
	  public static int fbg_observers_per_test = 100; 
	  
	  @Option("Max number of simple observers to be added to each test.")
	  public static int fbg_simple_observers_per_test = 100; 

	  @Option("Second phase TO.")
	  public static int fbg_extend_with_observers_TO = 90000;

	  @Option("Percentage of lines reserved for observers when --fbg-observer-detection is true, from the max number of lines given by the --maxsize parameter.")
	  public static double fbg_observer_lines = 0.50; 

	  

  @OptionGroup(value = "AbstractGenerator unpublicized options", unpublicized = true)
  @Unpublicized
  @Option("Dump each sequence to the log file")
  public static boolean dump_sequences = false;

  @RandoopStat(
      "Number of generation steps (one step consists of an attempt to generate and execute a new, distinct sequence)")
  public int num_steps = 0;

  @RandoopStat("Number of sequences generated.")
  public int num_sequences_generated = 0;

  @RandoopStat("Number of failing sequences generated.")
  public int num_failing_sequences = 0;

  @RandoopStat("Number of invalid sequences generated.")
  public int invalidSequenceCount = 0;

  /**
   * The timer used to determine how much time has elapsed since the start of
   * generator and whether generation should stop.
   */
  public final Timer timer = new Timer();

  /**
   * Time limit for generation. If generation reaches the specified time limit
   * (in milliseconds), the generator stops generating sequences.
   */
  public final long maxTimeMillis;

  /**
   * Sequence limit for generation. If generation reaches the specified sequence
   * limit, the generator stops generating sequences.
   */
  public final int maxGeneratedSequences;

  /**
   * Limit for output. Once the specified number of sequences are in the output
   * lists, the generator will stop.
   */
  public final int maxOutputSequences;

  /**
   * The list of statement kinds (methods, constructors, primitive value
   * declarations, etc.) used to generate sequences. In other words, statements
   * specifies the universe of operations from which sequences are generated.
   */
  public List<TypedOperation> operations;

  /**
   * Container for execution visitors used during execution of sequences.
   */
  protected ExecutionVisitor executionVisitor;

  /**
   * Component manager responsible for storing previously-generated sequences.
   */
  public ComponentManager componentManager;

  /**
   * Customizable stopping criterion in addition to time and sequence limits.
   */
  private IStopper stopper;

  /**
   * Manages notifications for listeners.
   *
   * @see randoop.generation.IEventListener
   */
  public RandoopListenerManager listenerMgr;

  /**
   * Updates the progress display message printed to the console.
   */
  private ProgressDisplay progressDisplay;

  /**
   * This field is set by Randoop to point to the sequence currently being
   * executed. In the event that Randoop appears to hang, this sequence is
   * printed out to console to help the user debug the cause of the hanging
   * behavior.
   */
  public static Sequence currSeq = null;

  /**
   * The list of error test sequences to be output as JUnit tests. May include
   * subsequences of other sequences in the list.
   */
  public List<ExecutableSequence> outErrorSeqs = new ArrayList<>();

  /**
   * The list of regression sequences to be output as JUnit tests. May include
   * subsequences of other sequences in the list.
   */
  public List<ExecutableSequence> outRegressionSeqs = new ArrayList<>();
  
  public List<ExecutableSequence> positiveRegressionSeqs = new ArrayList<>();
  public List<ExecutableSequence> modifierRegressionSeqs = new ArrayList<>();
  public List<ExecutableSequence> observerRegressionSeqs = new ArrayList<>();

  /**
   * A filter to determine whether a sequence should be added to the output
   * sequence lists.
   */
  public Predicate<ExecutableSequence> outputTest;

  /**
   * Visitor to generate checks for a sequence.
   */
  protected TestCheckGenerator checkGenerator;
private int positiveReferenceExtendingTests;
private int notExtendingPositiveTestsSaved;
private int observerPositiveTestsSaved;
private int positiveTestsSaved;
private int savedTests;
private int positiveTestsDropped;
private int discardedTests;
private int negativeTestsSaved;
private int negativeTestsDropped;

  /**
   * Constructs a generator with the given parameters.
   *
   * @param operations
   *          Statements (e.g. methods and constructors) used to create
   *          sequences. Cannot be null.
   * @param timeMillis
   *          maximum time to spend in generation. Must be non-negative.
   * @param maxGeneratedSequences
   *          the maximum number of sequences to generate. Must be non-negative.
   * @param maxOutSequences
   *          the maximum number of sequences to output. Must be non-negative.
   * @param componentManager
   *          the component manager to use to store sequences during
   *          component-based generation. Can be null, in which case the
   *          generator's component manager is initialized as
   *          <code>new ComponentManager()</code>.
   * @param stopper
   *          Optional, additional stopping criterion for the generator. Can be
   *          null.
   * @param listenerManager
   *          Manager that stores and calls any listeners to use during
   *          generation. Can be null.
   */
  public AbstractGenerator(
      List<TypedOperation> operations,
      long timeMillis,
      int maxGeneratedSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager) {
    assert operations != null;

    this.maxTimeMillis = timeMillis;
    this.secondPhaseMaxTimeMillis = maxTimeMillis + fbg_extend_with_observers_TO; 
    this.maxGeneratedSequences = maxGeneratedSequences;
    this.maxOutputSequences = maxOutSequences;
    this.operations = operations;
    this.executionVisitor = new DummyVisitor();
    this.outputTest = new AlwaysFalse<>();

    if (componentManager == null) {
      this.componentManager = new ComponentManager();
    } else {
      this.componentManager = componentManager;
    }

    this.stopper = stopper;
    this.listenerMgr = listenerManager;
  }

  /**
   * Registers test predicate with this generator for use while filtering
   * generated tests for output.
   *
   * @param outputTest
   *          the predicate to be added to object
   */
  public void addTestPredicate(Predicate<ExecutableSequence> outputTest) {
    if (outputTest == null) {
      throw new IllegalArgumentException("outputTest must be non-null");
    }
    this.outputTest = outputTest;
  }

  /**
   * Registers a visitor with this object for use while executing each generated
   * sequence.
   *
   * @param executionVisitor
   *          the visitor
   */
  public void addExecutionVisitor(ExecutionVisitor executionVisitor) {
    if (executionVisitor == null) {
      throw new IllegalArgumentException("executionVisitor must be non-null");
    }
    this.executionVisitor = executionVisitor;
  }

  /**
   * Registers a visitor with this object to generate checks following execution
   * of each generated test sequence.
   *
   * @param checkGenerator
   *          the check generating visitor
   */
  public void addTestCheckGenerator(TestCheckGenerator checkGenerator) {
    if (checkGenerator == null) {
      throw new IllegalArgumentException("checkGenerator must be non-null");
    }
    this.checkGenerator = checkGenerator;
  }

  /**
   * Tests stopping criteria and determines whether generation should stop.
   * Criteria are checked in this order:
   * <ul>
   * <li>if there is a listener manager,
   * {@link RandoopListenerManager#stopGeneration()} returns true,
   * <li>the elapsed generation time is greater than or equal to the max time in
   * milliseconds,
   * <li>the number of output sequences is equal to the maximum output,
   * <li>the number of generated sequences is equal to the maximum generated
   * sequence count, or
   * <li>if there is a stopper, {@link IStopper#stop()} returns true.
   * </ul>
   *
   * @return true if any of stopping criteria are met, otherwise false
   */
  protected boolean stop() {
    return (listenerMgr != null && listenerMgr.stopGeneration())
        || (timer.getTimeElapsedMillis() >= maxTimeMillis)
        || (GenInputsAbstract.stop_on_error_test && numErrorSequences() > 0)
        || (numOutputSequences() >= maxOutputSequences)
        || (numGeneratedSequences() >= maxGeneratedSequences)
        || (stopper != null && stopper.stop());
  }

  /**
   * Generate an individual test sequence
   *
   * @return a test sequence, may be null
   */
  public abstract ExecutableSequence step();

  /**
   * Returns the count of generated sequence currently for output.
   *
   * @return the sum of the number of error and regression test sequences for
   *         output
   */
  public int numOutputSequences() {
    return outErrorSeqs.size() + outRegressionSeqs.size();
  }

  /**
   * Returns the count of generated error-revealing sequences.
   *
   * @return the number of error test sequences
   */
  private int numErrorSequences() {
    return outErrorSeqs.size();
  }
  
  protected Set<Sequence> subsumed_sequences = new LinkedHashSet<>();

  protected Set<Sequence> subsumed_candidates;
private int genFirstAdditionalPositiveSeqs;
private int genFirstAdditionalNegativeSeqs;
private int operationBadTag;
private int genFirstAdditionalObsPositiveSeqs;
private int genFirstAdditionalObsNegativeSeqs;
private int genFirstAdditionalObsErrorSeqs;
private int genFirstAdditionalErrorSeqs; 
  
  public void saveSubsumedCandidates() {
	  for (Sequence is: subsumed_candidates) 
		  subsumed_sequences.add(is);
  }

  
  /*
  private boolean saveRarelyExtendingOperation(ExecutableSequence eSeq) {
	TypedOperation op = eSeq.getLastStmtOperation();
	  
	if (OperationClassifier.isModifier(op)) {
		if (fbg_save_not_extending_modifier_ratio == 0)
			return false;
		if (fbg_save_not_extending_modifier_ratio == Integer.MAX_VALUE)
			return true;

		int ratio = (numRegressionSequences() / fbg_save_not_extending_modifier_ratio);
		return op.timesExecutedInSavedPositiveModifiersTests <= ratio;
	} 
	else {
		if (fbg_save_not_extending_observer_ratio == 0)
			return false;
		if (fbg_save_not_extending_observer_ratio == Integer.MAX_VALUE)
			return true;

		int ratio = (numRegressionSequences() / fbg_save_not_extending_observer_ratio);
		return op.timesExecutedInSavedPositiveObserversTests <= ratio;
	}
	
  }
  */
  

  /**
   * Returns the count of sequences generated so far by the generator.
   *
   * @return the number of sequences generated
   */
  public abstract int numGeneratedSequences();

  private void treatPositiveSequence(ExecutableSequence eSeq) {
	  boolean save = false;
		  if (eSeq.enlargesExtensions == ExtendExtensionsResult.EXTENDED) {
			  save = true;
				  positiveReferenceExtendingTests++;
				  if (CanonicalizerLog.isLoggingOn()) 
					  CanonicalizerLog.logLine("> Extensions enlarged for reference type objects.");
		  }
		  else if (eSeq.enlargesExtensions == ExtendExtensionsResult.LIMITS_EXCEEDED) {
			  // We discard the tests exceeding limits (for now at least)
			  assert false: "LIMITS_EXCEEDED in extensions not implemented"; //"Should never happen as randoop's generation mechanism does not produce too large strings";
		  }
		  else {
			  // TODO: Need observer detection for the following line to work
			  //if (eSeq.getLastStmtOperation().isModifier() && saveNonExtendingModifierSequence(eSeq)) {
 			  if (CanonicalizerLog.isLoggingOn()) 
 				  CanonicalizerLog.logLine("> Extensions not enlarged.");
			  //if (saveRarelyExtendingOperation(eSeq)) {
 			  // TODO: Implement rarely extending operations
			  if (true) {
				  save = true; 
				  if (OperationClassifier.isModifier(eSeq.getLastStmtOperation())) {
					  //eSeq.getLastStmtOperation().timesExecutedInSavedPositiveModifiersTests++;
					  notExtendingPositiveTestsSaved++;
				  }
				  else {
					  //eSeq.getLastStmtOperation().timesExecutedInSavedPositiveObserversTests++;
					  observerPositiveTestsSaved++;
				  }

				  if (CanonicalizerLog.isLoggingOn()) 
					  CanonicalizerLog.logLine("> The last operation is rarely used in tests enlarging extensions.");
			  }
		  }

	  if (save) {
		  outRegressionSeqs.add(eSeq);
		  saveSubsumedCandidates();
		  positiveTestsSaved++;
		  savedTests++;

		  if (fbg_observer_detection) {
			  assert !OperationClassifier.notExecuted(eSeq.getLastStmtOperation()): "Operation must be executed at this point";
			  positiveRegressionSeqs.add(eSeq);
		  }
		  if (CanonicalizerLog.isLoggingOn()) 
			  CanonicalizerLog.logLine("> Current positive sequence saved.");
	  }
	  else {
		  positiveTestsDropped++;
		  discardedTests++;
		  if (CanonicalizerLog.isLoggingOn()) 
			  CanonicalizerLog.logLine("> Current positive sequence discarded.");
	  }
  }
  
  
  private void treatNegativeSequence(ExecutableSequence eSeq) {
	  boolean save = false;
	  // TODO: Implement drop observers
	  // if (saveNegativeSequence(eSeq)) {
	  if (true) {
		  save = true;
		  if (CanonicalizerLog.isLoggingOn()) 
			  CanonicalizerLog.logLine("> The last operation is rarely stored in negative sequences."); 
	  }
	  
	  if (save) {
		  outRegressionSeqs.add(eSeq);
		  saveSubsumedCandidates();
		  negativeTestsSaved++;
		  savedTests++;
		  //eSeq.getLastStmtOperation().timesExecutedInSavedNegativeTests++;
		  if (CanonicalizerLog.isLoggingOn()) 
			  CanonicalizerLog.logLine("> Current negative sequence saved.");
	  }
	  else {
		  negativeTestsDropped++;
		  discardedTests++;
		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine("> Current negative sequence discarded.");
	  }
  }


  
  /**
   * Creates and executes new sequences until stopping criteria is met.
 * @param genFirstAdditionalErrorSeqs 
   *
   * @see AbstractGenerator#stop()
   * @see AbstractGenerator#step()
   */
  public void explore() {
    if (checkGenerator == null) {
      throw new Error("Generator not properly initialized - must have a TestCheckGenerator");
    }
    Log.log(this.operations);

    timer.startTiming();

    if (!GenInputsAbstract.noprogressdisplay) {
      progressDisplay = new ProgressDisplay(this, listenerMgr, ProgressDisplay.Mode.MULTILINE, 200);
      progressDisplay.start();
    }

    if (Log.isLoggingOn()) {
      Log.logLine("Initial sequences (seeds):");
      for (Sequence s : componentManager.getAllGeneratedSequences()) {
        Log.logLine(s.toString());
      }
    }

    // Notify listeners that exploration is starting.
    if (listenerMgr != null) {
      listenerMgr.explorationStart();
    }

    while (!stop()) {

      // Notify listeners we are about to perform a generation step.
      if (listenerMgr != null) {
        listenerMgr.generationStepPre();
      }

      num_steps++;

      ExecutableSequence eSeq = step();

      if (dump_sequences) {
        System.out.printf("seq before run: %s%n", eSeq);
      }

      // Notify listeners we just completed generation step.
      if (listenerMgr != null) {
        listenerMgr.generationStepPost(eSeq);
      }

      if (eSeq == null) {
        continue;
      }

      num_sequences_generated++;

      if (eSeq.hasFailure()) {
        num_failing_sequences++;
      }

      if (outputTest.test(eSeq)) {
    	    if (!eSeq.hasInvalidBehavior()) {
    	  	  if (eSeq.hasFailure()) {
    	  		  outErrorSeqs.add(eSeq);
    	  	  } 
    	  	  else {
    	  		  if (field_based_gen == FieldBasedGenType.DISABLED) {
    	  			  outRegressionSeqs.add(eSeq);
    	  		  }
    	  		  else {
    	  			  if (eSeq.isNormalExecution()) 
    	  				  treatPositiveSequence(eSeq);
    	  			  else 
    	  				  treatNegativeSequence(eSeq);
    	  		  }
    	  	  } 
    	    }
    	    else {
    	  	  invalidSequenceCount++;
    	    }
      }

    	    
      if (dump_sequences) {
        System.out.printf("Sequence after execution:%n%s%n", eSeq.toString());
        System.out.printf("allSequences.size() = %d%n", numGeneratedSequences());
      }

      if (Log.isLoggingOn()) {
        Log.logLine("Sequence after execution: " + Globals.lineSep + eSeq.toString());
        Log.logLine("allSequences.size()=" + numGeneratedSequences());
      }
    }
    
    // Second phase
	if (field_based_gen == FieldBasedGenType.EXTENSIONS && fbg_observer_detection) {
		long secondPhaseStartTime = System.currentTimeMillis();

		genFirstAdditionalPositiveSeqs = 0;
		genFirstAdditionalNegativeSeqs = 0;
		genFirstAdditionalErrorSeqs = 0;

		for (ExecutableSequence eSeq: positiveRegressionSeqs) {
			if (OperationClassifier.isModifier(eSeq.getLastStmtOperation())) 
				modifierRegressionSeqs.add(eSeq);
			else if (OperationClassifier.isObserver(eSeq.getLastStmtOperation()))
				observerRegressionSeqs.add(eSeq);
		}

		ArrayList<TypedOperation> simpleObservers = new ArrayList<>();
		ArrayList<TypedOperation> operationsPermutable = new ArrayList<>();
		for (TypedOperation op: operations) {
			if (OperationClassifier.isObserver(op)/* || OperationClassifier.isFinalObserver(op)*/) {
				if (fbg_repeat_observers == 1)
					operationsPermutable.add(op);
				else {
					assert fbg_repeat_observers > 0;
					if (OperationClassifier.isNoParamsOp(op)) 
						operationsPermutable.add(op);
					else {
						for (int i = 0; i < fbg_repeat_observers; i++) {
							operationsPermutable.add(op);
						}
					}
				}
			}
		}

		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("\n\n>> Second phase for modifiers starting...\n");

		extendModifierTestsWithObservers(modifierRegressionSeqs, operationsPermutable, false, false);

		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("\n\n>> Second phase for observers starting...\n");
		extendObserverTestsWithObserverOps(observerRegressionSeqs, operationsPermutable, true, true, false);

		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("\n\n>> Second phase finished.\n");

		long secondPhaseTime = System.currentTimeMillis() - secondPhaseStartTime;
		String secondPhaseTimeInSecs = (secondPhaseTime / 1000) + " s";
		long sec = (secondPhaseTime / 1000) % 60;
		long min = ((secondPhaseTime / 1000) / 60) % 60;
		long hr = (((secondPhaseTime / 1000) / 60) / 60);
		String secondPhaseTimeHumanReadable = hr + "h" + min + "m" + sec + "s";

		System.out.println("\n\n> Generation ended.\n\n");
		System.out.println("> First phase stats:");
		System.out.println("Positive tests saved: " + positiveTestsSaved);
		System.out.println("Extending reference fields positive tests: " + positiveReferenceExtendingTests);
		System.out.println("Not extending positive tests saved: " + notExtendingPositiveTestsSaved);
		System.out.println("Observer tests saved: " + observerPositiveTestsSaved);
		System.out.println("Positive tests discarded: " + positiveTestsDropped);
		System.out.println("Negative tests saved: " + negativeTestsSaved);
		System.out.println("Negative tests discarded: " + negativeTestsDropped);
		System.out.println("> Second phase stats:");
		System.out.println("Operations incorrectly deemed observers in the first phase: " + operationBadTag); 
		System.out.println("Positive sequences observed in the second phase (modifiers): " + genFirstAdditionalPositiveSeqs); 
		System.out.println("Negative sequences observed in the second phase (modifiers): " + genFirstAdditionalNegativeSeqs); 
		System.out.println("Error sequences observed in the second phase (modifiers): " + genFirstAdditionalErrorSeqs); 
		System.out.println("Total sequences observed in the second phase (modifiers): " + (genFirstAdditionalPositiveSeqs + genFirstAdditionalNegativeSeqs + genFirstAdditionalErrorSeqs)); 
		System.out.println("Positive sequences observed in the second phase (observers): " + genFirstAdditionalObsPositiveSeqs); 
		System.out.println("Negative sequences observed in the second phase (observers): " + genFirstAdditionalObsNegativeSeqs); 
		System.out.println("Error sequences observed in the second phase (observers): " + genFirstAdditionalObsErrorSeqs); 
		System.out.println("Total sequences observed in the second phase (observers): " + (genFirstAdditionalObsPositiveSeqs + genFirstAdditionalObsNegativeSeqs + genFirstAdditionalObsErrorSeqs)); 
		System.out.println("Total operations: " + operations.size()); 
		System.out.println("Second phase execution time: " + secondPhaseTimeInSecs);

		System.out.println(""); 
		System.out.println("Total discarded tests: " + discardedTests);
		System.out.println("Second phase execution time: " + secondPhaseTimeHumanReadable);
		System.out.println(""); 

		System.out.println();
		System.out.println("Normal method executions:" + ReflectionExecutor.normalExecs());
		System.out.println("Exceptional method executions:" + ReflectionExecutor.excepExecs());
		System.out.println();
		System.out.println(
				"Average method execution time (normal termination):      "
						+ String.format("%.3g", ReflectionExecutor.normalExecAvgMillis()));
		System.out.println(
				"Average method execution time (exceptional termination): "
						+ String.format("%.3g", ReflectionExecutor.excepExecAvgMillis()));

	} 
    

    if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
      progressDisplay.display();
      progressDisplay.shouldStop = true;
    }

    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
      System.out.println("Normal method executions: " + ReflectionExecutor.normalExecs());
      System.out.println("Exceptional method executions: " + ReflectionExecutor.excepExecs());
      System.out.println();
      System.out.println(
          "Average method execution time (normal termination):      "
              + String.format("%.3g", ReflectionExecutor.normalExecAvgMillis()));
      System.out.println(
          "Average method execution time (exceptional termination): "
              + String.format("%.3g", ReflectionExecutor.excepExecAvgMillis()));
    }

    // Notify listeners that exploration is ending.
    if (listenerMgr != null) {
      listenerMgr.explorationEnd();
    }
  }
  
  private /*List<ExecutableSequence>*/ void extendModifierTestsWithObservers(List<ExecutableSequence> modiferSequencesToExtend, 
		  List<TypedOperation> operationsPermutable, boolean observers, boolean ignoreMaxSize) {
	  // Generation first add one of each observer operation to the end of the sequence
	  //List<ExecutableSequence> result = new LinkedList<>();
	  for (ExecutableSequence eSeq: modiferSequencesToExtend) {
		  if (!ignoreMaxSize)
			  //assert eSeq.getLastStmtOperation().isModifier(): "We only extend modifiers in this method";
		  
		  if (stopSecondPhase()) { 
			  if (CanonicalizerLog.isLoggingOn())
				  CanonicalizerLog.logLine("\n> WARNING: Second Phase stopped due to time constraints");
			  System.out.println("WARNING: Second Phase stopped due to time constraints");
			  //return result;
			  break;
		  }
		  
		  if (CanonicalizerLog.isLoggingOn()) 
			  CanonicalizerLog.logLine("> Sequence to be extended\n" + eSeq.sequence.toCodeString());
		  
		  if (!field_based_gen_extend_subsumed && subsumed_sequences.contains(eSeq.sequence)) {
			  if (CanonicalizerLog.isLoggingOn()) 
				  CanonicalizerLog.logLine("> Current sequence is subsumed and will not be extended");
			  continue;
		  }
		  
		  // TODO: If the sequence ends with a method incorrectly deemed modifier continue to the next sequence
		  // Implement a method to get the last method of a sequence.
		  List<Integer> candidates = null;
		  if (eSeq.enlargesExtensions == ExtendExtensionsResult.EXTENDED) {
			  candidates = eSeq.getLastStmtActiveIndexes(); 
			  assert !candidates.isEmpty(): "The sequence extended extensions, it must have an active index";
		  }
		  else {
			  candidates = new ArrayList<>();
			  candidates.addAll(eSeq.getLastStmtNonPrimIndexes());
			  // It might be that the only non primitive object in the sequence is null. 
			  if (candidates.isEmpty()) {
				  // Try to extend the sequence as an observer sequence.
				  observerRegressionSeqs.add(eSeq);
				  if (CanonicalizerLog.isLoggingOn()) 
					  CanonicalizerLog.logLine("> Current sequence non primitives are null, there is nothing to observe.");
				  continue;
			  }
		  }

		  int j = Randomness.randomMember(candidates);
		  int currTestNegativeObservers = 0;
		  int currTestObservers = 0;
		  
		  ExecutableSequence currentSeq = eSeq;
		  List<Type> seqLastStmtTypes = eSeq.sequence.getTypesForLastStatement();
		  Type observedType = seqLastStmtTypes.get(j);
		  int observedIndex = j;

		  if (CanonicalizerLog.isLoggingOn()) {
			  //CanonicalizerLog.logLine("\n>> First phase modifier sequence to extend:\n " + neweSeq.sequence.toCodeString());
			  CanonicalizerLog.logLine("> Type to extend: " + observedType);
			  CanonicalizerLog.logLine("> Index of type to extend: " + observedIndex);
		  }

		  operationsPermutable = permuteOperationList(operationsPermutable);
		  
		  //resetExecutionResults(currentSeq, null);
		  
		  // Should randomly mix the operations list each time we do this to avoid always the same execution order.
		  for (TypedOperation operation: operationsPermutable) {
			  
			  /*
			  if (operation.toString().contains("<get>") || operation.toString().contains("<set>")) {
				  System.out.println(operation.toString());
				  continue;
				  //assert false;
			  }
			  */
			  
			  if ((!ignoreMaxSize && currTestObservers > fbg_observers_per_test) ||
					  (ignoreMaxSize && currTestObservers > fbg_simple_observers_per_test)) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> Maximum number of observers exceeded for the current test. Continue with the next test");
				  // This sequence is already too large, continue with the next sequence
				  break;
			  }
			  
			  if (CanonicalizerLog.isLoggingOn()) {
				  CanonicalizerLog.logLine("\n> Starting an attempt to extend sequence:\n " + currentSeq.sequence.toCodeString());
				  CanonicalizerLog.logLine("> with operation: " + operation.toString());
			  }

			  if (!OperationClassifier.isObserver(operation)/* && !OperationClassifier.isFinalObserver(operation)*/) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> The current operation has been flagged as a modifier in the second phase, don't use it to extend tests anymore.");
				  //System.out.println("> Operation " + operation.toString() + " has been flagged as a modifier by the field based approach, don't use it to build additional tests");
				  continue;
			  }
			  if (operation.isConstructorCall()) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> The current operation is a constructor, don't use it to extend tests");
				  continue;
			  }

			  // The first integer of the tuple is the index of the variable chosen from newSeq, 
			  // the second integer is the corresponding index of a compatible type in operation
			  TypeTuple inputTypes = operation.getInputTypes();
			  Integer opIndex = getRandomConnectionBetweenTypes(observedType, inputTypes);
			  if (opIndex == null) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> The operation cannot observe type " + observedType);
				  continue;
			  }

			  List<Sequence> sequences = new ArrayList<>();
			  int totStatements = 0;
			  List<Integer> variables = new ArrayList<>();
			  boolean error = false;
			  for (int i = 0; i < inputTypes.size(); i++) {
				  Type inputType = inputTypes.get(i);
				  Variable randomVariable;
				  if (CanonicalizerLog.isLoggingOn()) 
					  CanonicalizerLog.logLine("> Input type: " + inputType.toString());
				  Sequence chosenSeq;
				  if (i == opIndex) {
					  chosenSeq = currentSeq.sequence; 
					  randomVariable = chosenSeq.getVariablesOfLastStatement().get(observedIndex);
					  if (CanonicalizerLog.isLoggingOn()) {
						  CanonicalizerLog.logLine("> Current sequence selected as input: ");
						  CanonicalizerLog.logLine("> Random variable: " + randomVariable.getName() + "," + randomVariable.index);
					  }
				  }
				  else {
					  SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
					  
					  if (l.isEmpty()) {
						  error = true;
						  break;
					  }
					  chosenSeq = Randomness.randomMember(l);
					  // TODO: Should only choose active indexes according to field based
					  /* if (inputType.isObject() || inputType.isPrimitive() || inputType.isBoxedPrimitive() || 
							  inputType.isArray())
						  randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);
					  else
					  */
					  randomVariable = chosenSeq.randomVariableForTypeLastStatementFB(inputType);
					  if (CanonicalizerLog.isLoggingOn()) {
						  CanonicalizerLog.logLine("> Random sequence selected as input: ");
						  CanonicalizerLog.logLine(chosenSeq.toCodeString());
						  CanonicalizerLog.logLine("> Random variable: " + randomVariable.getName() + "," + randomVariable.index);
					  }
				  }
				  // Now, find values that satisfy the constraint set.
				  if (randomVariable == null) {
					  throw new BugInRandoopException("type: " + inputType + ", sequence: " + chosenSeq);
				  }
				  // Fail, if we were unlucky and selected a null or primitive value as the
				  // receiver for a method call.
				  if (i == 0
						  && operation.isMessage()
						  && !(operation.isStatic())
						  && (chosenSeq.getCreatingStatement(randomVariable).isPrimitiveInitialization()
								  || randomVariable.getType().isPrimitive())) {
					  error = true;
					  break;
				  }
				  variables.add(totStatements + randomVariable.index);
				  sequences.add(chosenSeq);
				  totStatements += chosenSeq.size();
			  }

			  if (error) continue;
			  InputsAndSuccessFlag isequences = new InputsAndSuccessFlag(true, sequences, variables);
			  
			  int [] startIndexRef = {0};
			  Sequence concatSeq = Sequence.concatenateAndGetIndexes(isequences.sequences, currentSeq.sequence, startIndexRef);
			  int startIndex = startIndexRef[0];
			  int endIndex = startIndex + currentSeq.sequence.size()-1;
			  
			  if (CanonicalizerLog.isLoggingOn()) {
				  //CanonicalizerLog.logLine("> Concatenation result: \n" + concatSeq.toCodeString());
				  //CanonicalizerLog.logLine("> Indexed sequence: \n" + neweSeq.sequence.toCodeString());
				  CanonicalizerLog.logLine("> Indexes of the sequence to be extended within the concatenation result: " + startIndex + ", " + endIndex);
			  }
			  // Figure out input variables.
			  List<Variable> inputs = new ArrayList<>();
			  for (Integer oneinput : isequences.indices) {
				  Variable v = concatSeq.getVariable(oneinput);
				  inputs.add(v);
			  }
			  Sequence newSequence = concatSeq.extend(operation, inputs);
  			  if (CanonicalizerLog.isLoggingOn())
				  CanonicalizerLog.logLine("> Resulting sequence: \n" + newSequence.toCodeString()); 

			  // Discard if sequence is larger than size limit
  			  if (!ignoreMaxSize && newSequence.size() > GenInputsAbstract.maxsize) {
				  if (Log.isLoggingOn()) {
					  Log.logLine(
							  "Sequence discarded because size "
									  + newSequence.size()
									  + " exceeds maximum allowed size "
									  + GenInputsAbstract.maxsize);
				  }

				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> Current sequence is too large. Try another one"); 
				  // This sequence is too large, try another one 
				  // TODO: This might be inefficient, maybe we should break here
				  break;
			  }
			  
			  num_sequences_generated++;
			  
			  /*
			  if (this.allSequences.contains(newSequence)) {
				  if (Log.isLoggingOn()) {
					  Log.logLine("Sequence discarded because the same sequence was previously created.");
				  }
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> The current sequence was generated twice in the second phase \n"); 
				  continue;
			  }

			  this.allSequences.add(newSequence);	
			  */

			  
			  ExecutableSequence extendedSeq = new ExecutableSequence(newSequence);
			  //resetExecutionResults(currentSeq, extendedSeq);
			  executeExtendedSequenceNoReexecute(extendedSeq, currentSeq, startIndex, endIndex);

			  //processLastSequenceStatement(eSeq2ndPhase);

			  if (extendedSeq.isNormalExecution() && OperationClassifier.isModifier(operation)) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> Operation " + operation.toString() + " was incorrectly flagged as observer in the first phase.");
				  // Don't extend this test" ); 
				  operationBadTag++;
				  /* Keep the test and continue extending it for now.
				  eSeq2ndPhase.clearExtensions();
				  eSeq2ndPhase.clearExecutionResults();
				  continue;
				  */
			  }
			  /*
			  if (eSeq2ndPhase.isNormalExecution() && OperationClassifier.isModifier(operation)) {
				  CanonicalizerLog.logLine("> Operation " + operation.toString() + " was incorrectly flagged as observer in the first phase. Don't extend this test" ); 
				  operationBadTag++;
				  eSeq2ndPhase.clearExtensions();
				  eSeq2ndPhase.clearExecutionResults();
				  continue;
			  }
			  */

			  if (extendedSeq.hasFailure()) {
				  num_failing_sequences++;
			  }

			  if (outputTest.test(extendedSeq)) {

				  if (!extendedSeq.hasInvalidBehavior()) {
					  if (extendedSeq.hasFailure()) {
						  outErrorSeqs.add(extendedSeq);
						  if (!observers)
							  genFirstAdditionalErrorSeqs++;
						  else
							  genFirstAdditionalObsErrorSeqs++;

						  if (CanonicalizerLog.isLoggingOn()) 
							  CanonicalizerLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");

						  resetExecutionResults(currentSeq, extendedSeq);
					  } 
					  else {
						  // outRegressionSeqs.add(eSeq2ndPhase);
						  if (extendedSeq.isNormalExecution()) {
							  currTestObservers++;
							  
							  if (CanonicalizerLog.isLoggingOn()) 
								  CanonicalizerLog.logLine("> Current sequence saved as a regression test");
							  // continue extending this sequence;
							  currentSeq = extendedSeq;

							  // opIndex was calculated over operation's input types. observedIndex must be calculated over the output types of the operation 
							  if (operation.getOutputType().isVoid())
								  observedIndex = opIndex;
							  else
								  observedIndex = opIndex + 1;

							  assert currentSeq.sequence.getTypesForLastStatement().get(observedIndex).equals(observedType);
							  
							  if (!observers)
								  genFirstAdditionalPositiveSeqs++;
							  else
								  genFirstAdditionalObsPositiveSeqs++;
							  
							  // outRegressionSeqs.add(eSeq2ndPhase);
							  // result.add(extendedSeq);
							  // Add newSequence's inputs to subsumed_sequences
							  for (Sequence subsumed: isequences.sequences) {
								  subsumed_sequences.add(subsumed);
							  }	
							  
						  }
						  else {
							  if (currTestNegativeObservers < 0 /*field_based_gen_negative_observers_per_test */) {
								  currTestNegativeObservers++;
								  if (CanonicalizerLog.isLoggingOn()) 
									  CanonicalizerLog.logLine("> Current sequence saved as a negative regression test");

								  if (!observers)
									  genFirstAdditionalNegativeSeqs++;
								  else
									  genFirstAdditionalObsNegativeSeqs++;
								  
								  outRegressionSeqs.add(extendedSeq);
								  //result.add(extendedSeq);
								  // Add newSequence's inputs to subsumed_sequences
								  for (Sequence subsumed: isequences.sequences) {
									  subsumed_sequences.add(subsumed);
								  }	
							  }
							  else {
								  if (CanonicalizerLog.isLoggingOn()) 
									  CanonicalizerLog.logLine("> Negative observers per test limit exceeded. Discarding current sequence");

							  }
							  // Results are invalidated by the method that thrown the exception.
							  resetExecutionResults(currentSeq, extendedSeq);
						  } 
					  }
				  }
				  else {
					  /*
					  System.out.println("> ERROR: Sequence with invalid behavior in the second phase:");
					  System.out.println(eSeq2ndPhase.toCodeString());
					  */
					  if (CanonicalizerLog.isLoggingOn()) {
						  CanonicalizerLog.logLine("> ERROR: Sequence with invalid behaviour in the second phase:");
						  CanonicalizerLog.logLine(extendedSeq.toCodeString());
					  }
					  if (!observers)
						  genFirstAdditionalErrorSeqs++;
					  else
						  genFirstAdditionalObsErrorSeqs++;
					  
					  resetExecutionResults(currentSeq, extendedSeq);
				  }
			  }
			  else {
				  /*
				  System.out.println("> ERROR: Failing sequence in the second phase:");
				  System.out.println(eSeq2ndPhase.toCodeString());
				  */
				  if (CanonicalizerLog.isLoggingOn()) {
					  CanonicalizerLog.logLine("> ERROR: Failing sequence in the second phase:");
					  CanonicalizerLog.logLine(extendedSeq.toCodeString());
				  }
				  if (!observers)
					  genFirstAdditionalErrorSeqs++;
				  else
					  genFirstAdditionalObsErrorSeqs++;
				  
				  resetExecutionResults(currentSeq, extendedSeq);
			  }

		  }
		  
		  currentSeq.clearExecutionResults();
		  if (!currentSeq.equals(eSeq)) {
			  if (CanonicalizerLog.isLoggingOn())
				  CanonicalizerLog.logLine("\n>> Final extended modifier sequence:\n " + currentSeq.toCodeString());
			  outRegressionSeqs.add(currentSeq);
			  //result.add(currentSeq);
		  }
	  }
	  //return result;
  }
  
  
  
	public void executeExtendedSequenceNoReexecute(ExecutableSequence eSeq, ExecutableSequence extendedSeq, int startIndex,
			int endIndex) {
		// TODO Auto-generated method stub
		
	}
  
  private Integer getRandomConnectionBetweenTypes(Type observedType, TypeTuple inputTypes) {
	  List<Integer> l = new ArrayList<>();

	  for (int j = 0; j < inputTypes.size(); j++) {
		  if (inputTypes.get(j).isAssignableFrom(observedType)) {
			  l.add(j);
		  }
	  }

	  if (l.isEmpty())
		  return null;
	  else {
		  Integer res = Randomness.randomMember(l);
		  return res;
	  }
  }


  private List<TypedOperation> permuteOperationList(List<TypedOperation> operationsPermutable) {
	  RandomPerm.randomPermutation(operationsPermutable);
	  if (fbg_2nd_phase_simple_obs_detection) {
		  List<TypedOperation> obs = new LinkedList<>();
		  List<TypedOperation> simpleObs = new LinkedList<>();
		  for (TypedOperation op: operationsPermutable) {
			  if (OperationClassifier.isSimpleOp(op))
				  simpleObs.add(op);
			  else
				  obs.add(op);
		  }
		  simpleObs.addAll(obs);
		  operationsPermutable = simpleObs;
	  }
	  return operationsPermutable;
  }


  private void resetExecutionResults(ExecutableSequence currentSeq, ExecutableSequence extendedSeq) {
	  if (extendedSeq != null)
		  extendedSeq.clearExecutionResults();
	  currentSeq.clearExecutionResults();
  } 
  
  
  protected boolean stopSecondPhase() {
	  //return timer.getTimeElapsedMillis() >= maxTimeMillis;
	  return timer.getTimeElapsedMillis() >= secondPhaseMaxTimeMillis;
  }
  public final long secondPhaseMaxTimeMillis;

  
  private /*List<ExecutableSequence>*/ void extendObserverTestsWithObserverOps(List<ExecutableSequence> sequencesToExtend, 
			List<TypedOperation> operationsPermutable, boolean extendOnlyPrimitive, boolean observers, boolean ignoreMaxSize) {
		  // Generation first add one of each observer operation to the end of the sequence
		  //List<ExecutableSequence> result = new LinkedList<>();
		  for (ExecutableSequence eSeq: sequencesToExtend) {
			  // We might add a modifier sequence here by mistake
//			  assert eSeq.getLastStmtOperation().isObserver() || eSeq.getLastStmtOperation().isFinalObserver(): 
//				  "We only extend observers in this method and " + eSeq.getLastStmtOperation().toString() + 
//				  " is " + eSeq.getLastStmtOperation().fbExecState;
			  //assert !eSeq.getLastStmtOperation().notExecuted(): "Operation must have been executed at this point";
			  
			  if (stopSecondPhase()) { 
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("\n> WARNING: Second Phase stopped due to time constraints");

				  System.out.println("WARNING: Second Phase stopped due to time constraints");
				  
//				  return result;
				  break;
			  }

			  if (CanonicalizerLog.isLoggingOn()) 
				  CanonicalizerLog.logLine("> Sequence to be extended\n" + eSeq.sequence.toCodeString());
			  
			  if (!field_based_gen_extend_subsumed && subsumed_sequences.contains(eSeq.sequence)) {
				  if (CanonicalizerLog.isLoggingOn()) 
					  CanonicalizerLog.logLine("> Current sequence is subsumed and will not be extended");
				  continue;
			  }

			  ExecutableSequence currentSeq = eSeq;

			  // TODO: If the sequence ends with a method incorrectly deemed modifier continue to the next sequence
			  // Implement a method to get the last method of a sequence.
			  int currTestNegativeObservers = 0;
			  int currTestObservers = 0;
			  operationsPermutable = permuteOperationList(operationsPermutable);
			  
			  //resetExecutionResults(currentSeq, null);
			  
			  // Should randomly mix the operations list each time we do this to avoid always the same execution order.
			  for (TypedOperation operation: operationsPermutable) {
				  		  
				  /*
				  if (operation.toString().contains("<get>") || operation.toString().contains("<set>")) {
					  System.out.println(operation.toString());
					  //assert false;
				  }
				  */
				  if ((!ignoreMaxSize && currTestObservers > fbg_observers_per_test) ||
						  (ignoreMaxSize && currTestObservers > fbg_simple_observers_per_test)) {
					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine("> Maximum number of observers exceeded for the current test. Continue with the next test");
					  // This sequence is already too large, continue with the next sequence
					  break;
				  }
				  
				  if (CanonicalizerLog.isLoggingOn()) {
					  CanonicalizerLog.logLine("\n> Starting an attempt to extend sequence:\n " + currentSeq.sequence.toCodeString());
					  CanonicalizerLog.logLine("> with operation: " + operation.toString());
				  }
				  if (!OperationClassifier.isObserver(operation)/* && !OperationClassifier.isFinalObserver(operation)*/) {
					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine("> Current operation flagged as a modifier in the second phase, don't use it to extend tests anymore");
					  //System.out.println("> Operation " + operation.toString() + " has been flagged as a modifier by the field based approach, don't use it to build additional tests");
					  continue;
				  }
				  if (operation.isConstructorCall()) {
					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine("> Current operation is a constructor, don't use it to extend tests");
					  continue;
				  }

				  int lastIndex = currentSeq.sequence.size() - 1;
				  TypedOperation seqLastStmtOp = currentSeq.sequence.getStatement(lastIndex).getOperation();

				  TypeTuple seqLastStmtInputTypes = seqLastStmtOp.getInputTypes();
				  TypeTuple seqLastStmtOutputTypes = new TypeTuple();

				  List<Boolean> isLastStmtVarActive = currentSeq.getLastStmtNonPrimIndexesList();
				  if (!seqLastStmtOp.getOutputType().isVoid())
					  seqLastStmtOutputTypes.list.add(seqLastStmtOp.getOutputType());
				  for (int i = 0; i < seqLastStmtInputTypes.size(); i++)
					  seqLastStmtOutputTypes.list.add(seqLastStmtInputTypes.get(i));

				  // The first integer of the tuple is the index of the variable chosen from newSeq, 
				  // the second integer is the corresponding index of a compatible type in operation
				  TypeTuple inputTypes = operation.getInputTypes();
				  Tuple<Integer, Integer> connection = getRandomConnectionBetweenTypeTuples(seqLastStmtOutputTypes, isLastStmtVarActive, inputTypes, extendOnlyPrimitive);
				  if (connection == null) {
					  connection = getRandomConnectionBetweenTypeTuples(seqLastStmtOutputTypes, isLastStmtVarActive, inputTypes, false);
					  if (connection == null)
						  continue;
				  }
				  
				  List<Sequence> sequences = new ArrayList<>();
				  int totStatements = 0;
				  List<Integer> variables = new ArrayList<>();

				  boolean error = false;
				  for (int i = 0; i < inputTypes.size(); i++) {
					  Type inputType = inputTypes.get(i);
					  /* if (CanonicalizerLog.isLoggingOn()) 
						  CanonicalizerLog.logLine("> Input type: " + inputType.toString()); */

					  Sequence chosenSeq;
					  if (i == connection.getSecond()) {
						  chosenSeq = currentSeq.sequence; 

						  /* if (CanonicalizerLog.isLoggingOn()) 
							  CanonicalizerLog.logLine("> Sequence to be extended selected: "); */
					  }
					  else {
						  SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
						  if (l.isEmpty()) {
							  error = true;
							  break;
						  }

						  chosenSeq = Randomness.randomMember(l);

						  /* if (CanonicalizerLog.isLoggingOn()) 
							  CanonicalizerLog.logLine("> Random sequence selected: "); */
					  }

					  /* if (CanonicalizerLog.isLoggingOn()) 
						  CanonicalizerLog.logLine(chosenSeq.toCodeString()); */

					  // Now, find values that satisfy the constraint set.
					  Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);

					  if (randomVariable == null) {
						  error = true;
						  break;
						  //throw new BugInRandoopException("type: " + inputType + ", sequence: " + chosenSeq);
					  }

					  // Fail, if we were unlucky and selected a null or primitive value as the
					  // receiver for a method call.
					  if (i == 0
							  && operation.isMessage()
							  && !(operation.isStatic())
							  && (chosenSeq.getCreatingStatement(randomVariable).isPrimitiveInitialization()
									  || randomVariable.getType().isPrimitive())) {

						  error = true;
						  break;
					  }

					  variables.add(totStatements + randomVariable.index);
					  sequences.add(chosenSeq);
					  totStatements += chosenSeq.size();
				  }

				  if (error) continue;
				  InputsAndSuccessFlag isequences = new InputsAndSuccessFlag(true, sequences, variables);

				  int [] startIndexRef = {0};
				  Sequence concatSeq = Sequence.concatenateAndGetIndexes(isequences.sequences, currentSeq.sequence, startIndexRef);
				  int startIndex = startIndexRef[0];
				  int endIndex = startIndex + currentSeq.sequence.size() -1;
				  
				  /*
				  if (CanonicalizerLog.isLoggingOn()) {
//					  CanonicalizerLog.logLine("> Concatenation result: \n" + concatSeq.toCodeString());
//					  CanonicalizerLog.logLine("> Indexed sequence: \n" + neweSeq.sequence.toCodeString());
					  CanonicalizerLog.logLine("> Indexes of the sequence to be extended within the concatenation result: " + startIndex + ", " + endIndex);
				  }
				  */
				  // Sequence concatSeq = Sequence.concatenate(isequences.sequences);

				  // Figure out input variables.
				  List<Variable> inputs = new ArrayList<>();
				  for (Integer oneinput : isequences.indices) {
					  Variable v = concatSeq.getVariable(oneinput);
					  inputs.add(v);
				  }

				  Sequence newSequence = concatSeq.extend(operation, inputs);

				  // Discard if sequence is larger than size limit
				  if (!ignoreMaxSize && newSequence.size() > GenInputsAbstract.maxsize) {
					  if (Log.isLoggingOn()) {
						  Log.logLine(
								  "Sequence discarded because size "
										  + newSequence.size()
										  + " exceeds maximum allowed size "
										  + GenInputsAbstract.maxsize);
					  }

					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine("> Current sequence is too large. Try another one"); 
					  // This sequence is too large, try another one 
					  // TODO: This might be inefficient, maybe we should break here
					  break;
				  }

				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("> Resulting sequence: \n" + newSequence.toCodeString()); 

				  num_sequences_generated++;

				  /*
				  if (this.allSequences.contains(newSequence)) {
					  if (Log.isLoggingOn()) {
						  Log.logLine("Sequence discarded because the same sequence was previously created.");
					  }

					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine("> The current sequence was generated twice in the second phase \n"); 

					  continue;
				  }

				  this.allSequences.add(newSequence);	
				  */

				  ExecutableSequence extendedSeq = new ExecutableSequence(newSequence);
				  //resetExecutionResults(currentSeq, extendedSeq);
				  executeExtendedSequenceNoReexecute(extendedSeq, currentSeq, startIndex, endIndex);
				  //processLastSequenceStatement(eSeq2ndPhase);

				  if (extendedSeq.isNormalExecution() && OperationClassifier.isModifier(operation)) {
					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine("> Operation " + operation.toString() + " was incorrectly flagged as observer in the first phase.");
					  // Don't extend this test" ); 
					  operationBadTag++;
					  /* Keep the test and continue extending it for now.
					  eSeq2ndPhase.clearExtensions();
					  eSeq2ndPhase.clearExecutionResults();
					  continue;
					  */
				  }

				  if (extendedSeq.hasFailure()) {
					  num_failing_sequences++;
				  }

				  if (outputTest.test(extendedSeq)) {

					  if (!extendedSeq.hasInvalidBehavior()) {
						  if (extendedSeq.hasFailure()) {
							  outErrorSeqs.add(extendedSeq);
							  if (!observers)
								  genFirstAdditionalErrorSeqs++;
							  else
								  genFirstAdditionalObsErrorSeqs++;

							  if (CanonicalizerLog.isLoggingOn()) 
								  CanonicalizerLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");

							  resetExecutionResults(currentSeq, extendedSeq);
						  } 
						  else {
							  //outRegressionSeqs.add(eSeq2ndPhase);
							  if (extendedSeq.isNormalExecution()) {
								  currTestObservers++;
								  
								  if (CanonicalizerLog.isLoggingOn()) 
									  CanonicalizerLog.logLine("> Current sequence saved as a regression test");
								  // continue extending this sequence;
								  currentSeq = extendedSeq;
								  if (!observers)
									  genFirstAdditionalPositiveSeqs++;
								  else
									  genFirstAdditionalObsPositiveSeqs++;

								  // outRegressionSeqs.add(eSeq2ndPhase);
								  // Add newSequence's inputs to subsumed_sequences
								  for (Sequence subsumed: isequences.sequences) {
									  subsumed_sequences.add(subsumed);
								  }	
							  }
							  else {
								  if (currTestNegativeObservers < 0 /*field_based_gen_negative_observers_per_test*/) {
									  currTestNegativeObservers++;
									  if (CanonicalizerLog.isLoggingOn()) 
										  CanonicalizerLog.logLine("> Current sequence saved as a negative regression test");

									  if (!observers)
										  genFirstAdditionalNegativeSeqs++;
									  else
										  genFirstAdditionalObsNegativeSeqs++;
									  
									  outRegressionSeqs.add(extendedSeq);
									  //result.add(extendedSeq);
									  // Add newSequence's inputs to subsumed_sequences
									  for (Sequence subsumed: isequences.sequences) {
										  subsumed_sequences.add(subsumed);
									  }	
								  }
								  else {
									  if (CanonicalizerLog.isLoggingOn()) 
										  CanonicalizerLog.logLine("> Negative observers per test limit exceeded. Discarding current sequence");
								  }

								  resetExecutionResults(currentSeq, extendedSeq);
							  } 
							  
						  }
					  }
					  else {

						  if (CanonicalizerLog.isLoggingOn()) {
							  CanonicalizerLog.logLine("> ERROR: Sequence with invalid behaviour in the second phase:");
							  CanonicalizerLog.logLine(extendedSeq.toCodeString());
						  }
						  if (!observers)
							  genFirstAdditionalErrorSeqs++;
						  else
							  genFirstAdditionalObsErrorSeqs++;
					  
						  resetExecutionResults(currentSeq, extendedSeq);
					  }
				  }
				  else {
					  if (CanonicalizerLog.isLoggingOn()) {
						  CanonicalizerLog.logLine("> ERROR: Failing sequence in the second phase:");
						  CanonicalizerLog.logLine(extendedSeq.toCodeString());
					  }
					  if (!observers)
						  genFirstAdditionalErrorSeqs++;
					  else
						  genFirstAdditionalObsErrorSeqs++;

					  resetExecutionResults(currentSeq, extendedSeq);
				  }
				  
			  }

			  //neweSeq.clearExtensions();
			  currentSeq.clearExecutionResults();
			  if (!currentSeq.equals(eSeq)) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("\n>> Final extended observer sequence:\n " + currentSeq.toCodeString());
				  //result.add(currentSeq);
				  outRegressionSeqs.add(currentSeq);
			  }
		  }
		  
	  }


  
  private Tuple<Integer, Integer> getRandomConnectionBetweenTypeTuples(TypeTuple seqLastStmtOutputTypes, List<Boolean> isLastStmtVarActive, TypeTuple inputTypes, boolean extendOnlyPrimitive) {
	  
	  List<Tuple<Integer, Integer>> l = new ArrayList<>();
	  for (int i = 0; i < seqLastStmtOutputTypes.size(); i++) {
		  if (extendOnlyPrimitive && isLastStmtVarActive.get(i)) 
			  continue;
		  
		  for (int j = 0; j < inputTypes.size(); j++) {
			  if (inputTypes.get(j).isAssignableFrom(seqLastStmtOutputTypes.get(i))) {
				  l.add(new Tuple<Integer, Integer>(i,j));
			  }
		  }
	  }
	  
	  if (l.isEmpty())
		  return null;
	  else {
		  Tuple<Integer, Integer> res = Randomness.randomMember(l);
		  return res;
	  }
  }
  
  
  
  
  
  
  
  
  
  
  
  
  

  /**
   * Return all sequences generated by this object.
   *
   * @return return all generated sequences
   */
  public abstract Set<Sequence> getAllSequences();

  /**
   * Returns the set of sequences that are used as inputs in other sequences
   * (and can thus be thought of as subsumed by another sequence). This should
   * only be called for subclasses that support this.
   *
   * @return the set of sequences subsumed by other sequences
   */
  public Set<Sequence> getSubsumedSequences() {
    throw new Error("subsumed_sequences not supported for " + this.getClass());
  }

  /**
   * Returns the generated regression test sequences for output. Filters out
   * subsequences, which can be retrieved using {@link #getSubsumedSequences()}
   *
   * @return regression test sequences that do not occur in a longer sequence
   */
  // TODO replace this with filtering during generation
  public List<ExecutableSequence> getRegressionSequences() {
    List<ExecutableSequence> unique_seqs = new ArrayList<>();
    Set<Sequence> subsumed_seqs = this.getSubsumedSequences();
    for (ExecutableSequence es : outRegressionSeqs) {
      if (!subsumed_seqs.contains(es.sequence)) {
        unique_seqs.add(es);
      }
    }
    return unique_seqs;
  }

  /**
   * Returns the generated error-revealing test sequences for output.
   *
   * @return the generated error test sequences
   */
  public List<ExecutableSequence> getErrorTestSequences() {
    return outErrorSeqs;
  }

  /**
   * Returns the total number of test sequences generated to output, including
   * both regression tests and error-revealing tests.
   *
   * @return the total number of test sequences saved for output
   */
  public int outputSequenceCount() {
    return outRegressionSeqs.size() + outErrorSeqs.size();
  }

  /**
   * Sets the current sequence during exploration
   *
   * @param s
   *          the current sequence
   */
  protected void setCurrentSequence(Sequence s) {
    currSeq = s;
  }
}
