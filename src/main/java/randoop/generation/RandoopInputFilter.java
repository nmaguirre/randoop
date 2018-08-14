package randoop.generation;

import randoop.ExecutionVisitor;
import randoop.sequence.ExecutableSequence;

public class RandoopInputFilter implements Filter {

	@Override
	public boolean filterSequence(ExecutableSequence eSeq, ExecutionVisitor collector) {
		// Randoop saves all sequences as tests
		return true;
	}

}
