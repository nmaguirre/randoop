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
	    
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false);
	    canonizer3.activateReadableExtensions();
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    FieldExtensionsStrings fe3 = canonizer3.getReadableExtensions();
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew3.txt");
	    FieldExtensionsIndexes fe4 = canonizer3.getExtensions();
	    fe4.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew4.txt");
	        
	    assertTrue(fe3.equals(fe4.toFieldExtensionsStrings()));
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
	        
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false);
	    canonizer3.activateReadableExtensions();
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    FieldExtensionsStrings fe3 = canonizer3.getReadableExtensions();
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext3.txt");
	    FieldExtensionsIndexes fe4 = canonizer3.getExtensions();
	    fe4.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext4.txt");
	        
	    assertTrue(fe3.equals(fe4.toFieldExtensionsStrings()));
	  
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
	    
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false);
	    canonizer3.activateReadableExtensions();
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(l);
	    FieldExtensionsStrings fe3 = canonizer3.getReadableExtensions();
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext3.txt");
	    FieldExtensionsIndexes fe4 = canonizer3.getExtensions();
	    fe4.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext4.txt");
	        
	    assertTrue(fe3.equals(fe4.toFieldExtensionsStrings()));

	  }

}
