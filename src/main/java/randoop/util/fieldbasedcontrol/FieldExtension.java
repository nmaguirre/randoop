package randoop.util.fieldbasedcontrol;

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
	
	public String toString() {
		return fieldname + ":" + extension.toString();
	}
	
}