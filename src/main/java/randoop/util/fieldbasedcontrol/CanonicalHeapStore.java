package randoop.util.fieldbasedcontrol;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CanonicalHeapStore {
	
	private static CanonicalHeapStore instance = null;
	
	// For each class index stores a list of the objects corresponding to the class.
	// The position of the object in the list corresponds to the object's index in the canonization
	public ArrayList<LinkedList<Object>> store = new ArrayList<>();
	// For each class name stores the last index assigned to an object of the class
	public ArrayList<Integer> lastIndex;

	public static CanonicalHeapStore getInstance() {
		if (instance == null)
			instance = new CanonicalHeapStore();
		
		return instance;
	}
	
	private CanonicalHeapStore() {};
	
	public void addNewClass() {
		store.add(new LinkedList<Object>());
		lastIndex.add(-1);
	}
	
	private void clearStore() {
		for (List<Object> l: store) {
			l.clear();
		}
	}
	
	private void resetLastIndexes() {
		for (int i = 0; i < lastIndex.size(); i++)
			lastIndex.set(i, -1);
	}

	public void clearStoreAndIndexes()	{
		clearStore();
		resetLastIndexes();
	}
	

	/*
	public Integer getObjectIndex(CanonizerClass cc, Object o) {
		List<Object> l = store.get(cc.index);
		
		if (l == null) return -1;
		
		int ind = 0;
		for (Object obj: l) {
			if (o == obj) 
				return ind;			

			ind++;
		}
		
		return -1; 
	}
	*/
	
	// Returns a tuple (old index, new index)
	// if old index == new index then o was stored in a previous call
	// old index is -1 if the object wasn't stored previously
	public Tuple<Integer, Integer> assignIndexToObject(CanonizerClass cc, Object o) {
		List<Object> l = store.get(cc.index);

		int ind = 0;
		for (Object obj: l) {
			if (o == obj)
				break;

			ind++;
		}
		
		if (ind == l.size()) {
			int newIndex = lastIndex.get(cc.index) + 1;
			lastIndex.set(cc.index, newIndex);
			l.add(o);
			return new Tuple<Integer, Integer>(-1, newIndex);
		}
		
		return new Tuple<Integer, Integer>(ind, ind);
	}
	
	

}
