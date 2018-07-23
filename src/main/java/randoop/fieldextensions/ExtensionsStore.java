package randoop.fieldextensions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import extensions.BoundedFieldExtensionsCollector;
import extensions.FieldExtensionsCollector;
import extensions.IFieldExtensions;


public class ExtensionsStore {

	private int maxObjects;

	public ExtensionsStore(int maxObjects) {
		this.maxObjects = maxObjects;
	}
	
	// Classname -> (Method -> (#Parameter -> Collector))
	public Map<String, Map<String, Map<Integer, FieldExtensionsCollector>>> collectors = new LinkedHashMap<>(); 


	
	private Map<String, Map<Integer, FieldExtensionsCollector>> getOrCreateCollectorForClass(String cls) {
		Map<String, Map<Integer, FieldExtensionsCollector>> col = collectors.get(cls);
		if (col == null) {
			col = new LinkedHashMap<String, Map<Integer, FieldExtensionsCollector>>();
			collectors.put(cls, col);
		}
		return col;
	}	

	private Map<Integer, FieldExtensionsCollector> getOrCreateCollectorForMethod(String cls, String method) {
		Map<String, Map<Integer, FieldExtensionsCollector>> col = getOrCreateCollectorForClass(cls);
		Map<Integer, FieldExtensionsCollector> c = col.get(method);
		if (c == null) {
			c = new LinkedHashMap<Integer, FieldExtensionsCollector>(); 
			col.put(method, c);
		}
		return c;
	}
	
	public FieldExtensionsCollector getOrCreateCollectorForMethodParam(String cls, String method, Integer numParam) {
		Map<Integer, FieldExtensionsCollector> p = getOrCreateCollectorForMethod(cls, method);
		FieldExtensionsCollector c = p.get(numParam);
		if (c == null) {
			c = new BoundedFieldExtensionsCollector(maxObjects);
			p.put(numParam, c);
		}
		return c;
	}
	
	
	public Set<String> getClasses() {
		return collectors.keySet();
	}
	
	/*
	public SimpleEntry<Integer, Integer> extensionsSizeSumAvgForClass(String cls) {
		int sum = 0;
		int nMethods = 0;
		Map<String, FieldExtensionsCollector> col = getOrCreateCollectorForClass(cls);
		for (String method: col.keySet()) {
			IFieldExtensions methodExt = col.get(method).getExtensions();
			sum += methodExt.size();
			nMethods++;
		}
		
		if (nMethods == 0)
			return new SimpleEntry<>(0, 0);

		return new SimpleEntry<>(sum, sum/nMethods);
	}
	*/
	
	public String toString() {
		String res = "";
		for (String cls: collectors.keySet()) {
			res += "> Class: " + cls + "\n";
			Map<String, Map<Integer, FieldExtensionsCollector>> col = getOrCreateCollectorForClass(cls);
			for (String method: col.keySet()) {
				res += "  > Method: " + method + "\n";
				Map<Integer, FieldExtensionsCollector> mcol = col.get(method);
				for (Integer i: mcol.keySet()) {
					IFieldExtensions methodExt = mcol.get(i).getExtensions();
					res += "    > #Param " + i + " extensions (size " + methodExt.size()  + "): \n";
					res += methodExt.toString();
				}
			}
		}
		return res;
	}
	
	
	public void writeStatistics(BufferedWriter bw, String prefix) throws IOException {
		int totalExtSize = 0;
		int avgExtSize = 0;
		int totalExtDomSize = 0;
		for (String cls: getClasses()) {

			int clsExtDomSize = 0;
			int sum = 0;
			int avg = 0;
			int nMethods = 0;
			Map<String, Map<Integer, FieldExtensionsCollector>> col = getOrCreateCollectorForClass(cls);
			for (String method: col.keySet()) {
				Map<Integer, FieldExtensionsCollector> mcol = col.get(method);
				int msum = 0;
				for (Integer i: mcol.keySet()) {
					IFieldExtensions methodExt = mcol.get(i).getExtensions();
					msum += methodExt.size();
				}
				bw.write("  > Class: " + cls + ", Method: " + method + ", Extensions size " + msum + "\n");
				sum += msum;
				nMethods++;
			}
			
			if (nMethods > 0)
				avg = sum/nMethods;
			
			bw.write(prefix + " " + cls + " extensions size sum: "+ sum + "\n");
			bw.write(prefix + " " + cls + " extensions size avg: "+ avg + "\n");
			bw.write(prefix + " " + cls + " extensions domain size: "+ clsExtDomSize + "\n");
			totalExtSize += sum;
			avgExtSize += avg;
			totalExtDomSize += clsExtDomSize;
		}
		bw.write(prefix + " extensions size sum: "+ totalExtSize + "\n");
		int resavg = 0;
		if (getClasses().size() > 0)
			resavg = avgExtSize/getClasses().size();
		bw.write(prefix + " extensions size avg: "+ resavg + "\n");
		bw.write(prefix + " extensions domain size sum: "+ totalExtDomSize + "\n");
	}
	
	
	/*
	- extensionsSizeForClass(c)
	- domainsSizeForClass(c)
	- toString()
	*/
	
	
	
}
