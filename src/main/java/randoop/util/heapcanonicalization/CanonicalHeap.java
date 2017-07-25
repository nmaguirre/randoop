package randoop.util.heapcanonicalization;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorsWriter;


public class CanonicalHeap {

	private final int maxObjects;
	private final int maxArrayObjs;
	private final Map<CanonicalClass, List<CanonicalObject>> objects;
	private final CanonicalStore store;

	public CanonicalHeap(CanonicalStore store, int maxObjects) {
		this(store, maxObjects, Integer.MAX_VALUE);
	}
	
	public CanonicalHeap(CanonicalStore store, int maxObjects, int maxArrayObjs) {
		this.store = store;
		this.maxObjects = maxObjects;
		this.maxArrayObjs = maxArrayObjs;
		objects = new LinkedHashMap<>();
		/*
		if (CandidateVectorsWriter.isEnabled()) {
			// Candidate vectors need heaps with the shape of the classes provided
			// FIXME: Maybe not?
			for (String className: store.getAllCanonicalClassnames()) {
				CanonicalClass canonicalClass = store.getCanonicalClass(className);
				if (!canonicalClass.isPrimitive())
					objects.put(canonicalClass, new LinkedList<CanonicalObject>());
			}
		}
		*/
	}
	
	
	public Map.Entry<CanonicalizationResult, CanonicalObject> getCanonicalObject(Object obj) {
		if (obj == null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, null, -1, this));
		CanonicalClass cls = store.getCanonicalClass(obj.getClass());
		assert cls != null : "This method should be called for existing canonical classes only";
		return getExistingOrCreateCanonicalObject(obj,cls);
	}
	

	public Map.Entry<CanonicalizationResult, CanonicalObject> getExistingOrCreateCanonicalObject(Object obj, CanonicalClass clazz) {
		// Create a new CanonicalObject encapsulating null
		if (obj == null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, null, -1, this));
		// Get, update or create a new CanonicalObject encapsulating the primitive value 
		if (clazz.isPrimitive()) 
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, clazz, -1, this));
		// If there is already an object encapsulating obj, return it
		CanonicalObject res = findExistingCanonicalObject(obj, clazz);
		if (res != null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, res);
		// If the object limit is exceeded for the current class, report the error
		if (objects.get(clazz).size() >= maxObjects) 
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.LIMITS_EXCEEDED, null);
		// Create a new canonical object
		return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, newCanonicalObject(obj, clazz));
	}
	
	private CanonicalObject findExistingCanonicalObject(Object obj, CanonicalClass clazz) {
		List<CanonicalObject> clazzObjs = objects.get(clazz);
		// Some classes are found out at runtime, for example, when they are the return type of a method.
		if (clazzObjs == null) {
			objects.put(clazz, new LinkedList<CanonicalObject>());
			return null;
		}
		
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

	public int getMaxArrayObjects() {
		return maxArrayObjs;
	}
	
}
