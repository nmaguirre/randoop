package randoop.generation;

import plume.Option;
import plume.OptionGroup;
import plume.Unpublicized;
import randoop.*;
import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.fieldextensions.OperationManager.OpState;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.FieldBasedGen;
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
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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

  public int maxsize;
  
  /** Sequences that are used in other sequences (and are thus redundant) **/
  protected Set<Sequence> subsumed_sequences = new LinkedHashSet<>();



  /**
   * The set of ALL sequences ever generated, including sequences that were
   * executed and then discarded.
   */
  protected Set<Sequence> allSequences;
  
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

  /**
   * A filter to determine whether a sequence should be added to the output
   * sequence lists.
   */
  public Predicate<ExecutableSequence> outputTest;

  /**
   * Visitor to generate checks for a sequence.
   */
  protected TestCheckGenerator checkGenerator;

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
 * @param executionVisitor2 
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
    this.maxGeneratedSequences = maxGeneratedSequences;
    this.maxOutputSequences = maxOutSequences;
    this.operations = operations;
	this.executionVisitor = new DummyVisitor();
//    this.executionVisitor = executionVisitor;
    this.outputTest = new AlwaysFalse<>();

    if (componentManager == null) {
      this.componentManager = new ComponentManager();
    } else {
      this.componentManager = componentManager;
    }

    this.stopper = stopper;
    this.listenerMgr = listenerManager;
    
    if (GenInputsAbstract.field_based_gen == FieldBasedGen.GEN && 
    		GenInputsAbstract.fbg_extend_with_observers > 0 && 
    		GenInputsAbstract.fbg_observer_lines > 0)
    		this.maxsize = GenInputsAbstract.maxsize - GenInputsAbstract.fbg_observer_lines;
    else
    		this.maxsize = GenInputsAbstract.maxsize;
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
   * Returns the count of sequences generated so far by the generator.
   *
   * @return the number of sequences generated
   */
  public abstract int numGeneratedSequences();

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
          } else {
            outRegressionSeqs.add(eSeq);
          }
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

    // This is not needed now, but might be helpful in the future
    ExecutionVisitor visitorRef = executionVisitor;

    // TODO: Second phase: Extend tests with observers
    if (GenInputsAbstract.fbg_extend_with_observers > 0) {
    /*
    		for (ExecutableSequence eSeq: positiveRegressionSeqs) {
    			if (eSeq.getLastStmtOperation().isModifier()) 
    				modifierRegressionSeqs.add(eSeq);
    			else
    				observerRegressionSeqs.add(eSeq);
    		}
    		*/
	    System.out.println("\nSecond phase starting...");

       	long secondPhaseStartTime = System.currentTimeMillis();

    	  	ExtensionsCollectorInOutVisitor visitor = (ExtensionsCollectorInOutVisitor) visitorRef;
    		ArrayList<TypedOperation> simpleObserverOps = new ArrayList<>();
    		ArrayList<TypedOperation> observerOps = new ArrayList<>();
    		for (TypedOperation op: operations) {
    			if (op.isConstructorCall()) continue;
    			
    			if (visitor.getOperationState(op) != OpState.MODIFIER && visitor.getOperationState(op) != OpState.NOT_EXECUTED) {
    				if (op.isSimpleOp())
    					simpleObserverOps.add(op);
    				else
    					observerOps.add(op);
    			}
    		}

    		extendModifierTestsWithObservers(simpleObserverOps, observerOps);

    		long secondPhaseTime = (System.currentTimeMillis() - secondPhaseStartTime) / 1000;
    	    System.out.println("\nSecond phase execution time: " + secondPhaseTime  + " s");
    }
    
    
    
    if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
      progressDisplay.display();
      progressDisplay.shouldStop = true;
    }

    if (!GenInputsAbstract.noprogressdisplay) {
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
      

      if (GenInputsAbstract.fbg_debug) {
    	  	ExtensionsCollectorInOutVisitor visitor = (ExtensionsCollectorInOutVisitor) visitorRef;
    	  	for (TypedOperation op: operations) {
    		  System.out.println(op.toString() + 
		  ", \t\n Operation State: " + visitor.getOperationState(op) +
    		  ", \t\n Modifier executions: " + visitor.getNumberOfModifierExecutions(op) +
    		  ", \t\n Executions: " + visitor.getNumberOfExecutions(op) +
    		  ", \t\n Simple: " + op.isSimpleOp());
    	  	}
      }
    }

    // Notify listeners that exploration is ending.
    if (listenerMgr != null) {
      listenerMgr.explorationEnd();
    }
    
  }
  
 
	public static <T> void randomPermutation(List<T> l) {
		for (int i = 0; i < l.size(); i++) {	
			int exchangeInd = i + Randomness.nextRandomInt(l.size()-i);
			T temp = l.get(i);
			l.set(i, l.get(exchangeInd));
			l.set(exchangeInd, temp);
		}
	}
	
	  private Integer getRandomConnectionBetweenTypes(Type observedType, TypeTuple inputTypes) {
		  List<Integer> l = new ArrayList<>();
		  for (int j = 0; j < inputTypes.size(); j++) {
			  if (inputTypes.get(j).isAssignableFrom(observedType)) 
				  l.add(j);
		  }
		  if (l.isEmpty()) return null;
		  return Randomness.randomMember(l);
	  }

  
   private void extendModifierTestsWithObservers(List<TypedOperation> simpleObs, List<TypedOperation> obs) {
		  	  // Generation first adds one of each observer operation to the end of the sequence
	   List<ExecutableSequence> firstPhaseSeqs = new ArrayList<>() ;
	   for (ExecutableSequence s: outRegressionSeqs) {
		   if (s.isNormalExecution() && !subsumed_sequences.contains(s.sequence)) 
			   firstPhaseSeqs.add(s);
	   }
	   
		  	  for (ExecutableSequence eSeq: firstPhaseSeqs) {
		  		  // Implement a method to get the last method of a sequence.
		  		  List<Integer> candidates = null;
		  		  if (!eSeq.sequence.getFBActiveFlags().isEmpty()) {
		  			  candidates = eSeq.sequence.getFBActiveFlags();
		  		  }
		  		  else {
		  			  // TODO: Use runtime values compile time types to figure out 
		  			  candidates = new ArrayList<>();
		  			  candidates.addAll(eSeq.sequence.getLastStmtNonPrimitiveIndexes());
	  				  // Only primitive values as parameters for this sequence, continue to the next one
		  			  if (candidates.isEmpty()) continue;
		  		  }
		  
		  		  int j = Randomness.randomMember(candidates);
		  		  ExecutableSequence currentSeq = eSeq;
		  		  List<Type> seqLastStmtTypes = eSeq.sequence.getTypesForLastStatement();
		  		  Type observedType = seqLastStmtTypes.get(j);
		  		  int observedIndex = j;
		  
		  		  randomPermutation(simpleObs);
		  		  randomPermutation(obs);

		  		  if (GenInputsAbstract.fbg_extend_no_reexecute)
		  			  // Reexecute eSeq
		  			  eSeq.clearExecutionResults();
		  		  
		  		  int opsCount = 0;
		  		  int simpleObsInd = 0;
		  		  int obsInd = 0;
		  		  while (opsCount < GenInputsAbstract.fbg_extend_with_observers) {
//		  		  for (TypedOperation operation: operationsPermutable) {
		  			  /* if (operation.toString().contains("<get>") || operation.toString().contains("<set>")) {
		  				  continue;
		  			  } */
		  			  
		  			  TypedOperation operation = null;
		  			  // Take up to GenInputsAbstract.fbg_extend_with_observers/2 simple observers
		  			  if (simpleObsInd < simpleObs.size() && opsCount < GenInputsAbstract.fbg_extend_with_observers/2)
		  				  operation = simpleObs.get(simpleObsInd++);
		  			  // And then start taking observer operations...
		  			  else if (obsInd < obs.size())
		  				  operation = obs.get(obsInd++);
		  			  // Maybe we can still take some simple observers...
		  			  else if (simpleObsInd < simpleObs.size())
		  				  operation = simpleObs.get(simpleObsInd++);
		  			  else
		  				  break;
		  				  
	  				  //The current operation is a constructor, don't use it to extend tests";
		  			  if (operation.isConstructorCall()) continue;
		  
		  			  // The first integer of the tuple is the index of the variable chosen from newSeq, 
		  			  // the second integer is the corresponding index of a compatible type in operation
		  			  TypeTuple inputTypes = operation.getInputTypes();
		  			  Integer opIndex = getRandomConnectionBetweenTypes(observedType, inputTypes);
  					  // The current operation cannot observe type observedType
		  			  if (opIndex == null) continue;
		  
		  			  List<Sequence> sequences = new ArrayList<>();
		  			  int totStatements = 0;
		  			  List<Integer> variables = new ArrayList<>();
		  			  boolean error = false;
		  			  for (int i = 0; i < inputTypes.size(); i++) {
		  				  Type inputType = inputTypes.get(i);
		  				  Variable randomVariable;
		  				  Sequence chosenSeq;
		  				  if (i == opIndex) {
		  					  chosenSeq = currentSeq.sequence; 
		  					  randomVariable = chosenSeq.getVariablesOfLastStatement().get(observedIndex);
		  				  }
		  				  else {
		  					  SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
		  					  if (l.isEmpty()) {
		  						  error = true;
		  						  break;
		  					  }
		  					  chosenSeq = Randomness.randomMember(l);
		  					  randomVariable = chosenSeq.randomVariableForTypeLastStatementFB(inputType);
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
		  				  break;
		  			  }
		  			  
		  			  num_sequences_generated++;
		  			  
		  			  if (this.allSequences.contains(newSequence)) {
		  				  if (Log.isLoggingOn()) {
		  					  Log.logLine("Sequence discarded because the same sequence was previously created.");
		  				  }
		  				  continue;
		  			  }
		  			  this.allSequences.add(newSequence);	
		  			  
		  			  ExecutableSequence extendedSeq = new ExecutableSequence(newSequence);
		  			  if (GenInputsAbstract.fbg_extend_no_reexecute)
		  				  executeExtendedSequenceNoReexecute(extendedSeq, currentSeq, startIndex, endIndex);
		  			  else
		  				  extendedSeq.execute(new DummyVisitor(), checkGenerator);
		  			  //processLastSequenceStatement(extendedSeq);
		  
		  			  if (extendedSeq.hasFailure()) {
		  				  num_failing_sequences++;
		  			  }
		  
		  			  if (outputTest.test(extendedSeq)) {
		  				  if (!extendedSeq.hasInvalidBehavior()) {
		  					  if (extendedSeq.hasFailure()) {
		  						  outErrorSeqs.add(extendedSeq);
		  						  if (GenInputsAbstract.fbg_extend_no_reexecute)
		  							  currentSeq.clearExecutionResults();
		  					  } 
		  					  else {
		  						  // outRegressionSeqs.add(eSeq2ndPhase);
		  						  if (extendedSeq.isNormalExecution()) {
		  							  // continue extending this sequence;
		  							  currentSeq = extendedSeq;
		  
		  							  // opIndex was calculated over operation's input types. observedIndex must be calculated over the output types of the operation 
		  							  if (operation.getOutputType().isVoid())
		  								  observedIndex = opIndex;
		  							  else
		  								  observedIndex = opIndex + 1;
		  
		  							  assert currentSeq.sequence.getTypesForLastStatement().get(observedIndex).equals(observedType);

		  							  // Add newSequence's inputs to subsumed_sequences
		  							  for (Sequence subsumed: isequences.sequences) {
		  								  subsumed_sequences.add(subsumed);
		  							  }	
		  							  outRegressionSeqs.add(currentSeq);
		  							  opsCount++;
		  						  }
		  						  // Current sequence execution threw an exception
		  						  else {
		  							  // Results are invalidated by the method that thrown the exception; discard it
		  							  if (GenInputsAbstract.fbg_extend_no_reexecute)
		  								  currentSeq.clearExecutionResults();
		  						  } 
		  					  }
		  				  }
		  				  else {
		  					  // ERROR: Sequence with invalid behavior in the second phase
  							  if (GenInputsAbstract.fbg_extend_no_reexecute)
  								  currentSeq.clearExecutionResults();
		  				  }
		  			  }
		  			  else {
		  				  // ERROR: Failing sequence in the second phase
						  if (GenInputsAbstract.fbg_extend_no_reexecute)
							  currentSeq.clearExecutionResults();
		  			  }
		  		  }
		  	  }
	    }
   
   public void executeExtendedSequenceNoReexecute(ExecutableSequence extSeq, ExecutableSequence currSeq, int startIndex,
			int endIndex) {
		// TODO Auto-generated method stub
   		setCurrentSequence(extSeq.sequence);
   		extSeq.executeNoReexecute(new DummyVisitor(), checkGenerator, currSeq, startIndex, endIndex);
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
    System.out.printf("Generated tests: %d%n", numGeneratedSequences());
    System.out.printf("Regression test count before subsumption: %d%n", outRegressionSeqs.size());
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
