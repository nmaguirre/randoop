package randoop.reloader;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.sandbox.Sandbox.SandboxMode;


public class StaticFieldsReseter {

	private static Set<String> classesToReload;
	private static String [] classesToReloadArr;
	private static ClassLoader simpleReloader;

	public static void setupReloader(Set<String> clnames) { 
		classesToReload = new HashSet<>();
		classesToReload.addAll(clnames);
		/*
		for (String str: clnames) {
			if (org.evosuite.runtime.classhandling.ClassResetter.getInstance().getResetMethod(str) != null)
				classesToReload.add(str);
		}
		*/
		classesToReloadArr = classesToReload.toArray(new String[0]);
		simpleReloader = new SimpleReloader(classesToReload);
		setupReloader();
	}
		
		
	public static void setupReloader() { 
		/*
		for (int i = 0; i < classerUnderTestArr.length; i++)
	 		classerUnderTestArr[i] = classerUnderTestArr[i].replaceAll("\\.", "/");
			*/
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(ClassLoader.getSystemClassLoader());

		RuntimeSettings.useSeparateClassLoader = false;
		RuntimeSettings.resetStaticState = true;
		RuntimeSettings.maxNumberOfIterationsPerLoop = Integer.MAX_VALUE;

		org.evosuite.runtime.agent.InstrumentingAgent.initialize(); 
		org.evosuite.runtime.agent.InstrumentingAgent.deactivate(); 

		//initializeClasses();
		//RuntimeSettings.sandboxMode = SandboxMode.OFF;
		//org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
	}

	
	/*
	private static void initializeClasses() {

		org.evosuite.runtime.classhandling.ClassStateSupport.initializeClasses(ClassLoader.getSystemClassLoader(),
				classerUnderTestArr
		);
	  } 	
	  */
	
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
	}

	public static void resetAllClasses() {	  
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().resetAll();
	}

	public static ClassLoader getSimpleReloader() {
		return simpleReloader;
	}


}	



	
	
	
	
	
	

	  

