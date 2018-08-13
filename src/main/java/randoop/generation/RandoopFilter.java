package randoop.generation;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public class RandoopFilter implements Filter {

	@Override
	public boolean filterSequence(ExecutableSequence eSeq, ExtensionsCollectorInOutVisitor collector) {
		// Randoop saves all sequences as tests
		return true;
	}

}
