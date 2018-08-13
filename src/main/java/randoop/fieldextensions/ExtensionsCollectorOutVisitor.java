package randoop.fieldextensions;

import java.util.Set;


import randoop.sequence.ExecutableSequence;


public class ExtensionsCollectorOutVisitor extends ExtensionsCollectorInOutVisitor {
	
	public ExtensionsCollectorOutVisitor(Set<String> classesUnderTest, int maxObjects, int maxArrayObjects,
			int maxFieldDistance) {
		super(classesUnderTest, maxObjects, maxArrayObjects, maxFieldDistance);
	}

	@Override
	public void visitBeforeStatement(ExecutableSequence sequence, int i) {

	}
	
}
