package randoop.util.heapcanonization;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import randoop.util.heapcanonization.fieldextensions.FieldExtensionsCollector;
import randoop.util.heapcanonization.fieldextensions.FieldExtensionsDummyCollector;

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
	private final int maxFieldDistance;
		
	public HeapCanonizer(Collection<String> classNames, int maxObjects) {
		this(classNames, maxObjects, Integer.MAX_VALUE);
	}
	
	public HeapCanonizer(Collection<String> classNames, int maxObjects, int maxFieldDistance) {
		this.classNames = classNames;
		this.maxObjects = maxObjects;
		this.store = new CanonicalStore(classNames);
		this.maxFieldDistance = maxFieldDistance;
	}
	
	
	public Entry<CanonizationResult, CanonicalHeap> traverseBreadthFirstAndCanonize(Object root) {	
		return traverseBreadthFirstAndCanonize(root, new FieldExtensionsDummyCollector());
	}

	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public Entry<CanonizationResult, CanonicalHeap> traverseBreadthFirstAndCanonize(Object root, FieldExtensionsCollector collector) {

		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine("----------");
			CanonizerLog.logLine("Starting BFS traversal");
		}

		CanonicalHeap resHeap = new CanonicalHeap(store, maxObjects);

		DummyHeapRoot dummyRoot = new DummyHeapRoot(root);
		CanonicalObject canonicalRoot = resHeap.getCanonicalObject(dummyRoot).getValue();

		Queue<Entry<CanonicalObject, Integer>> workQueue = new LinkedList<>();
		workQueue.add(new AbstractMap.SimpleEntry<>(canonicalRoot, -1));

		Set<CanonicalObject> visited = new HashSet<>();
		visited.add(canonicalRoot);

		while (!workQueue.isEmpty()) {

			Entry<CanonicalObject, Integer> currElem = workQueue.poll();
			int currFieldDistance = currElem.getValue();
			CanonicalObject currObj = currElem.getKey(); 
			CanonicalClass currObjType = currObj.getCanonicalClass();
			assert currObj != null && 
					currObjType != null && 
					!currObj.isNull() && 
					!currObj.isPrimitive() : "Null/Primitive objects are never added to workQueue";

			if (CanonizerLog.isLoggingOn()) 
				CanonizerLog.logLine("Current object: " + currObj.toString());

			Entry<CanonizationResult, List<CanonicalField>> getFieldsRes = currObj.getCanonicalFields(); 
			if (getFieldsRes.getKey() != CanonizationResult.OK) {
				if (CanonizerLog.isLoggingOn()) 
					CanonizerLog.logLine("Canonization error: " + getFieldsRes.getKey().toString());
				return new AbstractMap.SimpleEntry<CanonizationResult, CanonicalHeap>(getFieldsRes.getKey(), null);
			}

			for (CanonicalField currField: getFieldsRes.getValue()) {

				Object fieldValue = currField.getValue(currObj);
				Entry<CanonizationResult, CanonicalObject> valueCanonizationRes = resHeap.getCanonicalObject(fieldValue);
				if (valueCanonizationRes.getKey() != CanonizationResult.OK) {
					if (CanonizerLog.isLoggingOn()) 
						CanonizerLog.logLine("Canonization error: " + valueCanonizationRes.getKey().toString());
					return new AbstractMap.SimpleEntry<CanonizationResult, CanonicalHeap>(valueCanonizationRes.getKey(), null);
				}

				CanonicalObject canonicalValue = valueCanonizationRes.getValue();
				if (CanonizerLog.isLoggingOn()) 
					CanonizerLog.logLine("	field value: " + /*currfield.toString()*/ currField.getName() + "->"  + canonicalValue.toString());

				if (!canonicalValue.isNull() && 
						!canonicalValue.isPrimitive() 
						&& visited.add(canonicalValue)) {
					workQueue.add(new AbstractMap.SimpleEntry<>(canonicalValue, currFieldDistance));	
					if (currFieldDistance < maxFieldDistance-1) {
						if (canonicalValue.isArray())
							// Arrays elements don't count as fields
							workQueue.add(new AbstractMap.SimpleEntry<>(canonicalValue, currFieldDistance));	
						else	
							workQueue.add(new AbstractMap.SimpleEntry<>(canonicalValue, currFieldDistance+1));	
					}
				}
				// Treat cobj current field;
				CanonizationResult addToFieldExtRes = collector.collect(currField, currObj, canonicalValue);
				if (addToFieldExtRes != CanonizationResult.OK) {
					if (CanonizerLog.isLoggingOn()) 
						CanonizerLog.logLine("Canonization error: " + addToFieldExtRes.toString());
					return new AbstractMap.SimpleEntry<CanonizationResult, CanonicalHeap>(addToFieldExtRes, null);
				}
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
	
	public Set<String> getAllCanonicalClassNames() {
		return store.getAllCanonicalClassnames();
	}

	
	

	

}
