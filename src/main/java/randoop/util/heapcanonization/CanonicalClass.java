package randoop.util.heapcanonization;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;


public class CanonicalClass {

	private static int globalID = 0;
	private String name;
	private int ID;
	private List<CanonicalField> fields;
	private boolean isPrimitive;
	private boolean isArray;
	private CanonicalClass ancestor;
	private Class<?> clazz;

	public CanonicalClass(String name) {
		this.name = name;
		ID = globalID++;
		fields = new LinkedList<>();
		try {
			clazz = Class.forName(name);
		} catch (ClassNotFoundException e) {
			assert false: "ERROR: Loading class " + name + " failed.";
		}
		isPrimitive = isPrimitive(clazz);
		//isArray = isArray(clazz);

		//visitAncestorsAndFields(store);
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
	

	public void visitAncestorsAndFields(CanonicalStore store) {
		if (isPrimitive) return;
		
		Class<?> ancestor = clazz.getSuperclass();
		if (!isPrimitive(ancestor)) {
			CanonicalClass canonicalAnc = store.getCanonicalClass(ancestor.getName());
			if (!canonicalAnc.isPrimitive()) {
				this.ancestor = canonicalAnc;
				fields.addAll(canonicalAnc.getCanonicalFields());
			}
		}
		
		for (Field fld: clazz.getDeclaredFields()) {
			Class<?> fldType = fld.getType();
			CanonicalClass fCanonicalType = null;
			if (!isPrimitive(fldType))
				fCanonicalType = store.getCanonicalClass(fldType.getName());
			fields.add(new CanonicalField(fld, this, fCanonicalType));
		}
	}
		
  	
   	public List<CanonicalField> getCanonicalFields() {
  		return fields;
	}
 	
	private boolean isPrimitive(Class<?> clazz) {
  		return (clazz.isPrimitive()
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
  		assert false: "ERROR: Creating an array class. Array classes are not supported yet.";
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
		String res = getName();
		res += ", ID=" + ID + ", ";
		for (CanonicalField fld: fields) {
			res += ", " + fld.toString();
		}
		return res;
	}


}
