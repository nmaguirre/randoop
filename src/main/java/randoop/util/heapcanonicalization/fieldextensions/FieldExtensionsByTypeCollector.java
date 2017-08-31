package randoop.util.heapcanonicalization.fieldextensions;

import randoop.util.heapcanonicalization.CanonicalField;
import randoop.util.heapcanonicalization.CanonicalObject;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;

public class FieldExtensionsByTypeCollector implements FieldExtensionsCollector {
	
	private static final String NULL_REPRESENTATION = "null";
	private final int maxStrLen;
	private FieldExtensionsByType extensions = new FieldExtensionsByType();
	
	public FieldExtensionsByTypeCollector() {
		this(Integer.MAX_VALUE);
	}

	public FieldExtensionsByTypeCollector(int maxStrLen) {
		this.maxStrLen = maxStrLen;
	}
	
	// pre: object cannot be null or primitive.
	private String objectRepresentation(CanonicalObject object) {
		return object.getCanonicalClass().getName() + object.getIndex();
	}

	@Override
	// pre: currObj cannot be null or primitive.
	public CanonicalizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
		String fieldStr = currObj.getCanonicalClass().getName() + "." + currField.getName();
		String objStr = objectRepresentation(currObj); 
		
		/*
		if (!collectPrimitives() && (canonicalValue.isNull() || canonicalValue.isPrimitive()))
			return;
		*/

		if (canonicalValue.isNull() || !canonicalValue.isPrimitive()) {
			String valueStr = "";
			if (canonicalValue.isNull()) 
				valueStr = NULL_REPRESENTATION;
			else
				valueStr = objectRepresentation(canonicalValue);
			
			extensions.addPairToReferenceField(fieldStr, objStr, valueStr);
		}
		else { 
			// Canonical value is primitive
			Object val = canonicalValue.getObject();
			PrimitiveType objType = PrimitiveType.fromObject(val);
			if (objType == PrimitiveType.STRING) {
				String str = (String) val;
				if (str.length() > maxStrLen) {
					// For now, do not throw an error and trim the string up to the given limit.
					//return CanonicalizationResult.STRING_LIMITS_EXCEEDED;
					if (CanonicalizerLog.isLoggingOn())
						CanonicalizerLog.log("CANONICALIZER INFO: Trimmed string from size " + str.length() + " to " + maxStrLen);
					val = str.substring(0, maxStrLen);
				}
			}
			
			extensions.addPairToPrimitiveField(objType.toString(), fieldStr, objStr, 
					new BinaryPrimitiveValue(val));
		}
		
		return CanonicalizationResult.OK;
	}

	boolean collectPrimitives() {
		return true;
	}

	@Override
	public FieldExtensions getExtensions() {
		return extensions;
	}

	public void initializeExtensions() {
		// TODO Auto-generated method stub
	}

}
