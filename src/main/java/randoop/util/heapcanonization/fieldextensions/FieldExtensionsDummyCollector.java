package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;

public class FieldExtensionsDummyCollector implements FieldExtensionsCollector {

	@Override
	public void collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
	}

	@Override
	public FieldExtensions getExtensions() {
		return null;
	}

}
