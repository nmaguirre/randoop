package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;
import randoop.util.heapcanonization.CanonizationResult;

public class FieldExtensionsByTypeCollector implements FieldExtensionsCollector {
	
	private static final String NULL_REPRESENTATION = "null";
	private int maxStrLen;
	
	public FieldExtensionsByTypeCollector(int maxStrLen) {
		this.maxStrLen = maxStrLen;
	}

	FieldExtensionsByType extensions = new FieldExtensionsByType();
	
	// pre: object cannot be null or primitive.
	private String objectRepresentation(CanonicalObject object) {
		return object.getCanonicalClass().getName() + object.getIndex();
	}

	@Override
	// pre: currObj cannot be null or primitive.
	public CanonizationResult collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
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
			PrimitiveType objType = PrimitiveType.fromObject(canonicalValue.getObject());
			if (objType == PrimitiveType.STRING && ((String)canonicalValue.getObject()).length() > maxStrLen)
				return CanonizationResult.STRING_LIMITS_EXCEEDED;
			
			extensions.addPairToPrimitiveField(objType.toString(), fieldStr, objStr, 
					new BinaryPrimitiveValue(canonicalValue.getObject()));
		}
		
		return CanonizationResult.OK;
	}

	boolean collectPrimitives() {
		return true;
	}

	@Override
	public FieldExtensions getExtensions() {
		return extensions;
	}

}
