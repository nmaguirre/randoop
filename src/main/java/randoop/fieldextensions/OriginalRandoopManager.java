package randoop.fieldextensions;

import java.util.List;

import randoop.generation.ComponentManager;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;

public class OriginalRandoopManager implements ISequenceManager {

	private IBuildersManager buildersManager;
	
	public OriginalRandoopManager(IBuildersManager buildersManager) {
		this.buildersManager = buildersManager;
	}
	
	@Override
	public void writeResults(String filename, boolean fullExt) { }

	@Override
	public boolean addGeneratedSequenceToManager(TypedOperation operation, ExecutableSequence eSeq, ComponentManager currMan, int seqLength) {
		currMan.addGeneratedSequence(eSeq.sequence);

		return true;
	}

	@Override
	public void writeBuilders(String filename) { }

	@Override
	public List<TypedOperation> getBuilders(int seqLength) {
		return buildersManager.getBuilders(seqLength);
	}

}
