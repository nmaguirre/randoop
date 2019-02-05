package randoop.generation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import randoop.util.SimpleList;

public class CartesianProduct<T> {

	private Map<Integer, SimpleList<T>> seqs = new LinkedHashMap<>();
	private Map<Integer, Integer> curr;
	private int nComps;
	private boolean hasNext;

	public CartesianProduct(int nComps) {
		this.nComps = nComps;
	}
	
	public boolean hasNext() {
		if (curr == null) {
			// The first time we initialize the current tuple, and check that it 
			// is possible to build at least one candidate
			curr = new LinkedHashMap<>();
			for (int i = 0; i < nComps; i++) 
				curr.put(i, 0);

			boolean allSmaller = true;
			for (int i = 0; i < nComps && allSmaller; i++) {
				if (curr.get(i) == seqs.get(i).size()) 
					allSmaller = false;
			}
			hasNext = allSmaller;
		}
		
		// After initialization we just return the variable hasNext, set by each next call
		return hasNext;
	}
	
	public List<T> next() {
		List<T> res = new ArrayList<T>(nComps);
		for (int i = 0; i < nComps; i++) {
			res.add(seqs.get(i).get(curr.get(i)));
		}

		int lastIdx = -1;
		for (int i = 0; i < nComps; i++) {
			int c = curr.get(i)+1;
			curr.put(i, c);
			if (c < seqs.get(i).size()) 
				break;

			curr.put(i, 0);
			lastIdx = i;
		}
		
		// lastIdx == nComps implies curr is set back to all zeroes, hence there is no next.
		hasNext = (lastIdx == nComps-1) ? false : true;
		return res;
	}

	public void setIthComponent(int i, SimpleList<T> l) {
		seqs.put(i, l);
	}
	
	
}
