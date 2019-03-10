package randoop.fieldextensions;

import java.util.List;

import randoop.operation.TypedOperation;

public interface IBuildersManager {

	List<TypedOperation> getBuilders(int seqLength);

	void addOperation(TypedOperation operation, int seqLength);

	void writeBuilders(String output_computed_builders);

}
