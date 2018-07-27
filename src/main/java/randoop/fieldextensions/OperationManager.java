package randoop.fieldextensions;

import java.util.LinkedHashMap;
import java.util.Map;

import randoop.operation.TypedOperation;

public class OperationManager {
	
	public enum OpState {
		NOT_EXECUTED,
		EXECUTED,
		MODIFIER 
	}

	private Map<String, OpState> opStates = new LinkedHashMap<>();
	
	public void executed(TypedOperation op) {
		String opName = op.toString();
		if (opStates.get(opName) == null) 
			opStates.put(opName, OpState.EXECUTED);
	}

	public void setModifier(TypedOperation op) {
		String opName = op.toString();
		opStates.put(opName, OpState.MODIFIER);
	}

	public OpState getOperationState(TypedOperation op) {
		String opName = op.toString();
		if (opStates.get(opName) == null) 
			return OpState.NOT_EXECUTED;
		return opStates.get(opName);
	}
	
}
