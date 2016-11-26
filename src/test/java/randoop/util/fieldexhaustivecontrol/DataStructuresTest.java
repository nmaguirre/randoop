package randoop.util.fieldexhaustivecontrol;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;


import org.jgrapht.graph.DirectedPseudograph;

import org.junit.Test;

import randoop.util.fieldexhaustivecontrol.HeapDump;
import randoop.util.fieldexhaustivecontrol.HeapVertex;
import randoop.util.fieldexhaustivecontrol.LabeledEdge;
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
    assertTrue(vertices.size() == 6);
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

    }

  }
  
  
  @Test
  public void testeListPrintGraph() throws IllegalArgumentException, IllegalAccessException {
	LinkedList l = new LinkedList();
	l.add(3);
	l.add(214);
	l.add("hola");
	l.add("chau");
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, null);
    
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
  
}
