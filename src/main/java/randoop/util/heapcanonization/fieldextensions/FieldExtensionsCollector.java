package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;
import randoop.util.heapcanonization.CanonizationResult;

public interface FieldExtensionsCollector {
	
	public FieldExtensions getExtensions();

	public CanonizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue);

}
