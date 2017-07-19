package randoop.util.heapcanonicalization.fieldextensions;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class FieldExtensionPrimitiveBinary {
	
	private String fieldname;
	private Map<String, BinaryPrimitiveValue> extension = new HashMap<>();

	public FieldExtensionPrimitiveBinary(String name) {
		fieldname = name;
	}
	
	public boolean addPair(String p1, BinaryPrimitiveValue p2) {
		BinaryPrimitiveValue s = extension.get(p1);
		if (s == null) {
			extension.put(p1, p2);
			return true;
		}
		
		return s.union(p2);
	}
	
	public boolean containsPair(String p1, BinaryPrimitiveValue p2) {
		BinaryPrimitiveValue s = extension.get(p1);
		if (s == null)
			return false;
		
		return s.contains(p2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((fieldname == null) ? 0 : fieldname.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldExtensionPrimitiveBinary other = (FieldExtensionPrimitiveBinary) obj;
		if (fieldname == null) {
			if (other.fieldname != null)
				return false;
		} else if (!fieldname.equals(other.fieldname))
			return false;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		return true;
	}

	public String toString() {
		return fieldname + ":" + extension.toString();
	}


	public boolean containsAll(FieldExtensionPrimitiveBinary other) {
		/*
		if (other.size > size)
			return false;
			*/
		
		if (!extension.keySet().containsAll(other.extension.keySet()))
			return false;
		
		for (String field: extension.keySet()) 
			if (other.extension.get(field) != null &&
				!extension.get(field).contains(other.extension.get(field)))
				return false;
		
		return true;
	}
	
	/*
	public int size() {
		return size;
	}
	*/

	public boolean addAll(FieldExtensionPrimitiveBinary other) {
		boolean res = false;
		for (String field: other.extension.keySet()) {
			BinaryPrimitiveValue currVal = extension.get(field);
			if (currVal == null) {
				currVal = new BinaryPrimitiveValue();
				extension.put(field, currVal);
			}
			res |= currVal.union(other.extension.get(field));
		}

		return res;
	}
	
	
}
