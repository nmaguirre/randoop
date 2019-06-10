package randoop.util.heapcanonicalization.candidatevectors;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.generation.AbstractGenerator;
import randoop.util.heapcanonicalization.CanonicalClass;
import randoop.util.heapcanonicalization.CanonicalField;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalObject;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;
import randoop.util.heapcanonicalization.DummyHeapRoot;
import randoop.util.heapcanonicalization.IDummySymbolic;

public class CandidateVectorGenerator {
	
	protected static int NULL_INT_REPRESENTATION = AbstractGenerator.cand_vect_null_rep; //Integer.MIN_VALUE;
	protected final Set<String> classesFromCode = new LinkedHashSet<>();
	protected final Set<String> singletonClasses = new HashSet<>();
	protected final boolean noPrimitiveFields;
	
	public CandidateVectorGenerator(CanonicalStore store, String rootClass) {
		this(store, rootClass, false);
	}

	public CandidateVectorGenerator(CanonicalStore store, String rootClass, boolean noPrimitiveFields) {
		this(store, noPrimitiveFields);
		CanonicalClass cls = store.getCanonicalClass(rootClass);
		/*
		 *  FIXME: Hack to avoid saving space for many instances of the root class in candidate vectors.
		 *  It may be useful for the generation of data structure's instances.
		 */
		if (!cls.hasFieldReferencingItself())
			singletonClasses.add(rootClass);
	}
	
	public CandidateVectorGenerator(CanonicalStore store) {
		this(store, false);
	}
	
	// Should be passed only those classes that were obtained from the code of the classes
	// we will canonize, before test generation starts. In this way, classes added to 
	// the store later during generation are ignored.
	public CandidateVectorGenerator(CanonicalStore store, boolean noPrimitiveFields) {
		this.classesFromCode.addAll(store.getClassnamesFromCode());
		singletonClasses.add(DummyHeapRoot.class.getName());
		this.noPrimitiveFields = noPrimitiveFields;
		
		if (AbstractGenerator.vectorization_max_array_objects != Integer.MAX_VALUE)
			maxArrays = AbstractGenerator.vectorization_max_array_objects;
	}
	
	protected Integer maxArrays = null;
	
	// TODO: Test setting maxArrays in Facu's case
	public void setMaxArrayObjects(int maxArrays) {
		this.maxArrays = maxArrays;
	}
	
	public CandidateVector<String> makeCandidateVectorsHeader(CanonicalHeap heap) {
		CandidateVector<String> header = new CandidateVector<>();
		CanonicalStore store = heap.getStore();

		for (String className: classesFromCode/*store.getAllCanonicalClassnames()*/) {
			CanonicalClass canonicalClass = store.getCanonicalClass(className);
			if (ignoreCanonicalClassInCandidateVectors(canonicalClass))
				continue;

			if (singletonClasses.contains(canonicalClass.getName())) 
				addCandidateVectorFields(heap, canonicalClass, 1, header);
			else if (canonicalClass.isArray()) {
				// for (int i = 0; i < heap.getMaxObjects(); i++) 
				// Start enumerating objects from 1 to handle null=0 well
				/*
				if (AbstractGenerator.vectorization_remove_unused_arrays && canonicalClass.isArray()) {
					addCandidateVectorArrayFields(heap, canonicalClass, 1, header);
				}
				else {
					for (int i = 1; i <= heap.getMaxObjects(); i++) 
						addCandidateVectorArrayFields(heap, canonicalClass, i, header);
				}
				*/
				int arrNum = (maxArrays != null) ? maxArrays : heap.getMaxObjects();
				
				for (int i = 1; i <= arrNum; i++)
					addCandidateVectorArrayFields(heap, canonicalClass, i, header);
			}
			else 
				// for (int i = 0; i < heap.getMaxObjects(); i++) 
				// Start enumerating objects from 1 to handle null=0 well
				for (int i = 1; i <= heap.getMaxObjects(); i++) 
					addCandidateVectorFields(heap, canonicalClass, i, header);
		}	
		
	    if (CanonicalizerLog.isLoggingOn()) {
	    	CanonicalizerLog.logLine("**********");
	    	CanonicalizerLog.logLine("Candidate vectors structure:");
	    	CanonicalizerLog.logLine(header.toString());
	    	CanonicalizerLog.logLine("**********");
	    }
		
		return header;
	}
	
	private boolean ignoreCanonicalClassInCandidateVectors(CanonicalClass clazz) {
		return 
				clazz.getName().endsWith("Element") ||
				/*clazz.getName().equals("singlylist.Element") ||
				clazz.getName().equals("doublylist.Element") ||*/
				clazz.isPrimitive() || clazz.isAbstract() || clazz.isInterface() || clazz.isObject() ||
				IDummySymbolic.class.isAssignableFrom(clazz.getConcreteClass());

				//|| !classesFromCode.contains(clazz.getName());
	}
	
	private void addCandidateVectorFields(CanonicalHeap heap, CanonicalClass clazz, int objNum, CandidateVector<String> header) {
		for (CanonicalField fld: clazz.getCanonicalFields()) {
			if (ignoreField(fld))
				continue;

			if (AbstractGenerator.vectorization_ignore_static && fld.isStatic())
				continue;
			
			if (fld.getName().equals("serialVersionUID")) {
			    if (CanonicalizerLog.isLoggingOn())
			    	CanonicalizerLog.logLine("CANONIZER INFO: Skipping field: " + fld.getName());
			}
			else {
				if (!noPrimitiveFields || (/*!fld.isObjectType() &&*/ !fld.isPrimitiveType()))
					header.addComponent(clazz.getName() + "->o" + objNum + "." + fld.getName());
			}
		}
	}

	private void addCandidateVectorArrayFields(CanonicalHeap heap, CanonicalClass clazz, int objNum, CandidateVector<String> header) {
		for (int i = 0; i < heap.getMaxObjects(); i++) 
			header.addComponent(clazz.getName() + "->o" + objNum + "[" + i + "]");
	}	
	
	public CandidateVector<Object> makeCandidateVectorFrom(CanonicalHeap heap) {
		CandidateVector<Object> res = new CandidateVector<>(); 
		CanonicalStore store = heap.getStore();
		for (String className: classesFromCode/*store.getAllCanonicalClassnames()*/) {
			CanonicalClass canonicalClass = store.getCanonicalClass(className);
			if (ignoreCanonicalClassInCandidateVectors(canonicalClass))
				continue;
			int i = 1;
			for (CanonicalObject obj: heap.getObjectsForClass(canonicalClass)) {
				addToCandidateVector(obj, heap, res);
				i++;
			}
			
			if (singletonClasses.contains(canonicalClass.getName())) continue; 
			
			// When vectorization_remove_unused_arrays is set we always print one array element or the null array
			// If there were more than one array randoop will fail because different vectors will have a different size
			if (/*AbstractGenerator.vectorization_remove_unused_arrays &&*/ canonicalClass.isArray()) {
				/*
				if (i == 1)
					addNullObjectToCandidateVector(canonicalClass, heap, res);
					*/
				
				int arrNum = (maxArrays != null) ? maxArrays : heap.getMaxObjects();
				for ( ; i <= arrNum; i++) 
					addNullObjectToCandidateVector(canonicalClass, heap, res);
			}
			else {
				for ( ; i <= heap.getMaxObjects(); i++) 
					addNullObjectToCandidateVector(canonicalClass, heap, res);
			}
		}	

		return res;
	}
	
	private void addNullObjectToCandidateVector(CanonicalClass canonicalClass, CanonicalHeap heap, CandidateVector<Object> v) {
		if (canonicalClass.isArray())
			for (int i = 0; i < heap.getMaxObjects(); i++)
				v.addComponent(-2);//NULL_INT_REPRESENTATION);
		else {
			for (CanonicalField fld: canonicalClass.getCanonicalFields()) {
				if (ignoreField(fld))
					continue;

				if (AbstractGenerator.vectorization_ignore_static && fld.isStatic())
					continue;
				
				if (!fld.getName().equals("serialVersionUID")) 
					if (!noPrimitiveFields || (/*!fld.isObjectType() &&*/ !fld.isPrimitiveType()))
						v.addComponent(-2);//NULL_INT_REPRESENTATION);
			}
		}
	}
	
	protected boolean ignoreField(CanonicalField fld) {
		if (fld.getName().startsWith("_"))
			return true;
		
		return false;
	}

	protected void addToCandidateVector(CanonicalObject obj, CanonicalHeap heap, CandidateVector<Object> v) {
		Object comp;
		Map.Entry<CanonicalizationResult, List<CanonicalField>> getFieldsRes = obj.getCanonicalFields();
		assert getFieldsRes.getKey() == CanonicalizationResult.OK : 
			"The heap should not be modified while printing a candidate vector";
		
		int fieldNumber = 0;
		for (CanonicalField fld: getFieldsRes.getValue()) {
			if (ignoreField(fld))
				continue;
			
			if (AbstractGenerator.vectorization_ignore_static && fld.isStatic())
				continue;
			if (fld.getName().equals("serialVersionUID"))
				continue; 
			if (noPrimitiveFields && (/*fld.isObjectType() ||*/ fld.isPrimitiveType()))
				continue;

			Object value = fld.getValue(obj);
			Map.Entry<CanonicalizationResult, CanonicalObject> canRes = heap.getCanonicalObject(value);
			assert canRes.getKey() == CanonicalizationResult.OK : 
				"The heap should not be modified the heap while printing a candidate vector.";

			CanonicalObject canValue = canRes.getValue();
			if (canValue.isNull()) 
				comp = NULL_INT_REPRESENTATION;
			else if (canValue.isPrimitive()) {
				if (canValue.getObject() instanceof java.lang.Float ||
					canValue.getObject() instanceof java.lang.Double) 
					// Add a float to the canonical vector.
					// In all other cases we add an integer
					comp = canValue.getObject();
				else if (canValue.getObject() instanceof java.lang.Boolean) {
					if (((Boolean)canValue.getObject())) 
						comp = 1;
					else
						comp = 0;
				}
				else 
					comp = canValue.getObject().hashCode();
			}
			else 
				// comp = canValue.getIndex();
				// Start enumerating the objects from 1 to handle null=0 well
				comp = canValue.getIndex() + 1;
			
			v.addComponent(comp);
			fieldNumber++;
		}
		
		if (obj.isArray()) {
			// Fields only exist for valid positions of the current array object.
			// Complete the remaining array values up to heap.getMaxObjects() with null
			for (int i = fieldNumber; i < heap.getMaxObjects(); i++) {
				v.addComponent(NULL_INT_REPRESENTATION);
			}
		}
	}
	
}