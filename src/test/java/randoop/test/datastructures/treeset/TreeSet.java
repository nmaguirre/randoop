package randoop.test.datastructures.treeset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;



//Authors: Marcelo Frias
/**
 * @Invariant ( this.RED==false ) && 
 *		( this.BLACK==true ) &&
 *		( this.root.parent in null ) &&
 *		( this.root!=null => this.root.color = this.BLACK ) && 
 *		( all n: TreeSetEntry | n in this.root.*(left @+ right @+ parent) @- null => ( 
 *				(n.key != null ) &&
 *				( n.left != null => n.left.parent = n ) &&
 *				( n.right != null => n.right.parent = n ) &&
 *				( n.parent != null => n in n.parent.(left @+ right) ) &&
 *				( n !in n.^parent ) &&
 *				( all x : TreeSetEntry | (( x in n.left.^(left @+ right) @+ n.left @- null ) => ( n.key > x.key )) ) &&
 *				( all x : TreeSetEntry | (( x in n.right.^(left @+ right) @+ n.right @- null ) => ( x.key > n.key ))) &&
 *				( n.color = this.RED && n.parent != null => n.parent.color = this.BLACK ) && 
 *				
 *				( ( n.left=null && n.right=null ) => ( n.blackHeight=1 ) ) &&
 *				( n.left!=null && n.right=null => ( 
 *				      ( n.left.color = this.RED ) && 
 *				      ( n.left.blackHeight = 1 ) && 
 *				      ( n.blackHeight = 1 )  
 *				)) &&
 *				( n.left=null && n.right!=null =>  ( 
 *				      ( n.right.color = this.RED ) && 
 *				      ( n.right.blackHeight = 1 ) && 
 *				      ( n.blackHeight = 1 ) 
 *				 )) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.RED && n.right.color=this.RED => ( 
 *				        ( n.left.blackHeight = n.right.blackHeight ) && 
 *				        ( n.blackHeight = n.left.blackHeight ) 
 *				)) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.BLACK && n.right.color=this.BLACK => ( 
 *				        ( n.left.blackHeight=n.right.blackHeight ) && 
 *				        ( n.blackHeight=n.left.blackHeight + 1 )  
 *				)) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.RED && n.right.color=this.BLACK => ( 
 *				        ( n.left.blackHeight=n.right.blackHeight + 1 ) && 
 *				        ( n.blackHeight = n.left.blackHeight  )  
 *				)) && 
 *				( n.left!=null && n.right!=null && n.left.color=this.BLACK && n.right.color=this.RED => ( 
 *				        ( n.right.blackHeight=n.left.blackHeight + 1 ) && 
 *				        ( n.blackHeight = n.right.blackHeight  )  )) 
 *				)) ; 
 */
public class TreeSet {
	

	
    // ------------------------ repOK ---------------------------//
/*    public boolean repOK() {
        if (root == null)
            return size == 0;
        // RootHasNoParent
        if (root.parent != null)
            return debug("RootHasNoParent");

        // Added by Pablo
     	if (root.color != BLACK) {
     		System.out.println("raiz roja");
			return false;
     	}	

        Set visited = new java.util.HashSet();
        visited.add(new Wrapper(root));
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            TreeSetEntry current = (TreeSetEntry) workList.removeFirst();
            // Acyclic
            // // if (!visited.add(new Wrapper(current)))
            // // return debug("Acyclic");
            // Parent Definition
            TreeSetEntry cl = current.left;
            if (cl != null) {
                if (!visited.add(new Wrapper(cl)))
                    return debug("Acyclic");
                if (cl.parent != current)
                    return debug("parent_Input1");
                workList.add(cl);
            }
            TreeSetEntry cr = current.right;
            if (cr != null) {
                if (!visited.add(new Wrapper(cr)))
                    return debug("Acyclic");
                if (cr.parent != current)
                    return debug("parent_Input2");
                workList.add(cr);
            }
        }
        // SizeOk
        if (visited.size() != size)
            return debug("SizeOk");
        if (!repOkColors())
            return false;
        return repOkKeysAndValues();
    }
    */
	
    private boolean repOkColors() {
        // RedHasOnlyBlackChildren
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            TreeSetEntry current = (TreeSetEntry) workList.removeFirst();
            TreeSetEntry cl = current.left;
            TreeSetEntry cr = current.right;
            if (current.color == RED) {
                if (cl != null && cl.color == RED)
                    return debug("RedHasOnlyBlackChildren1");
                if (cr != null && cr.color == RED)
                    return debug("RedHasOnlyBlackChildren2");
            }
            if (cl != null)
                workList.add(cl);
            if (cr != null)
                workList.add(cr);
        }
        // SimplePathsFromRootToNILHaveSameNumberOfBlackNodes
        int numberOfBlack = -1;
        workList = new java.util.LinkedList();
        workList.add(new Pair(root, 0));
        while (!workList.isEmpty()) {
            Pair p = (Pair) workList.removeFirst();
            TreeSetEntry e = p.e;
            int n = p.n;
            if (e != null && e.color == BLACK)
                n++;
            if (e == null) {
                if (numberOfBlack == -1)
                    numberOfBlack = n;
                else if (numberOfBlack != n)
                    return debug("SimplePathsFromRootToNILHaveSameNumberOfBlackNodes");
            } else {
                workList.add(new Pair(e.left, n));
                workList.add(new Pair(e.right, n));
            }
        }
        return true;
    }

    private boolean repOkKeysAndValues() {
        // BST1 and BST2
        // this was the old way of determining if the keys are ordered
        // java.util.LinkedList workList = new java.util.LinkedList();
        // workList = new java.util.LinkedList();
        // workList.add(root);
        // while (!workList.isEmpty()) {
        // Entry current = (Entry)workList.removeFirst();
        // Entry cl = current.left;
        // Entry cr = current.right;
        // if (current.key==current.key) ;
        // if (cl != null) {
        // if (compare(current.key, current.maximumKey()) <= 0)
        // return debug("BST1");
        // workList.add(cl);
        // }
        // if (cr != null) {
        // if (compare(current.key, current.minimumKey()) >= 0)
        // return debug("BST2");
        // workList.add(cr);
        // }
        // }
        // this is the new (Alex's) way to determine if the keys are ordered
        if (!orderedKeys(root, null, null))
            return debug("BST");
        // touch values
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            TreeSetEntry current = (TreeSetEntry) workList.removeFirst();

            if (current.left != null)
                workList.add(current.left);
            if (current.right != null)
                workList.add(current.right);
        }
        return true;
    }

    
    private boolean orderedKeys(TreeSetEntry e, Object min, Object max) {
        /*if (e.key == -1)
            return false;*/
        if (((min != null) && (compare(e.key, min) <= 0))
                || ((max != null) && (compare(e.key, max) >= 0)))
            return false;
        if (e.left != null)
            if (!orderedKeys(e.left, min, e.key))
                return false;
        if (e.right != null)
            if (!orderedKeys(e.right, e.key, max))
                return false;
        return true;
    }
    
    
    private int compare(Object k1, Object k2) {
        return ((Comparable) k1).compareTo(k2);
    }
    
    
    private final boolean debug(String s) {
        // System.out.println(s);
        return false;
    }
	
	
    

    /*** END OF repOK ***/

    
	public TreeSet() {
		root = null;
		size = 0;		
	}
    
    
    

	private /*@ nullable @*/ TreeSetEntry root;

	/**
	 * The number of entries in the tree
	 */
	private int size;


	static final int RED = 0;
	static final int BLACK = 1;


	public boolean contains(int aKey) {
		return getEntry(aKey) != null;
	}

	private TreeSetEntry getEntry(int key) {
		TreeSetEntry p = root;
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


	private TreeSetEntry getEntry_remove(int key) {
		TreeSetEntry p = root;
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


	private void init_TreeSetEntry(TreeSetEntry entry, int new_key, TreeSetEntry new_parent) {
		entry.color = RED;
		entry.left = null;
		entry.right = null;
		entry.key = new_key;
		entry.parent = new_parent;
	}


	public boolean add(int aKey) {
		TreeSetEntry t = root;

		if (t == null) {
			incrementSize();
			root = new TreeSetEntry();
			init_TreeSetEntry(root, aKey, null);

			return false;
		}

		boolean boolean_true= true;
		while (boolean_true) {

			if (aKey == t.key) {

				return true;
			} else if (aKey < t.key) {

				if (t.left != null) {

					t = t.left;
				} else {

					incrementSize();
					t.left = new TreeSetEntry();
					init_TreeSetEntry(t.left, aKey, t);

					fixAfterInsertion(t.left);

					return false;
				}
			} else { // cmp > 0


				if (t.right != null) {
					t = t.right;
				} else {
					incrementSize();
					t.right = new TreeSetEntry();
					init_TreeSetEntry(t.right, aKey, t);
					fixAfterInsertion(t.right);
					return false;
				}
			}
		}
		return false;
	}

	private void incrementSize() {
		size++;
	}


	/**
	 * Balancing operations.
	 *
	 * Implementations of rebalancings during insertion and deletion are
	 * slightly different than the CLR version.  Rather than using dummy
	 * nilnodes, we use a set of accessors that deal properly with null.  They
	 * are used to avoid messiness surrounding nullness checks in the main
	 * algorithms.
	 */

	private static /*boolean*/ int colorOf(TreeSetEntry p) {
		//boolean black = true;
		/*boolean*/int  result ;
		if (p==null)
			//result =black;
			result =BLACK;
		else
			result =p.color;
		return result;
	}

	private static TreeSetEntry parentOf(TreeSetEntry p) {
		TreeSetEntry result;
		if (p == null)
			result = null;
		else
			result = p.parent;

		return result;
	}

	private static void setColor(TreeSetEntry p, /*boolean*/ int c) {
		if (p != null)
			p.color = c;
	}

	private static TreeSetEntry leftOf(TreeSetEntry p) {
		TreeSetEntry result ;
		if (p == null)
			result = null;
		else
			result = p.left;
		return result;
	}

	private static TreeSetEntry rightOf(TreeSetEntry p) {
		TreeSetEntry result;
		if (p == null) 
			result = null;
		else
			result = p.right;
		return result;
	}

	/** From CLR **/
	private void rotateLeft(TreeSetEntry p) {
		TreeSetEntry r = p.right;
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
	}

	/** From CLR **/
	private void rotateRight(TreeSetEntry p) {
		TreeSetEntry l = p.left;
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
	}

	/** From CLR **/
	private void fixAfterInsertion(final TreeSetEntry entry) {
		TreeSetEntry x = entry;
		x.color = RED;

		while (x != null && x != root && x.parent.color == RED) {

			if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
				TreeSetEntry y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == rightOf(parentOf(x))) {
						x = parentOf(x);
						rotateLeft(x);
					} else {
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null) {
						rotateRight(parentOf(parentOf(x)));
					} else {
					}
				}
			} else {
				TreeSetEntry y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == leftOf(parentOf(x))) {
						x = parentOf(x);
						rotateRight(x);
					} else {
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					if (parentOf(parentOf(x)) != null) {
						rotateLeft(parentOf(parentOf(x)));
					} else {
					}

				}
			}
		}
		root.color = BLACK;
	}




	public boolean remove(int aKey) {
		TreeSetEntry p = getEntry_remove(aKey);
		if (p == null) {
			return false;
		}


		deleteEntry(p);

		return true;
	}

	/**
	 * Delete node p, and then rebalance the tree.
	 */
	private void deleteEntry(TreeSetEntry p) {
		decrementSize();

		// If strictly internal, copy successor's element to p and then make p
		// point to successor.
		if (p.left != null && p.right != null) {
			TreeSetEntry s = successor(p);
			p.key = s.key;

			p = s;
		} // p has 2 children

		// Start fixup at replacement node, if it exists.
		TreeSetEntry replacement;
		if (p.left != null )
			replacement = p.left ;
		else
			replacement = p.right;

		if (replacement != null) {

			// Link replacement to parent
			replacement.parent = p.parent;
			if (p.parent == null) {
				root = replacement;
			} else if (p == p.parent.left){
				p.parent.left = replacement;
			} else {
				p.parent.right = replacement;
			}

			// Null out links so they are OK to use by fixAfterDeletion.
			p.left = p.right = p.parent = null;

			// Fix replacement
			if (p.color == BLACK) {
				fixAfterDeletion(replacement);
			}
		} else if (p.parent == null) { // return if we are the only node.
			root = null;
		} else { //  No children. Use self as phantom replacement and unlink.
			if (p.color == BLACK) {
				fixAfterDeletion(p);
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
	}

	/** From CLR **/
	private void fixAfterDeletion(final TreeSetEntry entry) {
		TreeSetEntry x = entry;

		while (x != root && colorOf(x) == BLACK) {

			if (x == leftOf(parentOf(x))) {
				TreeSetEntry sib = rightOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}

				if (colorOf(leftOf(sib)) == BLACK
						&& colorOf(rightOf(sib)) == BLACK) {

					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			} else { // symmetric
				TreeSetEntry sib = leftOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}

				if (colorOf(rightOf(sib)) == BLACK
						&& colorOf(leftOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}
		setColor(x, BLACK);
	}

	private void decrementSize() {
		size--;
	}

	/*
	 * Returns the successor of the specified Entry, or null if no such.
	 */
	private TreeSetEntry successor(TreeSetEntry t) {
		if (t == null) {
			return null;
		} else if (t.right != null) {
			TreeSetEntry p = t.right;
			while (p.left != null) {
				p = p.left;
			}
			return p;
		} else {
			TreeSetEntry p = t.parent;
			TreeSetEntry ch = t;
			while (p != null && ch == p.right) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}

	

	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(size);        
		buf.append("{");
		if (root != null)
			buf.append(root.toStrings());
		buf.append("}");
		return buf.toString();
	}
	
	
	public TreeSet deepcopy() {
		
		LinkedList<TreeSetEntry> visited = new LinkedList<TreeSetEntry>();
		ArrayList<TreeSetEntry> nodes = new ArrayList<TreeSetEntry>(size);		
		ArrayList<TreeSetEntry> newnodes = new ArrayList<TreeSetEntry>(size);
		
		int ind = 0;
		
		if (root == null) {
			TreeSet res = new TreeSet();
			res.root = null;
			res.size = size;
			return res;
		}
		else {
			visited.add(root);
		
			while (!visited.isEmpty()) {
				TreeSetEntry currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.left != null) 
					visited.add(currNode.left);
				if (currNode.right != null) 
					visited.add(currNode.right);
			}
			
			for (int i=0; i<nodes.size();i++) {
				TreeSetEntry newnode = new TreeSetEntry();
				newnodes.add(newnode);
			}
	
			for (int i=0; i<nodes.size();i++) {
				TreeSetEntry currnode = nodes.get(i);
				TreeSetEntry newnode = newnodes.get(i);
				
				newnode.key = currnode.key;
				newnode.color = currnode.color;
				
				if (currnode.left != null)
					newnode.left = newnodes.get(currnode.left._index);
				else
					newnode.left = null;
	
				if (currnode.right != null)
					newnode.right = newnodes.get(currnode.right._index);
				else
					newnode.right = null;
				
				if (currnode.parent != null)
					newnode.parent = newnodes.get(currnode.parent._index);
				else
					newnode.parent = null;	
			}
			TreeSet res = new TreeSet();
			res.root = newnodes.get(root._index);
			res.size = size;
			return res;

		}	
	}
	
	
	@Override
	public int hashCode() {
		if (this.root == null) return 1;
		
		Set thisSet = new HashSet();
		this.root.toSet(thisSet);
		return thisSet.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeSet other = (TreeSet) obj;
		
		if (size != other.size)
			return false;
		
		if (this.root == null)
			if (other.root == null) 
				return true;
			else 
				return false;
		else {
			Set thisSet = new HashSet();
			Set otherSet = new HashSet();
			this.root.toSet(thisSet);
			other.root.toSet(otherSet);
			return thisSet.equals(otherSet);
		}
	}	
	
/*	
	public static void main(String [] args) {
		
		TreeSet tree = new TreeSet();
		tree.add(1);
		tree.add(2);
		tree.add(3);
		tree.add(0);
		System.out.println(tree.toString());

		
		TreeSet copy = tree.deepcopy();
		tree.remove(0);
		System.out.println(tree.toString());
		
		System.out.println(copy.toString());
		copy.add(5);
		System.out.println(copy.toString());
		System.out.println(tree.toString());
	}
	*/

	
	
	
}
