package randoop.util.fieldbasedcontrol;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Vector;

import org.junit.Test;

public class VectorsDifferentialTests {

  	@Test
  	public void testVectors1() throws IOException {
	    Vector v = new Vector();
	    v.addElement(3);
	    v.addElement(5);
	    v.addElement(7);
	
		FieldExtensions fe1 = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(v, fe1);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/vector.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/vectorext.txt");
	
	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true);
	    canonizer.canonizeAndEnlargeExtensions(v);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorextnew.txt");
	        
	    assertTrue(fe1.equals(fe2));
  	}    

    @Test
    public void testVectors2() throws IOException {
        Vector v = new Vector();
        v.addElement(3);
        v.addElement(5);
        v.addElement(7);

        Vector v2 = new Vector();
        v2.addElement(3);

        Vector [] va = new Vector [3];
        va[0] = v;
        va[1] = v;
        va[2] = v2;
    
		FieldExtensions fe1 = new FieldExtensions();
	    HeapDump objectDump = new HeapDump(va, fe1);
	    objectDump.heapToFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarr.dot");
	    objectDump.extensionsToFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrext.txt");   
	
	    FieldExtensions fe2 = new FieldExtensions();
	    HeapCanonizer canonizer = new HeapCanonizerHashMap(fe2, true);
	    canonizer.canonizeAndEnlargeExtensions(va);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrextnew.txt");
	    assertTrue(fe1.equals(fe2));
   
    }
	
}
