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

	/*
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((extensions == null) ? 0 : extensions.hashCode());
		result = prime * result + size;
		return result;
	}*/

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldExtensions other = (FieldExtensions) obj;
		if (size != other.size)
			return false;
		if (extensions == null) {
			if (other.extensions != null)
				return false;
		} else if (!extensions.equals(other.extensions))
			return false;
		return true;
	}
	

}
