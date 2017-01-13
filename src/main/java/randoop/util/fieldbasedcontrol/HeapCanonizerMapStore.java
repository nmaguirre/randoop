package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class HeapCanonizerMapStore extends HeapCanonizerObjectIndexes {
	// For each class name stores a map of objects with its corresponding indexes in the canonization
	private Map<String, Map<Object, Integer>> store;

	
	public HeapCanonizerMapStore(FieldExtensions extensions, boolean ignorePrimitive) {
		super(extensions, ignorePrimitive);
	}
	
	public HeapCanonizerMapStore(FieldExtensions extensions, boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		super(extensions, ignorePrimitive, fieldBasedGenClassnames);
	}

	protected Integer getObjectIndex(Object o) {
		String classname = CanonicalRepresentation.getClassCanonicalName(o.getClass());	
		Map<Object, Integer> m = store.get(classname);
		if (m == null || m.get(o) == null) return -1;
		return m.get(o);
	}

	protected void initStructures()  {
		store = new HashMap<String, Map<Object, Integer>>();
		lastIndex = new HashMap<String, Integer>();
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
	protected Tuple<Integer, Integer> assignIndexToObject(Object o) {
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
	
}
