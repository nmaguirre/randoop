package randoop.util.heapcanonicalization.candidatevectors;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.checkerframework.checker.units.qual.s;

import randoop.generation.AbstractGenerator;
import randoop.util.heapcanonicalization.CanonicalClass;
import randoop.util.heapcanonicalization.CanonicalField;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalObject;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;
import randoop.util.heapcanonicalization.DummyHeapRoot;

public class SymbolicCandidateVectorGenerator extends CandidateVectorGenerator {
	
	public SymbolicCandidateVectorGenerator(CanonicalStore store, String rootClass) {
		super(store, rootClass);
	}

	public SymbolicCandidateVectorGenerator(CanonicalStore store, String rootClass, boolean noPrimitiveFields) {
		super(store, rootClass, noPrimitiveFields);
	}
	
	public SymbolicCandidateVectorGenerator(CanonicalStore store) {
		super(store);
	}
	
	// Should be passed only those classes that were obtained from the code of the classes
	// we will canonize, before test generation starts. In this way, classes added to 
	// the store later during generation are ignored.
	public SymbolicCandidateVectorGenerator(CanonicalStore store, boolean noPrimitiveFields) {
		super(store, noPrimitiveFields);
	}
	
	
	@Override
	protected void addToCandidateVector(CanonicalObject obj, CanonicalHeap heap, CandidateVector<Object> v) {
		Object comp;
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
			else if (canValue.isPrimitive() || canValue.getCanonicalClass().getName().equals("randoop.util.heapcanonicalization.DummySymbolicObject"))
				comp = -1;
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
