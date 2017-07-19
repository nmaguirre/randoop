package randoop.util.heapcanonicalization.fieldextensions;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class FieldExtensionStrings {
	
	private String fieldname;
	private Map<String, Set<String>> extension = new HashMap<String, Set<String>>();
	// private int size;

	public FieldExtensionStrings(String name) {
		fieldname = name;
	}
	
	public boolean addPair(String p1, String p2) {
		Set<String> s = extension.get(p1);
		if (s == null) {
			s = new LinkedHashSet<String>();
			extension.put(p1, s);
		}
		return s.add(p2);

		/*
		boolean added = s.add(p2);
		if (added)
			size++;
		
		return added;
		*/
	}
	
	public boolean containsPair(String p1, String p2) {
		Set<String> s = extension.get(p1);
		if (s == null)
			return false;
		
		return s.contains(p2);
	}

	public String toString() {
		return fieldname + ":" + extension.toString();
	}

	public boolean containsAll(FieldExtensionStrings other) {
		/*
		if (other.size > size)
			return false;
			*/
		
		if (!extension.keySet().containsAll(other.extension.keySet()))
			return false;
		
		for (String field: extension.keySet()) 
			if (other.extension.get(field) != null &&
				!extension.get(field).containsAll(other.extension.get(field)))
				return false;
		
		return true;
	}
	
	/*
	public int size() {
		return size;
	}
	*/

	public boolean addAll(FieldExtensionStrings other) {
		boolean res = false;
		for (String field: other.extension.keySet()) {
			Set<String> currSet = extension.get(field);
			if (currSet == null) {
				currSet = new HashSet<>();
				extension.put(field, currSet);
			}
			res |= currSet.addAll(other.extension.get(field));
		}

		return res;
	}
	
}
