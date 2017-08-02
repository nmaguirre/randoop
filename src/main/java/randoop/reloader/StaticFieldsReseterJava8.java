/*
package randoop.reloader;

import java.util.List;
import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;

import randoop.main.ClassNameErrorHandler;
import randoop.main.GenInputsAbstract;
import randoop.operation.OperationParseException;
import randoop.operation.TypedOperation;
import randoop.reflection.OperationModel;
import randoop.reflection.ReflectionPredicate;
import randoop.reflection.VisibilityPredicate;
import randoop.types.PrimitiveTypes;

public class StaticFieldsReseter {

	private static Set<String> CUTClassNames;
	private static String[] CUTClassNamesArr;
	private static Set<String> coveredClassnames;
	private static VisibilityPredicate visibility;
	private static ReflectionPredicate reflectionPredicate;
	private static ClassNameErrorHandler classNameErrorHandler;
	private static Set<String> methodSignatures;
	private static ClassReloaderFromDisk reloader;

	public static EvoClassLoader evoClassLoader;
	
	public static boolean useEvoLoader = true;

	public static void setupReloader(Set<String> clnames) { 
		CUTClassNames = clnames;
		evoClassLoader = new EvoClassLoader();
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(evoClassLoader);
		//RuntimeSettings.resetStaticState = true;
	}
	
	public static void setupReloader(Set<String> clname, 
			Set<String> c,
			VisibilityPredicate v,
			ReflectionPredicate r,
			ClassNameErrorHandler ce,
			Set<String> m
			) {
		CUTClassNames = clname;
		CUTClassNamesArr = CUTClassNames.toArray(new String[0]);
		
		coveredClassnames = c;
		visibility = v;
		reflectionPredicate = r;
		classNameErrorHandler = ce;
		methodSignatures = m;
		
		evoClassLoader = new EvoClassLoader();
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(evoClassLoader);
		RuntimeSettings.resetStaticState = true;
	}	
	
	public static void resetAllStaticFields() {
		org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
				CUTClassNamesArr
		);
	}
	
	public static Class<?> getTypeForName(String typeName) throws ClassNotFoundException {
		Class<?> c = PrimitiveTypes.classForName(typeName);
		if (c == null) {
			c = Class.forName(typeName, true, evoClassLoader);
		}
		return c;
	}	
	
	
}
*/






	
	/*
	public static List<TypedOperation> reloadOperationModel() {
		
		//reloader = new ClassReloaderFromDisk(CUTClassNames);
		reloader = new ClassReloaderFromDisk(CUTClassNames, ClassLoader.getSystemClassLoader());

		OperationModel operationModel = null;
		try {
			operationModel =
					OperationModel.createModel(
							visibility,
							reflectionPredicate,
							CUTClassNames,
							coveredClassnames,
							methodSignatures,
							classNameErrorHandler,
							GenInputsAbstract.literals_file,
							true);
		} catch (OperationParseException e) {
			System.out.printf("Error: parse exception thrown %s%n", e);
			System.exit(1);
		} catch (NoSuchMethodException e) {
			System.out.printf("Error building operation model: %s%n", e);
			System.exit(1);
		}
		assert operationModel != null;

		if (!operationModel.hasClasses()) {
			System.out.println("No classes to test");
			System.exit(1);
		}

		reloader = null;
		return operationModel.getConcreteOperations();
	}




	public static void reloadAllClassesFromDisk() throws ClassNotFoundException {
		ClassReloaderFromDisk reloader = new ClassReloaderFromDisk(CUTClassNames);
		for (String className: CUTClassNames) {
			System.out.println(">> Reloading : " + className);
			reloader.loadClass(className);
		}

	}
	*/





	
	
	
	
	
	

	  

