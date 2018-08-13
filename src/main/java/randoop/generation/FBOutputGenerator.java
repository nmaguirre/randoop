package randoop.generation;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public class FBOutputGenerator implements Generator {

	@Override
	public boolean saveGeneratorSequence(ComponentManager componentManager, ExecutableSequence eSeq,
			ExtensionsCollectorInOutVisitor collector) {
		if (collector.generatesNewOutput()) {
			componentManager.addFieldBased(eSeq.sequence);
			// Sequence must be saved as a test
			return true;
		}

		return false;
	}

}
