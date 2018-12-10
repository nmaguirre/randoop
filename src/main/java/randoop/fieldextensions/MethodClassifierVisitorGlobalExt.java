package randoop.fieldextensions;

import java.io.FileWriter;
import java.io.IOException;

import canonicalizer.BFHeapCanonicalizer;
import extensions.BoundedFieldExtensionsCollector;
import extensions.FieldExtensionsCollector;
import extensions.IFieldExtensions;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class MethodClassifierVisitorGlobalExt implements ExecutionVisitor {
	
	private BFHeapCanonicalizer canonicalizer;
	private ExtensionsStore outputExt;
	private OperationManagerGlobalExt opManager;
	private IFieldExtensions [] prevExt;
	private int maxObjects;
	private int maxArrayObjects;
	private int maxFieldDistance;
	
	// classesUnderTest = null to consider all classes as relevant
	public MethodClassifierVisitorGlobalExt() {
		this.maxObjects = GenInputsAbstract.fbg_max_objects;
		this.maxArrayObjects = GenInputsAbstract.fbg_max_arr_objects;
		this.maxFieldDistance = GenInputsAbstract.fbg_max_field_distance;
		canonicalizer = new BFHeapCanonicalizer();
		canonicalizer.setMaxArrayObjs(maxArrayObjects);
		canonicalizer.setMaxFieldDistance(maxFieldDistance);
		outputExt = new ExtensionsStore(maxObjects);
		opManager = new OperationManagerGlobalExt();
		opManager.setMinExecutionsToModifier(GenInputsAbstract.min_execs_to_modifier);
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
		/*
		if (sequence.sequence.size() == 1 || i != sequence.sequence.size() -1) return;
		
		Statement stmt = sequence.sequence.getStatement(i);
		TypedOperation op = stmt.getOperation();
		// FIXME: var v = class.field are not method calls
		//assert op.isConstructorCall() || op.isMethodCall(): op.toParsableString() + "is not a constructor or method call";
		String className = Utils.getOperationClass(op);
		// We only want to see if methods of classes under test are exercised with new objects
		// For the remaining classes we drop any other sequence that is not a generator
		if (!Utils.classUnderTest(className)) return;

		Object[] inputs = sequence.getRuntimeInputs(i);
		prevExt = new IFieldExtensions [inputs.length];
		for (int j = 0; j < inputs.length; j++) {
			FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
			canonicalizer.canonicalize(inputs[j], currExtCollector);
			prevExt[j] = currExtCollector.getExtensions();
		}
		*/
	}

	@Override
	public void visitAfterStatement(ExecutableSequence sequence, int i) {
		if (i != sequence.sequence.size() -1) return;

		ExecutionOutcome statementResult = sequence.getResult(i);	
		TypedOperation op = sequence.sequence.getStatement(i).getOperation();
		String className = Utils.getOperationClass(op);
		
		if (!Utils.classUnderTest(className)) return;
		
		boolean extended = false;
		if (statementResult instanceof NormalExecution) {
			opManager.executed(op);
			// Check whether the result value generates new objects
			Statement stmt = sequence.sequence.getStatement(i);
			if (!stmt.getOutputType().isVoid()) {
				Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
				if (retVal != null && !Utils.isPrimitive(retVal)) {
					FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(retVal.getClass().getName());
					// makes extensionsWereExtended default to false
					collector.start();
					canonicalizer.canonicalize(retVal, collector);
					extended |= collector.extensionsWereExtended();
				}
			}
			
			if (!extended) {
				Object[] objsAfterExec = sequence.getRuntimeInputs(i);
				// Check whether the objects referenced by parameters are modified by the execution
				for (int j = 0; j < objsAfterExec.length; j++) {
					Object curr = objsAfterExec[j];
					if (curr == null || Utils.isPrimitive(curr)) continue;
					FieldExtensionsCollector collector = outputExt.getOrCreateCollectorForMethodParam(curr.getClass().getName());
					// makes extensionsWereExtended default to false
					collector.start();
					canonicalizer.canonicalize(curr, collector);
					extended |= collector.extensionsWereExtended();
				}
				/*
				Object[] objsAfterExec = sequence.getRuntimeInputs(i);
				// Check whether the objects referenced by parameters are modified by the execution
				for (int j = 0; j < objsAfterExec.length; j++) {
					Object curr = objsAfterExec[j];
					FieldExtensionsCollector currExtCollector = new BoundedFieldExtensionsCollector(maxObjects);
					canonicalizer.canonicalize(curr, currExtCollector);
					if (!prevExt[j].equals(currExtCollector.getExtensions())) {
						extended = true;
						break;
					}
				}
				*/
			}
			
			if (extended)
				opManager.setModifier(op);
		}
	}

	@Override
	public void initialize(ExecutableSequence executableSequence) {
		prevExt = null;
	}

	@Override
	public void visitAfterSequence(ExecutableSequence executableSequence) {
		
	}
	

	@Override
	public void doOnTermination() {
		if (GenInputsAbstract.method_classification_res == null) 
			throw new IllegalStateException("Set --method-classification to a valid file before using " + MethodClassifierVisitorGlobalExt.class.getName());

		try {
			FileWriter writer = new FileWriter(GenInputsAbstract.method_classification_res);
			writer.write(opManager.toString());
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
