package randoop.fieldextensions;

import randoop.generation.ComponentManager;
import randoop.sequence.ExecutableSequence;

public interface ISequenceManager {

	void writeFieldExtensions(String filename, boolean fullExt);

	boolean addGeneratedSequenceToManager(ExecutableSequence eSeq, ComponentManager currMan);

}