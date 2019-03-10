package randoop.fieldextensions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import randoop.operation.TypedOperation;

public class BuildersOnlyHandler implements IBuildersManager {

	private List<TypedOperation> operations;
	private int buildersSeqLength;
	private List<TypedOperation> builders = new ArrayList<>();
	private Set<String> buildersNames = new HashSet<>();

	public BuildersOnlyHandler(List<TypedOperation> operations, int buildersSeqLength) {
		this.operations = operations;
		this.buildersSeqLength = buildersSeqLength;
	}

	@Override
	public void addOperation(TypedOperation operation, int seqLength) {
		if (seqLength <= buildersSeqLength) {
			String opName = operation.toString();
			if (!buildersNames.contains(opName)) {
				builders.add(operation);
				buildersNames.add(opName);
			}
		}
	}

	@Override
	public List<TypedOperation> getBuilders(int seqLength) {
		if (seqLength < buildersSeqLength)
			return operations;
		else
			return builders; 
	}

	@Override
	public void writeBuilders(String filename) {
		try {
			FileWriter writer = new FileWriter(filename);
			for (TypedOperation builder: builders) {
				writer.write(builder.toString() + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
