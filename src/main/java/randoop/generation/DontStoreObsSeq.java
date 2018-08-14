package randoop.generation;

import java.util.Set;

import randoop.sequence.Sequence;

public class DontStoreObsSeq implements IObsSeqStore {

	public void storeObserverSequence(Sequence sequence) { }

	@Override
	public Set<Sequence> getStoredSequences() {
		assert false: "Calling getStoredSequences() on a DontStoreObjSeq object";
		return null;
	};

}
