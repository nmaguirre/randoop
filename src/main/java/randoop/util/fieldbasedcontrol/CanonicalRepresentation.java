package randoop.util.fieldbasedcontrol;

public class CanonicalRepresentation {
	
	public static String getCanonicalName(Object object, int index) {
		if (object == null) return "null";
		
/*		if (isPrimitive(object.getClass()))
			return object.toString(); */
		
/*		if (object.getClass().isArray())
			return "ARR_" + getSimpleNameWithoutArrayQualifier(object.getClass()) + index;*/			

		return object.getClass().getSimpleName() + index;
	}
	
  	public static boolean isPrimitive(Class clazz) {
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
  				|| clazz == java.util.Locale.class
  				|| clazz.isEnum());
  	}
  	
  	public static boolean isEnum(Object value) {
  		return value.getClass().isEnum();
  	}
  	
  	public static boolean isPrimitive(Object value) {
  		return isPrimitive(value.getClass());
  	}

  	public static String getSimpleNameWithoutArrayQualifier(Class clazz) {
  		String simpleName = clazz.getSimpleName();
  		int indexOfBracket = simpleName.indexOf('[');
  		if (indexOfBracket != -1) return simpleName.substring(0, indexOfBracket);
  		return simpleName;
  	}
}
