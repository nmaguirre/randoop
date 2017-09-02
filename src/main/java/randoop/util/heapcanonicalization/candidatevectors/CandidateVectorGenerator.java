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

public class CandidateVectorGenerator {
	
	private static int NULL_INT_REPRESENTATION = AbstractGenerator.cand_vect_null_rep; //Integer.MIN_VALUE;
	private final Set<String> classesFromCode = new LinkedHashSet<>();
	private final Set<String> singletonClasses = new HashSet<>();
	private final boolean noPrimitiveFields;
	
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
				for (int i = 1; i <= heap.getMaxObjects(); i++) 
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
				clazz.isPrimitive() || clazz.isAbstract() || clazz.isInterface() || clazz.isObject();
				//|| !classesFromCode.contains(clazz.getName());
	}
	
	private void addCandidateVectorFields(CanonicalHeap heap, CanonicalClass clazz, int objNum, CandidateVector<String> header) {
		for (CanonicalField fld: clazz.getCanonicalFields()) {
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
	
	public CandidateVector<Integer> makeCandidateVectorFrom(CanonicalHeap heap) {
		CandidateVector<Integer> res = new CandidateVector<>(); 
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
			
			if (!singletonClasses.contains(canonicalClass.getName()))
				for ( ; i <= heap.getMaxObjects(); i++) 
					addNullObjectToCandidateVector(canonicalClass, heap, res);
		}	

		return res;
	}
	
	private void addNullObjectToCandidateVector(CanonicalClass canonicalClass, CanonicalHeap heap, CandidateVector<Integer> v) {
		if (canonicalClass.isArray())
			for (int i = 0; i < heap.getMaxObjects(); i++)
				v.addComponent(NULL_INT_REPRESENTATION);
		else {
			for (CanonicalField fld: canonicalClass.getCanonicalFields()) {
				if (AbstractGenerator.vectorization_ignore_static && fld.isStatic())
					continue;
				
				if (!fld.getName().equals("serialVersionUID")) 
					if (!noPrimitiveFields || (/*!fld.isObjectType() &&*/ !fld.isPrimitiveType()))
						v.addComponent(NULL_INT_REPRESENTATION);
			}
		}
	}

	private void addToCandidateVector(CanonicalObject obj, CanonicalHeap heap, CandidateVector<Integer> v) {
		int comp;
		Map.Entry<CanonicalizationResult, List<CanonicalField>> getFieldsRes = obj.getCanonicalFields();
		assert getFieldsRes.getKey() == CanonicalizationResult.OK : 
			"The heap should not be modified while printing a candidate vector";
		
		int fieldNumber = 0;
		for (CanonicalField fld: getFieldsRes.getValue()) {
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
			else if (canValue.isPrimitive())
				comp = canValue.getObject().hashCode();
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
