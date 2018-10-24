package randoop.fieldextensions;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import canonicalizer.BFHeapCanonicalizer;
import extensions.BoundedFieldExtensionsCollector;
import extensions.FieldExtensionsCollector;
import extensions.IFieldExtensions;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.Globals;
import randoop.NormalExecution;
import randoop.fieldextensions.OperationManager.OpState;
import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class MethodClassifierVisitor implements ExecutionVisitor {
	
	private BFHeapCanonicalizer canonicalizer;
	// private ExtensionsStore outputExt;
	private OperationManager opManager;
	private Set<String> classesUnderTest;
	private IFieldExtensions [] prevExt;
	private int maxObjects;
	private int maxArrayObjects;
	private int maxFieldDistance;
	
	// classesUnderTest = null to consider all classes as relevant
	public MethodClassifierVisitor() {
		this.maxObjects = GenInputsAbstract.fbg_max_objects;
		this.maxArrayObjects = GenInputsAbstract.fbg_max_arr_objects;
		this.maxFieldDistance = GenInputsAbstract.fbg_max_field_distance;
		canonicalizer = new BFHeapCanonicalizer();
		canonicalizer.setMaxArrayObjs(maxArrayObjects);
		canonicalizer.setMaxFieldDistance(maxFieldDistance);
		//outputExt = new ExtensionsStore(maxObjects);
		opManager = new OperationManager();
		if (GenInputsAbstract.fbg_classlist != null)
			this.classesUnderTest = GenInputsAbstract.getClassnamesFBG();
	}
	
	/*
	public OpState getOperationState(TypedOperation op) {
		return opManager.getOperationState(op);
	}
	
	public int getNumberOfExecutions(TypedOperation op) {
		return opManager.getNumberOfExecutions(op);
	}
	
	public int getNumberOfModifierExecutions(TypedOperation op) {
		return opManager.getNumberOfModifierExecutions(op);
	}
	*/
	
	@Override
	public void visitBeforeStatement(ExecutableSequence sequence, int i) {
		if (sequence.sequence.size() == 1 || i != sequence.sequence.size() -1) return;
		
		Statement stmt = sequence.sequence.getStatement(i);
		TypedOperation op = stmt.getOperation();
		// FIXME: var v = class.field are not method calls
		//assert op.isConstructorCall() || op.isMethodCall(): op.toParsableString() + "is not a constructor or method call";
		String className = getOperationClass(op);
		// We only want to see if methods of classes under test are exercised with new objects
		// For the remaining classes we drop any other sequence that is not a generator
		if (!classUnderTest(className) ||
				opManager.modifier(op)) return;

		Object[] inputs = sequence.getRuntimeInputs(i);
		prevExt = new IFieldExtensions [inputs.length];
		for (int j = 0; j < inputs.length; j++) {
			FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
			canonicalizer.canonicalize(inputs[j], currExtCollector);
			prevExt[j] = currExtCollector.getExtensions();
		}
	}

	@Override
	public void visitAfterStatement(ExecutableSequence sequence, int i) {
		if (i != sequence.sequence.size() -1) return;

		ExecutionOutcome statementResult = sequence.getResult(i);	
		TypedOperation op = sequence.sequence.getStatement(i).getOperation();
		String className = getOperationClass(op);
		
		if (!classUnderTest(className) ||
				opManager.modifier(op)) return;
		
		if (statementResult instanceof NormalExecution) {
			opManager.executed(op);
			// Check whether the result value generates new objects
			Statement stmt = sequence.sequence.getStatement(i);
			if (!stmt.getOutputType().isVoid()) {
				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
				if (retVal != null && !isPrimitive(retVal)) 
					/*
					FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(retVal.getClass().getName());
					// makes extensionsWereExtended default to false
					collector.start();
					canonicalizer.canonicalize(retVal, collector);
					if (collector.extensionsWereExtended()) {
						newOutput = true;
						sequence.sequence.setFBActiveFlag(varIndex);
						opState = OpState.MODIFIER;
					}
					*/
					opManager.setModifier(op);
			}
			
			Object[] objsAfterExec = sequence.getRuntimeInputs(i);
			// Check whether the objects referenced by parameters are modified by the execution
			for (int j = 0; j < objsAfterExec.length; j++) {
				Object curr = objsAfterExec[j];
				FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
				canonicalizer.canonicalize(curr, currExtCollector);
				if (!prevExt[j].equals(currExtCollector.getExtensions()))
					opManager.setModifier(op);
				
				/*
				if (curr == null || isPrimitive(curr)) { varIndex++; continue; }
				
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(curr.getClass().getName());
				// makes extensionsWereExtended default to false
				collector.start();
				canonicalizer.canonicalize(curr, collector);
				if (collector.extensionsWereExtended()) {
				}
				*/
			}
		}
	}

	@Override
	public void initialize(ExecutableSequence executableSequence) {
		prevExt = null;
	}

	@Override
	public void visitAfterSequence(ExecutableSequence executableSequence) {
		
	}
	
	private boolean isPrimitive(Object o) {
		Class<?> cls = o.getClass();
		return NonreceiverTerm.isNonreceiverType(cls) && !cls.equals(Class.class);
	}
	
	private Map<String, String> classNameCache = new LinkedHashMap<>();

	private String getOperationClass(TypedOperation op) {
		TypedClassOperation typOp = (TypedClassOperation) op;
		// FIXME: Hack for the sequence:
		//			java.lang.Object obj0 = new java.lang.Object()
		if (op.toParsableString().equals("java.lang.Object.<init>()"))
			return "java.lang.Object";

		assert typOp.drawnFromClass != null: op.toParsableString() + "is not associated to any input class";
		
		String className = typOp.drawnFromClass.getName();
		String classNameNoGenerics = classNameCache.get(className);
		if (classNameNoGenerics == null) {
			if (className.indexOf('<') == -1)
				classNameNoGenerics = className;
			else
				classNameNoGenerics = className.substring(0, className.indexOf('<'));

			classNameCache.put(className, classNameNoGenerics);
		}
		return classNameNoGenerics;
	}

	private boolean classUnderTest(String className) {
		if (classesUnderTest == null) return true;

		// Only consider classes marked as important to be tested by the user
		// This is akin to try to cover a few classes that one wants to test
		return classesUnderTest.contains(className);
	}

	@Override
	public void doOnTermination() {
		if (GenInputsAbstract.method_classification == null) 
			throw new IllegalStateException("Set --method-classification to a valid file before using " + MethodClassifierVisitor.class.getName());

		try {
			GenInputsAbstract.method_classification.write(opManager.toString());
			GenInputsAbstract.method_classification.flush();
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
