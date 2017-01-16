package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

// TODO: Change name to CanonicalHeapStore
public class CanonicalHeapStore {
	
	static CanonicalHeapStore instance;
	
	private static boolean fieldBasedGenByClasses = false;
	List<String> ignoredClasses = Arrays.asList(new String [] {"java.io.", "java.nio.",
			"java.lang.reflect.", "java.net.", "java.security.", "java.beans.", "sun.", "com.sun."});
	private static Set<Integer> fieldBasedGenClasses;
	private static Set<Integer> fieldBasedGenClassesAndParents;

	public static CanonicalHeapStore getInstance() {
		if (instance == null)
			instance = new CanonicalHeapStore();
		
		return instance;
	}
	
	private CanonicalHeapStore() {}

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

	public final Integer MAX_STRING_SIZE = 50;
	
	private Map<Class, CanonizerClass> classes = new HashMap<Class, CanonizerClass>();
	private ArrayList<CanonizerClass> indexToClass = new ArrayList<CanonizerClass>();
	// private Map<Class, Map<Integer, CanonizerArrayField>> arrayFieldNames = new HashMap<Class, Map<Integer, CanonizerArrayField>>();
	// private int arrFieldIndex = -1;
	private ArrayList<CanonizerField> fields = new ArrayList<>();
	// For each class index stores a list of the objects corresponding to the class.
	// The position of the object in the list corresponds to the object's index in the canonization
	private ArrayList<LinkedList<CanonizerObject>> store = new ArrayList<>();
	
	
	public String canonizeArrayField(CanonizerClass arrType, Integer pos) {
		return arrType.name + pos;
		/*
		Class c = arrType.cls;
		Map<Integer, CanonizerArrayField> m1 = arrayFieldNames.get(c);
		if (m1 == null) {
			m1 = new HashMap<Integer, CanonizerArrayField>();
			arrayFieldNames.put(c, m1);
		}
	
		CanonizerArrayField t = m1.get(pos);
		if (t != null) return t;
		
		String name = arrType.name + ".elem" + pos;
		t = new CanonizerArrayField(c, pos, name, ++arrFieldIndex);
		m1.put(pos, t);
		
		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored array field " + name + " with index " + arrFieldIndex);

		return t;
		*/
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
		classes.put(c, cc);
		indexToClass.add(cc);
		store.add(new LinkedList<CanonizerObject>());
		
		if (cc.primitive) {
			cc.ignored = true;
		}
		else {
			if (!setupFBClsGen)
				cc.ignored = isIgnoredClass(cc);
		}
		
		// have the option of loading fields later if the caller wants
		if (!setupFBClsGen && !cc.ignored) 
			canonizeFields(cc);
		

		if (FieldBasedGenLog.isLoggingOn()) 
			FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored class " + cc.name + " with index " + cc.index);
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
			
			if (FieldBasedGenLog.isLoggingOn()) 
				FieldBasedGenLog.logLine("> CANONICAL REPRESENTATION: Stored field " + cf.name + " with index " + cf.index + 
						", belonging to class " + cf.fromClass.name + " with index " + cf.fromClass.index + ". ");
		}
			
		if (FieldBasedGenLog.isLoggingOn())
			FieldBasedGenLog.logLine("> No more fields for: " + cc.name);

		if (cc.cls.getSuperclass() != null) {
			CanonizerClass anc = canonizeClass(cc.cls.getSuperclass()); 
			if (!anc.ignored)
				cc.ancestor = anc;
		}
	}
	
	
  	public boolean isClassPrimitive(Class clazz) {
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
	}

	public CanonizerObject addObject(Object obj) {
		if (obj == null) return null;
		
		Class<?> objcls = obj.getClass();
		CanonizerClass cc = canonizeClass(objcls);

		if (cc.ignored) return new CanonizerObject(obj, cc, -1);
	
		List<CanonizerObject> l = store.get(cc.index);
		int ind = 0;
		for (CanonizerObject co: l) {
			if (co.obj == obj) {
				co.visited = true;
				return co;
			}
			ind++;
		}
		
		CanonizerObject res = new CanonizerObject(obj, cc, l.size());
		l.add(res);
		res.visited = false;
		return res;
	}


}
