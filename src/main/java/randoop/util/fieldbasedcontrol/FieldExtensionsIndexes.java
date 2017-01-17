package randoop.util.fieldbasedcontrol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class FieldExtensionsIndexes {
	
	private int size = 0;

	private ArrayList<Map<Integer, Map<Integer, Map<Integer, Set<Object>>>>> extensions = new ArrayList<>();
	private ArrayList<Map<Integer, Set<Object>>> arrayExtensions = new ArrayList<>();

	private boolean ignorePrimitive;
	
	public FieldExtensionsIndexes(boolean ignorePrimitive) {
		this.ignorePrimitive = ignorePrimitive;
	}
	
	public void addField() {
		extensions.add(new HashMap<Integer, Map<Integer,Map<Integer,Set<Object>>>>());
	}

	public void addArrayField() {
		arrayExtensions.add(new HashMap<Integer, Set<Object>>());
	}
	
	public boolean addPairToField(CanonizerField field, CanonizerClass c1, 
			CanonizerObject o1, CanonizerClass c2, CanonizerObject o2) {
		
		boolean extended;
		Map<Integer, Map<Integer,Map<Integer,Set<Object>>>> m = extensions.get(field.index);	
		
		Map<Integer,Map<Integer,Set<Object>>> m1 = m.get(c1.index);
		if (m1 == null) {
			m1 = new HashMap<>();
			m.put(c1.index, m1);
		}
		
		Map<Integer,Set<Object>> m2 = m1.get(c2.index);
		if (m2 == null) {
			m2 = new HashMap<>();
			m1.put(c2.index, m2);
		}
		
		Set<Object> m3 = m2.get(o1.index);
		if (m3 == null) {
			m3 = new HashSet<>();
			m2.put(o1.index, m3);
		}
		
		if (o2 == null)
			extended = m3.add(-1);
		else if (o2.primitive())
			extended = m3.add(o2.toString());
		else
			extended = m3.add(o2.index);
			
		return extended;
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
		FieldExtensionsIndexes other = (FieldExtensionsIndexes) obj;
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
