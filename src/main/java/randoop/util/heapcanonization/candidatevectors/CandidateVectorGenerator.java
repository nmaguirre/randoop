package randoop.util.heapcanonization.candidatevectors;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.generation.AbstractGenerator;
import randoop.util.heapcanonization.CanonicalClass;
import randoop.util.heapcanonization.CanonicalField;
import randoop.util.heapcanonization.CanonicalHeap;
import randoop.util.heapcanonization.CanonicalObject;
import randoop.util.heapcanonization.CanonicalStore;
import randoop.util.heapcanonization.CanonizationResult;
import randoop.util.heapcanonization.CanonizerLog;
import randoop.util.heapcanonization.DummyHeapRoot;

public class CandidateVectorGenerator {
	
	private static int NULL_INT_REPRESENTATION = AbstractGenerator.cand_vect_null_rep; //Integer.MIN_VALUE;
	private final Set<String> classesFromCode = new LinkedHashSet<>();

	// Should be passed only those classes that were obtained from the code of the classes
	// we will canonize, before test generation starts. In this way, classes added to 
	// the store later during generation are ignored.
	public CandidateVectorGenerator(Set<String> classesFromCode) {
		this.classesFromCode.addAll(classesFromCode);
	}
	
	public CandidateVector<String> makeCandidateVectorsHeader(CanonicalHeap heap) {
		CandidateVector<String> header = new CandidateVector<>();
		CanonicalStore store = heap.getStore();

		for (String className: classesFromCode/*store.getAllCanonicalClassnames()*/) {
			CanonicalClass canonicalClass = store.getCanonicalClass(className);
			if (ignoreCanonicalClassInCandidateVectors(canonicalClass))
				continue;

			if (canonicalClass.getName().equals(DummyHeapRoot.class.getName()))
				addCandidateVectorFields(heap, canonicalClass, 0, header);
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
		
	    if (CanonizerLog.isLoggingOn()) {
	    	CanonizerLog.logLine("**********");
	    	CanonizerLog.logLine("Candidate vectors structure:");
	    	CanonizerLog.logLine(header.toString());
	    	CanonizerLog.logLine("**********");
	    }
		
		return header;
	}
	
	private boolean ignoreCanonicalClassInCandidateVectors(CanonicalClass clazz) {
		return clazz.isPrimitive() || clazz.isAbstract() || clazz.isInterface();
				//|| !classesFromCode.contains(clazz.getName());
	}
	
	private void addCandidateVectorFields(CanonicalHeap heap, CanonicalClass clazz, int objNum, CandidateVector<String> header) {
		for (CanonicalField fld: clazz.getCanonicalFields()) 
			header.addComponent(clazz.getName() + "->o" + objNum + "." + fld.getName());
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
			int i = 0;
			for (CanonicalObject obj: heap.getObjectsForClass(canonicalClass)) {
				addToCandidateVector(obj, heap, res);
				i++;
			}
			
			if (!canonicalClass.getName().equals(DummyHeapRoot.class.getName()))
				for ( ; i < heap.getMaxObjects(); i++) 
					addNullObjectToCandidateVector(canonicalClass, heap, res);
		}	

		if (CanonizerLog.isLoggingOn()) {
			CanonizerLog.logLine("**********");
			CanonizerLog.logLine("Canonical vector:");
			CanonizerLog.logLine(res.toString());
			CanonizerLog.logLine("**********");
		}
		
		return res;
	}
	
	private void addNullObjectToCandidateVector(CanonicalClass canonicalClass, CanonicalHeap heap, CandidateVector<Integer> v) {
		if (canonicalClass.isArray())
			for (int i = 0; i < heap.getMaxObjects(); i++)
				v.addComponent(NULL_INT_REPRESENTATION);
		else
			for (int i = 0; i < canonicalClass.getCanonicalFields().size(); i++)
				v.addComponent(NULL_INT_REPRESENTATION);
	}

	private void addToCandidateVector(CanonicalObject obj, CanonicalHeap heap, CandidateVector<Integer> v) {
		int comp;
		Map.Entry<CanonizationResult, List<CanonicalField>> getFieldsRes = obj.getCanonicalFields();
		assert getFieldsRes.getKey() == CanonizationResult.OK : 
			"The heap should not be modified the heap while printing a candidate vector.";
		
		int fieldNumber = 0;
		for (CanonicalField fld: getFieldsRes.getValue()) {
			Object value = fld.getValue(obj);
			Map.Entry<CanonizationResult, CanonicalObject> canRes = heap.getCanonicalObject(value);
			assert canRes.getKey() == CanonizationResult.OK : 
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