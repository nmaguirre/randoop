package randoop.generation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.BugInRandoopException;
import randoop.DummyVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.SubTypeSet;
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

  private int bedepth;

  public ForwardGeneratorBE(
      List<TypedOperation> operations,
      Set<TypedOperation> observers,
      long timeMillis,
      int maxGenSequences,
      int maxOutSequences,
      int beiterations, 
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
    this.bedepth = beiterations;
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
  
  
  public void gen() {
	  ComponentManager prevMan = new ComponentManagerBE();
	  prevMan.gralSeeds = componentManager.gralSeeds;
	  for (int iteration = 0; iteration < bedepth; iteration++) {
		  ComponentManager currMan = new ComponentManagerBE(); 
		  currMan.gralSeeds = componentManager.gralSeeds;

		  if (stop()) {
			  System.out.println("DEBUG: Stopping criteria reached");
			  break;
		  }

		  // Notify listeners we are about to perform a generation step.
		  if (listenerMgr != null) {
			  listenerMgr.generationStepPre();
		  }

		  num_steps++;

		  if (Log.isLoggingOn()) {
			  Log.logLine("-------------------------------------------");
		  }

		  for (TypedOperation operation: this.operations) {
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
				  
				  // TODO: Pablo: no primitive values can be obtained here, never
				  // We always get null values from the list instead of primitives
				  SimpleList<Sequence> l = prevMan.getSequencesForType(operation, i);
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
					  /*
				  SimpleList<Sequence> l = componentManager.getSequencesForType(operation, i);
				  if (Log.isLoggingOn()) {
					  Log.logLine("Will query component set for objects of type" + inputType);
					  Log.logLine("components: " + l.size());
				  }

					   */
					  // Sequence chosenSeq = Randomness.randomMember(l);

					  // TODO: Pablo: See how to deal with null!!!
					  Sequence chosenSeq = currParams.get(i); 
					  Variable randomVariable = chosenSeq.randomVariableForTypeLastStatement(inputType);
					  if (randomVariable == null) {
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
				  if (this.allSequences.contains(newSequence)) 
					  continue;
				  

				  // If parameterless statement, subsequence inputs
				  // will all be redundant, so just remove it from list of statements.
				  // XXX does this make sense? especially in presence of side-effects
				  /*
				  if (operation.getInputTypes().isEmpty()) {
					operations.remove(operation);
				  }
				   */

				  randoopConsistencyTests(newSequence);
				  randoopConsistencyTest2(newSequence);

				  if (Log.isLoggingOn()) {
					  Log.logLine(
							  String.format("Successfully created new unique sequence:%n%s%n", newSequence.toString()));
				  }

				  // TODO: Is this useful in BE?
				  this.allSequences.add(newSequence);
				  ExecutableSequence eSeq = new ExecutableSequence(newSequence);

				  // Execute new sequence
				  execute(eSeq);

				  // TODO: Save the sequence for further extension?
				  if (eSeq.isNormalExecution())
					  currMan.addGeneratedSequence(eSeq.sequence);

				  // Notify listeners we just completed generation step.
				  if (listenerMgr != null) {
					  listenerMgr.generationStepPost(eSeq);
				  }

				  num_sequences_generated++;

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

			  } // End loop for current operation

		  } // End loop for all operations 

		  // Previous component manager is 
		  prevMan = currMan;
	  } // End of all iterations
  }
  

  

  public void execute(ExecutableSequence eSeq) {

  //  long startTime = System.nanoTime();

    setCurrentSequence(eSeq.sequence);

   // long endTime = System.nanoTime();
   // long gentime = endTime - startTime;
//    startTime = endTime; // reset start time.
    long startTime = System.nanoTime(); // reset start time.

    eSeq.execute(executionVisitor, checkGenerator);
    
    long endTime = System.nanoTime();

    eSeq.exectime = endTime - startTime;
    startTime = endTime; // reset start time.
 
    processSequence(eSeq);

    /*
    if (eSeq.sequence.hasActiveFlags()) {
        componentManager.addGeneratedSequence(eSeq.sequence);
    }
    */

    //endTime = System.nanoTime();
    //gentime += endTime - startTime;
    // eSeq.gentime = gentime;
    eSeq.gentime = 0;
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

}
