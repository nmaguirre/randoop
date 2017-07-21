package randoop.util.heapcanonicalization.fieldextensions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

public class FieldExtensionsStrings implements FieldExtensions {
	
	//private int size = 0;

	private Map<String, FieldExtensionStrings> extensions = new LinkedHashMap<String, FieldExtensionStrings>();

	public boolean addPairToField(String field, String src, String tgt) {
		FieldExtensionStrings fe = extensions.get(field);
		if (fe == null) {
			fe = new FieldExtensionStrings(field);
			extensions.put(field, fe);
		}
		return fe.addPair(src, tgt);
		/*
		boolean added = fe.addPair(src, tgt);
		if (added)
			size++;
		
		return added; 
		*/
	}
	
	public boolean pairBelongsToField(String field, String src, String tgt) {
		FieldExtensionStrings fe = extensions.get(field);
		if (fe == null) 
			return false;

		return fe.containsPair(src, tgt);
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

	/*
	public int size() {
		return size;
	}
	*/


	public boolean isEmpty() {
		return extensions.isEmpty();
	}

	@Override
	public boolean addAll(FieldExtensions other) {
		FieldExtensionsStrings otherExt = (FieldExtensionsStrings) other;
		boolean res = false;
		
		for (String field: otherExt.extensions.keySet()) {
			FieldExtensionStrings currExt = extensions.get(field);
			if (currExt == null) {
				currExt = new FieldExtensionStrings(field);
				extensions.put(field, currExt);
			}
			res |= currExt.addAll(otherExt.extensions.get(field));
		}

		return res;
	}

	@Override
	public boolean containsAll(FieldExtensions other) {
		FieldExtensionsStrings otherExt = (FieldExtensionsStrings) other;
		/*
		if (otherExt.size > size) 
			return false;
			*/
		
		if (!extensions.keySet().containsAll(otherExt.extensions.keySet()))
			return false;
		
		for (String field: otherExt.extensions.keySet()) 
			if (!extensions.get(field).containsAll(otherExt.extensions.get(field)))
				return false;
		
		return true;
	}
	

}
