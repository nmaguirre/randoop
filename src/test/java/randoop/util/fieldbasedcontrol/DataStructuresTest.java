package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


import org.jgrapht.graph.DirectedPseudograph;

import org.junit.Test;

import randoop.util.fieldbasedcontrol.HeapDump;
import randoop.util.fieldbasedcontrol.HeapVertex;
import randoop.util.fieldbasedcontrol.LabeledEdge;
import randoop.util.fieldexhuastive.structures.LinkedList;
import randoop.util.fieldexhuastive.structures.LinkedListNode;


public class DataStructuresTest {

  @Test
  public void testOneNodeListGraphHasAllComponents() throws IllegalArgumentException, IllegalAccessException {
	LinkedList l = new LinkedList();
	l.add(3);
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, null);
    DirectedPseudograph<HeapVertex, LabeledEdge> g = objectDump.getHeap();
    
    Set<HeapVertex> vertices = g.vertexSet();
    // One LinkedList objects, 2 Nodes, 2 Ints (1 for size and 3 for the value of the first node);
    assertTrue(vertices.size() == 6);/*
    for (HeapVertex vertex : vertices) {
    	Object o = vertex.getObject();
    	if (o == null) continue;
    	
    	if (o.getClass() == LinkedList.class) {
    		Set<LabeledEdge> outEdges = g.outgoingEdgesOf(vertex);
    		boolean hasHead = false, hasSize = false;    		
    		for (LabeledEdge<HeapVertex> e: g.outgoingEdgesOf(vertex)) {
    			assertTrue(e.getLabel().equals("header") || e.getLabel().equals("size"));
    			if (e.getLabel().equals("header")) {
    				hasHead = true;
    			}
    			if (e.getLabel().equals("size")) {
    				hasSize = true;
    			}
        	}
			assertTrue(hasHead);
			assertTrue(hasSize);
    		assertTrue(outEdges.size() == 2);			
    	}
    	
    	if (o.getClass() == LinkedListNode.class) {
    		Set<LabeledEdge> outEdges = g.outgoingEdgesOf(vertex);
    		boolean hasNext = false, hasPrev = false, hasVal = false;    		
    		for (LabeledEdge<HeapVertex> e: g.outgoingEdgesOf(vertex)) {
    			assertTrue(e.getLabel().equals("next") || e.getLabel().equals("previous") || e.getLabel().equals("value"));
    			if (e.getLabel().equals("next")) {
    				hasNext = true;
    			}
    			if (e.getLabel().equals("previous")) {
    				hasPrev = true;
    			}
    			if (e.getLabel().equals("value")) {
    				hasVal = true;
    			}
        	}
			assertTrue(hasPrev);
			assertTrue(hasVal);
			assertTrue(hasNext);    		
			assertTrue(outEdges.size() == 3);
    	}
    } */

  }
  
  
  @Test
  public void testLinkedListPrintGraph() throws IllegalArgumentException, IllegalAccessException, IOException {
	LinkedList l = new LinkedList();
	l.add(3);
	l.add(214);
	l.add("hola");
	l.add("chau");
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, null);

    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/llistgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/llistextensions.txt");
    
    System.out.println("Graph:");
    System.out.println(objectDump.heapToString());
    
    System.out.println("\n\nExtensions");
    System.out.println(objectDump.getFieldExtensions().toString());
  }
  
  
  @Test
  public void testArrayListPrintGraph() throws IllegalArgumentException, IllegalAccessException, IOException {
	ArrayList l = new ArrayList();
	String s = "3";
	l.add(s);
	l.add("214");
	l.add("hola");
	l.add("chau");
	l.add(s);
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, null);
   
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldexhaustivecontrol/arrlistgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldexhaustivecontrol/arrlistextensions.txt");
    
    System.out.println("Graph:");
    System.out.println(objectDump.heapToString());
    
    System.out.println("\n\nExtensions");
    System.out.println(objectDump.getFieldExtensions().toString());
  }
  
  
  
  
  
  @Test
  public void testTreeSetPrintGraph() throws IllegalArgumentException, IllegalAccessException, IOException {
	TreeSet l = new TreeSet();
	l.add("a");
	l.add("b");
	l.add("c");
	l.add("d");
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, new String [] {"BLACK", "RED", "parent", "values", "entrySet", "this$0", "MAX_ARRAY_SIZE", "navigableKeySet", "PRESENT","comparator", "descendingMap", "keySet", "modCount", "UNBOUNDED", "serialVersionUID"});
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldexhaustivecontrol/tsetgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldexhaustivecontrol/tsetextensions.txt");
    
    
    System.out.println("Graph:");
    System.out.println(objectDump.heapToString());
    
    System.out.println("\n\nExtensions");
    System.out.println(objectDump.getFieldExtensions().toString());
    
  }

  
  @Test
  public void testStringPrintGraph() throws IllegalArgumentException, IllegalAccessException, IOException {
	String l = "abc";
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, null);
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/astring.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/astringextensions.txt");

  }
  
  
  
  private String objectFieldsToString(Object o) throws IllegalArgumentException, IllegalAccessException {
	  String res = "";
	  Class currObjClass = o.getClass();
	  while (currObjClass != null && 
			currObjClass != Object.class) {
		res += "> Class: " + currObjClass.getSimpleName() + "\n";
		for (Field f: currObjClass.getDeclaredFields()) {
			f.setAccessible(true);
			res += "   " + o.toString() + "." + f.getName() + " = " + f.get(o).toString() + "\n";
		}
		currObjClass = currObjClass.getSuperclass();
	  }
	  return res;
  }
  
  /*
	return (clazz.isPrimitive()
				|| clazz == java.lang.Short.class
				|| clazz == java.lang.Long.class
				|| clazz == java.lang.String.class
				|| clazz == java.lang.Integer.class
				|| clazz == java.lang.Float.class
				|| clazz == java.lang.Byte.class
				|| clazz == java.lang.Character.class
				|| clazz == java.lang.Double.class
				|| clazz == java.lang.Boolean.class
				|| clazz == java.util.Date.class
				|| clazz.isEnum());
  */

 
  private enum Hola { A, B ,C };

  @Test
  public void testBasicTypes() throws IllegalArgumentException, IllegalAccessException {
	Integer i1 = new Integer(3);  
	System.out.println(objectFieldsToString(i1));
	System.out.println(objectFieldsToString(3));
	Integer i2 = new Integer(24179379);
	System.out.println(objectFieldsToString(i2));

	System.out.println(objectFieldsToString("hola"));
	System.out.println(objectFieldsToString("hola mundo"));
	
	System.out.println(objectFieldsToString(new Byte((byte) 3)));
	System.out.println(objectFieldsToString('a'));
	
	System.out.println(objectFieldsToString(Hola.A));
	  // A.name = A
	  // A.ordinal = 0
  }

}
