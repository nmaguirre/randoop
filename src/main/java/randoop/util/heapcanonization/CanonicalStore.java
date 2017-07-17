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
	private final Set<String> genClasses = new HashSet<>();
	
	public CanonicalStore(Collection<String> classNames) {
		genClasses.addAll(classNames);
		
		List<String> sortedNames = new LinkedList<>(classNames);
		sortedNames.add(DummyHeapRoot.class.getName());
		Collections.sort(sortedNames);
		for (String name: sortedNames) 
			getCanonicalClass(name);

		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine(toPrettyString());
		}
	}
	
	private CanonicalStore() { };

	public CanonicalClass getCanonicalClass(String name) {
		CanonicalClass res = classes.get(name);
		if (res != null)
			return res;
		
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
	
	public String toString(String padding) {
		String res = "";
		for (String name: classes.keySet()) 
			res += padding + classes.get(name).toString() + "\n";
		return res;
	}

	public String toPrettyString() {
		String res = "**********\n";
		res += "Canonical classes:\n";
		res += toString("") + "\n";
		res += "**********";
		return res;
	}
	
	public boolean isGenerationClass(CanonicalClass clazz) {
		return genClasses.contains(clazz.getName());
	}	

	public CanonicalStore clone() {
		CanonicalStore res = new CanonicalStore();
		res.classes.putAll(classes);
		res.genClasses.addAll(genClasses);
		return res;
	}	
	
}
