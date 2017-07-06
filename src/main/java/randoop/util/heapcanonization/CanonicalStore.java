package randoop.util.heapcanonization;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class CanonicalStore {

	private Map<String, CanonicalClass> classes = new LinkedHashMap<>();
	
	public CanonicalStore(Collection<String> classNames) {
		for (String name: classNames)
			getCanonicalClass(name);
	}
	
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
	
}
