package randoop.util.heapcanonization;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;


public class CanonicalStore {

	private Map<String, CanonicalClass> classes = new LinkedHashMap<>();
	
	public CanonicalStore(Collection<String> classNames) {
		for (String name: classNames) {
			CanonicalClass cls = getCanonicalClass(name);
		}

		for (String name: classes.keySet()) {
			CanonicalClass cls = classes.get(name);
			System.out.println("Loading class: " + cls.toString() + "\n");
		}
	}
	
	public CanonicalClass getCanonicalClass(String name) {
		CanonicalClass res = classes.get(name);
		if (res != null)
			return res;
		
		//System.out.print("New Class: " + name);
		//res = new CanonicalClass(name, this);
		res = new CanonicalClass(name);
		classes.put(name, res);
		res.visitAncestorsAndFields(this);

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
	
}
