package randoop.util.fieldExhaustiveControl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HeapDumpTest {

  @Test
  public void testDefaultDumpOfNullIsEmpty() {
    Object anObject = null;
    HeapDump objectDump = new HeapDump(anObject);
    assertTrue(objectDump.getObjectFieldExtensions().isEmpty());
    assertTrue(objectDump.getPrimitiveFieldExtensions().isEmpty());
  }

  @Test
  public void testDefaultDumpOfNonNullIsEmpty() {
    List<Integer> anObject = new ArrayList<Integer>();
    HeapDump objectDump = new HeapDump(anObject);
    assertTrue(objectDump.getObjectFieldExtensions().isEmpty());
    assertTrue(objectDump.getPrimitiveFieldExtensions().isEmpty());
  }

  @Test
  public void testDumpOfNullIsEmpty() {
    Object anObject = null;
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(anObject, maxDepth, maxArray, ignoredClasses);
    assertTrue(objectDump.getObjectFieldExtensions().isEmpty());
    assertTrue(objectDump.getPrimitiveFieldExtensions().isEmpty());
  }

  @Test
  public void testDumpOfNonNullIsNotEmpty() {
    List<Integer> anObject = new ArrayList<Integer>();
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(anObject, maxDepth, maxArray, ignoredClasses);
    assertFalse(objectDump.getObjectFieldExtensions().isEmpty());
    assertFalse(objectDump.getPrimitiveFieldExtensions().isEmpty());
    assertTrue(objectDump.getObjectFieldExtensions().containsKey("EMPTY_ELEMENTDATA"));
    assertTrue(objectDump.getPrimitiveFieldExtensions().containsKey("size"));
  }
}
