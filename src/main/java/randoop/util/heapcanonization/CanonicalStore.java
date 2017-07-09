package randoop.util.heapcanonization;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CanonicalStore {

	private final Map<String, CanonicalClass> classes = new LinkedHashMap<>();
	private final Set<String> genClasses;
	
	public CanonicalStore(Collection<String> classNames) {
		genClasses = new HashSet<>(classNames);
		List<String> sortedNames = new LinkedList<>(classNames);
		Collections.sort(sortedNames);
		for (String name: sortedNames) 
			getCanonicalClass(name);

		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine("**********");
			CanonizerLog.logLine("Canonical classes:");
			CanonizerLog.logLine(toString("  ")); 
			CanonizerLog.logLine("**********");
		}
	}
	
	public CanonicalClass getCanonicalClass(String name) {
		CanonicalClass res = classes.get(name);
		if (res != null)
			return res;
		
		//System.out.print("New Class: " + name);
		//res = new CanonicalClass(name, this);
		res = new CanonicalClass(name, this);
		classes.put(name, res);

		return res;
	}
	
	public CanonicalClass getCanonicalClass(Class<?> clazz) {
		return getCanonicalClass(clazz.getName());
	}
	
	public CanonicalClass getCanonicalClass(Object o) {
		if (o == null)
			return null;
		return getCanonicalClass(o.getClass().getName());
	}
	
	public Set<String> getAllCanonicalClassnames() {
		return classes.keySet();
	}
	
	public String toString() {
		return toString("");
	}
	
	public String toString(String pad) {
		String res = "";
		for (String name: classes.keySet()) 
			res += pad + classes.get(name).toString() + "\n";
		return res;
	}

	public boolean isGenerationClass(CanonicalClass clazz) {
		return genClasses.contains(clazz.getName());
	}	


}
