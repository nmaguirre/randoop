package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;

public interface FieldExtensionsCollector {
	
	public FieldExtensions getExtensions();

	public void collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue);

}
