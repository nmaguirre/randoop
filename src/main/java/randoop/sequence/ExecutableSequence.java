package randoop.sequence;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Arrays;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.NotExecuted;
import randoop.generation.AbstractGenerator;
import randoop.generation.ForwardGenerator;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.test.Check;
import randoop.test.TestCheckGenerator;
import randoop.test.TestChecks;
import randoop.types.Type;
import randoop.types.ReferenceType;
import randoop.util.IdentityMultiMap;
import randoop.util.ProgressDisplay;
import randoop.util.fieldbasedcontrol.CanonizationErrorException;
import randoop.util.fieldbasedcontrol.CanonizerClass;
import randoop.util.fieldbasedcontrol.FieldBasedGenLog;
import randoop.util.fieldbasedcontrol.FieldExtensionsIndexes;
import randoop.util.fieldbasedcontrol.FieldExtensionsIndexesMap;
import randoop.util.fieldbasedcontrol.FieldExtensionsStrings;
import randoop.util.fieldbasedcontrol.HeapCanonizerRuntimeEfficient;
import randoop.util.fieldbasedcontrol.Tuple;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;
import randoop.util.heapcanonicalization.ExtendExtensionsResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.fieldextensions.BugInFieldExtensionsCanonicalization;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensions;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsByTypeCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStringsCollector;

/**
 * An ExecutableSequence wraps a {@link Sequence} with functionality for
 * executing the sequence. It also lets the client add {@link Check}s that check
 * expected behaviors of the execution.
 * <p>
 * An ExecutableSequence augments a sequence with three additional pieces of
 * data:
 * <ul>
 * <li><b>Execution results.</b> An ExecutableSequence can be executed, and the
 * results of the execution (meaning the objects created during execution, and
 * any exceptions thrown) are made available to clients or execution visitors to
 * inspect.
 * <li><b>Checks.</b> A check is an object representing an expected runtime
 * behavior of the sequence. Clients can add checks to specific indices of the
 * sequence. For example, a client might add a <code>NotNull</code> check to the
 * ith index of a sequence to signify that the value returned by the statement
 * at index i should not be null.
 * <li><b>Check evaluation results.</b> Corresponding to every check is a
 * boolean value that represents whether the check passed or failed during the
 * last execution of the sequence.
 * </ul>
 * <p>
 *
 * Of the three pieces of data above, an ExecutableSequence only directly
 * manages the first one, i.e. the execution results. Other pieces of data,
 * including checks and check evaluation results, are added or removed by the
 * client of the ExecutableSequence. One way of doing this is by implementing an
 * {@link ExecutionVisitor} and passing it as an argument to the
 * <code>execute</code> method.
 *
 * <p>
 *
 * The method <code>execute(ExecutionVisitor v)</code> executes the code that
 * the sequence represents. This method uses reflection to execute each element
 * in the sequence (method call, constructor call, primitive or array
 * declaration, etc). Before executing each statement (e.g. the i-th statement),
 * execute(v) calls v.visitBefore(this, i), and after executing each statement,
 * it calls v.visitAfter(this, i). The purpose of the visitor is to examine the
 * unfolding execution, and take some action depending on its intended purpose.
 * For example, it may decorate the sequence with {@link Check}s about the
 * execution.
 *
 * <p>
 *
 * NOTES.
 *
 * <ul>
 * <li>It only makes sense to call the following methods <b>after</b> executing
 * the i-th statement in a sequence:
 * <ul>
 * <li>isNormalExecution(i)
 * <li>isExceptionalExecution(i)
 * <li>getExecutionResult(i)
 * <li>getResult(i)
 * <li>getException(i)
 * </ul>
 * </ul>
 *
 */
public class ExecutableSequence {

  // PABLO: fields for field based generation
  //  public static int seqnum = 0;
//   public String FILENAME = "logs/seq";
  // public boolean extendedExtensions;
  // public boolean canonizationError;
  // public boolean normalExecution;
//  private boolean DIFFERENTIAL = false;
//  private boolean DEBUG = false;
  public ExtendExtensionsResult enlargesExtensions = ExtendExtensionsResult.NOT_EXTENDED;
  public boolean canonizationError = false;
  public boolean endsWithObserverReturningNewValue;
  public boolean endsWithObserver;
	
  private List<FieldExtensionsIndexes> lastStmtFormerExt;
  private List<FieldExtensionsIndexes> lastStmtNextExt;


 /** The underlying sequence. */
  public Sequence sequence;

  /** The checks for this sequence */
  private TestChecks checks;

  /**
   * Contains the runtime objects created and exceptions thrown (if any) during
   * execution of this sequence. Invariant: sequence.size() ==
   * executionResults.size(). Transient because it can contain arbitrary objects
   * that may not be serializable.
   */
  private transient /* final */ Execution executionResults;

  /**
   * How long it took to generate this sequence in nanoseconds, excluding
   * execution time. Must be directly set by the generator that creates this
   * object (no code in this class sets its value).
   */
  public long gentime = -1;

  /**
   * How long it took to execute this sequence in nanoseconds, excluding
   * generation time. Must be directly set by the generator that creates this
   * object. (no code in this class sets its value).
   */
  public long exectime = -1;

  /**
   * Flag to record whether execution of sequence has a null input. [this is
   * wonky really belongs to execution
   */
  private boolean hasNullInput;

  /** Output buffer used to capture the output from the executed sequence **/
  private static ByteArrayOutputStream output_buffer = new ByteArrayOutputStream();
  private static PrintStream ps_output_buffer = new PrintStream(output_buffer);

  private IdentityMultiMap<Object, Variable> variableMap;



  



  /**
   * Create an executable sequence that executes the given sequence.
   *
   * @param sequence  the underlying sequence for this executable sequence
   */
  public ExecutableSequence(Sequence sequence) {
    this.sequence = sequence;
    this.executionResults = new Execution(sequence);
    this.hasNullInput = false;
    this.variableMap = new IdentityMultiMap<>();
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < sequence.size(); i++) {
      sequence.appendCode(b, i);
      // It's a bit confusing, but the commented execution results refer
      // to the statement ABOVE, not below as is standard for comments.
      if (executionResults.size() > i) b.append(executionResults.get(i).toString());
      if ((i == sequence.size() - 1) && (checks != null)) {
        Map<Check, Boolean> ckMap = checks.get();
        for (Map.Entry<Check, Boolean> entry : ckMap.entrySet()) {
          b.append(Globals.lineSep);
          b.append(entry.getKey().toString());
          b.append(" : ");
          b.append(entry.getValue().toString());
        }
      }
      b.append(Globals.lineSep);
    }
    return b.toString();
  }

  /**
   * Return this sequence as code. Similar to {@link Sequence#toCodeString()}
   * except includes the checks.
   *
   * If for a given statement there is a check of type
   * {@link randoop.test.ExceptionCheck}, that check's pre-statement code is printed
   * immediately before the statement, and its post-statement code is printed
   * immediately after the statement.
   *
   * @return the sequence as a string
   */
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < sequence.size(); i++) {

      // Only print primitive declarations if the last/only statement
      // of the sequence, because, otherwise, primitive values will be used as
      // actual parameters: e.g. "foo(3)" instead of "int x = 3 ; foo(x)"
      if (sequence.getStatement(i).getShortForm() != null && i < sequence.size() - 1) {
        continue;
      }

      StringBuilder oneStatement = new StringBuilder();
      sequence.appendCode(oneStatement, i);

      if (i == sequence.size() - 1 && checks != null) {
        // Print exception check first, if present.
        Check exObs = checks.getExceptionCheck();
        if (exObs != null) {
          oneStatement.insert(0, exObs.toCodeStringPreStatement());
          oneStatement.append(exObs.toCodeStringPostStatement());
          oneStatement.append(Globals.lineSep);
        }

        // Print the rest of the checks.
        for (Check d : checks.get().keySet()) {
          oneStatement.insert(0, d.toCodeStringPreStatement());
          oneStatement.append(d.toCodeStringPostStatement());
          oneStatement.append(Globals.lineSep);
        }
      }
      b.append(oneStatement);
    }
    return b.toString();
  }

  /**
   * Return the code representation of the i'th statement.
   * @param i  the statement index
   * @return the string representation of the statement
   */
  public String statementToCodeString(int i) {
    StringBuilder oneStatement = new StringBuilder();
    sequence.appendCode(oneStatement, i);
    return oneStatement.toString();
  }
  
  
  /**
   * Executes sequence, stopping on exceptions.
   *
   * @param visitor
   *          the {@link ExecutionVisitor} that collects checks from results
   * @param gen
   *          the check generator for tests
   */
  public void execute(ExecutionVisitor visitor, TestCheckGenerator gen) {
    execute(visitor, gen, true);
  }

  /**
   * Execute this sequence, invoking the given visitor as the execution unfolds.
   * After invoking this method, the client can query the outcome of executing
   * each statement via the method <code>getResult(i)</code>.
   *
   * <ul>
   * <li>Before the sequence is executed, clears execution results and calls
   * <code>visitor.initialize(this)</code>.
   * <li>Executes each statement in the sequence. Before executing each
   * statement calls the given visitor's <code>visitBefore</code> method. After
   * executing each statement, calls the visitor's <code>visitAfter</code>
   * method.
   * <li>Execution stops if one of the following conditions holds:
   * <ul>
   * <li>All statements in the sequences have been executed.
   * <li>A statement's execution results in an exception and
   * <code>stop_on_exception==true</code>.
   * <li>A <code>null</code> input value is implicitly passed to the statement
   * (i.e., not via explicit declaration like x = null)
   * <li>After executing the i-th statement and calling the visitor's
   * <code>visitAfter</code> method, a <code>ContractViolation</code> check is
   * present at index i.
   * </ul>
   * </ul>
   *
   * @param visitor
   *          the {@code ExecutionVisitor}
   * @param gen
   *          the check generator
   * @param ignoreException
   *          the flag to indicate exceptions should be ignored
   */
  private void execute(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException) {

    visitor.initialize(this);

    // reset execution result values
    hasNullInput = false;
    executionResults.theList.clear();
    for (int i = 0; i < sequence.size(); i++) {
      executionResults.theList.add(NotExecuted.create());
    }

    for (int i = 0; i < this.sequence.size(); i++) {

      // Find and collect the input values to i-th statement.
      List<Variable> inputs = sequence.getInputs(i);
      Object[] inputVariables;

      inputVariables = getRuntimeInputs(executionResults.theList, inputs);

      visitor.visitBeforeStatement(this, i);
      executeStatement(sequence, executionResults.theList, i, inputVariables);

      // make sure statement executed
      ExecutionOutcome statementResult = getResult(i);
      if (statementResult instanceof NotExecuted) {
        throw new Error("Unexecuted statement in sequence: " + this.toString());
      }
      // make sure no exception before final statement of sequence
      if ((statementResult instanceof ExceptionalExecution) && i < sequence.size() - 1) {
        if (ignoreException) {
          // this preserves previous behavior, which was simply to return if
          // exception occurred
          break;
        } else {
          String msg =
              "Encountered exception before final statement of error-revealing test (statement "
                  + i
                  + "): ";
          throw new Error(
              msg + ((ExceptionalExecution) statementResult).getException().getMessage());
        }
      }

      visitor.visitAfterStatement(this, i);
    }

    visitor.visitAfterSequence(this);

    checks = gen.visit(this);
  }
  

  public void executeFB(ExecutionVisitor visitor, TestCheckGenerator gen) {
	  try {
		  executeFB(visitor, gen, true, null, false);
	  } catch (CanonizationErrorException e) {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
	  }
  }

  
  /**
   * Executes sequence, stopping on exceptions.
   *
   * @param visitor
   *          the {@link ExecutionVisitor} that collects checks from results
   * @param gen
   *          the check generator for tests
 * @throws CanonizationErrorException 
   */
  public void executeFB(ExecutionVisitor visitor, TestCheckGenerator gen, HeapCanonizerRuntimeEfficient canonizer) throws CanonizationErrorException {
	  executeFB(visitor, gen, true, canonizer, false);
  }
  
  
   public void executeFBSecondPhase(ExecutionVisitor visitor, TestCheckGenerator gen, HeapCanonizerRuntimeEfficient canonizer) throws CanonizationErrorException {
	  executeFB(visitor, gen, true, canonizer, true);
  }
   
    public void executeFBSecondPhaseNoReexecute(ExecutionVisitor visitor, TestCheckGenerator gen, 
			HeapCanonizerRuntimeEfficient canonizer, ExecutableSequence extendedSeq, int startIndex, int endIndex) throws CanonizationErrorException {
    	executeFBSecondPhaseNoReexecute(visitor, gen, true, canonizer, true, extendedSeq, startIndex, endIndex);
    }
  
   public void executeFBSecondPhaseNoReexecute(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException, 
			HeapCanonizerRuntimeEfficient canonizer, boolean secondPhase, ExecutableSequence extendedSeq, int startIndex, int endIndex) throws CanonizationErrorException {
		    visitor.initialize(this);

		    // reset execution result values
		    hasNullInput = false;
		    executionResults.theList.clear();
		    int extendedSeqCurrInd = 0;
		    for (int i = 0; i < sequence.size(); i++) {
		    	if (i >= startIndex && i <= endIndex) {
		    		// These statements were already executed in extendedSeq
		    		executionResults.theList.add(extendedSeq.executionResults.theList.get(extendedSeqCurrInd));
		    		extendedSeqCurrInd++;
		    	}
		    	else	
		    		executionResults.theList.add(NotExecuted.create());
		    }
		    
		    lastStmtFormerExt = null;
		    lastStmtNextExt = null;
		    endsWithObserverReturningNewValue = false;
		    endsWithObserver = false;
		    for (int i = 0; i < this.sequence.size(); i++) {
		    	
		      // These statements were already executed in extendedSeq,
		      // do not execute them again
		      if (i >= startIndex && i <= endIndex) continue;

		      // Find and collect the input values to i-th statement.
		      List<Variable> inputs = sequence.getInputs(i);
		      Object[] inputVariables;

		      inputVariables = getRuntimeInputs(executionResults.theList, inputs);

		      TypedOperation op = null;
		      if (canonizer != null && i == sequence.size() - 1) {
		    	  op = sequence.getStatement(i).getOperation();
		     	  if (FieldBasedGenLog.isLoggingOn()) {
		    		  if (op.notExecuted())
		    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " was never executed"); 
		    		  else if (op.isObserver())
		    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is an observer for now"); 
		    		  else if (op.isFinalObserver())
		    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is an observer"); 
		    		  else
		    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is a modifier"); 
		     	  }

				  if (sequence.size() > 1 && !op.isFinalObserver() && (op.notExecuted() || op.isObserver()))
					  lastStmtFormerExt = createExtensionsForAllObjects(i, null, inputVariables, canonizer, true);
		      }
		      
		      visitor.visitBeforeStatement(this, i);
		      executeStatement(sequence, executionResults.theList, i, inputVariables);

		      // make sure statement executed
		      ExecutionOutcome statementResult = getResult(i);
		      if (statementResult instanceof NotExecuted) {
		        throw new Error("Unexecuted statement in sequence: " + this.toString());
		      }
		      // make sure no exception before final statement of sequence
		      if ((statementResult instanceof ExceptionalExecution) && i < sequence.size() - 1) {
		        if (ignoreException) {
		          // this preserves previous behavior, which was simply to return if
		          // exception occurred
		          break;
		        } else {
		          String msg =
		              "Encountered exception before final statement of error-revealing test (statement "
		                  + i
		                  + "): ";
		          throw new Error(
		              msg + ((ExceptionalExecution) statementResult).getException().getMessage());
		        }
		      }
		      
		      if (canonizer != null && i == sequence.size() - 1 && statementResult instanceof NormalExecution) {
		    	  
		    	  if (op.isFinalObserver()) {
					  lastStmtNextExt = createExtensionsForAllObjects(i, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer, false);
					  
					  createLastStmtActiveVars();
		    	  } 
		    	  else {
					  lastStmtNextExt = createExtensionsForAllObjects(i, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer, true);
					  
					  createLastStmtActiveVars();
				  
					  // check if the current op is an observer or a modifier
					  if (!op.isModifier()) { 
						  if (sequence.size() == 1) {
							  for (int j = 0; j < lastStmtNextExt.size(); j++) {
								  if (lastStmtNextExt.get(j) != null) { 							  
									  if (FieldBasedGenLog.isLoggingOn())
										  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as modifier");

									  op.setModifier();
									  break;
								  }
							  }
						  }
						  else { //sequence.size() > 1 
							  assert lastStmtFormerExt.size() == lastStmtNextExt.size();
							  for (int j = 0; j < lastStmtNextExt.size(); j++) {
								  assert !(lastStmtFormerExt.get(j) != null && lastStmtNextExt.get(j) == null);

								  if (lastStmtFormerExt.get(j) == null && lastStmtNextExt.get(j) == null)
									  continue;
								  else {
									  if (!lastStmtNextExt.get(j).equals(lastStmtFormerExt.get(j))) {
										  if (FieldBasedGenLog.isLoggingOn())
											  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as modifier");

										  op.setModifier();
										  // if (AbstractGenerator.field_based_gen == FieldBasedGenType.DISABLED || secondPhase)
										  break;
									  }
								  }
							  }
						  }
					  }

					  // If it's not a modifier, mark the current action as an observer or final observer
					  if (!op.isModifier()) {
						  // First check if the current observer produces a new primitive value
						  if (!secondPhase && !op.getOutputType().isVoid()) {
							  Object obj = ((NormalExecution)statementResult).getRuntimeValue();
							  if (obj != null && isObjectPrimtive(obj)) {
								  if (canonizer.traverseBreadthFirstAndEnlargeExtensions(obj, canonizer.getPrimitiveExtensions()) == ExtendExtensionsResult.EXTENDED) {
									  endsWithObserverReturningNewValue = true;

									  //System.out.println("Prim ext size:" + canonizer.getPrimitiveExtensions().size());
									  if (FieldBasedGenLog.isLoggingOn())
										  FieldBasedGenLog.logLine("> Value " + obj.toString() + " was added to primitive field bounds");				
								  }
								  else 
									  if (FieldBasedGenLog.isLoggingOn())
										  FieldBasedGenLog.logLine("> Value " + obj.toString() + " already belongs to primitive field bounds");				
							  }
						  }

						  op.observerTimesExecuted++;
						  if (secondPhase && op.observerTimesExecuted > AbstractGenerator.fbg_final_observer_after) {
							  op.setFinalObserver(); 
							  if (FieldBasedGenLog.isLoggingOn())
								  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as final observer");
						  }
						  else {
							  op.setObserver();
							  if (FieldBasedGenLog.isLoggingOn())
								  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as observer");
						  }

					  }
		    	  
		    	  }

		    	  if (!op.isModifier()) {
		    		  endsWithObserver = true;
		    		  if (FieldBasedGenLog.isLoggingOn()) 
		    			  FieldBasedGenLog.logLine("> The current sequence ends with an observer");
		    	  }
		    	  else 
					  if (FieldBasedGenLog.isLoggingOn()) 
						  FieldBasedGenLog.logLine("> The current sequence ends with a modifier");
		      }
		      
		      visitor.visitAfterStatement(this, i);
		    }

		    visitor.visitAfterSequence(this);

		    
		   	checks = gen.visit(this);
		
	}

   
   public Object getLastStmtReceiverObject() {
	   int i = sequence.size()-1;
	   List<Variable> inputs = sequence.getInputs(i);
	   if (inputs.size() == 0)
		   // No receiver object in the last statement
		   return null;

	   Object[] inputVariables = getRuntimeInputs(executionResults.theList, inputs);
	   return inputVariables[0];
   }
   
   
   public List<Object> getLastStmtRuntimeObjects() {
	   int i = sequence.size()-1;
	   List<Variable> inputs = sequence.getInputs(i);
	   List<Object> res = new LinkedList<>();

	   Statement stmt = sequence.getStatement(i);
	   if (!stmt.getOutputType().isVoid()) 
		   res.add(((NormalExecution)getResult(i)).getRuntimeValue());

	   for (Object o: getRuntimeInputs(executionResults.theList, inputs)) {
		   res.add(o);
	   }
	   
	   return res;
   }
   
  
  /**
   * Execute this sequence, invoking the given visitor as the execution unfolds.
   * After invoking this method, the client can query the outcome of executing
   * each statement via the method <code>getResult(i)</code>.
   *
   * <ul>
   * <li>Before the sequence is executed, clears execution results and calls
   * <code>visitor.initialize(this)</code>.
   * <li>Executes each statement in the sequence. Before executing each
   * statement calls the given visitor's <code>visitBefore</code> method. After
   * executing each statement, calls the visitor's <code>visitAfter</code>
   * method.
   * <li>Execution stops if one of the following conditions holds:
   * <ul>
   * <li>All statements in the sequences have been executed.
   * <li>A statement's execution results in an exception and
   * <code>stop_on_exception==true</code>.
   * <li>A <code>null</code> input value is implicitly passed to the statement
   * (i.e., not via explicit declaration like x = null)
   * <li>After executing the i-th statement and calling the visitor's
   * <code>visitAfter</code> method, a <code>ContractViolation</code> check is
   * present at index i.
   * </ul>
   * </ul>
   *
   * @param visitor
   *          the {@code ExecutionVisitor}
   * @param gen
   *          the check generator
   * @param ignoreException
   *          the flag to indicate exceptions should be ignored
 * @param canonizer 
 * @throws CanonizationErrorException 
   */
  private void executeFB(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException, HeapCanonizerRuntimeEfficient canonizer, boolean secondPhase)  throws CanonizationErrorException {

    visitor.initialize(this);

    // reset execution result values
    hasNullInput = false;
    executionResults.theList.clear();
    for (int i = 0; i < sequence.size(); i++) {
      executionResults.theList.add(NotExecuted.create());
    }

    
    lastStmtFormerExt = null;
    lastStmtNextExt = null;
    endsWithObserverReturningNewValue = false;
    endsWithObserver = false;
    for (int i = 0; i < this.sequence.size(); i++) {

      // Find and collect the input values to i-th statement.
      List<Variable> inputs = sequence.getInputs(i);
      Object[] inputVariables;

      inputVariables = getRuntimeInputs(executionResults.theList, inputs);

      TypedOperation op = null;
      if (canonizer != null && i == sequence.size() - 1) {
    	  op = sequence.getStatement(i).getOperation();
     	  if (FieldBasedGenLog.isLoggingOn()) {
    		  if (op.notExecuted())
    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " was never executed"); 
    		  else if (op.isObserver())
    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is an observer for now"); 
    		  else if (op.isFinalObserver())
    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is an observer"); 
    		  else
    			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is a modifier"); 
     	  }

		  if (sequence.size() > 1 && !op.isFinalObserver() && (op.notExecuted() || op.isObserver()))
			  lastStmtFormerExt = createExtensionsForAllObjects(i, null, inputVariables, canonizer, true);
      }
      
      visitor.visitBeforeStatement(this, i);
      executeStatement(sequence, executionResults.theList, i, inputVariables);

      // make sure statement executed
      ExecutionOutcome statementResult = getResult(i);
      if (statementResult instanceof NotExecuted) {
        throw new Error("Unexecuted statement in sequence: " + this.toString());
      }
      // make sure no exception before final statement of sequence
      if ((statementResult instanceof ExceptionalExecution) && i < sequence.size() - 1) {
        if (ignoreException) {
          // this preserves previous behavior, which was simply to return if
          // exception occurred
          break;
        } else {
          String msg =
              "Encountered exception before final statement of error-revealing test (statement "
                  + i
                  + "): ";
          throw new Error(
              msg + ((ExceptionalExecution) statementResult).getException().getMessage());
        }
      }
      
      if (canonizer != null && i == sequence.size() - 1) {
    	  if (statementResult instanceof NormalExecution) {

    		  if (op.isFinalObserver()) {
    			  //if (AbstractGenerator.field_based_gen_differential_runtime_checks) canonizer.saveToDifferentialExtensions = true;
    			  lastStmtNextExt = createExtensionsForAllObjects(i, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer, false);
    			  //if (AbstractGenerator.field_based_gen_differential_runtime_checks) canonizer.saveToDifferentialExtensions = false;

    			  createLastStmtActiveVars();
    		  } 
    		  else {
    			  //if (AbstractGenerator.field_based_gen_differential_runtime_checks) canonizer.saveToDifferentialExtensions = true;
    			  lastStmtNextExt = createExtensionsForAllObjects(i, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer, true);
    			  //if (AbstractGenerator.field_based_gen_differential_runtime_checks) canonizer.saveToDifferentialExtensions = false;

    			  createLastStmtActiveVars();

    			  // check if the current op is an observer or a modifier
    			  if (!op.isModifier()) { 
    				  if (sequence.size() == 1) {
    					  for (int j = 0; j < lastStmtNextExt.size(); j++) {
    						  if (lastStmtNextExt.get(j) != null) { 							  
    							  if (FieldBasedGenLog.isLoggingOn())
    								  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as modifier");

    							  op.setModifier();
    							  break;
    						  }
    					  }
    				  }
    				  else { //sequence.size() > 1 
    					  assert lastStmtFormerExt.size() == lastStmtNextExt.size();
    					  for (int j = 0; j < lastStmtNextExt.size(); j++) {
    						  assert !(lastStmtFormerExt.get(j) != null && lastStmtNextExt.get(j) == null);

    						  if (lastStmtFormerExt.get(j) == null && lastStmtNextExt.get(j) == null)
    							  continue;
    						  else {
    							  if (!lastStmtNextExt.get(j).equals(lastStmtFormerExt.get(j))) {
    								  if (FieldBasedGenLog.isLoggingOn())
    									  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as modifier");

    								  op.setModifier();
    								  // if (AbstractGenerator.field_based_gen == FieldBasedGenType.DISABLED || secondPhase)
    								  break;
    							  }
    						  }
    					  }
    				  }
    			  }

    			  // If it's not a modifier, mark the current action as an observer or final observer
    			  if (!op.isModifier()) {
    				  // First check if the current observer produces a new primitive value
    				  if (!secondPhase && !op.getOutputType().isVoid()) {
    					  Object obj = ((NormalExecution)statementResult).getRuntimeValue();
    					  if (obj != null && isObjectPrimtive(obj)) {
    						  if (canonizer.traverseBreadthFirstAndEnlargeExtensions(obj, canonizer.getPrimitiveExtensions()) == ExtendExtensionsResult.EXTENDED) {
    							  endsWithObserverReturningNewValue = true;

    							  if (FieldBasedGenLog.isLoggingOn())
    								  FieldBasedGenLog.logLine("> Value " + obj.toString() + " was added to primitive field bounds");				
    						  }
    						  else 
    							  if (FieldBasedGenLog.isLoggingOn())
    								  FieldBasedGenLog.logLine("> Value " + obj.toString() + " already belongs to primitive field bounds");				
    					  }
    				  }

    				  op.observerTimesExecuted++;
    				  if (secondPhase && op.observerTimesExecuted > AbstractGenerator.fbg_final_observer_after) {
    					  op.setFinalObserver(); 
    					  if (FieldBasedGenLog.isLoggingOn())
    						  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as final observer");
    				  }
    				  else {
    					  op.setObserver();
    					  if (FieldBasedGenLog.isLoggingOn())
    						  FieldBasedGenLog.logLine("> Operation " + op.toString() + " flagged as observer");
    				  }

    			  }

    		  }

    		  if (!op.isModifier()) {
    			  endsWithObserver = true;
    			  if (FieldBasedGenLog.isLoggingOn()) 
    				  FieldBasedGenLog.logLine("> The current sequence ends with an observer");
    		  }
    		  else 
    			  if (FieldBasedGenLog.isLoggingOn()) 
    				  FieldBasedGenLog.logLine("> The current sequence ends with a modifier");
    	  }
    	  else {
    		  // Execution failed, if the operation is not executed mark it as an observer for the moment
    		  if (op.notExecuted())
    			  op.setObserver();
       	  }
      }
    	  
      visitor.visitAfterStatement(this, i);
    }

    visitor.visitAfterSequence(this);

    
   	checks = gen.visit(this);
  
  }
  

   public void executeFB(ExecutionVisitor visitor, TestCheckGenerator gen, HeapCanonicalizer canonicalizer, FieldExtensions globalExt) {
	   executeFB(visitor, gen, true, canonicalizer, globalExt);
   }
  
   private void logOperationType(TypedOperation op) {
  	  if (FieldBasedGenLog.isLoggingOn()) {
 		  if (op.notExecuted())
 			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " was never executed"); 
 		  else if (op.isObserver())
 			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is an observer for the moment"); 
 		  else if (op.isFinalObserver())
 			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is an observer (final)"); 
 		  else
 			  FieldBasedGenLog.logLine("> Operation " + op.toString() + " is a modifier"); 
  	  }
   }
   
   
   private List<Boolean> lastStmtNonPrimIndexesList;
   private Set<Integer> lastStmtNonPrimIndexes;
   
   
   public List<Boolean> getLastStmtNonPrimIndexesList() {
	   return lastStmtNonPrimIndexesList;
   }

   public Set<Integer> getLastStmtNonPrimIndexes() {
	   return lastStmtNonPrimIndexes;
   }
   
   public List<Integer> getLastStmtActiveIndexes() {
	   return sequence.getActiveVars(sequence.size()-1);
   }
  
   private void computeLastStmtNonPrimIndexes(List<Object> objs) {
	   lastStmtNonPrimIndexes = new HashSet<>();
	   lastStmtNonPrimIndexesList = new ArrayList<>();
	   for (int i = 0; i < objs.size(); i++) {
		   if (objs.get(i) == null || !isPrimitive(objs.get(i))) {
			   lastStmtNonPrimIndexesList.add(true);
			   lastStmtNonPrimIndexes.add(i);
		   }
		   else 
			   lastStmtNonPrimIndexesList.add(false);
	   }
   }
   
   private List<Object> getObjectsForStatement(int i, Object statementResult, Object[] inputVariables) {
	   Statement stmt = sequence.getStatement(i);
	   List<Object> objects = new ArrayList<>();
	   if (!stmt.getOutputType().isVoid()) 
		   objects.add(statementResult);
	   for (int j = 0; j < inputVariables.length; j++)
		   objects.add(inputVariables[j]);
	   return objects;
   }
   
   private List<FieldExtensions> createExtensionsForAllObjects(List<Object> objects, HeapCanonicalizer canonicalizer) { 
	   List<FieldExtensions> extensions = new ArrayList<>();
	   for (Object o: objects) {
		   if (o != null) {
			   FieldExtensionsCollector collector;
			   if (AbstractGenerator.fbg_low_level_primitive)
				   collector = new FieldExtensionsByTypeCollector(GenInputsAbstract.string_maxlen);
			   else 
				   collector = new FieldExtensionsStringsCollector(GenInputsAbstract.string_maxlen);
			   Entry<CanonicalizationResult, CanonicalHeap> res = canonicalizer.traverseBreadthFirstAndCanonicalize(o, collector);
			   if (res.getKey() != CanonicalizationResult.OK)
				   throw new BugInFieldExtensionsCanonicalization("Structure exceeding limits. We don't support these yet.");
			   extensions.add(collector.getExtensions());
		   }
		   else
			   // We don't save sequences for building null objects
			   extensions.add(null);
	   }
   
	   
	   return extensions; 
   }
   
   
   private boolean isPrimitive(Object o) {
	   Class<?> cls = o.getClass();
	   return NonreceiverTerm.isNonreceiverType(cls) ||
	   		// We treat these as primitives
				  cls.equals(BigInteger.class) || 
				  cls.equals(BigDecimal.class);
   }
   
   private ExtendExtensionsResult lift(ExtendExtensionsResult oldres, ExtendExtensionsResult newres) {
	   assert oldres != ExtendExtensionsResult.LIMITS_EXCEEDED && newres != ExtendExtensionsResult.LIMITS_EXCEEDED:
		   "Canonicalization of structures exceeding limits not supported";

	   switch (oldres) {
	   case EXTENDED:
		   return oldres;
	   case EXTENDED_PRIMITIVE:
		   if (newres == ExtendExtensionsResult.EXTENDED)
			   return newres;
		   else
			   return oldres;
	   case NOT_EXTENDED:
		   return newres;
	   default: 
		   return null;
	   }
   }

   private ExtendExtensionsResult enlargeExtensions(List<Object> objects, List<FieldExtensions> newExtensions, 
		   FieldExtensions globalExt) { 
	   assert newExtensions != null: "Trying to enlarge global extensions with null list of extensions";
	   assert objects.size() == newExtensions.size(): "There should be exactly one extension for each object";
	   
	   ExtendExtensionsResult res = ExtendExtensionsResult.NOT_EXTENDED;
	   int index = sequence.size()-1;
	   //Statement stmt = sequence.getStatement(index);

	   List<FieldExtensions> enlargingExt = new LinkedList<>();
	   for (int i = 0; i < objects.size(); i++) {
		   FieldExtensions ext = newExtensions.get(i);
		   Object obj = objects.get(i);
		   
		   if (obj == null) continue;
		   
		   boolean enlargesExt = false;
		   if (AbstractGenerator.fbg_precise_extension_detection) {
			   if (!globalExt.containsAll(ext)) {
				   enlargingExt.add(ext);
				   enlargesExt = true;
			   }
		   }
		   else 
			   if (globalExt.addAll(ext))
				   enlargesExt = true;

		   if (enlargesExt) {
			   if (!isPrimitive(obj)) {
				   res = ExtendExtensionsResult.EXTENDED;
				   sequence.addActiveVar(index, i);
				    if (CanonicalizerLog.isLoggingOn()) {
				    	CanonicalizerLog.logLine("----------");
				    	CanonicalizerLog.logLine("Object enlarges reference extensions. Object extensions:");
				    	CanonicalizerLog.logLine(ext.toString());
				    	CanonicalizerLog.logLine("----------");
				    }
			   }
			   else {
				   res = lift(res, ExtendExtensionsResult.EXTENDED_PRIMITIVE);
				   if (CanonicalizerLog.isLoggingOn()) {
					   CanonicalizerLog.logLine("----------");
					   CanonicalizerLog.logLine("Value enlarges primitive extensions. Value extensions:");
					   CanonicalizerLog.logLine(ext.toString());
					   CanonicalizerLog.logLine("----------");
				   }
			   }
		   }
		   else {
			   if (CanonicalizerLog.isLoggingOn()) {
				   CanonicalizerLog.logLine("----------");
				   CanonicalizerLog.logLine("Extensions not enlarged.");
				   CanonicalizerLog.logLine("----------");
			   }
		   }
	   }

	   if (CanonicalizerLog.isLoggingOn() && res != ExtendExtensionsResult.NOT_EXTENDED) {
		   CanonicalizerLog.logLine("----------");
		   CanonicalizerLog.logLine("Former global extensions:");
		   CanonicalizerLog.logLine(globalExt.toString());
		   CanonicalizerLog.logLine("----------");
	   }

	   for (FieldExtensions ext: enlargingExt)
		   globalExt.addAll(ext);
	   
	   if (CanonicalizerLog.isLoggingOn() && res != ExtendExtensionsResult.NOT_EXTENDED) {
		   CanonicalizerLog.logLine("----------");
		   CanonicalizerLog.logLine("New global extensions:");
		   CanonicalizerLog.logLine(globalExt.toString());
		   CanonicalizerLog.logLine("----------");
	   }

	   return res;
   }

   private void executeFB(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException, 
		   HeapCanonicalizer canonicalizer, FieldExtensions globalExt) {

	    visitor.initialize(this);

	    // reset execution result values
	    hasNullInput = false;
	    executionResults.theList.clear();
	    for (int i = 0; i < sequence.size(); i++) {
	      executionResults.theList.add(NotExecuted.create());
	    }
	    List<FieldExtensions> lastStmtExtBeforeExecution = null;
	    List<FieldExtensions> lastStmtExtAfterExecution = null;

	    for (int i = 0; i < this.sequence.size(); i++) {
	      // Find and collect the input values to i-th statement.
	      List<Variable> inputs = sequence.getInputs(i);
	      Object[] inputVariables;
	      inputVariables = getRuntimeInputs(executionResults.theList, inputs);
	      TypedOperation op = null;

	      if (i == sequence.size()-1 && AbstractGenerator.fbg_observer_detection) {
	    	  op = sequence.getStatement(i).getOperation();
	    	  logOperationType(op);
	    	  if (sequence.size() > 1 && (op.notExecuted() || op.isObserver())) {
	    		  List<Object> objs = getObjectsForStatement(i, null, inputVariables);
	    		  lastStmtExtBeforeExecution = createExtensionsForAllObjects(objs, canonicalizer);
	    	  }
	      }

	      visitor.visitBeforeStatement(this, i);
	      executeStatement(sequence, executionResults.theList, i, inputVariables);
	      // make sure statement executed
	      ExecutionOutcome statementResult = getResult(i);
	      if (statementResult instanceof NotExecuted) {
	        throw new Error("Unexecuted statement in sequence: " + this.toString());
	      }
	      // make sure no exception before final statement of sequence
	      if ((statementResult instanceof ExceptionalExecution) && i < sequence.size() - 1) {
	        if (ignoreException) {
	          // this preserves previous behavior, which was simply to return if
	          // exception occurred
	          break;
	        } else {
	          String msg =
	              "Encountered exception before final statement of error-revealing test (statement "
	                  + i
	                  + "): ";
	          throw new Error(
	              msg + ((ExceptionalExecution) statementResult).getException().getMessage());
	        }
	      }
	      
	      if (i == sequence.size()-1 && statementResult instanceof NormalExecution) {
	    	  Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
	    	  List<Object> objs = getObjectsForStatement(i, retVal, inputVariables);
	    	  computeLastStmtNonPrimIndexes(objs);
	    	  // TODO: Optimize by not creating extesions for observers?
	    	  lastStmtExtAfterExecution = createExtensionsForAllObjects(objs, canonicalizer);
	    	  enlargesExtensions = enlargeExtensions(objs, lastStmtExtAfterExecution, globalExt);
	    	  
	    	  if (AbstractGenerator.fbg_observer_detection) {
	    		  // Update operation types according to their behavior w.r.t. extensions for observer detection.
	    		  if (enlargesExtensions == ExtendExtensionsResult.EXTENDED) 
	    			  op.setModifier();
	    		  // FIXME: Leave final observers for the second phase only?
	    		  if (!op.isModifier()/* && !op.isFinalObserver()*/) {
	    			  // Check if we have to set op to observer or a modifier due to the current execution 
	    			  if (sequence.size() == 1) {
   						  // There is an object that is not primitive
	    				  if (!lastStmtNonPrimIndexes.isEmpty()) {
	    					  for (Integer j: lastStmtNonPrimIndexes) {
	    						  op.setModifier(j);
	    						  break;
	    					  }
	    				  }
	    			  }
	    			  else { //sequence.size() > 1 
	    				  assert lastStmtExtBeforeExecution.size() == lastStmtExtBeforeExecution.size(): 
	    					  "Extensions before and after must be of the same size";
	    				  for (int j = 0; j < lastStmtExtAfterExecution.size(); j++) {
	    					  assert !(lastStmtExtBeforeExecution.get(j) != null && lastStmtExtAfterExecution.get(j) == null):
	    						  "Extensions before execution are not null but became null after";
	    					  if (!lastStmtNonPrimIndexes.contains(j))
	    						  // Object at index j is primitive
	    						  continue;
	    					  else {
	    						  if (!lastStmtExtAfterExecution.get(j).equals(lastStmtExtBeforeExecution.get(j))) {
	    							  op.setModifier(j);
	    							  break;
	    						  }
	    					  }
	    				  }
	    			  } 
	    		  }
	    		  // If it's not a modifier, mark the current action as an observer or final observer
	    		  if (!op.isModifier()) {
	    			  // FIXME: Leave final observers for the second phase only?
	    			  /*
	    			  if (op.observerTimesExecuted > AbstractGenerator.fbg_final_observer_after) 
	    				  op.setFinalObserver(); 
	    			  else 
	    			  */
	    			  if (!op.isObserver())
	    				  op.setObserver();
	    			  op.observerTimesExecuted++;
	    		  } 
	    		  assert !op.notExecuted(): "Action was executed but flagged as not executed.";
	    	  }

	      	}

	      visitor.visitAfterStatement(this, i);
	    }
	    visitor.visitAfterSequence(this);
	   	checks = gen.visit(this);
	  }
  
  
  public TypedOperation getLastStmtOperation() {
	  return sequence.getStatement(sequence.size() - 1).getOperation();
  } 

  public void clearExtensions() {
	 lastStmtFormerExt = null;
	 lastStmtNextExt = null;
  }
  
   public void clearExecutionResults() {
	 executionResults.theList.clear();
	 for (int i = 0; i < sequence.size(); i++) {
		 executionResults.theList.add(NotExecuted.create());
	 }
  }
  
  
  public void toFile(String filename) throws IOException {
	try (Writer writer = new BufferedWriter(new FileWriter(filename))) {
		writer.write(toString());
	}	
  }
  
  /*
  public void fastFieldBasedExecute(ExecutionVisitor visitor, TestCheckGenerator gen, ForwardGenerator generator, HeapCanonizer canonizer) {
	  fastFieldBasedExecute(visitor, gen, true, generator, canonizer);
  }

  
  private void fastFieldBasedExecute(ExecutionVisitor visitor, TestCheckGenerator gen, boolean ignoreException, ForwardGenerator generator, HeapCanonizer canonizer) {

	extendedExtensions = false;
	canonizationError = false;
	seqnum++;
  
    visitor.initialize(this);

    // reset execution result values
    hasNullInput = false;
    executionResults.theList.clear();
    for (int i = 0; i < sequence.size(); i++) {
      executionResults.theList.add(NotExecuted.create());
    }

    normalExecution = true;
    int lastStmtIndex = this.sequence.size()-1;
    Object[] inputVariables = null;
    ExecutionOutcome statementResult = null;
    for (int i = 0; i < this.sequence.size(); i++) {

      // Find and collect the input values to i-th statement.
      List<Variable> inputs = sequence.getInputs(i);

      inputVariables = getRuntimeInputs(executionResults.theList, inputs);

      visitor.visitBeforeStatement(this, i);

      executeStatement(sequence, executionResults.theList, i, inputVariables);

      // make sure statement executed
      statementResult = getResult(i);
      
      if (statementResult instanceof NotExecuted) {
    	  normalExecution = false;
          throw new Error("Unexecuted statement in sequence: " + this.toString());
      }
      // make sure no exception before final statement of sequence
      if ((statementResult instanceof ExceptionalExecution) /*&& i < sequence.size() - 1*//*) {
    	  normalExecution = false;
          if (ignoreException) {
            // this preserves previous behavior, which was simply to return if
            // exception occurred
            break;
          } else {
            String msg =
                "Encountered exception before final statement of error-revealing test (statement "
                    + i
                    + "): ";
            throw new Error(
                msg + ((ExceptionalExecution) statementResult).getException().getMessage());
          }
      }
    
      visitor.visitAfterStatement(this, i);
    }
    
    if (normalExecution) {
        // PABLO: Fast field based generation: For efficiency, only consider the last statement 
        // for attempting to enlarge field extensions
    	if (enlargeExtensions(lastStmtIndex, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer))
   			extendedExtensions = true;
    	
		// Update last method's weight
		if (!canonizationError && AbstractGenerator.field_based_weighted_selection) {
		    Statement stmt = sequence.getStatement(lastStmtIndex);		
			if (extendedExtensions)
				increaseOpearationWeight(stmt, generator);
			else
				decreaseOpearationWeight(stmt, generator);
		}
    }
	
    visitor.visitAfterSequence(this);

    checks = gen.visit(this);
  }*/
  

  
   // Returns a list of objects for each of the variables of the sequence (including the return value), 
   // that result from the execution of the whole sequence.
   public List<Object> getObjectsAfterExecution() {
	   
	   List<Object> objectsAfterExecution = new ArrayList<>();

	   int lastStmtIndex = this.sequence.size()-1;
	   List<Variable> inputVars = sequence.getInputs(lastStmtIndex);
	   Object[] outputObjs = getRuntimeInputs(executionResults.theList, inputVars);
	   ExecutionOutcome statementResult = getResult(lastStmtIndex);
	   Object runtimeResult = ((NormalExecution)statementResult).getRuntimeValue();

	   Statement stmt = sequence.getStatement(lastStmtIndex);
	   if (!stmt.getOutputType().isVoid()) {
		   objectsAfterExecution.add(runtimeResult);
	   }
	   for (int j=0; j<outputObjs.length; j++) {
		   objectsAfterExecution.add(outputObjs[j]);
	   }

	   return objectsAfterExecution;
   }
   
   public List<Tuple<CanonizerClass, FieldExtensionsIndexes>> canonizeObjectsAfterExecution(HeapCanonizerRuntimeEfficient canonizer) {
	   return canonizeObjectsAfterExecution(canonizer, true);
   }
   
   public List<Tuple<CanonizerClass, FieldExtensionsIndexes>> canonizeObjectsAfterExecution(HeapCanonizerRuntimeEfficient canonizer, boolean includePrimitive) {

	   List<Tuple<CanonizerClass, FieldExtensionsIndexes>> classesAndExtensions = new ArrayList<>();

	   for (Object o: getObjectsAfterExecution()) {
		   if (o == null || (!includePrimitive && isObjectPrimtive(o))) {
			   classesAndExtensions.add(null);
		   }
		   else {
			   FieldExtensionsIndexes ext = new FieldExtensionsIndexesMap(canonizer.store);
			   if (canonizer.traverseBreadthFirstAndEnlargeExtensions(o, ext) == ExtendExtensionsResult.LIMITS_EXCEEDED)
				   return null; 
			   classesAndExtensions.add(new Tuple<>(canonizer.store.canonizeClass(o.getClass()), ext));
		   }
	   }

	   return classesAndExtensions; 
   }
   
   
  
  private boolean isObjectPrimtive(Object o) {
	  Class<?> objectClass = o.getClass();
	  return NonreceiverTerm.isNonreceiverType(objectClass) || 
			  objectClass.equals(Object.class) ||
			  objectClass.equals(BigInteger.class) || 
			  objectClass.equals(BigDecimal.class);
  }
  

   private List<FieldExtensionsIndexes> createExtensionsForAllObjects(int i, Object statementResult, Object[] inputVariables, HeapCanonizerRuntimeEfficient canonizer, boolean canonize) throws CanonizationErrorException {

	  try {
		  Statement stmt = sequence.getStatement(i);

		  List<Object> parameters = new ArrayList<>();

		  if (!stmt.getOutputType().isVoid()) {
			  parameters.add(statementResult);
		  }

		  for (int j=0; j<inputVariables.length; j++) {
			  parameters.add(inputVariables[j]);
		  }

		  List<FieldExtensionsIndexes> extensions = new ArrayList<>();
			  
		  for (Object o: parameters) {
			  if (o != null && !isObjectPrimtive(o)) {
				  FieldExtensionsIndexes ext = new FieldExtensionsIndexesMap(canonizer.store);
				  if (canonize)
					  if (canonizer.traverseBreadthFirstAndEnlargeExtensions(o, ext) == ExtendExtensionsResult.LIMITS_EXCEEDED)
						  return null; 

				  extensions.add(ext);
				  

			  }
			  else 
				  extensions.add(null);
		  }
		  
		 return extensions; 
		  
	  }	catch (Exception e) {
		  e.printStackTrace();
		  throw new CanonizationErrorException("ERROR: Exception during heap canonization");
	  }
	  
   }
   
   public boolean lastStatementVarIsPrimitive(int varIndex) {
	   return lastStmtNextExt.get(varIndex) == null;
   }

   private List<Boolean> lastStmtActiveVars;

   public List<Boolean> getLastStmtActiveVars() { 
	   return lastStmtActiveVars;
   }
   
   public void createLastStmtActiveVars() {
	   lastStmtActiveVars = new ArrayList<>();
	   for (FieldExtensionsIndexes fe: lastStmtNextExt)
		   lastStmtActiveVars.add(fe != null);
   }
   
   
   public void tryToEnlargeExtensions(HeapCanonizerRuntimeEfficient canonizer) throws CanonizationErrorException {
	   if (lastStmtNextExt == null) {
		   enlargesExtensions = ExtendExtensionsResult.LIMITS_EXCEEDED;
		   return;
	   }
	   
	   enlargesExtensions = ExtendExtensionsResult.NOT_EXTENDED;
	   int index = sequence.size()-1;
	   Statement stmt = sequence.getStatement(index);
	   if (AbstractGenerator.fbg_precise_extension_detection) {

		   int varIndex = 0;
		   for (FieldExtensionsIndexes ext: lastStmtNextExt) {
			   if (ext != null && canonizer.getExtensions().testEnlarges(ext)) {
				   if (FieldBasedGenLog.isLoggingOn())
					   FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
							   + stmt.toString() + " (index " + index + ")");

				   sequence.addActiveVar(index, varIndex);
				   enlargesExtensions = ExtendExtensionsResult.EXTENDED;
			   }
			   
			   varIndex++;
		   }

		   if (sequence.getActiveVars(index) != null)
			   for (Integer i: sequence.getActiveVars(index))
				   canonizer.getExtensions().addAllPairs(lastStmtNextExt.get(i));

	   }
	   else {

		   int varIndex = 0;
		   for (FieldExtensionsIndexes ext: lastStmtNextExt) {
			   if (ext != null && canonizer.getExtensions().addAllPairs(ext)) {
				   if (FieldBasedGenLog.isLoggingOn())
					   FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
							   + stmt.toString() + " (index " + index + ")");

				   enlargesExtensions = ExtendExtensionsResult.EXTENDED;
			   }

			   varIndex++;
		   }

	   }
	   
	   if (enlargesExtensions == ExtendExtensionsResult.EXTENDED && getLastStmtOperation().isModifier())
		   getLastStmtOperation().timesExecutedInExtendingModifiers++;

   }
   
   
   public void enlargeExtensions(HeapCanonicalizer candVectCanonicalizer) {
	   /*
	   if (lastStmtNextExt == null) {
		   enlargesExtensions = ExtendedExtensionsResult.LIMITS_EXCEEDED;
		   return;
	   }
	   
	   enlargesExtensions = ExtendedExtensionsResult.NOT_EXTENDED;
	   int index = sequence.size() -1;
	   Statement stmt = sequence.getStatement(index);
	   if (AbstractGenerator.fbg_precise_extension_detection) {

		   int varIndex = 0;
		   for (FieldExtensionsIndexes ext: lastStmtNextExt) {
			   if (ext != null && canonizer.getExtensions().testEnlarges(ext)) {
				   if (FieldBasedGenLog.isLoggingOn())
					   FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
							   + stmt.toString() + " (index " + index + ")");

				   sequence.addActiveVar(index, varIndex);
				   enlargesExtensions = ExtendedExtensionsResult.EXTENDED;
			   }
			   
			   varIndex++;
		   }

		   if (sequence.getActiveVars(index) != null)
			   for (Integer i: sequence.getActiveVars(index))
				   canonizer.getExtensions().addAllPairs(lastStmtNextExt.get(i));

	   }
	   else {

		   int varIndex = 0;
		   for (FieldExtensionsIndexes ext: lastStmtNextExt) {
			   if (ext != null && canonizer.getExtensions().addAllPairs(ext)) {
				   if (FieldBasedGenLog.isLoggingOn())
					   FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
							   + stmt.toString() + " (index " + index + ")");

				   enlargesExtensions = ExtendedExtensionsResult.EXTENDED;
			   }

			   varIndex++;
		   }

	   }
	   
	   if (enlargesExtensions == ExtendedExtensionsResult.EXTENDED && getLastStmtOperation().isModifier())
		   getLastStmtOperation().timesExecutedInExtendingModifiers++;
		*/
   }
   
   
   
  private ExtendExtensionsResult enlargeExtensionsLimits(int i, Object statementResult, Object[] inputVariables, HeapCanonizerRuntimeEfficient canonizer) throws CanonizationErrorException {

	  ExtendExtensionsResult res = ExtendExtensionsResult.NOT_EXTENDED;
	  List<FieldExtensionsIndexes> extensions = createExtensionsForAllObjects(i, statementResult, inputVariables, canonizer, true);
				  
	  Statement stmt = sequence.getStatement(i);
	  if (AbstractGenerator.fbg_precise_extension_detection) {
			  
		  int varIndex = 0;
		  for (FieldExtensionsIndexes ext: extensions) {
			  if (ext != null && canonizer.getExtensions().testEnlarges(ext)) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
							  + stmt.toString() + " (index " + i + ")");

				  sequence.addActiveVar(i, varIndex);
				  res = ExtendExtensionsResult.EXTENDED;
			  }
			  varIndex++;
		  }
		  
		  if (sequence.getActiveVars(i) != null) {
			  for (Integer extIndex: sequence.getActiveVars(i)) 
				 canonizer.getExtensions().addAllPairs(extensions.get(extIndex));
		  }
		  
	  }
	  else {
	  
		  int varIndex = 0;
		  for (FieldExtensionsIndexes ext: extensions) {
			  if (ext != null && canonizer.getExtensions().addAllPairs(ext)) {
				  if (FieldBasedGenLog.isLoggingOn())
					  FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
							  + stmt.toString() + " (index " + i + ")");

				  res = ExtendExtensionsResult.EXTENDED;
			  }
			  varIndex++;
		  }

	  }
		  
	  return res;
  }
  

  
  private boolean enlargeExtensionsPrecise(int i, Object statementResult, Object[] inputVariables, HeapCanonizerRuntimeEfficient canonizer) throws CanonizationErrorException {
	  boolean extendedExtensions = false;	
/*	  
	  int oldSize = canonizer.getExtensions().size();
	  try {
		  List<Object> extendingObjs = new LinkedList<>();

		  Statement stmt = sequence.getStatement(i);
		  int varIndex = 0;
		  if (!stmt.getOutputType().isVoid()) {
			  Object obj = statementResult;
			  
			  if (obj != null && !CanonicalRepresentation.isObjectPrimitive(obj)) {
				  // test if the result enlarges the extensions
			  	  if (canonizer.canonizeAndEnlargeExtensions(obj, true)) {
		           	  if (FieldBasedGenLog.isLoggingOn()) {
	        	    		FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
	        	    				+ stmt.toString() + " (index " + i + ")");
		           	  }
		           	  
		           	  extendingObjs.add(obj);
			  		  sequence.addActiveVar(i, varIndex);
			  		  extendedExtensions = true;
			  	  }
			  }
			  varIndex++;
		  }
	
		  // Cover the field values belonging to all the current method's parameters
		  if (inputVariables.length > 0) {
				for (int j=0; j<inputVariables.length; j++) {
					if (inputVariables[j] != null && !CanonicalRepresentation.isObjectPrimitive(inputVariables[j])) {
						// test if the current parameter enlarges the extensions
	        	    	if (canonizer.canonizeAndEnlargeExtensions(inputVariables[j], true)) {
	        	    		if (FieldBasedGenLog.isLoggingOn()) {
	        	    			FieldBasedGenLog.logLine("> Enlarged extensions at variable " + varIndex + " of " 
	        	    					+ stmt.toString() + " (index " + i + ")");
	        	    		}

	        	    		extendingObjs.add(inputVariables[j]);
	        	    		sequence.addActiveVar(i, varIndex);		        	    		
	        	    		extendedExtensions = true;
	        	    	}
					}
					varIndex++;
				}
		  }
		  
		 
		  int intermediateSize = canonizer.getExtensions().size();
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine("> Old size: " + oldSize + ", intermediate size: " + intermediateSize);
		  
		  if (oldSize != intermediateSize)
			  throw new RuntimeException("FATAL ERROR: Added things to the extensions but it shouldn't have");

		  
		    
		  boolean extendedAux = false;
		  for (Object obj: extendingObjs) {
			  extendedAux |= canonizer.canonizeAndEnlargeExtensions(obj);
		  } 
		  
		  if (extendingObjs.size() >= 1 && !extendedAux) {
			  System.out.println("ERROR DURING CANONIZATION: Should have enlarged extensions but didn't");
			  throw new RuntimeException("ERROR DURING CANONIZATION: Should have enlarged extensions but didn't"); 
		  }
		  
	  }	catch (Exception e) {
		  e.printStackTrace();
		  throw new CanonizationErrorException("ERROR DURING CANONIZATION: Should have enlarged extensions but didn't");
	  }
	  
	  */
	  return extendedExtensions;
  }
  
  
  private void increaseOpearationWeight(Statement stmt, ForwardGenerator generator) {
	  // FIXME: I don't know when a constructor is from an array type or not.
	  // Array constructors return a null pointer exception when I try to increase
	  // their weight because the corresponding operation does not exist in generator.
	  // if (/*stmt.isConstructorCall() ||*/ stmt.isMethodCall()) {
	  if (stmt.isMethodCall() && !stmt.isConstructorCall() && !stmt.isPrimitiveInitialization())  {
		  TypedOperation op = stmt.getOperation();

		  Integer weightBefore = generator.getWeight(op);
		  if (weightBefore == null) {
			  if (FieldBasedGenLog.isLoggingOn()) {
			  	  FieldBasedGenLog.logLine("> Unknown operation: Not increasing " + op.toString() + " weight");
			  }

			  return;
		  }

		  generator.increaseWeight(stmt.getOperation());

		  if (FieldBasedGenLog.isLoggingOn()) {
		  	  FieldBasedGenLog.logLine("> Increased " + op.toString() + " weight to " + generator.getWeight(op) + " (was " + weightBefore + ")");
		  }
  	  }
  }

  private void decreaseOpearationWeight(Statement stmt, ForwardGenerator generator) {
	  // FIXME: I don't know when a constructor is from an array type or not.
	  // Array constructors return a null pointer exception when I try to increase
	  // their weight because the corresponding operation does not exist in generator.
	  if (stmt.isMethodCall() && !stmt.isConstructorCall() && !stmt.isPrimitiveInitialization())  {
		  TypedOperation op = stmt.getOperation();

		  Integer weightBefore = generator.getWeight(op);
		  if (weightBefore == null) {
			  if (FieldBasedGenLog.isLoggingOn()) {
			  	  FieldBasedGenLog.logLine("> Unknown operation: Not decreasing " + op.toString() + " weight");
			  }

			  return;
		  }
		  
		  generator.decreaseWeight(stmt.getOperation());

		  if (FieldBasedGenLog.isLoggingOn()) {
		  	  FieldBasedGenLog.logLine("> Decreased " + op.toString() + " weight to " + generator.getWeight(op) + " (was " + weightBefore + ")");
		  }
	  }
  }

  
  
  public ExtendExtensionsResult enlargeExtensionsMin(HeapCanonizerRuntimeEfficient canonizer, TestCheckGenerator checkgen, ForwardGenerator generator) throws CanonizationErrorException {
	enlargesExtensions = ExtendExtensionsResult.NOT_EXTENDED;
/*
	//	seqnum++;
	
	// FIXME: Figure out how to do this more efficiently.
    // Now that we know that the execution has completed normally reset execution result values
	// and attempt to enlarge the extensions. Otherwise we would add field values to the extensions
	// based on objects already modified by all the executed statements.
    hasNullInput = false;
    
    
    // TODO PABLO: Maybe we don't delete the checks to detect flaky tests as soon as possible?
    // checks = null;
    executionResults.theList.clear();
    
    for (int i = 0; i < sequence.size(); i++) {
      executionResults.theList.add(NotExecuted.create());
    }
    
	for (int i = 0; i < this.sequence.size(); i++) {
		
	  List<Variable> inputs = sequence.getInputs(i);
	  Object[] inputVariables;
	  inputVariables = getRuntimeInputs(executionResults.theList, inputs);

	  executeStatement(sequence, executionResults.theList, i, inputVariables);

	  // make sure statement executed
	  ExecutionOutcome statementResult = getResult(i);

	  if (!(statementResult instanceof NormalExecution)) {
		System.out.println("ERROR: augmenting field extensions using exceptional behavior. This is probably a flaky test");
		throw new CanonizationErrorException("ERROR: augmenting field extensions using exceptional behavior. This is probably a flaky test");
  	  }
    		
	  if (AbstractGenerator.field_based_gen == FieldBasedGenType.MIN) {
		  if (enlargeExtensions(i, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer))
			  enlargesExtensions = true;
	  }
  	  else {
 		  if (enlargeExtensionsPrecise(i, ((NormalExecution)statementResult).getRuntimeValue(), inputVariables, canonizer))
			  enlargesExtensions = true;
  	  }
	  
	  if (enlargesExtensions && i < this.sequence.size() -1) {
		  System.out.println("Importante: Aca funciona la idea de minimizacion!!!!");
		  if (FieldBasedGenLog.isLoggingOn())
			  FieldBasedGenLog.logLine("Importante: Aca funciona la idea de minimizacion!!!!");
		  
	  }
	  
    }
    	
    if (AbstractGenerator.field_based_gen_weighted_selection) {
    	// Figure out how to update weights
    	Set<Integer> activeIndexes = sequence.getActiveStatements();
    	for (int i = 0; i < this.sequence.size(); i++) {
    		Statement stmt = sequence.getStatement(i);
    		if (activeIndexes.contains(i))
    			increaseOpearationWeight(stmt, generator);
    		else
    			decreaseOpearationWeight(stmt, generator);
    	}
    }
    
    //checks = checkgen.visit(this);
    */
    return enlargesExtensions;
  }
  
  
  private Object[] getRuntimeInputs(List<ExecutionOutcome> outcome, List<Variable> inputs) {
    Object[] ros = getRuntimeValuesForVars(inputs, outcome);
    for (Object ro : ros) {
      if (ro == null) {
        this.hasNullInput = true;
      }
    }
    return ros;
  }

  /**
   * Returns the values for the given variables in the {@link Execution} object.
   * The variables are {@link Variable} objects in the {@link Sequence} of this
   * {@link ExecutableSequence} object.
   *
   * @param vars
   *          a list of {@link Variable} objects.
   * @param execution
   *          the object representing outcome of executing this sequence.
   * @return array of values corresponding to variables
   */
  public static Object[] getRuntimeValuesForVars(List<Variable> vars, Execution execution) {
    return getRuntimeValuesForVars(vars, execution.theList);
  }

  private static Object[] getRuntimeValuesForVars(
      List<Variable> vars, List<ExecutionOutcome> execution) {
    Object[] runtimeObjects = new Object[vars.size()];
    for (int j = 0; j < runtimeObjects.length; j++) {
      int creatingStatementIdx = vars.get(j).getDeclIndex();
      assert execution.get(creatingStatementIdx) instanceof NormalExecution
          : execution.get(creatingStatementIdx).getClass();
      NormalExecution ne = (NormalExecution) execution.get(creatingStatementIdx);
      runtimeObjects[j] = ne.getRuntimeValue();
    }
    return runtimeObjects;
  }

  // Execute the index-th statement in the sequence.
  // Precondition: this method has been invoked on 0..index-1.
  private static void executeStatement(
      Sequence s, List<ExecutionOutcome> outcome, int index, Object[] inputVariables) {
    Statement statement = s.getStatement(index);

    // Capture any output Synchronize with ProgressDisplay so that
    // we don't capture its output as well.
    synchronized (ProgressDisplay.print_synchro) {
      PrintStream orig_out = System.out;
      PrintStream orig_err = System.err;
      if (GenInputsAbstract.capture_output) {
        System.out.flush();
        System.err.flush();
        System.setOut(ps_output_buffer);
        System.setErr(ps_output_buffer);
      }

      // assert ((statement.isMethodCall() && !statement.isStatic()) ?
      // inputVariables[0] != null : true);

      ExecutionOutcome r = statement.execute(inputVariables, Globals.blackHole);
      assert r != null;
      if (GenInputsAbstract.capture_output) {
        System.setOut(orig_out);
        System.setErr(orig_err);
        r.set_output(output_buffer.toString());
        output_buffer.reset();
      }
      outcome.set(index, r);
    }
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * The result of executing the i-th element of the sequence.
   *
   * @param index the statement index
   * @return the outcome of the statement at index
   */
  public ExecutionOutcome getResult(int index) {
    sequence.checkIndex(index);
    return executionResults.get(index);
  }

  /**
   * Return the set of test checks for the most recent execution.
   *
   * @return the {@code TestChecks} generated from the most recent execution
   */
  public TestChecks getChecks() {
    return checks;
  }

  private Object getValue(int index) {
    ExecutionOutcome result = getResult(index);
    if (result instanceof NormalExecution) {
      return ((NormalExecution) result).getRuntimeValue();
    }
    throw new Error("Abnormal execution in sequence: " + this);
  }

  /**
   * Returns the list of (reference type) values created and used by the last statement
   * of this sequence.
   *
   * @return the list of values created and used by the last statement of this sequence
   */
  public List<ReferenceValue> getLastStatementValues() {
    Set<ReferenceValue> values = new LinkedHashSet<>();

    Object outputValue = getValue(sequence.size() - 1);
    if (outputValue != null) {
      Variable outputVariable = sequence.getLastVariable();

      Type outputType = outputVariable.getType();

      if (outputType.isReferenceType() && !outputType.isString()) {
        ReferenceValue value = new ReferenceValue((ReferenceType) outputType, outputValue);
        values.add(value);
        variableMap.put(outputValue, outputVariable);
      }
    }

    for (Variable inputVariable : sequence.getInputs(sequence.size() - 1)) {
      Object inputValue = getValue(inputVariable.index);
      if (inputValue != null) {
        Type inputType = inputVariable.getType();
        if (inputType.isReferenceType() && !inputType.isString()) {
          values.add(new ReferenceValue((ReferenceType) inputType, inputValue));
          variableMap.put(inputValue, inputVariable);
        }
      }
    }

    return new ArrayList<>(values);
  }

  /**
   * Returns the list of input reference type values used to compute the
   * input values of the last statement.
   *
   * @return the list of input values used to compute values in last statement
   */
  public List<ReferenceValue> getInputValues() {
    Set<Integer> skipSet = new HashSet<>();
    for (Variable inputVariable : sequence.getInputs(sequence.size() - 1)) {
      skipSet.add(inputVariable.index);
    }

    Set<ReferenceValue> values = new HashSet<>();
    for (int i = 0; i < sequence.size() - 1; i++) {
      if (!skipSet.contains(i)) {
        Object value = getValue(i);
        if (value != null) {
          Variable variable = sequence.getVariable(i);
          Type type = variable.getType();
          if (type.isReferenceType() && !type.isString()) {
            values.add(new ReferenceValue((ReferenceType) type, value));
            variableMap.put(value, variable);
          }
        }
      }
    }
    return new ArrayList<>(values);
  }

  /**
   * Returns the set of variables that have the given value in the outcome of executing this
   * sequence.
   *
   * @param value  the value
   * @return the set of variables that have the given value
   */
  public List<Variable> getVariables(Object value) {
    Set<Variable> variables = variableMap.get(value);
    if (variables == null) {
      return null;
    } else {
      return new ArrayList<>(variables);
    }
  }

  /**
   * This method is typically used by ExecutionVisitors.
   *
   * @param i  the statement index to test for normal execution
   * @return true if execution of the i-th statement terminated normally
   */
  private boolean isNormalExecution(int i) {
    sequence.checkIndex(i);
    return getResult(i) instanceof NormalExecution;
  }

  public int getNonNormalExecutionIndex() {
    for (int i = 0; i < this.sequence.size(); i++) {
      if (!isNormalExecution(i)) return i;
    }
    return -1;
  }

  public boolean isNormalExecution() {
    return getNonNormalExecutionIndex() == -1;
  }

  /**
   * @param exceptionClass  the exception thrown
   * @return the index in the sequence at which an exception of the given class
   *         (or a class compatible with it) was thrown. If no such exception,
   *         returns -1.
   */
  private int getExceptionIndex(Class<?> exceptionClass) {
    if (exceptionClass == null) {
      throw new IllegalArgumentException("exceptionClass<?> cannot be null");
    }
    for (int i = 0; i < this.sequence.size(); i++)
      if ((getResult(i) instanceof ExceptionalExecution)) {
        ExceptionalExecution e = (ExceptionalExecution) getResult(i);
        if (exceptionClass.isAssignableFrom(e.getException().getClass())) return i;
      }
    return -1;
  }

  /**
   * @param exceptionClass  the exception class
   * @return true if an exception of the given class (or a class compatible with
   *         it) has been thrown during this sequence's execution
   */
  public boolean throwsException(Class<?> exceptionClass) {
    return getExceptionIndex(exceptionClass) >= 0;
  }

  /**
   * Returns whether the sequence contains a non-executed statement. That
   * happens if some statement before the last one throws an exception.
   *
   * @return true if this sequence has non-executed statements, false otherwise
   */
  public boolean hasNonExecutedStatements() {
    return getNonExecutedIndex() != -1;
  }

  /**
   * Returns the index i for a non-executed statement, or -1 if there is no such
   * index. Note that a statement is considered executed even if it throws an
   * exception.
   *
   * @return the index of a non-executed statement in this sequence
   */
  public int getNonExecutedIndex() {
    // Starting from the end of the sequence is always faster to find
    // non-executed statements.
    for (int i = this.sequence.size() - 1; i >= 0; i--)
      if (getResult(i) instanceof NotExecuted) return i;
    return -1;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sequence.hashCode(), checks.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExecutableSequence)) return false;
    ExecutableSequence that = (ExecutableSequence) obj;
    if (!this.sequence.equals(that.sequence)) return false;
    if (this.checks == null) return (that.checks == null);
    return this.checks.equals(that.checks);
  }

  /**
   * Indicates whether the executed sequence has any null input values.
   *
   * @return  true if there is a null input value in this sequence, false otherwise
   */
  public boolean hasNullInput() {
    return hasNullInput;
  }

  /**
   * Indicate whether checks are failing or passing.
   *
   * @return true if checks are all failing, or false if all passing
   */
  public boolean hasFailure() {
    return checks != null && checks.hasErrorBehavior();
  }

  /**
   * Indicate whether there are any invalid checks.
   *
   * @return true if the test checks have been set and are for invalid behavior,
   *         false otherwise
   */
  public boolean hasInvalidBehavior() {
    return checks != null && checks.hasInvalidBehavior();
  }

  /**
   * Adds a covered class to the most recent execution results of this sequence.
   *
   * @param c
   *          the class covered by the execution of this sequence
   */
  public void addCoveredClass(Class<?> c) {
    executionResults.addCoveredClass(c);
  }

  /**
   * Indicates whether the given class is covered by the most recent execution
   * of this sequence.
   *
   * @param c
   *          the class to be covered
   * @return true if the class is covered by the sequence, false otherwise
   */
  public boolean coversClass(Class<?> c) {
    return executionResults.getCoveredClasses().contains(c);
  }




}
