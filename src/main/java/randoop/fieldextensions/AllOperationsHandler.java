package randoop.fieldextensions;

import java.util.List;
import java.util.Set;

import randoop.operation.TypedOperation;

public class AllOperationsHandler implements IBuildersManager {

	private List<TypedOperation> operations;

	public AllOperationsHandler(List<TypedOperation> operations) {
		this.operations = operations;
	}

	@Override
	public List<TypedOperation> getBuilders(int seqLength) {
		return operations;
	}

	@Override
	public void writeBuilders(String output_computed_builders) { }

	@Override
	public boolean isBuilder(TypedOperation operation) {
		return false;
	}

	@Override
	public void addBuilder(TypedOperation operation, int seqLength, Set<Integer> indexes) { }

	@Override
	public boolean alwaysBuilders() {
		return false;
	}

	@Override
	public Set<Integer> getIndexes(TypedOperation builder) {
		throw new Error("getIndexes method should never be called on AllOperationsHandler");
	} 
}
