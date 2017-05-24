package randoop.util.fieldbasedcontrol;

import java.util.ArrayList;
import java.util.List;

import randoop.util.Randomness;

public class RandomPerm {

	public static <T> void randomPermutation(List<T> l) {
		
		for (int i = 0; i < l.size(); i++) {	
			int exchangeInd = i + Randomness.nextRandomInt(l.size()-i);
			// System.out.println(i + " " + exchangeInd);
			T temp = l.get(i);
			l.set(i, l.get(exchangeInd));
			l.set(exchangeInd, temp);
			// System.out.println(l.toString());
		}
		
	}
	
	public static void main(String [] args) {
		ArrayList<Integer> l = new ArrayList<Integer>();
		for (int i = 0; i < 6; i++) {
			l.add(i);
		}
		randomPermutation(l);
		System.out.println(l.toString());
		
	}

}