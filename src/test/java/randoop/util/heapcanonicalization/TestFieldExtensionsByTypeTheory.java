package randoop.util.heapcanonicalization;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;

import randoop.test.datastructures.bstree.BSTree;
import randoop.test.datastructures.singlylistinner.SinglyLinkedListInner;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsByType;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsByTypeCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsCollector;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A few tests for the vector canonicalization algorithm.
 */
@RunWith(Theories.class)
public class TestFieldExtensionsByTypeTheory {

 	@DataPoint
	public static SinglyLinkedListInner l0;
  
	@DataPoint
	public static SinglyLinkedListInner l1;
	
	@DataPoint
	public static SinglyLinkedListInner l2;
	
	@DataPoint
	public static BSTree t0;
	
	@DataPoint
	public static BSTree t1;

	@DataPoint
	public static BSTree t2;
	
 	@DataPoint
	public static LinkedList<Integer> jll0;
  
	@DataPoint
	public static LinkedList<Integer> jll1;
	
	@DataPoint
	public static LinkedList<Integer> jll2;
	
 	@DataPoint
	public static LinkedList<String> jll3;
  
	@DataPoint
	public static LinkedList<String> jll4;
	
	@DataPoint
	public static LinkedList<String> jll5;


	@BeforeClass
	public static void beforeClass() {
		l0 = new SinglyLinkedListInner();
		
		l1 = new SinglyLinkedListInner();
		l1.add(2);

		l2 = new SinglyLinkedListInner();
		l2.add(2);
		l2.add(3);
		
		t0 = new BSTree();
		
		t1 = new BSTree();
		t1.add(2);

		t2 = new BSTree();
		t2.add(2);
		t2.add(3);

		/*
		 * A linked list alone crashes because extensions 
		 * cannot distinguish between an empty LinkedList of
		 * strings and an empty LinkedList of integers
		 * (and the same is true for other types).
		 * jll0 = new LinkedList<>();
		 */
		
		jll0 = new LinkedList<>();
		jll0.add(6);
		jll0.add(7);
		jll0.add(1);
			
		jll1 = new LinkedList<>();
		jll1.add(6);

		jll2 = new LinkedList<>();
		jll2.add(6);
		jll2.add(7);
		
		jll3 = new LinkedList<>();
		
		jll4 = new LinkedList<>();
		jll4.add("i6");

		jll5 = new LinkedList<>();
		jll5.add("i6");
		jll5.add("i7");
		
	}
	

	@Theory
	public void testSinglyLinedListWithInnerNode(Object o1, Object o2) {
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(SinglyLinkedListInner.class.getName());
		classNames.add(BSTree.class.getName());
		classNames.add(LinkedList.class.getName());

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		
		FieldExtensionsCollector collector = new FieldExtensionsByTypeCollector(maxObjects);
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(o1, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext1 = (FieldExtensionsByType) collector.getExtensions();
		/*
		System.out.println(o1.toString());
		System.out.println(ext1.toString());
		*/
		
		FieldExtensionsCollector collector2 = new FieldExtensionsByTypeCollector(maxObjects);
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(o2, collector2);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext2 = (FieldExtensionsByType) collector2.getExtensions();
		/*
		System.out.println(o2.toString());
		System.out.println(ext2.toString());
		*/

		Assert.assertTrue((o1 == o2) == (ext1.containsAll(ext2) & ext2.containsAll(ext1)));
		boolean resadd1 = ext2.addAll(ext1);
		boolean resadd2 = ext1.addAll(ext2);
		Assert.assertTrue(ext1.containsAll(ext2) && ext2.containsAll(ext1));
		Assert.assertTrue((o1 == o2) == (!resadd1 & !resadd2));
		Assert.assertTrue(!ext2.addAll(ext1) & !ext1.addAll(ext2));
		Assert.assertTrue(ext2.containsAll(ext1) & ext1.containsAll(ext2));

	}
	

	
}