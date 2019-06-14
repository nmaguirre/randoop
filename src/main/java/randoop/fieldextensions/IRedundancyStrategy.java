package randoop.fieldextensions;


import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;

public interface IRedundancyStrategy {

	void writeResults(String filename, boolean fullres);

	boolean checkGenNewObjects(TypedOperation operation, ExecutableSequence eSeq);

}