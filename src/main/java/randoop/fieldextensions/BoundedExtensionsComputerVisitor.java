package randoop.fieldextensions;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import canonicalizer.BFHeapCanonicalizer;
import extensions.BoundedFieldExtensionsCollector;
import extensions.FieldExtensionsCollector;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class BoundedExtensionsComputerVisitor implements ExecutionVisitor {
	
	private BFHeapCanonicalizer canonicalizer;
	private ExtensionsStore outputExt;
	private int maxObjects;
	private int maxArrayObjects;
	private int maxFieldDistance;
	private int maxBFDepth;
	
	// classesUnderTest = null to consider all classes as relevant
	public BoundedExtensionsComputerVisitor() {
		if (GenInputsAbstract.max_stopping_objects <= 0 || GenInputsAbstract.max_stopping_primitives <= 0)
			throw new Error("BoundedExtensionsComputerVisitor must be used with max_stopping_objects > 0 and max_stopping_primitives > 0");
		
		this.maxObjects = GenInputsAbstract.fbg_max_objects;
		this.maxArrayObjects = GenInputsAbstract.fbg_max_arr_objects;
		this.maxFieldDistance = GenInputsAbstract.fbg_max_field_distance;
		this.maxBFDepth = GenInputsAbstract.fbg_max_bf_depth;
		canonicalizer = new BFHeapCanonicalizer();
		canonicalizer.setMaxArrayObjs(maxArrayObjects);
		canonicalizer.setMaxFieldDistance(maxFieldDistance);
		canonicalizer.setMaxBFDepth(maxBFDepth);
		canonicalizer.setMaxObjects(GenInputsAbstract.max_stopping_objects);
		canonicalizer.setStopOnError();
		outputExt = new ExtensionsStore(maxObjects, true);
	}
	
	@Override
	public void visitBeforeStatement(ExecutableSequence sequence, int i) { }

	@Override
	public void visitAfterStatement(ExecutableSequence sequence, int i) {
		if (i != sequence.sequence.size() -1) return;

		ExecutionOutcome statementResult = sequence.getResult(i);	
		TypedOperation op = sequence.sequence.getStatement(i).getOperation();
		String className = Utils.getOperationClass(op);
		
		if (!Utils.classUnderTest(className)) return;
		
		if (statementResult instanceof NormalExecution) {
			Statement stmt = sequence.sequence.getStatement(i);
			// Sort objects by type first
			Map<String, List<Object>> objsByType = new LinkedHashMap<>();
			if (!stmt.getOutputType().isVoid()) {
				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
				if (retVal != null && !Utils.isPrimitive(retVal)) {
					List<Object> l = new LinkedList<>();
					l.add(retVal);
					objsByType.put(retVal.getClass().getName(), l);
				}
			}

			Object[] objsAfterExec = sequence.getRuntimeInputs(i);
			for (int j = 0; j < objsAfterExec.length; j++) {
				Object curr = objsAfterExec[j];
				if (curr == null || Utils.isPrimitive(curr)) continue;
				String cls = curr.getClass().getName();
				if (objsByType.get(cls) == null) {
					List<Object> l = new LinkedList<>();
					l.add(curr);
					objsByType.put(cls, l);
				}
				else 
					objsByType.get(cls).add(curr);
			}	
			
			for (String cls: objsByType.keySet()) {
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(cls);
				collector.start();
				collector.setTestMode();
				for (Object o: objsByType.get(cls)) {
					if (!canonicalizer.canonicalize(o, collector))
						throw new VisitorException();
				}
				collector.testCommitAllPairs();
				if (collector.testExtensionsLimitExceeded())
					throw new VisitorException();
			}
			
			// Test does not exceed the limits
			for (String cls: objsByType.keySet()) {
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(cls);
				if (collector.testExtensionsWereExtended())
					collector.commitSuccessfulTestsPairs();
			}	
		}
	}
	
	@Override
	public void initialize(ExecutableSequence executableSequence) {

	}

	@Override
	public void visitAfterSequence(ExecutableSequence executableSequence) {
		
	}

	@Override
	public void doOnTermination() {
		try {
			FileWriter writer = new FileWriter(GenInputsAbstract.extensions_computation_res);
			writer.write(outputExt.getStatistics(GenInputsAbstract.save_full_extensions));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
	
	/*
	private boolean testsMethodFromRelevantClass(ExecutableSequence sequence) {
		if (classesUnderTest == null) 
			return true;
		
		Statement stmt = sequence.sequence.getStatement(sequence.sequence.size()-1);
		TypedOperation op = stmt.getOperation();
		return classUnderTest(getOperationClass(op));	
	}
	*/

}
