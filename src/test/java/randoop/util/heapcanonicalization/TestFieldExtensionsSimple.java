package randoop.util.heapcanonicalization;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;

import randoop.test.datastructures.bstree.BSTree;
import randoop.test.datastructures.singlylistinner.SinglyLinkedListInner;
import randoop.types.PrimitiveType;
import randoop.util.heapcanonicalization.CanonicalHeap;
import randoop.util.heapcanonicalization.CanonicalStore;
import randoop.util.heapcanonicalization.CanonicalizationResult;
import randoop.util.heapcanonicalization.HeapCanonicalizer;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsByType;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsByTypeCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsCollector;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStrings;
import randoop.util.heapcanonicalization.fieldextensions.FieldExtensionsStringsCollector;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A few tests for the vector canonization algorithm.
 */

public class TestFieldExtensionsSimple {

	@Test
	public void SimpleTest1() {
		
		Set<String> classNames = new HashSet<String>();

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, Integer.MAX_VALUE);
		
		FieldExtensionsCollector collector = new FieldExtensionsByTypeCollector();
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize("a", collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext1 = (FieldExtensionsByType) collector.getExtensions();

		collector = new FieldExtensionsByTypeCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize("aa", collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext2 = (FieldExtensionsByType) collector.getExtensions();

		/*
		System.out.println(ext1.toString());
		System.out.println(ext2.toString());
		System.out.println(ext3.toString());
		*/

		Assert.assertTrue(ext2.containsAll(ext1));
		Assert.assertTrue(!ext1.containsAll(ext2));

	}
	
	@Test
	public void SimpleTest2() {
		
		Set<String> classNames = new HashSet<String>();

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, Integer.MAX_VALUE);
		
		FieldExtensionsCollector collector = new FieldExtensionsByTypeCollector();
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(1, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext1 = (FieldExtensionsByType) collector.getExtensions();

		collector = new FieldExtensionsByTypeCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(96, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext2 = (FieldExtensionsByType) collector.getExtensions();

		collector = new FieldExtensionsByTypeCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(97, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsByType ext3 = (FieldExtensionsByType) collector.getExtensions();
		
		/*
		 * 
		System.out.println(o1.toString());
		*/
		
		/*
		System.out.println(ext1.toString());
		System.out.println(ext2.toString());
		System.out.println(ext3.toString());
		*/

		Assert.assertTrue(ext2.addAll(ext1));
		Assert.assertTrue(!ext2.addAll(ext1));
		Assert.assertTrue(ext2.containsAll(ext3));
		Assert.assertTrue(!ext3.containsAll(ext2));
		Assert.assertTrue(!ext2.addAll(ext3));
		Assert.assertTrue(ext3.addAll(ext2));
		Assert.assertTrue(ext2.equals(ext3));
		/*
		System.out.println(ext1.toString());
		System.out.println(ext2.toString());
		System.out.println(ext3.toString());
		*/
		
	}
	
	@Test
	public void SimpleTest3() {
		
		Set<String> classNames = new HashSet<String>();

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, Integer.MAX_VALUE);
		
		FieldExtensionsCollector collector = new FieldExtensionsStringsCollector();
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize("a", collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext1 = (FieldExtensionsStrings) collector.getExtensions();

		collector = new FieldExtensionsStringsCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize("aa", collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext2 = (FieldExtensionsStrings) collector.getExtensions();

		/*
		System.out.println(ext1.toString());
		System.out.println(ext2.toString());
		*/

		Assert.assertTrue(!ext2.containsAll(ext1));
		Assert.assertTrue(!ext1.containsAll(ext2));

	}
	
	@Test
	public void SimpleTest4() {
		
		Set<String> classNames = new HashSet<String>();

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, Integer.MAX_VALUE);
		
		FieldExtensionsCollector collector = new FieldExtensionsStringsCollector();
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(96, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext1 = (FieldExtensionsStrings) collector.getExtensions();

		collector = new FieldExtensionsStringsCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(96, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext2 = (FieldExtensionsStrings) collector.getExtensions();

		collector = new FieldExtensionsStringsCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(97, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext3 = (FieldExtensionsStrings) collector.getExtensions();
		
		/*
		System.out.println(ext1.toString());
		System.out.println(ext2.toString());
		System.out.println(ext3.toString());
		*/

		Assert.assertTrue(ext2.containsAll(ext1));
		Assert.assertTrue(ext1.containsAll(ext2));
		Assert.assertTrue(ext1.equals(ext2));
		Assert.assertTrue(!ext2.addAll(ext1));
		Assert.assertTrue(ext2.containsAll(ext1));
		Assert.assertTrue(ext1.containsAll(ext2));
		Assert.assertTrue(ext1.equals(ext2));

		Assert.assertTrue(!ext3.containsAll(ext2));
		Assert.assertTrue(ext2.addAll(ext3));
		Assert.assertTrue(ext2.containsAll(ext3));
		Assert.assertTrue(!ext3.containsAll(ext2));
		Assert.assertTrue(ext3.addAll(ext2));
		
		/*
		System.out.println(ext1.toString());
		System.out.println(ext2.toString());
		System.out.println(ext3.toString());
		*/
		
	}
	
	@Test
	public void SimpleTest5() {
		
		Set<String> classNames = new HashSet<String>();

		/** Inicializar clases que hacen la canonizacion **/
		// El canonizador toma un set con el nombre de la clase principal, y la cantidad maxima de objetos
		// por clase en los vectores candidatos
		CanonicalStore store = new CanonicalStore(classNames);
		HeapCanonicalizer candVectCanonizer = new HeapCanonicalizer(store, Integer.MAX_VALUE);
		
		FieldExtensionsCollector collector = new FieldExtensionsStringsCollector();
		Entry<CanonicalizationResult, CanonicalHeap> canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(CanonicalizationResult.OK, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext1 = (FieldExtensionsStrings) collector.getExtensions();
		System.out.println(ext1.toString());
		
		collector = new FieldExtensionsStringsCollector();
		canonRes = candVectCanonizer.traverseBreadthFirstAndCanonicalize(randoop.util.heapcanonicalization.fieldextensions.PrimitiveType.INTEGER, collector);
		Assert.assertTrue(canonRes.getKey() == CanonicalizationResult.OK);
		FieldExtensionsStrings ext2 = (FieldExtensionsStrings) collector.getExtensions();
		System.out.println(ext2.toString());
	

	
	}
	
}