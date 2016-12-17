package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;


/* 
 * class HeapCanonizer
 * 
 * Builds a canonical representation of the heap 
 * and creates field extensions with it
 * 
 * Author: Pablo Ponzio.
 * Date: December 2016.
 */

public class HeapCanonizer {
	// For each class name stores a map of objects with its corresponding indexes in the canonization
	private Map<String, Map<Object, Integer>> store;
	// For each class name stores the last index assigned to an object of the class
	private Map<String, Integer> lastIndex;
	private Map<String, List<Field>> classFields;
	private FieldExtensions extensions;
	private boolean extendedExtensions;
	private boolean ignorePrimitive;
	
	private List<String> ignoredClasses;
	
	
	
	private boolean isIgnoredClass(Class clazz) {
	
		String canonicalName = CanonicalRepresentation.getClassCanonicalName(clazz);
		// Anonymous classes have a null canonicalName, and are considered primitive
		// for the moment
		if (canonicalName == null) 
			throw new RuntimeException("ERROR: Class " + clazz.getName() + "has null name, and this should never happen.");
		
		for (String s: ignoredClasses) {
			if (canonicalName.startsWith(s)) {
				//System.out.println("WARNING: Ignored class: " + canonicalName);				
				return true;
			}
		}

		return false;
	}
	
	
	
	public HeapCanonizer(FieldExtensions extensions) {
		//this(extensions, false);
		this(extensions, true);
	}
	
	public HeapCanonizer(FieldExtensions extensions, boolean ignorePrimitive) {
		classFields = new HashMap<String, List<Field>>();
		this.extensions = extensions;
		this.ignorePrimitive = ignorePrimitive;
		
		ignoredClasses = new LinkedList<String>();
		ignoredClasses.add("java.io.");
		ignoredClasses.add("java.nio.");
		ignoredClasses.add("java.lang.reflect.");
		ignoredClasses.add("java.net.");
		ignoredClasses.add("java.security.");
		//ignoredClasses.add("java.awt.");
		ignoredClasses.add("java.beans.");
		ignoredClasses.add("sun.");
		ignoredClasses.add("com.sun.");
		// ignoredFields.add("java.util.concurrent.");
		
	}
	
	public FieldExtensions getExtensions() {
		return extensions; 
	}

	private Map<Object, Integer> ensureObjectClassIsInStore(String classname) {
		Map<Object, Integer> m = store.get(classname);
		if (m == null) {
			m = new HashMap<Object, Integer>();
			store.put(classname, m);
			lastIndex.put(classname, -1);
		}
		return m;
	}

	// Returns a tuple (old index, new index)
	// if old index == new index then o was stored in a previous call
	// old index is -1 if the object wasn't stored previously
	private Tuple<Integer, Integer> assignIndexToObject(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());
		Map<Object, Integer> m = ensureObjectClassIsInStore(classname);
		Integer res = m.get(o);
		if (res == null) {
			Integer nextIndex = lastIndex.get(classname) + 1;
			m.put(o, nextIndex);
			lastIndex.put(classname, nextIndex);
			return new Tuple<Integer, Integer>(-1, nextIndex);
		}
		return new Tuple<Integer, Integer>(res, res);
	}
	
	// Returns a pair (b1, b2) where b1 iff the extensions were enlarged by this call, and
	// b2 iff tgt is an object visited for the first time in this call
	private Tuple<Boolean, Boolean> addToExtensions(Object src, Object tgt, String fieldname) {
		String srccls = CanonicalRepresentation.getClassCanonicalName(src.getClass());
		Integer srcInd = getObjectIndex(src);
		String srcString = srccls + srcInd;
		
		String tgtString;
		Class tgtClass = (tgt == null) ? null : tgt.getClass();
		boolean isTgtNew = false;
		if (tgt == null)
			tgtString = CanonicalRepresentation.getNullRepresentation();
		else if (isIgnoredClass(tgtClass))
			tgtString = CanonicalRepresentation.getDummyObjectRepresentation();
		else if (CanonicalRepresentation.isClassPrimitive(tgtClass)) {
			if (ignorePrimitive) 
				tgtString = CanonicalRepresentation.getDummyObjectRepresentation();
			else {
				tgtString = tgt.toString();
				// trim string values to length maxStringSize
				if (tgtClass == String.class && tgtString.length() > CanonicalRepresentation.MAX_STRING_SIZE)
					tgtString = tgtString.substring(0, CanonicalRepresentation.MAX_STRING_SIZE);
			}
		}
		else {
			String tgtcls = CanonicalRepresentation.getClassCanonicalName(tgtClass);
			Tuple<Integer, Integer> indt = assignIndexToObject(tgt);
			isTgtNew = indt.getFirst() == -1;
			tgtString = tgtcls + indt.getSecond();
		}
		
		return new Tuple<Boolean, Boolean>(
				extensions.addPairToField(fieldname, srcString, tgtString),
				isTgtNew);
	}
	
	/*
	private boolean addPrimitiveValueToExtensions(Object obj, String fieldname) {
		Class objClass = obj.getClass();
		String srccls = CanonicalRepresentation.getClassCanonicalName(objClass);
		Integer srcInd = getObjectIndex(obj);
		String srcString = srccls + srcInd;
		
		String value = obj.toString();
		// trim string values to length maxStringSize
		if (obj.getClass() == String.class && value.length() > CanonicalRepresentation.MAX_STRING_SIZE) {
			value = value.substring(0, CanonicalRepresentation.MAX_STRING_SIZE);
		}
		
		return extensions.addPairToField(fieldname, srcString, value);
	}*/

	private Integer getObjectIndex(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());	
		Map<Object, Integer> m = store.get(classname);
		if (m == null || m.get(o) == null) return -1;
		return m.get(o);
	}
	
	private void clearStructures() {
		store = new HashMap<String, Map<Object, Integer>>();
		lastIndex = new HashMap<String, Integer>();
	}
	
	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public boolean canonizeAndEnlargeExtensions(Object root) {
		clearStructures();
		extendedExtensions = false;
		
		if (root == null ||
				root.getClass() == Object.class ||	
				isIgnoredClass(root.getClass()) || 
				CanonicalRepresentation.isClassPrimitive(root.getClass()))
			return false;
		
  		LinkedList<Object> toVisit = new LinkedList<Object>();
  		toVisit.addLast(root);
  		assignIndexToObject(root);
  		while (!toVisit.isEmpty()) {
  			Object obj = toVisit.removeFirst();
  			Class objClass = obj.getClass();
  			
/*			if (CanonicalRepresentation.isObjectPrimitive(obj)) {
				// If ignorePrimitive do not store primitive objects
				// if (ignorePrimitive) continue;

				if (addPrimitiveValueToExtensions(obj, CanonicalRepresentation.getPrimitiveFieldCanonicalName(objClass)))
					extendedExtensions = true; 
			}
			else {*/
  			if (objClass.isArray()) {
				// Process an array object. Add a dummy field for each of its elements
				int length = Array.getLength(obj);

				// If ignorePrimitive do not store primitive objects
				/*if (ignorePrimitive && 
						length > 0 &&
						CanonicalRepresentation.isClassPrimitive(obj.getClass().getComponentType())) 
					continue;*/

				
				for (int i = 0; i < length; i++) {
					Object target = Array.get(obj, i);
					// Don't store null values
					// if (target == null) continue;
					
					// If ignorePrimitive do not store primitive objects
					/*if (ignorePrimitive && target != null && CanonicalRepresentation.isObjectPrimitive(target)) 
						continue;*/

					// If target does not belong to the store add it and assign an index to it
					/*if (target != null && getObjectIndex(target) == -1) {
						assignIndexToObject(target);
						toVisit.addLast(target);
					}*/
					
					String fname = CanonicalRepresentation.getArrayFieldCanonicalName(objClass, i);
					Tuple<Boolean, Boolean> addRes = addToExtensions(obj, target, fname);
					
					extendedExtensions |= addRes.getFirst();
					if (addRes.getSecond())
						// target is an object of a non ignored class that was encountered for the first time.
						// Enqueue it for its fields to be inspected in a following iteration.
						toVisit.addLast(target);
				}
			}
			else {
				// Process a non array, non primitive object.
				// Use its field values to enlarge the extensions
				// FIXME: Go back to the unsorted version for better performance
				for (Field fld: getAllClassFieldsSortedByName(objClass)) {
				//for (Field fld: getAllClassFields(objClass)) {
					fld.setAccessible(true);
					Object target;
					try {
						target = fld.get(obj);
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println("FAILURE in field: " + fld.toString());
						// Cannot happen
						throw new RuntimeException("ERROR: Illegal access to an object field during canonization");
					}

					// Don't store null values
					// if (target == null) continue;
	
					// If ignorePrimitive do not store primitive objects
					/* if (ignorePrimitive && target != null && CanonicalRepresentation.isObjectPrimitive(target)) 
						continue;*/
					
					// If target does not belong to the store add it and assign an index to it
					// Don't store null values
					/*
					if (target != null && getObjectIndex(target) == -1) {
						assignIndexToObject(target);
						toVisit.addLast(target);
					}*/
					
					String fname = CanonicalRepresentation.getFieldCanonicalName(fld);
					Tuple<Boolean, Boolean> addRes = addToExtensions(obj, target, fname);
					
					extendedExtensions |= addRes.getFirst();
					if (addRes.getSecond())
						// target is an object of a non ignored class that was encountered for the first time.
						// Enqueue it for its fields to be inspected in a following iteration.
						toVisit.addLast(target);
					
					// System.out.println(CanonicalRepresentation.getFieldCanonicalName(fld));
				}
			}
		}

  		return extendedExtensions;
	}
	
	
	// Returns true iff the last canonization enlarged the extensions
	public boolean extensionsExtended() {
		return extendedExtensions;
	}
	
	/*
	// Returns the list with cls' fields. It stores the fields 
	// the first time to return them in the same order later.
	private List<Field> getAllClassFields(Class cls) {
		String classname = CanonicalRepresentation.getClassCanonicalName(cls);
		List<Field> clsFields = classFields.get(classname);
		if (clsFields != null) return clsFields;
		
		clsFields = new LinkedList<Field>();
		while (cls != null && 
				cls != Object.class && 
				!CanonicalRepresentation.isPrimitive(cls)) {
						
			Field [] fieldsArray = cls.getDeclaredFields();
			for (int i = 0; i < fieldsArray.length; i++) 
				clsFields.add(fieldsArray[i]);

			cls = cls.getSuperclass();	
		}

		classFields.put(classname, clsFields);
		return clsFields;
	}*/
	
	
	// Returns the list with cls' fields. It stores the fields 
	// the first time to return them in the same order later.
	private List<Field> getAllClassFieldsSortedByName(Class cls) {
		String classname = CanonicalRepresentation.getClassCanonicalName(cls);
		
		List<Field> clsFields = classFields.get(classname);
		if (clsFields != null) return clsFields;

		clsFields = new LinkedList<Field>();
		while (cls != null && 
				cls != Object.class && 
				!CanonicalRepresentation.isClassPrimitive(cls) &&
				!isIgnoredClass(cls)) {
						
			Field [] fieldsArray = cls.getDeclaredFields();
			
			for (int i = 0; i < fieldsArray.length; i++) {
				Field f = fieldsArray[i];
				
				// FIXME: Cache fields in the ResourceBundle java class break the 
				// field exhaustive generation. Find a better way to detect these fields.
				/*
				if (f.getName().toLowerCase().contains("cache")) {
					System.out.println("WARNING: Ignored cache field: "  + CanonicalRepresentation.getFieldCanonicalName(f));
					continue;
				}
				*/
				//System.out.println(f.toString());
				//System.out.println(f.getType());
				// If ignorePrimitive do not consider primitive fields
				
				Class ftype = f.getType();
				if (isIgnoredClass(ftype))
					continue;
				
				clsFields.add(f);
			}
				
			cls = cls.getSuperclass();
		}
		
		// System.out.println(">>Result: " + clsFields);

		//System.out.println(clsFields);
		Collections.sort(clsFields, new FieldByNameComp());
		//clsFields.sort(new FieldByNameComp());
		//System.out.println(clsFields);
		
		classFields.put(classname, clsFields);
		return clsFields;
	}
}
