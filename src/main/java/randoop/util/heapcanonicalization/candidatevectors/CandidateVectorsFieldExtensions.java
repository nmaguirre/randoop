package randoop.util.heapcanonicalization.candidatevectors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CandidateVectorsFieldExtensions {
	
	private List<Set<Integer>> extensions = new ArrayList<>();

	public CandidateVectorsFieldExtensions(CandidateVector<String> header) {
		assert header.size() > 0: "Candidate vector header cannot be empty";
		if (extensions.isEmpty()) {
			for (int i = 0; i < header.size(); i++) {
				extensions.add(new HashSet<Integer>());
			}
		}
	}

	public boolean addToExtensions(CandidateVector<Integer> vector) {
		assert vector.size() > 0: "Candidate vectors cannot be empty";

//		System.out.println(vector.size());
		assert vector.size() == extensions.size() : "All candidate vectors must be of the same size"
				+ "Original size: " + extensions.size() + ", current size: " + vector.size();
		if (vector.size() != extensions.size())
			throw new BugInCandidateVectorsCanonicalization("Seen a candidate vector with a different size. "
					+ "Original size: " + extensions.size() + ", current size: " + vector.size());
		
		
		boolean extended = false;
		for (int i = 0; i < extensions.size(); i++) 
			extended |= extensions.get(i).add(vector.get(i));

		return extended;
	}
	
	public String toString() {
		String res = "";
		for (int i = 0; i < extensions.size(); i++) {
			if (i != 0)
				res += " ";
			res += extensions.get(i);
		}
		return res;
	}
	
	
}
