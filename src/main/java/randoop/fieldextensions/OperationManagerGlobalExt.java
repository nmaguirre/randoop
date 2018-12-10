package randoop.fieldextensions;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.operation.ConstructorCall;
import randoop.operation.TypedOperation;

public class OperationManagerGlobalExt implements IOperationManager {
	
	private int maxModExecs;
	protected int maxExecsToObs;
	protected Map<String, TypedOperation> operations = new LinkedHashMap<>();
	protected Map<String, OpState> opStates = new LinkedHashMap<>();
	protected Map<String, Integer> numExecs = new LinkedHashMap<>();
	protected Map<String, Integer> modExecs = new LinkedHashMap<>();

	public OperationManagerGlobalExt() {
		this.maxExecsToObs = Integer.MAX_VALUE;
	}
	
	public OperationManagerGlobalExt(int maxExecsToObs) {
		this.maxExecsToObs = maxExecsToObs;
	}
	
	public void setMinExecutionsToModifier(int me) {
		maxModExecs = me;
	}
	
	private void saveOperation(String opName, TypedOperation op) {
		if (!operations.containsKey(opName)) 
			operations.put(opName, op);
	}
	
	public void executed(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
		
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

	public void setModifier(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
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

	public OpState getOperationState(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
		if (opStates.get(opName) == null) 
			return OpState.NOT_EXECUTED;
		return opStates.get(opName);
	}
	
	public int getNumberOfExecutions(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
		if (numExecs.get(opName) == null) 
			return 0;
		return numExecs.get(opName);
	}
	
	public int getNumberOfModifierExecutions(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
		if (modExecs.get(opName) == null) 
			return 0;
		return modExecs.get(opName);
	}
	
	public boolean modifierOrObserver(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER || opState == OpState.OBSERVER;
	}
	
	public boolean modifier(TypedOperation op) {
		String opName = op.toJavaString();
		saveOperation(opName, op);
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER;
	}
	
	public String toString() {
		String res = "";
		for (String op: opStates.keySet()) {
			OpState opState = opStates.get(op);
			if (operations.get(op).getOperation() instanceof ConstructorCall)
				opState = OpState.CONSTRUCTOR; 

			// methods executed <= maxModExecs times are not considered modifiers 
			if (opState == OpState.MODIFIER && modExecs.get(op) <= maxModExecs)
				opState = OpState.EXECUTED; 

			res += op + ": " + opState +
					", modifier executions: " + modExecs.get(op) +
					", total executions: " + numExecs.get(op) + "\n";
		}
	  	return res;
	}
	
}
