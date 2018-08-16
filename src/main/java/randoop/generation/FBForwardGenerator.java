package randoop.generation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import randoop.DummyVisitor;
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
	private IObsSeqStore obsStore = new NoObsSeqStore();
	protected Set<Sequence> subsumed_candidates = new LinkedHashSet<>();
	
	public void setGenerator(Generator gen) {
		this.gen = gen;
	}
	
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	@Override
	public void setObserverSequenceStore(IObsSeqStore obsStore) { 
		this.obsStore = obsStore;
	}
	
	@Override
	public void setOriginalRandoopBehavior() {
		gen = new RandoopGeneratorApproach();
		filter = new RandoopInputFilter();
		addExecutionVisitor(new DummyVisitor());
	}
	
	@Override
	public void setFilterBehavior() {
		ForwardGeneratorFactory.setFBBehavior(FieldBasedGen.FILTER, this);
	}

	@Override
	protected boolean saveSequence(ExecutableSequence eSeq) {

		boolean newGen = gen.saveGeneratorSequence(this.componentManager, eSeq, executionVisitor);
		
		boolean newInput = !filter.filterSequence(eSeq, executionVisitor);

		if (!newGen && !newInput) return false;

		if (!newGen) obsStore.storeObserverSequence(eSeq.sequence);

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
	
	@Override
	public void saveNonGeneratorsAsGenerators() {
		assert (!(gen instanceof RandoopGeneratorApproach)): "Second phase cannot be used with the randoop approach for generators";
		
		for (Sequence seq: obsStore.getStoredSequences())
			componentManager.addGeneratedSequence(seq);
	}
	
	
}
