package randoop.util.heapcanonicalization;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import randoop.generation.AbstractGenerator;


public class CanonicalObject {

	private final Object object;
	private final CanonicalClass clazz;
	private final int index;
	private final CanonicalHeap heap;
	
	public CanonicalObject(Object obj, CanonicalClass clazz, int index, CanonicalHeap heap) {
		this.object = obj;
		this.clazz = clazz;
		this.index = index;
		this.heap = heap;
	}

	public Object getObject() {
		return object;
	}

	public CanonicalClass getCanonicalClass() {
		return clazz;
	}
	
	public Entry<CanonicalizationResult, List<CanonicalField>> getCanonicalFields() {
		assert !clazz.isPrimitive(): "Should never try to get the fields of primitive types";
		if (clazz.isArray()) {
			int arrLength = Array.getLength(object);

			if (AbstractGenerator.vectorization_hard_array_limits && arrLength > heap.getMaxObjects())
				return new AbstractMap.SimpleEntry<>(CanonicalizationResult.ARRAY_LIMITS_EXCEEDED, null); 

			List<CanonicalField> arrFields = new LinkedList<>();
			if (arrLength > heap.getMaxArrayObjects()) {
				if (CanonicalizerLog.isLoggingOn())
					CanonicalizerLog.logLine("CANONICALIZER INFO: only considering the first " + heap.getMaxArrayObjects() + 
							" elements of an array of size " + arrLength);
				arrLength = heap.getMaxArrayObjects();
			}
			for (int i = 0; i < arrLength; i++) {
				if (Array.get(object, 0) != null) {
					CanonicalClass objClass = heap.getStore().getUpdateOrCreateCanonicalClass(Array.get(object, 0).getClass(), 2);
					arrFields.add(new CanonicalField(i, clazz, objClass));//clazz.getArrayElementsType()));
				}
				else
					arrFields.add(new CanonicalField(i, clazz, clazz.getArrayElementsType()));
			}
			return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, arrFields);
		}
		return new AbstractMap.SimpleEntry<>(CanonicalizationResult.OK, clazz.getCanonicalFields());
	}

	public int getIndex() {
		return index;
	}
	
	public boolean isNull() {
		return object == null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CanonicalObject other = (CanonicalObject) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	
	public boolean isPrimitive() {
		return getCanonicalClass().isPrimitive();
	}
	
	public boolean isArray() {
		return getCanonicalClass().isArray();
	}

	public String toString() {
		if (isNull())
			return "[type=null,index=" + index + "]";
		else if (isPrimitive())
			return "[type=" + clazz.getName() + ",value=" + getObject().toString() + ",hash=" + getObject().hashCode() + "]";
		else
			return "[type=" + clazz.getName() + ",index=" + index + "]";
	}
	
	public String stringRepresentation() {
		if (isNull())
			return "null";
		if (isPrimitive())
			return getObject().toString();
		else
			return getCanonicalClass().getName() + ":" + getIndex();
			//return getCanonicalClass().getID() + ":" + getIndex();
	}
	
	
}
