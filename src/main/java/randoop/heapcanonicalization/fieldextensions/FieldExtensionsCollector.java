package randoop.heapcanonicalization.fieldextensions;

import randoop.heapcanonicalization.CanonicalField;
import randoop.heapcanonicalization.CanonicalObject;
import randoop.heapcanonicalization.CanonicalizationResult;

public interface FieldExtensionsCollector {
	
	public FieldExtensions getExtensions();

	public CanonicalizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue);

	public void initializeExtensions();

}
