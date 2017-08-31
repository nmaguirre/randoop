package randoop.util.heapcanonicalization.fieldextensions;

import randoop.util.heapcanonicalization.CanonicalField;
import randoop.util.heapcanonicalization.CanonicalObject;
import randoop.util.heapcanonicalization.CanonicalizationResult;

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
