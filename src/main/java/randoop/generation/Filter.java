package randoop.generation;

import randoop.ExecutionVisitor;
import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public interface Filter {

	public boolean filterSequence(ExecutableSequence eSeq, ExecutionVisitor executionVisitor); 

}
