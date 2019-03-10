package randoop.fieldextensions;

import randoop.generation.ComponentManager;
import randoop.sequence.ExecutableSequence;

public class OriginalRandoopManager implements ISequenceManager {

	@Override
	public void writeFieldExtensions(String filename, boolean fullExt) { }

	@Override
	public boolean addGeneratedSequenceToManager(ExecutableSequence eSeq, ComponentManager currMan) {
		currMan.addGeneratedSequence(eSeq.sequence);

		return true;
	}

}
