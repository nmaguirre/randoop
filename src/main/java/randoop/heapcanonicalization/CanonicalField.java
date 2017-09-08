package randoop.heapcanonicalization;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;

public class CanonicalField {

	private static int globalID = 0;
	private final int ID;
	private final Field field;
	private final boolean isFinal;
	private final CanonicalClass clazz;
	private final CanonicalClass type;
	private final String name;

	// Creates fields for non primitive types 
	public CanonicalField(Field field, CanonicalClass clazz, CanonicalClass type) {
		ID = globalID++;
		this.field = field;
		this.isFinal = isFinalField();
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
		this.isFinal = isFinalField();
		this.clazz = clazz;
		this.type = type;
	}

	
	private boolean isFinalField() {
		return (field == null) ? false : Modifier.isFinal(field.getModifiers());
	}
	
	
	public boolean isStatic() {
		return (field == null) ? false : Modifier.isStatic(field.getModifiers());
	}

	
	public boolean isFinal() {
		return isFinal;
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
		return /*(type == null) ? false: */ type.isObject();
	}
	
	public boolean isPrimitiveType() {
		return /*(type == null) ? false:*/ type.isPrimitive();
	}
	
	public boolean isArrayType() {
		return /*(type == null) ? true:*/ type.isArray();
	}

	public Object getValue(CanonicalObject canObj) {
		if (field == null /*clazz.isArray()*/) {
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
		return "{" + getName() + ",ID=" + ID + ",class="+ clazz.getName() + ",type=" + typeStr + ",final=" + isFinal + "}";
	}
	
	public void setValue(CanonicalObject obj, CanonicalObject val) {
		if (field != null) {
			try {
				field.set(obj.getObject(), val.getObject());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				System.out.println("Cannot find existing field: " + field.getName());
				System.out.println("Error objects: " + obj.getObject() + ", " + val.getObject());
				assert false: "Cannot find an existing field";
				System.exit(1);
			}
		}
		else {
			// Array field
			Array.set(obj.getObject(), ID, val.getObject());
		}
		
	}

	// The parameter is needed because this might be called for an object whose type 
	// is a subclass of the class where the field is defined.
	public String stringRepresentation(CanonicalObject obj) {
		return obj.getCanonicalClass().getName() + "." + getName();
	}
	
	
	public Field getField() {
		return field;
	}
}

