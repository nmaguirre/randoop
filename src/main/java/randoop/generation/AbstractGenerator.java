package randoop.generation;

import plume.Option;
import plume.OptionGroup;
import plume.Unpublicized;
import randoop.*;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Value;
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
import randoop.util.fieldbasedcontrol.CanonizationErrorException;
import randoop.util.fieldbasedcontrol.CanonizerClass;
import randoop.util.fieldbasedcontrol.FieldBasedGenLog;
import randoop.util.fieldbasedcontrol.FieldExtensionsIndexes;
import randoop.util.fieldbasedcontrol.HeapCanonizerRuntimeEfficient;
import randoop.util.fieldbasedcontrol.MurmurHash3;
import randoop.util.fieldbasedcontrol.MurmurHash3.LongPair;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.ExtendExtensionsResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVector;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorGenerator;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorsFieldExtensions;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorsWriter;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensions;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsByType;
import randoop.util.fieldbasedcontrol.RandomPerm;
import randoop.util.fieldbasedcontrol.Tuple;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
	
	public static CanonicalStore store;
	public static HeapCanonicalizer newCanonicalizer;
	public static FieldExtensions globalExtensions;
	public static CandidateVectorGenerator candVectGenerator;
	public static CandidateVectorsFieldExtensions candVectExtensions;
	
	public void initNewCanonicalizer(Collection<String> classNames, int maxObjects, int fieldDistance) {
		
	    store = new CanonicalStore(classNames);
		
		newCanonicalizer = new HeapCanonicalizer(store, maxObjects, fieldDistance);
		globalExtensions = new FieldExtensionsByType();
		// Initialize the candidate vector generator with the canonical classes that were mined from the code,
		// before the generation starts.
		if (CandidateVectorsWriter.isEnabled()) {
			candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
			CanonicalHeap heap = new CanonicalHeap(store, maxObjects);
			CandidateVector<String> header = AbstractGenerator.candVectGenerator.makeCandidateVectorsHeader(heap);
			AbstractGenerator.candVectExtensions = new CandidateVectorsFieldExtensions(header);
			CandidateVectorsWriter.logLine(header.toString());
		}
	}
	
  // The set of all primitive values seen during generation and execution
  // of sequences. This set is used to tell if a new primitive value has
  // been generated, to add the value to the components.
  protected Set<Object> runtimePrimitivesSeen = new LinkedHashSet<>();	
	
	
  protected Set<Sequence> allSequences;

 
  public enum FieldBasedGenType {
	    /** Field based generation is disabled */
	    DISABLED,
	    // Field based generation
	    EXTENSIONS,
	    // Hashes as proxy for object equality
	    HASHES
//	    MIN,
//	    MINPRECISE
	  }
  
  
  public enum CountType {
	    DISABLED,
	    EXTENSION,
	    HASH
  }
  /*
   @Option("Choose a field based generation approach. Field based generation can be done "
  		+ "considering only the last sentence of each test (value: FAST), or taking into"
  		+ "account all the sentences of each test (value: MIN). The latter approach can "
  		+ "be made more precise at the expense of a slower runtime (value: MINPRECISE) "
  		+ "The minimization approach is slower but it might produce fewer and better tests"
  		+ "(they tend to produce better results in testing evaluation metrics)."  
  		+ "Field based generation can also be disabled (value: DISABLED)."
  		+ "")
  	*/
 
  @OptionGroup("Field based generation")  
  @Option("Select a generation approach. "
  		+ "DISABLED: Original randoop. "
  		+ "EXTENSIONS: Field based generation. "
  		+ "HASHES: Use hashes as a proxy for object equality")
  public static FieldBasedGenType field_based_gen = FieldBasedGenType.EXTENSIONS;
  
  @Option("Ignore primitive values in the construction of field extensions")
  public static boolean field_based_gen_ignore_primitive = false;
  
  @Option("Only consider strings with up to this number of elements for augmenting the extensions")
  public static int field_based_gen_max_string_length = 5000;

  @Option("Only consider arrays with up to this number of elements for augmenting the extensions")
  public static int field_based_gen_max_array = 5000;

  @Option("Only store up to this number of objects during canonization")
  public static int field_based_gen_max_objects = 5000;
  
  @Option("Do not canonicalize structures having more than this number of objects of a single reference type.")
  public static int fbg_max_objects = Integer.MAX_VALUE;

  @Option("Only canonicalize objects reachable by this number of field traversals from the structure's root.")
  public static int fbg_field_distance = Integer.MAX_VALUE;

  @Option("Representation of the null value in candidate vectors")
  public static int cand_vect_null_rep = 0;//Integer.MIN_VALUE;
  
  @Option("Only store up to this number of objects for each individual class during canonization")
  public static int field_based_gen_max_class_objects = 5000;

  @Option("Set to false to not allow tests exceeding object/array/string limits to be used as inputs for "
  		+ "other tests")
  public static boolean field_based_gen_drop_tests_exceeding_object_limits = false;

  @Option("Disable randoop's collections and arrays generation heuristic")
  public static boolean disable_collections_generation_heuristic = false;

  @Option("Allows field based generation to detect precisely which objects enlarge the extensions."
  		+ " This may negatively affect runtime performance.")
  public static boolean fbg_precise_extension_detection = true;

  @Option("Save tests ending with observers.")
  public static boolean field_based_gen_save_observers = true;

  @Option("Number of negative tests to save during the second phase.")
  public static int field_based_gen_negative_observers_per_test = 0; 
  
  @Option("Max number of observers to be added to each test.")
  public static int field_based_gen_observers_per_test = 50; 
  
  @Option("Max number of lines reserved for observers from the max number of lines given by the --maxsize parameter.")
  public static int field_based_gen_reserved_observer_lines = 0;//200; 
  
  @Option("Max times an observer must be used in tests to flag it final.")
  public static int field_based_gen_observer_executions_before_final = 20;

  @Option("Max times a modifier can be executed in a test not extending the extensions.")
  public static int field_based_gen_non_extending_modifiers_ratio = 1000;
  
  @Option("Save test only if the last operation was used at most (#test gen./fbg_not_extending_ops_ratio)+1 times in tests"
  		+ "not extending the extensions.")
  public static int fbg_save_not_extending_ratio = 500;

  @Option("Max times an observer can be executed in a test not extending the extensions.")
  public static int field_based_gen_max_non_extending_observer_tests_ratio = 500;
  
  @Option("Save negative test only if the last operation was used at most (#test gen./fbg_save_negatives_ratio)+1 times in tests"
	  		+ "not extending the extensions.")
  public static int fbg_save_negatives_ratio = 500;

  @Option("Generation stops when this many tests are discarded.")
  public static int max_discarded_tests = 100000;

  @Option("Second phase TO.")
  public static int field_based_gen_second_phase_timeout_millis = 300000;
 
  @Option("Count the number of different objects generated by tests. For the field based approach it counts the number of "
  		+ "objects generated during the first phase (it doesn't continue with the second phase.")
  public static boolean count_objects = false; 

  @Option("Extend tests with observers.")
  public static boolean field_based_gen_extend_with_observers = false; 

  @Option("Drop a percentage of negative tests generated by randoop.")
  public static boolean randoop_drop_negatives = false; 

  @Option("Extend subsumed tests during the second phase.")
  public static boolean field_based_gen_extend_subsumed = false;
  
  @Option("Increase the probabilities of randomly selecting methods that contribute more frequently to the field extensions")
  public static boolean field_based_gen_weighted_selection = false; 
  @Option("Increment the weight of an action by this ammount each time it contributes to the extensions")
  public static int weight_increment = 10;
  @Option("Decrement the weight of an action by this ammount each time it does not contribute to the extensions")
  public static int weight_decrement = 0;
  @Option("Starting weight of all actions in weighted selection")
  public static int starting_weight = 10; 
  @Option("Smaller weight for an action in weighted selection")
  public static int smaller_weight = 10;
  @Option("Larger weight for an action in weighted selection")
  public static int larger_weight = 100;  
  @Option("Only log extensions with up to this size. Avoids very large log files")
  public static int max_extensions_size_to_log = 1000;  
   
  
  @OptionGroup(value = "AbstractGenerator unpublicized options", unpublicized = true)
  @Unpublicized
  @Option("Dump each sequence to the log file")
  public static boolean dump_sequences = false;

  @RandoopStat(
      "Number of generation steps (one step consists of an attempt to generate and execute a new, distinct sequence)")
  public int num_steps = 0;

  @RandoopStat("Number of sequences generated.")
  public int num_sequences_generated = 0;

  @RandoopStat("Number of sequences generated that reveal a failure.")
  public int num_failing_sequences = 0;

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
  public final long secondPhaseMaxTimeMillis;

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
//  public Set<TypedOperation> modifierOps;
  
  protected Set<Sequence> subsumed_sequences = new LinkedHashSet<>();

  protected Set<Sequence> subsumed_candidates; 

  
  // PABLO: Structures to add more weight to operations more frequently used
  // in random selection.
  public LinkedHashMap<TypedOperation, Integer> operationsWeight;
  
  public int sumOfWeights;

  public void setInitialOperationWeights() {
	  sumOfWeights = 0;
	  operationsWeight = new LinkedHashMap<>();
	  for (TypedOperation t: operations) {
		  operationsWeight.put(t, starting_weight);
	  }
	  sumOfWeights = starting_weight * operations.size();	  
  }
  
/*  public void initModifierOperationsHash() {
	  modifierOps = new HashSet<>();
  }
  */
  
  public void increaseWeight(TypedOperation t) {
	Integer currValue = operationsWeight.get(t);
	if (currValue + weight_increment <= larger_weight) {
		operationsWeight.put(t, currValue + weight_increment);
		sumOfWeights += weight_increment;
	}
	else {
		int diff = larger_weight - currValue;
		if (diff > 0) {		
			operationsWeight.put(t, larger_weight);
			sumOfWeights += diff;		
		}
	}

	
  }
  
  public void decreaseWeight(TypedOperation t) {
	Integer currValue = operationsWeight.get(t);
	if (currValue - weight_decrement >= smaller_weight) {
		operationsWeight.put(t, currValue - weight_decrement);
		sumOfWeights -= weight_decrement;	
	}
	else {
		int diff = currValue - smaller_weight;
		if (diff > 0) {		
			operationsWeight.put(t, smaller_weight);
			sumOfWeights -= diff;		
		}
	}
  }
  
  public Integer getWeight(TypedOperation t) {
	  return operationsWeight.get(t);
  } 

    
  public TypedOperation selectWeightedRandomOperation() {
	int randomPoint = Randomness.nextRandomInt(sumOfWeights);
    int currentPoint = 1;
    for (TypedOperation o: operationsWeight.keySet()) {
      currentPoint += operationsWeight.get(o);
      if (currentPoint >= randomPoint) {
        return o;
      }
    }
    throw new BugInRandoopException();
  }
  
 
  public HeapCanonizerRuntimeEfficient canonizer;

  public void initCanonizer() {
	  canonizer = new HeapCanonizerRuntimeEfficient(field_based_gen_ignore_primitive, field_based_gen_max_objects, field_based_gen_max_class_objects, field_based_gen_max_string_length, field_based_gen_max_array, field_based_gen_drop_tests_exceeding_object_limits);  
  }
  
  public void initCanonizer(Set<String> fieldBasedGenClassnames) {
	  canonizer = new HeapCanonizerRuntimeEfficient(field_based_gen_ignore_primitive, fieldBasedGenClassnames, field_based_gen_max_objects, field_based_gen_max_class_objects, field_based_gen_max_string_length, field_based_gen_max_array, field_based_gen_drop_tests_exceeding_object_limits);  
  }
  
  private int positiveTestsSaved;
  public int positiveReferenceExtendingTests;
  private int positivePrimitiveExtendingTests;
  private int positiveTestsDropped;
  private int negativeTestsSaved;
  private int negativeTestsDropped;
  private int testsExceedingLimits;

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


  protected List<Sequence> fbSeq;

  protected int operationBadTag;


private int genFirstAdditionalPositiveSeqs;
private int genFirstAdditionalNegativeSeqs;
private int genFirstAdditionalErrorSeqs;
private int genFirstAdditionalObsPositiveSeqs;
private int genFirstAdditionalObsNegativeSeqs;
private int genFirstAdditionalObsErrorSeqs;



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
    this.secondPhaseMaxTimeMillis = maxTimeMillis + field_based_gen_second_phase_timeout_millis; 
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
        || (discardedTests > max_discarded_tests)
        //|| (testsExtendingExt >= maxOutputSequences)
        || (numOutputSequences() >= maxOutputSequences)
        || (numGeneratedSequences() >= maxGeneratedSequences)
        || (stopper != null && stopper.stop());
  }

  protected boolean stopSecondPhase() {
	  //return timer.getTimeElapsedMillis() >= maxTimeMillis;
	  return timer.getTimeElapsedMillis() >= secondPhaseMaxTimeMillis;
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
    return /*outErrorSeqs.size()*/ + outRegressionSeqs.size();
  }

  public int numRegressionSequences() {
    return outRegressionSeqs.size();
  }

  /**
   * Returns the count of sequences generated so far by the generator.
   *
   * @return the number of sequences generated
   */
  public abstract int numGeneratedSequences();

  public void saveSubsumedCandidates() {
	  for (Sequence is: subsumed_candidates) 
		  subsumed_sequences.add(is);
  }
  
  private boolean saveNonExtendingModifierSequence(ExecutableSequence eSeq) {
	assert eSeq.getLastStmtOperation().isModifier();
	
	int modSeqNum = (numRegressionSequences() / field_based_gen_non_extending_modifiers_ratio) + 1;
	
	if (eSeq.getLastStmtOperation().timesExecutedInNotExtendingModifiers < modSeqNum) {
		eSeq.getLastStmtOperation().timesExecutedInNotExtendingModifiers++;
		return true;
	}
	else
		return false;
  }
  
  private boolean saveRarelyExtendingOperation(ExecutableSequence eSeq) {
	int ratio = (numRegressionSequences() / fbg_save_not_extending_ratio) + 1;
	return eSeq.getLastStmtOperation().timesExecutedInExtendingTests < ratio;
  }
  
  
  
  /*
  private boolean saveObserverAddingNewPrimitiveValue(ExecutableSequence eSeq) {
	assert eSeq.getLastStmtOperation().isObserver();
	
	if (!field_based_gen_save_observers || !eSeq.endsWithObserverReturningNewValue) return false;
	
	int limit = (numRegressionSequences() / field_based_gen_max_non_extending_observer_tests_ratio) + 1;
	
	if (eSeq.getLastStmtOperation().timesExecutedInObserversAddingPrimValue < limit) {
		eSeq.getLastStmtOperation().timesExecutedInObserversAddingPrimValue++;
		return true;
	}
	else
		return false;
  }
  */
  
   private boolean saveNegativeSequence(ExecutableSequence eSeq) {
	int limit = (numRegressionSequences() / fbg_save_negatives_ratio) + 1;
	if (eSeq.getLastStmtOperation().timesExecutedInNegativeTests < limit) {
		eSeq.getLastStmtOperation().timesExecutedInNegativeTests++;
		return true;
	}
	else
		return false;
  }


   private int discardedTests; 
  
  /**
   * Creates and executes new sequences until stopping criteria is met.
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

    discardedTests = 0;
    
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
				  if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine("> Execution of the current sequence failed, saving it as an error revealing test.");
   			  } else {
				  if (eSeq.isNormalExecution()) { 
					  treatPositiveSequence(eSeq);
				  }
				  else {
					  treatNegativeSequence(eSeq);
				  }
  			  }
  		  }
   		  else {
			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("**** ERROR: Current sequence has invalid behaviour");
   		  }
  	  }
	  else {
		  if (FieldBasedGenLog.isLoggingOn()) 
			  FieldBasedGenLog.logLine("**** ERROR: Execution of the current sequence failed");
	  }

   	  eSeq.clearExtensions();
   	  eSeq.clearExecutionResults();
     
      if (dump_sequences) {
        System.out.printf("Sequence after execution:%n%s%n", eSeq.toString());
        System.out.printf("allSequences.size() = %d%n", numGeneratedSequences());
      }

      if (Log.isLoggingOn()) {
        Log.logLine("Sequence after execution: " + Globals.lineSep + eSeq.toString());
        Log.logLine("allSequences.size()=" + numGeneratedSequences());
      }
    }
    
    // First phase ended, clean the hashStore
    hashStore = null;
    
   	long secondPhaseStartTime = System.currentTimeMillis();

    boolean displayStopped = false;
    if (count_objects) {
    	if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
    		progressDisplay.display();
    		progressDisplay.shouldStop = true;
    	}
    	countNumberOfDifferentRuntimeObjects();
    	displayStopped = true;
    }
   	// Don't count objects and field based gen enabled, perform the second phase of the genfirst approach
    else if (field_based_gen != FieldBasedGenType.DISABLED && field_based_gen_extend_with_observers) {
    	genFirstAdditionalPositiveSeqs = 0;
    	genFirstAdditionalNegativeSeqs = 0;
    	genFirstAdditionalErrorSeqs = 0;

    	ArrayList<TypedOperation> operationsPermutable = new ArrayList<>();
    	for (TypedOperation op: operations)
    		if (op.isObserver() || op.isFinalObserver())
    			operationsPermutable.add(op);

    	if (FieldBasedGenLog.isLoggingOn())
    		FieldBasedGenLog.logLine("\n\n>> Second phase starting...");

    	extendModifierTestsWithObserverOps(positiveRegressionSeqs, operationsPermutable, false);

    	/*
    	if (field_based_gen_save_observers) {
    		if (FieldBasedGenLog.isLoggingOn())
    			FieldBasedGenLog.logLine("\n\n>> Second phase of the generation first approach for tests ending with observers starting");
    		extendObserverTestsWithObserverOps(observerRegressionSeqs, operationsPermutable, true, true);
    	}
    	*/
    	if (FieldBasedGenLog.isLoggingOn())
    		FieldBasedGenLog.logLine("\n\n>> Second phase finished.");
    }

   	long secondPhaseTime = System.currentTimeMillis() - secondPhaseStartTime;

    int mod = 0;
    int obs = 0;
    int finalobs = 0;
    int notexec = 0;

    if (!displayStopped && !GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
    	progressDisplay.display();
    	progressDisplay.shouldStop = true;
    } 

    String secondPhaseTimeInSecs = (secondPhaseTime / 1000) + " s";
    long sec = (secondPhaseTime / 1000) % 60;
    long min = ((secondPhaseTime / 1000) / 60) % 60;
    long hr = (((secondPhaseTime / 1000) / 60) / 60);
    String secondPhaseTimeHumanReadable = hr + "h" + min + "m" + sec + "s";
    
    if (!GenInputsAbstract.noprogressdisplay) {
      if (FieldBasedGenLog.isLoggingOn()) {
    	  for (TypedOperation op: operations) {
    		  if (op.isModifier())
    			  mod++;
    		  else if (op.isObserver())
    			  obs++;
    		  else if (op.isFinalObserver())
    			  finalobs++;
    		  else
    			  notexec++; 
    	  }

    	  FieldBasedGenLog.logLine("\n\n> Generation ended.\n\n");
    	  FieldBasedGenLog.logLine("> First phase stats:");
     	  FieldBasedGenLog.logLine("Positive tests saved: " + positiveTestsSaved);
    	  FieldBasedGenLog.logLine("Positive tests extending reference extensions: " + positiveReferenceExtendingTests);
    	  FieldBasedGenLog.logLine("Positive tests extending primitive extensions: " + positivePrimitiveExtendingTests);
    	  FieldBasedGenLog.logLine("Postiive tests discarded: " + positiveTestsDropped);
    	  FieldBasedGenLog.logLine("Negative tests saved: " + negativeTestsSaved);
    	  FieldBasedGenLog.logLine("Negative tests discarded: " + negativeTestsDropped);
    	  FieldBasedGenLog.logLine("Tests excceding limits: " + testsExceedingLimits);
    	  FieldBasedGenLog.logLine("> Second phase stats:");
		  FieldBasedGenLog.logLine("Operations incorrectly deemed observers in the first phase: " + operationBadTag); 
		  FieldBasedGenLog.logLine("Positive sequences observed in the second phase (modifiers): " + genFirstAdditionalPositiveSeqs); 
		  FieldBasedGenLog.logLine("Negative sequences observed in the second phase (modifiers): " + genFirstAdditionalNegativeSeqs); 
		  FieldBasedGenLog.logLine("Error sequences observed in the second phase (modifiers): " + genFirstAdditionalErrorSeqs); 
		  FieldBasedGenLog.logLine("Total sequences observed in the second phase (modifiers): " + (genFirstAdditionalPositiveSeqs + genFirstAdditionalNegativeSeqs + genFirstAdditionalErrorSeqs)); 
		  FieldBasedGenLog.logLine("Positive sequences observed in the third phase (observers): " + genFirstAdditionalObsPositiveSeqs); 
		  FieldBasedGenLog.logLine("Negative sequences observed in the third phase (observers): " + genFirstAdditionalObsNegativeSeqs); 
		  FieldBasedGenLog.logLine("Error sequences observed in the third phase (observers): " + genFirstAdditionalObsErrorSeqs); 
		  FieldBasedGenLog.logLine("Total sequences observed in the third phase (observers): " + (genFirstAdditionalObsPositiveSeqs + genFirstAdditionalObsNegativeSeqs + genFirstAdditionalObsErrorSeqs)); 
    	  FieldBasedGenLog.logLine("Modifier operations: " + mod); 
    	  FieldBasedGenLog.logLine("Observer operations: " + obs); 
    	  FieldBasedGenLog.logLine("Final observer operations: " + finalobs); 
    	  FieldBasedGenLog.logLine("Not executed operations: " + notexec); 
    	  FieldBasedGenLog.logLine("");
    	  FieldBasedGenLog.logLine("\nModifier operations: "); 
    	  for (TypedOperation op: operations)
    		  if (op.isModifier())
    			  FieldBasedGenLog.log(op.getName() + ",");
    	  FieldBasedGenLog.logLine("\nObserver operations: "); 
    	  for (TypedOperation op: operations)
    		  if (op.isObserver() || op.isFinalObserver())
    			  FieldBasedGenLog.log(op.getName() + ",");
    	  FieldBasedGenLog.logLine("\nNot executed operations: "); 
    	  for (TypedOperation op: operations)
    		  if (op.notExecuted())
    			  FieldBasedGenLog.log(op.getName() + ",");
    	  
     	  FieldBasedGenLog.logLine("\nModifier statistics: "); 
    	  for (TypedOperation op: operations)
    		  if (op.isModifier())
    			  FieldBasedGenLog.logLine(op.getName() + ", in extending: " + op.timesExecutedInExtendingModifiers + ", in not extending: " + op.timesExecutedInNotExtendingModifiers);

    	  FieldBasedGenLog.logLine("Second phase execution time: " + secondPhaseTimeInSecs);
    	  FieldBasedGenLog.logLine("Second phase execution time: " + secondPhaseTimeHumanReadable);
      }

   	  System.out.println("\n\n> Generation ended.\n\n");
   	  System.out.println("> First phase stats:");
   	  System.out.println("Positive tests saved: " + positiveTestsSaved);
   	  System.out.println("Positive tests extending reference extensions: " + positiveReferenceExtendingTests);
   	  System.out.println("Positive tests extending primitive extensions: " + positivePrimitiveExtendingTests);
   	  System.out.println("Postiive tests discarded: " + positiveTestsDropped);
   	  System.out.println("Negative tests saved: " + negativeTestsSaved);
   	  System.out.println("Negative tests discarded: " + negativeTestsDropped);
   	  System.out.println("Tests excceding limits: " + testsExceedingLimits);
   	  System.out.println("> Second phase stats:");
      System.out.println("Operations incorrectly deemed observers in the first phase: " + operationBadTag); 
	  System.out.println("Positive sequences observed in the second phase (modifiers): " + genFirstAdditionalPositiveSeqs); 
	  System.out.println("Negative sequences observed in the second phase (modifiers): " + genFirstAdditionalNegativeSeqs); 
	  System.out.println("Error sequences observed in the second phase (modifiers): " + genFirstAdditionalErrorSeqs); 
	  System.out.println("Total sequences observed in the second phase (modifiers): " + (genFirstAdditionalPositiveSeqs + genFirstAdditionalNegativeSeqs + genFirstAdditionalErrorSeqs)); 
	  System.out.println("Positive sequences observed in the third phase (observers): " + genFirstAdditionalObsPositiveSeqs); 
	  System.out.println("Negative sequences observed in the third phase (observers): " + genFirstAdditionalObsNegativeSeqs); 
	  System.out.println("Error sequences observed in the third phase (observers): " + genFirstAdditionalObsErrorSeqs); 
	  System.out.println("Total sequences observed in the third phase (observers): " + (genFirstAdditionalObsPositiveSeqs + genFirstAdditionalObsNegativeSeqs + genFirstAdditionalObsErrorSeqs)); 
   	  System.out.println("Modifier operations: " + mod); 
   	  System.out.println("Observer operations: " + obs); 
   	  System.out.println("Final observer operations: " + finalobs); 
   	  System.out.println("Not executed operations: " + notexec); 
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

    // Notify listeners that exploration is ending.
    if (listenerMgr != null) {
      listenerMgr.explorationEnd();
    }
  }
        			
  
  private void treatPositiveSequence(ExecutableSequence eSeq) {
	  boolean save = false;
	  if (field_based_gen == FieldBasedGenType.DISABLED) 
		  save = true;
	  else {
		  if (eSeq.enlargesExtensions == ExtendExtensionsResult.EXTENDED ||
				  eSeq.enlargesExtensions == ExtendExtensionsResult.EXTENDED_PRIMITIVE) {
			  save = true;
			  if (FieldBasedGenLog.isLoggingOn()) {
				  if (eSeq.enlargesExtensions == ExtendExtensionsResult.EXTENDED) {
					  positiveReferenceExtendingTests++;
					  FieldBasedGenLog.logLine("> Extensions enlarged for reference type objects.");
				  }
				  else {
					  positivePrimitiveExtendingTests++;
					  FieldBasedGenLog.logLine("> Extensions enlarged for primitive values.");
				  }
			  }
		  }
		  else if (eSeq.enlargesExtensions == ExtendExtensionsResult.LIMITS_EXCEEDED) {
			  // We discard the tests exceeding limits (for now at least)
			  assert false: "Should never happen as randoop's generation mechanism does not produce too large strings";
		  /*
			  testsExceedingLimits++;
			  if (FieldBasedGenLog.isLoggingOn()) 
				  FieldBasedGenLog.logLine("> Current sequence exceeds generation limits.");
		   */
		  }
		  else {
			  // TODO: Need observer detection for the following line to work
			  //if (eSeq.getLastStmtOperation().isModifier() && saveNonExtendingModifierSequence(eSeq)) {
 			  if (FieldBasedGenLog.isLoggingOn()) 
 				  FieldBasedGenLog.logLine("> Extensions not enlarged.");
			  if (saveRarelyExtendingOperation(eSeq)) {
				  save = true;
				  if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine("> The last operation is rarely used in tests enlarging extensions.");
			  }
		  }
	  }

	  if (save) {
		  outRegressionSeqs.add(eSeq);
		  saveSubsumedCandidates();
		  positiveTestsSaved++;
		  eSeq.getLastStmtOperation().timesExecutedInExtendingTests++;
		  if (field_based_gen_extend_with_observers)
			  positiveRegressionSeqs.add(eSeq);
		  if (FieldBasedGenLog.isLoggingOn()) 
			  FieldBasedGenLog.logLine("> Current positive sequence saved.");
	  }
	  else {
		  positiveTestsDropped++;
		  if (FieldBasedGenLog.isLoggingOn()) 
			  FieldBasedGenLog.logLine("> Current positive sequence discarded.");
	  }
  }
  
  
  private void treatNegativeSequence(ExecutableSequence eSeq) {
	  boolean save = false;
	  if (field_based_gen == FieldBasedGenType.DISABLED) {
		  if (!randoop_drop_negatives || saveNegativeSequence(eSeq)) {
			  save = true;
			  
			  if (randoop_drop_negatives)
				  if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine("> The last operation is rarely used in negative sequences that are stored."); 
		  }
	  }
	  else {
		  if (saveNegativeSequence(eSeq)) {
			  save = true;
			  if (FieldBasedGenLog.isLoggingOn()) 
				  FieldBasedGenLog.logLine("> The last operation is rarely stored in negative sequences."); 
		  }
	  }
	  
	  if (save) {
		  outRegressionSeqs.add(eSeq);
		  saveSubsumedCandidates();
		  negativeTestsSaved++;
		  if (FieldBasedGenLog.isLoggingOn()) 
			  FieldBasedGenLog.logLine("> Current negative sequence saved.");
	  }
	  else {
		  negativeTestsDropped++;
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine("> Current negative sequence discarded.");
	  }
  }
	  

  private LongPair hashExtensions(FieldExtensionsIndexes ext) {
	  LongPair hash = new LongPair();
	  byte [] bytesExt = ext.toIndexesString().getBytes();
	  
	  MurmurHash3.murmurhash3_x64_128(bytesExt, 0, bytesExt.length - 1, 0, hash);
	  
	  return hash;
  }
  
  private void countNumberOfDifferentRuntimeObjects() {
	  // Count objects
	  if (FieldBasedGenLog.isLoggingOn())
		  FieldBasedGenLog.logLine("\n\n>> Starting to count objects generated during the first phase.");
	  System.out.println("\n\n>> Starting to count objects generated during the first phase.");

	  Map<CanonizerClass, Set<LongPair>> hashes = new HashMap<>();
	  int totalObjCount = 0;

	  for (ExecutableSequence eSeq: outRegressionSeqs) {
		  eSeq.execute(executionVisitor, checkGenerator);
		  if (!eSeq.isNormalExecution()) {
			  eSeq.clearExecutionResults();
			  continue;
		  }
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine(">> Current sequence:\n" + eSeq.toCodeString());

		  List<Tuple<CanonizerClass, FieldExtensionsIndexes>> lastStmtExt = eSeq.canonizeObjectsAfterExecution(canonizer);
		  
		  for (Tuple<CanonizerClass, FieldExtensionsIndexes> t: lastStmtExt) {
			  if (t == null) continue;

			  CanonizerClass cc = t.getFirst();
			  FieldExtensionsIndexes ext = t.getSecond();
			  Set<LongPair> hs = hashes.get(cc);
			  if (hs == null) {
				  hs = new HashSet<LongPair>();
				  hashes.put(cc, hs);
			  }
			  LongPair hash = hashExtensions(ext);
			  
			  if (hs.add(hash)) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> New object counted:");

				  totalObjCount++;
			  }
			  else {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> Object was already counted before:");
			  }

			  if (FieldBasedGenLog.isLoggingOn()) 
				  FieldBasedGenLog.logLine("> Type: " + cc.name + ", hash: ," + hash + " obj. ext.:\n" + ext.toString());
			  
		  }

		  eSeq.clearExecutionResults();
	  }

	  for (CanonizerClass cc: hashes.keySet()) {
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine("Type: " + cc.name + ", objects count: " + hashes.get(cc).size());
		  System.out.println("Type: " + cc.name + ", objects count: " + hashes.get(cc).size());
	  }

	  if (FieldBasedGenLog.isLoggingOn())
		  FieldBasedGenLog.logLine("Final count: " + totalObjCount); 
	  System.out.println("Final count: " + totalObjCount);
  }
  
  
  // For using hashes instead of extensions to deal with redundancy
  private Map<CanonizerClass, Set<LongPair>> hashStore = new HashMap<>();
  
  public boolean addObjectHashesToStore(ExecutableSequence eSeq) {

	  List<Tuple<CanonizerClass, FieldExtensionsIndexes>> lastStmtExt = eSeq.canonizeObjectsAfterExecution(canonizer, false);
	  eSeq.enlargesExtensions = ExtendExtensionsResult.NOT_EXTENDED;
	  boolean result = false;
	  
	  int lastStmtInd = eSeq.sequence.size() -1;
	  Statement stmt = eSeq.sequence.getStatement(lastStmtInd);
	  int varIndex = 0;
	  Map<CanonizerClass, Set<LongPair>> newHashes = new HashMap<>();
	  for (Tuple<CanonizerClass, FieldExtensionsIndexes> t: lastStmtExt) {
		  if (t == null) { 
			  varIndex++;
			  continue; 
		  }

		  CanonizerClass cc = t.getFirst();
		  FieldExtensionsIndexes ext = t.getSecond();
		  Set<LongPair> hs = hashStore.get(cc);
		  if (hs == null) {
			  hs = new HashSet<LongPair>();
			  hashStore.put(cc, hs);
		  }
		  LongPair hash = hashExtensions(ext);
		  
		  if (!hs.contains(hash)) {
			  if (FieldBasedGenLog.isLoggingOn()) {
				  FieldBasedGenLog.logLine("> New object saved:");
				  FieldBasedGenLog.logLine("> Type: " + cc.name + ", hash: ," + hash + " obj. ext.:\n" + ext.toString());
			  }
			  
			  Set<LongPair> ns = newHashes.get(cc);
			  if (ns == null) {
				  ns = new HashSet<LongPair>();
				  newHashes.put(cc, ns);
			  }
			  ns.add(hash);
			  
			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
					   + stmt.toString() + " (index " + lastStmtInd + ")");
			  
			  eSeq.sequence.addActiveVar(lastStmtInd, varIndex);
			  eSeq.enlargesExtensions = ExtendExtensionsResult.EXTENDED;
			  result = true;
		  }
		  else {
			  if (FieldBasedGenLog.isLoggingOn()) {
				  FieldBasedGenLog.logLine("> Object was already saved before:");
				  FieldBasedGenLog.logLine("> Type: " + cc.name + ", hash: ," + hash + " obj. ext.:\n" + ext.toString());
			  }
		  }

		  varIndex++;
	  }
	  
	  for (CanonizerClass cc: newHashes.keySet()) {
		  //numberOfHashes -= hashStore.get(cc).size();
		  hashStore.get(cc).addAll(newHashes.get(cc));
		  //numberOfHashes += hashStore.get(cc).size();
	  }
	  
	  //System.out.println("Number of hashes: " + numberOfHashes);
	  
	  return result;
  }
  
  //int numberOfHashes = 0;
  
  
  /*
  private void countNumberOfDifferentCompileTimeObjectsUsingExtensions() {

	  // Count objects
	  if (FieldBasedGenLog.isLoggingOn())
		  FieldBasedGenLog.logLine("\n\n>> Starting to count objects generated during the first phase.");
	  System.out.println("\n\n>> Starting to count objects generated during the first phase.");

	  int allObjCount = 0;
	  //int flakySeqs = 0;
	  for (Type type: componentManager.getAllGeneratedTypes()) {
		  //Set<Sequence> sequenceMap = componentManager.getAllGeneratedSequences();
		  List<ExecutableSequence> sequenceMap = outRegressionSeqs; 

		  Set<FieldExtensionsIndexes> currExtSet = new HashSet<>();
		  for (ExecutableSequence seq: sequenceMap) {
			  List<Type> lastStmtTypes = seq.sequence.getTypesForLastStatement();
			  
			  boolean hasType = false;
			  for (int j = 0; j < lastStmtTypes.size(); j++) {
				  if (lastStmtTypes.get(j).equals(type)) {
					  hasType = true;
					  break;
				  }
			  }
			  if (!hasType) continue;

			  ExecutableSequence eSeq = new ExecutableSequence(seq.sequence);
			  eSeq.execute(executionVisitor, checkGenerator);

			  if (!eSeq.isNormalExecution()) {
				  //flakySeqs++;
				  //System.out.println("ERROR: Flaky sequence while counting objects! Continuing to the next sequence...");
				  continue;
			  }

			  List<FieldExtensionsIndexes> lastStmtExt = null;
			  try {
				  lastStmtExt = eSeq.canonizeLastStatementObjects(canonizer);
			  } catch (CanonizationErrorException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }

			  for (int j = 0; j < lastStmtTypes.size(); j++) {
				  if (lastStmtTypes.get(j).equals(type)) {
					  
					  if (lastStmtExt.get(j) == null) continue;
					  
					  if (currExtSet.add(lastStmtExt.get(j))) {
						  if (FieldBasedGenLog.isLoggingOn())
							  FieldBasedGenLog.logLine("> Object of type: " + type.toString() + ", extensions:\n" + lastStmtExt.get(j));
					  }
				  }
			  }
		  }
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine("Type: " + type.toString() + ", objects count: " + currExtSet.size());
		  System.out.println("Type: " + type.toString() + ", objects count: " + currExtSet.size());
		  allObjCount += currExtSet.size();
	  }
	  
	  if (FieldBasedGenLog.isLoggingOn())
		  FieldBasedGenLog.logLine("Final count: " + allObjCount); 
	  System.out.println("Final count: " + allObjCount);
	  //System.out.println("Flaky seqs: " + flakySeqs);

  }
  
  /*
  private void countNumberOfDifferentCompileTimeObjectsUsingHashes() {

	  // Count objects
	  if (FieldBasedGenLog.isLoggingOn())
		  FieldBasedGenLog.logLine("\n\n>> Starting to count objects generated during the first phase.");
	  System.out.println("\n\n>> Starting to count objects generated during the first phase.");

	  int allObjCount = 0;
	  //int flakySeqs = 0;
	  
	  for (Type type: componentManager.getAllGeneratedTypes()) {
		  //Set<Sequence> sequenceMap = componentManager.getAllGeneratedSequences();
		  List<ExecutableSequence> sequenceMap = outRegressionSeqs; 

		  Set<LongPair> currHashSet = new HashSet<>();
		  for (ExecutableSequence seq: sequenceMap) {
			  List<Type> lastStmtTypes = seq.sequence.getTypesForLastStatement();
			  
			  boolean hasType = false;
			  for (int j = 0; j < lastStmtTypes.size(); j++) {
				  if (lastStmtTypes.get(j).equals(type)) {
					  hasType = true;
					  break;
				  }
			  }
			  if (!hasType) continue;

			  ExecutableSequence eSeq = new ExecutableSequence(seq.sequence);
			  eSeq.execute(executionVisitor, checkGenerator);

			  if (!eSeq.isNormalExecution()) {
				  //flakySeqs++;
				  //System.out.println("ERROR: Flaky sequence while counting objects! Continuing to the next sequence...");
				  continue;
			  }	  
	  
			  List<FieldExtensionsIndexes> lastStmtExt = null;
			  try {
				  lastStmtExt = eSeq.canonizeLastStatementObjects(canonizer);
			  } catch (CanonizationErrorException e) {
				  // TODO Auto-generated catch block
				  e.printStackTrace();
			  }

			  for (int j = 0; j < lastStmtTypes.size(); j++) {
				  if (lastStmtTypes.get(j).equals(type)) {
					  FieldExtensionsIndexes ext = lastStmtExt.get(j);

					  if (ext == null) continue;

					  LongPair hashres = new LongPair();
					  byte [] bytesExt = ext.toString().getBytes();
					  
					  MurmurHash3.murmurhash3_x64_128(bytesExt, 0, bytesExt.length - 1, 0, hashres);
					  
					  if (currHashSet.add(hashres)) {
						  if (FieldBasedGenLog.isLoggingOn())
							  FieldBasedGenLog.logLine("> Object of type: " + type.toString() + ", extensions:\n" + ext);
					  }
				  }
			  }
			  
		  }
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine("Type: " + type.toString() + ", objects count: " + currHashSet.size());
		  System.out.println("Type: " + type.toString() + ", objects count: " + currHashSet.size());
		  allObjCount += currHashSet.size();
	  }

	  if (FieldBasedGenLog.isLoggingOn()) 
		  FieldBasedGenLog.logLine("Final count: " + allObjCount); 
	  System.out.println("Final count: " + allObjCount);
	  // System.out.println("Flaky seqs: " + flakySeqs);
	  
  } 
  */

  private void extendModifierTestsWithObserverOps(List<ExecutableSequence> sequencesToExtend, List<TypedOperation> operationsPermutable, boolean observers) {
	  // Generation first add one of each observer operation to the end of the sequence
	  for (ExecutableSequence eSeq: sequencesToExtend) {
		  
		  if (!field_based_gen_extend_subsumed && subsumed_sequences.contains(eSeq.sequence)) continue;
		  
		  // TODO: If the sequence ends with a method incorrectly deemed modifier continue to the next sequence
		  // Implement a method to get the last method of a sequence.
		  List<Type> seqLastStmtTypes = eSeq.sequence.getTypesForLastStatement();
		  List<Boolean> isLastStmtVarActive = eSeq.getLastStmtActiveVars();
		  assert seqLastStmtTypes.size() == isLastStmtVarActive.size();
		  
		  List<Integer> candidates = new ArrayList<>();
		  for (int j = 0; j < seqLastStmtTypes.size(); j++) {
			  if (isLastStmtVarActive.get(j)) 
				  candidates.add(j);
		  }

		  if (candidates.isEmpty()) {
			  if (FieldBasedGenLog.isLoggingOn()) 
				  FieldBasedGenLog.logLine("> Current sequence cannot be extended\n" + eSeq.sequence.toCodeString());
			  
			  continue;
		  }
		  
		  int j = Randomness.randomMember(candidates);
		  ExecutableSequence neweSeq = eSeq;
		  Type observedType = seqLastStmtTypes.get(j);
		  int observedIndex = j;

		  if (FieldBasedGenLog.isLoggingOn()) {
			  FieldBasedGenLog.logLine("\n>> First phase modifier sequence to extend:\n " + neweSeq.sequence.toCodeString());
			  FieldBasedGenLog.logLine("> Type to extend: " + observedType);
			  FieldBasedGenLog.logLine("> Index of type to extend: " + observedIndex);
		  }
				  
		  int currTestNegativeObservers = 0;
		  int currTestObservers = 0;
		  RandomPerm.randomPermutation(operationsPermutable);
		  // Should randomly mix the operations list each time we do this to avoid always the same execution order.
		  for (TypedOperation operation: operationsPermutable) {

			  if (currTestObservers > field_based_gen_observers_per_test) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> Maximum number of observers exceeded for the current test. Continue with the next test");
				  
				  // This sequence is already too large, continue with the next sequence
				  break;
			  }
			  
			  if (stopSecondPhase()) { 
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("\n> WARNING: Second Phase stopped due to time constraints");
				  System.out.println("WARNING: Second Phase stopped due to time constraints");
				  return;
			  }
			  
			  /*
			  if (FieldBasedGenLog.isLoggingOn()) {
				  FieldBasedGenLog.logLine("\n> Sequence being extended:\n " + neweSeq.sequence.toCodeString());
				  FieldBasedGenLog.logLine("> Observed type: " + observedType);
				  FieldBasedGenLog.logLine("> Observed index: " + observedIndex);
			  }
		  
			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("> Operation: " + operation.toString());
			  */

			  if (!operation.isObserver() && !operation.isFinalObserver()) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The current operation has been flagged as a modifier in the second phase, don't use it to extend tests anymore");
				  //System.out.println("> Operation " + operation.toString() + " has been flagged as a modifier by the field based approach, don't use it to build additional tests");
				  continue;
			  }

			  if (operation.isConstructorCall()) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The current operation is a constructor, don't use it to extend tests");
				  continue;
			  }

			  // The first integer of the tuple is the index of the variable chosen from newSeq, 
			  // the second integer is the corresponding index of a compatible type in operation
			  TypeTuple inputTypes = operation.getInputTypes();
			  Integer opIndex = getRandomConnectionBetweenTypes(observedType, inputTypes);
			  if (opIndex == null) {
				  /*
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The operation cannot observe type " + observedType);
					  */
				  continue;
			  }

			  List<Sequence> sequences = new ArrayList<>();
			  int totStatements = 0;
			  List<Integer> variables = new ArrayList<>();
			  boolean error = false;
			  for (int i = 0; i < inputTypes.size(); i++) {
				  Type inputType = inputTypes.get(i);
				  Variable randomVariable;
				  
				  if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine("> Input type: " + inputType.toString());

				  Sequence chosenSeq;
				  if (i == opIndex) {
					  chosenSeq = neweSeq.sequence; 
					  randomVariable = chosenSeq.getVariablesOfLastStatement().get(observedIndex);

					  if (FieldBasedGenLog.isLoggingOn()) 
						  FieldBasedGenLog.logLine("> Current sequence selected as input: ");
				  }
				  else {
					  SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
					  if (l.isEmpty()) {
						  error = true;
						  break;
					  }

					  chosenSeq = Randomness.randomMember(l);
					  randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);

					  if (FieldBasedGenLog.isLoggingOn()) 
						  FieldBasedGenLog.logLine("> Random sequence selected as input: ");

					  if (FieldBasedGenLog.isLoggingOn()) 
						  FieldBasedGenLog.logLine(chosenSeq.toCodeString());
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
			  Sequence concatSeq = Sequence.concatenateAndGetIndexes(isequences.sequences, neweSeq.sequence, startIndexRef);
			  int startIndex = startIndexRef[0];
			  int endIndex = startIndex + neweSeq.sequence.size()-1;
			  
			  if (FieldBasedGenLog.isLoggingOn()) {
				  //FieldBasedGenLog.logLine("> Concatenation result: \n" + concatSeq.toCodeString());
				  //FieldBasedGenLog.logLine("> Indexed sequence: \n" + neweSeq.sequence.toCodeString());
				  FieldBasedGenLog.logLine("> Indexes of the sequence to be extended within the concatenation result: " + startIndex + ", " + endIndex);
			  }
			  
			  // Figure out input variables.
			  List<Variable> inputs = new ArrayList<>();
			  for (Integer oneinput : isequences.indices) {
				  Variable v = concatSeq.getVariable(oneinput);
				  inputs.add(v);
			  }

			  Sequence newSequence = concatSeq.extend(operation, inputs);

			  // Discard if sequence is larger than size limit
			  if (newSequence.size() > GenInputsAbstract.maxsize) {
				  if (Log.isLoggingOn()) {
					  Log.logLine(
							  "Sequence discarded because size "
									  + newSequence.size()
									  + " exceeds maximum allowed size "
									  + GenInputsAbstract.maxsize);
				  }
				  // This sequence is too large, try another one 
				  continue;
			  }

			  num_sequences_generated++;

			  if (this.allSequences.contains(newSequence)) {
				  if (Log.isLoggingOn()) {
					  Log.logLine("Sequence discarded because the same sequence was previously created.");
				  }

				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The current sequence was generated twice in the second phase \n"); 

				  continue;
			  }

			  this.allSequences.add(newSequence);	

			  ExecutableSequence eSeq2ndPhase = new ExecutableSequence(newSequence);
			  executeExtendedSequenceNoReexecute(eSeq2ndPhase, neweSeq, startIndex, endIndex);
			  processLastSequenceStatement(eSeq2ndPhase);

			  if (eSeq2ndPhase.isNormalExecution() && operation.isModifier()) {
				  FieldBasedGenLog.logLine("> Operation " + operation.toString() + " was incorrectly flagged as observer in the first phase. Don't extend this test" ); 
				  operationBadTag++;
				  eSeq2ndPhase.clearExtensions();
				  eSeq2ndPhase.clearExecutionResults();
				  continue;
			  }

			  if (eSeq2ndPhase.hasFailure()) {
				  num_failing_sequences++;
			  }

			  if (outputTest.test(eSeq2ndPhase)) {

				  if (!eSeq2ndPhase.hasInvalidBehavior()) {
					  if (eSeq2ndPhase.hasFailure()) {
						  outErrorSeqs.add(eSeq2ndPhase);
						  if (!observers)
							  genFirstAdditionalErrorSeqs++;
						  else
							  genFirstAdditionalObsErrorSeqs++;

						  if (FieldBasedGenLog.isLoggingOn()) 
							  FieldBasedGenLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");

						  eSeq2ndPhase.clearExecutionResults();
					  } 
					  else {
						  // outRegressionSeqs.add(eSeq2ndPhase);
						  if (eSeq2ndPhase.isNormalExecution()) {
							  currTestObservers++;
							  
							  if (FieldBasedGenLog.isLoggingOn()) 
								  FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
							  // continue extending this sequence;
							  neweSeq = eSeq2ndPhase;

							  // opIndex was calculated over operation's input types. observedIndex must be calculated over the output types of the operation 
							  if (operation.getOutputType().isVoid())
								  observedIndex = opIndex;
							  else
								  observedIndex = opIndex + 1;

							  assert neweSeq.sequence.getTypesForLastStatement().get(observedIndex).equals(observedType);
							  
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
							  if (currTestNegativeObservers < field_based_gen_negative_observers_per_test) {
								  currTestNegativeObservers++;
								  if (FieldBasedGenLog.isLoggingOn()) 
									  FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");

								  if (!observers)
									  genFirstAdditionalNegativeSeqs++;
								  else
									  genFirstAdditionalObsNegativeSeqs++;
								  
								  outRegressionSeqs.add(eSeq2ndPhase);
								  // Add newSequence's inputs to subsumed_sequences
								  for (Sequence subsumed: isequences.sequences) {
									  subsumed_sequences.add(subsumed);
								  }	
							  }
							  else {
								  if (FieldBasedGenLog.isLoggingOn()) 
									  FieldBasedGenLog.logLine("> Negative observers per test limit exceeded. Discarding current sequence");

							  }
							  eSeq2ndPhase.clearExecutionResults();
							  
						  } 
						  /*
						  // Add newSequence's inputs to subsumed_sequences
						  for (Sequence subsumed: isequences.sequences) {
							  subsumed_sequences.add(subsumed);
						  }	
						  */
					  }
				  }
				  else {
					  /*
					  System.out.println("> ERROR: Sequence with invalid behavior in the second phase:");
					  System.out.println(eSeq2ndPhase.toCodeString());
					  */
					  if (FieldBasedGenLog.isLoggingOn()) {
						  FieldBasedGenLog.logLine("> ERROR: Sequence with invalid behaviour in the second phase:");
						  FieldBasedGenLog.logLine(eSeq2ndPhase.toCodeString());
					  }
					  if (!observers)
						  genFirstAdditionalErrorSeqs++;
					  else
						  genFirstAdditionalObsErrorSeqs++;
					  
					  eSeq2ndPhase.clearExecutionResults();
				  }
			  }
			  else {
				  /*
				  System.out.println("> ERROR: Failing sequence in the second phase:");
				  System.out.println(eSeq2ndPhase.toCodeString());
				  */
				  if (FieldBasedGenLog.isLoggingOn()) {
					  FieldBasedGenLog.logLine("> ERROR: Failing sequence in the second phase:");
					  FieldBasedGenLog.logLine(eSeq2ndPhase.toCodeString());
				  }
				  if (!observers)
					  genFirstAdditionalErrorSeqs++;
				  else
					  genFirstAdditionalObsErrorSeqs++;
				  
				  eSeq2ndPhase.clearExecutionResults();
			  }

			  eSeq2ndPhase.clearExtensions();
		  }
		  
		  neweSeq.clearExtensions();
		  neweSeq.clearExecutionResults();
		  if (!neweSeq.equals(eSeq)) {
			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("\n>> Final extended modifier sequence:\n " + neweSeq.toCodeString());
			  outRegressionSeqs.add(neweSeq);
		  }
	  }
  } 
  
  
  


private void extendObserverTestsWithObserverOps(List<ExecutableSequence> sequencesToExtend, List<TypedOperation> operationsPermutable, boolean extendOnlyPrimitive, boolean observers) {
	  // Generation first add one of each observer operation to the end of the sequence
	  for (ExecutableSequence eSeq: sequencesToExtend) {

		  if (!field_based_gen_extend_subsumed && subsumed_sequences.contains(eSeq.sequence)) continue;
		  
		  ExecutableSequence neweSeq = eSeq;
		  
		  if (FieldBasedGenLog.isLoggingOn()) {
			  FieldBasedGenLog.logLine("\n>> First phase observer sequence to extend:\n " + neweSeq.sequence.toCodeString());
		  }

		  // TODO: If the sequence ends with a method incorrectly deemed modifier continue to the next sequence
		  // Implement a method to get the last method of a sequence.
		  int currTestNegativeObservers = 0;
		  int currTestObservers = 0;
		  RandomPerm.randomPermutation(operationsPermutable);
		  // Should randomly mix the operations list each time we do this to avoid always the same execution order.
		  for (TypedOperation operation: operationsPermutable) {

			  if (currTestObservers > field_based_gen_observers_per_test) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> Maximum number of observers exceeded for the current test. Continue with the next test");
				  // This sequence is already too large, continue with the next sequence
				  break;
			  }
			  
			  if (stopSecondPhase()) { 
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("\n> WARNING: Second Phase stopped due to time constraints");

				  System.out.println("WARNING: Second Phase stopped due to time constraints");
				  
				  return;
			  }

			  int lastIndex = neweSeq.sequence.size() - 1;

			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("\n> Starting an attempt to extend sequence:\n " + neweSeq.sequence.toCodeString());

			  TypedOperation seqLastStmtOp = neweSeq.sequence.getStatement(lastIndex).getOperation();

			  TypeTuple seqLastStmtInputTypes = seqLastStmtOp.getInputTypes();
			  TypeTuple seqLastStmtOutputTypes = new TypeTuple();

			  List<Boolean> isLastStmtVarActive = neweSeq.getLastStmtActiveVars();
			  if (!seqLastStmtOp.getOutputType().isVoid())
				  seqLastStmtOutputTypes.list.add(seqLastStmtOp.getOutputType());
			  for (int i = 0; i < seqLastStmtInputTypes.size(); i++)
				  seqLastStmtOutputTypes.list.add(seqLastStmtInputTypes.get(i));

			  /*
			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("> Operation: " + operation.toString());
			  */

			  if (!operation.isObserver() && !operation.isFinalObserver()) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The current operation has been flagged as a modifier in the second phase, don't use it to extend tests anymore");
				  //System.out.println("> Operation " + operation.toString() + " has been flagged as a modifier by the field based approach, don't use it to build additional tests");
				  continue;
			  }

			  if (operation.isConstructorCall()) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The current operation is a constructor, don't use it to extend tests");
				  continue;
			  }

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
				  /* if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine("> Input type: " + inputType.toString()); */

				  Sequence chosenSeq;
				  if (i == connection.getSecond()) {
					  chosenSeq = neweSeq.sequence; 

					  /* if (FieldBasedGenLog.isLoggingOn()) 
						  FieldBasedGenLog.logLine("> Sequence to be extended selected: "); */
				  }
				  else {
					  SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
					  if (l.isEmpty()) {
						  error = true;
						  break;
					  }

					  chosenSeq = Randomness.randomMember(l);

					  /* if (FieldBasedGenLog.isLoggingOn()) 
						  FieldBasedGenLog.logLine("> Random sequence selected: "); */
				  }

				  /* if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine(chosenSeq.toCodeString()); */

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
			  Sequence concatSeq = Sequence.concatenateAndGetIndexes(isequences.sequences, neweSeq.sequence, startIndexRef);
			  int startIndex = startIndexRef[0];
			  int endIndex = startIndex + neweSeq.sequence.size() -1;
			  
			  if (FieldBasedGenLog.isLoggingOn()) {
//				  FieldBasedGenLog.logLine("> Concatenation result: \n" + concatSeq.toCodeString());
//				  FieldBasedGenLog.logLine("> Indexed sequence: \n" + neweSeq.sequence.toCodeString());
				  FieldBasedGenLog.logLine("> Indexes of the sequence to be extended within the concatenation result: " + startIndex + ", " + endIndex);
			  }
			  // Sequence concatSeq = Sequence.concatenate(isequences.sequences);

			  // Figure out input variables.
			  List<Variable> inputs = new ArrayList<>();
			  for (Integer oneinput : isequences.indices) {
				  Variable v = concatSeq.getVariable(oneinput);
				  inputs.add(v);
			  }

			  Sequence newSequence = concatSeq.extend(operation, inputs);

			  // Discard if sequence is larger than size limit
			  if (newSequence.size() > GenInputsAbstract.maxsize) {
				  if (Log.isLoggingOn()) {
					  Log.logLine(
							  "Sequence discarded because size "
									  + newSequence.size()
									  + " exceeds maximum allowed size "
									  + GenInputsAbstract.maxsize);
				  }
				  // This sequence is too large, try another one 
				  continue;
			  }

			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("> Resulting sequence: \n" + newSequence.toCodeString()); 

			  num_sequences_generated++;

			  if (this.allSequences.contains(newSequence)) {
				  if (Log.isLoggingOn()) {
					  Log.logLine("Sequence discarded because the same sequence was previously created.");
				  }

				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> The current sequence was generated twice in the second phase \n"); 

				  continue;
			  }

			  this.allSequences.add(newSequence);	

			  ExecutableSequence eSeq2ndPhase = new ExecutableSequence(newSequence);
			  executeExtendedSequenceNoReexecute(eSeq2ndPhase, neweSeq, startIndex, endIndex);
			  processLastSequenceStatement(eSeq2ndPhase);
//			  executeExtendedSequence(eSeq2ndPhase);

			  if (eSeq2ndPhase.isNormalExecution() && operation.isModifier()) {
				  FieldBasedGenLog.logLine("> Operation " + operation.toString() + " was incorrectly flagged as observer in the first phase. Don't extend this test" ); 
				  operationBadTag++;
				  eSeq2ndPhase.clearExtensions();
				  eSeq2ndPhase.clearExecutionResults();
				  continue;
			  }

			  if (eSeq2ndPhase.hasFailure()) {
				  num_failing_sequences++;
			  }

			  if (outputTest.test(eSeq2ndPhase)) {

				  if (!eSeq2ndPhase.hasInvalidBehavior()) {
					  if (eSeq2ndPhase.hasFailure()) {
						  outErrorSeqs.add(eSeq2ndPhase);
						  if (!observers)
							  genFirstAdditionalErrorSeqs++;
						  else
							  genFirstAdditionalObsErrorSeqs++;

						  if (FieldBasedGenLog.isLoggingOn()) 
							  FieldBasedGenLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");

						  eSeq2ndPhase.clearExecutionResults();
					  } 
					  else {
						  //outRegressionSeqs.add(eSeq2ndPhase);
						  if (eSeq2ndPhase.isNormalExecution()) {
							  currTestObservers++;
							  
							  if (FieldBasedGenLog.isLoggingOn()) 
								  FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
							  // continue extending this sequence;
							  neweSeq = eSeq2ndPhase;
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
							  if (currTestNegativeObservers < field_based_gen_negative_observers_per_test) {
								  currTestNegativeObservers++;
								  if (FieldBasedGenLog.isLoggingOn()) 
									  FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");

								  if (!observers)
									  genFirstAdditionalNegativeSeqs++;
								  else
									  genFirstAdditionalObsNegativeSeqs++;
								  
								  outRegressionSeqs.add(eSeq2ndPhase);
								  // Add newSequence's inputs to subsumed_sequences
								  for (Sequence subsumed: isequences.sequences) {
									  subsumed_sequences.add(subsumed);
								  }	
							  }
							  else {
								  if (FieldBasedGenLog.isLoggingOn()) 
									  FieldBasedGenLog.logLine("> Negative observers per test limit exceeded. Discarding current sequence");
							  }
							  eSeq2ndPhase.clearExecutionResults();
						  } 
						  
					  }
				  }
				  else {
					  /*
					  System.out.println("> ERROR: Sequence with invalid behavior in the second phase:");
					  System.out.println(eSeq2ndPhase.toCodeString());
					  */
					  if (FieldBasedGenLog.isLoggingOn()) {
						  FieldBasedGenLog.logLine("> ERROR: Sequence with invalid behaviour in the second phase:");
						  FieldBasedGenLog.logLine(eSeq2ndPhase.toCodeString());
					  }
					  if (!observers)
						  genFirstAdditionalErrorSeqs++;
					  else
						  genFirstAdditionalObsErrorSeqs++;
				  
					  eSeq2ndPhase.clearExecutionResults();
				  }
			  }
			  else {
				  /*
				  System.out.println("> ERROR: Failing sequence in the second phase:");
				  System.out.println(eSeq2ndPhase.toCodeString());
				  */
				  if (FieldBasedGenLog.isLoggingOn()) {
					  FieldBasedGenLog.logLine("> ERROR: Failing sequence in the second phase:");
					  FieldBasedGenLog.logLine(eSeq2ndPhase.toCodeString());
				  }
				  if (!observers)
					  genFirstAdditionalErrorSeqs++;
				  else
					  genFirstAdditionalObsErrorSeqs++;

				  eSeq2ndPhase.clearExecutionResults();
			  }
			  
			  eSeq2ndPhase.clearExtensions();
		  }

		  neweSeq.clearExtensions();
		  neweSeq.clearExecutionResults();
		  if (!neweSeq.equals(eSeq)) {
			  if (FieldBasedGenLog.isLoggingOn())
				  FieldBasedGenLog.logLine("\n>> Final extended observer sequence:\n " + neweSeq.toCodeString());
			  outRegressionSeqs.add(neweSeq);
		  }
	  }
  }
  
private void processLastSequenceStatement(ExecutableSequence seq) {

	  if (!seq.isNormalExecution()) {
		  if (Log.isLoggingOn()) {
			  Log.logLine(
					  "Making all indices inactive (exception thrown, or failure revealed during execution).");
			  Log.logLine(
					  "Statement with non-normal execution: "
							  + seq.statementToCodeString(seq.getNonNormalExecutionIndex()));
		  }
		  return;
	  }

	  // Clear the active flags of some statements
	  int i = seq.sequence.size() -1;

	  // If there is no return value, clear its active flag
	  // Cast succeeds because of isNormalExecution clause earlier in this
	  // method.
	  NormalExecution e = (NormalExecution) seq.getResult(i);
	  Object runtimeValue = e.getRuntimeValue();
	  if (runtimeValue == null) {
		  if (Log.isLoggingOn()) {
			  Log.logLine("Making index " + i + " inactive (value is null)");
		  }
		  return;
	  }

	  // If it is a call to an observer method, clear the active flag of
	  // its receiver. (This method doesn't side effect the receiver, so
	  // Randoop should use the other shorter sequence that produces the
	  // receiver.)
	  Sequence stmts = seq.sequence;
	  Statement stmt = stmts.statements.get(i);
	  /*
	  if (stmt.isMethodCall() && observers.contains(stmt.getOperation())) {
		  List<Integer> inputVars = stmts.getInputsAsAbsoluteIndices(i);
		  int receiver = inputVars.get(0);
		  seq.sequence.clearActiveFlag(receiver);
	  }*/

	  // If its runtime value is a primitive value, clear its active flag,
	  // and if the value is new, add a sequence corresponding to that value.
	  Class<?> objectClass = runtimeValue.getClass();
	  if (NonreceiverTerm.isNonreceiverType(objectClass) && !objectClass.equals(Class.class)) {
		  if (Log.isLoggingOn()) {
			  Log.logLine("Making index " + i + " inactive (value is a primitive)");
		  }

		  boolean looksLikeObjToString =
				  (runtimeValue instanceof String)
				  && Value.looksLikeObjectToString((String) runtimeValue);
		  boolean tooLongString =
				  (runtimeValue instanceof String) && !Value.stringLengthOK((String) runtimeValue);
		  if (runtimeValue instanceof Double && Double.isNaN((double) runtimeValue)) {
			  runtimeValue = Double.NaN; // canonicalize NaN value
		  }
		  if (runtimeValue instanceof Float && Float.isNaN((float) runtimeValue)) {
			  runtimeValue = Float.NaN; // canonicalize NaN value
		  }
		  if (!looksLikeObjToString && !tooLongString && runtimePrimitivesSeen.add(runtimeValue)) {
			  // Have not seen this value before; add it to the component set.
			  if (FieldBasedGenLog.isLoggingOn()) 
				  FieldBasedGenLog.logLine("> New primitive value stored: " + runtimeValue.toString());

			  componentManager.addGeneratedSequence(Sequence.createSequenceForPrimitive(runtimeValue));
		  }
	  } else {
		  if (Log.isLoggingOn()) {
			  Log.logLine("Making index " + i + " active.");
		  }
	  }

} 


  
// Second phase all combinations  
//// Generation first add one of each observer operation to the end of the sequence
//for (ExecutableSequence eSeq: positiveRegressionSeqs) {
//	
////	Sequence origSeq = eSeq.sequence;
//	
//	ExecutableSequence neweSeq = eSeq;
//
//	RandomPerm.randomPermutation(operationsPermutable);
//	// Should randomly mix the operations list each time we do this to avoid always the same execution order.
//    for (TypedOperation operation: operationsPermutable) {
//
//	    	int lastIndex = neweSeq.sequence.size() - 1;
//
//	    	if (FieldBasedGenLog.isLoggingOn())
//	    		FieldBasedGenLog.logLine("\n> Starting an attempt to extend sequence:\n " + neweSeq.sequence.toCodeString());
//
//		TypedOperation seqLastStmtOp = neweSeq.sequence.getStatement(lastIndex).getOperation();
//		
//		TypeTuple seqLastStmtInputTypes = seqLastStmtOp.getInputTypes();
//		TypeTuple seqLastStmtOutputTypes = new TypeTuple();
//
//		List<Boolean> isLastStmtVarActive = neweSeq.getLastStmtActiveVars();
//		if (!seqLastStmtOp.getOutputType().isVoid())
//			seqLastStmtOutputTypes.list.add(seqLastStmtOp.getOutputType());
//		for (int i = 0; i < seqLastStmtInputTypes.size(); i++)
//			seqLastStmtOutputTypes.list.add(seqLastStmtInputTypes.get(i));
//
//    	if (FieldBasedGenLog.isLoggingOn())
//    		FieldBasedGenLog.logLine("> Operation: " + operation.toString());
//
//     	if (!operation.isObserver()) {
//			 if (FieldBasedGenLog.isLoggingOn())
//				FieldBasedGenLog.logLine("> The current operation has been flagged as a modifier in the second phase, don't use it to extend tests anymore");
//			//System.out.println("> Operation " + operation.toString() + " has been flagged as a modifier by the field based approach, don't use it to build additional tests");
//     		continue;
//    	}
//    	
//    	if (operation.isConstructorCall()) {
//    		if (FieldBasedGenLog.isLoggingOn())
//    			FieldBasedGenLog.logLine("> The current operation is a constructor, don't use it to extend tests");
//    		continue;
//    	}
//    	
//    	// The first integer of the tuple is the index of the variable chosen from newSeq, 
//    	// the second integer is the corresponding index of a compatible type in operation
//    	TypeTuple inputTypes = operation.getInputTypes();
//    	Tuple<Integer, Integer> connection = getRandomConnectionBetweenTypeTuples(seqLastStmtOutputTypes, isLastStmtVarActive, inputTypes);
//    	if (connection == null) continue;
//    	
//        List<Sequence> sequences = new ArrayList<>();
//        int totStatements = 0;
//        List<Integer> variables = new ArrayList<>();
//        
//    	boolean error = false;
//    	for (int i = 0; i < inputTypes.size(); i++) {
//    		Type inputType = inputTypes.get(i);
//    		if (FieldBasedGenLog.isLoggingOn()) 
//				FieldBasedGenLog.logLine("> Input type: " + inputType.toString());
//
//    		Sequence chosenSeq;
//    		if (i == connection.getSecond()) {
//    			chosenSeq = neweSeq.sequence; 
//
//    			if (FieldBasedGenLog.isLoggingOn()) 
//    				FieldBasedGenLog.logLine("> Sequence to be extended selected: ");
//    		}
//    		else {
//    			SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
//    			if (l.isEmpty()) {
//					error = true;
//					break;
//    			}
//
//    			chosenSeq = Randomness.randomMember(l);
//
//    			if (FieldBasedGenLog.isLoggingOn()) 
//    				FieldBasedGenLog.logLine("> Random sequence selected: ");
//    		}
//
//			if (FieldBasedGenLog.isLoggingOn()) 
//				FieldBasedGenLog.logLine(chosenSeq.toCodeString());
//
//    	    // Now, find values that satisfy the constraint set.
//    	    Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);
//
//    	      if (randomVariable == null) {
//    	        throw new BugInRandoopException("type: " + inputType + ", sequence: " + chosenSeq);
//    	      }
//
//    	      // Fail, if we were unlucky and selected a null or primitive value as the
//    	      // receiver for a method call.
//    	      if (i == 0
//    	          && operation.isMessage()
//    	          && !(operation.isStatic())
//    	          && (chosenSeq.getCreatingStatement(randomVariable).isPrimitiveInitialization()
//    	              || randomVariable.getType().isPrimitive())) {
//
//  				error = true;
//  				break;
//  			}
//
//    	    variables.add(totStatements + randomVariable.index);
//    	    sequences.add(chosenSeq);
//    	    totStatements += chosenSeq.size();
//    	}
//	
//		if (error) continue;
//		InputsAndSuccessFlag isequences = new InputsAndSuccessFlag(true, sequences, variables);
//		
//		Sequence concatSeq = Sequence.concatenate(isequences.sequences);
//		// Figure out input variables.
//		List<Variable> inputs = new ArrayList<>();
//		for (Integer oneinput : isequences.indices) {
//			Variable v = concatSeq.getVariable(oneinput);
//			inputs.add(v);
//		}
//
//		Sequence newSequence = concatSeq.extend(operation, inputs);
//		
//		// Discard if sequence is larger than size limit
//		if (newSequence.size() > GenInputsAbstract.maxsize) {
//		  if (Log.isLoggingOn()) {
//			Log.logLine(
//				"Sequence discarded because size "
//					+ newSequence.size()
//					+ " exceeds maximum allowed size "
//					+ GenInputsAbstract.maxsize);
//		  }
//		  // This sequence is already too large, continue with the next sequence
//		  break;
//		}
//		
//		if (FieldBasedGenLog.isLoggingOn())
//			FieldBasedGenLog.logLine("> Resulting sequence: \n" + newSequence.toCodeString()); 
//
//		num_sequences_generated++;
//		
//	    if (this.allSequences.contains(newSequence)) {
//	        if (Log.isLoggingOn()) {
//	          Log.logLine("Sequence discarded because the same sequence was previously created.");
//	        }
//	        
//	        if (FieldBasedGenLog.isLoggingOn())
//	        	FieldBasedGenLog.logLine("> The current sequence was generated twice in the second phase \n"); 
//	        
//	        continue;
//	    }
//
//	    this.allSequences.add(newSequence);	
//		
//		ExecutableSequence eSeq2ndPhase = new ExecutableSequence(newSequence);
//		    setCurrentSequence(eSeq2ndPhase.sequence);
//		    executeExtendedSequence(eSeq2ndPhase);
//		eSeq2ndPhase.clearLastStmtExtensions();
//
//		if (operation.isModifier()) {
//			FieldBasedGenLog.logLine("> Operation " + operation.toString() + " was incorrectly flagged as observer in the first phase. Discarding test" ); 
//			operationBadTag++;
//			continue;
//		}
//
//		if (eSeq2ndPhase.hasFailure()) {
//			num_failing_sequences++;
//		}
//
//		if (outputTest.test(eSeq2ndPhase)) {
//
//			if (!eSeq2ndPhase.hasInvalidBehavior()) {
//				if (eSeq2ndPhase.hasFailure()) {
//					outErrorSeqs.add(eSeq2ndPhase);
//					genFirstAdditionalErrorSeqs++;
//
//					if (FieldBasedGenLog.isLoggingOn()) 
//						FieldBasedGenLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");
//				} 
//				else {
//						outRegressionSeqs.add(eSeq2ndPhase);
//					if (eSeq2ndPhase.isNormalExecution()) {
//						if (FieldBasedGenLog.isLoggingOn()) 
//							FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
//						// continue extending this sequence;
//						neweSeq = eSeq2ndPhase;
//						genFirstAdditionalPositiveSeqs++;
//					}
//					else {
//						if (FieldBasedGenLog.isLoggingOn()) 
//    						FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");
//						genFirstAdditionalNegativeSeqs++;
//					} 
//					// Add newSequence's inputs to subsumed_sequences
//					for (Sequence subsumed: isequences.sequences) {
//						subsumed_sequences.add(subsumed);
//					}	
//				}
//			}
//			else {
//				System.out.println("> ERROR: Sequence with invalid behavior in the second phase:");
//				System.out.println(eSeq2ndPhase.toCodeString());
//				if (FieldBasedGenLog.isLoggingOn()) {
//					FieldBasedGenLog.logLine("> ERROR: Sequence with invalid behaviour in the second phase:");
//					FieldBasedGenLog.logLine(eSeq2ndPhase.toCodeString());
//				}
//				genFirstAdditionalErrorSeqs++;
//			}
//		}
//		else {
//			System.out.println("> ERROR: Failing sequence in the second phase:");
//			System.out.println(eSeq2ndPhase.toCodeString());
//			if (FieldBasedGenLog.isLoggingOn()) {
//				FieldBasedGenLog.logLine("> ERROR: Failing sequence in the second phase:");
//				FieldBasedGenLog.logLine(eSeq2ndPhase.toCodeString());
//			}
//			genFirstAdditionalErrorSeqs++;
//		}
//
//    }
//}

  
  
  private Integer getRandomConnectionBetweenTypes(Type observedType, TypeTuple inputTypes) {
	  
	  List<Integer> l = new ArrayList<>();

	  for (int j = 0; j < inputTypes.size(); j++) {
		  if (inputTypes.get(j).isAssignableFrom(observedType)) {
//		  if (inputTypes.get(j).equals(observedType)) {
			  /*
			  if (FieldBasedGenLog.isLoggingOn()) 
				  FieldBasedGenLog.logLine("> Type " + inputTypes.get(j).toString() + " (index " + j + ")"
//						  + " is equal to " + observedType.toString()); 
						  + " is assignable from " + observedType.toString()); 
						  */
			  
			  l.add(j);
		  }
	  }

/*	  if (FieldBasedGenLog.isLoggingOn()) 
    	  FieldBasedGenLog.logLine("> Connections among tuples: " + l.toString()); 
    	  */
	  
	  if (l.isEmpty())
		  return null;
	  else {
		  Integer res = Randomness.randomMember(l);

		  /*
		  if (FieldBasedGenLog.isLoggingOn()) 
			  FieldBasedGenLog.logLine("> Selected connection: " + res.toString()); 
			  */
	
		  return res;
	  }
  }
  
  
  
  
  private Tuple<Integer, Integer> getRandomConnectionBetweenTypeTuples(TypeTuple seqLastStmtOutputTypes, List<Boolean> isLastStmtVarActive, TypeTuple inputTypes, boolean extendOnlyPrimitive) {
	  
	  List<Tuple<Integer, Integer>> l = new ArrayList<>();
	  for (int i = 0; i < seqLastStmtOutputTypes.size(); i++) {
		  if (//!extendPrimitive && !isLastStmtVarActive.get(i) ||
				  extendOnlyPrimitive && isLastStmtVarActive.get(i)) 
			  continue;
		  
		  for (int j = 0; j < inputTypes.size(); j++) {
			  if (inputTypes.get(j).isAssignableFrom(seqLastStmtOutputTypes.get(i))) {
				  /*if (FieldBasedGenLog.isLoggingOn()) 
					  FieldBasedGenLog.logLine("> Type " + inputTypes.get(j).toString() + " (index " + j + ")"
					  		+ " is assignable from " + seqLastStmtOutputTypes.get(i) + " (index " + i + ")"); */
				  
				  l.add(new Tuple<Integer, Integer>(i,j));
			  }
		  }
	  }

/*	  if (FieldBasedGenLog.isLoggingOn()) 
    	  FieldBasedGenLog.logLine("> Connections among tuples: " + l.toString()); 
    	  */
	  
	  if (l.isEmpty())
		  return null;
	  else {
		  Tuple<Integer, Integer> res = Randomness.randomMember(l);

		  /*
		  if (FieldBasedGenLog.isLoggingOn()) 
			  FieldBasedGenLog.logLine("> Selected connection: " + res.toString()); 
			  */
	
		  return res;
	  }
  }
  
     // Generation first field all combinations approach
        
    /*
    for (TypedOperation operation: operations) {
    	
    	if (FieldBasedGenLog.isLoggingOn())
    		FieldBasedGenLog.logLine("> Operation: " + operation.toString());

     	if (modifierOps.contains(operation)) {
			 if (FieldBasedGenLog.isLoggingOn())
				FieldBasedGenLog.logLine("> The current operation has been flagged as a modifier by the field based approach, don't use it to build additional tests");
			 
			//System.out.println("> Operation " + operation.toString() + " has been flagged as a modifier by the field based approach, don't use it to build additional tests");
     		//continue;
    	}
   	
    	if (operation.isConstructorCall()) {
    		if (FieldBasedGenLog.isLoggingOn())
    			FieldBasedGenLog.logLine("> The current operation is a constructor, don't use it to build additional tests");
    		continue;
    	}
    		
    	TypeTuple inputTypes = operation.getInputTypes();

    	ArrayList<SimpleList<Sequence>> allParams = new ArrayList<>();
    	for (int i = 0; i < inputTypes.size(); i++) {

    		Type inputType = inputTypes.get(i);
    		SimpleList<Sequence> sequences = componentManager.getSequencesForType(operation, i);
    		allParams.add(sequences);

    		if (FieldBasedGenLog.isLoggingOn()) {
				FieldBasedGenLog.logLine("> Input type: " + inputType.toString());
				FieldBasedGenLog.logLine("> Sequences for this type: ");
				for (int j = 0; j < sequences.size(); j++) 
					FieldBasedGenLog.logLine(sequences.get(j).toCodeString());
    		}

    	}

    	TupleGenerator<Sequence> tupleGen = new TupleGenerator<>(allParams);
    	while (tupleGen.hasNext()) {

    		List<Sequence> sequences = new ArrayList<>();
    		int totStatements = 0;
    		List<Integer> variables = new ArrayList<>();

    		if (FieldBasedGenLog.isLoggingOn())
    			FieldBasedGenLog.logLine("> Current tuple: ");

    		int i = 0;
    		boolean error = false;
    		for (Sequence chosenSeq: tupleGen.next()) {
    			
        		if (FieldBasedGenLog.isLoggingOn())
        			FieldBasedGenLog.logLine(chosenSeq.toCodeString() + "(" + inputTypes.get(i) + "),"); 			
    			
    			Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputTypes.get(i));
    			if (randomVariable == null) {
    				throw new BugInRandoopException("type: " + inputTypes.get(i) + ", sequence: " + chosenSeq);
    			}

    			// Fail, if we were unlucky and selected a null or primitive value as the
    			// receiver for a method call.
    			if (i == 0
    					&& operation.isMessage()
    					&& !(operation.isStatic())
    					&& (chosenSeq.getCreatingStatement(randomVariable).isPrimitiveInitialization()
    							|| randomVariable.getType().isPrimitive())) {

    	    		if (FieldBasedGenLog.isLoggingOn())
    	    			FieldBasedGenLog.logLine("> FIELD BASED GEN WARNING: Selected null or primitive value? What does this mean?");

    				
    				error = true;
    				break;
    			}

    			variables.add(totStatements + randomVariable.index);
    			sequences.add(chosenSeq);
    			totStatements += chosenSeq.size();

    			i++;
    		}

    		if (error) continue;

    		InputsAndSuccessFlag isequences = new InputsAndSuccessFlag(true, sequences, variables);
    		
    		Sequence concatSeq = Sequence.concatenate(isequences.sequences);
    		// Figure out input variables.
    		List<Variable> inputs = new ArrayList<>();
    		for (Integer oneinput : isequences.indices) {
    			Variable v = concatSeq.getVariable(oneinput);
    			inputs.add(v);
    		}

    		Sequence newSequence = concatSeq.extend(operation, inputs);
    		
    		if (FieldBasedGenLog.isLoggingOn())
    			FieldBasedGenLog.logLine("> Resulting sequence: \n" + newSequence.toCodeString()); 

    		ExecutableSequence newExecutableSequence = new ExecutableSequence(newSequence);
    		outRegressionSeqs.add(newExecutableSequence);
    		genFirstAdditionalSeqs++;
    		// TODO: Execute newSequence and add it to outRegressionSeqs if execution succeeds

    		// Add newSequence's inputs to subsumed_sequences
    		for (Sequence subsumed: isequences.sequences) {
    			subsumed_sequences.add(subsumed);
    		}	
    	}

    }*/
    
    
  

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
    System.out.println(">>>> Subsumed: " + subsumed_seqs.size());
    System.out.println(">>>> Output seqs: " + outRegressionSeqs.size());
    for (ExecutableSequence es : outRegressionSeqs) {
      if (!subsumed_seqs.contains(es.sequence)) {
        unique_seqs.add(es);
      }
    }
    
    System.out.println(">>>> Unique: " + unique_seqs.size());
    
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
  
  
  protected void setFieldBasedSequences(List<Sequence> s) {
	fbSeq = s;
  }

public void executeExtendedSequence(ExecutableSequence eSeq) {
	// TODO Auto-generated method stub
}

public void executeExtendedSequenceNoReexecute(ExecutableSequence eSeq, ExecutableSequence extendedSeq, int startIndex,
		int endIndex) {
	// TODO Auto-generated method stub
	
}


}

