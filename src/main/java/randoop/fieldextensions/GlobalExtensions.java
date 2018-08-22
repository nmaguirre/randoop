package randoop.fieldextensions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import java.util.Map.Entry;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import canonicalizer.BFHeapCanonicalizer;
import extensions.FieldExtensionsCollector;



public class GlobalExtensions {
	
	private static BFHeapCanonicalizer canonicalizer;
//	private static Map<String, FieldExtensionsCollector> extensionsCollector;
	private static ExtensionsStore extensionsCollector;
	private static DiffObjsStore diffObjsCollector = new DiffObjsStore();
	private static Path userPath;
	private static int maxFieldDistance; 
	private static int maxObjects; 
	private static int maxArrayObjects; 
	/*
	private static int maxArrayObjects; 
	private static int maxStrLength; 
	*/
	private static boolean createExtensions;
	// private static boolean computeDomainSize;
	private static boolean singleFieldValue;
	private static boolean countObjects;
	// private static boolean includePrimitives;
	public static FileWriter canonicalizerLog;
	private static String outputFilename;

	//private static Map<CanonicalClass, Set<LongPair>> hashes;
	private static Set<String> classesToCover;
	private static int totalObjCount;
	private static boolean initialized = false;
	private static String logFileName;
	
	/*
	private static void countNumberOfDifferentRuntimeObjects(CanonicalClass cc, FieldExtensions ext) {
		Set<LongPair> hs = hashes.get(cc);
		if (hs == null) {
			hs = new HashSet<LongPair>();
			hashes.put(cc, hs);
		}
		String extStr = ext.toString();
		LongPair hash = hashExtensions(extStr);

		if (hs.add(hash)) {
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("> New object counted:");

			totalObjCount++;
		}
		else {
			if (CanonicalizerLog.isLoggingOn())
				CanonicalizerLog.logLine("> Object was already counted before:");
		}

		if (CanonicalizerLog.isLoggingOn()) 
			CanonicalizerLog.logLine("> Type: " + cc.getName() + ", hash: ," + hash + " obj. ext.:\n" + extStr);
	}
	*/

	/*
	private static LongPair hashExtensions(String ext) {
		LongPair hash = new LongPair();
		byte [] bytesExt = ext.getBytes();

		MurmurHash3.murmurhash3_x64_128(bytesExt, 0, bytesExt.length - 1, 0, hash);

		return hash;
	}	
	*/
	
	public static boolean coverObject(Object o) {
		if (classesToCover == null) 
			return true;
		
		return classesToCover.contains(o.getClass().getName());
	}
	
	public static boolean coverClass(String str) {
		if (classesToCover == null) 
			return true;
		
		return classesToCover.contains(str);
	}

	

	private static String globalExtMethod = "global";
	
	/*
	// Object o is an input for method
	public static void extend(Object o) {
		extend(o, globalExtMethod);
	}
	
	private static int globalNumParam = 0;
	// Object o is an input for method
	public static void extend(Object o, String method) {
		extend(o, globalExtMethod, globalNumParam);
	}
	*/
	
	// Object o is an input for method
	public static void extend(Object o, String cls, String method, Integer numParam) {
		if (!initialized)
			initialize();

		if (createExtensions) {
			if (o == null || !coverClass(cls)) // !coverObject(o)) 
				return;

			FieldExtensionsCollector ext = extensionsCollector.getOrCreateCollectorForMethodParam(cls, 
					method, 
					numParam);
			canonicalizer.canonicalize(o, ext);
			if (ext.extensionsWereExtended() /*&& coverObject(o)*/) {
				diffObjsCollector.increaseNumObjsForMethodParam(cls, method, numParam);
			}
					 //extensionsCollectorForClass(o.getClass().getName()));
		}
	}

	private static Path getCurrentUserPath() {
		return Paths.get(".").toAbsolutePath().normalize();
	}

	private static void initialize() {
		readConfigFromFile();
		if (createExtensions) {
			canonicalizer = new BFHeapCanonicalizer();
			canonicalizer.setMaxArrayObjs(maxArrayObjects);
			canonicalizer.setMaxFieldDistance(maxFieldDistance);
			
			extensionsCollector = new ExtensionsStore(maxObjects);
					//new LinkedHashMap<>();
			//for (String clsName: classesToCover)
			//	extensionsCollector.addClass(clsName);
				//extensionsCollector.put(clsName, new BoundedFieldExtensionsCollector(maxObjects));
		}
		//hashes = new HashMap<>();
		initialized = true;
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
		
		String classesFile = null;
		if ((classesFile = prop.getProperty("cover.classes")) != null) {
			classesToCover = new LinkedHashSet<String>();
			try (BufferedReader br = new BufferedReader(new FileReader(userPath.resolve(classesFile).toString()))) {
				String line = "";
				while ((line = br.readLine()) != null) {
					classesToCover.add(line);
					System.out.println("Covering: " + line);
				}
			} catch (IOException e) {
				System.out.println("Error: Could not find file with classes to cover: " + userPath.resolve(classesFile));
				System.exit(1);
			}
		}
		
		logFileName = prop.getProperty("log.filename");
		
		if (prop.getProperty("measure.coverage", "false").equals("true")) {
			//System.out.println("INFO: Computing extensions during tests execution");
			createExtensions = true;
		}
		else {
			createExtensions = false;
		}
		
		/*
		if (prop.getProperty("single.value", "false").equals("true")) {
			//System.out.println("INFO: Computing extensions during tests execution");
			singleFieldValue = true;
		}
		else {
			singleFieldValue = false;
		}
		*/
	
		
		if (prop.getProperty("count.objects", "false").equals("true")) {
			//System.out.println("INFO: Computing extensions during tests execution");
			countObjects = true;
		}
		else {
			countObjects = false;
		}
		maxFieldDistance = Integer.parseInt(prop.getProperty("max.field.distance", Integer.toString(Integer.MAX_VALUE))); 
		maxObjects = Integer.parseInt(prop.getProperty("max.objects", Integer.toString(Integer.MAX_VALUE))); 
		maxArrayObjects = Integer.parseInt(prop.getProperty("max.arr.objects", Integer.toString(maxObjects)));
		//maxArrayObjects = Integer.parseInt(prop.getProperty("max.array.objects", Integer.toString(Integer.MAX_VALUE))); 
		//maxStrLength = Integer.parseInt(prop.getProperty("max.string.length", Integer.toString(Integer.MAX_VALUE))); 
		outputFilename = prop.getProperty("output.filename", "coverage-result.txt");

	}

	public static void writeTotal(boolean end) {
		if (!initialized)
			initialize();

		String prefix = "Initial";
		if (end)
			prefix = "Final";
		
		
		if (end && createExtensions) {
				//|| countObjects
			String absoluteFilename = userPath.resolve(outputFilename).toString();
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(absoluteFilename, false))) {
				if (createExtensions) {
					extensionsCollector.writeStatistics(bw, prefix);
					diffObjsCollector.writeStatistics(bw, prefix);
					
					/*
					int totalExtSize = 0;
					int avgExtSize = 0;
					int totalExtDomSize = 0;
					for (String clsName: extensionsCollector.getClasses()) {
						SimpleEntry<Integer, Integer> res = extensionsCollector.extensionsSizeSumAvgForClass(clsName);
						int sum = res.getKey();
						int avg = res.getValue();
						int clsExtDomSize = 0;
						//extensionsCollector.get(clsName).getExtensions().domainSize();
						bw.write(prefix + " " + clsName + " extensions size sum: "+ sum + "\n");
						bw.write(prefix + " " + clsName + " extensions size avg: "+ avg + "\n");
						bw.write(prefix + " " + clsName + " extensions domain size: "+ clsExtDomSize + "\n");
						totalExtSize += sum;
						avgExtSize += avg;
						totalExtDomSize += clsExtDomSize;
					}
					bw.write(prefix + " extensions size sum: "+ totalExtSize + "\n");
					bw.write(prefix + " extensions size avg: "+ (avgExtSize/extensionsCollector.getClasses().size()) + "\n");
					bw.write(prefix + " extensions domain size sum: "+ totalExtDomSize + "\n");
					*/
				}

			} catch (IOException e) {
				System.out.println("Cound not open file: " + absoluteFilename);
				System.exit(1);
			}

			
			if (end && isLoggingOn()) {
				activateLogger();
				writeLogLine(extensionsCollector.toString());
				writeLogLine(diffObjsCollector.toString());
				closeLogger();
			}
			
		}
	}
	
	
	private static void activateLogger() {
		try {
			canonicalizerLog = new FileWriter(userPath.resolve(logFileName).toString());
		} catch (IOException e) {
			System.out.println("Error: Log file: " + userPath.resolve(logFileName) + " cannot be created");
			System.exit(1);
		}
	}

	private static void closeLogger() {
		try {
			canonicalizerLog.close();
		} catch (IOException e) {
			System.out.println("Cound not close the log file");
			System.exit(1);;
		}
	}

	private static boolean isLoggingOn() {
		return logFileName != null;
	}
	
	private static void writeLogLine(String line) {
		if (!isLoggingOn()) return;
		
		try {
			canonicalizerLog.write(line + "\n");
		} catch (IOException e) {
			System.out.println("Cound not write to log file");
			System.exit(1);
		}
		
	}
	

}
