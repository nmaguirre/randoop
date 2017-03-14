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


public class FieldExtensionsIndexesArray implements FieldExtensionsIndexes {
	
	private int size = 0;
	private CanonicalHeapStore store;
	private boolean syncWithStore;
	
	public FieldExtensionsIndexesArray(CanonicalHeapStore store, boolean syncWithStore) {
		this.store = store;
		this.syncWithStore = syncWithStore;
	}
		public FieldExtensionsIndexesArray(CanonicalHeapStore store) {
		this.store = store;
		this.syncWithStore = false;
	}
	
	private ArrayList<Map<Integer, Map<Integer, Map<Integer, Set<String>>>>> extensions = new ArrayList<>();

	public void addField() {
		extensions.add(new HashMap<Integer, Map<Integer,Map<Integer,Set<String>>>>());
	}
	
	private void syncFieldsWithStore() {
		while (store.fields.size() > extensions.size()) {
			addField();
		}
	}
	
	public boolean addPairToField(CanonizerField field, CanonizerObject o1, CanonizerObject o2) {
		
		CanonizerClass c1 = o1.cc;
		CanonizerClass c2 = o2.cc; 
			
		if (o2.primitive()) 
			return addPairToField(field.index, c1.index, c2.index, o1.index, o2.obj.toString());
		else
			return addPairToField(field.index, c1.index, c2.index, o1.index, o2.index.toString());
	}
		
		
	public boolean addPairToField(Integer fieldIndex, Integer c1Index, Integer c2Index, Integer o1Index, String o2) {		

		if (syncWithStore) syncFieldsWithStore();
	
		boolean extended;
		Map<Integer, Map<Integer,Map<Integer,Set<String>>>> m = extensions.get(fieldIndex);	
		
		Map<Integer,Map<Integer,Set<String>>> m1 = m.get(c1Index);
		if (m1 == null) {
			m1 = new HashMap<>();
			m.put(c1Index, m1);
		}
		
		Map<Integer,Set<String>> m2 = m1.get(c2Index);
		if (m2 == null) {
			m2 = new HashMap<>();
			m1.put(c2Index, m2);
		}
		
		Set<String> m3 = m2.get(o1Index);
		if (m3 == null) {
			m3 = new HashSet<>();
			m2.put(o1Index, m3);
		}
		
		extended = m3.add(o2);
			
		if (extended) size++;
		
		return extended;
	}
	
	
	public boolean fieldContainsPair(Integer fieldIndex, Integer c1Index, Integer c2Index, Integer o1Index, String o2) {		

		if (syncWithStore) syncFieldsWithStore();
	
		boolean extended;
		Map<Integer, Map<Integer,Map<Integer,Set<String>>>> m = extensions.get(fieldIndex);	
		
		Map<Integer,Map<Integer,Set<String>>> m1 = m.get(c1Index);
		if (m1 == null) 
			return false;
		
		Map<Integer,Set<String>> m2 = m1.get(c2Index);
		if (m2 == null)
			return false;
		
		Set<String> m3 = m2.get(o1Index);
		if (m3 == null) 
			return false;
		
		return m3.contains(o2);
	}
	
	
	public String toString() {
		String res = "";

		for (int i = 0; i < extensions.size(); i++) {

			res += store.fields.get(i).name + ":{";

			Map<Integer, Map<Integer,Map<Integer,Set<String>>>> m = extensions.get(i);      
			for (Integer c1: m.keySet()) {
				CanonizerClass srccls = store.indexToClass.get(c1);

				Map<Integer,Map<Integer,Set<String>>> m1 = m.get(c1);
				for (Integer c2: m1.keySet()) {
					CanonizerClass tgtcls = store.indexToClass.get(c2);

					Map<Integer,Set<String>> m2 = m1.get(c2);
					for (Integer o1: m2.keySet()) {

						Set<String> m3 = m2.get(o1);
						for (String o2: m3) {
							res += "(" + srccls.name + o1.toString() + ", ";

							if (tgtcls.cls == DummyNullClass.class)
								res += "null";
							else {
								if (!tgtcls.primitive) 
									res += tgtcls.name;
								res += o2;
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
	
	
	public FieldExtensionsStrings toFieldExtensionsStrings() {
		FieldExtensionsStrings stringsExt = new FieldExtensionsStrings();
		
		for (int i = 0; i < extensions.size(); i++) {

			//			res += store.fields.get(i).name + ":{";
			String field = store.fields.get(i).name;

			Map<Integer, Map<Integer,Map<Integer,Set<String>>>> m = extensions.get(i);	
			for (Integer c1: m.keySet()) {
				CanonizerClass srccls = store.indexToClass.get(c1);
				
				Map<Integer,Map<Integer,Set<String>>> m1 = m.get(c1);
				for (Integer c2: m1.keySet()) {
					CanonizerClass tgtcls = store.indexToClass.get(c2);

					Map<Integer,Set<String>> m2 = m1.get(c2);
					for (Integer o1: m2.keySet()) {

						Set<String> m3 = m2.get(o1);
						for (String o2: m3) {
							String src = srccls.name + o1.toString();
							String tgt = "";

							if (tgtcls.cls == DummyNullClass.class)
								tgt = "null";
							else {
								if (!tgtcls.primitive) 
									tgt = tgtcls.name;
								tgt += o2;
							}
							stringsExt.addPairToField(field, src, tgt);
						}
					}
				
				}
				
			}

		}

		return stringsExt;
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
		FieldExtensionsIndexesArray other = (FieldExtensionsIndexesArray) obj;
		if (size != other.size)
			return false;
		if (extensions == null) {
			if (other.extensions != null)
				return false;
		} else if (!extensions.equals(other.extensions))
			return false;
		return true;
	}
	
	
	public boolean addAllPairs(FieldExtensionsIndexes currHeapExt) {

		boolean added = false;
		for (int i = 0; i < ((FieldExtensionsIndexesArray)currHeapExt).extensions.size(); i++) {

			Map<Integer, Map<Integer,Map<Integer,Set<String>>>> m = ((FieldExtensionsIndexesArray)currHeapExt).extensions.get(i);      
			for (Integer c1: m.keySet()) {

				Map<Integer,Map<Integer,Set<String>>> m1 = m.get(c1);
				for (Integer c2: m1.keySet()) {

					Map<Integer,Set<String>> m2 = m1.get(c2);
					for (Integer o1: m2.keySet()) {

						Set<String> m3 = m2.get(o1);
						for (String o2: m3) {

							if (addPairToField(i, c1, c2, o1, o2))
								added = true;

						}

					}

				}

			}

		}	
		
		return added;
	}
	
	
	public boolean testEnlarges(FieldExtensionsIndexes other) {

		for (int i = 0; i < ((FieldExtensionsIndexesArray)other).extensions.size(); i++) {

			Map<Integer, Map<Integer,Map<Integer,Set<String>>>> m = ((FieldExtensionsIndexesArray)other).extensions.get(i);      
			for (Integer c1: m.keySet()) {

				Map<Integer,Map<Integer,Set<String>>> m1 = m.get(c1);
				for (Integer c2: m1.keySet()) {

					Map<Integer,Set<String>> m2 = m1.get(c2);
					for (Integer o1: m2.keySet()) {

						Set<String> m3 = m2.get(o1);
						for (String o2: m3) {

							if (!fieldContainsPair(i, c1, c2, o1, o2))
								return true;

						}

					}

				}

			}

		}	
		
		return false;
	}

	@Override
	public String toIndexesString() {
		return null;
	}
	


}
