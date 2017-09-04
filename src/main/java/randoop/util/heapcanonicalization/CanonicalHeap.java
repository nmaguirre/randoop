package randoop.util.heapcanonicalization;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import randoop.util.Randomness;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensions;


public class CanonicalHeap {

	private final int maxObjects;
	private final int maxArrayObjs;
	private final Map<CanonicalClass, List<CanonicalObject>> objects;
	private final CanonicalStore store;

	public CanonicalHeap(CanonicalStore store, int maxObjects) {
		this(store, maxObjects, Integer.MAX_VALUE);
	}
	
	public CanonicalHeap(CanonicalStore store, int maxObjects, int maxArrayObjs) {
		this.store = store;
		this.maxObjects = maxObjects;
		this.maxArrayObjs = maxArrayObjs;
		objects = new LinkedHashMap<>();
		/*
		if (CandidateVectorsWriter.isEnabled()) {
			for (String className: store.getAllCanonicalClassnames()) {
				CanonicalClass canonicalClass = store.getCanonicalClass(className);
				if (!canonicalClass.isPrimitive())
					objects.put(canonicalClass, new LinkedList<CanonicalObject>());
			}
		}
		*/
	}
	
	
	public Map.Entry<CanonicalizationResult, CanonicalObject> getCanonicalObject(Object obj) {
		if (obj == null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, null, -1, this));
		CanonicalClass cls = store.getCanonicalClass(obj.getClass());
		assert cls != null : "This method should be called for existing canonical classes only";
		return getExistingOrCreateCanonicalObject(obj, cls);
	}
	

	public Map.Entry<CanonicalizationResult, CanonicalObject> getExistingOrCreateCanonicalObject(Object obj, CanonicalClass clazz) {
		// Create a new CanonicalObject encapsulating null
		if (obj == null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, null, -1, this));
		// Get, update or create a new CanonicalObject encapsulating the primitive value 
		if (clazz.isPrimitive()) 
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, new CanonicalObject(obj, clazz, -1, this));
		// If there is already an object encapsulating obj, return it
		CanonicalObject res = findExistingCanonicalObject(obj, clazz);
		if (res != null)
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, res);
		// If the object limit is exceeded for the current class, report the error
		if (objects.get(clazz).size() >= maxObjects) 
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.LIMITS_EXCEEDED, null);
		// Create a new canonical object
		return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, newCanonicalObject(obj, clazz));
	}
	
	private CanonicalObject findExistingCanonicalObject(Object obj, CanonicalClass clazz) {
		List<CanonicalObject> clazzObjs = objects.get(clazz);
		// Some classes are found out at runtime, for example, when they are the return type of a method.
		if (clazzObjs == null) {
			objects.put(clazz, new LinkedList<CanonicalObject>());
			return null;
		}
		
		for (CanonicalObject currObj: clazzObjs)
			if (currObj.getObject() == obj) 
				return currObj;
		
		return null;
	}
	
	private CanonicalObject newCanonicalObject(Object obj, CanonicalClass clazz) {
		List<CanonicalObject> clazzObjs = objects.get(clazz);
		CanonicalObject res = new CanonicalObject(obj, clazz, clazzObjs.size(), this);
		clazzObjs.add(res);	
		return res;
	}
	
	public List<CanonicalObject> getObjectsForClass(CanonicalClass clazz) {
		List<CanonicalObject> res = objects.get(clazz);
		if (res == null)
			return new LinkedList<>();
		return res;
	}
	
	public int getMaxObjects() {
		return maxObjects;
	}
	
	public CanonicalStore getStore() {
		return store;
	}

	public int getMaxArrayObjects() {
		return maxArrayObjs;
	}

	// Mutate a reference field of the object represented by the heap in a way that 
	// the mutated field falls outside of the global extensions.  
	public boolean mutateRandomObjectFieldOutsideExtensions(FieldExtensions extensions, int retries) {
		int retriesLeft = retries;

		boolean succeeded = false;
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("----------");
			CanonicalizerLog.logLine("Starting a mutation attempt:");
			CanonicalizerLog.logLine("Heap contents:\n" + toString());
		}
		while (!succeeded && retriesLeft > 0) {
			// 1- Pick a class whose objects are mutation candidates.
			List<CanonicalClass> candidateClasses = new ArrayList<>(); 
			for (CanonicalClass c: objects.keySet()) {
				if (!c.isObject() && !c.isPrimitive() && store.isClassFromCode(c) &&
						!c.getName().equals(DummyHeapRoot.class.getName()))
					candidateClasses.add(c);
			}
			assert !candidateClasses.isEmpty(): "There cannot be no non-primitive classes";
			int rdmClassInd = Randomness.nextRandomInt(candidateClasses.size());
			CanonicalClass rdmClass = candidateClasses.get(rdmClassInd);
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Randomly picked a class: " + rdmClass.getName());

			// 2- Pick an object of the selected class.
			List<CanonicalObject> objs = objects.get(rdmClass);
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Available objects for class: " + objs);
			if (objs.isEmpty()) {
				// No objects of the selected class available to mutate.
				retriesLeft--;
				if (CanonicalizerLog.isLoggingOn())
					CanonicalizerLog.logLine("No objects available to mutate. Retrying...");
				continue;
			}
			int rdmObjInd = Randomness.nextRandomInt(objs.size());
			CanonicalObject toMutate = objs.get(rdmObjInd);
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Randomly picked an object:" + toMutate.stringRepresentation());
			
			// 3- Pick the field to be mutated.
			List<CanonicalField> candidateFields = new ArrayList<>(); 
			
			Entry<CanonicalizationResult, List<CanonicalField>> objFieldsRes = toMutate.getCanonicalFields();
			assert objFieldsRes.getKey() == CanonicalizationResult.OK: "Getting fields of object " + toMutate + " failed";
			for (CanonicalField fld: objFieldsRes.getValue() /*rdmClass.getCanonicalFields()*/) {
				CanonicalClass fType = fld.getCanonicalType();
				if (!fld.isFinal() && !fType.isObject() && !fType.isPrimitive() && store.isClassFromCode(fType))
					candidateFields.add(fld);
			}			
			if (candidateFields.isEmpty()) {
				// No objects of the selected class available to mutate.
				retriesLeft--;
				if (CanonicalizerLog.isLoggingOn())
					CanonicalizerLog.logLine("No fields available to mutate. Retrying...");
				continue;
			}
			int rdmFldInd = Randomness.nextRandomInt(candidateFields.size());
			CanonicalField rdmFld = candidateFields.get(rdmFldInd);			
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Randomly picked a field:" + rdmFld.stringRepresentation(toMutate));

			// 4- Pick the value the selected field will be set to.
			List<CanonicalObject> allValues = new LinkedList<>();
			CanonicalClass fldType = rdmFld.getCanonicalType();
			if (objects.get(fldType) != null)
				allValues.addAll(objects.get(fldType));
			for (CanonicalClass descFldType: rdmFld.getCanonicalType().getDescendants()) {
				if (objects.get(descFldType) != null)
					allValues.addAll(objects.get(descFldType));
			}
			
			allValues.add(new CanonicalObject(null, null, -1, this));
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Available objects to set the field:" + allValues);

			Set<String> objsInExtensions = extensions.getValuesFor(rdmFld.stringRepresentation(toMutate), toMutate.stringRepresentation());
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Objects in extensions:" + objsInExtensions);
			if (objsInExtensions == null)
				objsInExtensions = new HashSet<>();
			List<CanonicalObject> candidateValues = new ArrayList<>();
			for (CanonicalObject candidate: allValues) {
				if (!objsInExtensions.contains(candidate.stringRepresentation()))
					candidateValues.add(candidate);
			}
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Objects outside extensions:" + candidateValues);
			if (candidateValues.isEmpty()) {
				// No values outside the extensions to select for mutation.
				retriesLeft--;
				if (CanonicalizerLog.isLoggingOn())
					CanonicalizerLog.logLine("No objects ouside extensions. Retrying...");
				continue;
			}
			int rdmValInd = Randomness.nextRandomInt(candidateValues.size());
			CanonicalObject rdmValue = candidateValues.get(rdmValInd);
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Randomly picked a value for the field: " + rdmValue.stringRepresentation());		

			// 5- set toMutate.rdmFld = rdmValue
			rdmFld.setValue(toMutate, rdmValue);
			succeeded = true;
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Mutation successful. Field " + rdmFld.stringRepresentation(toMutate) + " of object " + 
						toMutate.stringRepresentation() + " set to " + rdmValue.stringRepresentation());
		}

		if (!succeeded)
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Retries limit exceeded. No mutation performed.");

		return succeeded; 
	}
	
	
	public String toString() {
		String res = "";
		for (CanonicalClass cls: objects.keySet()) {
			res += "class=" + cls.getName() + ", objects=" + objects.get(cls).toString() + "\n";
		}
		return res;
	}
	
	
	public Map<CanonicalClass, Integer> objectsPerClass() {
		Map<CanonicalClass, Integer> res = new LinkedHashMap<>();
		for (CanonicalClass cls: objects.keySet())
			res.put(cls, objects.get(cls).size());
		return res;
	}

	public boolean mutateObjectFieldOutsideExtensions(FieldExtensions extensions, CanonicalClass cls,
			int toMutateInd, int retries) {
		int retriesLeft = retries;
		boolean succeeded = false;
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("----------");
			CanonicalizerLog.logLine("Starting a mutation attempt:");
			CanonicalizerLog.logLine("Heap contents:\n" + toString());
		}
		
		while (!succeeded && retriesLeft > 0) {

			CanonicalObject toMutate = objects.get(cls).get(toMutateInd);
			// 3- Pick the field to be mutated.
			List<CanonicalField> candidateFields = new ArrayList<>(); 
			Entry<CanonicalizationResult, List<CanonicalField>> objFieldsRes = toMutate.getCanonicalFields();
			assert objFieldsRes.getKey() == CanonicalizationResult.OK: "Getting fields of object " + toMutate + " failed";
			for (CanonicalField fld: objFieldsRes.getValue() /*cls.getCanonicalFields()*/) {
				CanonicalClass fType = fld.getCanonicalType();
				if (!fType.getName().endsWith("Element") &&
						!fld.isStatic() && !fld.isFinal() && !fType.isObject() && !fType.isPrimitive() && store.isClassFromCode(fType))
					candidateFields.add(fld);
			}			
			if (candidateFields.isEmpty()) {
				// No objects of the selected class available to mutate.
				retriesLeft--;
				if (CanonicalizerLog.isLoggingOn())
					CanonicalizerLog.logLine("No fields available to mutate. Retrying...");
				continue;
			}
			int rdmFldInd = Randomness.nextRandomInt(candidateFields.size());
			CanonicalField rdmFld = candidateFields.get(rdmFldInd);			
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Randomly picked a field:" + rdmFld.stringRepresentation(toMutate));

			// 4- Pick the value the selected field will be set to.
			List<CanonicalObject> allValues = new LinkedList<>();
			CanonicalClass fldType = rdmFld.getCanonicalType();
			if (objects.get(fldType) != null)
				allValues.addAll(objects.get(fldType));
			for (CanonicalClass descFldType: rdmFld.getCanonicalType().getDescendants()) {
				if (objects.get(descFldType) != null)
					allValues.addAll(objects.get(descFldType));
			}
			
			allValues.add(new CanonicalObject(null, null, -1, this));
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Available objects to set the field:" + allValues);

			Set<String> objsInExtensions = extensions.getValuesFor(rdmFld.stringRepresentation(toMutate), toMutate.stringRepresentation());
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Objects in extensions:" + objsInExtensions);
			if (objsInExtensions == null)
				objsInExtensions = new HashSet<>();
			List<CanonicalObject> candidateValues = new ArrayList<>();
			for (CanonicalObject candidate: allValues) {
				if (!objsInExtensions.contains(candidate.stringRepresentation()))
					candidateValues.add(candidate);
			}
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Objects outside extensions:" + candidateValues);
			if (candidateValues.isEmpty()) {
				// No values outside the extensions to select for mutation.
				retriesLeft--;
				if (CanonicalizerLog.isLoggingOn())
					CanonicalizerLog.logLine("No objects ouside extensions. Retrying...");
				continue;
			}
			int rdmValInd = Randomness.nextRandomInt(candidateValues.size());
			CanonicalObject rdmValue = candidateValues.get(rdmValInd);
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Randomly picked a value for the field: " + rdmValue.stringRepresentation());		

			// 5- set toMutate.rdmFld = rdmValue
			rdmFld.setValue(toMutate, rdmValue);
			succeeded = true;
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Mutation successful. Field " + rdmFld.stringRepresentation(toMutate) + " of object " + 
						toMutate.stringRepresentation() + " set to " + rdmValue.stringRepresentation());
		}

		if (!succeeded)
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Retries limit exceeded. No mutation performed.");

		return succeeded; 
	}
	
	
}
