package randoop.fieldextensions;

import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;

public class OriginalRandoopManager implements IRedundancyStrategy {

	public OriginalRandoopManager() {
	}
	
	@Override
	public void writeResults(String filename, boolean fullExt) { }

	@Override
	public boolean checkGenNewObjects(TypedOperation operation, ExecutableSequence eSeq) {
		//currMan.addGeneratedSequence(eSeq.sequence);
		return true;
	}

}
