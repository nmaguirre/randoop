package randoop.util.heapcanonicalization;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class CanonicalHeap {

	private final int maxObjects;
	private final Map<CanonicalClass, List<CanonicalObject>> objects;
	private final CanonicalStore store;

	public CanonicalHeap(CanonicalStore store, int maxObjects) {
		this.store = store;
		this.maxObjects = maxObjects;
		objects = new LinkedHashMap<>();
		for (String className: store.getAllCanonicalClassnames()) {
			CanonicalClass canonicalClass = store.getCanonicalClass(className);
			if (!canonicalClass.isPrimitive())
				objects.put(canonicalClass, new LinkedList<CanonicalObject>());
		}
	}

	public Map.Entry<CanonicalizationResult, CanonicalObject> getCanonicalObject(Object obj) {
		// Create a new CanonicalObject encapsulating null
		if (obj == null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, null, -1, this));
		
		// Create or get a new CanonicalObject encapsulating the primitive value 
		CanonicalClass clazz = store.getCanonicalClass(obj.getClass());
		if (clazz.isPrimitive()) 
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, clazz, -1, this));

		// If there is already an object encapsulating obj, return it
		CanonicalObject res = findExistingCanonicalOject(obj, clazz);
		if (res != null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, res);
		
		// If the object limit is exceeded for the current class, report the error
		if (objects.get(clazz).size() >= maxObjects) 
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.LIMITS_EXCEEDED, null);
		
		// Create a new canonical object
		return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, newCanonicalObject(obj, clazz));
	}
	
	private CanonicalObject findExistingCanonicalOject(Object obj, CanonicalClass clazz) {
		// FIXME: Problem with list iterators, we cannot retrieve the objects for the class.
		List<CanonicalObject> clazzObjs = objects.get(clazz);
		/*
		if (clazzObjs == null) {
			clazzObjs = new LinkedList<>();
			objects.put(clazz, clazzObjs);
		}
		*/
		
		for (CanonicalObject currObj: clazzObjs) 
			if (currObj.getObject() == obj) 
				return currObj;
		
		return null;
	}
	
	private CanonicalObject newCanonicalObject(Object obj, CanonicalClass clazz) {
		List<CanonicalObject> clazzObjs = objects.get(clazz);
		CanonicalObject res = new CanonicalObject(obj, clazz, clazzObjs.size(), this);
		clazzObjs.add(res);	
		return res;
	}
	
	public List<CanonicalObject> getObjectsForClass(CanonicalClass clazz) {
		return objects.get(clazz); 
	}
	
	public int getMaxObjects() {
		return maxObjects;
	}
	
	public CanonicalStore getStore() {
		return store;
	}
	
}
