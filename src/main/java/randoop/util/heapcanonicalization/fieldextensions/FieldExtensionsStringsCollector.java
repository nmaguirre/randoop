package randoop.util.heapcanonicalization.fieldextensions;

import randoop.util.heapcanonicalization.CanonicalField;
import randoop.util.heapcanonicalization.CanonicalObject;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;

public class FieldExtensionsStringsCollector implements FieldExtensionsCollector {
	
	private static final String NULL_REPRESENTATION = "null";
	private final int maxStrLen;
	private FieldExtensionsStrings extensions = new FieldExtensionsStrings();
	
	public FieldExtensionsStringsCollector() {
		this(Integer.MAX_VALUE);
	}

	public FieldExtensionsStringsCollector(int maxStrLen) {
		this.maxStrLen = maxStrLen;
	}
	
	// pre: object cannot be null or primitive.
	/*
	private String objectRepresentation(CanonicalObject object) {
		return object.getCanonicalClass().getName() + object.getIndex();
	}
	*/

	@Override
	// pre: currObj cannot be null or primitive.
	public CanonicalizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
		String fieldStr = currObj.getCanonicalClass().getName() + "." + currField.getName();
		String objStr = currObj.stringRepresentation(); 
		
		if (!collectPrimitives() && (canonicalValue.isNull() || canonicalValue.isPrimitive()))
			return CanonicalizationResult.OK;

		String valueStr = "";
		if (canonicalValue.isNull()) 
			valueStr = NULL_REPRESENTATION;
		else if (canonicalValue.isPrimitive()) {
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
			valueStr = val.toString();
		}
		else
			valueStr = canonicalValue.stringRepresentation();
		
		extensions.addPairToField(fieldStr, objStr, valueStr);
		
		return CanonicalizationResult.OK;
	}

	boolean collectPrimitives() {
		return true;
	}

	@Override
	public FieldExtensions getExtensions() {
		return extensions;
	}

}
