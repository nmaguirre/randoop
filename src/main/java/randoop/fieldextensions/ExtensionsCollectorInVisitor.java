package randoop.fieldextensions;

import java.util.Set;


import randoop.sequence.ExecutableSequence;


public class ExtensionsCollectorInVisitor extends ExtensionsCollectorInOutVisitor {
	
	public ExtensionsCollectorInVisitor(Set<String> classesUnderTest, int maxObjects, int maxArrayObjects,
			int maxFieldDistance) {
		super(classesUnderTest, maxObjects, maxArrayObjects, maxFieldDistance);
	}
	
	@Override
	public void visitAfterStatement(ExecutableSequence sequence, int i) {

	}
	
}
