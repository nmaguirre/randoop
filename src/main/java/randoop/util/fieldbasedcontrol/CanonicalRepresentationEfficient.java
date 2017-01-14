package randoop.util.fieldbasedcontrol;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class CanonicalRepresentationEfficient {
	
	private List<String> ignoredClasses;

	public boolean isIgnoredClass(CanonizerClass cc) {
		if (fieldBasedGenByClasses)
			return !belongsToFieldBasedClasses(cc);
		else
			return isIgnoredNonClassBased(cc);
	}
	
	private boolean isIgnoredNonClassBased(CanonizerClass cc) {
		for (String s: ignoredClasses) {
			if (cc.name.startsWith(s)) {
				//System.out.println("WARNING: Ignored class: " + canonicalName);				
				return true;
			}
		}

		return false;
	}
	
	public boolean belongsToFieldBasedClassesOrParents(CanonizerClass cc) {
		
		// Allow primitive fields and arrays to contribute to the extensions
		if (classIndexPrimitive.get(cc.index) || c.isArray())
			return true;
		
		return fieldBasedGenClassesAndParents.contains(t.getFirst());
	}
	
	public boolean belongsToFieldBasedClasses(CanonizerClass cc) {
		// Allow primitive fields and arrays to contribute to the extensions
		if (cc.primitive || cc.cls.isArray())
			return true;
	
		return fieldBasedGenClasses.contains(cc.index);
	}
	
	
	private static boolean fieldBasedGenByClasses = false;
	
	private static Set<Integer> fieldBasedGenClasses;
	private static Set<Integer> fieldBasedGenClassesAndParents;
	
	public CanonicalRepresentationEfficient() {
		fieldBasedGenByClasses = false;
		ignoredClasses = Arrays.asList(new String [] {"java.io.", "java.nio.",
			"java.lang.reflect.", "java.net.", "java.security.", "java.beans.", "sun.", "com.sun."});
	}
	
	public CanonicalRepresentationEfficient(Set<String> classNames) {
		ignoredClasses = new LinkedList<String>();
		fieldBasedGenByClasses = true;
		
		fieldBasedGenClasses = new HashSet<>();
		fieldBasedGenClassesAndParents = new HashSet<>();
		
		// For each given class, put its superclasses in fieldBasedGenClassnames to avoid 
		// discarding fields that have superclasses as types.
		for (String name: classNames) {
			Class cls = null;
			try {
				cls = Class.forName(name);
			} catch (ClassNotFoundException e) {
				try {
					if (FieldBasedGenLog.isLoggingOn())
						FieldBasedGenLog.logLine("> Class " + name + " not found");

					int last = name.lastIndexOf(".");
					name = name.substring(0, last) + "$" + name.substring(last+1);
					if (FieldBasedGenLog.isLoggingOn())
						FieldBasedGenLog.logLine("> Trying: " + name);
					cls = Class.forName(name);
				} catch (ClassNotFoundException e2) {
					if (FieldBasedGenLog.isLoggingOn())
						FieldBasedGenLog.logLine("FATAL ERROR DURING CANONIZATION: Class " + name + " not found. Check your --field-based-gen-classnames file for errors");
					System.out.println("FATAL ERROR DURING CANONIZATION: Class " + name + " not found. Check your --field-based-gen-classnames file for errors");
					e.printStackTrace();
					System.exit(1);
				}
			}

			Tuple<String, Integer> classTuple = getClassCanonicalNameAndIndex(cls);
			String cname = classTuple.getFirst();
			Integer cind = classTuple.getSecond();	
		
			fieldBasedGenClasses.add(cind);
			fieldBasedGenClassesAndParents.add(cind);
			if (FieldBasedGenLog.isLoggingOn()) 
				FieldBasedGenLog.logLine("> Added " + cname + " for canonization");
						
			cls = cls.getSuperclass();
			classTuple = getClassCanonicalNameAndIndex(cls);
			cname = classTuple.getFirst();
			cind = classTuple.getSecond();	
			
			while (cls != null && 
					cls != Object.class && 
					!classIndexPrimitive.get(cind)) {
				if (fieldBasedGenClassesAndParents.add(cind)) {
					if (FieldBasedGenLog.isLoggingOn())
						FieldBasedGenLog.logLine("> Added " + name + "'s parent " + cname + " for canonization");
				}
			
				cls = cls.getSuperclass();
				classTuple = getClassCanonicalNameAndIndex(cls);
				cname = classTuple.getFirst();
				cind = classTuple.getSecond();	
			}
		}		

	}

	
	public final Integer MAX_STRING_SIZE = 50;
	
	private Map<Class, CanonizerClass> classNames = new HashMap<Class, CanonizerClass>();
	private ArrayList<CanonizerClass> indexToClass = new ArrayList<CanonizerClass>();
	private Map<Class, Map<Integer, CanonizerArrayField>> arrayFieldNames = new HashMap<Class, Map<Integer, CanonizerArrayField>>();
	


	
	
	
	// For each class index stores a list of the objects corresponding to the class.
	// The position of the object in the list corresponds to the object's index in the canonization
	public static ArrayList<LinkedList<Object>> store = new ArrayList<>();
	
	public static void clearStore() {
		for (LinkedList l: store) {
			l.clear();
		}
	}

	public static Integer getObjectIndex(Integer classIndex, Object o) {
		List<Object> l = store.get(classIndex);
		
		if (l == null) return -1;
		
		int ind = 0;
		for (Object obj: l) {
			if (o == obj) 
				return ind;			

			ind++;
		}
		
		return -1; 
	}
	
	// Returns a tuple (old index, new index)
	// if old index == new index then o was stored in a previous call
	// old index is -1 if the object wasn't stored previously
	public static Tuple<Integer, Integer> assignIndexToObject(Integer clsIndex, Object o) {
		List<Object> l = store.get(clsIndex);

		int ind = 0;
		for (Object obj: l) {
			if (o == obj)
				break;

			ind++;
		}
		
		if (ind == l.size()) {
			lastIndex.set(clsIndex, lastIndex.get(clsIndex) + 1);
			l.add(o);
			return new Tuple<Integer, Integer>(-1, lastIndex.get(clsIndex));
		}
		
		return new Tuple<Integer, Integer>(ind, ind);
	}
	
	
	
	// For each class name stores the last index assigned to an object of the class
	public static ArrayList<Integer> lastIndex;
	
	public static void resetLastIndexes() {
		for (int i = 0; i < lastIndex.size(); i++)
			lastIndex.set(i, -1);
	}

	public static void clearStoreAndIndexes()	{
		clearStore();
		resetLastIndexes();
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
		
		Tuple<String, Integer> classTuple = getClassCanonicalNameAndIndex(f.getDeclaringClass());
		String cname = classTuple.getFirst();
		int cind = classTuple.getSecond();

		String fname = cname + "." + f.getName();
		tuple = new Tuple<String, Integer>(fname, ++fieldIndex);
		fieldNames.put(f, tuple);
		indexToField.add(f);
		assert indexToField.size() == fieldIndex;
		
		classFields.get(cind).add(fieldIndex);
		
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored field " + fname + " with index " + fieldIndex + 
					", belonging to class " + cname + " with index " + cind + ". "
					+ "Pair (" + cind + "," + fieldIndex + ") added to class fields.");

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

		indexToClass.add(c);
		assert indexToClass.size() == classIndex;

		classIndexPrimitive.add(isClassPrimitive(c));
		assert classIndexPrimitive.size() == classIndex;
		
		classFields.add(new LinkedList<Integer>());
		assert classFields.size() == classIndex;
		
		store.add(new LinkedList<Object>());
		assert store.size() == classIndex;
		
		lastIndex.add(-1);
		assert lastIndex.size() == classIndex;
		
		loadFieldsForClass(c, tuple);
			
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored class " + name + " with index " + classIndex);
		return tuple;
	}
	
	
	// Returns the list with cls' fields. It stores the fields 
	// the first time to return them in the same order later.
	private static void loadFieldsForClass(Class cls, Tuple<String, Integer> tuple) {
		String classname = tuple.getFirst();
		Integer classindex = tuple.getSecond();
		
	
		while (cls != null && 
				cls != Object.class && 
				!CanonicalRepresentation.isClassPrimitive(cls) &&
				!isIgnoredClass(cls, tuple)) {
			
			if (FieldBasedGenLog.isLoggingOn()) 
				FieldBasedGenLog.logLine("> Considering fields for class " + classname + ": ");
						
			Field [] fieldsArray = cls.getDeclaredFields();
			for (int i = 0; i < fieldsArray.length; i++) {
				Field f = fieldsArray[i];
				
				Class ftype = f.getType();
				Tuple<String, Integer> ftypetuple = getClassCanonicalNameAndIndex(ftype);

				if (isIgnoredClass(ftypetuple.getFirst())) {
					// System.out.println("Ignored: " + f.getType() + " " + f.getName());
					continue;
				}
				if (fieldBasedGenByClasses && !belongsToFieldBasedClassesOrParents(ftype, ftypetuple))
					continue;
			
			
				Tuple<String, Integer> ftuple = getFieldCanonicalNameAndIndex(f);
				if (FieldBasedGenLog.isLoggingOn()) 
					FieldBasedGenLog.logLine(ftypetuple.getFirst() +  " " +  ftuple.getFirst());			
			}
				
			if (FieldBasedGenLog.isLoggingOn())
				FieldBasedGenLog.logLine("> No more fields for: " + classname);

			cls = cls.getSuperclass();
			tuple = getClassCanonicalNameAndIndex(cls);
			classname = tuple.getFirst();
			classindex = tuple.getSecond();	
		}
		
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
