package randoop.fieldextensions;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class ObjectCountStore {
	
//	private BFHeapCanonicalizer canonicalizer;
//	private int maxObjects;
//	private int maxArrayObjects;
//	private int maxFieldDistance;
	private Map<String, Set<Object>> objs = new LinkedHashMap<>();
	private int cantCount;
	private Map<String, Integer> objsSize = new LinkedHashMap<>();

//	public ObjectCountStore(int maxObjects, int maxArrayObjects, int maxFieldDistance) {
//		this.maxObjects= maxObjects;
//		this.maxArrayObjects = maxArrayObjects;
//		this.maxFieldDistance = maxFieldDistance;
//		/*
//		canonicalizer = new BFHeapCanonicalizer();
//		canonicalizer.setMaxArrayObjs(maxArrayObjects);
//		canonicalizer.setMaxFieldDistance(maxFieldDistance);
//		*/
//	}

	
//	@Override
//	public void visitBeforeStatement(ExecutableSequence sequence, int i) { }
//
//	@Override
//	public void visitAfterStatement(ExecutableSequence sequence, int i) {
//		if (i != sequence.sequence.size() -1) return;
//
//		ExecutionOutcome statementResult = sequence.getResult(i);	
//		if (statementResult instanceof NormalExecution) {
//			// Check whether the result value generates new objects
//			Statement stmt = sequence.sequence.getStatement(i);
//			if (!stmt.getOutputType().isVoid()) {
//				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
//				countObject(retVal);
//					/*
//				if (retVal != null) {
//					FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
//					canonicalizer.canonicalize(retVal, currExtCollector);
//					// TODO: Count current object 
//					// Class name: retVal.getClass().getName();
//				}
//					*/
//			}
//			
//			Object[] objsAfterExec = sequence.getRuntimeInputs(i);
//			// Count objects referenced by parameters 
//			for (int j = 0; j < objsAfterExec.length; j++) {
//				Object curr = objsAfterExec[j];
//				countObject(curr);
//				/*
//				FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
//				canonicalizer.canonicalize(curr, currExtCollector);
//				*/
//				// TODO: Count current object 
//				// Class name: curr.getClass().getName();
//			}
//		}
//	}
	
	
	public void countObjects(ExecutableSequence sequence) {
		if (!sequence.isNormalExecution()) return;

		int lastStmtInd = sequence.sequence.size() -1;
		Statement stmt = sequence.sequence.getStatement(lastStmtInd);
		if (!stmt.getOutputType().isVoid()) {
			ExecutionOutcome statementResult = sequence.getResult(lastStmtInd);
			Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
			countObject(retVal);
		}
		
		Object[] objsAfterExec = sequence.getRuntimeInputs(lastStmtInd);
		// Count objects referenced by parameters 
		for (int j = 0; j < objsAfterExec.length; j++) {
			Object curr = objsAfterExec[j];
			countObject(curr);
		}
		
		if (GenInputsAbstract.measure_objects)
			measureObjects(sequence);
		
	}
	
	
	private void measureObjects(ExecutableSequence sequence) {
		//if (!sequence.isNormalExecution()) return;

		int lastStmtInd = sequence.sequence.size() -1;
		Statement stmt = sequence.sequence.getStatement(lastStmtInd);
		if (!stmt.getOutputType().isVoid()) {
			ExecutionOutcome statementResult = sequence.getResult(lastStmtInd);
			Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
			measureObject(retVal);
		}
		
		Object[] objsAfterExec = sequence.getRuntimeInputs(lastStmtInd);
		// Count objects referenced by parameters 
		for (int j = 0; j < objsAfterExec.length; j++) {
			Object curr = objsAfterExec[j];
			measureObject(curr);
		}
	}
	
	
	private void countObject(Object o) {
		try {
			if (o != null) {
				String cls = o.getClass().getName();
				Set<Object> clsObjs = objs.get(cls);
				if (clsObjs == null) {
					clsObjs = new LinkedHashSet<>();
					objs.put(cls, clsObjs);
				}
				clsObjs.add(o);
			}
		} catch (Exception e) {
			cantCount++;
		}
	}
	
	
	private void measureObject(Object o) {
		try {
			if (o != null) {
				String cls = o.getClass().getName();
				Method m = o.getClass().getMethod("size", null);
				m.setAccessible(true);
				Integer size = (Integer) m.invoke(o);
				
				Integer maxSize = objsSize.get(cls);
				if (maxSize == null) 
					objsSize.put(cls, size);
				else
					objsSize.put(cls, Math.max(maxSize, size));
			}
		} catch (Exception e) {

		}
	}
	

	public String countResultToString() {
		StringBuilder sb = new StringBuilder();
		int sum = 0;
		for (String cls: objs.keySet()) {
			int clsSum = objs.get(cls).size(); 
			sb.append("Objects for class ")
				.append(cls)
				.append(": ")
				.append(clsSum)
				.append("\n");
			sum += clsSum;
		}
		sb.append("Total objects count: ")
			.append(sum)
			.append("\n");
		sb.append("Objects not counted: ")
			.append(cantCount)
			.append("\n");
		
		if (GenInputsAbstract.measure_objects) {
			for (String cls: objsSize.keySet()) {
				sb.append("Max object size for class ")
					.append(cls)
					.append(": ")
					.append(objsSize.get(cls))
					.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	private boolean isPrimitive(Object o) {
		Class<?> cls = o.getClass();
		return NonreceiverTerm.isNonreceiverType(cls) && !cls.equals(Class.class);
	}

//	@Override
//	public void initialize(ExecutableSequence executableSequence) { }
//
//	@Override
//	public void visitAfterSequence(ExecutableSequence executableSequence) { }

}
