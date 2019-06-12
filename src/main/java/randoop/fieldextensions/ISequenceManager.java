package randoop.fieldextensions;

import java.util.Set;

import randoop.generation.ComponentManager;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import utils.Tuple;

public interface ISequenceManager {

	void writeResults(String filename, boolean fullres);

	Tuple<Boolean, Set<Integer>> addGeneratedSequenceToManager(TypedOperation operation, ExecutableSequence eSeq, ComponentManager currMan, int seqLength);

}