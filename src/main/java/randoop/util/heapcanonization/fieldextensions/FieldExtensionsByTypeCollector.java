package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;

public class FieldExtensionsByTypeCollector implements FieldExtensionsCollector {
	
	private static final String NULL_REPRESENTATION = "null";

	FieldExtensionsByType extensions = new FieldExtensionsByType();
	
	// pre: object cannot be null or primitive.
	private String objectRepresentation(CanonicalObject object) {
		return object.getCanonicalClass().getName() + object.getIndex();
	}

	@Override
	// pre: currObj cannot be null or primitive.
	public void collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
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
			extensions.addPairToPrimitiveField(objType.toString(), fieldStr, objStr, 
					new BitwisePrimitiveValue(canonicalValue.getObject()));
		}
	}

	boolean collectPrimitives() {
		return true;
	}

	@Override
	public FieldExtensions getExtensions() {
		return extensions;
	}

}
