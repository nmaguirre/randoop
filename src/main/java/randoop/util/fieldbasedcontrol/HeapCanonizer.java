package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
	
	public HeapCanonizer(FieldExtensions ext) {
		store = new HashMap<String, Map<Object, Integer>>();
		lastIndex = new HashMap<String, Integer>();
		classFields = new HashMap<String, List<Field>>();
		extensions = ext;
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

	private Integer assignIndexToObject(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());
		Map<Object, Integer> m = ensureObjectClassIsInStore(classname);
		Integer res = m.get(o);
		if (res == null) {
			Integer nextIndex = lastIndex.get(classname) + 1;
			m.put(o, nextIndex);
			lastIndex.put(classname, nextIndex);
			return nextIndex;			
		}
		return res;
	}
	
	// src is already in the store (and has an assigned index), tgt might or not be 
	// in the store (and might or not be assigned an index, respectively)
	// Returns a tuple (a,b) with: 
	//		a iff tgt is already in the store 
	//		b iff the extensions were extended
	private Tuple<Boolean, Boolean> addToExtensions(Object src, Object tgt, String fieldname) {
		String srccls = CanonicalRepresentation.getClassCanonicalName(src.getClass());
		Integer srcInd = getObjectIndex(src);
		String srcString = srccls + srcInd;
		
		boolean tgtInStore;
		String tgtString;
		if (tgt != null) {
			String tgtcls = CanonicalRepresentation.getClassCanonicalName(tgt.getClass());
			// Assign indexes to the objects
			Integer oldTgtInd = getObjectIndex(tgt);
			tgtInStore = oldTgtInd != -1; 
			Integer tgtInd = assignIndexToObject(tgt);
			tgtString = tgtcls + tgtInd;
		}
		else {
			// return true as if null was present in the store, so it is never added to it
			tgtInStore = true;
			tgtString = CanonicalRepresentation.getNullRepresentation();
		}
		
		boolean extensionsExtended = extensions.addPairToField(fieldname, srcString, tgtString);
		// Enlarge the extensions
		return new Tuple<Boolean, Boolean>(tgtInStore, extensionsExtended);
	}

	private Integer getObjectIndex(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());	
		Map<Object, Integer> m = store.get(classname);
		if (m == null || m.get(o) == null) return -1;
		return m.get(o);
	}
	
	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public boolean canonizeAndEnlargeExtensions(Object root) {
		if (root == null) return false;
		
		boolean extendedExtensions = false;
  		LinkedList<Object> toVisit = new LinkedList<Object>();
  		toVisit.push(root);
  		assignIndexToObject(root);

  		while (!toVisit.isEmpty()) {
  			Object obj = toVisit.pop();
  			Class objClass = obj.getClass();
  			// FIXME: Should assign ids to objects when it gets them out of the queue
  			// for real BFS tagging?
  			
  			// Do nothing with objects of primitve type
			if (!CanonicalRepresentation.isPrimitive(obj)) {
  				if (objClass.isArray()) {
					// Process an array object. Add a dummy field for each of its elements
					for (int i = 0; i < Array.getLength(obj); i++) {
						Object objInArray = Array.get(obj, i);
						Tuple<Boolean, Boolean> addRes = addToExtensions(obj, 
								objInArray, 
								CanonicalRepresentation.getArrayFieldCanonicalName(objClass, i)); 
						if (!addRes.getFirst())
							toVisit.push(objInArray);
						extendedExtensions = extendedExtensions || addRes.getSecond();
					}
  				}
  				else {
  					// Process a non array, non primitive object.
  					// Use its field values to enlarge the extensions
  					// FIXME: Go back to the unsorted version for better performance
  					for (Field fld: getAllClassFieldsSortedByName(objClass)) {
  						fld.setAccessible(true);
  						Object fieldTarget;
						try {
							fieldTarget = fld.get(obj);
						} catch (Exception e) {
							// Cannot happen
							throw new RuntimeException("ERROR: Illegal access to an object field during canonization");
						}
						Tuple<Boolean, Boolean> addRes = addToExtensions(obj, 
								fieldTarget, 
								CanonicalRepresentation.getFieldCanonicalName(fld));
						if (!addRes.getFirst())
							toVisit.push(fieldTarget);
						extendedExtensions = extendedExtensions || addRes.getSecond();							
  					}
  				}
			}
  		}
  		return extendedExtensions;
	}
	
	
	// Returns the list with cls' fields. It stores the fields 
	// the first time to return them in the same order later.
	private List<Field> getAllClassFields(Class cls) {
		List<Field> clsFields = classFields.get(cls);
		if (clsFields != null) return clsFields;

		String classname = CanonicalRepresentation.getClassCanonicalName(cls);
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
	}
	
	
	// Returns the list with cls' fields. It stores the fields 
	// the first time to return them in the same order later.
	private List<Field> getAllClassFieldsSortedByName(Class cls) {
		List<Field> clsFields = classFields.get(cls);
		if (clsFields != null) return clsFields;

		String classname = CanonicalRepresentation.getClassCanonicalName(cls);
		clsFields = new LinkedList<Field>();
		while (cls != null && 
				cls != Object.class && 
				!CanonicalRepresentation.isPrimitive(cls)) {
						
			Field [] fieldsArray = cls.getDeclaredFields();
			for (int i = 0; i < fieldsArray.length; i++) 
				clsFields.add(fieldsArray[i]);

			cls = cls.getSuperclass();	
		}

		System.out.println(clsFields);
		clsFields.sort(new FieldByNameComp());
		System.out.println(clsFields);
		
		
		classFields.put(classname, clsFields);
		return clsFields;
	}
}
