package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ArraysDifferentialTests {
	
	  @Test
	  public void testArrays1() throws IllegalArgumentException, IllegalAccessException, IOException {
		  
		String [] l = { "abc", "a", "abc" };
		
		FieldExtensionsStrings fe = new FieldExtensionsStrings();
	    HeapDump objectDump = new HeapDump(l, fe);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/strarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/strarrext.txt");
	
	    FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(l);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew1.txt");
	    
	    FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
	    canonizer2.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew2.txt");
	    
	    FieldExtensionsStrings fe3 = new FieldExtensionsStrings();
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew3.txt");
	        
	    assertTrue(fe1.equals(fe2));
	    assertTrue(fe2.equals(fe3));
	  }
	    
	    
	  @Test
	  public void testArrays2() throws IllegalArgumentException, IllegalAccessException, IOException {
		
		int [] l = { 2, 3, 3, 4, 3 };
		
		FieldExtensionsStrings fe = new FieldExtensionsStrings();
	    HeapDump objectDump = new HeapDump(l, fe);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/intarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext.txt");
	
	    FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(l);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext1.txt");

	    FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer = new HeapCanonizerMapStore(fe2, false);
	    canonizer.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext2.txt");
	        
	    FieldExtensionsStrings fe3 = new FieldExtensionsStrings();
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext3.txt");
	        
	    assertTrue(fe1.equals(fe2));
	    assertTrue(fe2.equals(fe3));
	  
	  }
	  
	  @Test
	  public void testArrays3() throws IllegalArgumentException, IllegalAccessException, IOException {
		 
		Integer [] l = { 2, 3, 3, 4, 3 };

		FieldExtensionsStrings fe = new FieldExtensionsStrings();
	    HeapDump objectDump = new HeapDump(l, fe);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext.txt");

	    FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(l);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext1.txt");

		FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
	    canonizer2.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext2.txt");
	    
	    FieldExtensionsStrings fe3 = new FieldExtensionsStrings();
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(fe3, false);
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext3.txt");
	        
	    assertTrue(fe1.equals(fe2));
	    assertTrue(fe2.equals(fe3));

	  }

}
