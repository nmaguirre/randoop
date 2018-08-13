package randoop.generation;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.sequence.ExecutableSequence;

public interface Filter {

	public boolean filterSequence(ExecutableSequence eSeq, ExtensionsCollectorInOutVisitor collector); 

}
