package randoop.generation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.tmatesoft.svn.core.wc.admin.ISVNHistoryHandler;

import com.thoughtworks.xstream.XStream;

import java.util.Set;

import randoop.BugInRandoopException;
import randoop.DummyVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.SubTypeSet;
import randoop.fieldextensions.AllOperationsHandler;
import randoop.fieldextensions.BoundedExtensionsComputer;
import randoop.fieldextensions.BuildersOnlyHandler;
import randoop.fieldextensions.IBuildersManager;
import randoop.fieldextensions.ObjectHashComputer;
import randoop.fieldextensions.OriginalRandoopManager;
import randoop.main.GenInputsAbstract;
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
import randoop.util.heapcanonicalization.CanonicalClass;
import randoop.util.heapcanonicalization.CanonicalField;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalObject;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;
import randoop.util.heapcanonicalization.DummyHeapRoot;
import randoop.util.heapcanonicalization.DummySymbolicAVL;
import randoop.util.heapcanonicalization.DummySymbolicTSet;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.IDummySymbolic;
import randoop.util.heapcanonicalization.candidatevectors.BugInCandidateVectorsCanonicalization;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVector;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorGenerator;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorsFieldExtensions;
import randoop.util.heapcanonicalization.candidatevectors.NegativeSymbolicVectorsWriter;
import randoop.util.heapcanonicalization.candidatevectors.NegativeVectorsWriter;
import randoop.util.heapcanonicalization.candidatevectors.SymbolicCandidateVectorGenerator;
import randoop.util.heapcanonicalization.candidatevectors.SymbolicVectorsWriter;
import randoop.util.heapcanonicalization.candidatevectors.VectorsWriter;

/**
 * Randoop's forward, component-based generator.
 */
public class ForwardGeneratorBE extends AbstractGeneratorBE {

  /**
   * The set of ALL sequences ever generated, including sequences that were
   * executed and then discarded.
   */
  private final Set<Sequence> allSequences;
  private final Set<TypedOperation> observers;

  /** Sequences that are used in other sequences (and are thus redundant) **/
  private Set<Sequence> subsumed_sequences = new LinkedHashSet<>();

  // For testing purposes only. If Globals.randooptestrun==false then the array
  // is never populated or queried. This set contains the same set of
  // components as the set "allsequences" above, but stores them as
  // strings obtained via the toCodeString() method.
  private final List<String> allsequencesAsCode = new ArrayList<>();

  // For testing purposes only.
  private final List<Sequence> allsequencesAsList = new ArrayList<>();

  // The set of all primitive values seen during generation and execution
  // of sequences. This set is used to tell if a new primitive value has
  // been generated, to add the value to the components.
  private Set<Object> runtimePrimitivesSeen = new LinkedHashSet<>();

  private int maxSeqLength;
  private Set<String> strSeqs = new LinkedHashSet<>();
  


  public ForwardGeneratorBE(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      int maxSeqLength, 
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
    this.maxSeqLength = maxSeqLength;
  }

  public ForwardGeneratorBE(
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

    initializeRuntimePrimitivesSeen();
    
	IBuildersManager buildersManager;
    if (GenInputsAbstract.always_use_builders || GenInputsAbstract.builders_at_length < Integer.MAX_VALUE) {
    	buildersManager = new BuildersOnlyHandler(GenInputsAbstract.always_use_builders, operations, GenInputsAbstract.builders_at_length);
    }
    else {
    	buildersManager = new AllOperationsHandler(operations);
    }
    
    switch (GenInputsAbstract.filtering) {
    case FE:
    	opManager = new BoundedExtensionsComputer(GenInputsAbstract.max_objects, 
    			GenInputsAbstract.max_extensions_primitives, 
    			GenInputsAbstract.max_array_objects, 
    			buildersManager,
    			GenInputsAbstract.omitfields);
    	break;
    case BE:
    	opManager = new ObjectHashComputer(GenInputsAbstract.max_objects, 
    			GenInputsAbstract.max_extensions_primitives, 
    			GenInputsAbstract.max_array_objects, 
    			buildersManager,
    			GenInputsAbstract.omitfields,
    			GenInputsAbstract.output_objects,
    			GenInputsAbstract.output_object_seqs);
    	break; 
    case NO:
    	opManager = new OriginalRandoopManager(buildersManager);
    	break;
    }

    if (VectorsWriter.isEnabled()) 
    	initNewCanonicalizerForVectorization(GenInputsAbstract.getClassnamesFromArgs(), AbstractGenerator.vectorization_max_objects);
    
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
      es.execute(new DummyVisitor(), new DummyCheckGenerator());
      NormalExecution e = (NormalExecution) es.getResult(0);
      Object runtimeValue = e.getRuntimeValue();
      runtimePrimitivesSeen.add(runtimeValue);
    }
  }
  
  Set<String> posVectors = new HashSet<>();
  Set<String> negVectors = new HashSet<>();
  Set<String> posSymbVectors = new HashSet<>();
  Set<String> negSymbVectors = new HashSet<>();
  
  public void gen() {
	  ComponentManager prevMan = new ComponentManager();
	  // componentManager has only primitive sequences, we copy them to the current manager
	  prevMan.copyAllSequences(componentManager);
	  for (int seqLength = 1; seqLength <= maxSeqLength; seqLength++) {
		  int newSeqs = 0;
		  int builders = 0;
		  int execSeqs = 0;
		  ComponentManager currMan = new ComponentManager(); 
		  currMan.copyAllSequences(componentManager);

		  if (GenInputsAbstract.iterations_to_omitfields != Integer.MAX_VALUE && seqLength == GenInputsAbstract.iterations_to_omitfields) {
			  opManager.setOmitFields(GenInputsAbstract.omitfields_after_iterations);
		  }

		  // Notify listeners we are about to perform a generation step.
		  if (listenerMgr != null) {
			  listenerMgr.generationStepPre();
		  }

		  num_steps++;

		  if (Log.isLoggingOn()) {
			  Log.logLine("-------------------------------------------");
		  }
		  
		  for (int opIndex = 0; opIndex < operations.size(); opIndex++) {

			  if (stop()) {
				  System.out.println("DEBUG: Stopping criteria reached");
				  break;
			  }

			  TypedOperation operation = operations.get(opIndex);
			  if (Log.isLoggingOn()) {
				  Log.logLine("Selected operation: " + operation.toString());
			  }
			  
			  if (operation.toString().equals("java.lang.Object.<init> : () -> java.lang.Object"))
				  // TODO: Think about how to deal with the operation that builds a single Object
				  continue;

			  // jhp: add flags here
			  //InputsAndSuccessFlag sequences = selectInputs(operation);
			  TypeTuple inputTypes = operation.getInputTypes();
			  CartesianProduct<Sequence> cp = new CartesianProduct<>(inputTypes.size());
			  for (int i = 0; i < inputTypes.size(); i++) {
				  //Type inputType = inputTypes.get(i);
				  
			      boolean isReceiver = (i == 0 && (operation.isMessage()) && (!operation.isStatic()));
				  
				  SimpleList<Sequence> l = prevMan.getSequencesForType(operation, i);
				  
				  if (!GenInputsAbstract.forbid_null) {
					  /*
					  // FIXME: Very slow, change to improve performance
					  List<Sequence> l = prevMan.getSequencesForType(operation, i).toJDKList();
					  // Receiver for a method can't be null
					  if (!isReceiver && !inputTypes.get(i).isPrimitive()) {
						  TypedOperation st = TypedOperation.createNullOrZeroInitializationForType(inputTypes.get(i));
						  l.add(new Sequence().extend(st, new ArrayList<Variable>()));
					  }
					  */
				  }
				  
				  cp.setIthComponent(i, l);
			  }
			  
			  // For each sequence in the cartesian product of feasible inputs
			  while (cp.hasNext()) {
				  List<Sequence> currParams = cp.next();

				  List<Sequence> seqs = new ArrayList<>();
				  int totStatements = 0;
				  List<Integer> variables = new ArrayList<>();

				  for (int i = 0; i < inputTypes.size(); i++) {
					  Type inputType = inputTypes.get(i);

					  Sequence chosenSeq = currParams.get(i); 
					  
					  
					  // TODO: We are not generating all feasible sequences here, just choosing a variable randomly
					  Variable randomVariable = chosenSeq.variableForTypeLastStatement(inputType);
					  
//					  for (Variable randomVariable: chosenSeq.getBuilderVariables()) {
					  
					  
					  if (randomVariable == null) {
						  System.out.println(operation.toParsableString());
						  System.out.println(chosenSeq.toParsableString());
						  System.out.println(currParams.toString());
						  System.out.println(chosenSeq.getBuilderIndexes().toString());
						  
						  
						  throw new BugInRandoopException("type: " + inputType + ", sequence: " + chosenSeq);

					  }
					  if (i == 0
							  && operation.isMessage()
							  && !(operation.isStatic())
							  && (chosenSeq.getCreatingStatement(randomVariable).isPrimitiveInitialization()
									  || randomVariable.getType().isPrimitive())) {

						  throw new Error("we were unlucky and selected a null or primitive value as the receiver for a method call");
						  // return new InputsAndSuccessFlag(false, null, null);
					  }

					  variables.add(totStatements + randomVariable.index);
					  seqs.add(chosenSeq);
					  totStatements += chosenSeq.size();
				  }

				  InputsAndSuccessFlag sequences = new InputsAndSuccessFlag(true, seqs, variables);

				  if (!sequences.success) {
					  if (Log.isLoggingOn()) Log.logLine("Failed to find inputs for statement.");
					  throw new Error("Failed to find inputs for statement.");
				  }

				  Sequence concatSeq = Sequence.concatenate(sequences.sequences);

				  // Figure out input variables.
				  List<Variable> inputs = new ArrayList<>();
				  for (Integer oneinput : sequences.indices) {
					  Variable v = concatSeq.getVariable(oneinput);
					  inputs.add(v);
				  }

				  Sequence newSequence = concatSeq.extend(operation, inputs);
				  
				  // Avoid repetition of, for example, single constructors.
				  // TODO: Check if this does not break anything.
				  
				  /*
				  String seqStr = newSequence.toCodeString();
				  if (this.strSeqs.contains(seqStr))
					  continue;
				  
				  this.strSeqs.add(seqStr);
				  */
				  
				 /* 
				  if (this.allSequences.contains(newSequence)) 
					  continue;
				  
				  // To prune sequences generated twice 
				  this.allSequences.add(newSequence);
				  */

				  // If parameterless statement, subsequence inputs
				  // will all be redundant, so just remove it from list of statements.
				  // XXX does this make sense? especially in presence of side-effects
				  /*
				  if (operation.getInputTypes().isEmpty()) {
					operations.remove(operation);
				  }
				   */

				  /*
				  randoopConsistencyTests(newSequence);
				  randoopConsistencyTest2(newSequence);
				  */

				  if (Log.isLoggingOn()) {
					  Log.logLine(
							  String.format("Successfully created new unique sequence:%n%s%n", newSequence.toString()));
				  }

				  
				  ExecutableSequence eSeq = new ExecutableSequence(newSequence);

				  // Execute new sequence
				  setCurrentSequence(eSeq.sequence);

				  // long endTime = System.nanoTime();
				  // long gentime = endTime - startTime;
				  //				     startTime = endTime; // reset start time.
				  long startTime = System.nanoTime(); // reset start time.

				  eSeq.execute(executionVisitor, checkGenerator);

				  long endTime = System.nanoTime();

				  eSeq.exectime = endTime - startTime;
				  startTime = endTime; // reset start time.

				  processSequence(eSeq);

				  //endTime = System.nanoTime();
				  //gentime += endTime - startTime;
				  // eSeq.gentime = gentime;
				  eSeq.gentime = 0;

				  /*
				  if (GenInputsAbstract.field_exhaustive_filtering) {
					  if (eSeq.isNormalExecution()) {
						  Set<Integer> activeIndexes = extensionsComputer.newFieldValuesInitialized(eSeq);
						  if (activeIndexes == null || activeIndexes.size() == 0)
							  continue;
						  
						  currMan.addGeneratedSequence(eSeq.sequence, activeIndexes);
						  
						  String opName = operation.toString();
						  if (!buildersNames.contains(opName)) {
							  builders.add(operation);
							  buildersNames.add(opName);
						  }
					  }
				  }
				  else {
					  if (eSeq.sequence.hasActiveFlags()) 
						  currMan.addGeneratedSequence(eSeq.sequence);
				  }
				  */

				  num_sequences_generated++;
				  
				  
				  execSeqs++;
				  if (eSeq.sequence.hasActiveFlags()) {
					  //if (seqLength != GenInputsAbstract.max_objects) {
						  if (!opManager.addGeneratedSequenceToManager(operation, eSeq, currMan, seqLength)) {
							  //					  if (!opManager.addGeneratedSequenceToManager(operation, eSeq, prevMan, seqLength)) {
							  // Sequence does not create a new object, or its last statement is a builder and
							  // builders are always enabled
							  //if (!GenInputsAbstract.always_use_builders || !buildersManager.isBuilder(operation))
							  eSeq.clean();
							  continue;
						  }
					 // }
					  builders++;
				  }
				  newSeqs++;
				  
				  
				  if (VectorsWriter.isEnabled()) {
					  if (eSeq.isNormalExecution()) { 
						  if (CanonicalizerLog.isLoggingOn()) {
							  CanonicalizerLog.logLine("**********");
							  CanonicalizerLog.logLine("Canonicalizing runtime objects in the last statement of sequence:\n" + eSeq.toCodeString());
							  CanonicalizerLog.logLine("**********");
						  }	
						  

						  
						  makeCanonicalVectorsForLastStatement(eSeq);
						  

					  }
				  }
				  
				  
				  /*
				  for (Sequence is : sequences.sequences) {
					  subsumed_sequences.add(is);
			      }
			      */

				  // Notify listeners we just completed generation step.
				  if (listenerMgr != null) {
					  listenerMgr.generationStepPost(eSeq);
				  }

				  if (eSeq.hasFailure()) {
					  num_failing_sequences++;
				  }

				  // Save sequence as regression test if needed
				  if (outputTest.test(eSeq)) {
					  if (!eSeq.hasInvalidBehavior()) {
						  if (eSeq.hasFailure()) {
							  outErrorSeqs.add(eSeq);
						  } else {
							  outRegressionSeqs.add(eSeq);
						  }
					  }
				  }
				  
				  eSeq.clean();

			  } // End loop for current operation

		  } // End loop for all operations 

		  if (!GenInputsAbstract.noprogressdisplay) 
			  System.out.println("\n>>> Iteration: " + seqLength + 
					  ", Exec: " + execSeqs + 
					  ", New: " + newSeqs +
					  ", Builders: " + builders +
					  ", Error: " + (newSeqs - builders));

		  operations = opManager.getBuilders(seqLength);
		  // Previous component manager is 
		  //prevMan.copyAllSequences(currMan);
		  prevMan = currMan;
	  } // End of all iterations
  
  
  	  if (VectorsWriter.isEnabled()) {
		  for (String vector: posVectors)
			  VectorsWriter.logLine(vector);
	  }

	  if (NegativeVectorsWriter.isEnabled()) {
		  for (String vector: negVectors)
			  NegativeVectorsWriter.logLine(vector);
	  }
	  
	  if (SymbolicVectorsWriter.isEnabled()) {
		  for (String vector: posSymbVectors)
			  SymbolicVectorsWriter.logLine(vector);
	  }
	  if (NegativeSymbolicVectorsWriter.isEnabled()) {
		  for (String vector: negSymbVectors)
			  NegativeSymbolicVectorsWriter.logLine(vector);
	  }
	  
  }
  
  
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
	        // FIXME: Don't save newly discovered primitive values for now
	        /*
	        if (!looksLikeObjToString && !tooLongString && runtimePrimitivesSeen.add(runtimeValue)) {
	          // Have not seen this value before; add it to the component set.
	          componentManager.addGeneratedSequence(Sequence.createSequenceForPrimitive(runtimeValue));
	        }
	        */
	      } else {
	        if (Log.isLoggingOn()) {
	          Log.logLine("Making index " + i + " active.");
	        }
	      }
	    }
	  }


  @Override
  public Set<Sequence> getAllSequences() {
    return Collections.unmodifiableSet(this.allSequences);
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

  @Override
  public Set<Sequence> getSubsumedSequences() {
    return subsumed_sequences;
  }

  @Override
  public int numGeneratedSequences() {
    return allSequences.size();
  }

  @Override
  public ExecutableSequence step() {
	  // TODO Auto-generated method stub
	  throw new Error(this.getClass().getName() + " cannot be used by Randoop's AbstractGenerator");
  }

  
  
  
  // VECTORIZATION
  
	public static CanonicalStore store; 
	public static HeapCanonicalizer vectorCanonicalizer;
	public static CandidateVectorGenerator candVectGenerator;
	public static SymbolicCandidateVectorGenerator symbCandVectGenerator;
	public static CandidateVectorsFieldExtensions candVectExtensions;
	
	public void initNewCanonicalizerForVectorization(Collection<String> classNames, int vectMaxObjects) {
		//vectorization_hard_array_limits = true;
	    store = new CanonicalStore(classNames, Integer.MAX_VALUE);

		vectorCanonicalizer = new HeapCanonicalizer(store, vectMaxObjects, vectMaxObjects);
		// Initialize the candidate vector generator with the canonical classes that were mined from the code,
		// before the generation starts.
		if (vectorization_remove_unused) {                                                                                                                                                                           
			if (classNames.size() != 1)                                                                                                                                                                          
				throw new BugInCandidateVectorsCanonicalization("Only the --testclass option can be used for generation when --vectorization-remove-unused=true.");                                                                       
			for (String s: classNames) {
				candVectGenerator = new CandidateVectorGenerator(store, s, vectorization_no_primitives);                                                                                                   
				symbCandVectGenerator = new SymbolicCandidateVectorGenerator(store, s, vectorization_no_primitives);                                                                                                   
			}
		}                                                                                                                                                                                                        
		else {
			candVectGenerator = new CandidateVectorGenerator(store, vectorization_no_primitives); 
			symbCandVectGenerator = new SymbolicCandidateVectorGenerator(store, vectorization_no_primitives);                                                                                                   
		}

		CanonicalHeap heap = new CanonicalHeap(store, vectMaxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(heap);
		candVectExtensions = new CandidateVectorsFieldExtensions(header);
		VectorsWriter.logLine(header.toString());
		
		if (SymbolicVectorsWriter.isEnabled()) {
			header = symbCandVectGenerator.makeCandidateVectorsHeader(heap);
			SymbolicVectorsWriter.logLine(header.toString());
		}
	}

  
  
  // Use the new canonizer to generate a candidate vector for all the objects of the given classes in the last method of the sequence
  // Pre: (!mutateStructures => CandidateVectorsWriterLog.isEnabled()) && (mutateStructures => NegativeCandidateVectorsWriterLog.isEnabled())
  protected void makeCanonicalVectorsForLastStatement(ExecutableSequence eSeq) {
	  // FIXME?: We canonicalize every object, no only those that extend the global extensions
	  int maxIndex = eSeq.getLastStmtRuntimeObjects().size();
	  for (int index = 0; index < maxIndex; index++) {
		  eSeq.execute(executionVisitor, checkGenerator);
		  Object obj = eSeq.getLastStmtRuntimeObjects().get(index);
		  CanonicalClass rootClass = store.getCanonicalClass(obj.getClass());
		  if (obj == null || eSeq.isPrimitive(obj) || !store.isMainClass(rootClass))
			  continue;

		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(obj);
		  /*
		  boolean isPos = isPositive(obj);
		  if (!isPos)
			  System.out.println(eSeq.toCodeString());
			  */
		  assert isPositive(obj): "Negative instance generated by the tests";
		  if (res.getKey() == CanonicalizationResult.OK) {

			  CanonicalHeap objHeap = res.getValue();

			  // Generate positive symbolic vectors
			  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(objHeap);
			  if (CanonicalizerLog.isLoggingOn()) {
				  CanonicalizerLog.logLine("----------");
				  CanonicalizerLog.logLine("New vector:");
				  CanonicalizerLog.logLine(candidateVector.toString());
				  CanonicalizerLog.logLine("----------");
			  }
			  //candVectExtensions.addToExtensions(candidateVector);
			  posVectors.add(candidateVector.toString());

			  if (SymbolicVectorsWriter.isEnabled()) {
				  if (GenInputsAbstract.pseudoexhaustive_mutations)
					  generateSystematicSymbolicStructures(eSeq, index);
				  else {
					  for (int k = 0; k < GenInputsAbstract.pos_strs_rep; k++)
						  generateRandomSymbolicStructures(eSeq, index);
				  }
			  }
			  
			  // Generate negative symbolic vectors
			  if (NegativeSymbolicVectorsWriter.isEnabled()) {
				  for (int k = 0; k < GenInputsAbstract.neg_strs_rep; k++) {
					  if (GenInputsAbstract.pseudoexhaustive_mutations) {
						  int mutAttempts = 10;
						  for (int j = 0; j < mutAttempts; j++) {
							  if (generateSystematicNegativeSymbolicStructures(eSeq, index))
								  break;
						  }
					  }
					  else
						  mutateRandomFieldAndGenerateNegativeSymbolicStructures(eSeq, index);
				  }
			  }
		  }
		  else { // res.getKey() != CanonicalizationResult.OK
			  if (CanonicalizerLog.isLoggingOn()) {
				  CanonicalizerLog.logLine("----------");
				  CanonicalizerLog.logLine("Not canonicalizing an object with more than " + 
						  vectorization_max_objects + " objects of the same type");
				  CanonicalizerLog.logLine("Error message: " + res.getKey());
				  CanonicalizerLog.logLine("----------");
			  }
		  }
		  index++;
	  }
  }
  
  private XStream xstream = new XStream();
  
  private Object cloneObject(Object o) {
	  String xml = xstream.toXML(o);
	  return xstream.fromXML(xml);
  }
  
  private int MAXSYMB = GenInputsAbstract.symbolic_mutations_by_structure;
  
  private void generateSystematicSymbolicStructures(ExecutableSequence eSeq, int index) {
		  // Rebuild object
		  eSeq.execute(executionVisitor, checkGenerator);
		  Object toMutate = eSeq.getLastStmtRuntimeObjects().get(index);
		  
		  if (CanonicalizerLog.isLoggingOn()) {
			  CanonicalizerLog.logLine(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			  CanonicalizerLog.logLine(">>>> Object to make symbolic:");
		  }
		  Set<String> genVects = new HashSet<>();
		  int scope = AbstractGenerator.vectorization_max_objects;
		  generateSystematicSymbolicStructures(toMutate, genVects, scope);
  }
  
  private void generateSystematicSymbolicStructures(Object o, Set<String> genVects, int depth) {
	  if (genVects.size() == MAXSYMB || depth == 0) return;
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(o);
		  CanonicalHeap objHeap = res.getValue();
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  String vectorStr = candidateVector.toString();
		  assert isStructurallyPositive(o);
		  if (!genVects.add(vectorStr))
			  return;
		  int toMutateInd = 0;
		  if (objHeap.objectsPerClass().get(cls) == null) return;
		  
		  while (toMutateInd < objHeap.objectsPerClass().get(cls)) {
			  int fldInd = 0;
			  while (true) {
				  if (toMutateInd >= objHeap.objectsPerClass().get(cls))
					  // Number of objects in the heap decreases after abstraction
					  return;
				  CanonicalObject toMut = objHeap.getCanonicalObject(cls, toMutateInd);
				  List<CanonicalField> fields = objHeap.getSymbolicFields(toMut);
				  if (fldInd >= fields.size())
					  break;
				  CanonicalField fld = fields.get(fldInd);
				  // Don't allow to make symbolic already symbolic values
				  Object oldValue = fld.getValue(toMut);
				  if (oldValue != null && IDummySymbolic.class.isAssignableFrom(oldValue.getClass())) {
					  fldInd++;
					  continue;
				  }
				  // Save original object
				  Object oClone = cloneObject(o);
				  // Mutate and call recursively from the mutated object
				  fld.setValue(toMut, objHeap.getSymbolicNode());
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("Mutation successful. Field " + fld.stringRepresentation(toMut) + " of object " + 
							  toMut.stringRepresentation() + " set to " + objHeap.getSymbolicNode().stringRepresentation());
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(o);
				  objHeap = res.getValue();
				  assert isStructurallyPositive(o);
				  candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
				  vectorStr = candidateVector.toString();
				  if (posSymbVectors.add(vectorStr)) {
					  if (CanonicalizerLog.isLoggingOn()) {
						  CanonicalizerLog.logLine("New symbolic vector:");
						  CanonicalizerLog.logLine(candidateVector.toString());
					  }
				  }
				  generateSystematicSymbolicStructures(o, genVects, depth-1);
				  // Recover original object and continue
				  o = oClone;
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(o);
				  objHeap = res.getValue();
				  assert isStructurallyPositive(o);
				  fldInd++;
			  }
			  toMutateInd++;
		  }
	  }
  }
  
  private boolean generateSystematicNegativeSymbolicStructures(ExecutableSequence eSeq, int index) {
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  // Rebuild object
		  eSeq.execute(executionVisitor, checkGenerator);
		  Object mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
		  // Canonicalize and mutate the current object
		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine(">>> Object before mutation");
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  CanonicalHeap toMutateHeap = res.getValue();	
		  // Get a random object to mutate
		  CanonicalObject toMutate = toMutateHeap.getRandomCanonicalObject(cls);
		  // No objects of the class to mutate
		  if (toMutate == null) return false;

		  if (!toMutateHeap.mutateRandomObjectField(toMutate)
				  || isStructurallyPositive(mutObj))
			  return false;
		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine(">>> Object after mutation");
		  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  negVectors.add(candidateVector.toString());
		  Set<String> genVects = new HashSet<>();
		  int scope = AbstractGenerator.vectorization_max_objects;
		  generateSystematicNegativeSymbolicStructures(mutObj, genVects, scope, toMutateHeap.mutatedObject, toMutateHeap.mutatedField);
	  }
	  return true;
  }
  
  private void generateSystematicNegativeSymbolicStructures(Object curr, Set<String> genVects, int depth, CanonicalObject mutatedObject, CanonicalField mutatedField) {
	  if (genVects.size() == MAXSYMB || depth == 0) return;
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(curr);
		  CanonicalHeap objHeap = res.getValue();
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  String vectorStr = candidateVector.toString();
		  //assert isStructurallyPositive(curr);
		  if (!genVects.add(vectorStr))
			  return;
		  
		  //CanonicalObject mutatedObj = mutatedObject;
		  CanonicalObject mutatedObj = objHeap.findExistingCanonicalObjectByName(mutatedObject);
		  if (objHeap.objectsPerClass().get(cls) == null) return;

		  int toMutateInd = 0;
		  while (toMutateInd < objHeap.objectsPerClass().get(cls)) {
			  int fldInd = 0;
			  while (true) {
				  if (toMutateInd >= objHeap.objectsPerClass().get(cls))
					  // Number of objects in the heap decreases after abstraction
					  return;
				  CanonicalObject toMut = objHeap.getCanonicalObject(cls, toMutateInd);
				  List<CanonicalField> fields = objHeap.getSymbolicFields(toMut);
				  if (fldInd >= fields.size())
					  break;
				  CanonicalField fld = fields.get(fldInd);
				  // Do not allow to mutate the mutated object field in the generation of negative symbolic instances
				  if (toMut.getObject() == mutatedObj.getObject() &&
						  fld.getName().equals(mutatedField.getName())) {
					  fldInd++;
					  continue;
				  }
				  // Don't allow to make symbolic already symbolic values
				  Object oldValue = fld.getValue(toMut);
				  if (oldValue != null && IDummySymbolic.class.isAssignableFrom(oldValue.getClass())) {
					  fldInd++;
					  continue;
				  }
				  // Save original object
				  Object oClone = cloneObject(curr);
				  // Make field symbolic 
				  fld.setValue(toMut, objHeap.getSymbolicNode());
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine("Mutation successful. Field " + fld.stringRepresentation(toMut) + " of object " + 
							  toMut.stringRepresentation() + " set to " + objHeap.getSymbolicNode().stringRepresentation());
				  
				  if (isSymbolicPositive(curr)) {
					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine(">>>> Object became positive after symbolic abstraction!!!");
					  // Restore object before failed symbolic abstraction and continue
					  curr = oClone;
					  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(curr);
					  objHeap = res.getValue(); 
					  CanonicalObject mutatedObjClone = objHeap.findExistingCanonicalObjectByName(mutatedObj);
					  assert mutatedObjClone != null: "Mutated object clone must exist after a copy";
					  mutatedObj = mutatedObjClone;
					  fldInd++;
					  continue;
				  }
				  
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(curr);
				  objHeap = res.getValue();
				  CanonicalObject updatedMutatedObj = objHeap.findExistingCanonicalObject(mutatedObj.getObject(), mutatedObj.getCanonicalClass());
				  // Check reachability of mutatedObj in mutHeap
				  if (updatedMutatedObj == null) {
					  if (CanonicalizerLog.isLoggingOn())
						  CanonicalizerLog.logLine(">>>> The mutation was lost during symbolic abstraction. The object became positive!!!");
					  // Restore object before failed symbolic abstraction and continue
					  curr = oClone;
					  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(curr);
					  objHeap = res.getValue(); 
					  CanonicalObject mutatedObjClone = objHeap.findExistingCanonicalObjectByName(mutatedObj);
					  assert mutatedObjClone != null: "Mutated object clone must exist after a copy";
					  mutatedObj = mutatedObjClone;
					  fldInd++;
					  continue; 
				  }
				  
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine(">>>> New negative symbolic object"); 
				  
				  candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
				  vectorStr = candidateVector.toString();
				  if (negSymbVectors.add(vectorStr)) {
					  if (CanonicalizerLog.isLoggingOn()) {
						  CanonicalizerLog.logLine("New negative symbolic vector:");
						  CanonicalizerLog.logLine(candidateVector.toString());
					  }
				  }
				  // Call recursively from the symbolic object
				  generateSystematicNegativeSymbolicStructures(curr, genVects, depth-1, updatedMutatedObj, mutatedField);
				  // Recover original object and continue
				  curr = oClone;
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(curr);
				  objHeap = res.getValue();
				  CanonicalObject mutatedObjClone = objHeap.findExistingCanonicalObjectByName(mutatedObj);
				  assert mutatedObjClone != null: "Mutated object clone must exist after a copy";
				  mutatedObj = mutatedObjClone;
				  fldInd++;
			  }
			  toMutateInd++;
		  }
	  }
  } 
  
  
  
  
  
  
  


  private void generateRandomSymbolicStructures(ExecutableSequence eSeq, int index) {
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  // Rebuild object
		  eSeq.execute(executionVisitor, checkGenerator);
		  Object mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
		  
		  if (CanonicalizerLog.isLoggingOn()) {
			  CanonicalizerLog.logLine(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			  CanonicalizerLog.logLine(">>>> Object to make symbolic:");
		  }
		  
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  CanonicalHeap objHeap = res.getValue();
		  assert isStructurallyPositive(mutObj);

		  //int numNodes = AbstractGenerator.vectorization_max_objects;//objHeap.objectsPerClass().get(cls);
		  int numNodes = GenInputsAbstract.symbolic_mutations_by_structure;
		  makeRandomObjectFieldsSymbolic(mutObj, objHeap, cls, numNodes);
	  }
  }
  
  private CanonicalHeap makeRandomObjectFieldsSymbolic(Object mutObj, CanonicalHeap objHeap, CanonicalClass cls, int maxSymbNodes) {
	  CanonicalHeap ch = objHeap;
	  for (int ind = 0; ind < maxSymbNodes; ind++) {
		  if (!ch.makeRandomObjectFieldSymbolic(cls))
			  continue;

		  if (CanonicalizerLog.isLoggingOn()) {
			  CanonicalizerLog.logLine(">>>> New symbolic object");
		  }

		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  posSymbVectors.add(candidateVector.toString());
		  if (CanonicalizerLog.isLoggingOn()) {
			  CanonicalizerLog.logLine("New symbolic vector:");
			  CanonicalizerLog.logLine(candidateVector.toString());
		  }
		  assert isSymbolicPositive(mutObj) : "Negative vector generated after symbolic abstraction: " + candidateVector.toString();
		  ch = res.getValue();
	  }
	  return ch;
  }
  
  private boolean mutateRandomFieldAndGenerateNegativeSymbolicStructures(ExecutableSequence eSeq, int index) {
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  // Rebuild object before mutation
		  eSeq.execute(executionVisitor, checkGenerator);
		  Object mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
		  // Canonicalize and mutate the current object
		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine(">>> Object before mutation");
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  CanonicalHeap toMutateHeap = res.getValue();	

		  // Get a random object to mutate
		  CanonicalObject toMutate = toMutateHeap.getRandomCanonicalObject(cls);
		  if (!toMutateHeap.mutateRandomObjectField(toMutate)
				  || isStructurallyPositive(mutObj))
			  return false;

		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine(">>> Object after mutation");
		  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  negVectors.add(candidateVector.toString());
		  if (NegativeSymbolicVectorsWriter.isEnabled()) {
			  int numNodes = GenInputsAbstract.symbolic_mutations_by_structure;
			  generateNegativeSymbolicStructures(mutObj, res.getValue(), toMutateHeap.mutatedObject, toMutateHeap.mutatedField, toMutateHeap.mutatedValue, numNodes);
		  }
	  }
	  return true;
  } 

  private void generateNegativeSymbolicStructures(Object mutObj, CanonicalHeap mutHeap, CanonicalObject mutatedObject, CanonicalField mutatedField, CanonicalObject mutatedValue, int maxSymbNodes) {
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  CanonicalHeap ch = mutHeap;
		  for (int ind = 0; ind < maxSymbNodes; ind++) {
			  if (!ch.makeRandomObjectFieldSymbolic(cls, mutatedObject, mutatedField))
				  continue;
			  if (CanonicalizerLog.isLoggingOn()) 
				  CanonicalizerLog.logLine(">>>> New symbolic object");
			  if (isSymbolicPositive(mutObj)) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine(">>>> Object became positive after symbolic abstraction!!!");
				  return;
			  }
			  
			  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
			  ch = res.getValue();
			  // Check reachability of mutObj in mutHeap
			  if (ch.findExistingCanonicalObject(mutatedObject.getObject(), mutatedObject.getCanonicalClass()) == null) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine(">>>> The mutation was lost during symbolic abstraction. The object became positive!!!");
				  return; 
			  }
			  
			  if (CanonicalizerLog.isLoggingOn())
				  CanonicalizerLog.logLine(">>>> New negative symbolic object");

			  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
			  negSymbVectors.add(candidateVector.toString());
			  if (CanonicalizerLog.isLoggingOn()) {
				  CanonicalizerLog.logLine("New negative symbolic vector:");
				  CanonicalizerLog.logLine(candidateVector.toString());
			  }
		  }
	  }
  }
  
  
  private void generateSymbolicStructures(ExecutableSequence eSeq, int index, CanonicalHeap objHeap) {
	  for (CanonicalClass cls: objHeap.objectsPerClass().keySet()) {
		  if (cls.getName().equals(DummyHeapRoot.class.getName()) ||
				  IDummySymbolic.class.isAssignableFrom(cls.getConcreteClass()))
			  continue;

		  //assert cls.getName().equals("symbolicheap.bounded.AvlTree") : "Not an AVL: " + cls.getName();
		  assert !cls.getName().contains("DummySymbolic");
		  for (int k = 0; k < GenInputsAbstract.pos_strs_rep; k++) {
			  // Rebuild object before mutation
			  eSeq.execute(executionVisitor, checkGenerator);
			  Object mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
			  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
			  //CandidateVector<Object> cv = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
			  objHeap = res.getValue();
			  assert isStructurallyPositive(mutObj);
			  
			  // Generate BFS negatives
			  for (int toMutateInd = 0; toMutateInd < objHeap.objectsPerClass().get(cls); toMutateInd++) {
				  objHeap = makeObjectFieldsSymbolic(mutObj, objHeap, cls, toMutateInd);
			  }
			  
			  // Rebuild object before mutation
			  eSeq.execute(executionVisitor, checkGenerator);
			  mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
			  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
			  //CandidateVector<Object> cv = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
			  objHeap = res.getValue();
			  // Generate inverted BFS negative
			  //for (int toMutateInd = objHeap.getObjectsForClass(cls).size() - 1; toMutateInd >= 0; toMutateInd--) {
			  int toMutateInd = objHeap.objectsPerClass().get(cls) - 1;
			  while (toMutateInd > 0) {
				  objHeap = makeObjectFieldsSymbolic(mutObj, objHeap, cls, toMutateInd);
				  if (toMutateInd > objHeap.objectsPerClass().get(cls) - 1)
					  toMutateInd = objHeap.objectsPerClass().get(cls) - 1;
				  else
					  toMutateInd--;
			  }
		  }
	  }
  }
  

  private CanonicalHeap makeObjectFieldsSymbolic(Object mutObj, CanonicalHeap objHeap, CanonicalClass cls, int toMutateInd) {
	  int mutateFields = Randomness.nextRandomInt(3);
	  CanonicalHeap ch = objHeap;
	  for (int ind = 0; ind < mutateFields/*GenInputsAbstract.pos_strs_rep*/; ind++) {
		  //int toMutateInd = Randomness.nextRandomInt(objHeap.objectsPerClass().get(cls));

		  CanonicalObject canMutObj = objHeap.getCanonicalObject(cls, toMutateInd);
		  if (!makeRandomFieldSymbolic(mutObj, canMutObj, ch, null, null))
			  continue;
		  // assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  posSymbVectors.add(candidateVector.toString());
		  //boolean isPositive = isSymbolicPositive(mutObj);
		  assert isSymbolicPositive(mutObj) : "Negative vector generated after symbolic abstraction: " + candidateVector.toString();
		  ch = res.getValue();
	  } 
	  
	  return ch;
  }
  

  private boolean makeRandomFieldSymbolic(Object mutObj, CanonicalObject canMutObj, CanonicalHeap toMutateHeap, CanonicalObject mutatedObject, CanonicalField mutatedField) {
	  int objRetries = 30;
	  int positiveRetries = 20;
	  boolean succeed = false;

	  while (!succeed && positiveRetries > 0) {
		  // Canonicalize and mutate the current object
		  // Mutate to make structure symbolic
		  if (!toMutateHeap.makeRandomFieldSymbolic(canMutObj, objRetries, mutatedObject, mutatedField)) {
			  //if (!toMutateHeap.mutateObjectFieldOutsideExtensions(globalExtensions, cls, toMutateInd, objRetries)) {
			  // Object could not be mutated outside the extensions
			  if (CanonicalizerLog.isLoggingOn()) 
				  CanonicalizerLog.logLine("> Mutation of the current object failed");
			  positiveRetries--;
			  continue;
		  }
		  succeed = true;
	  }
	  
	  return succeed;
  }
  
  
  private boolean mutateARandomFieldOfObjectAndGenNegativeVector(ExecutableSequence eSeq, int index) {
	  for (CanonicalClass cls: vectorCanonicalizer.getStore().getCanonicalClassesFromArgs()) {
		  // Rebuild object before mutation
		  eSeq.execute(executionVisitor, checkGenerator);
		  Object mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
		  // Canonicalize and mutate the current object
		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine(">>> Object before mutation");
		  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  CanonicalHeap toMutateHeap = res.getValue();	

		  // Get a random object to mutate
		  CanonicalObject toMutate = toMutateHeap.getRandomCanonicalObject(cls);
		  if (!toMutateHeap.mutateRandomObjectField(toMutate)
				  || isStructurallyPositive(mutObj))
			  return false;

		  if (CanonicalizerLog.isLoggingOn())
			  CanonicalizerLog.logLine(">>> Object after mutation");
		  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
		  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
		  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
		  negVectors.add(candidateVector.toString());
		  if (NegativeSymbolicVectorsWriter.isEnabled())
			  generateNegativeSymbolicStructures(mutObj, res.getValue(), toMutateHeap.mutatedObject, toMutateHeap.mutatedField, toMutateHeap.mutatedValue);
	  }
	  return true;
  }
  
  
  // Mutate the object at position 'index' in the last statement of sequence 'eSeq'. One field of each object reachable  
  // from the root is mutated selected randomly and mutated at most once. 
  private void mutateAFieldOfEachObjectAndGenNegativeVector(ExecutableSequence eSeq, int index) {
	  eSeq.execute(executionVisitor, checkGenerator);
	  Object mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
	  assert isStructurallyPositive(mutObj);
	  // Canonicalize and mutate the current object
	  Entry<CanonicalizationResult, CanonicalHeap> res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
	  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
	  CanonicalHeap toMutateHeap = res.getValue();	
	  Map<CanonicalClass, Integer> objsPerClass = toMutateHeap.objectsPerClass();
	  for (CanonicalClass cls: objsPerClass.keySet()) {
		  if (cls.getName().equals(DummyHeapRoot.class.getName()) ||
				  IDummySymbolic.class.isAssignableFrom(cls.getConcreteClass()))
			  continue;
		  //assert cls.getName().equals("symbolicheap.bounded.AvlTree") : "Not an AVL: " + cls.getName();
		  assert !cls.getName().contains("DummySymbolic");

		  for (int toMutateInd = 0; toMutateInd < objsPerClass.get(cls); toMutateInd++) {
			  int objRetries = 30;
			  int positiveRetries = 20;
			  boolean succeed = false;
			  while (!succeed && positiveRetries > 0) {
				  // Rebuild object before mutation
				  eSeq.execute(executionVisitor, checkGenerator);
				  mutObj = eSeq.getLastStmtRuntimeObjects().get(index);
				  // Canonicalize and mutate the current object
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine(">>> Object before mutation");
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
				  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
				  toMutateHeap = res.getValue();	
				  CanonicalObject toMutate = toMutateHeap.getCanonicalObject(cls, toMutateInd);
				  if (!toMutateHeap.mutateRandomObjectField(toMutate, objRetries)
						  || isStructurallyPositive(mutObj)) {
					  // Object could not be mutated outside the extensions
					  if (CanonicalizerLog.isLoggingOn()) 
						  CanonicalizerLog.logLine("> Mutation of the current object failed");
					  positiveRetries--;
					  continue;
				  }
				  succeed = true;
			  }
			  if (succeed) {
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine(">>> Object after mutation");
				  assert !isStructurallyPositive(mutObj);
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
				  assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
				  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
				  negVectors.add(candidateVector.toString());
				  if (NegativeSymbolicVectorsWriter.isEnabled())
					  generateNegativeSymbolicStructures(mutObj, res.getValue(), toMutateHeap.mutatedObject, toMutateHeap.mutatedField, toMutateHeap.mutatedValue);
			  }
		  }
	  }
  }

  
  /*
  private void generateNegativeSymbolicStructures(Object mutObj, CanonicalHeap mutHeap, CanonicalObject mutatedObject, CanonicalField mutatedField, CanonicalObject mutatedValue) {
	  for (CanonicalClass cls: mutHeap.objectsPerClass().keySet()) {
		  if (cls.getName().equals(DummyHeapRoot.class.getName()) ||
				  IDummySymbolic.class.isAssignableFrom(cls.getConcreteClass()))
			  continue;

		  //assert cls.getName().equals("symbolicheap.bounded.AvlTree") : "Not an AVL: " + cls.getName();
		  assert !cls.getName().contains("DummySymbolic");
		  Entry<CanonicalizationResult, CanonicalHeap> res = null;
		  // for (int toMutateInd = 0; toMutateInd < objsPerClass.get(cls); toMutateInd++) {
		  for (int ind = 0; ind < GenInputsAbstract.symneg_strs_rep; ind++) {
			  int toMutateInd = Randomness.nextRandomInt(mutHeap.objectsPerClass().get(cls));
			  CanonicalObject canMutObj = mutHeap.getCanonicalObject(cls, toMutateInd);
			  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
			  
			  //executeResetAccess(mutObj);
			  //executeInstrRepOK(mutObj);
			  if (CanonicalizerLog.isLoggingOn()) {
				  CanonicalizerLog.logLine(">>> Object after access verification");
			  }
			  
			  if (!makeRandomFieldSymbolic(mutObj, canMutObj, mutHeap, mutatedObject, mutatedField)) {
			  //assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
				  continue;
			  }
			  
			  if (isSymbolicPositive(mutObj)) return;
			  
			  if (CanonicalizerLog.isLoggingOn())
				  CanonicalizerLog.logLine(">>> Object after symbolic abstraction");
			  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
			  mutHeap = res.getValue();
			  
			  //CandidateVector<Object> cv = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
			  
			  // Check reachability of mutObj in mutHeap
			  if (mutHeap.findExistingCanonicalObject(mutatedObject.getObject(), mutatedObject.getCanonicalClass()) == null)
				  return;
			  
			  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
			  negSymbVectors.add(candidateVector.toString());
			  
			  //boolean isPositive = isSymbolicPositive(mutObj);
			  assert !isSymbolicPositive(mutObj) : "Positive vector generated after mutation: " + candidateVector.toString();
		  }
	  }
  }
  */
  
  private void generateNegativeSymbolicStructures(Object mutObj, CanonicalHeap mutHeap, CanonicalObject mutatedObject, CanonicalField mutatedField, CanonicalObject mutatedValue) {
	  for (CanonicalClass cls: mutHeap.objectsPerClass().keySet()) {
		  if (cls.getName().equals(DummyHeapRoot.class.getName()) ||
				  IDummySymbolic.class.isAssignableFrom(cls.getConcreteClass()))
			  continue;

		  //assert cls.getName().equals("symbolicheap.bounded.AvlTree") : "Not an AVL: " + cls.getName();
		  assert !cls.getName().contains("DummySymbolic");
		  Entry<CanonicalizationResult, CanonicalHeap> res = null;
		  // for (int toMutateInd = 0; toMutateInd < objsPerClass.get(cls); toMutateInd++) {
		  for (int ind = 0; ind < GenInputsAbstract.symneg_strs_rep; ind++) {
		  int toMutateInd = mutHeap.objectsPerClass().get(cls) - 1;
		  while (toMutateInd > 0) {

			  //int toMutateInd = Randomness.nextRandomInt(mutHeap.objectsPerClass().get(cls));
			  CanonicalObject canMutObj = mutHeap.getCanonicalObject(cls, toMutateInd);
			  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);

			  //executeResetAccess(mutObj);
			  //executeInstrRepOK(mutObj);
			  if (CanonicalizerLog.isLoggingOn()) {
				  CanonicalizerLog.logLine(">>> Object after access verification");
			  }

			  if (makeRandomFieldSymbolic(mutObj, canMutObj, mutHeap, mutatedObject, mutatedField)) {
				  if (isSymbolicPositive(mutObj))
					  return;

				  //assert res.getKey() == CanonicalizationResult.OK: "Mutation should not create new objects";
				  //continue;
				  if (CanonicalizerLog.isLoggingOn())
					  CanonicalizerLog.logLine(">>> Object after symbolic abstraction");
				  res = vectorCanonicalizer.traverseBreadthFirstAndCanonicalize(mutObj);
				  mutHeap = res.getValue();

				  //CandidateVector<Object> cv = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());

				  // Check reachability of mutObj in mutHeap
				  if (mutHeap.findExistingCanonicalObject(mutatedObject.getObject(), mutatedObject.getCanonicalClass()) == null)
					  return;

				  CandidateVector<Object> candidateVector = symbCandVectGenerator.makeCandidateVectorFrom(res.getValue());
				  negSymbVectors.add(candidateVector.toString());

				  //boolean isPositive = isSymbolicPositive(mutObj);
				  assert !isSymbolicPositive(mutObj) : "Positive vector generated after mutation: " + candidateVector.toString();
			  }

			  if (toMutateInd > mutHeap.objectsPerClass().get(cls) - 1)
				  toMutateInd = mutHeap.objectsPerClass().get(cls) - 1;
			  else
				  toMutateInd--;
		  }

	  }
	  }
  }
  
  
  
  private String executeToString(Object obj) {
	  Method m;
	  String res = null;
	  try {
		  m = obj.getClass().getDeclaredMethod("treeToString");
		  m.setAccessible(true);
		  res = (String) m.invoke(obj);
	  } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		  System.out.println("ERROR: problem occurred while trying to invoke repOK method to classify negatives");
		  e.printStackTrace();
		  System.exit(0);
	  }
	  return res;
  }
  
  
  private void executeResetAccess(Object obj) {
	  Method m;
	  try {
		  m = obj.getClass().getDeclaredMethod("resetAccess");
		  m.setAccessible(true);
		  m.invoke(obj);
	  } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		  System.out.println("ERROR: problem occurred while trying to invoke repOK method to classify negatives");
		  e.printStackTrace();
		  System.exit(0);
	  }
  }
  
  
  private boolean executeInstrRepOK(Object obj) {
	  Boolean res = false;
	  Method m;
	  try {
		  m = obj.getClass().getDeclaredMethod("repOK_Instr");
		  m.setAccessible(true);
		  res = ((Boolean) m.invoke(obj));
	  } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		  System.out.println("ERROR: problem occurred while trying to invoke repOK_Instr method to classify negatives");
		  e.printStackTrace();
		  System.exit(0);
	  }
	  return res;
  }

  
  private boolean isPositive(Object obj) {
	  Boolean res = false;
	  Method m;
	  try {
		  m = obj.getClass().getDeclaredMethod("repOK");
		  m.setAccessible(true);
		  res = ((Boolean) m.invoke(obj));
	  } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		  System.out.println("ERROR: problem occurred while trying to invoke repOK method to classify negatives");
		  e.printStackTrace();
		  System.exit(0);
	  }
	  return res;
  }

  private boolean isStructurallyPositive(Object obj) {
	  Boolean res = false;
	  Method m;
	  try {
//		  m = obj.getClass().getDeclaredMethod("repOK");
		  m = obj.getClass().getDeclaredMethod("structuralHybridRepOK");
		  m.setAccessible(true);
		  res = ((Boolean) m.invoke(obj));
	  } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		  System.out.println("ERROR: problem occurred while trying to invoke structuralHybridRepOK method to classify negatives");
		  e.printStackTrace();
		  System.exit(0);
	  }
	  return res;
  }

  
  private boolean isSymbolicPositive(Object obj) {
	  Boolean res = false;
	  Method m;
	  try {
//		  m = obj.getClass().getDeclaredMethod("hybridRepOK");
		  m = obj.getClass().getDeclaredMethod("structuralHybridRepOK");
		  m.setAccessible(true);
		  res = ((Boolean) m.invoke(obj));
	  } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		  System.out.println("ERROR: problem occurred while trying to invoke structuralHybridRepOK method to classify negatives");
		  e.printStackTrace();
		  System.exit(0);
	  }
	  return res;
  }


  


  
}
