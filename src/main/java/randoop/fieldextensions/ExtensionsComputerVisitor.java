package randoop.fieldextensions;

import java.io.FileWriter;
import java.io.IOException;

import canonicalizer.BFHeapCanonicalizer;
import extensions.FieldExtensionsCollector;
import randoop.ExecutionOutcome;
import randoop.ExecutionVisitor;
import randoop.NormalExecution;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class ExtensionsComputerVisitor implements ExecutionVisitor {
	
	private BFHeapCanonicalizer canonicalizer;
	private ExtensionsStore outputExt;
	private int maxObjects;
	private int maxArrayObjects;
	private int maxFieldDistance;
	private int maxBFDepth;
	
	// classesUnderTest = null to consider all classes as relevant
	public ExtensionsComputerVisitor() {
		this.maxObjects = GenInputsAbstract.fbg_max_objects;
		this.maxArrayObjects = GenInputsAbstract.fbg_max_arr_objects;
		this.maxFieldDistance = GenInputsAbstract.fbg_max_field_distance;
		this.maxBFDepth = GenInputsAbstract.fbg_max_bf_depth;
		canonicalizer = new BFHeapCanonicalizer();
		canonicalizer.setMaxArrayObjs(maxArrayObjects);
		canonicalizer.setMaxFieldDistance(maxFieldDistance);
		canonicalizer.setMaxBFDepth(maxBFDepth);
		if (GenInputsAbstract.max_stopping_objects > 0) {
			canonicalizer.setMaxObjects(GenInputsAbstract.max_stopping_objects);
			canonicalizer.setStopOnError();
		}
		outputExt = new ExtensionsStore(maxObjects);
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

			// Check if we should canonicalize or not
			if (GenInputsAbstract.max_stopping_objects > 0) {

				Statement stmt = sequence.sequence.getStatement(i);
				if (!stmt.getOutputType().isVoid()) {
					Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
					if (retVal != null && !Utils.isPrimitive(retVal)) {
						FieldExtensionsCollector col = new FieldExtensionsCollector();
						if (!canonicalizer.canonicalize(retVal, col))
							throw new VisitorException();
					}
				}

				Object[] objsAfterExec = sequence.getRuntimeInputs(i);
				// Check whether the objects referenced by parameters are modified by the execution
				for (int j = 0; j < objsAfterExec.length; j++) {
					Object curr = objsAfterExec[j];
					if (curr == null || Utils.isPrimitive(curr)) continue;

					FieldExtensionsCollector col = new FieldExtensionsCollector();
					if (!canonicalizer.canonicalize(curr, col))
						throw new VisitorException();
				}
			}
			
			// Object sizes ok, really canonicalize now
			boolean extended = false;
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
			
			if (GenInputsAbstract.field_based_gen && !extended)
				throw new VisitorException();
				
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
