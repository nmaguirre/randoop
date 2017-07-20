package randoop.util.heapcanonization;

import org.junit.Assert;
import org.junit.Test;

import randoop.test.datastructures.binheap.BinomialHeap;
import randoop.test.datastructures.bstree.BSTree;
import randoop.test.datastructures.singlylist.SinglyLinkedList;
import randoop.test.datastructures.singlylistinner.SinglyLinkedListInner;
import randoop.test.datastructures.treeset.TreeSet;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVector;
import randoop.util.heapcanonicalization.candidatevectors.CandidateVectorGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A few tests for the vector canonization algorithm.
 */
public class TestVectorCanonization {
   
	@Test
	public void testSinglyLinkedListWithInnerNode() {
		
		String headerOracle = "randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o1.next,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o1.value,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o2.next,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o2.value,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o3.next,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o3.value,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o4.next,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o4.value,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o5.next,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner$Node->o5.value,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o1.header,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o1.size,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o2.header,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o2.size,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o3.header,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o3.size,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o4.header,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o4.size,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o5.header,randoop.test.datastructures.singlylistinner.SinglyLinkedListInner->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "2,0,3,2,4,3,0,4,0,0,1,3,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(SinglyLinkedListInner.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		System.out.println(header.toString());
		Assert.assertTrue(header.toString().equals(headerOracle));
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		SinglyLinkedListInner obj = new SinglyLinkedListInner();
		obj.add(2);
		obj.add(3);
		obj.add(4);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));

	}	
	
	
	@Test
	public void testSinglyLinkedList() {
		
		String headerOracle = "randoop.test.datastructures.singlylist.Node->o1._index,randoop.test.datastructures.singlylist.Node->o1.next,randoop.test.datastructures.singlylist.Node->o1.value,randoop.test.datastructures.singlylist.Node->o2._index,randoop.test.datastructures.singlylist.Node->o2.next,randoop.test.datastructures.singlylist.Node->o2.value,randoop.test.datastructures.singlylist.Node->o3._index,randoop.test.datastructures.singlylist.Node->o3.next,randoop.test.datastructures.singlylist.Node->o3.value,randoop.test.datastructures.singlylist.Node->o4._index,randoop.test.datastructures.singlylist.Node->o4.next,randoop.test.datastructures.singlylist.Node->o4.value,randoop.test.datastructures.singlylist.Node->o5._index,randoop.test.datastructures.singlylist.Node->o5.next,randoop.test.datastructures.singlylist.Node->o5.value,randoop.test.datastructures.singlylist.SinglyLinkedList->o1.header,randoop.test.datastructures.singlylist.SinglyLinkedList->o1.size,randoop.test.datastructures.singlylist.SinglyLinkedList->o2.header,randoop.test.datastructures.singlylist.SinglyLinkedList->o2.size,randoop.test.datastructures.singlylist.SinglyLinkedList->o3.header,randoop.test.datastructures.singlylist.SinglyLinkedList->o3.size,randoop.test.datastructures.singlylist.SinglyLinkedList->o4.header,randoop.test.datastructures.singlylist.SinglyLinkedList->o4.size,randoop.test.datastructures.singlylist.SinglyLinkedList->o5.header,randoop.test.datastructures.singlylist.SinglyLinkedList->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "0,2,0,0,3,2,0,4,3,0,0,4,0,0,0,1,3,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(SinglyLinkedList.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		SinglyLinkedList obj = new SinglyLinkedList();
		obj.add(2);
		obj.add(3);
		obj.add(4);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));

	}
	
	@Test
	public void testJavaLinkedList() {
		
		String headerOracle = "java.util.LinkedList$Node->o1.item,java.util.LinkedList$Node->o1.next,java.util.LinkedList$Node->o1.prev,java.util.LinkedList$Node->o2.item,java.util.LinkedList$Node->o2.next,java.util.LinkedList$Node->o2.prev,java.util.LinkedList$Node->o3.item,java.util.LinkedList$Node->o3.next,java.util.LinkedList$Node->o3.prev,java.util.LinkedList$Node->o4.item,java.util.LinkedList$Node->o4.next,java.util.LinkedList$Node->o4.prev,java.util.LinkedList$Node->o5.item,java.util.LinkedList$Node->o5.next,java.util.LinkedList$Node->o5.prev,java.util.LinkedList->o1.MAX_ARRAY_SIZE,java.util.LinkedList->o1.modCount,java.util.LinkedList->o1.first,java.util.LinkedList->o1.last,java.util.LinkedList->o1.serialVersionUID,java.util.LinkedList->o1.size,java.util.LinkedList->o2.MAX_ARRAY_SIZE,java.util.LinkedList->o2.modCount,java.util.LinkedList->o2.first,java.util.LinkedList->o2.last,java.util.LinkedList->o2.serialVersionUID,java.util.LinkedList->o2.size,java.util.LinkedList->o3.MAX_ARRAY_SIZE,java.util.LinkedList->o3.modCount,java.util.LinkedList->o3.first,java.util.LinkedList->o3.last,java.util.LinkedList->o3.serialVersionUID,java.util.LinkedList->o3.size,java.util.LinkedList->o4.MAX_ARRAY_SIZE,java.util.LinkedList->o4.modCount,java.util.LinkedList->o4.first,java.util.LinkedList->o4.last,java.util.LinkedList->o4.serialVersionUID,java.util.LinkedList->o4.size,java.util.LinkedList->o5.MAX_ARRAY_SIZE,java.util.LinkedList->o5.modCount,java.util.LinkedList->o5.first,java.util.LinkedList->o5.last,java.util.LinkedList->o5.serialVersionUID,java.util.LinkedList->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "2,3,0,4,0,3,3,2,1,0,0,0,0,0,0,2147483639,3,1,2,1179245439,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(LinkedList.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));		
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		LinkedList obj = new LinkedList();
		obj.add(2);
		obj.add(3);
		obj.add(4);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));

	}
	
	@Test
	public void testJavaLinkedListTooLarge() {
		
		String headerOracle = "java.util.LinkedList$Node->o1.item,java.util.LinkedList$Node->o1.next,java.util.LinkedList$Node->o1.prev,java.util.LinkedList$Node->o2.item,java.util.LinkedList$Node->o2.next,java.util.LinkedList$Node->o2.prev,java.util.LinkedList$Node->o3.item,java.util.LinkedList$Node->o3.next,java.util.LinkedList$Node->o3.prev,java.util.LinkedList$Node->o4.item,java.util.LinkedList$Node->o4.next,java.util.LinkedList$Node->o4.prev,java.util.LinkedList$Node->o5.item,java.util.LinkedList$Node->o5.next,java.util.LinkedList$Node->o5.prev,java.util.LinkedList->o1.MAX_ARRAY_SIZE,java.util.LinkedList->o1.modCount,java.util.LinkedList->o1.first,java.util.LinkedList->o1.last,java.util.LinkedList->o1.serialVersionUID,java.util.LinkedList->o1.size,java.util.LinkedList->o2.MAX_ARRAY_SIZE,java.util.LinkedList->o2.modCount,java.util.LinkedList->o2.first,java.util.LinkedList->o2.last,java.util.LinkedList->o2.serialVersionUID,java.util.LinkedList->o2.size,java.util.LinkedList->o3.MAX_ARRAY_SIZE,java.util.LinkedList->o3.modCount,java.util.LinkedList->o3.first,java.util.LinkedList->o3.last,java.util.LinkedList->o3.serialVersionUID,java.util.LinkedList->o3.size,java.util.LinkedList->o4.MAX_ARRAY_SIZE,java.util.LinkedList->o4.modCount,java.util.LinkedList->o4.first,java.util.LinkedList->o4.last,java.util.LinkedList->o4.serialVersionUID,java.util.LinkedList->o4.size,java.util.LinkedList->o5.MAX_ARRAY_SIZE,java.util.LinkedList->o5.modCount,java.util.LinkedList->o5.first,java.util.LinkedList->o5.last,java.util.LinkedList->o5.serialVersionUID,java.util.LinkedList->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "2,3,0,4,0,3,3,2,1,0,0,0,0,0,0,2147483639,3,1,2,1179245439,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(LinkedList.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));		
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		LinkedList obj = new LinkedList();
		for (int i = 0; i<=5; i++) {
			obj.add(i);
		}
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.LIMITS_EXCEEDED);

	}
	
	
	
	@Test
	public void testJavaArrayList() {
		
		String headerOracle = "[Ljava.lang.Object;->o1[0],[Ljava.lang.Object;->o1[1],[Ljava.lang.Object;->o1[2],[Ljava.lang.Object;->o1[3],[Ljava.lang.Object;->o1[4],[Ljava.lang.Object;->o1[5],[Ljava.lang.Object;->o1[6],[Ljava.lang.Object;->o1[7],[Ljava.lang.Object;->o1[8],[Ljava.lang.Object;->o1[9],[Ljava.lang.Object;->o2[0],[Ljava.lang.Object;->o2[1],[Ljava.lang.Object;->o2[2],[Ljava.lang.Object;->o2[3],[Ljava.lang.Object;->o2[4],[Ljava.lang.Object;->o2[5],[Ljava.lang.Object;->o2[6],[Ljava.lang.Object;->o2[7],[Ljava.lang.Object;->o2[8],[Ljava.lang.Object;->o2[9],[Ljava.lang.Object;->o3[0],[Ljava.lang.Object;->o3[1],[Ljava.lang.Object;->o3[2],[Ljava.lang.Object;->o3[3],[Ljava.lang.Object;->o3[4],[Ljava.lang.Object;->o3[5],[Ljava.lang.Object;->o3[6],[Ljava.lang.Object;->o3[7],[Ljava.lang.Object;->o3[8],[Ljava.lang.Object;->o3[9],[Ljava.lang.Object;->o4[0],[Ljava.lang.Object;->o4[1],[Ljava.lang.Object;->o4[2],[Ljava.lang.Object;->o4[3],[Ljava.lang.Object;->o4[4],[Ljava.lang.Object;->o4[5],[Ljava.lang.Object;->o4[6],[Ljava.lang.Object;->o4[7],[Ljava.lang.Object;->o4[8],[Ljava.lang.Object;->o4[9],[Ljava.lang.Object;->o5[0],[Ljava.lang.Object;->o5[1],[Ljava.lang.Object;->o5[2],[Ljava.lang.Object;->o5[3],[Ljava.lang.Object;->o5[4],[Ljava.lang.Object;->o5[5],[Ljava.lang.Object;->o5[6],[Ljava.lang.Object;->o5[7],[Ljava.lang.Object;->o5[8],[Ljava.lang.Object;->o5[9],[Ljava.lang.Object;->o6[0],[Ljava.lang.Object;->o6[1],[Ljava.lang.Object;->o6[2],[Ljava.lang.Object;->o6[3],[Ljava.lang.Object;->o6[4],[Ljava.lang.Object;->o6[5],[Ljava.lang.Object;->o6[6],[Ljava.lang.Object;->o6[7],[Ljava.lang.Object;->o6[8],[Ljava.lang.Object;->o6[9],[Ljava.lang.Object;->o7[0],[Ljava.lang.Object;->o7[1],[Ljava.lang.Object;->o7[2],[Ljava.lang.Object;->o7[3],[Ljava.lang.Object;->o7[4],[Ljava.lang.Object;->o7[5],[Ljava.lang.Object;->o7[6],[Ljava.lang.Object;->o7[7],[Ljava.lang.Object;->o7[8],[Ljava.lang.Object;->o7[9],[Ljava.lang.Object;->o8[0],[Ljava.lang.Object;->o8[1],[Ljava.lang.Object;->o8[2],[Ljava.lang.Object;->o8[3],[Ljava.lang.Object;->o8[4],[Ljava.lang.Object;->o8[5],[Ljava.lang.Object;->o8[6],[Ljava.lang.Object;->o8[7],[Ljava.lang.Object;->o8[8],[Ljava.lang.Object;->o8[9],[Ljava.lang.Object;->o9[0],[Ljava.lang.Object;->o9[1],[Ljava.lang.Object;->o9[2],[Ljava.lang.Object;->o9[3],[Ljava.lang.Object;->o9[4],[Ljava.lang.Object;->o9[5],[Ljava.lang.Object;->o9[6],[Ljava.lang.Object;->o9[7],[Ljava.lang.Object;->o9[8],[Ljava.lang.Object;->o9[9],[Ljava.lang.Object;->o10[0],[Ljava.lang.Object;->o10[1],[Ljava.lang.Object;->o10[2],[Ljava.lang.Object;->o10[3],[Ljava.lang.Object;->o10[4],[Ljava.lang.Object;->o10[5],[Ljava.lang.Object;->o10[6],[Ljava.lang.Object;->o10[7],[Ljava.lang.Object;->o10[8],[Ljava.lang.Object;->o10[9],java.util.ArrayList->o1.MAX_ARRAY_SIZE,java.util.ArrayList->o1.modCount,java.util.ArrayList->o1.DEFAULT_CAPACITY,java.util.ArrayList->o1.EMPTY_ELEMENTDATA,java.util.ArrayList->o1.MAX_ARRAY_SIZE,java.util.ArrayList->o1.elementData,java.util.ArrayList->o1.serialVersionUID,java.util.ArrayList->o1.size,java.util.ArrayList->o2.MAX_ARRAY_SIZE,java.util.ArrayList->o2.modCount,java.util.ArrayList->o2.DEFAULT_CAPACITY,java.util.ArrayList->o2.EMPTY_ELEMENTDATA,java.util.ArrayList->o2.MAX_ARRAY_SIZE,java.util.ArrayList->o2.elementData,java.util.ArrayList->o2.serialVersionUID,java.util.ArrayList->o2.size,java.util.ArrayList->o3.MAX_ARRAY_SIZE,java.util.ArrayList->o3.modCount,java.util.ArrayList->o3.DEFAULT_CAPACITY,java.util.ArrayList->o3.EMPTY_ELEMENTDATA,java.util.ArrayList->o3.MAX_ARRAY_SIZE,java.util.ArrayList->o3.elementData,java.util.ArrayList->o3.serialVersionUID,java.util.ArrayList->o3.size,java.util.ArrayList->o4.MAX_ARRAY_SIZE,java.util.ArrayList->o4.modCount,java.util.ArrayList->o4.DEFAULT_CAPACITY,java.util.ArrayList->o4.EMPTY_ELEMENTDATA,java.util.ArrayList->o4.MAX_ARRAY_SIZE,java.util.ArrayList->o4.elementData,java.util.ArrayList->o4.serialVersionUID,java.util.ArrayList->o4.size,java.util.ArrayList->o5.MAX_ARRAY_SIZE,java.util.ArrayList->o5.modCount,java.util.ArrayList->o5.DEFAULT_CAPACITY,java.util.ArrayList->o5.EMPTY_ELEMENTDATA,java.util.ArrayList->o5.MAX_ARRAY_SIZE,java.util.ArrayList->o5.elementData,java.util.ArrayList->o5.serialVersionUID,java.util.ArrayList->o5.size,java.util.ArrayList->o6.MAX_ARRAY_SIZE,java.util.ArrayList->o6.modCount,java.util.ArrayList->o6.DEFAULT_CAPACITY,java.util.ArrayList->o6.EMPTY_ELEMENTDATA,java.util.ArrayList->o6.MAX_ARRAY_SIZE,java.util.ArrayList->o6.elementData,java.util.ArrayList->o6.serialVersionUID,java.util.ArrayList->o6.size,java.util.ArrayList->o7.MAX_ARRAY_SIZE,java.util.ArrayList->o7.modCount,java.util.ArrayList->o7.DEFAULT_CAPACITY,java.util.ArrayList->o7.EMPTY_ELEMENTDATA,java.util.ArrayList->o7.MAX_ARRAY_SIZE,java.util.ArrayList->o7.elementData,java.util.ArrayList->o7.serialVersionUID,java.util.ArrayList->o7.size,java.util.ArrayList->o8.MAX_ARRAY_SIZE,java.util.ArrayList->o8.modCount,java.util.ArrayList->o8.DEFAULT_CAPACITY,java.util.ArrayList->o8.EMPTY_ELEMENTDATA,java.util.ArrayList->o8.MAX_ARRAY_SIZE,java.util.ArrayList->o8.elementData,java.util.ArrayList->o8.serialVersionUID,java.util.ArrayList->o8.size,java.util.ArrayList->o9.MAX_ARRAY_SIZE,java.util.ArrayList->o9.modCount,java.util.ArrayList->o9.DEFAULT_CAPACITY,java.util.ArrayList->o9.EMPTY_ELEMENTDATA,java.util.ArrayList->o9.MAX_ARRAY_SIZE,java.util.ArrayList->o9.elementData,java.util.ArrayList->o9.serialVersionUID,java.util.ArrayList->o9.size,java.util.ArrayList->o10.MAX_ARRAY_SIZE,java.util.ArrayList->o10.modCount,java.util.ArrayList->o10.DEFAULT_CAPACITY,java.util.ArrayList->o10.EMPTY_ELEMENTDATA,java.util.ArrayList->o10.MAX_ARRAY_SIZE,java.util.ArrayList->o10.elementData,java.util.ArrayList->o10.serialVersionUID,java.util.ArrayList->o10.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "0,0,0,0,0,0,0,0,0,0,2,3,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2147483639,3,10,1,2147483639,2,-515460224,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 10;
		Set<String> classNames = new HashSet<String>();
		classNames.add(ArrayList.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));		
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		ArrayList obj = new ArrayList();
		obj.add(2);
		obj.add(3);
		obj.add(4);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));
	}
	
	
	@Test
	public void testJavaArrayListTooLarge() {
		
		String headerOracle = "[Ljava.lang.Object;->o1[0],[Ljava.lang.Object;->o1[1],[Ljava.lang.Object;->o1[2],[Ljava.lang.Object;->o1[3],[Ljava.lang.Object;->o1[4],[Ljava.lang.Object;->o1[5],[Ljava.lang.Object;->o1[6],[Ljava.lang.Object;->o1[7],[Ljava.lang.Object;->o1[8],[Ljava.lang.Object;->o1[9],[Ljava.lang.Object;->o2[0],[Ljava.lang.Object;->o2[1],[Ljava.lang.Object;->o2[2],[Ljava.lang.Object;->o2[3],[Ljava.lang.Object;->o2[4],[Ljava.lang.Object;->o2[5],[Ljava.lang.Object;->o2[6],[Ljava.lang.Object;->o2[7],[Ljava.lang.Object;->o2[8],[Ljava.lang.Object;->o2[9],[Ljava.lang.Object;->o3[0],[Ljava.lang.Object;->o3[1],[Ljava.lang.Object;->o3[2],[Ljava.lang.Object;->o3[3],[Ljava.lang.Object;->o3[4],[Ljava.lang.Object;->o3[5],[Ljava.lang.Object;->o3[6],[Ljava.lang.Object;->o3[7],[Ljava.lang.Object;->o3[8],[Ljava.lang.Object;->o3[9],[Ljava.lang.Object;->o4[0],[Ljava.lang.Object;->o4[1],[Ljava.lang.Object;->o4[2],[Ljava.lang.Object;->o4[3],[Ljava.lang.Object;->o4[4],[Ljava.lang.Object;->o4[5],[Ljava.lang.Object;->o4[6],[Ljava.lang.Object;->o4[7],[Ljava.lang.Object;->o4[8],[Ljava.lang.Object;->o4[9],[Ljava.lang.Object;->o5[0],[Ljava.lang.Object;->o5[1],[Ljava.lang.Object;->o5[2],[Ljava.lang.Object;->o5[3],[Ljava.lang.Object;->o5[4],[Ljava.lang.Object;->o5[5],[Ljava.lang.Object;->o5[6],[Ljava.lang.Object;->o5[7],[Ljava.lang.Object;->o5[8],[Ljava.lang.Object;->o5[9],[Ljava.lang.Object;->o6[0],[Ljava.lang.Object;->o6[1],[Ljava.lang.Object;->o6[2],[Ljava.lang.Object;->o6[3],[Ljava.lang.Object;->o6[4],[Ljava.lang.Object;->o6[5],[Ljava.lang.Object;->o6[6],[Ljava.lang.Object;->o6[7],[Ljava.lang.Object;->o6[8],[Ljava.lang.Object;->o6[9],[Ljava.lang.Object;->o7[0],[Ljava.lang.Object;->o7[1],[Ljava.lang.Object;->o7[2],[Ljava.lang.Object;->o7[3],[Ljava.lang.Object;->o7[4],[Ljava.lang.Object;->o7[5],[Ljava.lang.Object;->o7[6],[Ljava.lang.Object;->o7[7],[Ljava.lang.Object;->o7[8],[Ljava.lang.Object;->o7[9],[Ljava.lang.Object;->o8[0],[Ljava.lang.Object;->o8[1],[Ljava.lang.Object;->o8[2],[Ljava.lang.Object;->o8[3],[Ljava.lang.Object;->o8[4],[Ljava.lang.Object;->o8[5],[Ljava.lang.Object;->o8[6],[Ljava.lang.Object;->o8[7],[Ljava.lang.Object;->o8[8],[Ljava.lang.Object;->o8[9],[Ljava.lang.Object;->o9[0],[Ljava.lang.Object;->o9[1],[Ljava.lang.Object;->o9[2],[Ljava.lang.Object;->o9[3],[Ljava.lang.Object;->o9[4],[Ljava.lang.Object;->o9[5],[Ljava.lang.Object;->o9[6],[Ljava.lang.Object;->o9[7],[Ljava.lang.Object;->o9[8],[Ljava.lang.Object;->o9[9],[Ljava.lang.Object;->o10[0],[Ljava.lang.Object;->o10[1],[Ljava.lang.Object;->o10[2],[Ljava.lang.Object;->o10[3],[Ljava.lang.Object;->o10[4],[Ljava.lang.Object;->o10[5],[Ljava.lang.Object;->o10[6],[Ljava.lang.Object;->o10[7],[Ljava.lang.Object;->o10[8],[Ljava.lang.Object;->o10[9],java.util.ArrayList->o1.MAX_ARRAY_SIZE,java.util.ArrayList->o1.modCount,java.util.ArrayList->o1.DEFAULT_CAPACITY,java.util.ArrayList->o1.EMPTY_ELEMENTDATA,java.util.ArrayList->o1.MAX_ARRAY_SIZE,java.util.ArrayList->o1.elementData,java.util.ArrayList->o1.serialVersionUID,java.util.ArrayList->o1.size,java.util.ArrayList->o2.MAX_ARRAY_SIZE,java.util.ArrayList->o2.modCount,java.util.ArrayList->o2.DEFAULT_CAPACITY,java.util.ArrayList->o2.EMPTY_ELEMENTDATA,java.util.ArrayList->o2.MAX_ARRAY_SIZE,java.util.ArrayList->o2.elementData,java.util.ArrayList->o2.serialVersionUID,java.util.ArrayList->o2.size,java.util.ArrayList->o3.MAX_ARRAY_SIZE,java.util.ArrayList->o3.modCount,java.util.ArrayList->o3.DEFAULT_CAPACITY,java.util.ArrayList->o3.EMPTY_ELEMENTDATA,java.util.ArrayList->o3.MAX_ARRAY_SIZE,java.util.ArrayList->o3.elementData,java.util.ArrayList->o3.serialVersionUID,java.util.ArrayList->o3.size,java.util.ArrayList->o4.MAX_ARRAY_SIZE,java.util.ArrayList->o4.modCount,java.util.ArrayList->o4.DEFAULT_CAPACITY,java.util.ArrayList->o4.EMPTY_ELEMENTDATA,java.util.ArrayList->o4.MAX_ARRAY_SIZE,java.util.ArrayList->o4.elementData,java.util.ArrayList->o4.serialVersionUID,java.util.ArrayList->o4.size,java.util.ArrayList->o5.MAX_ARRAY_SIZE,java.util.ArrayList->o5.modCount,java.util.ArrayList->o5.DEFAULT_CAPACITY,java.util.ArrayList->o5.EMPTY_ELEMENTDATA,java.util.ArrayList->o5.MAX_ARRAY_SIZE,java.util.ArrayList->o5.elementData,java.util.ArrayList->o5.serialVersionUID,java.util.ArrayList->o5.size,java.util.ArrayList->o6.MAX_ARRAY_SIZE,java.util.ArrayList->o6.modCount,java.util.ArrayList->o6.DEFAULT_CAPACITY,java.util.ArrayList->o6.EMPTY_ELEMENTDATA,java.util.ArrayList->o6.MAX_ARRAY_SIZE,java.util.ArrayList->o6.elementData,java.util.ArrayList->o6.serialVersionUID,java.util.ArrayList->o6.size,java.util.ArrayList->o7.MAX_ARRAY_SIZE,java.util.ArrayList->o7.modCount,java.util.ArrayList->o7.DEFAULT_CAPACITY,java.util.ArrayList->o7.EMPTY_ELEMENTDATA,java.util.ArrayList->o7.MAX_ARRAY_SIZE,java.util.ArrayList->o7.elementData,java.util.ArrayList->o7.serialVersionUID,java.util.ArrayList->o7.size,java.util.ArrayList->o8.MAX_ARRAY_SIZE,java.util.ArrayList->o8.modCount,java.util.ArrayList->o8.DEFAULT_CAPACITY,java.util.ArrayList->o8.EMPTY_ELEMENTDATA,java.util.ArrayList->o8.MAX_ARRAY_SIZE,java.util.ArrayList->o8.elementData,java.util.ArrayList->o8.serialVersionUID,java.util.ArrayList->o8.size,java.util.ArrayList->o9.MAX_ARRAY_SIZE,java.util.ArrayList->o9.modCount,java.util.ArrayList->o9.DEFAULT_CAPACITY,java.util.ArrayList->o9.EMPTY_ELEMENTDATA,java.util.ArrayList->o9.MAX_ARRAY_SIZE,java.util.ArrayList->o9.elementData,java.util.ArrayList->o9.serialVersionUID,java.util.ArrayList->o9.size,java.util.ArrayList->o10.MAX_ARRAY_SIZE,java.util.ArrayList->o10.modCount,java.util.ArrayList->o10.DEFAULT_CAPACITY,java.util.ArrayList->o10.EMPTY_ELEMENTDATA,java.util.ArrayList->o10.MAX_ARRAY_SIZE,java.util.ArrayList->o10.elementData,java.util.ArrayList->o10.serialVersionUID,java.util.ArrayList->o10.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "0,0,0,0,0,0,0,0,0,0,2,3,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2147483639,3,10,1,2147483639,2,-515460224,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 10;
		Set<String> classNames = new HashSet<String>();
		classNames.add(ArrayList.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));		
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		ArrayList obj = new ArrayList();
		for (int i = 0; i<=10; i++) {
			obj.add(i);
		}
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.ARRAY_LIMITS_EXCEEDED);

	}
	
	
	
	@Test
	public void testBSTree() {
		
		String headerOracle = "randoop.test.datastructures.bstree.Node->o1._index,randoop.test.datastructures.bstree.Node->o1.key,randoop.test.datastructures.bstree.Node->o1.left,randoop.test.datastructures.bstree.Node->o1.right,randoop.test.datastructures.bstree.Node->o2._index,randoop.test.datastructures.bstree.Node->o2.key,randoop.test.datastructures.bstree.Node->o2.left,randoop.test.datastructures.bstree.Node->o2.right,randoop.test.datastructures.bstree.Node->o3._index,randoop.test.datastructures.bstree.Node->o3.key,randoop.test.datastructures.bstree.Node->o3.left,randoop.test.datastructures.bstree.Node->o3.right,randoop.test.datastructures.bstree.Node->o4._index,randoop.test.datastructures.bstree.Node->o4.key,randoop.test.datastructures.bstree.Node->o4.left,randoop.test.datastructures.bstree.Node->o4.right,randoop.test.datastructures.bstree.Node->o5._index,randoop.test.datastructures.bstree.Node->o5.key,randoop.test.datastructures.bstree.Node->o5.left,randoop.test.datastructures.bstree.Node->o5.right,randoop.test.datastructures.bstree.BSTree->o1.root,randoop.test.datastructures.bstree.BSTree->o1.size,randoop.test.datastructures.bstree.BSTree->o2.root,randoop.test.datastructures.bstree.BSTree->o2.size,randoop.test.datastructures.bstree.BSTree->o3.root,randoop.test.datastructures.bstree.BSTree->o3.size,randoop.test.datastructures.bstree.BSTree->o4.root,randoop.test.datastructures.bstree.BSTree->o4.size,randoop.test.datastructures.bstree.BSTree->o5.root,randoop.test.datastructures.bstree.BSTree->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "0,2,2,3,0,1,0,0,0,3,0,4,0,4,0,0,0,0,0,0,1,4,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(BSTree.class.getName());
		
		// O lo que es lo mismo que la linea anterior:
		// classNames.add("java.util.LinkedList");

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		BSTree obj = new BSTree();
		obj.add(2);
		obj.add(3);
		obj.add(4);
		obj.add(1);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));

	}
	
	
	@Test
	public void testTreeSet() {
		
		String headerOracle = "randoop.test.datastructures.treeset.TreeSetEntry->o1._index,randoop.test.datastructures.treeset.TreeSetEntry->o1.color,randoop.test.datastructures.treeset.TreeSetEntry->o1.key,randoop.test.datastructures.treeset.TreeSetEntry->o1.left,randoop.test.datastructures.treeset.TreeSetEntry->o1.parent,randoop.test.datastructures.treeset.TreeSetEntry->o1.right,randoop.test.datastructures.treeset.TreeSetEntry->o2._index,randoop.test.datastructures.treeset.TreeSetEntry->o2.color,randoop.test.datastructures.treeset.TreeSetEntry->o2.key,randoop.test.datastructures.treeset.TreeSetEntry->o2.left,randoop.test.datastructures.treeset.TreeSetEntry->o2.parent,randoop.test.datastructures.treeset.TreeSetEntry->o2.right,randoop.test.datastructures.treeset.TreeSetEntry->o3._index,randoop.test.datastructures.treeset.TreeSetEntry->o3.color,randoop.test.datastructures.treeset.TreeSetEntry->o3.key,randoop.test.datastructures.treeset.TreeSetEntry->o3.left,randoop.test.datastructures.treeset.TreeSetEntry->o3.parent,randoop.test.datastructures.treeset.TreeSetEntry->o3.right,randoop.test.datastructures.treeset.TreeSetEntry->o4._index,randoop.test.datastructures.treeset.TreeSetEntry->o4.color,randoop.test.datastructures.treeset.TreeSetEntry->o4.key,randoop.test.datastructures.treeset.TreeSetEntry->o4.left,randoop.test.datastructures.treeset.TreeSetEntry->o4.parent,randoop.test.datastructures.treeset.TreeSetEntry->o4.right,randoop.test.datastructures.treeset.TreeSetEntry->o5._index,randoop.test.datastructures.treeset.TreeSetEntry->o5.color,randoop.test.datastructures.treeset.TreeSetEntry->o5.key,randoop.test.datastructures.treeset.TreeSetEntry->o5.left,randoop.test.datastructures.treeset.TreeSetEntry->o5.parent,randoop.test.datastructures.treeset.TreeSetEntry->o5.right,randoop.test.datastructures.treeset.TreeSet->o1.BLACK,randoop.test.datastructures.treeset.TreeSet->o1.RED,randoop.test.datastructures.treeset.TreeSet->o1.root,randoop.test.datastructures.treeset.TreeSet->o1.size,randoop.test.datastructures.treeset.TreeSet->o2.BLACK,randoop.test.datastructures.treeset.TreeSet->o2.RED,randoop.test.datastructures.treeset.TreeSet->o2.root,randoop.test.datastructures.treeset.TreeSet->o2.size,randoop.test.datastructures.treeset.TreeSet->o3.BLACK,randoop.test.datastructures.treeset.TreeSet->o3.RED,randoop.test.datastructures.treeset.TreeSet->o3.root,randoop.test.datastructures.treeset.TreeSet->o3.size,randoop.test.datastructures.treeset.TreeSet->o4.BLACK,randoop.test.datastructures.treeset.TreeSet->o4.RED,randoop.test.datastructures.treeset.TreeSet->o4.root,randoop.test.datastructures.treeset.TreeSet->o4.size,randoop.test.datastructures.treeset.TreeSet->o5.BLACK,randoop.test.datastructures.treeset.TreeSet->o5.RED,randoop.test.datastructures.treeset.TreeSet->o5.root,randoop.test.datastructures.treeset.TreeSet->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "0,1,3,2,0,3,0,1,1,4,1,5,0,1,4,0,1,0,0,0,0,0,2,0,0,0,2,0,2,0,1,0,1,5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(TreeSet.class.getName());

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		TreeSet obj = new TreeSet();
		obj.add(2);
		obj.add(3);
		obj.add(4);
		obj.add(1);
		obj.add(0);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));

	}
	
	
	@Test
	public void testBinHeap() {
		
		String headerOracle = "randoop.test.datastructures.binheap.BinomialHeapNode->o1._index,randoop.test.datastructures.binheap.BinomialHeapNode->o1.child,randoop.test.datastructures.binheap.BinomialHeapNode->o1.degree,randoop.test.datastructures.binheap.BinomialHeapNode->o1.key,randoop.test.datastructures.binheap.BinomialHeapNode->o1.parent,randoop.test.datastructures.binheap.BinomialHeapNode->o1.sibling,randoop.test.datastructures.binheap.BinomialHeapNode->o2._index,randoop.test.datastructures.binheap.BinomialHeapNode->o2.child,randoop.test.datastructures.binheap.BinomialHeapNode->o2.degree,randoop.test.datastructures.binheap.BinomialHeapNode->o2.key,randoop.test.datastructures.binheap.BinomialHeapNode->o2.parent,randoop.test.datastructures.binheap.BinomialHeapNode->o2.sibling,randoop.test.datastructures.binheap.BinomialHeapNode->o3._index,randoop.test.datastructures.binheap.BinomialHeapNode->o3.child,randoop.test.datastructures.binheap.BinomialHeapNode->o3.degree,randoop.test.datastructures.binheap.BinomialHeapNode->o3.key,randoop.test.datastructures.binheap.BinomialHeapNode->o3.parent,randoop.test.datastructures.binheap.BinomialHeapNode->o3.sibling,randoop.test.datastructures.binheap.BinomialHeapNode->o4._index,randoop.test.datastructures.binheap.BinomialHeapNode->o4.child,randoop.test.datastructures.binheap.BinomialHeapNode->o4.degree,randoop.test.datastructures.binheap.BinomialHeapNode->o4.key,randoop.test.datastructures.binheap.BinomialHeapNode->o4.parent,randoop.test.datastructures.binheap.BinomialHeapNode->o4.sibling,randoop.test.datastructures.binheap.BinomialHeapNode->o5._index,randoop.test.datastructures.binheap.BinomialHeapNode->o5.child,randoop.test.datastructures.binheap.BinomialHeapNode->o5.degree,randoop.test.datastructures.binheap.BinomialHeapNode->o5.key,randoop.test.datastructures.binheap.BinomialHeapNode->o5.parent,randoop.test.datastructures.binheap.BinomialHeapNode->o5.sibling,randoop.test.datastructures.binheap.BinomialHeap->o1.Nodes,randoop.test.datastructures.binheap.BinomialHeap->o1.size,randoop.test.datastructures.binheap.BinomialHeap->o2.Nodes,randoop.test.datastructures.binheap.BinomialHeap->o2.size,randoop.test.datastructures.binheap.BinomialHeap->o3.Nodes,randoop.test.datastructures.binheap.BinomialHeap->o3.size,randoop.test.datastructures.binheap.BinomialHeap->o4.Nodes,randoop.test.datastructures.binheap.BinomialHeap->o4.size,randoop.test.datastructures.binheap.BinomialHeap->o5.Nodes,randoop.test.datastructures.binheap.BinomialHeap->o5.size,randoop.util.heapcanonicalization.DummyHeapRoot->o1.theroot";
		String vectorOracle = "0,0,0,3,0,2,0,3,2,0,0,0,0,4,1,1,2,5,0,0,0,2,3,0,0,0,0,9,2,0,1,5,0,0,0,0,0,0,0,0,1";
		
		int maxObjects = 5;
		Set<String> classNames = new HashSet<String>();
		classNames.add(BinomialHeap.class.getName());

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, maxObjects);
		// El generador de vectores candidatos toma los nombres de las clases que el canonizador saco del codigo fuente
		CandidateVectorGenerator candVectGenerator = new CandidateVectorGenerator(store.getAllCanonicalClassnames());
	
		/* Descomentar esto para imprimir el header de los vectores candidatos */
		CanonicalHeap emptyHeap = new CanonicalHeap(store, maxObjects);
		CandidateVector<String> header = candVectGenerator.makeCandidateVectorsHeader(emptyHeap);
		Assert.assertTrue(header.toString().equals(headerOracle));
		
		/** Crear el objeto a canonizar. Esto es lo unico que cambia, el resto del codigo es siempre igual **/
		//List<Integer> obj = new LinkedList<>();
		//obj.add(2);
		BinomialHeap obj = new BinomialHeap();
		obj.insert(2);
		obj.insert(1);
		obj.insert(9);
		obj.insert(0);
		obj.insert(3);
		
		/** Canonizar el objeto creado **/
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonize(obj);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		CandidateVector<Integer> candVect = candVectGenerator.makeCandidateVectorFrom(canonRes.getValue());
		Assert.assertTrue(candVect.toString().equals(vectorOracle));

	}
	
}