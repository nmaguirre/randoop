package randoop.generation;

import randoop.ExecutionVisitor;
import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public class FBGeneratorApproach implements Generator {

	@Override
	public boolean saveGeneratorSequence(ComponentManager componentManager, ExecutableSequence eSeq,
			ExecutionVisitor collector) {
		ExtensionsCollectorInOutVisitor extcollector = (ExtensionsCollectorInOutVisitor) collector;

		if (extcollector.generatesNewOutput()) {
			componentManager.addFieldBased(eSeq.sequence);
			// Sequence must be saved as a test
			return true;
		}

		return false;
	}

}
