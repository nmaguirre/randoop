package randoop.fieldextensions;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.operation.TypedOperation;

public class OperationManager implements IOperationManager {
	
	protected int maxExecsToObs;
	protected Map<String, OpState> opStates = new LinkedHashMap<>();
	protected Map<String, Integer> numExecs = new LinkedHashMap<>();
	protected Map<String, Integer> modExecs = new LinkedHashMap<>();

	public OperationManager() {
		this.maxExecsToObs = Integer.MAX_VALUE;
	}
	
	public OperationManager(int maxExecsToObs) {
		this.maxExecsToObs = maxExecsToObs;
	}
	
	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#executed(randoop.operation.TypedOperation)
	 */
	@Override
	public void executed(TypedOperation op) {
		String opName = op.toJavaString();
		if (opStates.get(opName) == null) 
			opStates.put(opName, OpState.EXECUTED);
		
		countExecution(opName);
	}

	private void countExecution(String opName) {
		if (numExecs.get(opName) == null) 
			numExecs.put(opName, 0);
		if (modExecs.get(opName) == null) 
			modExecs.put(opName, 0);

		int currExecs = numExecs.get(opName) + 1;
		numExecs.put(opName, currExecs);

		if (opStates.get(opName) == OpState.EXECUTED && currExecs == maxExecsToObs) 
			opStates.put(opName, OpState.OBSERVER);
	}

	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#setModifier(randoop.operation.TypedOperation)
	 */
	@Override
	public void setModifier(TypedOperation op) {
		String opName = op.toJavaString();
		// Do not set an operation to modifier state the first time it is executed and generates new values 
		// (i.e. because it initializes variables only the first time)
		// If the operation never generates new values afterwards its not a real modifier
		/*
		if (opStates.get(opName) == null || (opStates.get(opName) == OpState.EXECUTED && numExecs.get(opName) <= 10))
			executed(op);
		else*/

		opStates.put(opName, OpState.MODIFIER);

		//countExecution(opName);
		countModifierExecution(opName);
	}
	
	private void countModifierExecution(String opName) {
		if (modExecs.get(opName) == null) 
			modExecs.put(opName, 0);
		modExecs.put(opName, modExecs.get(opName) + 1);
	}

	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#getOperationState(randoop.operation.TypedOperation)
	 */
	@Override
	public OpState getOperationState(TypedOperation op) {
		String opName = op.toJavaString();
		if (opStates.get(opName) == null) 
			return OpState.NOT_EXECUTED;
		return opStates.get(opName);
	}
	
	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#getNumberOfExecutions(randoop.operation.TypedOperation)
	 */
	@Override
	public int getNumberOfExecutions(TypedOperation op) {
		String opName = op.toJavaString();
		if (numExecs.get(opName) == null) 
			return 0;
		return numExecs.get(opName);
	}
	
	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#getNumberOfModifierExecutions(randoop.operation.TypedOperation)
	 */
	@Override
	public int getNumberOfModifierExecutions(TypedOperation op) {
		String opName = op.toJavaString();
		if (modExecs.get(opName) == null) 
			return 0;
		return modExecs.get(opName);
	}
	
	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#modifierOrObserver(randoop.operation.TypedOperation)
	 */
	@Override
	public boolean modifierOrObserver(TypedOperation op) {
		String opName = op.toJavaString();
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER || opState == OpState.OBSERVER;
	}
	
	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#modifier(randoop.operation.TypedOperation)
	 */
	@Override
	public boolean modifier(TypedOperation op) {
		String opName = op.toJavaString();
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER;
	}
	
	/* (non-Javadoc)
	 * @see randoop.fieldextensions.IOperationManager#toString()
	 */
	@Override
	public String toString() {
		String res = "";
	  	for (String op: opStates.keySet()) {
		  res += op + ": " + opStates.get(op) + 
				  ", modifier executions: " + modExecs.get(op) +
				  ", total executions: " + numExecs.get(op);
		  res += "\n";
	  	}
	  	return res;
	}
	
}
