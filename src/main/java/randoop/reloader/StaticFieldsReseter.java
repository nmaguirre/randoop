package randoop.reloader;

import java.util.Set;

import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.sandbox.Sandbox.SandboxMode;


public class StaticFieldsReseter {

	private static Set<String> classerUnderTest;
	private static String [] classerUnderTestArr;
	private static ClassLoader simpleReloader;

	public static void setupReloader(Set<String> clnames) { 
		classerUnderTest = clnames;
		classerUnderTestArr = classerUnderTest.toArray(new String[0]);
		simpleReloader = new SimpleReloader(classerUnderTest);
		/*
		for (int i = 0; i < classerUnderTestArr.length; i++)
			classerUnderTestArr[i] = classerUnderTestArr[i].replaceAll("\\.", "/");
			*/
		org.evosuite.runtime.agent.InstrumentingAgent.initialize(); 
		//initializeClasses();
		RuntimeSettings.useSeparateClassLoader = false;
		RuntimeSettings.resetStaticState = true;
		RuntimeSettings.maxNumberOfIterationsPerLoop = Integer.MAX_VALUE;
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
	
	public static void resetClasses() {
		org.evosuite.runtime.classhandling.ClassResetter.getInstance().setClassLoader(ClassLoader.getSystemClassLoader());
		org.evosuite.runtime.classhandling.ClassStateSupport.resetClasses(
				classerUnderTestArr
				);
	}

	public static void activateReloader() {	  
		org.evosuite.runtime.agent.InstrumentingAgent.activate(); 
	}


	public static void deactivateReloader() {	  
		org.evosuite.runtime.agent.InstrumentingAgent.deactivate(); 
	}


	public static ClassLoader getSimpleReloader() {
		return simpleReloader;
	}


}	



	
	
	
	
	
	

	  

