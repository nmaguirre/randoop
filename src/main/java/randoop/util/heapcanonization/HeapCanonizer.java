package randoop.util.heapcanonization;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;



/* 
 * Author: Pablo Ponzio.
 * Date: July 2017.
 */

public class HeapCanonizer {
	
	private final CanonicalStore store;
	private final Collection<String> classNames;
	private final int maxObjects;
		
	public HeapCanonizer(Collection<String> classNames, int maxObjects) {
		this.classNames = classNames;
		this.maxObjects = maxObjects;
		this.store = new CanonicalStore(classNames);
	}
	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public Map.Entry<CanonizationResult, CanonicalHeap> traverseBreadthFirstAndCanonize(Object root) {

		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine("----------");
			CanonizerLog.logLine("Starting BFS traversal");
		}

		CanonicalHeap resHeap = new CanonicalHeap(store, maxObjects);

		DummyHeapRoot dummyRoot = new DummyHeapRoot(root);
		CanonicalObject canonicalRoot = resHeap.getCanonicalObject(dummyRoot).getValue();

		Queue<CanonicalObject> workQueue = new LinkedList<>();
		workQueue.add(canonicalRoot);

		Set<CanonicalObject> visited = new HashSet<>();
		visited.add(canonicalRoot);

		while (!workQueue.isEmpty()) {

			CanonicalObject currObj = workQueue.poll();
			CanonicalClass currObjType = currObj.getCanonicalClass();
			assert currObj != null && 
					currObjType != null && 
					!currObj.isNull() && 
					!currObj.isPrimitive() : "Null/Primitive objects are never added to workQueue";

					//assert !currObjType.isArray() : "Arrays are not supported yet.";

					if (CanonizerLog.isLoggingOn()) 
						CanonizerLog.logLine("Current object: " + currObj.toString());

					Entry<CanonizationResult, List<CanonicalField>> getFieldsRes = currObj.getCanonicalFields(); 
					if (getFieldsRes.getKey() != CanonizationResult.OK) {
						if (CanonizerLog.isLoggingOn()) 
							CanonizerLog.logLine("Canonization error: " + getFieldsRes.getKey().toString());
						return new AbstractMap.SimpleEntry<CanonizationResult, CanonicalHeap>(getFieldsRes.getKey(), null);
					}
					
					for (CanonicalField currfield: getFieldsRes.getValue()) {

						Object fieldValue = currfield.getValue(currObj);
						Entry<CanonizationResult, CanonicalObject> valueCanonizationRes = resHeap.getCanonicalObject(fieldValue);
						if (valueCanonizationRes.getKey() != CanonizationResult.OK) {
							if (CanonizerLog.isLoggingOn()) 
								CanonizerLog.logLine("Canonization error: " + valueCanonizationRes.getKey().toString());
							return new AbstractMap.SimpleEntry<CanonizationResult, CanonicalHeap>(valueCanonizationRes.getKey(), null);
						}

						CanonicalObject canonicalValue = valueCanonizationRes.getValue();
						if (CanonizerLog.isLoggingOn()) 
							CanonizerLog.logLine("	field value: " + currfield.toString() + "->"  + canonicalValue.toString());

						if (!canonicalValue.isNull() && !canonicalValue.isPrimitive() && visited.add(canonicalValue)) 
							workQueue.add(canonicalValue);	
						// Treat cobj current field;
						// printVectorComponent(cobj)
					}

					// Treat all cobj fields;
					// printVectorComponent(cobj)
		}

		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine("Finishing BFS traversal");
			CanonizerLog.logLine("----------");
		}
		return new AbstractMap.SimpleEntry<CanonizationResult, CanonicalHeap>(CanonizationResult.OK, resHeap);
	}

	public CanonicalStore getStore() {
		return store;
	}

	
	

	

}
