package randoop.util.heapcanonicalization;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsDummyCollector;

import java.util.Queue;
import java.util.Set;

/* 
 * Author: Pablo Ponzio.
 * Date: July 2017.
 */

public class HeapCanonicalizer {
	
	private final CanonicalStore store;
	private final int maxObjects;
	private final int maxArrayObjs;
	private final int maxBFSDepth;
		
	public HeapCanonicalizer(CanonicalStore store, int maxObjects) {
		this(store, maxObjects, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public HeapCanonicalizer(CanonicalStore store, int maxObjects, int maxArrayObjs) {
		this(store, maxObjects, maxArrayObjs, Integer.MAX_VALUE);
	}

	public HeapCanonicalizer(CanonicalStore store, int maxObjects, int maxArrayObjs, int maxBFSDepth) {
		this.maxObjects = maxObjects;
		this.maxArrayObjs = maxArrayObjs;
		this.store = store; 
		this.maxBFSDepth = maxBFSDepth;
	}
	
	public Entry<CanonicalizationResult, CanonicalHeap> traverseBreadthFirstAndCanonicalize(Object root) {	
		return traverseBreadthFirstAndCanonicalize(root, new FieldExtensionsDummyCollector());
	}

	// Canonicalize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public Entry<CanonicalizationResult, CanonicalHeap> traverseBreadthFirstAndCanonicalize(Object root, FieldExtensionsCollector collector) {
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("----------");
			CanonicalizerLog.logLine("Starting BFS traversal");
		}

		CanonicalHeap resHeap = new CanonicalHeap(store, maxObjects, maxArrayObjs);
		DummyHeapRoot dummyRoot = new DummyHeapRoot(root);
		CanonicalObject canonicalRoot = resHeap.getCanonicalObject(dummyRoot).getValue();

		Queue<Entry<CanonicalObject, Integer>> workQueue = new LinkedList<>();
		workQueue.add(new AbstractMap.SimpleEntry<>(canonicalRoot, -1));
		Set<CanonicalObject> visited = new HashSet<>();
		visited.add(canonicalRoot);

		while (!workQueue.isEmpty()) {

			Entry<CanonicalObject, Integer> currElem = workQueue.poll();
			int currDepth = currElem.getValue();
			CanonicalObject currObj = currElem.getKey(); 
			CanonicalClass currObjType = currObj.getCanonicalClass();
			assert currObj != null && 
					currObjType != null && 
					!currObj.isNull() && 
					!currObj.isPrimitive() : "Null/Primitive objects are never added to workQueue";

			if (CanonicalizerLog.isLoggingOn()) 
				CanonicalizerLog.logLine("Current object: " + currObj.toString());

			Entry<CanonicalizationResult, List<CanonicalField>> getFieldsRes = currObj.getCanonicalFields(); 
			if (getFieldsRes.getKey() != CanonicalizationResult.OK) {
				if (CanonicalizerLog.isLoggingOn()) 
					CanonicalizerLog.logLine("Canonicalization error: " + getFieldsRes.getKey().toString());
				return new AbstractMap.SimpleEntry<CanonicalizationResult, CanonicalHeap>(getFieldsRes.getKey(), null);
			}

			for (CanonicalField currField: getFieldsRes.getValue()) {
				Object fieldValue = currField.getValue(currObj);
				CanonicalClass fieldValType = null;
				// Ensure a canonical class for fieldValue is created and if it is,
				// update its field distance accordingly
				if (fieldValue != null) {
					int newFieldDistance = 0;
					if (currObj.getObject() != dummyRoot) {
						newFieldDistance = currObjType.getFieldDistance()+1;
					}
					fieldValType = store.getUpdateOrCreateCanonicalClass(fieldValue.getClass(), newFieldDistance);
				}
				Entry<CanonicalizationResult, CanonicalObject> valueCanonizationRes = resHeap.getExistingOrCreateCanonicalObject(fieldValue, fieldValType);
				if (valueCanonizationRes.getKey() != CanonicalizationResult.OK) {
					if (CanonicalizerLog.isLoggingOn()) 
						CanonicalizerLog.logLine("Canonicalization error: " + valueCanonizationRes.getKey().toString());
					return new AbstractMap.SimpleEntry<CanonicalizationResult, CanonicalHeap>(valueCanonizationRes.getKey(), null);
				}

				CanonicalObject canonicalValue = valueCanonizationRes.getValue();
				if (CanonicalizerLog.isLoggingOn()) 
					CanonicalizerLog.logLine("	field value: " + /*currfield.toString()*/ currField.getName() + "->"  + canonicalValue.toString());

				if (!canonicalValue.isNull() && 
						!canonicalValue.isPrimitive() 
						&& visited.add(canonicalValue)) {
					if (currDepth < maxBFSDepth-1) {
						if (canonicalValue.isArray())
							// Arrays elements don't count for BFS depth 
							workQueue.add(new AbstractMap.SimpleEntry<>(canonicalValue, currDepth));	
						else	
							workQueue.add(new AbstractMap.SimpleEntry<>(canonicalValue, currDepth+1));	
					}
				}
				// Treat cobj current field;
				CanonicalizationResult addToFieldExtRes = collector.collect(currField, currObj, canonicalValue);
				if (addToFieldExtRes != CanonicalizationResult.OK) {
					if (CanonicalizerLog.isLoggingOn()) 
						CanonicalizerLog.logLine("Canonicalization error: " + addToFieldExtRes.toString());
					return new AbstractMap.SimpleEntry<CanonicalizationResult, CanonicalHeap>(addToFieldExtRes, null);
				}
				// printVectorComponent(cobj)
			}

			// Treat all cobj fields;
			// printVectorComponent(cobj)
		}

		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("Finishing BFS traversal");
			CanonicalizerLog.logLine("----------");
		}
		return new AbstractMap.SimpleEntry<CanonicalizationResult, CanonicalHeap>(CanonicalizationResult.OK, resHeap);
	}

	public CanonicalStore getStore() {
		return store;
	}


	
	

	

}
