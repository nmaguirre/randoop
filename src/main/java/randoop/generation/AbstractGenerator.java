package randoop.generation;

import plume.Option;
import plume.OptionGroup;
import plume.Unpublicized;
import randoop.*;
import randoop.generation.AbstractGenerator.FieldBasedGenType;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.test.TestCheckGenerator;
import randoop.util.Log;
import randoop.util.ProgressDisplay;
import randoop.util.Randomness;
import randoop.util.ReflectionExecutor;
import randoop.util.SimpleList;
import randoop.util.Timer;
import randoop.util.WeightedElement;
import randoop.util.fieldbasedcontrol.FieldBasedGenLog;
import randoop.util.fieldbasedcontrol.FieldExtensionsStrings;
import randoop.util.fieldbasedcontrol.HeapCanonizerListStore;
import randoop.util.fieldbasedcontrol.HeapCanonizerMapStore;
import randoop.util.fieldbasedcontrol.HeapCanonizerRuntimeEfficient;
import randoop.util.fieldbasedcontrol.HeapCanonizer;
import randoop.util.predicate.AlwaysFalse;
import randoop.util.predicate.Predicate;

import java.util.ArrayList;
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
	
 
  public int fieldBasedDroppedSeq = 0;
	
  public enum FieldBasedGenType {
	    /** Field based generation is disabled */
	    DISABLED,
	    /** Fast but not very precise field based generation */
	    FAST,
	    /** Field based generation with test miminization. Slower but precise */
	    MIN,
	    /** Field based generation with test miminization. Much slower but very precise */
	    MINPRECISE
	  }
  
  
  @OptionGroup("Field based generation")  
  @Option("Choose a field based generation approach. Field based generation can be done "
  		+ "considering only the last sentence of each test (value: FAST), or taking into"
  		+ "account all the sentences of each test (value: MIN). The latter approach can "
  		+ "be made more precise at the expense of a slower runtime (value: MINPRECISE) "
  		+ "The minimization approach is slower but it might produce fewer and better tests"
  		+ "(they tend to produce better results in testing evaluation metrics)."  
  		+ "Field based generation can also be disabled (value: DISABLED)."
  		+ "")
  public static FieldBasedGenType field_based_gen = FieldBasedGenType.FAST;
  
  @Option("Ignore primitive values in the construction of field extensions")
  public static boolean field_based_gen_ignore_primitive = false;
  
  @Option("Keep a percentage of the tests that did not contribute to the field extensions")
  public static float field_based_gen_keep_non_contributing_tests_percentage = 1;
  
  @Option("Activate regression tests to try to find bugs in the different implementations of the field extensions. "
  		+ "When active it slowdowns the analysis a lot. Only for debug purposes")
  public static boolean field_based_gen_differential_runtime_checks = false;

 @Option("Only consider arrays with up to this number of elements for augmenting the extensions")
  public static int field_based_gen_max_array = 10000;

 @Option("Only store up to this number of objects for each individual class during canonization")
  public static int field_based_gen_max_objects = 100000;



//   @Option("Use a precise, but slower heap canonization. The faster canonization relies on the HashCode method of classes under test, which might be bugged, and its use is not recommended")
//  public static boolean field_based_gen_precise_canonization = true;
  
  
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
	  canonizer = new HeapCanonizerRuntimeEfficient(field_based_gen_ignore_primitive, field_based_gen_max_objects, field_based_gen_max_array);  
  }
  
  public void initCanonizer(Set<String> fieldBasedGenClassnames) {
	  canonizer = new HeapCanonizerRuntimeEfficient(field_based_gen_ignore_primitive, fieldBasedGenClassnames, field_based_gen_max_objects, field_based_gen_max_array);  
  }
  

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


  protected List<Sequence> fbSeq;

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
//    	System.out.println("NULL SEQUENCE");
        continue;
      }

      num_sequences_generated++;
      
      if (eSeq.hasFailure()) {
        num_failing_sequences++;
      }
      
      // TODO: If we are going to throw away a test because it didn't contribute to the extensions, do it before 
      // any of the checks below are performed to improve performance.
      if (outputTest.test(eSeq)) {
    	  
        if (!eSeq.hasInvalidBehavior()) {
        	boolean stored = false;

        	if (eSeq.hasFailure() || eSeq.canonizationError) {
        		outErrorSeqs.add(eSeq);
        		stored = true;

        		if (FieldBasedGenLog.isLoggingOn()) 
        			FieldBasedGenLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");

        	} else {
        		
        		if (field_based_gen_keep_non_contributing_tests_percentage == 1 || (eSeq.isNormalExecution() && eSeq.enlargesExtensions) || !eSeq.isNormalExecution()) {
            		if (FieldBasedGenLog.isLoggingOn()) {
            			if (eSeq.isNormalExecution())
            				FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
            			else
            				FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");
            		}
        			
        			outRegressionSeqs.add(eSeq);
        			stored = true;
        		}
        		else {
	        		if (Randomness.weighedCoinFlip(field_based_gen_keep_non_contributing_tests_percentage)) {
	        			
						if (FieldBasedGenLog.isLoggingOn()) {
							FieldBasedGenLog.logLine("> Coin flip result: true");
							
							if (eSeq.isNormalExecution())
								FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
							else
								FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");
						}
						
						outRegressionSeqs.add(eSeq);
						stored = true;
	        			
	        		} else{
						if (FieldBasedGenLog.isLoggingOn()) {
							FieldBasedGenLog.logLine("> Coin flip result: false");
							FieldBasedGenLog.logLine("> Current sequence dropped");
						}
						
						stored = false;
	        		}
        		}
        		
        		/*
        		// Execution of the current sequence was successful 
        		if (field_based_gen_keep_non_contributing_tests_percentage != 1 && eSeq.isNormalExecution() && !eSeq.enlargesExtensions) {
        			stored = false;
        			if (FieldBasedGenLog.isLoggingOn())
        				FieldBasedGenLog.logLine("> Current sequence and subsumption candidates discarded");
        		}
        		else {
            		if (FieldBasedGenLog.isLoggingOn()) {
            			if (eSeq.isNormalExecution())
            				FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
            			else
            				FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");
            		}
        			
        			outRegressionSeqs.add(eSeq);
        			stored = true;
        		}
        		*/
        		
        	}

        	// If the sequence was stored there are subsumed sequences, save them
        	if (field_based_gen_keep_non_contributing_tests_percentage != 1 && stored) {
        		for (Sequence is : subsumed_candidates) {
        			subsumed_sequences.add(is);
        		}

        		if (FieldBasedGenLog.isLoggingOn()) 
        			FieldBasedGenLog.logLine("> The candidates for subsumed sequences are now saved as subsumed");	
        	}
        	
         	if (field_based_gen_keep_non_contributing_tests_percentage != 1 && !stored) {
        		if (FieldBasedGenLog.isLoggingOn()) 
        			FieldBasedGenLog.logLine("> The candidates for subsumed sequences are dropped");	
         	}
       	

        }
        else {
        	System.out.println("> Sequence with invalid behavior:");
        	System.out.println(eSeq.sequence.toCodeString());
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
    
    System.out.println("Tests not augmenting extensions: " + fieldBasedDroppedSeq);


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
    }

    // Notify listeners that exploration is ending.
    if (listenerMgr != null) {
      listenerMgr.explorationEnd();
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
}

