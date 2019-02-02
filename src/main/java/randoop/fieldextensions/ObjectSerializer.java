package randoop.fieldextensions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.operation.NonreceiverTerm;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.Statement;


public class ObjectSerializer {
	
	private String serializeClass;
	private String file;
	private ObjectOutputStream oos;

	public ObjectSerializer(String serializeClass, String file) { 
		this.serializeClass = serializeClass;
		this.file = file;
        try {
			oos = new ObjectOutputStream(new FileOutputStream(file));
		} catch (IOException e) {
			throw new Error("Can't create file: " + file);
		}
	}
	
	public void close() {
		try { 
			oos.close();
		} catch (IOException e) {
			throw new Error("Can't close serial file");
		}
	}

	public void serializeObjects(ExecutableSequence sequence) {
		if (!sequence.isNormalExecution()) return;
		
		Set<Integer> fbIndexes = new HashSet<>(sequence.sequence.getFBActiveFlags());

		int lastStmtInd = sequence.sequence.size() -1;
		Statement stmt = sequence.sequence.getStatement(lastStmtInd);
		
		int varIndex = 0;
		if (!stmt.getOutputType().isVoid()) {
			ExecutionOutcome statementResult = sequence.getResult(lastStmtInd);
			Object retVal = ((NormalExecution)statementResult).getRuntimeValue();
			
			if (fbIndexes.contains(varIndex))
				serializeObject(retVal);
			varIndex++;
		}
		
		Object[] objsAfterExec = sequence.getRuntimeInputs(lastStmtInd);
		// Count objects referenced by parameters 
		for (int j = 0; j < objsAfterExec.length; j++) {
			Object curr = objsAfterExec[j];
			
			if (fbIndexes.contains(varIndex))
				serializeObject(curr);
			varIndex++;
		}
	}

	private void serializeObject(Object o) {
		if (o == null || isPrimitive(o))
			return;

		try {
			String cls = o.getClass().getName();
			if (cls.equals(serializeClass))
				oos.writeObject(o);

		} catch (Exception e) {
			throw new Error("Can't serialize object: " + o.toString());
		}
	}
	
	private boolean isPrimitive(Object o) {
		Class<?> cls = o.getClass();
		return NonreceiverTerm.isNonreceiverType(cls) && !cls.equals(Class.class);
	}
	

}
