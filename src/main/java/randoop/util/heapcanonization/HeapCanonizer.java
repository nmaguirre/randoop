package randoop.util.heapcanonization;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	private boolean enqueueRepeated;
	private boolean storeNullObjs;
		
	public HeapCanonizer(Collection<String> classNames, int maxObjects, boolean enqueueRepeated, boolean storeNullObjs) {
		this.classNames = classNames;
		this.maxObjects = maxObjects;
		classNames.add("randoop.util.heapcanonization.DummyHeapRoot");
		this.store = new CanonicalStore(classNames);
		this.enqueueRepeated = enqueueRepeated;
		this.storeNullObjs = storeNullObjs;
	}
	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public CanonizationResult traverseBreadthFirst(Object root) {
		try {
			if (root == null) return CanonizationResult.OK;
			
			this.heap = new CanonicalHeap(maxObjects, storeNullObjs);
			Set<CanonicalObject> visited; 
			/*
			if (enqueueRepeated)
				visited = new DummySet<>();
			else 
			*/

			visited = new HashSet<>();

			/*
			DummyHeapRoot dummyRoot = new DummyHeapRoot(root);
CanonicalObject canonicalRoot = heap.getCanonicalObject(dummyRoot, store.getCanonicalClass(dummyRoot.getClass()));
			*/
			Queue<CanonicalObject> workQueue = new LinkedList<>();

			CanonicalObject canonicalRoot = heap.getCanonicalObject(root, store.getCanonicalClass(root.getClass()));
			workQueue.add(canonicalRoot);
			visited.add(canonicalRoot);

			CanonizationResult res = CanonizationResult.OK;

			while (!workQueue.isEmpty()) {

				CanonicalObject currObj = workQueue.poll();
				CanonicalClass currObjType = currObj.getCanonicalClass();

				if (currObjType.isArray())
					throw new RuntimeException("ERROR: Canonization of arrays not supported yet.");

				for (CanonicalField cf: currObjType.getCanonicalFields()) {

					if (cf.isPrimitiveType()) continue;

					CanonicalObject targetObj = cf.getTarget(currObj, heap);

					if (!targetObj.isNull() && !visited.add(targetObj)) 
						workQueue.add(targetObj);	

						// Treat cobj current field;
						// printVectorComponent(cobj)
				}

				// Treat all cobj fields;
				// printVectorComponent(cobj)
			}
			return res;

		} catch (LimitsExceededException e) {
			return CanonizationResult.LIMITS_EXCEEDED;
		}

	}

	public CanonicalStore getStore() {
		return store;
	}

	public CanonicalHeap getHeap() {
		return heap;
	}
	
	
	

	

}
