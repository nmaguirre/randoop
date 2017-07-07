package randoop.util.heapcanonization;

import java.lang.reflect.Field;
import java.util.Map.Entry;

public class CanonicalField {

	private static int globalID = 0;
	private int ID;
	private Field field;
	private CanonicalClass belongs;
	private CanonicalClass type;

	public CanonicalField(Field field, CanonicalClass belongs, CanonicalClass type) {
		ID = globalID++;
		this.field = field;
		field.setAccessible(true);
		this.belongs = belongs;
		this.type = type;
	}

	public String getName() {
		return field.getName();
	}
	
	public int getIndex() { 
		return ID;
	}

	public CanonicalClass getType() {
		return type;
	}

	public CanonicalClass getBelongs() {
		return belongs;
	}
	
	public boolean isPrimitiveType() {
//		return (type == null) ? true : type.isPrimitive();
		return type.isPrimitive();
	}
	
	public boolean isArrayType() {
//		return (type == null) ? false : type.isArray();
		return type.isArray();
	}

	public Entry<CanonizationResult, CanonicalObject> getTarget(CanonicalObject cobj, CanonicalHeap heap) {
		Object obj = cobj.getObject();
		Object target = null;
		try {
			target = field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.out.println("Error field: " + field.getName()); 
			System.out.println("Error object: " + obj);
			assert false: "ERROR: Cannot find an existing field.";
		}
		return heap.getCanonicalObject(target);
	}

	public String toString() {
		String res = "{ " + getName();
		res += " ID=" + ID + "} ";
		return res;
	}
	
}

