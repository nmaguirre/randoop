package randoop.reloader;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.sandbox.Sandbox.SandboxMode;


public class StaticFieldsReseter {

	private static Set<String> classesToReload;
	private static String [] classesToReloadArr;
	private static ClassLoader simpleReloader;
	private static boolean first = true;

	public static void setupReloader(Set<String> clnames) { 
		classesToReload = new HashSet<>();
		classesToReload.addAll(clnames);



		classesToReloadArr = classesToReload.toArray(new String[0]);
		simpleReloader = new SimpleReloader(classesToReload);
		setupReloader();
	}
	
	
	/*
	public void addClassToReload(String className) {
		
		
	}
	*/
		
		
	private static void setupReloader() { 
		/*
		for (int i = 0; i < classerUnderTestArr.length; i++)
	 		classerUnderTestArr[i] = classerUnderTestArr[i].replaceAll("\\.", "/");
			*/

		org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(ClassLoader.getSystemClassLoader());
		RuntimeSettings.useSeparateClassLoader = false;
		RuntimeSettings.resetStaticState = true;
		RuntimeSettings.maxNumberOfIterationsPerLoop = Integer.MAX_VALUE;

		org.evosuite.runtime.agent.InstrumentingAgent.initialize(); 

		initializeClasses();
		//RuntimeSettings.sandboxMode = SandboxMode.OFF;
		//org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
	}

	
	private static void initializeClasses() {

		org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
		org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(ClassLoader.getSystemClassLoader(),
				classesToReloadArr	
				);
		org.evosuite.runtime.agent.InstrumentingAgent.deactivate(); 
		/*
		for (String str:org.evosuite.runtime.classhandling.ClassResetter.getInstance().classesToReset) {
			if (classesToReload.add(str))
				System.out.println(str);
		}
		*/

		// The first time we filter out the classes that do not have static fields 
		Set<String> filteredClasses = new HashSet<>();
		for (String str: classesToReload) {
			
			if (str.endsWith("CheckLevel"))
				filteredClasses.add(str);
			
			if (org.evosuite.runtime.classhandling.ClassResetter.getInstance().getResetMethod(str) != null) 
				filteredClasses.add(str);
			
		}
		classesToReload = filteredClasses;
		classesToReloadArr = classesToReload.toArray(new String[0]);
	} 	
	
	public static Set<String> getClassesToReload() {
		return classesToReload;
	}
	
	public static void resetClasses() {
		org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
				classesToReloadArr
				);
	}

	public static void activateReloader() {	  
		org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
	}


	public static void deactivateReloader() {	  
		org.evosuite.runtime.agent.InstrumentingAgent.deactivate(); 
		/*
		for (String str:org.evosuite.runtime.classhandling.ClassResetter.getInstance().classesToReset) {
			if (classesToReload.add(str))
				System.out.println(str);
		}
		classesToReloadArr = classesToReload.toArray(new String[0]);
		*/
	}

	/*
	public static void resetAllClasses() {	  
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().resetAll();
	}
	*/

	public static ClassLoader getSimpleReloader() {
		return simpleReloader;
	}


}	



	
	
	
	
	
	

	  

