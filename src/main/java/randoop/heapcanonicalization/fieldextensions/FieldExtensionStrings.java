package randoop.heapcanonicalization.fieldextensions;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;


public class FieldExtensionStrings {
	
	private String fieldname;
	private Map<String, Set<String>> extension = new LinkedHashMap<String, Set<String>>();
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
		
		for (String field: other.extension.keySet()) 
			if (!extension.get(field).containsAll(other.extension.get(field)))
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
				currSet = new LinkedHashSet<>();
				extension.put(field, currSet);
			}
			res |= currSet.addAll(other.extension.get(field));
		}

		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldname == null) ? 0 : fieldname.hashCode());
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
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
		FieldExtensionStrings other = (FieldExtensionStrings) obj;
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

	public Set<String> getValues(String object) {
		return extension.get(object);
	}
	
	
	public int size() {
		int size = 0;
		for (String key: extension.keySet()) {
			Set<String> s = extension.get(key);
			if (s != null)
				size += s.size();
		}
		return size;
	}
	
}
