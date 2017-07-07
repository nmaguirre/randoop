package randoop.util.heapcanonization;

import java.lang.reflect.Array;
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
	
	private CanonicalStore store;
	private CanonicalHeap heap;
	private Collection<String> classNames;
	private int maxObjects;
		
	public HeapCanonizer(Collection<String> classNames, int maxObjects) {
		this.classNames = classNames;
		this.maxObjects = maxObjects;
		classNames.add("randoop.util.heapcanonization.DummyHeapRoot");
		this.store = new CanonicalStore(classNames);
	}
	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public CanonizationResult traverseBreadthFirst(Object root) {
			System.out.println("Starting BFS traversal");
		
			this.heap = new CanonicalHeap(store, maxObjects);

			DummyHeapRoot dummyRoot = new DummyHeapRoot(root);
			CanonicalObject canonicalRoot = heap.getCanonicalObject(dummyRoot).getValue();

			Queue<CanonicalObject> workQueue = new LinkedList<>();
			workQueue.add(canonicalRoot);

			Set<CanonicalObject> visited = new HashSet<>();
			visited.add(canonicalRoot);

			CanonizationResult res = CanonizationResult.OK;

			while (!workQueue.isEmpty()) {

				CanonicalObject currObj = workQueue.poll();
				CanonicalClass currObjType = currObj.getCanonicalClass();
				assert currObj != null && 
						currObjType != null && 
						!currObj.isNull() && 
						!currObj.isPrimitive() : "Null/Primitive objects are never added to workQueue";

				assert !currObjType.isArray() : "Arrays are not supported yet.";

				System.out.println("Current object: " + currObj.toString());
				
				for (CanonicalField cf: currObjType.getCanonicalFields()) {

					Entry<CanonizationResult, CanonicalObject> targetRes = cf.getTarget(currObj, heap);
					if (targetRes.getKey() != CanonizationResult.OK)
						return targetRes.getKey();

					CanonicalObject targetObj = targetRes.getValue();

					System.out.println("	Field value for" + cf.toString() + ": " + targetObj.toString());

					if (!targetObj.isNull() && !targetObj.isPrimitive() && visited.add(targetObj)) 
						workQueue.add(targetObj);	
						// Treat cobj current field;
						// printVectorComponent(cobj)
				}

				// Treat all cobj fields;
				// printVectorComponent(cobj)
			}
			System.out.println("Finishing BFS traversal");
			return res;
	}

	public CanonicalStore getStore() {
		return store;
	}

	public CanonicalHeap getHeap() {
		return heap;
	}
	
	
	

	

}
