package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

public class CanonicalRepresentation {
	
	public final static Integer MAX_STRING_SIZE = 50;
	
	private static Map<Class, String> classNames = new HashMap<Class, String> ();
	//private static Map<Field, String> fieldNames = new HashMap<Field, String> ();
	
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
			//name = c.getCanonicalName();
			name = c.getName();
			
			// To handle anonymous classes correctly
			if (name == null) {
				name = c.getCanonicalName();
				//name = c.getName();
				//System.out.println("NEW CLASS NAME FOUND: " + c.getName());
			}
			//System.out.println("NEW CLASS NAME FOUND: " + name);
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
  		if (clazz.isPrimitive()
   				|| clazz == Short.class
  				|| clazz == Long.class
  				|| clazz == String.class
  				|| clazz == Integer.class
  				|| clazz == Float.class
  				|| clazz == Byte.class
  				|| clazz == Character.class
  				|| clazz == Double.class
  				|| clazz == Boolean.class
  				// || clazz == Date.class
  				|| clazz == Object.class
		  		|| clazz.isEnum()
				// FIXME: Not sure if this is the best place to put these classes.
				//|| clazz == java.util.ResourceBundle.class
				//|| clazz == java.lang.ClassLoader.class
				// This class shows a bug during canonization only
				//|| clazz == java.awt.color.ICC_Profile.class
				)  			
  			return true;
  		
  		/*
  		String canonicalName = CanonicalRepresentation.getClassCanonicalName(clazz);
  		// Anonymous classes have a null canonicalName, and are considered primitive
  		// for the moment
  		if (canonicalName == null) 
  			throw new RuntimeException("ERROR: Class " + clazz.getName() + "has null name, and this should never happen.");
  		
  		if (canonicalName.startsWith("java.io.") ||
  				canonicalName.startsWith("java.nio.") ||
  				canonicalName.startsWith("java.lang.reflect") ||
  				canonicalName.startsWith("java.net.") ||
  				canonicalName.startsWith("java.security.") ||
  				//canonicalName.startsWith("java.lang.") ||
  				canonicalName.startsWith("java.awt.") ||
  				canonicalName.startsWith("java.beans.") ||
  				//canonicalName.startsWith("java.util.concurrent") ||
  				//canonicalName.startsWith("java") ||
  				canonicalName.startsWith("sun.") ||
  				canonicalName.startsWith("com.sun.")
  				) {
  			//System.out.println("WARNING: Ignored class: " + canonicalName);
  			return true;
  		}
  		*/
  		
  		return false;
  	}
  	
  	
  	
  	
  	
  	
  	
  	/*
  	private static boolean isPrimitive(Class clazz) {
  		return (clazz.isPrimitive()
  				
  				|| clazz == short.class
  				|| clazz == long.class
  				|| clazz == int.class
  				|| clazz == float.class
  				|| clazz == byte.class
  				|| clazz == char.class
  				|| clazz == double.class
  				|| clazz == boolean.class
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
		  		|| clazz.isEnum()
		  		);
  	}
  	*/
  	
  	public static boolean isEnum(Object value) {
  		return value.getClass().isEnum();
  	}
  	
  	public static boolean isObjectPrimitive(Object value) {
  		// FIXME: ResourceBundle has a cache that expires and breaks field based generation.
  		// For now we will consider ResourceBundle as primitive to avoid canonization of its objects.
  		// We do not want to deal with ClassLoader
  		/*if (value instanceof java.util.ResourceBundle 
  				//|| value instanceof java.lang.ClassLoader
  				// FIXME: There is a bug with this class that only occurs during canonization
  				//|| value instanceof java.awt.color.ICC_Profile
  				// Locales are huge and do not help 
//  				|| value instanceof java.util.Locale
  				) 
  			return true;*/
  		

  		return isClassPrimitive(value.getClass());
  	}

	public static String getNullRepresentation() {
		return "null";
	}

	public static String getDummyObjectRepresentation() {
		return "_DUMMY_";
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
