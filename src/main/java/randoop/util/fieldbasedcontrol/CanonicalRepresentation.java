package randoop.util.fieldbasedcontrol;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

public class CanonicalRepresentation {
	
	public final static Integer MAX_STRING_SIZE = 50;
	
	private static int classIndex = -1; 
	
	private static int fieldIndex = -1;
	
	private static Map<Class, Tuple<String, Integer>> classNames = new HashMap<Class, Tuple<String, Integer>> ();
	private static Map<Field, Tuple<String, Integer>> fieldNames = new HashMap<Field, Tuple<String, Integer>> ();
	// private static Map<String, Integer> arrayFieldNames = new HashMap<String, Integer> ();
	private static Map<Class, Map<Integer, Tuple<String, Integer>>> arrayFieldNames = new HashMap<Class, Map<Integer, Tuple<String, Integer>>> ();
	
	public static String getArrayFieldCanonicalName(Class c, Integer pos) {
		return getArrayFieldCanonicalNameAndIndex(c, pos).getFirst();
	}
	
	public static Tuple<String, Integer> getArrayFieldCanonicalNameAndIndex(Class c, Integer pos) {
		Map<Integer, Tuple<String, Integer>> m1 = arrayFieldNames.get(c);
		if (m1 == null) {
			m1 = new HashMap<Integer, Tuple<String, Integer>>();
			arrayFieldNames.put(c, m1);
		}
	
		Tuple<String, Integer> t = m1.get(pos);
		if (t != null) return t;
		
		String name = getClassCanonicalName(c) + ".elem" + pos;
		t = new Tuple<String, Integer>(name, ++fieldIndex);
		m1.put(pos, t);
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored array field " + name + " with index " + fieldIndex);
		return t;
	}
	
	public static String getFieldCanonicalName(Field f) {
		return getFieldCanonicalNameAndIndex(f).getFirst();
	}

	public static Tuple<String, Integer> getFieldCanonicalNameAndIndex(Field f) {
		Tuple<String, Integer> tuple = fieldNames.get(f);
		if (tuple != null) return tuple;
		
		String name = getClassCanonicalName(f.getDeclaringClass()) + "." + f.getName();
		tuple = new Tuple<String, Integer>(name, ++fieldIndex);
		fieldNames.put(f, tuple);
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored field " + name + " with index " + fieldIndex);
		return tuple;
	}

	public static String getClassCanonicalName(Class c) {
		return getClassCanonicalNameAndIndex(c).getFirst();
	}

	public static Tuple<String, Integer> getClassCanonicalNameAndIndex(Class c) {
		Tuple<String, Integer> tuple = classNames.get(c);
		if (tuple != null) return tuple;
		
		String name = c.getName();
		// FIXME: To handle anonymous classes correctly, not sure if this is still needed
		if (name == null) {
			name = c.getCanonicalName();
		}
		
		tuple = new Tuple<String, Integer>(name, ++classIndex);
		classNames.put(c, tuple);
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored class " + name + " with index " + classIndex);
		return tuple;
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
	
	// DEPRECATED: Only used for backwards compatibility, we shouldn't use these methods anymore.
	public static String getFieldCanonicalName(Field f, Class c) {
		return getClassCanonicalName(c) + "." + f.getName();
	}
	
	public static String getVertexCanonicalName(HeapVertex v) {
		Object object = v.getObject();
		if (object == null) return "null";
		return getClassCanonicalName(object.getClass()) + v.getIndex();
	}
	
	public static String getPrimitiveFieldCanonicalName(Class c) {
		return getClassCanonicalName(c) + ".value";
	}
}
