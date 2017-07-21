package randoop.util.heapcanonicalization.fieldextensions;

import java.util.HashMap;
import java.util.Map;

public class FieldExtensionsByType implements FieldExtensions {
	
	private FieldExtensionsStrings referenceExtensions = new FieldExtensionsStrings();
	private Map<String, FieldExtensionsPrimitiveBinary> primitiveExtensions = new HashMap<>();
	
	public void addPairToReferenceField(String fieldStr, String objStr, String valueStr) {
		referenceExtensions.addPairToField(fieldStr, objStr, valueStr);
	}

	public void addPairToPrimitiveField(String typeStr, String fieldStr, String objStr, BinaryPrimitiveValue bitwisePrimitiveValue) {
		FieldExtensionsPrimitiveBinary currPrimExt = primitiveExtensions.get(typeStr); 
		if (currPrimExt == null) {
			currPrimExt = new FieldExtensionsPrimitiveBinary();
			primitiveExtensions.put(typeStr, currPrimExt);
		}
		currPrimExt.addPairToField(fieldStr, objStr, bitwisePrimitiveValue);
	}
	
	public String toString() {
		String result = "";
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
		return result;
	}

	@Override
	public boolean addAll(FieldExtensions other) {
		FieldExtensionsByType otherExt = (FieldExtensionsByType) other;
		boolean res = false;
		res |= referenceExtensions.addAll(otherExt.referenceExtensions);
		for (String type: otherExt.primitiveExtensions.keySet()) {
			FieldExtensionsPrimitiveBinary currPrimExt = primitiveExtensions.get(type); 
			if (currPrimExt == null) {
				currPrimExt = new FieldExtensionsPrimitiveBinary();
				primitiveExtensions.put(type, currPrimExt);
			}
			res |= primitiveExtensions.get(type).addAll(otherExt.primitiveExtensions.get(type));
		}
		
		return res;
	}

	@Override
	public boolean containsAll(FieldExtensions other) {
		FieldExtensionsByType otherExt = (FieldExtensionsByType) other;
		if (!primitiveExtensions.keySet().containsAll(otherExt.primitiveExtensions.keySet()))
			return false;

		if (!referenceExtensions.containsAll(otherExt.referenceExtensions))
			return false;
		
		for (String type: primitiveExtensions.keySet()) 
			if (otherExt.primitiveExtensions.get(type) != null &&
				!primitiveExtensions.get(type).containsAll(otherExt.primitiveExtensions.get(type)))
				return false;
		
		return true;
	}


}
