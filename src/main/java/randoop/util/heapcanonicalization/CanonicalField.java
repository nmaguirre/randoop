package randoop.util.heapcanonicalization;

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
	
	public boolean isObjectType() {
		return type.isObject();
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
				System.out.println("Cannot find existing field: " + field.getName());
				System.out.println("Error object: " + canObj.getObject());
				assert false: "Cannot find an existing field";
				System.exit(1);
			}
			return target;
		}
	}

	public String toString() {
		String typeStr = (type == null) ? "null" : type.getName();
		return "{" + getName() + ",ID=" + ID + ",class="+ clazz.getName() + ",type=" + typeStr + "}";
	}
	
	public void setValue(CanonicalObject obj, CanonicalObject val) {
		try {
			field.set(obj.getObject(), val.getObject());
		} catch (IllegalArgumentException | IllegalAccessException e) {
			System.out.println("Cannot find existing field: " + field.getName());
			System.out.println("Error objects: " + obj.getObject() + ", " + val.getObject());
			assert false: "Cannot find an existing field";
			System.exit(1);
		}
	}

	// The parameter is needed because this might be called for an object whose type 
	// is a subclass of the class where the field is defined.
	public String stringRepresentation(CanonicalObject obj) {
		return obj.getCanonicalClass().getName() + "." + getName();
	}
	
}

