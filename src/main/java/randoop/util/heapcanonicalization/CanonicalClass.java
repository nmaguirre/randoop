package randoop.util.heapcanonicalization;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
	private final boolean isObject;
	private final boolean isPrimitive;
	private final boolean isArray;
	private final boolean isAbstract;
	private final boolean isInterface;
	private final CanonicalClass ancestor;
	private final Class<?> clazz;
	//private final CanonicalClass arrObjectsType;
	private final CanonicalStore store;
	private int fieldDistance;

	public CanonicalClass(String name, CanonicalStore store, int fieldDistance, int maxFieldDistance) {
		this.store = store;
		this.name = name;
		ID = globalID++;
		fields = new LinkedList<>();

		Class<?> cls = null;
		try {
			cls = Class.forName(name);
		} catch (ClassNotFoundException e) {
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("CANONICALIZER INFO: Class for name " + name + " not found, assuming it's a primitive type.");
		}
		clazz = cls;
		isObject = isObject(clazz);
		isPrimitive = isPrimitive(clazz);
		isArray = isArray(clazz);
		//arrObjectsType = (!isArray) ? null : store.getUpdateOrCreateCanonicalClass(clazz.getComponentType(), fieldDistance);
		isInterface = isInterface(clazz);
		isAbstract = isAbstract(clazz);
		
		if (isObject || isPrimitive)
			this.fieldDistance = 0;
		else if (isArray)
			// For lack of a better method, for the moment the field distance of arrays is always set to maxFieldDistance-1.
			this.fieldDistance = maxFieldDistance-1;
		else
			this.fieldDistance = fieldDistance;
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("CANONICALIZER INFO: Class " + name + " created. Field distance=" + this.fieldDistance);
		
		ancestor = canonicalizeAncestors();
		if (this.fieldDistance < maxFieldDistance)
			canonicalizeFields();
	}
	
	private boolean isInterface(Class<?> clazz) {
		return (clazz == null || isObject || isPrimitive || isArray) ? false : Modifier.isInterface(clazz.getModifiers());
	}
	
	private boolean isAbstract(Class<?> clazz) {
		return (clazz == null || isObject || isPrimitive || isArray) ? false : Modifier.isAbstract(clazz.getModifiers());
	}

	private CanonicalClass canonicalizeAncestors() {
		/* 
		 * Ugly hack to deal with weird randoop test:
		 * Object o = new Object();
		 */
		if (isObject || isPrimitive || isArray || isInterface) return null;

		CanonicalClass ancestor = store.getUpdateOrCreateCanonicalClass(clazz.getSuperclass(), fieldDistance);
		if (!ancestor.isPrimitive() && !ancestor.clazz.equals(Object.class)) {
			return ancestor;
		}
		return null;
	}
		
	private void canonicalizeFields() {
		/* 
		 * Ugly hack to deal with weird randoop test:
		 * Object o = new Object();
		 */
		if (isObject || isPrimitive || isArray || isInterface) return;

		if (ancestor != null)
			fields.addAll(ancestor.getCanonicalFields());
		
		List<Field> sortedFields = Arrays.asList(clazz.getDeclaredFields());
		Collections.sort(sortedFields, new FieldComparatorByName());
		for (Field fld: sortedFields) {
			Class<?> fldType = fld.getType();
			// If this is an inner class, ignore the this$0 field pointing to its enclosing class.
			if (fld.getName().equals("this$0") && clazz.getEnclosingClass() != null)
				continue;
			
			CanonicalClass fCanonicalType = null;
			if (fldType.getName().equals(name))
				fCanonicalType = this;
			else 
				fCanonicalType = store.getUpdateOrCreateCanonicalClass(fldType.getName(), fieldDistance+1);
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
  				//|| clazz == Object.class
		  		|| clazz.isEnum()
				);
  	}	
	
	private boolean isObject(Class<?> clazz) {
  		return (clazz == null) ? false : clazz == Object.class;
  	}	
	
	public boolean isPrimitive() {
		return isPrimitive;
	}
	
	public boolean isObject() {
		return isObject;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public boolean isInterface() {
		return isInterface;
	}
  	
  	private boolean isArray(Class<?> clazz) {
  		return (clazz == null) ? false : clazz.isArray();
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
		String res = "name=" + getName() + ",distance=" + fieldDistance + ",ID=" + ID + ",fields=[";
		for (CanonicalField fld: fields) {
			res += "\n\t" + fld.toString();
		}
		return res + "]";
	}

	/*
	public CanonicalClass getArrayElementsType() {
		assert isArray: "Asking for the type of objects of an array for a non array class";
		return arrObjectsType;
	}
	*/
	
	public boolean hasFieldReferencingItself() {
		for (CanonicalField f: fields)
			if (f.getCanonicalType().equals(this))
				return true;
		
		return false;
	}

	public void updateFieldDistance(int fieldDistance, int maxFieldDistance) {
		if (isObject || isPrimitive || isArray || this.fieldDistance <= fieldDistance)
			return;
		
		this.fieldDistance = fieldDistance;
		if (CanonicalizerLog.isLoggingOn())
			CanonicalizerLog.logLine("CANONICALIZER INFO: Updated class " + name + ". Field distance=" + fieldDistance);

		/*
		if (isArray) {
			return;
			//arrObjectsType.updateFieldDistance(fieldDistance, maxFieldDistance);
		}
		*/
		if (ancestor != null)
			ancestor.updateFieldDistance(fieldDistance, maxFieldDistance);
		if (fieldDistance < maxFieldDistance)
			canonicalizeFields();
	}

	public int getFieldDistance() {
		return fieldDistance;
	}

}
