package randoop.util.heapcanonization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map.Entry;

public class CanonicalField {

	private static int globalID = 0;
	private final int ID;
	private final Field field;
	private final CanonicalClass clazz;
	private final CanonicalClass type;
	private final String name;

	// Creates fields for non primitive types 
	public CanonicalField(Field field, CanonicalClass clazz, CanonicalClass type) {
		ID = globalID++;
		this.field = field;
		field.setAccessible(true);
		this.name = field.getName();
		this.clazz = clazz;
		this.type = type;
	}
	
	// Creates (dummy) array fields 
	public CanonicalField(Integer name, CanonicalClass clazz, CanonicalClass type) {
		ID = name;
		this.name = Integer.toString(name);
		this.field = null;
		this.clazz = clazz;
		this.type = type;
	}

	public String getName() {
		return name;
	}
	
	public int getIndex() { 
		return ID;
	}

	public CanonicalClass getCanonicalType() {
		return type;
	}

	public CanonicalClass getCanonicalClass() {
		return clazz;
	}
	
	public boolean isPrimitiveType() {
		return type.isPrimitive();
	}
	
	public boolean isArrayType() {
		return type.isArray();
	}

	public Object getValue(CanonicalObject canObj) {
		if (clazz.isArray()) {
			return Array.get(canObj.getObject(), ID);
		} 
		else {
			Object target = null;
			try {
				target = field.get(canObj.getObject());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				System.out.println("Cannot find field: " + field.getName());
				System.out.println("Error object: " + canObj.getObject());
				assert false: "Cannot find an existing field";
				System.exit(1);
			}
			return target;
		}
		//heap.getCanonicalObject(target);
	}

	public String toString() {
		return "{" + getName() + ",ID=" + ID + ",class="+ clazz.getName() + ",type=" + type.getName() + "}";
	}
	
}

