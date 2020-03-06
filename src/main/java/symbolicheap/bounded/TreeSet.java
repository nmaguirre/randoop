package symbolicheap.bounded;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import randoop.CheckRep;
import randoop.util.heapcanonicalization.DummySymbolicTSet;


public class TreeSet {

  // -------------------------------- LI infrastructure begin - do not
  // remove----------------------------//

	/*
  public void dumpStructure(TreeSet root) {
  }

  public void countStructure(TreeSet root) {
  }

  public boolean repOK(TreeSet root, int size) {
    return true;
  }

  */
	
// public TreeSet() {
//	 
// }
	

  public TreeSet(int aKey) {
      init_TreeSet(this, aKey, null);
      //assert repOK_Concrete(this);
  }

 // public static final TreeSet SYMBOLICTREESET = new TreeSet(); // field added to execute the
                                                               // hybridRepOK.
  //public static final int SYMBOLICINT = (int) Integer.MIN_VALUE; // field added to execute the
                                                                 // hybridRepOK.

  /*
  // Methods for instances generation
  private static void genPositiveVectors() {
  }

  private static void genNegativeVectors() {
  }
  private static int MAX_SCOPE = 9;
  */


  private boolean structuralHybridRepOK() {
    Set<TreeSet> visited = new HashSet<TreeSet>();
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    if (!(this instanceof DummySymbolicTSet)) {
      visited.add(this);

      worklist.add(this);

      if (this.parent != null && !(parent instanceof DummySymbolicTSet))
        return false;

      while (!worklist.isEmpty()) {
        TreeSet node = worklist.remove(0);

        TreeSet left = node.left;
        if (left != null && !(left instanceof DummySymbolicTSet)) {
          if (!visited.add(left)) {
            return false;
          }

          if (!(left.parent instanceof DummySymbolicTSet) && left.parent != node) {
            return false;
          }
          worklist.add(left);

        }

        TreeSet right = node.right;
        if (right != null && !(right instanceof DummySymbolicTSet)) {
          if (!visited.add(right)) {
            return false;
          }

          if (!(right.parent instanceof DummySymbolicTSet) && right.parent != node) {
            return false;
          }
          worklist.add(right);
        }

      }
      return visited.size() <= LIMIT && this.hybridRepOK_colors(this);
    } else {
      return true;
    }
  }

  private boolean hybridRepOK_colors(TreeSet root) {
    if (!(this instanceof DummySymbolicTSet) && root.color != -1) {
      if (root.color() != BLACK)
        return false;
      List<TreeSet> worklist = new ArrayList<TreeSet>();
      worklist.add(root);
      while (!worklist.isEmpty()) {
        TreeSet current = worklist.remove(0);
        TreeSet cl = current.left();
        TreeSet cr = current.right();
        if (current.color() == RED) {
          if (cl != null && cl.color() == RED) {
            return false;
          }
          if (cr != null && cr.color() == RED) {
            return false;
          }
        }
        if (cl != null && !(cl instanceof DummySymbolicTSet)) {
          worklist.add(cl);
        }
        if (cr != null && !(cr instanceof DummySymbolicTSet)) {
          worklist.add(cr);
        }
      }
      int numberOfBlack = -1;
      List<Pair<TreeSet, Integer>> worklist2 = new ArrayList<TreeSet.Pair<TreeSet, Integer>>();

      worklist2.add(new Pair<TreeSet, Integer>(root, 0)); // assumes root is not symbolic, which was
                                                          // checked above
      // root.dumpStructure(root);
      while (!worklist2.isEmpty()) {
        // System.out.println("->0");
        Pair<TreeSet, Integer> p = worklist2.remove(0);
        TreeSet e = p.first();
        int n = p.second();
        if (e != null && e.color() == BLACK) {
          n++;
          // System.out.println("->1 n=" + n);
        }
        if (e == null) {
          // System.out.println("->2");
          if (numberOfBlack == -1) {
            numberOfBlack = n;
            // System.out.println("->3 n=" + n);
          } else if (numberOfBlack != n) {
            // System.out.println("REPOK FAIL n = " + n + " noBlk = " + numberOfBlack);
            return false;
          }
        } else {
          // System.out.println("->4");
          if (!(e.left instanceof DummySymbolicTSet))
            worklist2.add(new Pair<TreeSet, Integer>(e.left(), n));
          if (!(e.right instanceof DummySymbolicTSet))
            worklist2.add(new Pair<TreeSet, Integer>(e.right(), n));
        }
      }
      return true;
    } else
      return true;
  }

  private class Pair<T, U> {
    private T a;
    private U b;

    public Pair(T a, U b) {
      this.a = a;
      this.b = b;
    }

     T first() {
      return a;
    }

     U second() {
      return b;
    }
  }

  // -------------------------------- LI infrastructure end - do not
  // remove----------------------------//

  private static final int RED = 0;

  private static final int BLACK = 1;

  private int key;

  private int color;

  private TreeSet left;

  private TreeSet right;

  private TreeSet parent;

  /*
  public TreeSet(int key, int color) {
	  if (color != 0 || color != 1)
		  throw new RuntimeException("");
		   
    this.key = key;
    this.color = color;
    // this.color = Verify.random(1);
    // System.out.println("I AM HERE " + this.color);
    left = right = parent = null;
  }
  */

  private TreeSet left() {
    return left;
  }

  private TreeSet right() {
    return right;
  }

  private int color() {
    if (color == RED) {
      color = RED;
    } else {
      color = BLACK;
    }
    return color;
  }

  // ----------------------------- contains-begin-----------------------------------//

  private boolean contains(TreeSet root, int key) {
    TreeSet p = root;
    while (p != null) {
      // p.dumpStructure(p);
      // Verify.ignoreIf(!repOK(root, LIMIT));
      if (key == p.key) {
        return true;
      } else if (key < p.key) {
        p = p.left;
      } else {
        p = p.right;
      }
    }
    return false;
  }

  // ----------------------------- contains-end-----------------------------------//

  // ------------------------------add-roops-begin-------------------------------------//
//  public TreeSet add(int aKey) {
//    TreeSet t = this;
//
//    /*
//    if (t == null) {
//      init_TreeSet(this, aKey, null);
//
//      return this;
//    }
//    */
//
//    boolean boolean_true = true;
//    while (boolean_true) {
//
//      if (aKey == t.key) {
//
//        return this;
//      } else if (aKey < t.key) {
//
//        if (t.left != null) {
//
//          t = t.left;
//        } else {
//
//          t.left = new TreeSet(aKey);
//          init_TreeSet(t.left, aKey, t);
//
//          return fixAfterInsertion(t.left, this);
//        }
//      } else { // cmp > 0
//
//        if (t.right != null) {
//
//          t = t.right;
//        } else {
//
//          t.right = new TreeSet(aKey);
//          init_TreeSet(t.right, aKey, t);
//          return fixAfterInsertion(t.right, this);
//        }
//      }
//    }
//
//    return this;
//  }
  
  
  
 public TreeSet add(int aKey) {
	 return add(aKey, this);
 }
  

 private TreeSet add(int aKey, TreeSet root) {
    TreeSet t = root;

    if (t == null) {
      init_TreeSet(root, aKey, null);
      return root;
    }

    boolean boolean_true = true;
    while (boolean_true) {

      if (aKey == t.key) {
        return root;
      } else if (aKey < t.key) {

        if (t.left != null) {

          t = t.left;
        } else {

          t.left = new TreeSet(aKey);
          init_TreeSet(t.left, aKey, t);

          root = fixAfterInsertion(t.left, root);
          return root;
        }
      } else { // cmp > 0

        if (t.right != null) {

          t = t.right;
        } else {

          t.right = new TreeSet(aKey);
          init_TreeSet(t.right, aKey, t);
          root = fixAfterInsertion(t.right, root);
          return root;
        }
      }
    }

    return root;
  }
 

  private void init_TreeSet(TreeSet entry, int new_key, TreeSet new_parent) {
    entry.color = BLACK;//RED; //
    entry.left = null;
    entry.right = null;
    entry.key = new_key;
    entry.parent = new_parent;
  }

  /**
   * Balancing operations.
   *
   * Implementations of rebalancings during insertion and deletion are slightly different than the
   * CLR version. Rather than using dummy nilnodes, we use a set of accessors that deal properly
   * with null. They are used to avoid messiness surrounding nullness checks in the main algorithms.
   */

  private static int colorOf(TreeSet p) {
    int result;
    if (p == null)
      result = BLACK;
    else
      result = p.color;
    return result;
  }

  private static TreeSet parentOf(TreeSet p) {
    TreeSet result;
    if (p == null)
      result = null;
    else
      result = p.parent;

    return result;
  }

  private static void setColor(TreeSet p, int c) {
    if (p != null)
      p.color = c;
  }

  private static TreeSet leftOf(TreeSet p) {
    TreeSet result;
    if (p == null)
      result = null;
    else
      result = p.left;
    return result;
  }

  private static TreeSet rightOf(TreeSet p) {
    TreeSet result;
    if (p == null)
      result = null;
    else
      result = p.right;
    return result;
  }

  /** From CLR **/

  // invariant: must be called as root.rotate(node,root)
  private TreeSet rotateLeft_add(TreeSet p, TreeSet root) {
    TreeSet r = p.right;
    p.right = r.left;
    if (r.left != null)
      r.left.parent = p;
    r.parent = p.parent;
    if (p.parent == null)
      root = r;
    else if (p.parent.left == p)
      p.parent.left = r;
    else
      p.parent.right = r;
    r.left = p;
    p.parent = r;
    return root;
  }

  /** From CLR **/
  // invariant: must be called as root.rotate(node,root)
  private TreeSet rotateRight_add(TreeSet p, TreeSet root) {
    TreeSet l = p.left;
    p.left = l.right;
    if (l.right != null)
      l.right.parent = p;
    l.parent = p.parent;
    if (p.parent == null)
      root = l;
    else if (p.parent.right == p)
      p.parent.right = l;
    else
      p.parent.left = l;
    l.right = p;
    p.parent = l;
    return root;
  }

  // invariant: must be called as root.fix(node,root)
  private TreeSet fixAfterInsertion(final TreeSet entry, TreeSet root) {
    TreeSet x = entry;
    x.color = RED;

    while (x != null && x != root && x.parent.color == RED) {

      if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {

        TreeSet y = rightOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {

          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {

          if (x == rightOf(parentOf(x))) {

            x = parentOf(x);
            root = rotateLeft_add(x, root);
          } else {
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) {
            root = rotateRight_add(parentOf(parentOf(x)), root);
          } else {
          }
        }
      } else {

        TreeSet y = leftOf(parentOf(parentOf(x)));
        if (colorOf(y) == RED) {

          setColor(parentOf(x), BLACK);
          setColor(y, BLACK);
          setColor(parentOf(parentOf(x)), RED);
          x = parentOf(parentOf(x));
        } else {

          if (x == leftOf(parentOf(x))) {

            x = parentOf(x);
            root = rotateRight_add(x, root);
          } else {
          }
          setColor(parentOf(x), BLACK);
          setColor(parentOf(parentOf(x)), RED);
          if (parentOf(parentOf(x)) != null) {
            root = rotateLeft_add(parentOf(parentOf(x)), root);
          } else {
          }

        }
      }
    }
    root.color = BLACK;
    return root;
  }

  // ----------------------------------------add-roops-end-----------------------------------------//

  // --------------------------------------remove-begin-------------------------------------//

  
  public TreeSet remove(int aKey) {
	  return remove(aKey, this);
  }
  
  private TreeSet remove(final int myKey, TreeSet root) {
    TreeSet p = this.getEntry(myKey);
    //TreeSet root = this;

    if (p != null)
      root = this.deleteEntry(p, root);
    return root;
  }

  private TreeSet getEntry(int myKey) {
    TreeSet found = null;

    TreeSet p;
    p = this;
    while (p != null && found == null) {
      if (myKey < p.key) {
        p = p.left;
      } else {
        if (myKey > p.key) {
          p = p.right;
        } else
          found = p;
      }
    }
    return found;
  }

  private TreeSet getEntry_remove(int key, TreeSet root) {
    TreeSet p = root;
    while (p != null) {

      if (key == p.key) {

        return p;
      } else if (key < p.key) {

        p = p.left;
      } else {

        p = p.right;
      }
    }
    return null;
  }

  private TreeSet deleteEntry(TreeSet p, TreeSet root) {
    // decrementSize();

    // If strictly internal, copy successor's element to p and then make p
    // point to successor.
    if (p.left != null && p.right != null) {

      TreeSet s = root.successor(p);
      p.key = s.key;

      p = s;
    } // p has 2 children

    // Start fixup at replacement node, if it exists.
    TreeSet replacement;
    if (p.left != null)
      replacement = p.left;
    else
      replacement = p.right;

    if (replacement != null) {

      // Link replacement to parent
      replacement.parent = p.parent;
      if (p.parent == null) {
        root = replacement;
      } else if (p == p.parent.left) {

        p.parent.left = replacement;
      } else {

        p.parent.right = replacement;
      }

      // Null out links so they are OK to use by fixAfterDeletion.
      p.left = p.right = p.parent = null;

      // Fix replacement
      if (p.color == BLACK) {

        root = fixAfterDeletion(replacement, root);
      }
    } else if (p.parent == null) { // return if we are the only node.

      root = null;
    } else { // No children. Use self as phantom replacement and unlink.
      if (p.color == BLACK) {

        root = fixAfterDeletion(p, root);
      }

      if (p.parent != null) {

        if (p == p.parent.left) {

          p.parent.left = null;
        } else if (p == p.parent.right) {

          p.parent.right = null;
        }

        p.parent = null;
      }
    }
    return root;
  }

  private TreeSet successor(TreeSet t) {
    if (t == null) {

      return null;
    } else if (t.right != null) {
      TreeSet p = t.right;
      while (p.left != null) {
        p = p.left;
      }

      return p;
    } else {
      TreeSet p = t.parent;
      TreeSet ch = t;
      while (p != null && ch == p.right) {
        ch = p;
        p = p.parent;
      }
      return p;
    }
  }

  private TreeSet fixAfterDeletion(TreeSet entry, TreeSet root) {
    TreeSet x = entry;

    while (x != root && colorOf(x) == BLACK) {

      if (x == leftOf(parentOf(x))) {

        TreeSet sib = rightOf(parentOf(x));

        if (colorOf(sib) == RED) {

          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          root = rotateLeft_remove(parentOf(x), root);
          sib = rightOf(parentOf(x));
        }

        if (colorOf(leftOf(sib)) == BLACK && colorOf(rightOf(sib)) == BLACK) {

          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(rightOf(sib)) == BLACK) {

            setColor(leftOf(sib), BLACK);
            setColor(sib, RED);
            root.rotateRight_remove(sib, root);
            sib = rightOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(rightOf(sib), BLACK);
          root = rotateLeft_remove(parentOf(x), root);
          x = root;
        }
      } else { // symmetric
        TreeSet sib = leftOf(parentOf(x));

        if (colorOf(sib) == RED) {

          setColor(sib, BLACK);
          setColor(parentOf(x), RED);
          root = rotateRight_remove(parentOf(x), root);
          sib = leftOf(parentOf(x));
        }

        if (colorOf(rightOf(sib)) == BLACK && colorOf(leftOf(sib)) == BLACK) {

          setColor(sib, RED);
          x = parentOf(x);
        } else {
          if (colorOf(leftOf(sib)) == BLACK) {

            setColor(rightOf(sib), BLACK);
            setColor(sib, RED);
            root = rotateLeft_remove(sib, root);
            sib = leftOf(parentOf(x));
          }
          setColor(sib, colorOf(parentOf(x)));
          setColor(parentOf(x), BLACK);
          setColor(leftOf(sib), BLACK);
          root = rotateRight_remove(parentOf(x), root);
          x = root;
        }
      }
    }

    setColor(x, BLACK);
    return root;

  }

  private TreeSet rotateRight_remove(TreeSet z, TreeSet root) {
    TreeSet y = z.left;
    z.left = y.right;
    if (y.right != null)
      y.right.parent = z;
    y.parent = z.parent;
    if (z.parent == null)
      root = y;
    else if (z == z.parent.right)
      z.parent.right = y;
    else
      z.parent.left = y;
    y.right = z;
    z.parent = y;

    return root;
  }

  private TreeSet rotateLeft_remove(TreeSet z, TreeSet root) {
    TreeSet y = z.right;
    z.right = y.left;
    if (y.left != null)
      y.left.parent = z;
    y.parent = z.parent;
    if (z.parent == null)
      root = y;
    else if (z == z.parent.left)
      z.parent.left = y;
    else
      z.parent.right = y;
    y.left = z;
    z.parent = y;

    return root;
  }

  // --------------------------------------remove-end-------------------------------------//

  // --------------------------------------bfsTraverse-begin-------------------------------------//

  /*
  public void bfsTraverse(TreeSet root) {
    Set<TreeSet> visited = new HashSet<TreeSet>();
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    visited.add(root);
    worklist.add(root);
    while (!worklist.isEmpty() && visited.size() <= LIMIT) {
      TreeSet node = worklist.remove(0);
      TreeSet left = node.left;
      if (left != null && visited.add(left)) {

        worklist.add(left);
      }
      TreeSet right = node.right;
      if (right != null && visited.add(right)) {
        worklist.add(right);
      }
    }
  }

  // --------------------------------------bfsTraverse-end-------------------------------------//

  // --------------------------------------dfsTraverse-begin-------------------------------------//

  public void dfsTraverse(TreeSet root) {
    HashSet<TreeSet> visited = new HashSet<TreeSet>();
    dfsTraverseAux(root, visited);
  }

  private void dfsTraverseAux(TreeSet root, HashSet<TreeSet> visited) {
    if (root != null && visited.add(root)) {

      if (root.left != null) {
        dfsTraverseAux(root.left, visited);
      }
      if (root.right != null) {
        dfsTraverseAux(root.right, visited);
      }

    }
  }
  */

  // --------------------------------------dfsTraverse-end-------------------------------------//

  // ------------------------- repOK_Concrete begin -----------------------------------//

  private boolean repOK() {
	  return repOK_Concrete(this);
  }
  
  private boolean repOK_Concrete(TreeSet root) {
    return repOK_Structure(root) && repOK_Colors(root)/* && repOK_KeysAndValues(root) */;
  }

  private boolean repOK_Structure(TreeSet root) {
    Set<TreeSet> visited = new HashSet<TreeSet>();
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    visited.add(root);
    worklist.add(root);
    if (root.parent != null)
      return false;

    while (!worklist.isEmpty()) {
      TreeSet node = worklist.remove(0);
      TreeSet left = node.left();
      if (left != null) {
        if (!visited.add(left)) {
          return false;
        }
        if (left.parent != node) {
          return false;
        }
        worklist.add(left);
      }
      TreeSet right = node.right();
      if (right != null) {
        if (!visited.add(right)) {
          return false;
        }
        if (right.parent != node) {
          return false;
        }
        worklist.add(right);
      }
    }
    return visited.size() <= LIMIT;
  }

  private boolean repOK_Colors(TreeSet root) {
    if (root.color() != BLACK)
      return false;
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    worklist.add(root);
    while (!worklist.isEmpty()) {
      TreeSet current = worklist.remove(0);
      TreeSet cl = current.left();
      TreeSet cr = current.right();
      if (current.color() == RED) {
        if (cl != null && cl.color() == RED) {
          return false;
        }
        if (cr != null && cr.color() == RED) {
          return false;
        }
      }
      if (cl != null) {
        worklist.add(cl);
      }
      if (cr != null) {
        worklist.add(cr);
      }
    }
    int numberOfBlack = -1;
    List<Pair<TreeSet, Integer>> worklist2 = new ArrayList<TreeSet.Pair<TreeSet, Integer>>();
    worklist2.add(new Pair<TreeSet, Integer>(root, 0));
    // root.dumpStructure(root);
    while (!worklist2.isEmpty()) {
      // System.out.println("->0");
      Pair<TreeSet, Integer> p = worklist2.remove(0);
      TreeSet e = p.first();
      int n = p.second();
      if (e != null && e.color() == BLACK) {
        n++;
      }
      if (e == null) {
        if (numberOfBlack == -1) {
          numberOfBlack = n;
        } else if (numberOfBlack != n) {
          return false;
        }
      } else {
        worklist2.add(new Pair<TreeSet, Integer>(e.left(), n));
        worklist2.add(new Pair<TreeSet, Integer>(e.right(), n));
      }
    }
    return true;
  }

  private boolean repOK_KeysAndValues(TreeSet root) {
    int min = repOK_findMin(root);
    int max = repOK_findMax(root);
    if (!repOK_orderedKeys(root, min - 1, max + 1)) {
      return false;
    }
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    worklist.add(root);
    while (!worklist.isEmpty()) {
      TreeSet current = worklist.remove(0);
      if (current.left() != null) {
        worklist.add(current.left());
      }
      if (current.right() != null) {
        worklist.add(current.right());
      }
    }
    return true;
  }

  private int repOK_findMin(TreeSet root) {
    TreeSet curr = root;
    while (curr.left() != null) {
      curr = curr.left();
    }
    return curr.key;
  }

  private int repOK_findMax(TreeSet root) {
    TreeSet curr = root;
    while (curr.right() != null) {
      curr = curr.right();
    }
    return curr.key;
  }

  private boolean repOK_orderedKeys(TreeSet e, int min, int max) {
    if ((e.key <= min) || (e.key >= max)) {
      return false;
    }
    if (e.left() != null) {
      if (!repOK_orderedKeys(e.left(), min, e.key)) {
        return false;
      }
    }
    if (e.right() != null) {
      if (!repOK_orderedKeys(e.right(), e.key, max)) {
        return false;
      }
    }
    return true;
  }

  // ------------------------- repOK_Concrete end -----------------------------------//

  // ------------------------- repOK_Concrete-Post begin -----------------------------------//

  private boolean repOK_ConcretePost(TreeSet root) {
    return repOK_StructurePost(root)
        && repOK_ColorsPost(root) /* && repOK_KeysAndValuesPost(root) */;
  }

  private boolean repOK_StructurePost(TreeSet root) {
    Set<TreeSet> visited = new HashSet<TreeSet>();
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    visited.add(root);
    worklist.add(root);
    if (root.parent != null)
      return false;

    while (!worklist.isEmpty()) {
      TreeSet node = worklist.remove(0);
      TreeSet left = node.left();
      if (left != null) {
        if (!visited.add(left)) {

          return false;

        }
        if (left.parent != node) {

          return false;
        }
        worklist.add(left);
      }
      TreeSet right = node.right();
      if (right != null) {
        if (!visited.add(right)) {

          return false;
        }
        if (right.parent != node) {

          return false;
        }
        worklist.add(right);
      }
    }
    return true;
  }

  private boolean repOK_ColorsPost(TreeSet root) {
    if (root.color() != BLACK)
      return false;
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    worklist.add(root);
    while (!worklist.isEmpty()) {
      TreeSet current = worklist.remove(0);
      TreeSet cl = current.left();
      TreeSet cr = current.right();
      if (current.color() == RED) {
        if (cl != null && cl.color() == RED) {
          return false;
        }
        if (cr != null && cr.color() == RED) {
          return false;
        }
      }
      if (cl != null) {
        worklist.add(cl);
      }
      if (cr != null) {
        worklist.add(cr);
      }
    }
    int numberOfBlack = -1;
    List<Pair<TreeSet, Integer>> worklist2 = new ArrayList<TreeSet.Pair<TreeSet, Integer>>();
    worklist2.add(new Pair<TreeSet, Integer>(root, 0));
    while (!worklist2.isEmpty()) {
      Pair<TreeSet, Integer> p = worklist2.remove(0);
      TreeSet e = p.first();
      int n = p.second();
      if (e != null && e.color() == BLACK) {
        n++;
      }
      if (e == null) {
        if (numberOfBlack == -1) {
          numberOfBlack = n;
        } else if (numberOfBlack != n) {
          return false;
        }
      } else {
        worklist2.add(new Pair<TreeSet, Integer>(e.left(), n));
        worklist2.add(new Pair<TreeSet, Integer>(e.right(), n));
      }
    }
    return true;
  }

  private boolean repOK_KeysAndValuesPost(TreeSet root) {
    int min = repOK_findMin(root);
    int max = repOK_findMax(root);
    if (!repOK_orderedKeys(root, min - 1, max + 1)) {
      return false;
    }
    List<TreeSet> worklist = new ArrayList<TreeSet>();
    worklist.add(root);
    while (!worklist.isEmpty()) {
      TreeSet current = worklist.remove(0);
      if (current.left() != null) {
        worklist.add(current.left());
      }
      if (current.right() != null) {
        worklist.add(current.right());
      }
    }
    return true;
  }

  // ------------------------- repOK_Concrete-Post end -----------------------------------//

  // marked in tables as bfsTraverse
  private static int LIMIT = 30;

  /*
   * public static void main(String[] arcs) { TreeSet X = new TreeSet(LIMIT, BLACK); X = (TreeSet)
   * Debug.makeSymbolicRefBounded("N", X); if (X != null) { if (X.repOK_Concrete(X)) {
   * X.genPositiveVectors(); } else { X.genNegativeVectors(); } } }
   */

  /*
  public static void main(String[] args) {
	  TreeSet X = new TreeSet(LIMIT, BLACK);
      X = (TreeSet) Debug.makeSymbolicRef("X", X);
      //                X = (TreeSet) Debug.makeSymbolicRefBounded("X", X);

        try {
              int k = 1;
              X = X.add(k, X);
              X.countStructure(X);
        } catch(Exception e) {
                // ignored!
        }
  }
  */

}
