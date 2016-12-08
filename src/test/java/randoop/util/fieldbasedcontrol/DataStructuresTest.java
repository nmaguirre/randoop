package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.AttributedCharacterIterator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

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
    /*assertTrue(vertices.size() == 6);
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
   
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/arrlistgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/arrlistextensions.txt");
    
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
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/tsetgraph.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/tsetextensions.txt");
    
    
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

  /*
  @Test
  public void decimalFormatFields() throws IllegalArgumentException, IllegalAccessException {
	  for (Field f: java.text.DecimalFormat.class.getDeclaredFields()) {
			System.out.println(f.getName() + " " + Modifier.toString(f.getModifiers()));

	  }
  }*/
  
  @Test
  public void testArrays() throws IllegalArgumentException, IllegalAccessException, IOException {
	String [] l = { "abc", "a", "abc" };
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(l, maxDepth, maxArray, ignoredClasses, null);
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/strarr.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/strarrext.txt");

	int [] i = { 2, 3, 3, 4, 3 };
    objectDump = new HeapDump(i, maxDepth, maxArray, ignoredClasses, null);
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/intarr.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext.txt");

    
	Integer [] I = { 2, 3, 3, 4, 3 };
    objectDump = new HeapDump(I, maxDepth, maxArray, ignoredClasses, null);
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarr.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext.txt");
    


  } 
  
  @Test
  public void testVectors() throws IllegalArgumentException, IllegalAccessException, IOException {
	  
    Vector v = new Vector();
    v.addElement(3);
    v.addElement(5);
    v.addElement(7);

    Vector v2 = new Vector();
    v.addElement(3);
    
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(v, maxDepth, maxArray, ignoredClasses, null);
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/vector.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/vectorext.txt");
    
    Vector [] va = new Vector [3];
    va[0] = v;
    va[1] = v;
    va[2] = v2;
    
    objectDump = new HeapDump(va, maxDepth, maxArray, ignoredClasses, null);
    
    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarr.dot");
    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrext.txt");    

  }
  
  
  @Test
  public void testNumberFormat() throws IllegalArgumentException, IllegalAccessException, IOException {
	  NumberFormat numberFormat0 = NumberFormat.getCurrencyInstance();
	  AttributedCharacterIterator attributedCharacterIterator2 = numberFormat0.formatToCharacterIterator((java.lang.Object)10.0d);

	  int maxDepth = 100000;
	  int maxArray = 100000;/*
	  String[] ignoredClasses = {"AbstractMap", "AbstractCollection", "AbstractList", "HashMap", "Attribute"};
	  String [] ignoredFields = {"text", "capacityIncrement", "elementCount", "MAX_ARRAY_SIZE", "currentRunIndex", "relevantAttributes",
			  "currentRunLimit",
			  "currentRunStart",
			  "endIndex",
			  "currentIndex",
			  "beginIndex",
			  "runCount",
			  "runAttributeValues",
			  "ARRAY_SIZE_INCREMENT",
			  "runArraySize",
			  //"runAttributes",
			  "runStarts",
			  "EXPONENT_SIGN",
			  "DECIMAL_SEPARATOR",
			  "EXPONENT_SYMBOL",
			  "GROUPING_SEPARATOR",
			  "SIGN",
			  "PERCENT",
			  "FRACTION",
			  "INTEGER",
			  "EXPONENT",
			  "CURRENCY",
			  "PERMILLE"
	  }; //{"serialVersionUID"}; */ 
	  String[] ignoredClasses = {};
	  String [] ignoredFields = {};
	  
	  
	  
	  HeapDump objectDump = new HeapDump(attributedCharacterIterator2, maxDepth, maxArray, ignoredClasses, ignoredFields);
	  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/numberformatext.txt");
	  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/numberformat.dot");
	      
  }
  
  @Test
  public void testPlot() throws IllegalArgumentException, IllegalAccessException, IOException {

	  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
	  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
	  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
	  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
	  int i4 = 5;
	  org.jfree.chart.util.RectangleEdge rectangleEdge5 = combinedRangeXYPlot1.getDomainAxisEdge(5);
	  org.jfree.chart.JFreeChart jFreeChart6 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
	  int maxDepth = 100000;
	  int maxArray = 100000;
	  String[] ignoredClasses = {};
	  String [] ignoredFields = {};
	  
	  FieldExtensions ext = new FieldExtensions();
	  
	  HeapDump objectDump = new HeapDump(valueAxis0, ext);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  objectDump = new HeapDump(rectangleInsets2, ext);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  objectDump = new HeapDump(rectangleInsets2, ext);
	  objectDump = new HeapDump(rectangleEdge5, ext);
	  objectDump = new HeapDump(jFreeChart6, ext);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  
	  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/plotext.txt");
	  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/plot.dot");
	  
  }
  
  @Test
  public void testPlot2() throws IllegalArgumentException, IllegalAccessException, IOException {

	  FieldExtensions ext = new FieldExtensions();
	  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
	  HeapDump objectDump = new HeapDump(valueAxis0, ext);
	  
	  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  objectDump = new HeapDump(valueAxis0, ext);
	  
	  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
	  objectDump = new HeapDump(org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS, ext);
	  objectDump = new HeapDump(rectangleInsets2, ext);
	  
	  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  objectDump = new HeapDump(rectangleInsets2, ext);
	  
	  org.jfree.chart.util.RectangleEdge rectangleEdge5 = combinedRangeXYPlot1.getDomainAxisEdge(5);
	  objectDump = new HeapDump(5, ext);
	  objectDump = new HeapDump(rectangleEdge5, ext);
	  
	  org.jfree.chart.JFreeChart jFreeChart6 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
	  objectDump = new HeapDump(jFreeChart6, ext);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  
	  int maxDepth = 100000;
	  int maxArray = 100000;
	  String[] ignoredClasses = {};
	  String [] ignoredFields = {};
	  
	  

	  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/plot2ext.txt");
	  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/plot2.dot");
	  
  }
  
  
  @Test
  public void testChart() {
	  FieldExtensions ext = new FieldExtensions();

	  org.jfree.chart.axis.ValueAxis valueAxis0 = null;
	  HeapDump objectDump = new HeapDump(valueAxis0, ext);
	  org.jfree.chart.plot.CombinedRangeXYPlot combinedRangeXYPlot1 = new org.jfree.chart.plot.CombinedRangeXYPlot(valueAxis0);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  org.jfree.chart.util.RectangleInsets rectangleInsets2 = org.jfree.chart.axis.CategoryAxis.DEFAULT_AXIS_LABEL_INSETS;
	  combinedRangeXYPlot1.setAxisOffset(rectangleInsets2);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  combinedRangeXYPlot1.clearAnnotations();
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  int i5 = 0;
	  org.jfree.data.xy.XYSeries xYSeries6 = null;
	  org.jfree.data.xy.XYSeriesCollection xYSeriesCollection7 = new org.jfree.data.xy.XYSeriesCollection(xYSeries6);
	  objectDump = new HeapDump(xYSeriesCollection7, ext);
	  org.jfree.data.DomainOrder domainOrder8 = xYSeriesCollection7.getDomainOrder();
	  objectDump = new HeapDump(domainOrder8, ext);
	  combinedRangeXYPlot1.setDataset(0, (org.jfree.data.xy.XYDataset)xYSeriesCollection7);
	  objectDump = new HeapDump(xYSeriesCollection7, ext);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  java.awt.Stroke stroke10 = org.jfree.chart.plot.XYPlot.DEFAULT_GRIDLINE_STROKE;
	  objectDump = new HeapDump(stroke10, ext);
	  combinedRangeXYPlot1.setRangeMinorGridlineStroke(stroke10);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  org.jfree.chart.JFreeChart jFreeChart12 = new org.jfree.chart.JFreeChart((org.jfree.chart.plot.Plot)combinedRangeXYPlot1);
	  objectDump = new HeapDump(combinedRangeXYPlot1, ext);
	  objectDump = new HeapDump(jFreeChart12, ext);
	  
	  objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/chartext.txt");
	  objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/chart.dot");
	  
  }
  
  
  
}
