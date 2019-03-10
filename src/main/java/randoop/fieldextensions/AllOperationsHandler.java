package randoop.fieldextensions;

import java.util.List;

import randoop.operation.TypedOperation;

public class AllOperationsHandler implements IBuildersManager {

	private List<TypedOperation> operations;

	public AllOperationsHandler(List<TypedOperation> operations) {
		this.operations = operations;
	}

	@Override
	public void addOperation(TypedOperation operation, int seqLength) { }

	@Override
	public List<TypedOperation> getBuilders(int seqLength) {
		return operations;
	}

	@Override
	public void writeBuilders(String output_computed_builders) { }

}
