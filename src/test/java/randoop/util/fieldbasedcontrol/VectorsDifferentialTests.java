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
	
	    FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(v);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorext1.txt");

		FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
	    canonizer2.canonizeAndEnlargeExtensions(v);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorext2.txt");;
	        
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false);
	    canonizer3.activateReadableExtensions();
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(v);
	    FieldExtensionsStrings fe3 = canonizer3.getReadableExtensions();
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorext3.txt");
	    FieldExtensionsIndexes fe4 = canonizer3.getExtensions();
	    fe4.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorext4.txt");
	        
	    assertTrue(fe3.equals(fe4.toFieldExtensionsStrings()));
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
    
	    FieldExtensionsStrings fe1 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer1 = new HeapCanonizerListStore(fe1, false);
	    canonizer1.canonizeAndEnlargeExtensions(va);
	    fe1.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrext1.txt");

		FieldExtensionsStrings fe2 = new FieldExtensionsStrings();
	    HeapCanonizer canonizer2 = new HeapCanonizerMapStore(fe2, false);
	    canonizer2.canonizeAndEnlargeExtensions(va);
	    fe2.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrext2.txt");
	    
	    HeapCanonizerRuntimeEfficient canonizer3 = new HeapCanonizerRuntimeEfficient(false);
	    canonizer3.activateReadableExtensions();
	    canonizer3.traverseBreadthFirstAndEnlargeExtensions(va);
	    FieldExtensionsStrings fe3 = canonizer3.getReadableExtensions();
	    fe3.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrext3.txt");
	    FieldExtensionsIndexes fe4 = canonizer3.getExtensions();
	    fe4.toFile("src/test/java/randoop/util/fieldbasedcontrol/vectorarrext4.txt");
	        
	    assertTrue(fe3.equals(fe4.toFieldExtensionsStrings()));
   
    }
	
}
