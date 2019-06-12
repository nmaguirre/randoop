package randoop.fieldextensions;

import java.util.HashSet;
import java.util.Set;

import randoop.generation.ComponentManager;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import utils.Tuple;

public class OriginalRandoopManager implements ISequenceManager {

	public OriginalRandoopManager() {
	}
	
	@Override
	public void writeResults(String filename, boolean fullExt) { }

	@Override
	public Tuple<Boolean, Set<Integer>> addGeneratedSequenceToManager(TypedOperation operation, ExecutableSequence eSeq, ComponentManager currMan, int seqLength) {
		currMan.addGeneratedSequence(eSeq.sequence);

		return new Tuple<Boolean, Set<Integer>>(true, new HashSet<Integer>());
	}

}
