package randoop.fieldextensions;

import java.util.List;
import java.util.regex.Pattern;

import randoop.generation.ComponentManager;
import randoop.operation.TypedOperation;
import randoop.sequence.ExecutableSequence;

public interface ISequenceManager {

	void writeResults(String filename, boolean fullres);

	boolean addGeneratedSequenceToManager(TypedOperation operation, ExecutableSequence eSeq, ComponentManager currMan, int seqLength);

	void writeBuilders(String filename);

	List<TypedOperation> getBuilders(int seqLength);
	
	public void setOmitFields(Pattern omitfields);
}