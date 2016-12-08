package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;

public class CanonicalRepresentation {
	
	
	
	public static String getFieldCanonicalName(Field f) {
		//return f.getName();
		return getClassCanonicalName(f.getDeclaringClass()) + "." + f.getName();
	}

	
	public static String getClassCanonicalName(Class c) {
		return c.getCanonicalName();
	}
	
	
	public static String getVertexCanonicalName(HeapVertex v) {
		Object object = v.getObject();
		if (object == null) return "null";
		return getClassCanonicalName(object.getClass()) + v.getIndex();
	}
	
  	private static boolean isPrimitive(Class clazz) {
  		return (clazz.isPrimitive()
  				|| clazz == java.lang.Short.class
  				|| clazz == java.lang.Long.class
  				|| clazz == java.lang.String.class
  				|| clazz == java.lang.Integer.class
  				|| clazz == java.lang.Float.class
  				|| clazz == java.lang.Byte.class
  				|| clazz == java.lang.Character.class
  				|| clazz == java.lang.Double.class
  				|| clazz == java.lang.Boolean.class
  				|| clazz == java.util.Date.class
  				// FIXME: Not sure if this is the best place to put these classes.
  				/*|| clazz. == java.util.Locale.class
  				|| clazz == java.util.ResourceBundle.class*/
  				|| clazz.isEnum());
  	}
  	
  	public static boolean isEnum(Object value) {
  		return value.getClass().isEnum();
  	}
  	
  	public static boolean isPrimitive(Object value) {
  		if (value instanceof java.util.Locale || value instanceof java.util.ResourceBundle)
  			return true;
  		
  		return isPrimitive(value.getClass());
  	}

  	/*
  	public static String getSimpleNameWithoutArrayQualifier(Class clazz) {
  		String simpleName = clazz.getSimpleName();
  		int indexOfBracket = simpleName.indexOf('[');
  		if (indexOfBracket != -1) return simpleName.substring(0, indexOfBracket);
  		return simpleName;
  	}
  	*/
}
