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
	
	public OperationManager(int maxExecsToObs) {
		this.maxExecsToObs = maxExecsToObs;
	}
	
	public void executed(TypedOperation op) {
		String opName = op.toString();
		if (opStates.get(opName) == null) {
			opStates.put(opName, OpState.EXECUTED);
			numExecs.put(opName, 0);
		}
		
		if (opStates.get(opName) == OpState.EXECUTED) {
			int currExecs = numExecs.get(opName) + 1;
			if (currExecs == maxExecsToObs) 
				opStates.put(opName, OpState.OBSERVER);
			else 
				numExecs.put(opName, currExecs);
		}
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
	}

	public OpState getOperationState(TypedOperation op) {
		String opName = op.toString();
		if (opStates.get(opName) == null) 
			return OpState.NOT_EXECUTED;
		return opStates.get(opName);
	}
	
	public boolean modifierOrObserver(TypedOperation op) {
		String opName = op.toString();
		OpState opState = opStates.get(opName);
		return opState == OpState.MODIFIER || opState == OpState.OBSERVER;
	}
	
	
}
