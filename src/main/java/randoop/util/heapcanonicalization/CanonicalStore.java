package randoop.util.heapcanonicalization;

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
	private final int maxFieldDistance;
	
	public CanonicalStore(Collection<String> classNames) {
		this(classNames, Integer.MAX_VALUE);
	}

	public CanonicalStore(Collection<String> classNames, int maxFieldDistance) {
		this.maxFieldDistance = maxFieldDistance;
		genClasses.addAll(classNames);
		List<String> sortedNames = new LinkedList<>(classNames);
		sortedNames.add(DummyHeapRoot.class.getName());
		Collections.sort(sortedNames);
		for (String name: sortedNames) 
			getUpdateOrCreateCanonicalClass(name, 0);
	}
	
	
	/*
	public CanonicalClass getCanonicalClass(String name) {
		CanonicalClass res = classes.get(name);
		if (res != null) {
			return res;
		}

		res = new CanonicalClass(name, this, 0, maxFieldDistance);
		classes.put(name, res);
		return res;
	}
	*/
	
	public CanonicalClass getCanonicalClass(String name) {
		CanonicalClass cls = classes.get(name);
		assert cls != null : "This method should be called for existing canonical classes only";
		return cls;
	}
	
	public CanonicalClass getCanonicalClass(Class<?> clazz) {
		return getCanonicalClass(clazz.getName());
	}
	
	public CanonicalClass getUpdateOrCreateCanonicalClass(String name, int fieldDistance) {
		assert fieldDistance <= maxFieldDistance: "Field distance should never be greater than: " + maxFieldDistance;
		CanonicalClass res = classes.get(name);
		if (res != null) {
			if (fieldDistance < maxFieldDistance)
				res.updateFieldDistance(fieldDistance, maxFieldDistance);
			return res;
		}

		res = new CanonicalClass(name, this, fieldDistance, maxFieldDistance);
		classes.put(name, res);
		return res;
	}
	
	public CanonicalClass getUpdateOrCreateCanonicalClass(Class<?> clazz, int fieldDistance) {
		return getUpdateOrCreateCanonicalClass(clazz.getName(), fieldDistance);
	}


/*	
	protected CanonicalClass getCanonicalClass(String name, int fieldDistance) {
		CanonicalClass res = classes.get(name);
		if (res != null) {
			if (fieldDistance < maxFieldDistance)
				res.updateFieldDistance(fieldDistance);
			return res;
		}

		res = new CanonicalClass(name, this, fieldDistance, maxFieldDistance);
		classes.put(name, res);
		return res;
	}
	
	public CanonicalClass getCanonicalClass(Class<?> clazz, int fieldDistance) {
		return getCanonicalClass(clazz.getName(), fieldDistance);
	}
	
	/*
	public CanonicalClass getCanonicalClass(Object o) {
		if (o == null)
			return null;
		return getCanonicalClass(o.getClass().getName());
	}
	*/
	
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

	public boolean isGenerationClass(CanonicalClass clazz) {
		return genClasses.contains(clazz.getName());
	}	

	/*
	public CanonicalStore clone() {
		CanonicalStore res = new CanonicalStore();
		res.classes.putAll(classes);
		res.genClasses.addAll(genClasses);
		return res;
	}	
	*/
	
}
