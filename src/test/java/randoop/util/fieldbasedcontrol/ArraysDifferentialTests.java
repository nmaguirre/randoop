package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

public class ArraysDifferentialTests {
	
	  @Test
	  public void testArrays1() throws IllegalArgumentException, IllegalAccessException, IOException {
		String [] l = { "abc", "a", "abc" };
		FieldExtensions fe1 = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(l, fe1);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/strarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/strarrext.txt");
	
	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true);
	    canonizer.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/strarrextnew.txt");
	        
	    assertTrue(fe1.equals(fe2));
	  }
	    
	    
	  @Test
	  public void testArrays2() throws IllegalArgumentException, IllegalAccessException, IOException {
		int [] l = { 2, 3, 3, 4, 3 };
		FieldExtensions fe1 = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(l, fe1);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/intarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/intarrext.txt");
	
	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true);
	    canonizer.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/intarrextnew.txt");
	        
	    assertTrue(fe1.equals(fe2));
	  }
	  
	  @Test
	  public void testArrays3() throws IllegalArgumentException, IllegalAccessException, IOException {
		Integer [] l = { 2, 3, 3, 4, 3 };
		FieldExtensions fe1 = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(l, fe1);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrext.txt");
	
	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true);
	    canonizer.canonizeAndEnlargeExtensions(l);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/Intarrextnew.txt");
	        
	    assertTrue(fe1.equals(fe2));
	  }

}
