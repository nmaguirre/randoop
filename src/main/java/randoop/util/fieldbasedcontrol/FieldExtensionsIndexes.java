package randoop.util.fieldbasedcontrol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class FieldExtensionsIndexes {
	
	private int size = 0;
	
	private CanonicalHeapStore store;
	
	public FieldExtensionsIndexes(CanonicalHeapStore store) {
		this.store = store;
	}
	
	private ArrayList<Map<Integer, Map<Integer, Map<Integer, Set<Object>>>>> extensions = new ArrayList<>();

	public void addField() {
		extensions.add(new HashMap<Integer, Map<Integer,Map<Integer,Set<Object>>>>());
	}
	
	public boolean addPairToField(CanonizerField field, CanonizerObject o1, CanonizerObject o2) {
		CanonizerClass c1 = o1.cc;
		CanonizerClass c2 = o2.cc; 
		
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
		
		if (o2.primitive()) {
			if (o2.cc.cls == String.class) {
				String toStore = ((String)o2.obj);
				if (toStore.length() > CanonicalRepresentation.MAX_STRING_SIZE)
					toStore = toStore.substring(0, CanonicalRepresentation.MAX_STRING_SIZE);
				extended = m3.add(toStore);
			} 
			else 
				extended = m3.add(o2.obj.toString());
		}
		else
			extended = m3.add(o2.index);
			
		if (extended) size++;
		
		return extended;
	}
	
	
	public String toString() {
		String res = "";
		
		for (int i = 0; i < extensions.size(); i++) {

			res += store.fields.get(i).name + ":{";

			Map<Integer, Map<Integer,Map<Integer,Set<Object>>>> m = extensions.get(i);	
			for (Integer c1: m.keySet()) {
				CanonizerClass srccls = store.indexToClass.get(c1);
				
				Map<Integer,Map<Integer,Set<Object>>> m1 = m.get(c1);
				for (Integer c2: m1.keySet()) {
					CanonizerClass tgtcls = store.indexToClass.get(c2);

					Map<Integer,Set<Object>> m2 = m1.get(c2);
					for (Integer o1: m2.keySet()) {

						Set<Object> m3 = m2.get(o1);
						for (Object o2: m3) {
							res += "(" + srccls.name + o1.toString() + ", ";
							
							if (tgtcls.cls == DummyNullClass.class)
								res += "null";
							else {
								if (!tgtcls.primitive) 
									res += tgtcls.name;
								res += o2.toString();
							}
							res += "), ";

						}
					}
				
				}
				
			}

			res += "}\n";
		}

		return res;
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
