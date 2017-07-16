package randoop.util.heapcanonization.fieldextensions;

import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalObject;

public class FieldExtensionsStringsCollector implements FieldExtensionsCollector {
	
	private static final String NULL_REPRESENTATION = "null";

	FieldExtensionsStrings extensions = new FieldExtensionsStrings();
	
	// pre: object cannot be null or primitive.
	private String objectRepresentation(CanonicalObject object) {
		return object.getCanonicalClass().getName() + object.getIndex();
	}

	@Override
	// pre: currObj cannot be null or primitive.
	public void collect(CanonicalField currField, CanonicalObject currObj, CanonicalObject canonicalValue) {
		String fieldStr = currObj.getCanonicalClass().getName() + "." + currField.getName();
		String objStr = objectRepresentation(currObj); 
		
		if (!collectPrimitives() && (canonicalValue.isNull() || canonicalValue.isPrimitive()))
			return;

		String valueStr = "";
		if (canonicalValue.isNull()) 
			valueStr = NULL_REPRESENTATION;
		else if (canonicalValue.isPrimitive())
			valueStr = canonicalValue.getObject().toString();
		else
			// Start enumerating the objects from 1 to handle null=0 well
			valueStr = objectRepresentation(currObj);
		
		extensions.addPairToField(fieldStr, objStr, valueStr);
	}

	boolean collectPrimitives() {
		return true;
	}

	@Override
	public FieldExtensions getExtensions() {
		return extensions;
	}

}
