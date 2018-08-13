package randoop.generation;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public class RandoopGenerator implements Generator {

	@Override
	public boolean saveGeneratorSequence(ComponentManager componentManager, ExecutableSequence eSeq,
			ExtensionsCollectorInOutVisitor collector) {
		  if (eSeq.sequence.hasActiveFlags()) {
			  componentManager.addGeneratedSequence(eSeq.sequence);
		  }
		  // Randoop generator never forces a sequence to be saved as a test
		  return false;
	}

}
