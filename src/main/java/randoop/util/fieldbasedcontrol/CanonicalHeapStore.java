package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class CanonicalHeapStore {
	
	//private static CanonicalHeapStore instance;
	
	// To canonize non primitive objects
//	public FieldExtensionsIndexes extensions;
	// To canonize primitive values 
//	public FieldExtensionsIndexes primitiveExtensions;
	
	// Max number of stored objects for each (non-primitive) individual class
	private int maxClassObjects;
	
	// Max total number of stored objects for (non-primitive) classes
	private int maxGlobalObjects;
	
	// Max number of characters for string objects
	public int maxStringLength;
	
	private boolean dropTestsExceedingLimits;

	private static boolean fieldBasedGenByClasses = false;
	List<String> ignoredClasses = Arrays.asList(new String [] {"java.io.", "java.nio.",
			"java.lang.reflect.", "java.net.", "java.security.", "java.beans.", "sun.", "com.sun.", "java.util.concurrent."});
	private static Set<Integer> fieldBasedGenClasses;
	private static Set<Integer> fieldBasedGenClassesAndParents;

	/*
	public static CanonicalHeapStore getInstance(FieldExtensionsIndexes extensions) {
		if (instance == null)
			instance = new CanonicalHeapStore(extensions);
		
		return instance;
	}*/
	
	public CanonicalHeapStore(int maxGlobalObjects, int maxClassObjects, int maxStringLength, boolean dropTestsExceedingLimits) {
		this.maxGlobalObjects = maxGlobalObjects;
		this.maxClassObjects = maxClassObjects;
		this.maxStringLength = maxStringLength;
		this.dropTestsExceedingLimits = dropTestsExceedingLimits;

	}

	private boolean isIgnoredFieldType(CanonizerClass cc) {
		if (fieldBasedGenByClasses) 
			return !belongsToFieldBasedClassesOrParents(cc);
		else 
			return isIgnoredNonClassBased(cc);
	}
	
	
	private boolean isIgnoredClass(CanonizerClass cc) {
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
		if (cc.primitive || cc.cls.isArray())
			return true;
		
		return fieldBasedGenClassesAndParents.contains(cc.index);
	}
	
	public boolean belongsToFieldBasedClasses(CanonizerClass cc) {
		// Allow primitive fields and arrays to contribute to the extensions
		if (cc.primitive || cc.cls.isArray())
			return true;
	
		return fieldBasedGenClasses.contains(cc.index);
	}
	
	
	public void setFieldBasedGenByClasses(Set<String> fbGenClasses) {
		ignoredClasses = null;
		fieldBasedGenByClasses = true;
		
		fieldBasedGenClasses = new HashSet<>();
		fieldBasedGenClassesAndParents = new HashSet<>();
		
		Set<Integer> addedClasses = new HashSet<>();
		// For each given class, put its superclasses in fieldBasedGenClassnames to avoid 
		// discarding fields that have superclasses as types.
		for (String name: fbGenClasses) {
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

			CanonizerClass cc = canonizeClass(cls, true);
			if (cc.primitive) continue;

			addedClasses.add(cc.index);

			fieldBasedGenClasses.add(cc.index);
			fieldBasedGenClassesAndParents.add(cc.index);

			if (FieldBasedGenLog.isLoggingOn()) 
				FieldBasedGenLog.logLine("> Added " + cc.name + " for canonization, with index " + cc.index);
			
			while (true) {
				cls = cc.cls.getSuperclass();
				if (cls == null) break;
				cc = canonizeClass(cls, true);
				if (cc.primitive) break;

				addedClasses.add(cc.index);

				if (fieldBasedGenClassesAndParents.add(cc.index)) 
					if (FieldBasedGenLog.isLoggingOn())
						FieldBasedGenLog.logLine("> Added " + name + "'s parent " + cc.name + " for canonization, with index" + cc.index);
			}
		}		
		
		// Now that we have the classes for field based generation figured out, 
		// we can add fields to those classes (some fields will not be added because 
		// their types do not belong to field based generation classes)
		for (Integer k: addedClasses) {
			CanonizerClass cc = indexToClass.get(k);
			if (!cc.primitive) {
				canonizeFields(cc);
				cc.ignored = !fieldBasedGenClasses.contains(k);
			}
		}
	}

	
	public Map<Class, CanonizerClass> classes = new HashMap<Class, CanonizerClass>();
	public ArrayList<CanonizerClass> indexToClass = new ArrayList<CanonizerClass>();
	public Map<String, Map<Integer, CanonizerField>> arrayFieldNames = new HashMap<String, Map<Integer, CanonizerField>>();
	public ArrayList<CanonizerField> fields = new ArrayList<>();
	// For each class index stores a list of the objects corresponding to the class.
	// The position of the object in the list corresponds to the object's index in the canonization
	private ArrayList<LinkedList<CanonizerObject>> store = new ArrayList<>();

	private int storeSize;
	
	
	
	public CanonizerField canonizeArrayField(CanonizerClass arrType, Integer pos) {
		Map<Integer, CanonizerField> m1 = arrayFieldNames.get(arrType.name);
		if (m1 == null) {
			m1 = new HashMap<Integer, CanonizerField>();
			arrayFieldNames.put(arrType.name, m1);
		}
	
		CanonizerField cf = m1.get(pos);
		if (cf != null) return cf;
		
		cf = new CanonizerField(arrType.name + pos, fields.size());
		m1.put(pos, cf);
		fields.add(cf);
		//extensions.addField();
		//primitiveExtensions.addField();
		
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> Stored array field " + cf.name + " with index " + cf.index);

		return cf;
	}
	
	
	public CanonizerClass canonizeClass(Class c) {
		return canonizeClass(c, false);
	}
	
	public CanonizerClass canonizeClass(Class c, boolean setupFBClsGen) {
		CanonizerClass cc = classes.get(c);
		if (cc != null) return cc;
		
		String name = c.getName();
		// FIXME: To handle anonymous classes correctly, not sure if this is still needed
		if (name == null) {
			name = c.getCanonicalName();
		}
		
		cc = new CanonizerClass(c, name, classes.size(), isClassPrimitive(c));
		
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> Storing class " + cc.name + " with index " + cc.index);
		
		classes.put(c, cc);
		indexToClass.add(cc);
		store.add(new LinkedList<CanonizerObject>());
		
		if (!setupFBClsGen)
			cc.ignored = isIgnoredClass(cc);
		
		// have the option of loading fields later if the caller wants
		if (!setupFBClsGen && !cc.ignored && !cc.cls.isArray() && !cc.primitive) 
			canonizeFields(cc);
		
		return cc;
	}
	
	
	// Returns the list with cls' fields. It stores the fields 
	// the first time to return them in the same order later.
	private void canonizeFields(CanonizerClass cc) {
		
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> Considering fields for class " + cc.name + ": ");
					
		Field [] fieldsArray = cc.cls.getDeclaredFields();
		for (int i = 0; i < fieldsArray.length; i++) {
			Field f = fieldsArray[i];
			
			Class<?> ftype = f.getType();
			CanonizerClass ccFieldType = canonizeClass(ftype);

			if (isIgnoredFieldType(ccFieldType)) {
				if (FieldBasedGenLog.isLoggingOn()) 
					FieldBasedGenLog.logLine("Ignored field: " + f.getType() + " " + f.getName());

				continue;
			}
		
			CanonizerField cf = new CanonizerField(f, cc, fields.size()); 
			fields.add(cf);
			cc.addField(cf);
			//extensions.addField();
			//primitiveExtensions.addField();
			
			if (FieldBasedGenLog.isLoggingOn()) 
				FieldBasedGenLog.logLine("Stored field " + cf.name + " with index " + cf.index + 
						", belonging to class " + cf.fromClass.name + " with index " + cf.fromClass.index + ". ");
		}
			
		if (FieldBasedGenLog.isLoggingOn())
			FieldBasedGenLog.logLine("> No more fields for: " + cc.name);

		if (cc.cls.getSuperclass() != null) {
			CanonizerClass anc = canonizeClass(cc.cls.getSuperclass()); 
			if (!anc.ignored && !anc.primitive)
				cc.ancestor = anc;
		}
	}
	
	
  	public boolean isClassPrimitive(Class clazz) {
  		if (clazz.isPrimitive()
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
				)  			
  			return true;
  		
  		return false;
  	}
  	
  
	public String getNullRepresentation() {
		return "null";
	}

	public String getDummyObjectRepresentation() {
		return "_DUMMY_";
	}
	

	
	public void clear() {
		for (List<CanonizerObject> l: store) {
			l.clear();
		}
		storeSize = 0;
	}

	private boolean classObjWarningShown = false;
	private boolean totalObjWarningShown = false;
	private boolean stringWarningShown = false;
	// Returns null when an object cannot be added due to the given limits
	public CanonizerObject addObject(Object obj) {
		
		Class<?> objcls; 
		if (obj == null) {
			objcls = DummyNullClass.class;
		}			
		else {
			objcls = obj.getClass();
		}
		
		CanonizerClass cc = canonizeClass(objcls);

		if (cc.cls == String.class && ((String)obj).length() >= maxStringLength) {
			
			if (dropTestsExceedingLimits) {
				if (!stringWarningShown) {
					String message = "> FIELD BASED GENERATION WARNING: Max string length (" + maxStringLength + ") exceeded. "
							+ "String: " + ((String)obj).substring(0, Math.min(((String)obj).length(), 100)) + "[...]" + 
							". Length: " + ((String)obj).length();
					System.out.println(message);
					stringWarningShown = true;
				}
				if (FieldBasedGenLog.isLoggingOn()) {
					String message = "> FIELD BASED GENERATION WARNING: Max string length (" + maxStringLength + ") exceeded. "
							+ "String: " + ((String)obj).substring(0, Math.min(((String)obj).length(), 100)) + "[...]" + 
							". Length: " + ((String)obj).length();
					FieldBasedGenLog.logLine(message);
				}
				
				return null; 
			}
			
			return new CanonizerObject(((String)obj).substring(0, maxStringLength), cc, -1);
			
		}

		// if the class is ignored we just return a dummy object
		if (obj == null || cc.ignored || cc.primitive) return new CanonizerObject(obj, cc, -1);
	
		List<CanonizerObject> l = store.get(cc.index);
		
		for (CanonizerObject co: l) {
			if (co.obj == obj) {
				co.visited = true;
				return co;
			}
		}
		
		if (storeSize > maxGlobalObjects) {
			if (!totalObjWarningShown) {
				String message = "> FIELD BASED GENERATION WARNING: Max global number of objects limit (" + maxGlobalObjects + ") exceeded";
				System.out.println(message);
				totalObjWarningShown = true;
			}	
			if (FieldBasedGenLog.isLoggingOn()) {
				String message = "> FIELD BASED GENERATION WARNING: Max global number of objects limit (" + maxGlobalObjects + ") exceeded";
				FieldBasedGenLog.logLine(message);
			}
			
			return null;
		}	
		
		if (l.size() > maxClassObjects) {
			if (!classObjWarningShown) {
				String message = "> FIELD BASED GENERATION WARNING: Number of objects limit (" + maxClassObjects + ") exceeded for class " + cc.name;
				System.out.println(message);
				classObjWarningShown = true;
			}
			if (FieldBasedGenLog.isLoggingOn()) {
				String message = "> FIELD BASED GENERATION WARNING: Number of objects limit (" + maxClassObjects + ") exceeded for class " + cc.name;
				FieldBasedGenLog.logLine(message);
			}
			
			return null;
		}

		CanonizerObject res = new CanonizerObject(obj, cc, l.size());
		l.add(res);
		res.visited = false;
		storeSize++;

		return res;

	}


}
