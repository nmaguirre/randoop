package randoop.util.fieldExhaustiveControl;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.junit.Test;

public class LinkedListDumpTest {



  @Test
  public void testLinkedListDump() {
	LinkedList l = new LinkedList();
	l.add(3);
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses);
    
    DefaultDirectedGraph<Object, LabeledEdge<Object>> g = objectDump.getHeap();
    for (Object o : g.vertexSet()) {
        for (LabeledEdge<Object> e: g.outgoingEdgesOf(o)) {
        	System.out.println("******");
        	System.out.println(o.toString());
        	System.out.println(e.getLabel());
        	System.out.println(e.getV2().toString());
        	System.out.println("******");
        }
    }
    
/*    assertFalse(objectDump.getObjectFieldExtensions().isEmpty());
    assertFalse(objectDump.getPrimitiveFieldExtensions().isEmpty());
    assertTrue(objectDump.getObjectFieldExtensions().containsKey("EMPTY_ELEMENTDATA"));
    assertTrue(objectDump.getPrimitiveFieldExtensions().containsKey("size"));*/
  }
}
