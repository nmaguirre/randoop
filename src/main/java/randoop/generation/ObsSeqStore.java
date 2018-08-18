package randoop.generation;

import java.util.LinkedHashSet;
import java.util.Set;

import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;

public class ObsSeqStore implements IObsSeqStore {
	
	private Set<Sequence> store = new LinkedHashSet<>();

	@Override
	public Set<Sequence> getStoredSequences() {
		return store;
	}

	@Override
	public void storeObserverSequence(Sequence seq) {
		if (seq.hasActiveFlags())
			store.add(seq);
	}

}
