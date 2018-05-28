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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import canonicalizer.java.BFJavaHeapCanonicalizer;
import canonicalizer.visitors.BoundedFieldExtensionsCollector;
import canonicalizer.visitors.FieldExtensionsCollector;

import java.util.Map.Entry;



public class GlobalExtensions {
	
	private static BFJavaHeapCanonicalizer canonicalizer;
	private static Map<String, FieldExtensionsCollector> extensionsCollector;
	private static Path userPath;
	private static int maxFieldDistance; 
	private static int maxObjects; 
	/*
	private static int maxArrayObjects; 
	private static int maxStrLength; 
	*/
	private static boolean createExtensions;
	// private static boolean computeDomainSize;
	private static boolean countObjects;
	// private static boolean includePrimitives;
	public static FileWriter canonicalizerLog;
	private static String outputFilename;

	//private static Map<CanonicalClass, Set<LongPair>> hashes;
	private static Set<String> classesToCover;
	private static int totalObjCount;
	private static boolean initialized = false;
	
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
	
	
	public static FieldExtensionsCollector extensionsCollectorForClass(String className) {
		return extensionsCollector.get(className);
	}

	public static void extend(Object o) {
		if (!initialized)
			initialize();


		if (createExtensions 
				//|| countObjects) {
				) {
				
			if (o == null || !coverObject(o)) return;

			 canonicalizer.canonicalize(o, extensionsCollectorForClass(o.getClass().getName()));

			 /*
			if (createExtensions) {
				if (globalExtensions.addAll((FieldExtensionsStrings)extensionsCollector.getExtensions())) {
					if (CanonicalizerLog.isLoggingOn()) {
						CanonicalizerLog.logLine("**********");
						CanonicalizerLog.logLine("Extensions extended:");
						CanonicalizerLog.logLine(globalExtensions.toString());
						CanonicalizerLog.logLine("Size: " + ((FieldExtensionsStrings) globalExtensions).size());
						CanonicalizerLog.logLine("Domain size: " + ((FieldExtensionsStrings) globalExtensions).domainSize());
						CanonicalizerLog.logLine("**********");
					}
				}
			}
			*/
			
			/*
			if (countObjects) 
				countNumberOfDifferentRuntimeObjects(store.getCanonicalClass(o.getClass()), extensionsCollector.getExtensions());
				*/
		}
	}
	
	private static Path getCurrentUserPath() {
		return Paths.get(".").toAbsolutePath().normalize();
	}

	private static void initialize() {
		readConfigFromFile();
		if (createExtensions) {
			canonicalizer = new BFJavaHeapCanonicalizer();
			extensionsCollector = new LinkedHashMap<>();
			for (String clsName: classesToCover)
				extensionsCollector.put(clsName, new BoundedFieldExtensionsCollector(maxObjects));
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
			/*
			if (prop.getProperty("compute.domain.size", "false").equals("true")) {
				computeDomainSize = true;
			}
			*/
		}
		else {
			createExtensions = false;
		}
		if (prop.getProperty("count.objects", "false").equals("true")) {
			//System.out.println("INFO: Computing extensions during tests execution");
			countObjects = true;
		}
		else {
			countObjects = false;
		}
		maxFieldDistance = Integer.parseInt(prop.getProperty("max.field.distance", Integer.toString(Integer.MAX_VALUE))); 
		maxObjects = Integer.parseInt(prop.getProperty("max.objects", Integer.toString(Integer.MAX_VALUE))); 
		//maxArrayObjects = Integer.parseInt(prop.getProperty("max.array.objects", Integer.toString(Integer.MAX_VALUE))); 
		//maxStrLength = Integer.parseInt(prop.getProperty("max.string.length", Integer.toString(Integer.MAX_VALUE))); 
		outputFilename = prop.getProperty("output.filename", "coverage-result.txt");
		/*
		if (prop.getProperty("include.primitives", "true").equals("true")) {
			includePrimitives = true;
			extensionsCollector = new FieldExtensionsStringsCollector(maxStrLength);
		}
		else {
			includePrimitives = false;
			extensionsCollector = new FieldExtensionsStringsNonPrimitiveCollector(maxStrLength);
		}
		*/
		
		/*
		if (canonicalizerLog.isLoggingOn()) {
			canonicalizerLog.logLine("> Read configuration options: ");
			canonicalizerLog.logLine("    measure.coverage="+createExtensions);
			canonicalizerLog.logLine("    compute.domain.size="+computeDomainSize);
			canonicalizerLog.logLine("    count.objects="+countObjects);
			canonicalizerLog.logLine("    include.primitives="+includePrimitives);
			canonicalizerLog.logLine("    max.field.distance="+maxFieldDistance);
			canonicalizerLog.logLine("    max.objects="+maxObjects);
			canonicalizerLog.logLine("    max.array.objects="+maxArrayObjects);
			canonicalizerLog.logLine("    max.string.length="+maxStrLength);
			canonicalizerLog.logLine("    output.filename="+outputFilename);
			canonicalizerLog.logLine("    cover.classes="+classesFile);
			canonicalizerLog.logLine("\n");
		}
		*/
	}

	public static void writeTotal(boolean end) {
		if (!initialized)
			initialize();

		String prefix = "Initial";
		if (end)
			prefix = "Final";
		
		if (createExtensions || countObjects) {
			String absoluteFilename = userPath.resolve(outputFilename).toString();
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(absoluteFilename, true))) {
				if (createExtensions) {
					int totalExtSize = 0;
					int totalExtDomSize = 0;
					for (String clsName: extensionsCollector.keySet()) {
						int clsExtSize = extensionsCollector.get(clsName).getExtensions().size();
						int clsExtDomSize = extensionsCollector.get(clsName).getExtensions().domainSize();
						bw.write(prefix + " " + clsName + " extensions size: "+ clsExtSize + "\n");
						bw.write(prefix + " " + clsName + " extensions domain size: "+ clsExtDomSize + "\n");
						totalExtSize += clsExtSize;
						totalExtDomSize += clsExtDomSize;
						/*
						if (computeDomainSize)
							bw.write(prefix + " domain size: "+ ((FieldExtensionsStrings) globalExtensions).domainSize() + "\n");
							*/
					}
					bw.write(prefix + " extensions size sum: "+ totalExtSize + "\n");
					bw.write(prefix + " extensions domain size sum: "+ totalExtDomSize + "\n");
				}
				/*
				if (countObjects) {
					bw.write(prefix + " objects count: " + totalObjCount + "\n");
					if (prefix.equals("Final")) {
						for (CanonicalClass cc: hashes.keySet()) {
							bw.write("Type: " + cc.getName() + ", objects count: " + hashes.get(cc).size() + "\n");
							if (CanonicalizerLog.isLoggingOn())
								CanonicalizerLog.logLine("Type: " + cc.getName() + ", objects count: " + hashes.get(cc).size());
						}
					}
				}
				*/
			} catch (IOException e) {
				System.out.println("Cound not open file: " + absoluteFilename);
				System.exit(1);
			}

			/*
			if (canonicalizerLog.isLoggingOn()) {
				canonicalizerLog.logLine("**********");
				canonicalizerLog.logLine(prefix + " extensions size: " + ((FieldExtensionsStrings) globalExtensions).size());
				canonicalizerLog.logLine(prefix + " domain size: "+ ((FieldExtensionsStrings) globalExtensions).domainSize() + "\n");
				canonicalizerLog.logLine(prefix + " objects number: " + totalObjCount);
				canonicalizerLog.logLine("**********");
			}
			*/
			
			if (end && isLoggingOn()) {
				for (String clsName: extensionsCollector.keySet()) {
					writeLogLine(clsName + " extensions:");
					writeLogLine(extensionsCollector.get(clsName).getExtensions().toString());
					/*
					if (computeDomainSize)
						bw.write(prefix + " domain size: "+ ((FieldExtensionsStrings) globalExtensions).domainSize() + "\n");
						*/
				}
				try {
					canonicalizerLog.close();
				} catch (IOException e) {
					System.out.println("Cound not close the log file");
					System.exit(1);;
				}
			}
			
		}
	}
	
	private static boolean isLoggingOn() {
		return canonicalizerLog != null;
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
