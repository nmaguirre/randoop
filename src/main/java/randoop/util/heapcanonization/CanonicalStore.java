package randoop.util.heapcanonization;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class CanonicalStore {

	private final Map<String, CanonicalClass> classes = new LinkedHashMap<>();
	
	public CanonicalStore(Collection<String> classNames) {
		for (String name: classNames) 
			getCanonicalClass(name);

		System.out.print(toString());
	}
	
	public CanonicalClass getCanonicalClass(String name) {
		CanonicalClass res = classes.get(name);
		if (res != null)
			return res;
		
		//System.out.print("New Class: " + name);
		//res = new CanonicalClass(name, this);
		res = new CanonicalClass(name, this);
		classes.put(name, res);

		return res;
	}
	
	public CanonicalClass getCanonicalClass(Class<?> clazz) {
		//System.out.print("From Class: " + clazz.getName());
		return getCanonicalClass(clazz.getName());
	}
	
	
	public String candidateVectorCanonization(CanonicalHeap heap) {
		String res = "";
		
		boolean first = true;
		for (String className: classes.keySet()) {
			int i = 0;
			
			CanonicalClass canonicalClass = classes.get(className);
			if (canonicalClass.isPrimitive()) continue;

			for (CanonicalObject obj: heap.getObjectsForClass(canonicalClass)) {
				if (first) 
					first = false;
				else 
					res += " ";
				res += obj.candidateVectorCanonization(heap);
			}
				

			for ( ; i < heap.getMaxObjects(); i++) {
				if (first) 
					first = false;
				else 
					res += " ";
				res += new CanonicalObject(null, canonicalClass, i).candidateVectorCanonization(heap);
			}
			
		}
		
		res += "";
		return res;
	}
	
	
	public String toString() {
		String res = "";
		for (String name: classes.keySet()) 
			res += "Loading class: " + classes.get(name).toString() + "\n";
		return res;
	}
	
}
