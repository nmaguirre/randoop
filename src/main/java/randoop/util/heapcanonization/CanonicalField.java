package randoop.util.heapcanonization;

import java.lang.reflect.Field;

public class CanonicalField {

	private static int globalID = 0;
	private int ID;
	private Field field;
	private CanonicalClass belongs;
	private CanonicalClass type;

	public CanonicalField(Field field, CanonicalClass belongs, CanonicalClass type) {
		ID = globalID++;
		this.field = field;
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
		return (type == null) ? true : type.isPrimitive();
	}
	
	public boolean isArrayType() {
		return (type == null) ? false : type.isArray();
	}

	public CanonicalObject getTarget(CanonicalObject cobj, CanonicalHeap heap) throws LimitsExceededException {
		Object obj = cobj.getObject();
		if (obj == null)
			return heap.getCanonicalObject(obj, type);

		Object target = null;
		try {
			target = field.get(obj);
		} catch (Exception e) {
			System.out.println(field.getName()); 
			System.out.println(obj);
			assert false: "ERROR: Cannot find an existing field.";
		}
		return heap.getCanonicalObject(target, type);
	}

	public String toString() {
		String res = "{ " + getName();
		res += ", ID=" + ID + "} ";
		return res;
	}
	
}

