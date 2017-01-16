package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import randoop.util.fieldbasedcontrol.HeapDump;


public class DataStructuresDiferentialTests {

  
  @Test
  public void testLinkedListPrintGraph() throws IllegalArgumentException, IllegalAccessException, IOException {
	LinkedList l = new LinkedList();
	l.add(3);
	l.add(214);
	l.add("hola");
	l.add("chau");

	FieldExtensions fe = new FieldExtensions();	
    HeapDump objectDump = new HeapDump(l, fe);
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/llistgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/llistextensions.txt");
    
    Set<String> fbclasses = new LinkedHashSet<>();
    fbclasses.add("java.util.LinkedList");
    fbclasses.add("java.util.LinkedList.Node");
    
    //HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true, fbclasses);
    //HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true, fbclasses);
    /*
    HeapCanonizer canonizer = new HeapCanonizerMapStore(fe2, false);
    canonizer.canonizeAndEnlargeExtensions(l);
    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/llistextensionsnew.txt");
    */
    
    FieldExtensions fe1 = new FieldExtensions();
    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
    canonizer1.canonizeAndEnlargeExtensions(l);
    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/llistextensions1.txt");

	FieldExtensions fe2 = new FieldExtensions();
    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
    canonizer2.canonizeAndEnlargeExtensions(l);
    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/llistextensions2.txt");
    
    FieldExtensions fe3 = new FieldExtensions();
    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/llistextensions3.txt");
        
    assertTrue(fe1.equals(fe2));
    assertTrue(fe2.equals(fe3));
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
	
    FieldExtensions fe1 = new FieldExtensions();	
    HeapDump objectDump = new HeapDump(l, fe1);
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/arrlistgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/arrlistextensions.txt");
    
    FieldExtensions fe2 = new FieldExtensions();
    HeapCanonizer canonizer = new HeapCanonizerMapStore(fe2, false);
    canonizer.canonizeAndEnlargeExtensions(l);
    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/arrlistextensions2.txt");
    
    FieldExtensions fe3 = new FieldExtensions();
    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/arrlistextensions3.txt");
        
    //assertTrue(fe1.equals(fe2));
    assertTrue(fe2.equals(fe3));
  }
  
  @Test
  public void testTreeSetPrintGraph() throws IllegalArgumentException, IllegalAccessException, IOException {
	TreeSet l = new TreeSet();
	l.add("a");
	l.add("b");
	l.add("c");
	l.add("d");

    FieldExtensions fe1 = new FieldExtensions();	
//    HeapDump objectDump = new HeapDump(l, fe1);
//    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/tsetgraph.dot");
//    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/tsetextensions.txt");
//    
    FieldExtensions fe2 = new FieldExtensions();
    HeapCanonizer canonizer = new HeapCanonizerMapStore(fe2, false);
    canonizer.canonizeAndEnlargeExtensions(l);
    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/tsetextensions2.txt");
        
    System.out.println("Extensions1 size: " + fe1.size());
    System.out.println("Extensions2 size: " + fe2.size());
    
    FieldExtensions fe3 = new FieldExtensions();
    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/tsetextensions3.txt");
        
    // This reveals a bug with the old implementation. 
    // The problem is that jgrapht uses equals to decide whether to create a new node for an   
    // object in the graph, and hence there will be only one node in the graph for 
    // different objects o1 and o2 such that o1.equals(o2) (because of a bug in equals, for example).
    // This is clearly unwanted behaviour. Randoop is not good detecting this kind of behaviour.
//    assertFalse(fe1.equals(fe2));
    assertTrue(fe2.equals(fe3));
  }

  
  /*
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
  */
  
 /* 
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
*/
  /*
  @Test
  public void decimalFormatFields() throws IllegalArgumentException, IllegalAccessException {
	  for (Field f: java.text.DecimalFormat.class.getDeclaredFields()) {
			System.out.println(f.getName() + " " + Modifier.toString(f.getModifiers()));

	  }
  }*/
  
 
  
}
