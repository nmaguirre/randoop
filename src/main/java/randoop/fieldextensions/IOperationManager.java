package randoop.fieldextensions;

import randoop.operation.TypedOperation;

public interface IOperationManager {
	
	public enum OpState {
		NOT_EXECUTED,
		EXECUTED,
		OBSERVER,
		MODIFIER,
		CONSTRUCTOR
	}

	void executed(TypedOperation op);

	void setModifier(TypedOperation op);

	OpState getOperationState(TypedOperation op);

	int getNumberOfExecutions(TypedOperation op);

	int getNumberOfModifierExecutions(TypedOperation op);

	boolean modifierOrObserver(TypedOperation op);

	boolean modifier(TypedOperation op);

	String toString();

}