package randoop.util.fieldbasedcontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/* 
 * class HeapCanonizer
 * 
 * Builds a canonical representation of the heap 
 * and creates field extensions with it
 * 
 * Author: Pablo Ponzio.
 * Date: December 2016.
 */

public class HeapCanonizerListStore extends HeapCanonizer {

	// For each class name stores a map of objects with its corresponding indexes in the canonization
	private Map<String, List<Object>> store;
	

	public HeapCanonizerListStore(FieldExtensions extensions, boolean ignorePrimitive) {
		super(extensions, ignorePrimitive);
	}
	

	public HeapCanonizerListStore(FieldExtensions extensions, boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		super(extensions, ignorePrimitive, fieldBasedGenClassnames);
	}

	
	protected Integer getObjectIndex(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());	
		List<Object> l = store.get(classname);
		
		if (l == null) return -1;
		
		int ind = 0;
		for (Object obj: l) {
			if (o == obj) 
				return ind;			

			ind++;
		}
		
		return -1; 
	}
	
	
	protected void initStructures()  {
		store = new HashMap<String, List<Object>>();
		lastIndex = new HashMap<String, Integer>();
	} 

	
	private List<Object> ensureObjectClassIsInStore(String classname) {
		List<Object> m = store.get(classname);
		if (m == null) {
			m = new LinkedList<Object>();
			store.put(classname, m);
			lastIndex.put(classname, -1);
		}
		return m;
	}

	
	// Returns a tuple (old index, new index)
	// if old index == new index then o was stored in a previous call
	// old index is -1 if the object wasn't stored previously
	protected Tuple<Integer, Integer> assignIndexToObject(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());
		List<Object> m = ensureObjectClassIsInStore(classname);

		int ind = 0;
		for (Object obj: m) {
			if (o == obj)
				break;

			ind++;
		}
		
		if (ind == m.size()) {
			Integer nextIndex = lastIndex.get(classname) + 1;
			m.add(o);
			lastIndex.put(classname, nextIndex);
			return new Tuple<Integer, Integer>(-1, nextIndex);
		}
		
		return new Tuple<Integer, Integer>(ind, ind);
	}
	
}
