package randoop.fieldextensions;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class DiffObjsStore {


	public DiffObjsStore() {
	}
	
	// Classname -> (Method -> (#Parameter -> Integer))
	public Map<String, Map<String, Map<Integer, Integer>>> collectors = new LinkedHashMap<>(); 


	
	private Map<String, Map<Integer, Integer>> getOrCreateCollectorForClass(String cls) {
		Map<String, Map<Integer, Integer>> col = collectors.get(cls);
		if (col == null) {
			col = new LinkedHashMap<String, Map<Integer, Integer>>();
			collectors.put(cls, col);
		}
		return col;
	}	

	private Map<Integer, Integer> getOrCreateCollectorForMethod(String cls, String method) {
		Map<String, Map<Integer, Integer>> col = getOrCreateCollectorForClass(cls);
		Map<Integer, Integer> c = col.get(method);
		if (c == null) {
			c = new LinkedHashMap<Integer, Integer>(); 
			col.put(method, c);
		}
		return c;
	}
	
	public Integer increaseNumObjsForMethodParam(String cls, String method, Integer numParam) {
		Map<Integer, Integer> p = getOrCreateCollectorForMethod(cls, method);
		Integer c = p.get(numParam);
		if (c == null) {
			c = 0;
		}
		c++;
		p.put(numParam, c);
		return c;
	}
	
	
	
	
	public Set<String> getClasses() {
		return collectors.keySet();
	}
	
	/*
	public SimpleEntry<Integer, Integer> extensionsSizeSumAvgForClass(String cls) {
		int sum = 0;
		int nMethods = 0;
		Map<String, Integer> col = getOrCreateCollectorForClass(cls);
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
			Map<String, Map<Integer, Integer>> col = getOrCreateCollectorForClass(cls);
			for (String method: col.keySet()) {
				res += "  > Method: " + method + "\n";
				Map<Integer, Integer> mcol = col.get(method);
				for (Integer i: mcol.keySet()) {
					Integer diffObjs = mcol.get(i);
					res += "    > #Param " + i + " different objects: " + diffObjs  + "): \n";
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
			Map<String, Map<Integer, Integer>> col = getOrCreateCollectorForClass(cls);
			for (String method: col.keySet()) {
				Map<Integer, Integer> mcol = col.get(method);
				int msum = 0;
				for (Integer i: mcol.keySet()) {
					Integer diffObjs = mcol.get(i);
					msum += diffObjs;
				}
				bw.write("  > Class: " + cls + ", Method: " + method + ", Different objects: " + msum + "\n");
				sum += msum;
				nMethods++;
			}
			
			if (nMethods > 0)
				avg = sum/nMethods;
			
			bw.write(prefix + " " + cls + " different objects sum: "+ sum + "\n");
			bw.write(prefix + " " + cls + " different objects avg: "+ avg + "\n");
			// bw.write(prefix + " " + cls + " extensions domain size: "+ clsExtDomSize + "\n");
			totalExtSize += sum;
			avgExtSize += avg;
			totalExtDomSize += clsExtDomSize;
		}
		bw.write(prefix + " different objects sum: "+ totalExtSize + "\n");
		int resavg = 0;
		if (getClasses().size() > 0)
			resavg = avgExtSize/getClasses().size();
		bw.write(prefix + " different objects avg: "+ resavg + "\n");
		//bw.write(prefix + " extensions domain size sum: "+ totalExtDomSize + "\n");
	}
	
	
	/*
	- extensionsSizeForClass(c)
	- domainsSizeForClass(c)
	- toString()
	*/
	
	
	
}
