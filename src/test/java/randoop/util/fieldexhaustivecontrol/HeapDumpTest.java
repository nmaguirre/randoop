package randoop.util.fieldexhaustivecontrol;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import randoop.util.fieldexhaustivecontrol.HeapDump;

public class HeapDumpTest {

  @Test
  public void testDefaultDumpOfNullIsEmpty() throws IllegalArgumentException, IllegalAccessException {
    Object anObject = null;
    HeapDump objectDump = new HeapDump(anObject);
    assertTrue(objectDump.getObjectFieldExtensions().isEmpty());
    assertTrue(objectDump.getPrimitiveFieldExtensions().isEmpty());
  }

  @Test
  public void testDefaultDumpOfNonNullIsEmpty() throws IllegalArgumentException, IllegalAccessException {
    List<Integer> anObject = new ArrayList<Integer>();
    HeapDump objectDump = new HeapDump(anObject);
    assertTrue(objectDump.getObjectFieldExtensions().isEmpty());
    assertTrue(objectDump.getPrimitiveFieldExtensions().isEmpty());
  }

  @Test
  public void testDumpOfNullIsEmpty() throws IllegalArgumentException, IllegalAccessException {
    Object anObject = null;
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(anObject, maxDepth, maxArray, ignoredClasses, null);
    assertTrue(objectDump.getObjectFieldExtensions().isEmpty());
    assertTrue(objectDump.getPrimitiveFieldExtensions().isEmpty());
  }

  @Test
  public void testDumpOfNonNullIsNotEmpty() throws IllegalArgumentException, IllegalAccessException {
    List<Integer> anObject = new ArrayList<Integer>();
    int maxDepth = 1000;
    int maxArray = 1000;
    String[] ignoredClasses = {};
    HeapDump objectDump = new HeapDump(anObject, maxDepth, maxArray, ignoredClasses, null);
    assertFalse(objectDump.getObjectFieldExtensions().isEmpty());
    assertFalse(objectDump.getPrimitiveFieldExtensions().isEmpty());
    assertTrue(objectDump.getObjectFieldExtensions().containsKey("EMPTY_ELEMENTDATA"));
    assertTrue(objectDump.getPrimitiveFieldExtensions().containsKey("size"));
  }
}
