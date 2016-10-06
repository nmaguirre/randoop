package randoop.util.fieldexhaustivecontrol;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

public class FieldExtensionsTest {

	/*
  @Test
  public void testListSubsumption() {
    FieldExtensions extensions = new FieldExtensions();
    LinkedList<Integer> listA = new LinkedList<Integer>();
    LinkedList<Integer> listB = new LinkedList<Integer>();
    listA.add(1);
    listB.add(1);
    assertTrue(extensions.add(listA));
    assertFalse(extensions.add(listB));
  }

  @Test
  public void testListNoSubsumption() {
    FieldExtensions extensions = new FieldExtensions();
    LinkedList<Integer> listA = new LinkedList<Integer>();
    LinkedList<Integer> listB = new LinkedList<Integer>();
    LinkedList<Integer> listC = new LinkedList<Integer>();
    LinkedList<Integer> listD = new LinkedList<Integer>();
    listA.add(1);
    listA.add(1);

    listB.add(2);
    listB.add(2);

    listC.add(1);
    listC.add(2);

    listD.add(2);
    listD.add(1);

    assertTrue(extensions.add(listA));
    assertTrue(extensions.add(listB));

    assertFalse(extensions.add(listC));
    assertFalse(extensions.add(listD));
  }

  @Test
  public void testTreeSubsumption() {
    FieldExtensions extensions = new FieldExtensions();
    TreeSet<Integer> treeA = new TreeSet<Integer>();
    TreeSet<Integer> treeB = new TreeSet<Integer>();
    treeA.add(1);
    treeB.add(1);
    assertTrue(extensions.add(treeA));
    assertFalse(extensions.add(treeB));
  }

  @Test
  public void testTreeNoSubsumption() {
    FieldExtensions extensions = new FieldExtensions();
    TreeSet<Integer> treeA = new TreeSet<Integer>();
    TreeSet<Integer> treeB = new TreeSet<Integer>();
    TreeSet<Integer> treeC = new TreeSet<Integer>();
    TreeSet<Integer> treeD = new TreeSet<Integer>();

    treeA.add(1);
    treeA.add(2);
    treeA.add(3);
    treeA.add(4);

    treeB.add(3);
    treeB.add(2);
    treeB.add(1);

    treeC.add(2);
    treeC.add(3);
    treeC.add(4);

    treeD.add(4);
    treeD.add(3);
    treeD.add(2);
    treeD.add(1);

    assertTrue(extensions.add(treeA));
    assertTrue(extensions.add(treeB));

    assertTrue(extensions.add(treeC));
    assertTrue(extensions.add(treeD));
  }

  @Test
  public void testTreePotentialBug() {
    FieldExtensions extensions = new FieldExtensions();
    TreeSet<Integer> treeA = new TreeSet<Integer>();
    assertTrue(extensions.add(treeA));
    treeA.remove(3);
    assertFalse(extensions.add(treeA));
    treeA.remove(4);
    assertFalse(extensions.add(treeA));
    TreeSet<Integer> treeB = new TreeSet<Integer>();
    assertFalse(extensions.add(treeB));
    treeA.remove(3);
    assertFalse(extensions.add(treeB));
    treeA.remove(4);
    assertFalse(extensions.add(treeB));
  }
  */
}
