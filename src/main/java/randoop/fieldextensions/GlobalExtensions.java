package randoop.fieldextensions;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Map.Entry;

import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.CanonicalizerLog;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensions;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStrings;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStringsCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStringsNonPrimitiveCollector;


public class GlobalExtensions {
	
	private static FieldExtensions globalExtensions;
	private static HeapCanonicalizer canonicalizer;
	private static FieldExtensionsCollector extensionsCollector;
	private static CanonicalStore store;
	private static Path userPath;
	private static int maxFieldDistance; 
	private static int maxObjects; 
	private static int maxArrayObjects; 
	private static int maxStrLength; 
	private static boolean createExtensions;
	private static boolean includePrimitives;
	public static FileWriter canonicalizerLog;
	private static String outputFilename;

	public static void extend(Object o) {
		if (globalExtensions == null)
			initialize();
		else {
			assert getCurrentUserPath().equals(userPath): "User path changed during execution of the tests";
		}
		if (!createExtensions) return;
		if (o == null) return;

		extensionsCollector.initializeExtensions();
		Entry<CanonicalizationResult, CanonicalHeap> res = canonicalizer.traverseBreadthFirstAndCanonicalize(o, extensionsCollector);
		assert res.getKey().equals(CanonicalizationResult.OK): "Computation of extensions failed for an object";

		if (globalExtensions.addAll((FieldExtensionsStrings)extensionsCollector.getExtensions())) {
			if (CanonicalizerLog.isLoggingOn()) {
				CanonicalizerLog.logLine("**********");
				CanonicalizerLog.logLine("Extensions extended:");
				CanonicalizerLog.logLine(globalExtensions.toString());
				CanonicalizerLog.logLine("Size: " + ((FieldExtensionsStrings) globalExtensions).size());
				CanonicalizerLog.logLine("**********");
			}
		}
			
	}
	
	private static Path getCurrentUserPath() {
		return Paths.get(".").toAbsolutePath().normalize();
	}

	private static void initialize() {
		readConfigFromFile();
		globalExtensions = new FieldExtensionsStrings();
		store = new CanonicalStore(new LinkedHashSet<String>(), maxFieldDistance);
		canonicalizer = new HeapCanonicalizer(store, maxObjects, maxArrayObjects);
	}

	private static void readConfigFromFile() {
		userPath = getCurrentUserPath();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(userPath.resolve("tests.properties").toString());
			prop.load(input);
		} catch (IOException ex) {
			System.out.println("Error: Could not find properties file: " + userPath.resolve("tests.properties"));
			System.exit(1);
			
		}
		
		String logFileName = prop.getProperty("log.filename");
		if (logFileName != null) {
			try {
				canonicalizerLog = new FileWriter(userPath.resolve(logFileName).toString());
			} catch (IOException e) {
				System.out.println("Error: Log file: " + userPath.resolve(logFileName) + " cannot be created");
				System.exit(1);
			}
		}
		
		if (prop.getProperty("measure.coverage", "false").equals("true")) {
			//System.out.println("INFO: Computing extensions during tests execution");
			createExtensions = true;
		}
		else {
			createExtensions = false;
		}
		maxFieldDistance = Integer.parseInt(prop.getProperty("max.field.distance", Integer.toString(Integer.MAX_VALUE))); 
		maxObjects = Integer.parseInt(prop.getProperty("max.objects", Integer.toString(Integer.MAX_VALUE))); 
		maxArrayObjects = Integer.parseInt(prop.getProperty("max.array.objects", Integer.toString(Integer.MAX_VALUE))); 
		maxStrLength = Integer.parseInt(prop.getProperty("max.string.length", Integer.toString(Integer.MAX_VALUE))); 
		outputFilename = prop.getProperty("output.filename", "extensions-size.txt");
		if (prop.getProperty("include.primitives", "true").equals("true")) {
			includePrimitives = true;
			extensionsCollector = new FieldExtensionsStringsCollector(maxStrLength);
		}
		else {
			includePrimitives = false;
			extensionsCollector = new FieldExtensionsStringsNonPrimitiveCollector(maxStrLength);
		}
		
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("> Read configuration options: ");
			CanonicalizerLog.logLine("    create.extensions="+createExtensions);
			CanonicalizerLog.logLine("    include.primitives="+includePrimitives);
			CanonicalizerLog.logLine("    max.field.distance="+maxFieldDistance);
			CanonicalizerLog.logLine("    max.objects="+maxObjects);
			CanonicalizerLog.logLine("    max.array.objects="+maxArrayObjects);
			CanonicalizerLog.logLine("    max.string.length="+maxStrLength);
			CanonicalizerLog.logLine("    output.filename="+outputFilename);
			CanonicalizerLog.logLine("\n");
		}
	}

	public static void writeTotal(String prefix) {
		if (globalExtensions == null)
			initialize();
		else {
			assert getCurrentUserPath().equals(userPath): "User path changed during execution of the tests";
		}	
		if (!createExtensions) return;
	
		int size = ((FieldExtensionsStrings) globalExtensions).size();
	
		String absoluteFilename = userPath.resolve(outputFilename).toString();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(absoluteFilename, true))) {
			bw.write(prefix + size + "\n");
		} catch (IOException e) {
			System.out.println("Cound not open file: " + absoluteFilename);
			System.exit(1);
		}
		
		if (CanonicalizerLog.isLoggingOn()) {
			CanonicalizerLog.logLine("**********");
			CanonicalizerLog.logLine(prefix + size);
			CanonicalizerLog.logLine("**********");
		}
	}
	

}
