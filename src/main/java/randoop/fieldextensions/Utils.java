package randoop.fieldextensions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import randoop.main.GenInputsAbstract;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;

public class Utils {

	private static Set<String> classesUnderTest;
	private static boolean initialized;
	private static Map<String, String> classNameCache = new LinkedHashMap<>();

	public static String getOperationClass(TypedOperation op) {
		TypedClassOperation typOp = (TypedClassOperation) op;
		// FIXME: Hack for the sequence:
		//			java.lang.Object obj0 = new java.lang.Object()
		if (op.toParsableString().equals("java.lang.Object.<init>()"))
			return "java.lang.Object";

		assert typOp.drawnFromClass != null: 
			op.toParsableString() + "is not associated to any input class";
		
		String className = typOp.drawnFromClass.getName();
		String classNameNoGenerics = classNameCache.get(className);
		if (classNameNoGenerics == null) {
			if (className.indexOf('<') == -1)
				classNameNoGenerics = className;
			else
				classNameNoGenerics = className.substring(0, className.indexOf('<'));

			classNameCache.put(className, classNameNoGenerics);
		}
		return classNameNoGenerics;
	}

	public static boolean classUnderTest(String className) {
		initialize();
		if (classesUnderTest == null) return true;

		// Only consider classes marked as important to be tested by the user
		// This is akin to try to cover a few classes that one wants to test
		return classesUnderTest.contains(className);
	}

	private static void initialize() {
		if (!initialized) {
			if (GenInputsAbstract.fbg_classlist != null)
				classesUnderTest = GenInputsAbstract.getClassnamesFBG();	
			initialized = true;
		}
	}
	
	public static boolean isPrimitive(Object o) {
		Class<?> cls = o.getClass();
		return NonreceiverTerm.isNonreceiverType(cls) && !cls.equals(Class.class);
	}
	
}
