package randoop.util.fieldbasedcontrol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FieldExtensions {
	
	int size = 0;

	private Map<String, FieldExtension> extensions = new HashMap<String, FieldExtension>();

	public boolean addPairToField(String field, String src, String tgt) {
		FieldExtension fe = extensions.get(field);
		if (fe == null) {
			fe = new FieldExtension(field);
			extensions.put(field, fe);
		}
		boolean added = fe.addPair(src, tgt);
		if (added)
			size++;
		
		return added; 
	}
	
	public String toString() {
		String result = "";
		for (String fname: extensions.keySet()) 
			result += extensions.get(fname).toString() + '\n';
		return result;
	}
	
	public void toFile(String filename) throws IOException {
		try (Writer writer = new BufferedWriter(new FileWriter(filename))) {
			writer.write(toString());
		}	
	}

	public int size() {
		return size;
	}

}
