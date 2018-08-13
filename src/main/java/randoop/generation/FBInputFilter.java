package randoop.generation;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public class FBInputFilter implements Filter {

	@Override
	public boolean filterSequence(ExecutableSequence eSeq, ExtensionsCollectorInOutVisitor collector) {
		// The sequence does not belong to relevant classes, or it does but does not generate new input
		return (!collector.testsMethodFromRelevantClass(eSeq) || 
				!collector.testsWithNewInput());
	}

}
