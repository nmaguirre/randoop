package randoop.util.heapcanonicalization;

import java.lang.reflect.Field;
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
	
	public CanonicalObject findExistingCanonicalObjectByName(CanonicalObject obj) {
		List<CanonicalObject> clazzObjs = objects.get(obj.getCanonicalClass());
		// Some classes are found out at runtime, for example, when they are the return type of a method.
		if (clazzObjs == null) {
			return null;
		}
		
		for (CanonicalObject currObj: clazzObjs)
			if (currObj.stringRepresentation().equals(obj.stringRepresentation())) 
				return currObj;
		
		return null;
	}
	
	public CanonicalObject findExistingCanonicalObject(Object obj, CanonicalClass clazz) {
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
//			if (CanonicalizerLog.isLoggingOn())
//				CanonicalizerLog.logLine("Randomly picked a class: " + rdmClass.getName());

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
//			if (CanonicalizerLog.isLoggingOn())
//				CanonicalizerLog.logLine("Mutating object:" + toMutate.stringRepresentation());
//			
			// 3- Mutate some randomly selected field of the object
			if (!mutateRandomObjectField(toMutate)) {
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
	
	
	public CanonicalObject mutatedObject;
	public CanonicalObject mutatedValue;
	public CanonicalField mutatedField;
	
	public boolean mutateRandomObjectField(CanonicalObject toMutate) {
		// 3- Pick the field to be mutated.
		CanonicalField rdmFld =	pickRandomFieldToMutate(toMutate);
		if (rdmFld == null) {
			return false;
		}

		// 4- Pick the value the selected field will be set to.
		Object fldValue = rdmFld.getValue(toMutate);
		List<CanonicalObject> allValues = getReferenceObjectsFromHeap(rdmFld, fldValue);
		// Not all values in allValues can be used to set the field
		CanonicalObject rdmValue = selectValueToSetField(toMutate, rdmFld, allValues);
		if (rdmValue == null) {
			return false;
		}

		// Mutating to exactly the same structure
		//if (rdmFld.getValue(toMutate) == rdmValue.getObject())
		//	return false;
		assert rdmFld.getValue(toMutate) != rdmValue.getObject();
		
		// 5- set toMutate.rdmFld = rdmValue
		rdmFld.setValue(toMutate, rdmValue);
		
		mutatedObject = toMutate;
		mutatedField = rdmFld;
		mutatedValue = rdmValue;
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Mutation successful. Field " + rdmFld.stringRepresentation(toMutate) + " of object " + 
					toMutate.stringRepresentation() + " set to " + rdmValue.stringRepresentation());
		
		return true;
	}

	private List<CanonicalObject> getReferenceObjectsFromHeap(CanonicalField rdmFld, Object ignoreObject) {
		List<CanonicalObject> allValues = new LinkedList<>();
		CanonicalClass fldType = rdmFld.getCanonicalType();
		if (objects.get(fldType) != null) {
			// allValues.addAll(objects.get(fldType));
			for (CanonicalObject obj: objects.get(fldType)) {
				if (obj.getObject() != ignoreObject)
					allValues.add(obj);
			}
		}
		for (CanonicalClass descFldType: rdmFld.getCanonicalType().getDescendants()) {
			if (objects.get(descFldType) != null) {
				//allValues.addAll(objects.get(descFldType));
				for (CanonicalObject obj: objects.get(descFldType)) {
					if (obj.getObject() != ignoreObject)
						allValues.add(obj);
				}
			}
		}
		
		if (ignoreObject != null)
			allValues.add(new CanonicalObject(null, null, -1, this));
		return allValues;
	}

	private CanonicalField pickRandomFieldToMutate(CanonicalObject toMutate) {
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
					((!fType.isPrimitive() && store.isClassFromCode(fType)))
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
//		if (CanonicalizerLog.isLoggingOn())
//			CanonicalizerLog.logLine("Randomly picked a field:" + rdmFld.stringRepresentation(toMutate));
		
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
	
	public CanonicalObject getCanonicalObject(CanonicalClass cls, int toMutateInd) {
		return objects.get(cls).get(toMutateInd);
	}
	
	public CanonicalObject getRandomCanonicalObject(CanonicalClass cls) {
		List<CanonicalObject> objs = objects.get(cls);
		if (objs == null) return null;
		int rdmIndex = Randomness.nextRandomInt(objs.size());
		return objs.get(rdmIndex);	
	}
	
	public boolean mutateRandomObjectField(CanonicalObject toMutate, int retries) {
		int retriesLeft = retries;
		boolean succeeded = false;
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("----------");
			CanonicalizerLog.logLine("Starting a mutation attempt:");
			CanonicalizerLog.logLine("Heap contents:\n" + toString());
		}
		
		while (!succeeded && retriesLeft > 0) {
//			if (CanonicalizerLog.isLoggingOn()) 
//				CanonicalizerLog.logLine("Mutating object: " + toMutate.stringRepresentation());
			if (!mutateRandomObjectField(toMutate)) {
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

	private CanonicalObject selectValueToSetField(CanonicalObject toMutate, CanonicalField rdmFld, List<CanonicalObject> allValues) { 
		List<CanonicalObject> candidateValues = allValues;
		if (candidateValues.isEmpty()) {
			return null;
		}
		int rdmValInd = Randomness.nextRandomInt(candidateValues.size());
		CanonicalObject rdmValue = candidateValues.get(rdmValInd);
//		if (CanonicalizerLog.isLoggingOn())
//			CanonicalizerLog.logLine("Randomly picked a value for the field: " + rdmValue.stringRepresentation());	
		return rdmValue;
	}

	public boolean makeRandomFieldSymbolic(CanonicalObject toMutate, int retries, CanonicalObject mutatedObject, CanonicalField mutatedField) {
		int retriesLeft = retries;
		boolean succeeded = false;
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("----------");
			CanonicalizerLog.logLine("Starting a mutation attempt:");
			CanonicalizerLog.logLine("Heap contents:\n" + toString());
		}
		
		while (!succeeded && retriesLeft > 0) {
//			if (CanonicalizerLog.isLoggingOn()) 
//				CanonicalizerLog.logLine("Mutating object: " + toMutate.stringRepresentation());
			if (!makeRandomObjectFieldSymbolic(toMutate, mutatedObject, mutatedField)) {
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
	

	public boolean makeRandomObjectFieldSymbolic(CanonicalClass cls) {
		return makeRandomObjectFieldSymbolic(cls, null, null); 
	}


	public boolean makeRandomObjectFieldSymbolic(CanonicalClass cls, CanonicalObject mutatedObject, CanonicalField mutatedField) {
		return makeRandomObjectFieldSymbolic(getRandomCanonicalObject(cls), mutatedObject, mutatedField); 
	}
	
	
	public CanonicalObject getSymbolicNode() {
			CanonicalObject symbValue = null;
			if (GenInputsAbstract.testclass.get(0).equals("symbolicheap.bounded.AvlTree"))
				symbValue = getCanonicalObject(DummySymbolicAVL.getObject()).getValue();
			else if (GenInputsAbstract.testclass.get(0).equals("symbolicheap.bounded.TreeSet"))
				symbValue = getCanonicalObject(DummySymbolicTSet.getObject()).getValue();
			else if (GenInputsAbstract.testclass.get(0).equals("symbolicheap.bounded.BinomialHeap"))
				symbValue = getCanonicalObject(DummySymbolicBHeapNode.getObject()).getValue();
			else
				assert false : "Unsupported class";
			return symbValue;
	  }
	

	private boolean makeRandomObjectFieldSymbolic(CanonicalObject toMutate, CanonicalObject mutatedObject, CanonicalField mutatedField) {
		// 3- Pick the field to be mutated.
		CanonicalField rdmFld =	pickRandomFieldToMakeSymbolic(toMutate);
		if (rdmFld == null) {
			return false;
		}

		CanonicalObject rdmValue = null;
		if (GenInputsAbstract.testclass.get(0).equals("symbolicheap.bounded.AvlTree"))
			rdmValue = getCanonicalObject(DummySymbolicAVL.getObject()).getValue();
		else if (GenInputsAbstract.testclass.get(0).equals("symbolicheap.bounded.TreeSet"))
			rdmValue = getCanonicalObject(DummySymbolicTSet.getObject()).getValue();
		else if (GenInputsAbstract.testclass.get(0).equals("symbolicheap.bounded.BinomialHeap"))
			rdmValue = getCanonicalObject(DummySymbolicBHeapNode.getObject()).getValue();
		else
			assert false : "Unsupported class";
		
		// Do not allow to mutate the mutated object field in the generation of negative symbolic instances
		if (mutatedObject != null) {
			if (toMutate.getObject() == mutatedObject.getObject() &&
					rdmFld.getName().equals(mutatedField.getName()))
				return false;
		}

		// Don't allow to make symbolic already symbolic values
		Object oldValue = rdmFld.getValue(toMutate);
		if (oldValue != null && IDummySymbolic.class.isAssignableFrom(oldValue.getClass()))
				//instanceof DummySymbolicAVL) || 
//			(rdmFld.getValue(toMutate) instanceof DummySymbolicTSet))
			return false;

		// 5- set toMutate.rdmFld = rdmValue
		rdmFld.setValue(toMutate, rdmValue);
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("Mutation successful. Field " + rdmFld.stringRepresentation(toMutate) + " of object " + 
					toMutate.stringRepresentation() + " set to " + rdmValue.stringRepresentation());
		
		return true;
	}
	
	
	private boolean readAccessedField(Object o, String f) {
		Field fld;
		boolean res = false;
		try {
			fld = o.getClass().getDeclaredField(f);
			fld.setAccessible(true);
			res = (boolean) fld.get(o);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		return res;
	}
	
	
	private CanonicalField pickRandomFieldToMakeSymbolic(CanonicalObject toMutate) {
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
					((!fType.isPrimitive() && store.isClassFromCode(fType)))
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
//		if (CanonicalizerLog.isLoggingOn())
//			CanonicalizerLog.logLine("Randomly picked a field:" + rdmFld.stringRepresentation(toMutate));
		
		return rdmFld;
	}
	
	
	public List<CanonicalField> getSymbolicFields(CanonicalObject toMutate) {
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
					((!fType.isPrimitive() && store.isClassFromCode(fType)))
					)
				candidateFields.add(fld);
		}	
		
		if (candidateFields.isEmpty()) {
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("No fields available to mutate. Retrying...");
			return null;
		}
		
		return candidateFields;
	}
	
	
}
