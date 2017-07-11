package randoop.generation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.checkerframework.checker.initialization.qual.NotOnlyInitialized;

import randoop.BugInRandoopException;
import randoop.DummyVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.SubTypeSet;
import randoop.generation.AbstractGenerator.FieldBasedGenType;
import randoop.main.GenInputsAbstract;
import randoop.main.GenTests;
import randoop.operation.NonreceiverTerm;
import randoop.operation.Operation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;
import randoop.sequence.Statement;
import randoop.sequence.Value;
import randoop.sequence.Variable;
import randoop.test.DummyCheckGenerator;
import randoop.types.JavaTypes;
import randoop.types.InstantiatedType;
import randoop.types.JDKTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.MultiMap;
import randoop.util.Randomness;
import randoop.util.SimpleList;
import randoop.util.WeightedElement;
import randoop.util.fieldbasedcontrol.CanonizationErrorException;
import randoop.util.fieldbasedcontrol.FieldBasedGenLog;
import randoop.util.fieldbasedcontrol.HeapCanonizerRuntimeEfficient.ExtendedExtensionsResult;
import randoop.util.heapcanonization.CanonicalClass;
import randoop.util.heapcanonization.CanonicalHeap;
import randoop.util.heapcanonization.CanonicalStore;
import randoop.util.heapcanonization.CanonizationResult;
import randoop.util.heapcanonization.CanonizerLog;
import randoop.util.heapcanonization.HeapCanonizer;
import randoop.util.heapcanonization.candidatevectors.CandidateVector;
import randoop.util.heapcanonization.candidatevectors.CandidateVectorGenerator;
import randoop.util.heapcanonization.candidatevectors.CandidateVectorsWriter;


/**
 * Randoop's forward, component-based generator.
 */
public class ForwardGenerator extends AbstractGenerator {

  // PABLO: Fields for field based generation
  // public FieldExtensions fieldExtensions;
  // public FieldExtensions fieldExtensionsCanonizer;
  // public boolean fieldBasedGen = true;
  //public boolean fieldBasedGen = false;
  private int canonizationErrorNum = 0;
  //	public boolean weightedRandomSelection = true;
  // public boolean weightedRandomSelection = false;
  

  /**
   * The set of ALL sequences ever generated, including sequences that were
   * executed and then discarded.
   */
//  private final Set<Sequence> allSequences;
  private final Set<TypedOperation> observers;

  
  /** Sequences that are used in other sequences (and are thus redundant) **/
  // PABLO: This was here in original randoop. For now I moved it up to AbstractGenerator 
  // because I needed it for subsumption related issues (ugly hack).
//  private Set<Sequence> subsumed_sequences = new LinkedHashSet<>();

//  private Set<Sequence> subsumed_candidates; 


  
  
  // For testing purposes only. If Globals.randooptestrun==false then the array
  // is never populated or queried. This set contains the same set of
  // components as the set "allsequences" above, but stores them as
  // strings obtained via the toCodeString() method.
  private final List<String> allsequencesAsCode = new ArrayList<>();

  // For testing purposes only.
  private final List<Sequence> allsequencesAsList = new ArrayList<>();


private int maxsize;

  // The set of all primitive values seen during generation and execution
  // of sequences. This set is used to tell if a new primitive value has
  // been generated, to add the value to the components.
  //private Set<Object> runtimePrimitivesSeen = new LinkedHashSet<>();


  public ForwardGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      RandoopListenerManager listenerManager) {
    this(
        operations,
        observers,
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        null,
        listenerManager);
  }

  public ForwardGenerator(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      ComponentManager componentManager,
      IStopper stopper,
      RandoopListenerManager listenerManager) {

    super(
        operations,
        timeMillis,
        maxGenSequences,
        maxOutSequences,
        componentManager,
        stopper,
        listenerManager);

    this.observers = observers;
    this.allSequences = new LinkedHashSet<>();
    
    // PABLO: Initialized field extensions
    //fieldExtensions = new FieldExtensions();
    //fieldExtensionsCanonizer = new FieldExtensions();
    
    
    // PABLO: Initialized operation weights for random selection
    setInitialOperationWeights();
    //initModifierOperationsHash();
    
    
    // FIXME: PABLO: Very ugly hack to initialize the canonizer. 
//    if (field_based_gen != FieldBasedGenType.DISABLED) {
    	if (GenInputsAbstract.field_based_gen_classlist == null)
    		initCanonizer();
    	else 
    		initCanonizer(GenTests.field_based_gen_classes);
//    }
    
    initializeRuntimePrimitivesSeen();
    
    /*
	if (FieldBasedGenLog.isLoggingOn()) {
		// Only log extensions with up to max_extensions_size_to_log elements to avoid a very large log file
		FieldBasedGenLog.logLine("> New field extensions, size " + canonizer.getExtensions().size() + ":");
		if (canonizer.getExtensions().size() <= max_extensions_size_to_log) 
			FieldBasedGenLog.logLine(canonizer.getExtensions().toString());
		else 
			FieldBasedGenLog.logLine("> Extensions exceed the log limit (" + max_extensions_size_to_log + ") and will not be shown");
	}
	*/

    if (field_based_gen != FieldBasedGenType.DISABLED)
    	this.maxsize = GenInputsAbstract.maxsize - AbstractGenerator.field_based_gen_reserved_observer_lines;
    else
    	this.maxsize = GenInputsAbstract.maxsize;
    
  }
  
  
  
  /**
   * The runtimePrimitivesSeen set contains primitive values seen during
   * generation/execution and is used to determine new values that should be
   * added to the component set. The component set initially contains a set of
   * primitive sequences; this method puts those primitives in this set.
   */
  // XXX this is goofy - these values are available in other ways
  private void initializeRuntimePrimitivesSeen() {
    for (Sequence s : componentManager.getAllPrimitiveSequences()) {
      ExecutableSequence es = new ExecutableSequence(s);

      if (field_based_gen == FieldBasedGenType.DISABLED)
    	 es.execute(new DummyVisitor(), new DummyCheckGenerator()); 
      else {
		  try {
			es.executeFB(new DummyVisitor(), new DummyCheckGenerator(), null);
		  } catch (CanonizationErrorException e2) {
				// Should never happen 
				e2.printStackTrace();
		  }
      }
    	  
      /*
      if (field_based_gen != FieldBasedGenType.DISABLED) {
		  try {
			es.enlargeExtensionsFast(canonizer, this);
		  } catch (CanonizationErrorException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		  }
      }
      */
      
      NormalExecution e = (NormalExecution) es.getResult(0);
      Object runtimeValue = e.getRuntimeValue();
      runtimePrimitivesSeen.add(runtimeValue);
    }
  }
  
  
    @Override
  public void executeExtendedSequenceNoReexecute(ExecutableSequence eSeq, ExecutableSequence extendedSeq, int startIndex,
		  int endIndex) {

    	long startTime = System.nanoTime();

    	/*
    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0) {
      componentManager.clearGeneratedSequences();
    }

    if (eSeq == null) {
      return null;
    }

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }
    	 */

    	setCurrentSequence(eSeq.sequence);

    	long endTime = System.nanoTime();

    	long gentime = endTime - startTime;
    	startTime = endTime; // reset start time.

    	try {
    		if (extendedSeq.hasNonExecutedStatements())
    			eSeq.executeFBSecondPhase(executionVisitor, checkGenerator, canonizer);
    		else
    			eSeq.executeFBSecondPhaseNoReexecute(executionVisitor, checkGenerator, canonizer, extendedSeq, startIndex, endIndex);
    	} catch (CanonizationErrorException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    	if (FieldBasedGenLog.isLoggingOn()) {
    		FieldBasedGenLog.logLine("> Executed current sequence: ");
    		FieldBasedGenLog.logLine(eSeq.sequence.toCodeString());
    	}

    	endTime = System.nanoTime();
    	eSeq.exectime = endTime - startTime;
    	startTime = endTime; // reset start time.


    	endTime = System.nanoTime();
    	gentime += endTime - startTime;
    	eSeq.gentime = gentime;

	}
  
    @Override
  public void executeExtendedSequence(ExecutableSequence eSeq) {

    long startTime = System.nanoTime();

    /*
    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0) {
      componentManager.clearGeneratedSequences();
    }

    if (eSeq == null) {
      return null;
    }

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }
    */

    setCurrentSequence(eSeq.sequence);

    long endTime = System.nanoTime();
    
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.
    
	try {
		eSeq.executeFBSecondPhase(executionVisitor, checkGenerator, canonizer);
	} catch (CanonizationErrorException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	if (FieldBasedGenLog.isLoggingOn()) {
		FieldBasedGenLog.logLine("> Executed current sequence: ");
		FieldBasedGenLog.logLine(eSeq.sequence.toCodeString());
	}
	
	endTime = System.nanoTime();
	eSeq.exectime = endTime - startTime;
	startTime = endTime; // reset start time.
    
   	
    endTime = System.nanoTime();
    gentime += endTime - startTime;
    eSeq.gentime = gentime;
    
  }

    
    
    
	// Use the new canonizer to generate a candidate vector for receiver object 
    // of the last method of the sequence
    private void makeCanonicalVectorsForLastStatement(ExecutableSequence eSeq) {
		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine("**********");
			CanonizerLog.logLine("Canonizing runtime objects in the last statement of sequence:\n" + eSeq.toCodeString());
			CanonizerLog.logLine("**********");
		}	

		List<Integer> activeVars = eSeq.sequence.getActiveVars(eSeq.sequence.size() -1);
		if (activeVars != null)
			assert AbstractGenerator.field_based_gen == FieldBasedGenType.EXTENSIONS: 
					"Active vars are only computed when running field based generation.";
		
		int index = -1;
		HeapCanonizer newCanonizer = AbstractGenerator.candVectCanonizer;
		CanonicalStore store = newCanonizer.getStore();
		for (Object o: eSeq.getLastStmtRuntimeObjects()) {
			index++;

			if (activeVars != null && !activeVars.contains(index)) 
				continue;
			if (CanonizerLog.isLoggingOn())
				CanonizerLog.logLine("INFO: Active variable index: " + index);
			
			Entry<CanonizationResult, CanonicalHeap> res;
			CanonicalClass rootClass = store.getCanonicalClass(o);
			// FIXME: Should check the compile time type of o instead of its runtime type to 
			// avoid generating null objects of other types? 
			if (o == null || (o != null && store.isGenerationClass(rootClass))) {
				// Root is not an object we are interested in generating a candidate vector for.
				// Notice that we are always interested in generating a candidate object for null, 
				// even if we don't know its type.
				res = AbstractGenerator.candVectCanonizer.traverseBreadthFirstAndCanonize(o);
				if (res.getKey() == CanonizationResult.OK) {
					CandidateVector candidateVector = candVectGenerator.makeCandidateVectorFrom(res.getValue());
							//CandidateVectorGenerator.printAsCandidateVector(res.getValue());
					candVectExtensions.addToExtensions(candidateVector);
					// FIXME: Assuming candidate vector writer is enabled, otherwise we don't reach this code.
					// if (CandidateVectorsWriter.isEnabled())
					CandidateVectorsWriter.logLine(candidateVector.toString());
				}
				else {
					// assert res.getKey() == CanonizationResult.LIMITS_EXCEEDED: "No other error message implemented yet.";
					if (res.getKey() != CanonizationResult.OK) {
						if (CanonizerLog.isLoggingOn()) {
							CanonizerLog.logLine("----------");
							CanonizerLog.logLine("Not canonizing an object that is larger than permitted "
									+ "by cand_vectors_max_objs=" + AbstractGenerator.cand_vect_max_objs);
							CanonizerLog.logLine("Error message: " + res.getKey());
							CanonizerLog.logLine("----------");
						}
					}
				}
			}
		}
    }
    
  
 
  @Override
  public ExecutableSequence step() {

    long startTime = System.nanoTime();

    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0) {
      componentManager.clearGeneratedSequences();
    }

    ExecutableSequence eSeq = createNewUniqueSequence();

    if (eSeq == null) {
      return null;
    }

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }

    setCurrentSequence(eSeq.sequence);

    long endTime = System.nanoTime();
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.
    
    // Original randoop behaviour
    if (field_based_gen == FieldBasedGenType.DISABLED) {
    	eSeq.execute(executionVisitor, checkGenerator);

    	endTime = System.nanoTime();

    	eSeq.exectime = endTime - startTime;
    	startTime = endTime; // reset start time.

    	processSequence(eSeq);

    	if (eSeq.sequence.hasActiveFlags()) {
    		componentManager.addGeneratedSequence(eSeq.sequence);

    		if (CandidateVectorsWriter.isEnabled())
    			makeCanonicalVectorsForLastStatement(eSeq);
    	}
    }
    else {
    	// FB randoop behaviour
    	try {
    		eSeq.executeFB(executionVisitor, checkGenerator, canonizer);
    		endTime = System.nanoTime();
    		eSeq.exectime = endTime - startTime;
    		startTime = endTime; // reset start time.

    		if (eSeq.isNormalExecution()) {

    			if (FieldBasedGenLog.isLoggingOn())
    				FieldBasedGenLog.logLine("> Current sequence executed normally. Try to enlarge field extensions");
    			
   				// Field based filtering is only done on non error sequences
    			if (field_based_gen == FieldBasedGenType.EXTENSIONS)
    				eSeq.tryToEnlargeExtensions(canonizer);
   				else 
   					addObjectHashesToStore(eSeq);
   				    				
    			if (eSeq.enlargesExtensions == ExtendedExtensionsResult.EXTENDED) {
    				
    				if (CandidateVectorsWriter.isEnabled())
    					makeCanonicalVectorsForLastStatement(eSeq);

    				if (eSeq.getLastStmtOperation().isModifier())
    					eSeq.getLastStmtOperation().timesExecutedInExtendingModifiers++;

    				testsExtendingExt++;
    				if (FieldBasedGenLog.isLoggingOn())
    					FieldBasedGenLog.logLine("> The current sequence contributed to field extensions");

    				processSequence(eSeq);

    				if (AbstractGenerator.field_based_gen_precise_enlarging_objects_detection) 
   						componentManager.addFieldBasedActiveSequence(eSeq.sequence);
    				else
    					componentManager.addGeneratedSequence(eSeq.sequence);

    				if (FieldBasedGenLog.isLoggingOn())
    					FieldBasedGenLog.logLine("> Current sequence stored to be used as input for other sequences");

    				if (FieldBasedGenLog.isLoggingOn() && field_based_gen == FieldBasedGenType.EXTENSIONS) {
    					// Only log extensions with up to max_extensions_size_to_log elements to avoid a very large log file
    					FieldBasedGenLog.logLine("> New field extensions, size " + canonizer.getExtensions().size() + ":");
    					if (canonizer.getExtensions().size() <= max_extensions_size_to_log) 
    						FieldBasedGenLog.logLine(canonizer.getExtensions().toString());
    					else 
    						FieldBasedGenLog.logLine("> Extensions exceed the log limit (" + max_extensions_size_to_log + ") and will not be shown");
    				}
    			}
    			else if (eSeq.enlargesExtensions == ExtendedExtensionsResult.LIMITS_EXCEEDED) {
    				assert false : "ERROR in field based generation, limits exceeded not implemented";
    				throw new RuntimeException("ERROR in field based generation, limits exceeded not implemented");
    			}
    			else {
    				if (FieldBasedGenLog.isLoggingOn())
    					FieldBasedGenLog.logLine("> The current sequence didn't contribute to field extensions");

    				eSeq.sequence.clearAllActiveFlags();
    				processSequence(eSeq);
    				testsNotExtendingExt++;
    			}

    		}
    		else {
    			// Execution finished with errors/failures 
    			if (FieldBasedGenLog.isLoggingOn()) 
    				FieldBasedGenLog.logLine("> Execution of the current sequence finished with exceptions or failures. Don't use the sequence to enlarge field extensions");

    			// Original randoop behavior when the current sequence produces an error
    			processSequence(eSeq);

    			if (eSeq.sequence.hasActiveFlags()) 
    				componentManager.addGeneratedSequence(eSeq.sequence);
    		}

    	}	
    	catch (CanonizationErrorException e) {
    		canonizationErrorNum++;
    		eSeq.sequence.clearAllActiveFlags();
    		System.out.println(eSeq.toCodeString());
    		System.out.println("ERROR: Number of canonization errors: " + canonizationErrorNum);
    		if (FieldBasedGenLog.isLoggingOn()) {
    			FieldBasedGenLog.logLine("> ERROR: Number of canonization errors: " + canonizationErrorNum + "Error sequence:");
    			FieldBasedGenLog.logLine(eSeq.toCodeString());
    		}
    		eSeq.canonizationError = true;
    	}
    			



    }

    endTime = System.nanoTime();
    gentime += endTime - startTime;
    eSeq.gentime = gentime;

    return eSeq;
  }
/*
  
  @Override
  public ExecutableSequence step() {

    long startTime = System.nanoTime();

    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0) {
      componentManager.clearGeneratedSequences();
    }

    ExecutableSequence eSeq = createNewUniqueSequence();

    if (eSeq == null) {
      return null;
    }

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }

    setCurrentSequence(eSeq.sequence);

    long endTime = System.nanoTime();
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.

    eSeq.execute(executionVisitor, checkGenerator, this, canonizer);

    // PABLO: This was here. Check if it is needed in fieldExhaustiveGeneration
    // processSequence(eSeq);
    
    if (eSeq.canonizationError) {
    	canonizationErrorNum++;
    	eSeq.sequence.clearAllActiveFlags();
    	System.out.println(eSeq.toCodeString());
    	System.out.println("ERROR: Number of canonization errors: " + canonizationErrorNum);
    }
    else if (eSeq.normalExecution && !eSeq.extendedExtensions) {
	    fieldBasedDroppedSeq++;
		eSeq.sequence.clearAllActiveFlags();
	}
	else {
        processSequence(eSeq);
	    if (eSeq.sequence.hasActiveFlags()) {
	    	componentManager.addFieldBasedGeneratedSequence(eSeq.sequence);
	    }

	    	/*
			try {
				canonizer.getExtensions().toFile(eSeq.FILENAME + ExecutableSequence.seqnum + ".txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	*//*

    }
    

    /*
    num_sequences_generated_fb = -1;
    if (fieldBasedGen) {
    	// Field based randoop behaviour

    	try {
    		boolean extendedExtensions = eSeq.enlargeExtensions(canonizer, this);
    	    // PABLO: If field extensions have not been augmented by this sequence, mark seq as not 
    	    // active so it is not considered for extension anymore.
    	    if (!extendedExtensions) {
    	    	fieldBasedDroppedSeq++;
    			eSeq.sequence.clearAllActiveFlags();
    			//System.out.println("Sequence number: " + eSeq.seqnum);
    			//System.out.println("Field based dropped sequences: " + fieldBasedDroppedSeq);
    			//System.out.println("Sequences - dropped: " + (eSeq.seqnum - fieldBasedDroppedSeq));
    	    } else {
    	    	
		        processSequence(eSeq);    	    	
    	    	List<Sequence> fbSequences = componentManager.addFieldBasedActiveSequences(eSeq.sequence);
   		        num_sequences_generated_fb = fbSequences.size(); 

    		    System.out.println("> Extensions size:" + canonizer.getExtensions().size());

    		    
   		        /*
				try {
					canonizer.getExtensions().toFile(eSeq.FILENAME + ExecutableSequence.seqnum + ".txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*//*
    		}
    	
    	}
    	catch (CanonizationErrorException e) {
        	canonizationErrorNum++;
        	eSeq.sequence.clearAllActiveFlags();
        	System.out.println(eSeq.toCodeString());
        	System.out.println("ERROR: Number of canonization errors: " + canonizationErrorNum);
    	}
    }
    else {
    	// Original randoop behaviour
		processSequence(eSeq);
		
	    if (eSeq.sequence.hasActiveFlags()) {
	      componentManager.addGeneratedSequence(eSeq.sequence);

	    }
    }*/
/*

    endTime = System.nanoTime();
    gentime += endTime - startTime;
    eSeq.gentime = gentime;

    return eSeq;
  }
  */
  
  
  /*
  @Override
  public ExecutableSequence step() {

    long startTime = System.nanoTime();

    if (componentManager.numGeneratedSequences() % GenInputsAbstract.clear == 0) {
      componentManager.clearGeneratedSequences();
    }

    ExecutableSequence eSeq = createNewUniqueSequence();

    if (eSeq == null) {
      return null;
    }

    if (GenInputsAbstract.dontexecute) {
      this.componentManager.addGeneratedSequence(eSeq.sequence);
      return null;
    }

    setCurrentSequence(eSeq.sequence);

    long endTime = System.nanoTime();
    long gentime = endTime - startTime;
    startTime = endTime; // reset start time.


    eSeq.execute(executionVisitor, checkGenerator/*, fieldExtensions, fieldBasedGen, canonizer*/ /*);

    endTime = System.nanoTime();

    eSeq.exectime = endTime - startTime;
    startTime = endTime; // reset start time.

    // PABLO: This was here. Check if it is needed in fieldExhaustiveGeneration
    // processSequence(eSeq);
    
    num_sequences_generated_fb = -1;
    if (fieldBasedGen) {
    	// Field based randoop behaviour

    	try {
    		boolean extendedExtensions = eSeq.enlargeExtensions(canonizer, this);
    	    // PABLO: If field extensions have not been augmented by this sequence, mark seq as not 
    	    // active so it is not considered for extension anymore.
    	    if (!extendedExtensions) {
    	    	fieldBasedDroppedSeq++;
    			eSeq.sequence.clearAllActiveFlags();
    			//System.out.println("Sequence number: " + eSeq.seqnum);
    			//System.out.println("Field based dropped sequences: " + fieldBasedDroppedSeq);
    			//System.out.println("Sequences - dropped: " + (eSeq.seqnum - fieldBasedDroppedSeq));
    	    } else {
    	    	
		        processSequence(eSeq);    	    	
    	    	List<Sequence> fbSequences = componentManager.addFieldBasedActiveSequences(eSeq.sequence);
   		        num_sequences_generated_fb = fbSequences.size(); 

    		    System.out.println("> Extensions size:" + canonizer.getExtensions().size());

    		    
   		        /*
				try {
					canonizer.getExtensions().toFile(eSeq.FILENAME + ExecutableSequence.seqnum + ".txt");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*//*
    		}
    	
    	}
    	catch (CanonizationErrorException e) {
        	canonizationErrorNum++;
        	eSeq.sequence.clearAllActiveFlags();
        	System.out.println(eSeq.toCodeString());
        	System.out.println("ERROR: Number of canonization errors: " + canonizationErrorNum);
    	}
    }
    else {
    	// Original randoop behaviour
		processSequence(eSeq);
		
	    if (eSeq.sequence.hasActiveFlags()) {
	      componentManager.addGeneratedSequence(eSeq.sequence);

	    }
    }


    endTime = System.nanoTime();
    gentime += endTime - startTime;
    eSeq.gentime = gentime;

    return eSeq;
  }
*/


  @Override
  public Set<Sequence> getAllSequences() {
    return Collections.unmodifiableSet(this.allSequences);
  }

  /**
   * Determines what indices in the given sequence are active. An active index i
   * means that the i-th method call creates an interesting/useful value that
   * can be used as an input to a larger sequence; inactive indices are never
   * used as inputs. The effect of setting active/inactive indices is that the
   * SequenceCollection to which the given sequences is added only considers the
   * active indices when deciding whether the sequence creates values of a given
   * type.
   * <p>
   * In addition to determining active indices, this method determines if any
   * primitive values created during execution of the sequence are new values
   * not encountered before. Such values are added to the component manager so
   * they can be used during subsequent generation attempts.
   *
   * @param seq  the sequence
   */
  private void processSequence(ExecutableSequence seq) {

    if (seq.hasNonExecutedStatements()) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Making all indices inactive (sequence has non-executed statements, so judging it inadequate for further extension).");
        Log.logLine(
            "Non-executed statement: " + seq.statementToCodeString(seq.getNonExecutedIndex()));
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasFailure()) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Making all indices inactive (sequence reveals a failure, so judging it inadequate for further extension)");
        Log.logLine("Failing sequence: " + seq.toCodeString());
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (seq.hasInvalidBehavior()) {
      if (Log.isLoggingOn()) {
        Log.logLine("Making all indices inactive (sequence has invalid behavior)");
        Log.logLine("Invalid sequence: " + seq.toCodeString());
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    if (!seq.isNormalExecution()) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Making all indices inactive (exception thrown, or failure revealed during execution).");
        Log.logLine(
            "Statement with non-normal execution: "
                + seq.statementToCodeString(seq.getNonNormalExecutionIndex()));
      }
      seq.sequence.clearAllActiveFlags();
      return;
    }

    // Clear the active flags of some statements
    for (int i = 0; i < seq.sequence.size(); i++) {

      // If there is no return value, clear its active flag
      // Cast succeeds because of isNormalExecution clause earlier in this
      // method.
      NormalExecution e = (NormalExecution) seq.getResult(i);
      Object runtimeValue = e.getRuntimeValue();
      if (runtimeValue == null) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (value is null)");
        }
        seq.sequence.clearActiveFlag(i);
        continue;
      }

      // If it is a call to an observer method, clear the active flag of
      // its receiver. (This method doesn't side effect the receiver, so
      // Randoop should use the other shorter sequence that produces the
      // receiver.)
      Sequence stmts = seq.sequence;
      Statement stmt = stmts.statements.get(i);
      if (stmt.isMethodCall() && observers.contains(stmt.getOperation())) {
        List<Integer> inputVars = stmts.getInputsAsAbsoluteIndices(i);
        int receiver = inputVars.get(0);
        seq.sequence.clearActiveFlag(receiver);
      }

      // If its runtime value is a primitive value, clear its active flag,
      // and if the value is new, add a sequence corresponding to that value.
      Class<?> objectClass = runtimeValue.getClass();
      if (NonreceiverTerm.isNonreceiverType(objectClass) && !objectClass.equals(Class.class)) {
        if (Log.isLoggingOn()) {
          Log.logLine("Making index " + i + " inactive (value is a primitive)");
        }
        seq.sequence.clearActiveFlag(i);

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
    
  }

  

  
  
 
  
  /**
   * Tries to create and execute a new sequence. If the sequence is new (not
   * already in the specified component manager), then it is executed and added
   * to the manager's sequences. If the sequence created is already in the
   * manager's sequences, this method has no effect, and returns null.
   *
   * @return a new sequence, or null
   */
  private ExecutableSequence createNewUniqueSequence() {

    if (Log.isLoggingOn()) {
      Log.logLine("-------------------------------------------");
    }

    if (this.operations.isEmpty()) {
      return null;
    }

    // Select a StatementInfo
    TypedOperation operation;
    
    if (field_based_gen_weighted_selection) {
    	// PABLO: Perform a weighted random selection
      operation = selectWeightedRandomOperation();
      if (FieldBasedGenLog.isLoggingOn()) 
    	  FieldBasedGenLog.logLine("> Selected operation: " + operation + " . Weight: " + getWeight(operation));
    }
    else
    	operation = Randomness.randomMember(this.operations);
    
    if (Log.isLoggingOn()) {
      Log.logLine("Selected operation: " + operation.toString());
    }

    // jhp: add flags here
    InputsAndSuccessFlag sequences = selectInputs(operation);

    if (!sequences.success) {
      if (Log.isLoggingOn()) Log.logLine("Failed to find inputs for statement.");
      return null;
    }

    Sequence concatSeq = Sequence.concatenate(sequences.sequences);

    // Figure out input variables.
    List<Variable> inputs = new ArrayList<>();
    for (Integer oneinput : sequences.indices) {
      Variable v = concatSeq.getVariable(oneinput);
      inputs.add(v);
    }

    Sequence newSequence = concatSeq.extend(operation, inputs);

    // With .5 probability, do a primitive value heuristic.
    if (GenInputsAbstract.repeat_heuristic && Randomness.nextRandomInt(10) == 0) {
      int times = Randomness.nextRandomInt(100);
      newSequence = repeat(newSequence, operation, times);
      if (Log.isLoggingOn()) Log.log(">>>" + times + newSequence.toCodeString());
    }

    // If parameterless statement, subsequence inputs
    // will all be redundant, so just remove it from list of statements.
    // XXX does this make sense? especially in presence of side-effects
    if (operation.getInputTypes().isEmpty()) {
      operations.remove(operation);
    }

    // Discard if sequence is larger than size limit
    if (newSequence.size() > this.maxsize) {
      if (Log.isLoggingOn()) {
        Log.logLine(
            "Sequence discarded because size "
                + newSequence.size()
                + " exceeds maximum allowed size "
                + GenInputsAbstract.maxsize);
      }
      return null;
    }

    randoopConsistencyTests(newSequence);

    
    if (this.allSequences.contains(newSequence)) {
      if (Log.isLoggingOn()) {
        Log.logLine("Sequence discarded because the same sequence was previously created.");
      }
      return null;
    }

    this.allSequences.add(newSequence);

    for (Sequence s : sequences.sequences) {
      s.lastTimeUsed = java.lang.System.currentTimeMillis();
    }
    
    
    randoopConsistencyTest2(newSequence);

    if (Log.isLoggingOn()) {
      Log.logLine(
          String.format("Successfully created new unique sequence:%n%s%n", newSequence.toString()));
    }
    // System.out.println("###" + statement.toStringVerbose() + "###" +
    // statement.getClass());

    // Keep track of any input sequences that are used in this sequence.
    // Tests that contain only these sequences are probably redundant.
    
    
    if (FieldBasedGenLog.isLoggingOn()) 
    	FieldBasedGenLog.logLine("\n\n>> New sequence constructed:\n" + newSequence.toCodeString());
 
    // PABLO: Subsumption changes when the flag field_based_gen_drop_non_contributing_tests is enabled
    if (field_based_gen != FieldBasedGenType.DISABLED || (field_based_gen == FieldBasedGenType.DISABLED && drop_randoop_negative_tests)) {
		// Temporarily store possibly subsumed sequences. They will be subsumed only if the current 
		// test is saved later
		if (FieldBasedGenLog.isLoggingOn())
			FieldBasedGenLog.logLine("> Temporarily store candidates for subsumed sequences");
		
		subsumed_candidates = new LinkedHashSet<>();
		for (Sequence is : sequences.sequences) {
		  subsumed_candidates.add(is);
		}
    }
    else {
   		if (FieldBasedGenLog.isLoggingOn()) 
   			FieldBasedGenLog.logLine("> New subsumed sequences stored");
    	    	
    	// PABLO: Original randoop behaviour
	    for (Sequence is : sequences.sequences) {
	      subsumed_sequences.add(is);
	    }
    }

    return new ExecutableSequence(newSequence);
  }

  /**
   * Adds the given operation to a new {@code Sequence} with the statements of
   * this object as a prefix, repeating the operation the given number of times.
   * Used during generation.
   *
   * @param seq  the sequence to extend
   * @param operation
   *          the {@link TypedOperation} to repeat.
   * @param times
   *          the number of times to repeat the {@link Operation}.
   * @return a new {@code Sequence}
   */
  private Sequence repeat(Sequence seq, TypedOperation operation, int times) {
    Sequence retval = new Sequence(seq.statements);
    for (int i = 0; i < times; i++) {
      List<Integer> vil = new ArrayList<>();
      for (Variable v : retval.getInputs(retval.size() - 1)) {
        if (v.getType().equals(JavaTypes.INT_TYPE)) {
          int randint = Randomness.nextRandomInt(100);
          retval =
              retval.extend(
                  TypedOperation.createPrimitiveInitialization(JavaTypes.INT_TYPE, randint));
          vil.add(retval.size() - 1);
        } else {
          vil.add(v.getDeclIndex());
        }
      }
      List<Variable> vl = new ArrayList<>();
      for (Integer vi : vil) {
        vl.add(retval.getVariable(vi));
      }
      retval = retval.extend(operation, vl);
    }
    return retval;
  }

  // Adds the string corresponding to the given newSequences to the
  // set allSequencesAsCode. The latter set is intended to mirror
  // the set allSequences, but stores strings instead of Sequences.
  private void randoopConsistencyTest2(Sequence newSequence) {
    // Testing code.
    if (GenInputsAbstract.debug_checks) {
      this.allsequencesAsCode.add(newSequence.toCodeString());
      this.allsequencesAsList.add(newSequence);
    }
  }

  // Checks that the set allSequencesAsCode contains a set of strings
  // equivalent to the sequences in allSequences.
  private void randoopConsistencyTests(Sequence newSequence) {
    // Testing code.
    if (GenInputsAbstract.debug_checks) {
      String code = newSequence.toCodeString();
      if (this.allSequences.contains(newSequence)) {
        if (!this.allsequencesAsCode.contains(code)) {
          throw new IllegalStateException(code);
        }
      } else {
        if (this.allsequencesAsCode.contains(code)) {
          int index = this.allsequencesAsCode.indexOf(code);
          StringBuilder b = new StringBuilder();
          Sequence co = this.allsequencesAsList.get(index);
          assert co.equals(newSequence); // XXX this was a floating call to equals
          b.append("new component:")
              .append(Globals.lineSep)
              .append("")
              .append(newSequence.toString())
              .append("")
              .append(Globals.lineSep)
              .append("as code:")
              .append(Globals.lineSep)
              .append("")
              .append(code)
              .append(Globals.lineSep);
          b.append("existing component:")
              .append(Globals.lineSep)
              .append("")
              .append(this.allsequencesAsList.get(index).toString())
              .append("")
              .append(Globals.lineSep)
              .append("as code:")
              .append(Globals.lineSep)
              .append("")
              .append(this.allsequencesAsList.get(index).toCodeString());
          throw new IllegalStateException(b.toString());
        }
      }
    }
  }

  // This method is responsible for doing two things:
  //
  // 1. Selecting at random a collection of sequences that can be used to
  // create input values for the given statement, and
  //
  // 2. Selecting at random valid indices to the above sequence specifying
  // the values to be used as input to the statement.
  //
  // The selected sequences and indices are wrapped in an InputsAndSuccessFlag
  // object and returned. If an appropriate collection of sequences and indices
  // was not found (e.g. because there are no sequences in the componentManager
  // that create values of some type required by the statement), the success
  // flag
  // of the returned object is false.
  @SuppressWarnings("unchecked")
  private InputsAndSuccessFlag selectInputs(TypedOperation operation) {

    // Variable inputTypes contains the values required as input to the
    // statement given as a parameter to the selectInputs method.

    TypeTuple inputTypes = operation.getInputTypes();

    // The rest of the code in this method will attempt to create
    // a sequence that creates at least one value of type T for
    // every type T in inputTypes, and thus can be used to create all the
    // inputs for the statement.
    // We denote this goal sequence as "S". We don't create S explicitly, but
    // define it as the concatenation of the following list of sequences.
    // In other words, S = sequences[0] + ... + sequences[sequences.size()-1].
    // (This representation choice is for efficiency: it is cheaper to perform
    // a single concatenation of the subsequences in the end than repeatedly
    // extending S.)

    List<Sequence> sequences = new ArrayList<>();

    // We store the total size of S in the following variable.

    int totStatements = 0;

    // The method also returns a list of randomly-selected variables to
    // be used as inputs to the statement, represented as indices into S.
    // For example, given as statement a method M(T1)/T2 that takes as input
    // a value of type T1 and returns a value of type T2, this method might
    // return, for example, the sequence
    //
    // T0 var0 = new T0(); T1 var1 = var0.getT1()"
    //
    // and the singleton list [0] that represents variable var1. The variable
    // indices are stored in the following list. Upon successful completion
    // of this method, variables will contain inputTypes.size() variables.
    // Note additionally that for every i in variables, 0 <= i < |S|.

    List<Integer> variables = new ArrayList<>();

    // [Optimization]
    // The following two variables are used in the loop below only when
    // an alias ratio is present (GenInputsAbstract.alias_ratio != null).
    // Their purpose is purely to improve efficiency. For a given loop iteration
    // i, "types" contains the types of all variables in S, and "typesToVars"
    // maps each type to all variable indices of the given type.
    SubTypeSet types = new SubTypeSet(false);
    MultiMap<Type, Integer> typesToVars = new MultiMap<>();

    for (int i = 0; i < inputTypes.size(); i++) {
      Type inputType = inputTypes.get(i);

      // true if statement st represents an instance method, and we are
      // currently
      // selecting a value to act as the receiver for the method.
      boolean isReceiver = (i == 0 && (operation.isMessage()) && (!operation.isStatic()));

      // If alias ratio is given, attempt with some probability to use a
      // variable already in S.
      if (GenInputsAbstract.alias_ratio != 0
          && Randomness.weighedCoinFlip(GenInputsAbstract.alias_ratio)) {

        // candidateVars will store the indices that can serve as input to the
        // i-th input in st.
        List<SimpleList<Integer>> candidateVars = new ArrayList<>();

        // For each type T in S compatible with inputTypes[i], add all the
        // indices in S of type T.
        for (Type match : types.getMatches(inputType)) {
          // Sanity check: the domain of typesToVars contains all the types in
          // variable types.
          assert typesToVars.keySet().contains(match);
          candidateVars.add(
              new ArrayListSimpleList<>(new ArrayList<>(typesToVars.getValues(match))));
        }

        // If any type-compatible variables found, pick one at random as the
        // i-th input to st.
        SimpleList<Integer> candidateVars2 = new ListOfLists<>(candidateVars);
        if (!candidateVars2.isEmpty()) {
          int randVarIdx = Randomness.nextRandomInt(candidateVars2.size());
          Integer randVar = candidateVars2.get(randVarIdx);
          variables.add(randVar);
          continue;
        }
      }

      // If we got here, it means we will not attempt to use a value already
      // defined in S,
      // so we will have to augment S with new statements that yield a value of
      // type inputTypes[i].
      // We will do this by assembling a list of candidate sequences n(stored in
      // the list declared
      // immediately below) that create one or more values of the appropriate
      // type,
      // randomly selecting a single sequence from this list, and appending it
      // to S.
      SimpleList<Sequence> l;

      // We use one of two ways to gather candidate sequences, but the second
      // case below
      // is by far the most common.

      if (!disable_collections_generation_heuristic && 
    		  inputType.isArray()) {

        // 1. If T=inputTypes[i] is an array type, ask the component manager for
        // all sequences
        // of type T (list l1), but also try to directly build some sequences
        // that create arrays (list l2).
        SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i);
        if (Log.isLoggingOn()) {
          Log.logLine("Array creation heuristic: will create helper array of type " + inputType);
        }
        SimpleList<Sequence> l2 =
            HelperSequenceCreator.createArraySequence(componentManager, inputType);
        l = new ListOfLists<>(l1, l2);

      } else if (!disable_collections_generation_heuristic && 
    		  inputType.isParameterized()
          && ((InstantiatedType) inputType)
              .getGenericClassType()
              .isSubtypeOf(JDKTypes.COLLECTION_TYPE)) {
        InstantiatedType classType = (InstantiatedType) inputType;

        SimpleList<Sequence> l1 = componentManager.getSequencesForType(operation, i);
        if (Log.isLoggingOn()) {
          Log.logLine("Collection creation heuristic: will create helper of type " + classType);
        }
        ArrayListSimpleList<Sequence> l2 = new ArrayListSimpleList<>();
        Sequence creationSequence =
            HelperSequenceCreator.createCollection(componentManager, classType);
        if (creationSequence != null) {
          l2.add(creationSequence);
        }
        l = new ListOfLists<>(l1, l2);

      } else {

        // 2. COMMON CASE: ask the component manager for all sequences that
        // yield the required type.
        if (Log.isLoggingOn()) {
          Log.logLine("Will query component set for objects of type" + inputType);
        }
        l = componentManager.getSequencesForType(operation, i);
      }
      assert l != null;

      if (Log.isLoggingOn()) {
        Log.logLine("components: " + l.size());
      }

      // If we were not able to find (or create) any sequences of type
      // inputTypes[i], and we are
      // allowed the use null values, use null. If we're not allowed, then
      // return with failure.
      if (l.isEmpty()) {
        if (isReceiver || GenInputsAbstract.forbid_null) {
          if (Log.isLoggingOn()) {
            Log.logLine("forbid-null option is true. Failed to create new sequence.");
          }
          return new InputsAndSuccessFlag(false, null, null);
        } else {
          if (Log.isLoggingOn()) Log.logLine("Will use null as " + i + "-th input");
          TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputType);
          Sequence seq = new Sequence().extend(st, new ArrayList<Variable>());
          variables.add(totStatements);
          sequences.add(seq);
          assert seq.size() == 1;
          totStatements++;
          // Null is not an interesting value to add to the set of
          // possible values to reuse, so we don't update typesToVars or types.
          continue;
        }
      }

      // At this point, we have one or more sequences that create non-null
      // values of type inputTypes[i].
      // However, the user may have requested that we use null values as inputs
      // with some given frequency.
      // If this is the case, then use null instead with some probability.
      if (!isReceiver
          && GenInputsAbstract.null_ratio != 0
          && Randomness.weighedCoinFlip(GenInputsAbstract.null_ratio)) {
        if (Log.isLoggingOn()) {
          Log.logLine("null-ratio option given. Randomly decided to use null as input.");
        }
        TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputType);
        Sequence seq = new Sequence().extend(st, new ArrayList<Variable>());
        variables.add(totStatements);
        sequences.add(seq);
        assert seq.size() == 1;
        totStatements++;
        continue;
      }

      // At this point, we have a list of candidate sequences and need to select
      // a
      // randomly-chosen sequence from the list.
      Sequence chosenSeq;
      if (GenInputsAbstract.small_tests) {
        chosenSeq = Randomness.randomMemberWeighted(l);
      } else {
        chosenSeq = Randomness.randomMember(l);
      }

      // Now, find values that satisfy the constraint set.
      Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);

      // We are not done yet: we have chosen a sequence that yields a value of
      // the required
      // type inputTypes[i], but there may be more than one such value. Our last
      // random
      // selection step is to select from among all possible values.
      // if (i == 0 && statement.isInstanceMethod()) m = Match.EXACT_TYPE;
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

        return new InputsAndSuccessFlag(false, null, null);
      }

      // [Optimization.] Update optimization-related variables "types" and
      // "typesToVars".
      if (GenInputsAbstract.alias_ratio != 0) {
        // Update types and typesToVars.
        for (int j = 0; j < chosenSeq.size(); j++) {
          Statement stk = chosenSeq.getStatement(j);
          if (stk.isPrimitiveInitialization()) {
            continue; // Prim decl not an interesting candidate for multiple
          }
          // uses.
          Type outType = stk.getOutputType();
          types.add(outType);
          typesToVars.add(outType, totStatements + j);
        }
      }

      variables.add(totStatements + randomVariable.index);
      sequences.add(chosenSeq);
      totStatements += chosenSeq.size();
    }

    return new InputsAndSuccessFlag(true, sequences, variables);
  }
  
  
  
  
  
  
  
  
  
  
  
  
  

  /**
   * Returns the set of sequences that are included in other sequences to
   * generate inputs (and, so, are subsumed by another sequence).
   */
  @Override
  public Set<Sequence> getSubsumedSequences() {
    return subsumed_sequences;
  }
  
  public void addSubsumedSequence(Sequence s) {
	  subsumed_sequences.add(s);
  }

  @Override
  public int numGeneratedSequences() {
    return allSequences.size();
  }


}
