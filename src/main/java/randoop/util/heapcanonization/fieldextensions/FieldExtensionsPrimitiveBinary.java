package randoop.util.heapcanonization.fieldextensions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public class FieldExtensionsPrimitiveBinary {

	// TOOD: Is it easy to compute size with these extensions?
	//private int size = 0;

	private Map<String, FieldExtensionPrimitiveBinary> extensions = new HashMap<>();

	public boolean addPairToField(String field, String src, BinaryPrimitiveValue tgt) {
		FieldExtensionPrimitiveBinary fe = extensions.get(field);
		if (fe == null) {
			fe = new FieldExtensionPrimitiveBinary(field);
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
	
	public boolean pairBelongsToField(String field, String src, BinaryPrimitiveValue tgt) {
		FieldExtensionPrimitiveBinary fe = extensions.get(field);
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
	
	

	public boolean addAll(FieldExtensionsPrimitiveBinary other) {
		boolean res = false;
		
		for (String field: other.extensions.keySet()) {
			FieldExtensionPrimitiveBinary currExt = extensions.get(field);
			if (currExt == null) {
				currExt = new FieldExtensionPrimitiveBinary(field);
				extensions.put(field, currExt);
			}
			res |= currExt.addAll(other.extensions.get(field));
		}

		return res;
	}


	public boolean containsAll(FieldExtensionsPrimitiveBinary other) {
		/*
		if (otherExt.size > size) 
			return false;
			*/
		
		if (!extensions.keySet().containsAll(other.extensions.keySet()))
			return false;
		
		for (String field: extensions.keySet()) 
			if (!extensions.get(field).containsAll(other.extensions.get(field)))
				return false;
		
		return true;
	}
	

}