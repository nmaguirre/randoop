package randoop.fieldextensions;

import java.util.Set;


import randoop.sequence.ExecutableSequence;


public class ExtensionsCollectorOutVisitor extends ExtensionsCollectorInOutVisitor {
	
	public ExtensionsCollectorOutVisitor(Set<String> classesUnderTest, int maxObjects, int maxArrayObjects,
			int maxFieldDistance) {
		super(classesUnderTest, maxObjects, maxArrayObjects, maxFieldDistance);
	}
	
	public ExtensionsCollectorOutVisitor(Set<String> classesUnderTest, int maxObjects, int maxArrayObjects, 
			int maxFieldDistance, boolean preciseObserversDetection, int maxExecsToObs)  {
		super(classesUnderTest, maxObjects, maxArrayObjects, maxFieldDistance, preciseObserversDetection, maxExecsToObs);
	}
	
	@Override
	protected boolean filterInputs() {
		return false;
	}
	
}
