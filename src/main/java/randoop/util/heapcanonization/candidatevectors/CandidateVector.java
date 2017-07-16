package randoop.util.heapcanonization.candidatevectors;

import java.util.LinkedList;
import java.util.List;

public class CandidateVector<T> {
	
	private List<T> vector = new LinkedList<>();
	
	public void addComponent(T i) { 
		vector.add(i);
	}
	
	public String toString() {
		String res = "";
		int i = 0;
		for (T comp: vector) {
			res += comp;
			if (i < vector.size()-1)
				res += ",";
			i++;
		}
		return res;
	}

	public int size() {
		return vector.size();
	}

	public T get(Integer i) {
		return vector.get(i);
	}

}
