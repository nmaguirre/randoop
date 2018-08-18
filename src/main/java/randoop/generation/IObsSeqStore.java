package randoop.generation;

import java.util.Set;

import randoop.sequence.Sequence;

public interface IObsSeqStore {

	public void storeObserverSequence(Sequence seq);
	
	public Set<Sequence> getStoredSequences();

}
