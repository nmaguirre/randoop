package randoop.util.fieldexhaustivecontrol;

import java.util.HashMap;
import java.util.Map;

public class FieldExtensions {

	private Map<String, FieldExtension> extensions = new HashMap<String, FieldExtension>();

	public boolean addPairToField(String field, String src, String tgt) {
		FieldExtension fe = extensions.get(field);
		if (fe == null) {
			fe = new FieldExtension(field);
			extensions.put(field, fe);
		}
		return fe.addPair(src, tgt);
	}
	
	public String toString() {
		String result = "";
		for (String fname: extensions.keySet()) 
			result += extensions.get(fname).toString() + '\n';
		return result;
	}

}
