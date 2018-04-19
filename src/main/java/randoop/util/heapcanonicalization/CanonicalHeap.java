package randoop.util.heapcanonicalization;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import randoop.generation.AbstractGenerator;
import randoop.main.GenInputsAbstract;
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
		
		boolean mutateWithinExtensions = false;
		if (AbstractGenerator.vectorization_mutate_within_extensions && 
				Randomness.weighedCoinFlip(AbstractGenerator.vectorization_mutate_within_extensions_percentage)) 		
			mutateWithinExtensions = true;
		
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
				CanonicalizerLog.logLine("Mutating object:" + toMutate.stringRepresentation());
			
			// 3- Mutate some randomly selected field of the object
			if (!mutateRandomObjectField(mutateWithinExtensions, toMutate, extensions)) {
				retriesLeft--;
				continue;
			}

			succeeded = true;
		}

		if (!succeeded)
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Retries limit exceeded. No mutation performed.");

		return succeeded; 
	}
	
	private boolean mutateRandomObjectField(boolean mutateWithinExtensions, CanonicalObject toMutate, FieldExtensions extensions) {
		// 3- Pick the field to be mutated.
		CanonicalField rdmFld =	pickRandomFieldToMutate(mutateWithinExtensions, toMutate);
		if (rdmFld == null) {
			return false;
		}

		// 4- Pick the value the selected field will be set to.
		Set<String> objsInExtensions = getObjectsInExtensions(extensions, toMutate, rdmFld);
		List<CanonicalObject> allValues = getAvailableValuesToSetField(rdmFld, objsInExtensions);
		// Not all values in allValues can be used to set the field
		CanonicalObject rdmValue = selectValueToSetField(mutateWithinExtensions, toMutate, rdmFld,
				allValues, objsInExtensions);
		if (rdmValue == null) {
			return false;
		}

		// 5- set toMutate.rdmFld = rdmValue
		rdmFld.setValue(toMutate, rdmValue);
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Mutation successful. Field " + rdmFld.stringRepresentation(toMutate) + " of object " + 
					toMutate.stringRepresentation() + " set to " + rdmValue.stringRepresentation());
		
		if (mutateWithinExtensions)
			AbstractGenerator.vectorization_mutated_within_extensions = true;
		
		return true;
	}

	private List<CanonicalObject> getAvailableValuesToSetField(CanonicalField rdmFld, Set<String> objsInExtensions) {
		List<CanonicalObject> allValues = new LinkedList<>();
		if (rdmFld.isPrimitiveType()) {
			// Primitive fields are always mutated within extensions.
			createPrimitiveValuesFromExtensions(rdmFld, objsInExtensions, allValues);
		}
		else {
			getReferenceObjectsFromHeap(rdmFld, allValues, rdmFld.getCanonicalType());
		}
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Available objects to set the field:" + allValues);
		return allValues;
	}

	private void getReferenceObjectsFromHeap(CanonicalField rdmFld, List<CanonicalObject> allValues,
			CanonicalClass fldType) {
		if (objects.get(fldType) != null)
			allValues.addAll(objects.get(fldType));
		for (CanonicalClass descFldType: rdmFld.getCanonicalType().getDescendants()) {
			if (objects.get(descFldType) != null)
				allValues.addAll(objects.get(descFldType));
		}
		allValues.add(new CanonicalObject(null, null, -1, this));
	}

	private void createPrimitiveValuesFromExtensions(CanonicalField rdmFld, Set<String> objsInExtensions,
			List<CanonicalObject> allValues) {
		for (String primStr: objsInExtensions) {
			Object res = null;
			try {
				res = Integer.parseInt(primStr);
			}
			catch (NumberFormatException e) {
				try {
					res = Double.parseDouble(primStr);
				}
				catch (NumberFormatException e1) {
					try {
						res = Float.parseFloat(primStr);
					}	
					catch (NumberFormatException e2) {	
						try {
							res = Long.parseLong(primStr);
						}	
						catch (NumberFormatException e3) {	
							// res is boolean, string or char.
							if ("true".equalsIgnoreCase(primStr) || "false".equalsIgnoreCase(primStr))
								res = Boolean.parseBoolean(primStr);
							else
								res = primStr;
						}
					}
				}
			}
			allValues.add(new CanonicalObject(res, rdmFld.getCanonicalType(), -1, null));
		}
	}

	private Set<String> getObjectsInExtensions(FieldExtensions extensions, CanonicalObject toMutate,
			CanonicalField rdmFld) {
		Set<String> objsInExtensions = extensions.getValuesFor(rdmFld.stringRepresentation(toMutate), toMutate.stringRepresentation());
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Objects in extensions:" + objsInExtensions);
		if (objsInExtensions == null)
			objsInExtensions = new HashSet<>();
		return objsInExtensions;
	}

	private CanonicalField pickRandomFieldToMutate(boolean mutateWithinExtensions, CanonicalObject toMutate) {
		List<CanonicalField> candidateFields = new ArrayList<>();
		Entry<CanonicalizationResult, List<CanonicalField>> objFieldsRes = toMutate.getCanonicalFields();
		assert objFieldsRes.getKey() == CanonicalizationResult.OK: "Getting fields of object " + toMutate + " failed";
		Pattern dontMutate = GenInputsAbstract.vectorization_dont_mutate_fields;

		for (CanonicalField fld: objFieldsRes.getValue() /*rdmClass.getCanonicalFields()*/) {
			if (dontMutate != null && dontMutate.matcher(fld.getName()).find()) {
				if (CanonicalizerLog.isLoggingOn()) {
					CanonicalizerLog.logLine("Field " + fld.getName() + " matches "
							+ "--vectorization-dont-mutate-fields regular expression, don't mutate it");
				}
				continue;
			}
			
			CanonicalClass fType = fld.getCanonicalType();
			if (!fld.isStatic() &&
					!fld.isFinal() && 
					!fType.isObject() && 
					// We allow for mutation of non reference fields when mutating within extensions
					(mutateWithinExtensions || (!fType.isPrimitive() && store.isClassFromCode(fType)))
					)
				candidateFields.add(fld);
		}	
		
		if (candidateFields.isEmpty()) {
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("No fields available to mutate. Retrying...");
			return null;
		}
		int rdmFldInd = Randomness.nextRandomInt(candidateFields.size());
		CanonicalField rdmFld = candidateFields.get(rdmFldInd);			
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Randomly picked a field:" + rdmFld.stringRepresentation(toMutate));
		
		return rdmFld;
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
		
		boolean mutateWithinExtensions = false;
		if (AbstractGenerator.vectorization_mutate_within_extensions && 
				Randomness.weighedCoinFlip(AbstractGenerator.vectorization_mutate_within_extensions_percentage)) 		
			mutateWithinExtensions = true;
		
		while (!succeeded && retriesLeft > 0) {

			CanonicalObject toMutate = objects.get(cls).get(toMutateInd);
			if (CanonicalizerLog.isLoggingOn()) 
				CanonicalizerLog.logLine("Mutating object: " + toMutate.stringRepresentation());
			if (!mutateRandomObjectField(mutateWithinExtensions, toMutate, extensions)) {
				retriesLeft--;
				continue;
			}

			succeeded = true;
		}

		if (!succeeded)
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Retries limit exceeded. No mutation performed.");

		return succeeded; 
	}

	private CanonicalObject selectValueToSetField(boolean mutateWithinExtensions, CanonicalObject toMutate,
			CanonicalField rdmFld, List<CanonicalObject> allValues, Set<String> objsInExtensions) {
		List<CanonicalObject> candidateValues = new ArrayList<>();
		
		if (!mutateWithinExtensions) {
			for (CanonicalObject candidate: allValues) {
				if (!objsInExtensions.contains(candidate.stringRepresentation()))
					candidateValues.add(candidate);
			}
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Mutating object outside extensions\nObjects outside extensions:" + candidateValues);
		}
		else {
			Object currFldVal = rdmFld.getValue(toMutate);
			for (CanonicalObject candidate: allValues) {
				String candStr = candidate.stringRepresentation();
				if ((!rdmFld.isPrimitiveType() || !candidate.getObject().equals(currFldVal)) &&
					(rdmFld.isPrimitiveType() || candidate.getObject() != currFldVal) &&
						objsInExtensions.contains(candStr))
					candidateValues.add(candidate);
			}
			
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("Mutating object within extensions.\nObjects within extensions:" + candidateValues);
		}
		
		if (candidateValues.isEmpty()) {
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("No objects ouside extensions. Retrying...");
			return null;
		}
		int rdmValInd = Randomness.nextRandomInt(candidateValues.size());
		CanonicalObject rdmValue = candidateValues.get(rdmValInd);
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Randomly picked a value for the field: " + rdmValue.stringRepresentation());	
		return rdmValue;
	}
	
	
}
