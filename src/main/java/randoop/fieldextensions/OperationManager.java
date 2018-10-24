package randoop.fieldextensions;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.operation.TypedOperation;

public class OperationManager {
	
	public enum OpState {
		NOT_EXECUTED,
		EXECUTED,
		OBSERVER,
		MODIFIER 
	}

	private int maxExecsToObs;
	private Map<String, OpState> opStates = new LinkedHashMap<>();
	private Map<String, Integer> numExecs = new LinkedHashMap<>();
	private Map<String, Integer> modExecs = new LinkedHashMap<>();

	public OperationManager() {
		this.maxExecsToObs = Integer.MAX_VALUE;
	}
	
	public OperationManager(int maxExecsToObs) {
		this.maxExecsToObs = maxExecsToObs;
	}
	
	public void executed(TypedOperation op) {
		String opName = op.toString();
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
		String opName = op.toString();
		// Do not set an operation to modifier state the first time it is executed and generates new values 
		// (i.e. because it initializes variables only the first time)
		// If the operation never generates new values afterwards its not a real modifier
		/*
		if (opStates.get(opName) == null || (opStates.get(opName) == OpState.EXECUTED && numExecs.get(opName) <= 10))
			executed(op);
		else*/

		opStates.put(opName, OpState.MODIFIER);

		countExecution(opName);
		countModifierExecution(opName);
	}
	
	private void countModifierExecution(String opName) {
		if (modExecs.get(opName) == null) 
			modExecs.put(opName, 0);
		modExecs.put(opName, modExecs.get(opName) + 1);
	}

	public OpState getOperationState(TypedOperation op) {
		String opName = op.toString();
		if (opStates.get(opName) == null) 
			return OpState.NOT_EXECUTED;
		return opStates.get(opName);
	}
	
	public int getNumberOfExecutions(TypedOperation op) {
		String opName = op.toString();
		if (numExecs.get(opName) == null) 
			return 0;
		return numExecs.get(opName);
	}
	
	public int getNumberOfModifierExecutions(TypedOperation op) {
		String opName = op.toString();
		if (modExecs.get(opName) == null) 
			return 0;
		return modExecs.get(opName);
	}
	
	public boolean modifierOrObserver(TypedOperation op) {
		String opName = op.toString();
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER || opState == OpState.OBSERVER;
	}
	
	public boolean modifier(TypedOperation op) {
		String opName = op.toString();
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER;
	}
	
	public String toString() {
		String res = "";
	  	for (String op: opStates.keySet()) {
		  res += op + " is " + opStates.get(op) + 
				  ", modifier executions: " + modExecs.get(op) +
				  ", total executions: " + numExecs.get(op);
		  res += "\n";
	  	}
	  	return res;
	}
	
}
