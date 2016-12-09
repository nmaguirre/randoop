package randoop.util.fieldbasedcontrol;

import java.util.HashMap;
import java.util.Map;

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
	private FieldExtensions extensions;
	
	public HeapCanonizer(FieldExtensions ext) {
		store = new HashMap<String, Map<Object, Integer>>();
		lastIndex = new HashMap<String, Integer>();
		extensions = ext;
	}
	
	public FieldExtensions getExtensions() {
		return extensions; 
	}

	private void ensureObjectClassIsInStore(String classname) {
		Map<Object, Integer> m = store.get(classname);
		if (m == null) {
			m = new HashMap<Object, Integer>();
			store.put(classname, m);
			lastIndex.put(classname, -1);
		}
	}

	private Integer assignIndexToObject(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());
		ensureObjectClassIsInStore(classname);
		Map<Object, Integer> m = store.get(classname);
		Integer res = m.get(o);
		if (res == null) {
			Integer nextIndex = lastIndex.get(classname) + 1;
			m.put(o, nextIndex);
			lastIndex.put(classname, nextIndex);
			return nextIndex;			
		}
		else 
			return res;
	}
	
	public boolean addToExtensions(Object src, Object tgt, String fieldname) {
		String srccls = CanonicalRepresentation.getClassCanonicalName(src.getClass());
		String tgtcls = CanonicalRepresentation.getClassCanonicalName(tgt.getClass());
		// Put the classes in the store if they aren't there
		ensureObjectClassIsInStore(srccls);
		ensureObjectClassIsInStore(tgtcls);
		// Assign indexes to the objects
		Integer srcInd = assignIndexToObject(src);
		Integer tgtInd = assignIndexToObject(tgt);
		// Enlarge the extensions
		return extensions.addPairToField(fieldname, srccls + srcInd, tgtcls + tgtInd);
	}

	// Canonize the heap in a breadth first manner starting at root. 
	// Enlarge the extensions during this process. 
	// Returns true iff at least an element is added to the extensions.
	public boolean canonize(Object root) {
		// TODO: Work in progress
		
		return false;
	}
	
}
