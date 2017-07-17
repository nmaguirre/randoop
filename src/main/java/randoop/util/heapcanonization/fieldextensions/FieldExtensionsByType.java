package randoop.util.heapcanonization.fieldextensions;

import java.util.HashMap;
import java.util.Map;

public class FieldExtensionsByType implements FieldExtensions {
	
	private FieldExtensionsStrings referenceExtensions = new FieldExtensionsStrings();
	private Map<String, FieldExtensionsPrimitiveBitwise> primitiveExtensions = new HashMap<>();
	
	public FieldExtensionsByType() {
		for (PrimitiveType t: PrimitiveType.values()) 
			primitiveExtensions.put(t.toString(), new FieldExtensionsPrimitiveBitwise());
	}

	public void addPairToReferenceField(String fieldStr, String objStr, String valueStr) {
		referenceExtensions.addPairToField(fieldStr, objStr, valueStr);
	}

	public void addPairToPrimitiveField(String typeStr, String fieldStr, String objStr, BitwisePrimitiveValue bitwisePrimitiveValue) {
		FieldExtensionsPrimitiveBitwise currPrimExt = primitiveExtensions.get(typeStr); 
		currPrimExt.addPairToField(fieldStr, objStr, bitwisePrimitiveValue);
	}
	
	
	public String toString() {
		String result = "---------- FIELD EXTENSIONS ----------\n";
		if (!referenceExtensions.isEmpty()) {
			result += "REFERENCE type: \n";
			result += referenceExtensions.toString() + "\n";
		}

		for (String fname: primitiveExtensions.keySet()) {
			if (!primitiveExtensions.get(fname).isEmpty()) {
				result += fname + " type: \n";
				result += primitiveExtensions.get(fname).toString() + "\n";
			}
		}
		result += "----------";
		return result;
	}
}
