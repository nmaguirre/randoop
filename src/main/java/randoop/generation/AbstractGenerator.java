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
import randoop.sequence.Statement;
import randoop.sequence.Variable;
import randoop.test.TestCheckGenerator;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.MultiMap;
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
import randoop.util.fieldbasedcontrol.HeapCanonizerRuntimeEfficient.ExtendedExtensionsResult;
import randoop.util.fieldbasedcontrol.TupleGenerator;
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
	
 
  public int notPassingFieldBasedFilter = 0;
  public int seqsExceedingLimits = 0;
  public int fieldBasedDroppedSeqs = 0;
	
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
  protected boolean coinFlipRes = false;
  
  @Option("Keep a percentage of the negative tests generated")
  public static float keep_negative_tests_percentage = 1;
  private int negativeTestsGen;
  private int negativeTestsDropped;
  
  @Option("Activate regression tests to try to find bugs in the different implementations of the field extensions. "
  		+ "When active it slowdowns the analysis a lot. Only for debug purposes")
  public static boolean field_based_gen_differential_runtime_checks = false;

  @Option("Only consider strings with up to this number of elements for augmenting the extensions")
  public static int field_based_gen_max_string_length = 10000;

  @Option("Only consider arrays with up to this number of elements for augmenting the extensions")
  public static int field_based_gen_max_array = 10000;

  @Option("Only store up to this number of objects during canonization")
  public static int field_based_gen_max_objects = 100000;

  @Option("Only store up to this number of objects for each individual class during canonization")
  public static int field_based_gen_max_class_objects = 10000;

  @Option("Set to false to not allow tests exceeding object/array/string limits to be used as inputs for "
  		+ "other tests")
  public static boolean field_based_gen_drop_tests_exceeding_object_limits = false;

  @Option("Disable randoop's collections and arrays generation heuristic")
  public static boolean disable_collections_generation_heuristic = false;

  @Option("Allows field based generation to detect precisely which objects enlarge the extensions."
  		+ " This may negatively affect runtime performance.")
  public static boolean field_based_gen_precise_enlarging_objects_detection = false;

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
	  canonizer = new HeapCanonizerRuntimeEfficient(field_based_gen_ignore_primitive, field_based_gen_max_objects, field_based_gen_max_class_objects, field_based_gen_max_string_length, field_based_gen_max_array, field_based_gen_drop_tests_exceeding_object_limits);  
  }
  
  public void initCanonizer(Set<String> fieldBasedGenClassnames) {
	  canonizer = new HeapCanonizerRuntimeEfficient(field_based_gen_ignore_primitive, fieldBasedGenClassnames, field_based_gen_max_objects, field_based_gen_max_class_objects, field_based_gen_max_string_length, field_based_gen_max_array, field_based_gen_drop_tests_exceeding_object_limits);  
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
      

      if (outputTest.test(eSeq)) {
    	  
        if (!eSeq.hasInvalidBehavior()) {
        	boolean stored = false;

        	if (eSeq.hasFailure() || eSeq.canonizationError) {
        		outErrorSeqs.add(eSeq);
        		stored = true;

        		if (FieldBasedGenLog.isLoggingOn()) 
        			FieldBasedGenLog.logLine("> Current sequence reveals a failure, saved it as an error revealing test");

        	} else {
        		
        		if (field_based_gen == FieldBasedGenType.DISABLED ||
        				field_based_gen_keep_non_contributing_tests_percentage == 1 || 
        				(eSeq.isNormalExecution() && eSeq.enlargesExtensions == ExtendedExtensionsResult.EXTENDED) 
        				|| !eSeq.isNormalExecution()) {
        			
           			if (eSeq.isNormalExecution()) {
           				if (FieldBasedGenLog.isLoggingOn()) 
            				FieldBasedGenLog.logLine("> Current sequence saved as a regression test");
						 outRegressionSeqs.add(eSeq);
						 stored = true;
           			}
           			else {
        				negativeTestsGen++;
        				if (keep_negative_tests_percentage == 1) {
        					if (FieldBasedGenLog.isLoggingOn()) 
        						FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");
							outRegressionSeqs.add(eSeq);
							stored = true;
        				} 
        				else {
        					if (Randomness.weighedCoinFlip(keep_negative_tests_percentage)) {
        						if (FieldBasedGenLog.isLoggingOn()) {
        							FieldBasedGenLog.logLine("> Coin flip result: true");
        							FieldBasedGenLog.logLine("> Current sequence saved as a negative regression test");
        						}
       						
        						outRegressionSeqs.add(eSeq);
        						stored = true;
       						}
       						else {
         						if (FieldBasedGenLog.isLoggingOn()) {
        							FieldBasedGenLog.logLine("> Coin flip result: false");
        							FieldBasedGenLog.logLine("> Current sequence dropped");
        						}
        						stored = false;
       						}
        				}
            		}
        			
        		}
        		else {
	        		if (coinFlipRes) {
	        			
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
	        			fieldBasedDroppedSeqs++;
						if (FieldBasedGenLog.isLoggingOn()) {
							FieldBasedGenLog.logLine("> Coin flip result: false");
							FieldBasedGenLog.logLine("> Current sequence dropped");
						}
						
						stored = false;
	        		}
        		}
        		
        	}

        	// If the sequence was stored there are subsumed sequences, save them
        	if ((field_based_gen_keep_non_contributing_tests_percentage != 1 || 
        			keep_negative_tests_percentage != 1) && stored) {
        		for (Sequence is : subsumed_candidates) {
        			subsumed_sequences.add(is);
        		}

        		if (FieldBasedGenLog.isLoggingOn()) 
        			FieldBasedGenLog.logLine("> The candidates for subsumed sequences are now saved as subsumed");	
        	}
        	
         	if ((field_based_gen_keep_non_contributing_tests_percentage != 1 ||
         			keep_negative_tests_percentage != 1) && !stored) {
        		if (FieldBasedGenLog.isLoggingOn()) 
        			FieldBasedGenLog.logLine("> The candidates for subsumed sequences are dropped");	
         	}
       	

        }
        else {
        	System.out.println("> ERROR: Sequence with invalid behavior:");
        	System.out.println(eSeq.toCodeString());
      		if (FieldBasedGenLog.isLoggingOn()) {
    			FieldBasedGenLog.logLine("> ERROR: Sequence with invalid behaviour:");
    			FieldBasedGenLog.logLine(eSeq.toCodeString());
      		}
        }

      }
      else {
    	  System.out.println("> ERROR: Failing sequence:");
    	  System.out.println(eSeq.toCodeString());
    	  
  		if (FieldBasedGenLog.isLoggingOn()) {
			FieldBasedGenLog.logLine("> ERROR: Failing sequence:");
			FieldBasedGenLog.logLine(eSeq.toCodeString());
			
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
    
    
    if (FieldBasedGenLog.isLoggingOn())
    	FieldBasedGenLog.logLine("\n\n>> Second phase of the generation first approach starting");

    int genFirstAdditionalSeqs = 0;
    
    // Generation first field based approach
    for (TypedOperation operation: operations) {

    	if (FieldBasedGenLog.isLoggingOn())
    		FieldBasedGenLog.logLine("> Operation: " + operation.toString());
    	
    	if (operation.isConstructorCall()) {
    		if (FieldBasedGenLog.isLoggingOn())
    			FieldBasedGenLog.logLine("> The current operation is a constructor, moving to the next one");
    		continue;
    	}
    		
    	TypeTuple inputTypes = operation.getInputTypes();

    	ArrayList<SimpleList<Sequence>> allParams = new ArrayList<>();
    	for (int i = 0; i < inputTypes.size(); i++) {

    		Type inputType = inputTypes.get(i);
    		SimpleList<Sequence> sequences = componentManager.getSequencesForType(operation, i);
    		allParams.add(sequences);

    		if (FieldBasedGenLog.isLoggingOn()) {
    			if (!inputType.isPrimitive()) {
					FieldBasedGenLog.logLine("> Input type: " + inputType.toString());
					FieldBasedGenLog.logLine("> Sequences for this type: ");
					for (int j = 0; j < sequences.size(); j++) 
						FieldBasedGenLog.logLine(sequences.get(j).toCodeString());
    			}
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

    		genFirstAdditionalSeqs++;
    		// Execute newSequence and add it to outRegressionSeqs if execution succeeds

    		// Add newSequence's inputs to subsumed_sequences
    		
    	}

    }


    

    if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
      progressDisplay.display();
      progressDisplay.shouldStop = true;
    }

    if (!GenInputsAbstract.noprogressdisplay) {
      if (FieldBasedGenLog.isLoggingOn()) {
    	  FieldBasedGenLog.logLine("Additional sequences generated by the second stage of the generation first approach: " + genFirstAdditionalSeqs); 
    	  FieldBasedGenLog.logLine("Tests not augmenting extensions: " + notPassingFieldBasedFilter);
    	  FieldBasedGenLog.logLine("Tests excceding limits: " + seqsExceedingLimits);
    	  FieldBasedGenLog.logLine("Field based dropped tests: " + fieldBasedDroppedSeqs);
    	  FieldBasedGenLog.logLine("Negative tests generated: " + negativeTestsGen);
    	  FieldBasedGenLog.logLine("Negative tests discarded: " + negativeTestsDropped);
    	  FieldBasedGenLog.logLine("");
      }

      System.out.println();
   	  System.out.println("Additional sequences generated by the second stage of the generation first approach: " + genFirstAdditionalSeqs); 
   	  System.out.println("Tests not augmenting extensions: " + notPassingFieldBasedFilter);
      System.out.println("Tests excceding limits: " + seqsExceedingLimits);
      System.out.println("Field based dropped tests: " + fieldBasedDroppedSeqs);
      System.out.println("Negative tests generated: " + negativeTestsGen);
      System.out.println("Negative tests discarded: " + negativeTestsDropped);
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

