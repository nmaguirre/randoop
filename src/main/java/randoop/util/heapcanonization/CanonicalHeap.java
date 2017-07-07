package randoop.util.heapcanonization;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class CanonicalHeap {

	private int maxObjects;
	private boolean storeNullObjects;
	private Map<CanonicalClass, List<CanonicalObject>> objects;

	public CanonicalHeap(int maxObjects, boolean storeNullObjects) {
		this.maxObjects = maxObjects;
		this.storeNullObjects = storeNullObjects;
		objects = new LinkedHashMap<>();
	}

	public CanonicalObject getCanonicalObject(Object obj, CanonicalClass clazz) throws LimitsExceededException {
		if (!storeNullObjects && obj == null) 
			return new CanonicalObject(null, clazz, -1);
		
		List<CanonicalObject> clazzObjs = objects.get(clazz);
		if (clazzObjs == null) {
			clazzObjs = new LinkedList<>();
			objects.put(clazz, clazzObjs);
		}
		
		if (obj != null) {
			for (CanonicalObject currObj: clazzObjs) {
				if (currObj.getObject() == obj) 
					return currObj;
			}
		}
		
		if (objects.size() >= maxObjects) 
			throw new LimitsExceededException();
		
		CanonicalObject res = new CanonicalObject(obj, clazz, objects.size());
		clazzObjs.add(res);

		return res;
	}
	
	public List<CanonicalObject> getObjectsForClass(CanonicalClass clazz) {
		return objects.get(clazz); 
	}
	
	public int getMaxObjects() {
		return maxObjects;
	}

	
	
}
