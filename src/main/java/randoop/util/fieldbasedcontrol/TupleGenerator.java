package randoop.util.fieldbasedcontrol;

import java.util.ArrayList;

import randoop.util.SimpleList;

public class TupleGenerator<T> {

	private ArrayList<SimpleList<T>> takeFrom;
	private ArrayList<Integer> currTupleIndexes;
	private ArrayList<Integer> tupleIndexesLimits;
	boolean hasNext = true;

	public TupleGenerator(ArrayList<SimpleList<T>> takeFrom) {

		if (takeFrom.isEmpty()) {
			hasNext = false;
			return;
		}

		for (int i = 0; i < takeFrom.size(); i++) {
			if (takeFrom.get(i).isEmpty()) {
				hasNext = false;
				return;
			}
		}

		this.takeFrom = takeFrom;
		this.currTupleIndexes = new ArrayList<>();
		this.tupleIndexesLimits = new ArrayList<>();
		for (int i = 0; i < takeFrom.size(); i++) {
			this.currTupleIndexes.add(0);
			this.tupleIndexesLimits.add(takeFrom.get(i).size() - 1);
		}
		
	}
	
	
	public boolean hasNext() {
		return hasNext;
	}
	
	
	public ArrayList<T> next() {
		
		// System.out.println("Indexes: " + currTupleIndexes.toString() + ", sizes:" + tupleIndexesLimits.toString());
		
		ArrayList<T> res = new ArrayList<>();
		for (int i = 0; i < takeFrom.size(); i++) {
			res.add(i, takeFrom.get(i).get(currTupleIndexes.get(i)));
		}

		moveToNext();
		
		return res;
	}


	private void moveToNext() {

		int i = 0; 
		while (i < currTupleIndexes.size() && currTupleIndexes.get(i).equals(tupleIndexesLimits.get(i))) {
			currTupleIndexes.set(i, 0);
			i++; 
		}
		if (i == currTupleIndexes.size())
			hasNext = false;
		else
			// Increase the first i such that currTupleIndexes.get(i) < tupleIndexesLimits.get(i)
			currTupleIndexes.set(i, currTupleIndexes.get(i) + 1);
	}
	
	
}
