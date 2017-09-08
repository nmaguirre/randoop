package randoop.fieldextensions;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import randoop.heapcanonicalization.CanonicalClass;
import randoop.heapcanonicalization.CanonicalHeap;
import randoop.heapcanonicalization.CanonicalStore;
import randoop.heapcanonicalization.CanonicalizationResult;
import randoop.heapcanonicalization.CanonicalizerLog;
import randoop.heapcanonicalization.HeapCanonicalizer;
import randoop.heapcanonicalization.fieldextensions.FieldExtensions;
import randoop.heapcanonicalization.fieldextensions.FieldExtensionsCollector;
import randoop.heapcanonicalization.fieldextensions.FieldExtensionsStrings;
import randoop.heapcanonicalization.fieldextensions.FieldExtensionsStringsCollector;
import randoop.heapcanonicalization.fieldextensions.FieldExtensionsStringsNonPrimitiveCollector;
import randoop.heapcanonicalization.fieldextensions.MurmurHash3;
import randoop.heapcanonicalization.fieldextensions.MurmurHash3.LongPair;


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
	private static boolean countObjects;
	private static boolean includePrimitives;
	public static FileWriter canonicalizerLog;
	private static String outputFilename;

	private static Map<CanonicalClass, Set<LongPair>> hashes;
	private static int totalObjCount;
	
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

	private static LongPair hashExtensions(String ext) {
		LongPair hash = new LongPair();
		byte [] bytesExt = ext.getBytes();

		MurmurHash3.murmurhash3_x64_128(bytesExt, 0, bytesExt.length - 1, 0, hash);

		return hash;
	}	

	public static void extend(Object o) {
		if (globalExtensions == null)
			initialize();
		else {
			assert getCurrentUserPath().equals(userPath): "User path changed during execution of the tests";
		}
		if (o == null) return;

		if (createExtensions || countObjects) {
			extensionsCollector.initializeExtensions();
			Entry<CanonicalizationResult, CanonicalHeap> res = canonicalizer.traverseBreadthFirstAndCanonicalize(o, extensionsCollector);

			assert res.getKey().equals(CanonicalizationResult.OK): "Computation of extensions failed for an object";

			if (createExtensions) {
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
			
			if (countObjects) 
				countNumberOfDifferentRuntimeObjects(store.getCanonicalClass(o.getClass()), extensionsCollector.getExtensions());
			
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
		hashes = new HashMap<>();
		totalObjCount = 0;
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
		if (prop.getProperty("count.objects", "false").equals("true")) {
			//System.out.println("INFO: Computing extensions during tests execution");
			countObjects = true;
		}
		else {
			countObjects = false;
		}
		maxFieldDistance = Integer.parseInt(prop.getProperty("max.field.distance", Integer.toString(Integer.MAX_VALUE))); 
		maxObjects = Integer.parseInt(prop.getProperty("max.objects", Integer.toString(Integer.MAX_VALUE))); 
		maxArrayObjects = Integer.parseInt(prop.getProperty("max.array.objects", Integer.toString(Integer.MAX_VALUE))); 
		maxStrLength = Integer.parseInt(prop.getProperty("max.string.length", Integer.toString(Integer.MAX_VALUE))); 
		outputFilename = prop.getProperty("output.filename", "coverage-result.txt");
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
			CanonicalizerLog.logLine("    measure.coverage="+createExtensions);
			CanonicalizerLog.logLine("    count.objects="+countObjects);
			CanonicalizerLog.logLine("    include.primitives="+includePrimitives);
			CanonicalizerLog.logLine("    max.field.distance="+maxFieldDistance);
			CanonicalizerLog.logLine("    max.objects="+maxObjects);
			CanonicalizerLog.logLine("    max.array.objects="+maxArrayObjects);
			CanonicalizerLog.logLine("    max.string.length="+maxStrLength);
			CanonicalizerLog.logLine("    output.filename="+outputFilename);
			CanonicalizerLog.logLine("\n");
		}
	}

	public static void writeTotal(boolean end) {
		if (globalExtensions == null)
			initialize();
		else {
			assert getCurrentUserPath().equals(userPath): "User path changed during execution of the tests";
		}	
		String prefix = "Initial";
		if (end)
			prefix = "Final";
		
		if (createExtensions || countObjects) {
			String absoluteFilename = userPath.resolve(outputFilename).toString();
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(absoluteFilename, true))) {
				if (createExtensions)
					bw.write(prefix + " extensions size: " + ((FieldExtensionsStrings) globalExtensions).size() + "\n");
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
			} catch (IOException e) {
				System.out.println("Cound not open file: " + absoluteFilename);
				System.exit(1);
			}

			if (CanonicalizerLog.isLoggingOn()) {
				CanonicalizerLog.logLine("**********");
				CanonicalizerLog.logLine(prefix + " extensions size: " + ((FieldExtensionsStrings) globalExtensions).size());
				CanonicalizerLog.logLine(prefix + " objects number: " + totalObjCount);
				CanonicalizerLog.logLine("**********");
			}
		}
	}
	

}
