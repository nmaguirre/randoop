package randoop.util.heapcanonization;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class CanonicalClass {

	private static int globalID = 0;
	private final String name;
	private final int ID;
	private final List<CanonicalField> fields;
	private final boolean isPrimitive;
	private final boolean isArray;
	private final CanonicalClass ancestor;
	private final Class<?> clazz;
	private final CanonicalStore store;

	public CanonicalClass(String name, CanonicalStore store) {
		this.store = store;
		this.name = name;
		ID = globalID++;
		fields = new LinkedList<>();

		Class<?> cls = null;
		try {
			cls = Class.forName(name);
		} catch (ClassNotFoundException e) {
			if (CanonizerLog.isLoggingOn())
				CanonizerLog.logLine("WARNING: Class for name " + name + " not found, assuming it is a primitive type.");
		}
		clazz = cls;
		isPrimitive = isPrimitive(clazz);
		//isArray = isArray(clazz);
		isArray = false;

		if (!isPrimitive) {
			ancestor = createAncestors();
			addFields();
		}
		else 
			ancestor = null;
	}
	
	/* TODO: We should use something like this method if loading of classes fail for any class.
	public static String getCanonicalClassName(Class<?> clazz) {
		String name = clazz.getName();
		// FIXME: To handle anonymous classes correctly, not sure if this is still needed
		if (name == null) {
			name = clazz.getCanonicalName();
		}
		return name;
	}
	*/

	private CanonicalClass createAncestors() {
		CanonicalClass ancestor = store.getCanonicalClass(clazz.getSuperclass());
		if (!ancestor.isPrimitive()) {
			return ancestor;
		}
		return null;
	}
		
	private void addFields() {
		if (ancestor != null)
			fields.addAll(ancestor.getCanonicalFields());
		
		List<Field> sortedFields = Arrays.asList(clazz.getDeclaredFields());
		Collections.sort(sortedFields, new FieldComparatorByName());
		for (Field fld: sortedFields) {
			Class<?> fldType = fld.getType();
			CanonicalClass fCanonicalType = null;
			if (fldType.getName().equals(name))
				fCanonicalType = this;
			else
				fCanonicalType = store.getCanonicalClass(fldType.getName());
			fields.add(new CanonicalField(fld, this, fCanonicalType));
		}
	}
  	
   	public List<CanonicalField> getCanonicalFields() {
  		return fields;
	}
 	
	private boolean isPrimitive(Class<?> clazz) {
  		return (clazz == null) 
  				|| (clazz.isPrimitive()
   				|| clazz == Short.class
  				|| clazz == Long.class
  				|| clazz == String.class
  				|| clazz == Integer.class
  				|| clazz == BigInteger.class
  				|| clazz == BigDecimal.class
  				|| clazz == Float.class
  				|| clazz == Byte.class
  				|| clazz == Character.class
  				|| clazz == Double.class
  				|| clazz == Boolean.class
  				// || clazz == Date.class
  				|| clazz == Object.class
		  		|| clazz.isEnum()
				);
  	}	
	
	public boolean isPrimitive() {
		return isPrimitive;
	}
  	
  	private boolean isArray(Class<?> clazz) {
  		// assert false: "ERROR: Creating an array class. Array classes are not supported yet.";
  		return clazz.isArray();
  	}	
  	
  	public boolean isArray() {
  		return isArray;
  	}
	
	public String getName() {
		return name;
	}

	public int getID() {
		return ID;
	}
	
	public CanonicalClass getAncestor() {
		return ancestor;
	}

	public CanonicalStore getStore() {
		return store;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ID;
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
		CanonicalClass other = (CanonicalClass) obj;
		if (ID != other.ID)
			return false;
		return true;
	}

	public String toString() {
		String res = "name=" + getName() + ",ID=" + ID + ",fields=[";
		for (CanonicalField fld: fields) {
			res += fld.toString();
		}
		return res + "]";
	}


}