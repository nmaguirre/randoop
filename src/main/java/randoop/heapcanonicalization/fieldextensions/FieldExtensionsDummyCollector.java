package randoop.heapcanonicalization.fieldextensions;

import randoop.heapcanonicalization.CanonicalField;
import randoop.heapcanonicalization.CanonicalObject;
import randoop.heapcanonicalization.CanonicalizationResult;

public class FieldExtensionsDummyCollector implements FieldExtensionsCollector {

	@Override
	public CanonicalizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
		return CanonicalizationResult.OK;
	}

	@Override
	public FieldExtensions getExtensions() {
		return null;
	}

	@Override
	public void initializeExtensions() {
		// TODO Auto-generated method stub
		
	}

}
