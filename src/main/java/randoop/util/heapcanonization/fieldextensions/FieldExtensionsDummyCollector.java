package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;
import randoop.util.heapcanonization.CanonizationResult;

public class FieldExtensionsDummyCollector implements FieldExtensionsCollector {

	@Override
	public CanonizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
		return CanonizationResult.OK;
	}

	@Override
	public FieldExtensions getExtensions() {
		return null;
	}

}
