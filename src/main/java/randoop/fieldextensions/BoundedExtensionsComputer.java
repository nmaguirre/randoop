package randoop.fieldextensions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import canonicalizer.BFHeapCanonicalizer;
import extensions.FieldExtensionsCollector;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.generation.ComponentManager;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;
import utils.Tuple;


public class BoundedExtensionsComputer implements ISequenceManager {
	
	protected BFHeapCanonicalizer canonicalizer;
	protected ExtensionsStore outputExt;
	//protected ExtensionsStore currExt;
	protected int maxObjects;
	protected int maxArrayObjects;
	protected int maxFieldDistance;
	protected int maxBFDepth;
	protected IBuildersManager buildersManager;
	private int maxStoppingPrims;
	private int maxStoppingObjs;
	
	// classesUnderTest = null to consider all classes as relevant
	public BoundedExtensionsComputer(int maxStoppingObjs, int maxStoppingPrims, IBuildersManager buildersManager) {
		if (maxStoppingObjs <= 0 || maxStoppingPrims <= 0)
			throw new Error("BoundedExtensionsComputerVisitor must be used with max_stopping_objects > 0 and max_stopping_primitives > 0");
		
		this.buildersManager = buildersManager;
		this.maxObjects = Integer.MAX_VALUE; 
		this.maxArrayObjects = Integer.MAX_VALUE;
		this.maxFieldDistance = Integer.MAX_VALUE;
		this.maxBFDepth = Integer.MAX_VALUE;
		canonicalizer = new BFHeapCanonicalizer();
		canonicalizer.setMaxArrayObjs(maxArrayObjects);
		canonicalizer.setMaxFieldDistance(maxFieldDistance);
		canonicalizer.setMaxBFDepth(maxBFDepth);
		canonicalizer.setMaxObjects(maxStoppingObjs);
		canonicalizer.setStopOnError();
		outputExt = new ExtensionsStore(maxStoppingPrims, true);
		//currExt = new ExtensionsStore(maxStoppingPrims, true);
		this.maxStoppingPrims = maxStoppingPrims;
		this.maxStoppingObjs = maxStoppingObjs;
	}
	

	// Returns the indices where the objects that initialize new field values are, null if 
	// some object exceeds given bounds or the execution of the sequence fails 
	protected Set<Integer> newFieldValuesInitialized(ExecutableSequence sequence) {
		Set<Integer> indices = new LinkedHashSet<>();
		int i = sequence.sequence.size() -1;

		ExecutionOutcome statementResult = sequence.getResult(i);	
		TypedOperation op = sequence.sequence.getStatement(i).getOperation();

		//String className = Utils.getOperationClass(op);
		//if (!Utils.classUnderTest(className)) return;
		
		if (statementResult instanceof NormalExecution) {
			int index = 0;
			Statement stmt = sequence.sequence.getStatement(i);
			// Sort objects by type first
			Map<String, List<Tuple<Object, Integer>>> objsByType = new LinkedHashMap<>();
			if (!stmt.getOutputType().isVoid()) {
				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
				if (retVal != null && !Utils.isPrimitive(retVal)) {
					List<Tuple<Object, Integer>> l = new LinkedList<>();
					l.add(new Tuple<>(retVal, index));
					objsByType.put(retVal.getClass().getName(), l);
				}
				index++;
			}

			Object[] objsAfterExec = sequence.getRuntimeInputs(i);
			for (int j = 0; j < objsAfterExec.length; j++) {
				Object curr = objsAfterExec[j];
				if (curr == null || Utils.isPrimitive(curr)) { index++; continue; }
				String cls = curr.getClass().getName();
				if (objsByType.get(cls) == null) {
					List<Tuple<Object, Integer>> l = new LinkedList<>();
					l.add(new Tuple<>(curr, index));;
					objsByType.put(cls, l);
				}
				else 
					objsByType.get(cls).add(new Tuple<>(curr, index));
				index++;
			}	
			
			for (String cls: objsByType.keySet()) {
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(cls);
//				FieldExtensionsCollector collector = currExt.getOrCreateCollectorForMethodParam(cls);
				collector.start();
				collector.setTestMode();
				for (Tuple<Object, Integer> t: objsByType.get(cls)) {
					if (!canonicalizer.canonicalize(t.getFirst(), collector))
						return null;
				}
				collector.testCommitAllPairs();
				if (collector.testExtensionsLimitExceeded())
					return null;
			}
			
			// Test does not exceed the limits
			for (String cls: objsByType.keySet()) {
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(cls);
//				FieldExtensionsCollector collector = currExt.getOrCreateCollectorForMethodParam(cls);
				if (collector.testExtensionsWereExtended()) {
					collector.commitSuccessfulTestsPairs();
					// FIXME: We currently do not keep track of the exact object that has extended the extensions
					// but return all indices of the objects for the class for which we saw new field values
					for (Tuple<Object, Integer> t: objsByType.get(cls)) {
						indices.add(t.getSecond());
					}
				}
			}	
		}
		else {
			// Abnormal execution
			return null;
		}

		return indices;
	}
	
	@Override
	public void writeResults(String filename, boolean fullExt) {
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(outputExt.getStatistics(fullExt));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	
//	private int maxSeqLength = 0;

	@Override
	public boolean addGeneratedSequenceToManager(TypedOperation operation, ExecutableSequence eSeq, ComponentManager currMan, int seqLength) {
		/*
		if (seqLength > maxSeqLength) {
			maxSeqLength = seqLength;
			outputExt.addAllExtensions(currExt);
			currExt = new ExtensionsStore(maxStoppingPrims, true);
		}
		*/
		
		Set<Integer> activeIndexes = newFieldValuesInitialized(eSeq);

		if (activeIndexes == null) return false;

		if (activeIndexes.size() == 0) { 
			if (buildersManager.alwaysBuilders() && buildersManager.isBuilder(operation)) {
				activeIndexes = buildersManager.getIndexes(operation);
			}
			else
				return false;
		}

		buildersManager.addBuilder(operation, seqLength, activeIndexes);
		
		currMan.addGeneratedSequence(eSeq.sequence, activeIndexes);
		return true;
	}


	@Override
	public void writeBuilders(String filename) {
		buildersManager.writeBuilders(filename);
	}


	@Override
	public List<TypedOperation> getBuilders(int seqLength) {
		return buildersManager.getBuilders(seqLength);
	}

}
