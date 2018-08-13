package randoop.generation;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public interface Generator {

	// true iff the sequence must be saved as a test
	public boolean saveGeneratorSequence(ComponentManager componentManager, ExecutableSequence eSeq,
			ExtensionsCollectorInOutVisitor collector); 

}
