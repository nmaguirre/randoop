package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

public class CanonicalRepresentation {
	
	public final static Integer MAX_STRING_SIZE = 50;
	
	private static Map<Class, String> classNames = new HashMap<Class, String> (); 
	
	public static String getFieldCanonicalName(Field f) {
		return getClassCanonicalName(f.getDeclaringClass()) + "." + f.getName();
	}

	/*
	public static String getFieldCanonicalName(Field f, Class c) {
		return getClassCanonicalName(c) + "." + f.getName();
	}*/
	
	public static String getArrayFieldCanonicalName(Class c, Integer pos) {
		return getClassCanonicalName(c) + ".elem" + pos;
	}

	public static String getPrimitiveFieldCanonicalName(Class c) {
		return getClassCanonicalName(c) + ".value";
	}
	
	
	public static String getClassCanonicalName(Class c) {
		String name = classNames.get(c);
		if (name == null) {
			name = c.getCanonicalName();
			classNames.put(c, name);
		}

		return name;
	}
	
	
	public static String getVertexCanonicalName(HeapVertex v) {
		Object object = v.getObject();
		if (object == null) return "null";
		return getClassCanonicalName(object.getClass()) + v.getIndex();
	}

	
  	public static boolean isClassPrimitive(Class clazz) {
  		return (clazz.isPrimitive()
  				/*
  				|| clazz == short.class
  				|| clazz == long.class
  				|| clazz == int.class
  				|| clazz == float.class
  				|| clazz == byte.class
  				|| clazz == char.class
  				|| clazz == double.class
  				|| clazz == boolean.class*/
   				|| clazz == Short.class
  				|| clazz == Long.class
  				|| clazz == String.class
  				|| clazz == Integer.class
  				|| clazz == Float.class
  				|| clazz == Byte.class
  				|| clazz == Character.class
  				|| clazz == Double.class
  				|| clazz == Boolean.class
  				|| clazz == Date.class
  				// FIXME: Not sure if this is the best place to put these classes.
  				|| clazz == java.util.ResourceBundle.class
				|| clazz == java.lang.ClassLoader.class
				|| clazz == java.awt.color.ICC_Profile.class
				|| clazz == java.net.ServerSocket.class
  				//|| clazz. == java.util.Locale.class
  				|| clazz.isEnum());
  	}
  	
  	public static boolean isEnum(Object value) {
  		return value.getClass().isEnum();
  	}
  	
  	public static boolean isObjectPrimitive(Object value) {
  		// FIXME: ResourceBundle has a cache that expires and breaks field based generation.
  		// For now we will consider ResourceBundle as primitive to avoid canonization of its objects.
  		// We do not want to deal with ClassLoader, ICC_Profile for the same reason.
  		if (value instanceof java.util.ResourceBundle 
  				|| value instanceof java.lang.ClassLoader
  				|| value instanceof java.awt.color.ICC_Profile
  				|| value instanceof java.net.ServerSocket
  				// Locales are huge and do not help 
//  				|| value instanceof java.util.Locale
  				) 
  			return true;
  	/*	if (value instanceof java.util.ResourceBundle)
  				//|| value instanceof java.util.Locale ||
  				//value instanceof java.util.TimeZone ||
  				//value instanceof java.util.Calendar)
  			return true;
  		*/
  		return isClassPrimitive(value.getClass());
  	}

	public static String getNullRepresentation() {
		return "null";
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
