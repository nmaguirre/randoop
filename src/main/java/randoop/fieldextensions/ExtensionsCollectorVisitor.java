package randoop.fieldextensions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import canonicalizer.BFHeapCanonicalizer;
import extensions.BoundedFieldExtensionsCollector;
import extensions.FieldExtensionsCollector;
import extensions.IFieldExtensions;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.NormalExecution;
import randoop.fieldextensions.OperationManager.OpState;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class ExtensionsCollectorVisitor implements ExecutionVisitor {
	
	private BFHeapCanonicalizer canonicalizer;
	private ExtensionsStore methodInputExt;
	private ExtensionsStore outputExt;
	private OperationManager opManager;
	private boolean newInput;
	private boolean newOutput;
	private boolean preciseObserversDetection;
	private Set<String> classesUnderTest;
	private IFieldExtensions [] prevExt;
	private int maxObjects;
	private int maxArrayObjects;
	private int maxFieldDistance;

	public ExtensionsCollectorVisitor(Set<String> classesUnderTest, int maxObjects, int maxArrayObjects, int maxFieldDistance) {
		this(classesUnderTest, maxArrayObjects, maxArrayObjects, maxFieldDistance, false, Integer.MAX_VALUE);
	}
	
	// classesUnderTest = null to consider all classes as relevant
	public ExtensionsCollectorVisitor(Set<String> classesUnderTest, int maxObjects, int maxArrayObjects, 
			int maxFieldDistance, boolean preciseObserversDetection, int maxExecsToObs) {
		this.maxObjects= maxObjects;
		this.maxArrayObjects = maxArrayObjects;
		this.maxFieldDistance = maxFieldDistance;
		canonicalizer = new BFHeapCanonicalizer();
		canonicalizer.setMaxArrayObjs(maxArrayObjects);
		canonicalizer.setMaxFieldDistance(maxFieldDistance);
		methodInputExt = new ExtensionsStore(maxObjects);
		outputExt = new ExtensionsStore(maxObjects);
		opManager = new OperationManager(maxExecsToObs);
		this.classesUnderTest = classesUnderTest;
		this.preciseObserversDetection = preciseObserversDetection;
	}
	
	public boolean testsWithNewInput() {
		return newInput;
	}

	public boolean generatesNewOutput() {
		return newOutput;
	}

	public OpState getOperationState(TypedOperation op) {
		return opManager.getOperationState(op);
	}
	
	public int getNumberOfExecutions(TypedOperation op) {
		return opManager.getNumberOfExecutions(op);
	}
	
	public int getNumberOfModifierExecutions(TypedOperation op) {
		return opManager.getNumberOfModifierExecutions(op);
	}

	public boolean testsMethodFromRelevantClass(ExecutableSequence sequence) {
		if (classesUnderTest == null) 
			return true;
		
		Statement stmt = sequence.sequence.getStatement(sequence.sequence.size()-1);
		TypedOperation op = stmt.getOperation();
		return classUnderTest(getOperationClass(op));	
	}
	
	@Override
	public void visitBeforeStatement(ExecutableSequence sequence, int i) {
		if (sequence.sequence.size() == 1 || i != sequence.sequence.size() -1) return;
		
		Statement stmt = sequence.sequence.getStatement(i);
		TypedOperation op = stmt.getOperation();
		String methodName = op.toString();
		// FIXME: var v = class.field are not method calls
		//assert op.isConstructorCall() || op.isMethodCall(): op.toParsableString() + "is not a constructor or method call";
		String className = getOperationClass(op);
		// We only want to see if methods of classes under test are exercised with new objects
		// For the remaining classes we drop any other sequence that is not a generator
		if (!classUnderTest(className)) return;

		Object[] inputs = sequence.getRuntimeInputs(i);
		if (preciseObserversDetection && !opManager.modifierOrObserver(op))
			prevExt = new IFieldExtensions [inputs.length];
		for (int j = 0; j < inputs.length; j++) {
			FieldExtensionsCollector collector = methodInputExt.getOrCreateCollectorForMethodParam(className, methodName, j);
			// makes extensionsWereExtended default to false
			collector.start();
			canonicalizer.canonicalize(inputs[j], collector);
			newInput |= collector.extensionsWereExtended();

			if (preciseObserversDetection && !opManager.modifierOrObserver(op) && inputs[j] != null) {
				FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
				canonicalizer.canonicalize(inputs[j], currExtCollector);
				prevExt[j] = currExtCollector.getExtensions();
			}
		}
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
	public void visitAfterStatement(ExecutableSequence sequence, int i) {
		if (i != sequence.sequence.size() -1) return;

		ExecutionOutcome statementResult = sequence.getResult(i);	
		TypedOperation op = sequence.sequence.getStatement(i).getOperation();
		OpState opState = null; 
		
		if (statementResult instanceof NormalExecution) {
			int varIndex = 0;
			// Check whether the result value generates new objects
			Statement stmt = sequence.sequence.getStatement(i);
			if (!stmt.getOutputType().isVoid()) {
				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
				if (retVal != null && !isPrimitive(retVal)) { 
					FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(retVal.getClass().getName());
					// makes extensionsWereExtended default to false
					collector.start();
					canonicalizer.canonicalize(retVal, collector);
					if (collector.extensionsWereExtended()) {
						newOutput = true;
						sequence.sequence.setFBActiveFlag(varIndex);
						opState = OpState.MODIFIER;
					}
				}
				varIndex++;
			}
			
			Object[] objsAfterExec = sequence.getRuntimeInputs(i);
			// Check whether the objects referenced by parameters are modified by the execution
			for (int j = 0; j < objsAfterExec.length; j++) {
				Object curr = objsAfterExec[j];
				
				if (preciseObserversDetection && !opManager.modifierOrObserver(op) && curr != null) {
					FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
					canonicalizer.canonicalize(curr, currExtCollector);
					if (!prevExt[j].equals(currExtCollector.getExtensions()))
						opState = OpState.MODIFIER;
				}
				
				if (curr == null || isPrimitive(curr)) { varIndex++; continue; }
				
				FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(curr.getClass().getName());
				// makes extensionsWereExtended default to false
				collector.start();
				canonicalizer.canonicalize(curr, collector);
				if (collector.extensionsWereExtended()) {
					newOutput = true;
					sequence.sequence.setFBActiveFlag(varIndex);
					if (!preciseObserversDetection)
						opState = OpState.MODIFIER;
				}

				varIndex++;
			}

			if (opState == OpState.MODIFIER)
				opManager.setModifier(op);
			else
				opManager.executed(op);

			/*
			// FIXME: Allow single constructor calls to be used as generators. 
			// This allows to build generic structures instantiated with different types that otherwise would get discarded 
			if (!newOutput && sequence.sequence.size() == 1 && sequence.sequence.getStatement(0).getOperation().isConstructorCall())
				newOutput = true;
				*/
		}
	}
	
	private boolean isPrimitive(Object o) {
		Class<?> cls = o.getClass();
		return NonreceiverTerm.isNonreceiverType(cls) && !cls.equals(Class.class);
	}

	@Override
	public void initialize(ExecutableSequence executableSequence) {
		newInput = false;
		newOutput = false;
		prevExt = null;
	}

	@Override
	public void visitAfterSequence(ExecutableSequence executableSequence) {
		
	}

}
