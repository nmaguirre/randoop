package randoop.generation;

import randoop.ExecutionVisitor;
import randoop.sequence.ExecutableSequence;

public class RandoopGeneratorApproach implements Generator {

	@Override
	public boolean saveGeneratorSequence(ComponentManager componentManager, ExecutableSequence eSeq,
			ExecutionVisitor collector) {
		  if (eSeq.sequence.hasActiveFlags()) {
			  componentManager.addGeneratedSequence(eSeq.sequence);
		  }
		  // Randoop generator never forces a sequence to be saved as a test
		  return false;
	}

}
