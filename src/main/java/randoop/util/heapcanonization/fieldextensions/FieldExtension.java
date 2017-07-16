package randoop.util.heapcanonization.fieldextensions;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;


public class FieldExtension {
	
	private String fieldname;
	private Map<String, Set<String>> extension = new HashMap<String, Set<String>>();

	public FieldExtension(String name) {
		fieldname = name;
	}
	
	public boolean addPair(String p1, String p2) {
		Set<String> s = extension.get(p1);
		if (s == null) {
			s = new LinkedHashSet<String>();
			extension.put(p1, s);
		}
		
		return s.add(p2);
	}
	
	public boolean containsPair(String p1, String p2) {
		Set<String> s = extension.get(p1);
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
		FieldExtension other = (FieldExtension) obj;
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
	
}
