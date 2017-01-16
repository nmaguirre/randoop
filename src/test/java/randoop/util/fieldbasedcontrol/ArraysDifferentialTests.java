package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ArraysDifferentialTests {
	
	  @Test
	  public void testArrays1() throws IllegalArgumentException, IllegalAccessException, IOException {
		  
		String [] l = { "abc", "a", "abc" };
		
		FieldExtensions fe = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(l, fe);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/strarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/strarrext.txt");
	
	    FieldExtensions fe1 = new FieldExtensions();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(l);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew1.txt");
	    
	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
	    canonizer2.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew2.txt");
	    
	    FieldExtensions fe3 = new FieldExtensions();
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew3.txt");
	        
	    assertTrue(fe1.equals(fe2));
	    assertTrue(fe2.equals(fe3));
	  }
	    
	    
	  @Test
	  public void testArrays2() throws IllegalArgumentException, IllegalAccessException, IOException {
		
		int [] l = { 2, 3, 3, 4, 3 };
		
		FieldExtensions fe = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(l, fe);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/intarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext.txt");
	
	    FieldExtensions fe1 = new FieldExtensions();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(l);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext1.txt");

	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizerMapStore(fe2, false);
	    canonizer.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext2.txt");
	        
	    FieldExtensions fe3 = new FieldExtensions();
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext3.txt");
	        
	    assertTrue(fe1.equals(fe2));
	    assertTrue(fe2.equals(fe3));
	  
	  }
	  
	  @Test
	  public void testArrays3() throws IllegalArgumentException, IllegalAccessException, IOException {
		 
		Integer [] l = { 2, 3, 3, 4, 3 };

		FieldExtensions fe = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(l, fe);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext.txt");

	    FieldExtensions fe1 = new FieldExtensions();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(l);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext1.txt");

		FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
	    canonizer2.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext2.txt");
	    
	    FieldExtensions fe3 = new FieldExtensions();
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext3.txt");
	        
	    assertTrue(fe1.equals(fe2));
	    assertTrue(fe2.equals(fe3));

	  }

}
