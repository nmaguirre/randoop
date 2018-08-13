package randoop.generation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.FieldBasedGen;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Sequence;

public class FBForwardGenerator extends ForwardGenerator {

	public FBForwardGenerator(List<TypedOperation> operations, Set<TypedOperation> observers, long timeMillis,
			int maxGenSequences, int maxOutSequences, ComponentManager componentManager,
			RandoopListenerManager listenerManager) {
		super(operations, observers, timeMillis, maxGenSequences, maxOutSequences, componentManager, listenerManager);
	}
	
	private Generator gen;
	private Filter filter;
	protected Set<Sequence> subsumed_candidates = new LinkedHashSet<>();
	
	public void setGenerator(Generator gen) {
		this.gen = gen;
	}
	
	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	@Override
	protected boolean saveSequence(ExecutableSequence eSeq) {
		ExtensionsCollectorInOutVisitor collector = (ExtensionsCollectorInOutVisitor) executionVisitor;

		boolean resSave = gen.saveGeneratorSequence(this.componentManager, eSeq, collector);
		boolean resFilter = filter.filterSequence(eSeq, collector);
		if (!resSave && !resFilter) return false;

		subsumed_sequences.addAll(subsumed_candidates);
		return true;
	}
	
	@Override
	protected void subsumeSequences(InputsAndSuccessFlag sequences) {
		subsumed_candidates = new LinkedHashSet<>();
		for (Sequence is : sequences.sequences) {
			subsumed_candidates.add(is);
		}
	}
	
}
