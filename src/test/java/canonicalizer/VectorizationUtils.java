package canonicalizer;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVector;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorGenerator;

/**
 * Provide vectorization utilities
 * @author fmolina
 *
 */
public class VectorizationUtils {

	public static void deserializeAndVectorize(String serializedObjs,String outputVectors,int maxObjects) throws IOException {
		
		// Read the collection of serialized objs
		Deserialization deserializer = new Deserialization();
		Collection<Object> collection = deserializer.deserialize(serializedObjs);
		
		// Create the canonicalizer
		Class<?> objClazz = collection.iterator().next().getClass();
		Set<String> classNames = new HashSet<String>();
		classNames.add(objClazz.getName());
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		//CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(candVectCanonizer.getStore().getAllCanonicalClassnames());
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(candVectCanonizer.getStore().getAllCanonicalClassnames(),
				objClazz.getName(), store);
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		System.out.println(header.toString());
		// Canonicalize each object in the collection
		for (Object obj : collection) {
			// Canonize the current obj
			Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
			if (canonRes.getKey() != CanonicalizationResult.OK) {
				// El objeto es mas grande de lo que se permite almacenar en el vector candidato
				System.out.println("Canonization error: Object too large.");
				return; 
			}
			CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
			System.out.println(candVect.toString());
		}
		
	}
}
