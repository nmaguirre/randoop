package symbolicheap.bounded;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class AvlTree {


  // Private members

  private int element;
  private int height;
  private AvlTree left;
  private AvlTree right;

  // Constructors

  public AvlTree(int element) {
    this.element = element;
    this.height = 1;
    this.left = this.right = null;
  }

//  public AvlTree(int element, int height, AvlTree left, AvlTree right) {
//    this.element = element;
//    this.height = height;
//    this.left = left;
//    this.right = right;
//  }

  // Projectors

  private AvlTree left() {
    return left;
  }

  private AvlTree right() {
    return right;
  }

  // ========= Methods to be verified ===================================

  // ~~~~~~~~~ Begin findMinimum ~~~~~~~~~~

  private int findMinimum(AvlTree root) {
    assert (root != null);
    AvlTree curr = root;
    while (curr.left() != null) {
      curr = curr.left();
    }
    return curr.element;
  }

  // ~~~~~~~~~ End of findMinimum ~~~~~~~~~~

  // ~~~~~~~~~ Begin contains ~~~~~~~~~~

  private boolean contains(AvlTree root, int x) {
    AvlTree p = root;
    while (p != null) {
      // Verify.ignoreIf(!repOK(root, LIMIT));
      if (x == p.element) {
        return true;
      } else if (x < p.element) {
        p = p.left;
      } else {
        p = p.right;
      }
    }
    return false;
  }

  // ~~~~~~~~~ End of contains ~~~~~~~~~~

  // ~~~~~~~~~ Begin insert ~~~~~~~~~~
  
  
  public AvlTree insert(int x) {
    AvlTree t = insert(this, x);

    //assert repOK_Concrete(t);
    return t;
  }
  

  private AvlTree insert(AvlTree root, int x) {
    AvlTree t = root;

    if (t == null) {
      t = new AvlTree(x);
    } else if (x < t.element) {
      t.left = insert(t.left, x);
      if (AvlTree.height(t.left) - AvlTree.height(t.right) == 2) {
        if (x < t.left.element) {
          t = AvlTree.rotateWithLeftChild(t);
        } else {
          t = AvlTree.doubleWithLeftChild(t);
        }
      }
    } else if (x > t.element) {
      t.right = insert(t.right, x);
      if (AvlTree.height(t.right) - AvlTree.height(t.left) == 2) {
        if (x > t.right.element) {
          t = AvlTree.rotateWithRightChild(t);
        } else {
          t = AvlTree.doubleWithRightChild(t);
        }
      }
    } else {
      ; // Duplicate; do nothing
    }
    t.height = AvlTree.max(AvlTree.height(t.left), AvlTree.height(t.right)) + 1;
    
    //assert repOK_Concrete(t);
    return t;
  }


  
//
//  public AvlTree insert(AvlTree root, int x) {
//    AvlTree t = root;
//
//    if (t == null) {
//      t = new AvlTree(x);
//    } else if (x < t.element) {
//      t.left = insert(t.left, x);
//      if (AvlTree.height(t.left) - AvlTree.height(t.right) == 2) {
//        if (x < t.left.element) {
//          t = AvlTree.rotateWithLeftChild(t);
//        } else {
//          t = AvlTree.doubleWithLeftChild(t);
//        }
//      }
//    } else if (x > t.element) {
//      t.right = insert(t.right, x);
//      if (AvlTree.height(t.right) - AvlTree.height(t.left) == 2) {
//        if (x > t.right.element) {
//          t = AvlTree.rotateWithRightChild(t);
//        } else {
//          t = AvlTree.doubleWithRightChild(t);
//        }
//      }
//    } else {
//      ; // Duplicate; do nothing
//    }
//    t.height = AvlTree.max(AvlTree.height(t.left), AvlTree.height(t.right)) + 1;
//    
//    assert repOK_Concrete(t);
//    return t;
//  }
//
  /**
   * Return the height of t, or -1 if null.
   */
  private static int height(AvlTree t) {
//     BUG: return t == null ? -1 : t.height;
    return t == null ? 0 : t.height;
  }

  /**
   * Return maximum of two ints.
   */
  private static int max(int lhs, int rhs) {
    return lhs > rhs ? lhs : rhs;
  }

  /**
   * Double rotate binary tree node: first left child with its right child; then node k3 with new
   * left child. For AVL trees, this is a double rotation for case 2. Update heights, then return
   * new root.
   */
  private static AvlTree doubleWithLeftChild(final AvlTree k3) {
    k3.left = AvlTree.rotateWithRightChild(k3.left);
    return AvlTree.rotateWithLeftChild(k3);
  }

  /**
   * Double rotate binary tree node: first right child with its left child; then node k1 with new
   * right child. For AVL trees, this is a double rotation for case 3. Update heights, then return
   * new root.
   */
  private static AvlTree doubleWithRightChild(final AvlTree k1) {
    k1.right = AvlTree.rotateWithLeftChild(k1.right);
    return AvlTree.rotateWithRightChild(k1);
  }

  /**
   * Rotate binary tree node with left child. For AVL trees, this is a single rotation for case 1.
   * Update heights, then return new root.
   */
  private static AvlTree rotateWithLeftChild(final AvlTree k2) {
    final AvlTree k1 = k2.left;
    k2.left = k1.right;
    k1.right = k2;
    k2.height = AvlTree.max(AvlTree.height(k2.left), AvlTree.height(k2.right)) + 1;
    k1.height = AvlTree.max(AvlTree.height(k1.left), k2.height) + 1;
    return k1;
  }

  /**
   * Rotate binary tree node with right child. For AVL trees, this is a single rotation for case 4.
   * Update heights, then return new root.
   */
  private static AvlTree rotateWithRightChild(final AvlTree k1) {
    final AvlTree k2 = k1.right;
    k1.right = k2.left;
    k2.left = k1;
    k1.height = AvlTree.max(AvlTree.height(k1.left), AvlTree.height(k1.right)) + 1;
    k2.height = AvlTree.max(AvlTree.height(k2.right), k1.height) + 1;
    return k2;
  }

  // ~~~~~~~~~ End of insert ~~~~~~~~~~

  // --------------------------------------bfsTraverse-begin-------------------------------------//

  private void bfsTraverse(AvlTree root) {
    Set<AvlTree> visited = new HashSet<AvlTree>();
    List<AvlTree> worklist = new ArrayList<AvlTree>();
    visited.add(root);
    worklist.add(root);
    while (!worklist.isEmpty() && visited.size() <= LIMIT) {
      AvlTree node = worklist.remove(0);
      AvlTree left = node.left;
      if (left != null && visited.add(left)) {

        worklist.add(left);
      }
      AvlTree right = node.right;
      if (right != null && visited.add(right)) {
        worklist.add(right);
      }
    }
  }

  // --------------------------------------bfsTraverse-end-------------------------------------//

  // --------------------------------------dfsTraverse-begin-------------------------------------//

  private boolean dfsTraverse(AvlTree root) {
    HashSet<AvlTree> visited = new HashSet<AvlTree>();
    dfsTraverseAux(root, visited);
    return visited.size() <= LIMIT;
  }

  private void dfsTraverseAux(AvlTree root, HashSet<AvlTree> visited) {
    if (root != null && visited.add(root)) {

      if (root.left != null) {
        dfsTraverseAux(root.left, visited);
      }
      if (root.right != null) {
        dfsTraverseAux(root.right, visited);
      }

    }
  }

  // --------------------------------------dfsTraverse-end-------------------------------------//

  // ========= CONCRETE INVARIANT ====================

  // ~~~~~~~~~ Begin repOK_Concrete ~~~~~~~~~~

  private boolean repOK_Concrete(AvlTree root) {
    return repOK_Structure(root); // && repOK_Ordered(root);
  }

  private boolean repOK_Structure(AvlTree root) {
    Set<AvlTree> visited = new HashSet<AvlTree>();
    List<AvlTree> worklist = new ArrayList<AvlTree>();

    if (root != null) {
      visited.add(root);
      worklist.add(root);
    }

    while (!worklist.isEmpty()) {

      AvlTree node = worklist.remove(0);

      if (!repOK_Structure_CheckHeight(node))
        return false; // Unbalanced or wrong height value!

      AvlTree left = node.left();
      if (left != null) {
        if (!visited.add(left))
          return false; // Not acyclic!

        worklist.add(left);
      }

      AvlTree right = node.right();
      if (right != null) {
        if (!visited.add(right))
          return false; // Not acyclic!

        worklist.add(right);
      }

    }
    return visited.size() <= LIMIT;
  }

  // Return true if node.height is consistent and within [-1, 1].
  // Assume node != null.
  //
  private boolean repOK_Structure_CheckHeight(AvlTree node) {
    int lh, rh;

    if (node.left == null)
      lh = 0;
    else
      lh = node.left.height;

    if (node.right == null)
      rh = 0;
    else
      rh = node.right.height;

    int difference = lh - rh;
    if (difference < -1 || difference > 1) {
      return false; // Not balanced!
    }
    int max = AvlTree.max(lh, rh);
    if (node.height != 1 + max)
      return false; // Wrong value in height field!

    return true;
  }

  private boolean repOK_Ordered(AvlTree root) {
    int min = repOK_findMin(root);
    int max = repOK_findMax(root);
    if (!repOK_ElementsAreOrdered(root, min - 1, max + 1)) {
      return false;
    }

    // PEND: What does this do? Is it really necessary?
    /*
     * List<AvlTree> worklist = new ArrayList<AvlTree>(); worklist.add(root); while
     * (!worklist.isEmpty()) { AvlTree current = worklist.remove(0); if (current.left() != null) {
     * worklist.add(current.left()); } if (current.right() != null) { worklist.add(current.right());
     * } }
     */

    return true;
  }

  // Return smallest element.
  // Assume root != null.
  //
  private int repOK_findMin(AvlTree root) {
    AvlTree curr = root;
    while (curr.left() != null) {
      curr = curr.left();
    }
    return curr.element;
  }

  // Return largest element.
  // Assume root != null.
  //
  private int repOK_findMax(AvlTree root) {
    AvlTree curr = root;
    while (curr.right() != null) {
      curr = curr.right();
    }
    return curr.element;
  }

  // Return true iff e is a strict (no dups allowed!) search tree.
  // Assume e != null.
  //
  private boolean repOK_ElementsAreOrdered(AvlTree e, int min, int max) {
    if ((e.element <= min) || (e.element >= max)) {
      return false;
    }
    if (e.left() != null) {
      if (!repOK_ElementsAreOrdered(e.left(), min, e.element)) {
        return false;
      }
    }
    if (e.right() != null) {
      if (!repOK_ElementsAreOrdered(e.right(), e.element, max)) {
        return false;
      }
    }
    return true;
  }

  // ~~~~~~~~~ End of repOK_Concrete ~~~~~~~~~~

  // ~~~~~~~~~ Begin dumpTree (just for manual testing purposes) ~~~~~~~~~~~

  private static void dumpTree(AvlTree root) {
    System.out.println("");
    dumpTree(root, 0);
    System.out.println("");
  }

  private static void dumpTree(AvlTree root, int level) {
    if (root == null)
      return;
    dumpTree(root.right, level + 1);
    if (level != 0) {
      for (int i = 0; i < level - 1; i++)
        System.out.print("|\t");
      System.out.println("|-------" + root.element);
    } else
      System.out.println(root.element);
    dumpTree(root.left, level + 1);
  }

  public AvlTree remove(int x, AvlTree t) {
    if (t == null)
      return t; // Item not found; do nothing

    if (x < t.element)
      t.left = remove(x, t.left);
    else if (x > t.element)
      t.right = remove(x, t.right);
    else if (t.left != null && t.right != null) // Two children
    {
      t.element = findMinimum(t.right);
      t.right = remove(t.element, t.right);
    } else
      t = (t.left != null) ? t.left : t.right;
    return balance(t);
  }

  private AvlTree balance(AvlTree t) {
    if (t == null)
      return t;

    if (height(t.left) - height(t.right) > 1)
      if (height(t.left.left) >= height(t.left.right))
        t = rotateWithLeftChild(t);
      else
        t = doubleWithLeftChild(t);
    else if (height(t.right) - height(t.left) > 1)
      if (height(t.right.right) >= height(t.right.left))
        t = rotateWithRightChild(t);
      else
        t = doubleWithRightChild(t);

    t.height = Math.max(height(t.left), height(t.right)) + 1;
    return t;
  }
  // ~~~~~~~~~ End of dumpTree ~~~~~~~~~~

  // marked in tables as dfsTraverse
  // private static int LIMIT = 10;
  // public static void main(String[] args) {
  //
  // AvlTree X = new AvlTree(10);
  // X = (AvlTree) Debug.makeSymbolicRef("X", X);
  // X = (AvlTree) Debug.makeSymbolicRefBounded("X", X);
  //
  // if (X != null) {
  // try {
  // X.dfsTraverse(X);
  // X.countStructure(X);
  // X.dumpStructure(X);
  // } catch (Exception e) {}
  // }
  //
  // }
  //

  // marked in tables as bfsTraverse
  private static int LIMIT = 30;

  // Main to gen instances

  /*
   * public static void main(String[] args) { AvlTree X = new AvlTree(LIMIT); X = (AvlTree)
   * Debug.makeSymbolicRefBounded("N", X); if (X != null) { if (X.repOK_Concrete(X)) {
   * X.genPositiveVectors(); X.countStructure(X); } else { X.genNegativeVectors(); } } }
   */

  /*
  public static void main(String[] args) {
    AvlTree X = new AvlTree(LIMIT); //
    X = (AvlTree) Debug.makeSymbolicRef("X", X);
    if (X != null) {
      try {
        X.bfsTraverse(X);
        X.countStructure(X);
      } catch (Exception e) {
      }
    }
  }
  */

  // marked in tables as repOK_Concrete
  // private static int LIMIT = __SIZE__;
  // public static void main(String[] args) {
  //
  // AvlTree X = new AvlTree(10);
  // X = (AvlTree) Debug.makeSymbolicRef("X", X);
  // //X = (AvlTree) Debug.makeSymbolicRefBounded("X", X);
  //
  // if (X != null && X.repOK_Concrete(X)) {
  // X.countStructure(X);
  // }
  //
  // }
  //
  //

} // ~~~~~~~~~~~~~~~~~ End of class ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
