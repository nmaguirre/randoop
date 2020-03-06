package symbolicheap.bounded;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import randoop.util.heapcanonicalization.DummySymbolicBHeapNode;

public class BinomialHeap {

	private/* @ nullable @ */BinomialHeapNode Nodes;

	private int size;

	public BinomialHeap() {
		Nodes = null;
		size = 0;
	}

//	public static final BinomialHeap SYMBOLICBINOMIALHEAP = new BinomialHeap(); // field added to
	// execute the
	// hybridRepOK.
//	public static final int SYMBOLICINT = (int) Integer.MIN_VALUE; // field added to execute the
	// hybridRepOK.

	private boolean repOK() {
		return repOK_Concrete();
	}	

	private boolean repOK_Concrete() {
		BinomialHeapNode root = this.Nodes;
		final LinkedList<BinomialHeapNode> seen = new LinkedList<BinomialHeapNode>();
		if (root != null) {

			// son aciclicos los hijos de root?
			if (!isAcyclic(root, seen)) {
				return false;
			}
			// esta ordenado el arbol que cuelga de root?
			if (!ordered(root)) {
				return false;
			}

			// el parent de root es null?
			if (root.parent != null) {
				return false;
			}
			// si hay un root.sibling, el degree de root es menor
			if (root.sibling != null) {
				if (root.degree >= root.sibling.degree) {
					return false;
				}
			}

			// los ns son los siblings de root
			BinomialHeapNode ns = root.sibling;

			while (ns != null) {

				// son aciclicos los hijos del sibling de root
				if (!isAcyclic(ns, seen)) {
					return false;
				}
				// el parent del sibling es root?
				if (ns.parent != null) {
					return false;
				}
				// el degree de este sibling es menor al del siguiente?
				if (ns.sibling != null) {
					if (ns.degree >= ns.sibling.degree) {
						return false;
					}
				}

				// estan ordenados el subarbol que cuelga de este sibling?
				if (!ordered(ns)) {
					return false;
				}

				// proximo sibling de root
				ns = ns.sibling;
			}

		}

		return size == seen.size() && size <= LIMIT;

	}

	private boolean repOK_ConcretePost() {

		BinomialHeapNode root = this.Nodes;
		final LinkedList<BinomialHeapNode> seen = new LinkedList<BinomialHeapNode>();

		if (root != null) {

			// son aciclicos los hijos de root?
			if (!isAcyclic(root, seen)) {
				return false;
			}

			// esta ordenado el arbol que cuelga de root?
			if (!ordered(root)) {
				return false;
			}

			// el parent de root es null?
			if (root.parent != null) {
				return false;
			}

			// si hay un root.sibling, el degree de root es menor
			if (root.sibling != null) {
				if (root.degree >= root.sibling.degree) {
					return false;
				}
			}

			// los ns son los siblings de root
			BinomialHeapNode ns = root.sibling;

			while (ns != null) {

				// son aciclicos los hijos del sibling de root
				if (!isAcyclic(ns, seen)) {
					return false;
				}

				// el parent del sibling de root es null?
				if (ns.parent != null) {
					return false;
				}

				// el degree de este sibling es menor al del siguiente?
				if (ns.sibling != null) {
					if (ns.degree >= ns.sibling.degree) {
						return false;
					}
				}

				// estan ordenados el subarbol que cuelga de este sibling?
				if (!ordered(ns)) {
					return false;
				}

				// proximo sibling de root
				ns = ns.sibling;
			}

		}

		return size == seen.size();
	}

	private boolean structuralHybridRepOK() {
		BinomialHeapNode root = this.Nodes;

		final LinkedList<BinomialHeapNode> seen = new LinkedList<BinomialHeapNode>();

		if (root != null && !(root instanceof DummySymbolicBHeapNode)) {

			// son aciclicos los hijos de root?
			if (!acyclic_hybrid(root, seen))
				return false;

			// el parent de root es null?
			if (!(root.parent instanceof DummySymbolicBHeapNode) && root.parent != null)
				return false;

			// los ns son los siblings de root
			BinomialHeapNode ns = root.sibling;

			while (ns != null && !(ns instanceof DummySymbolicBHeapNode)) {

				// son aciclicos los hijos del sibling de root
				if (!acyclic_hybrid(ns, seen))
					return false;

				// es parent del sibling de root es null?
				if (!(ns.parent instanceof DummySymbolicBHeapNode) && ns.parent != null)
					return false;

				// proximo sibling de root
				ns = ns.sibling;
			}
		}

		return true;
	}

	/**
	 * retorna true si es aciclico el subarbol y de paso - verifica que parent este bien formado -
	 * chequea que degree sea positivo - que key sea non_null (si key es Object) - que degree sea la
	 * cantidad de hijos (no descendientes) del nodo
	 */
	private static boolean isAcyclic(final BinomialHeapNode start,
			final LinkedList<BinomialHeapNode> seen) {
		if (seen.contains(start)) {
			return false;
		}
		if (start.degree < 0) {
			return false;
		}

		// enable these lines if key is instance of Object
		// if (start.key==null)
		// return false;

		seen.add(start);

		BinomialHeapNode currchild = start.child;

		int child_count = 0;

		while (currchild != null) {
			child_count++;

			if (currchild.parent != start) {
				return false;
			}
			if (!isAcyclic(currchild, seen)) {
				return false;
			}
			if (currchild.sibling != null) {
				if (currchild.degree <= currchild.sibling.degree) {
					return false;
				}
			}
			currchild = currchild.sibling;
		}

		if (start.degree != child_count) {
			return false;
		}

		if (start.child != null) {
			int tam_child = 1;
			if (start.child.child != null) {
				BinomialHeapNode curr = start.child.child;
				while (curr != null) {
					tam_child += count_nodes(curr);
					curr = curr.sibling;
				}
			}

			int tam_sibling = 1;
			if (start.child.sibling != null) {
				BinomialHeapNode curr = start.child.sibling;
				while (curr != null) {
					tam_sibling += count_nodes(curr);
					curr = curr.sibling;
				}
			}

			if (tam_child != tam_sibling) {
				return false;
			}
		}
		return true;
	}

	/**
	 * retorna true si es aciclico el subarbol y de paso - verifica que parent este bien formado -
	 * chequea que degree sea positivo - que key sea non_null (si key es Object) - que degree sea la
	 * cantidad de hijos (no descendientes) del nodo
	 */
	private static boolean acyclic_hybrid(final BinomialHeapNode start,
			final LinkedList<BinomialHeapNode> seen) {
		if (!(start instanceof DummySymbolicBHeapNode)) {

			if (seen.contains(start))
				return false;

			seen.add(start);

			BinomialHeapNode child = start.child;

			while (child != null && !(child instanceof DummySymbolicBHeapNode)) {

				if (!(child.parent instanceof DummySymbolicBHeapNode) && child.parent != start)
					return false;

				if (!acyclic_hybrid(child, seen))
					return false;

				child = child.sibling;
			}

			if (child instanceof DummySymbolicBHeapNode) {
				return true;
			}
		}
		return true;
	}


	private static int count_nodes(BinomialHeapNode start) {
		int node_count = 1;
		BinomialHeapNode child = start.child;
		while (child != null) {
			node_count += count_nodes(child);
			child = child.sibling;
		}
		return node_count;
	}


	private static boolean ordered(final BinomialHeapNode node) {
		if (node.child != null) {
			if (node.child.key < node.key) {
				return false;
			}
			if (!ordered(node.child)) {
				return false;
			}
			for (BinomialHeapNode ns = node.child.sibling; ns != null; ns = ns.sibling) {
				if (ns.key < node.key) {
					return false;
				}
				if (!ordered(ns)) {
					return false;
				}
			}
			return true;
		}
		return true;
	}

	private void dfsTraverse(BinomialHeapNode root) {
		HashSet<BinomialHeapNode> visited = new HashSet<BinomialHeapNode>();
		dfsTraverseAux(root, visited);
		// visited.size() <= LIMIT;
	}

	private void dfsTraverseAux(BinomialHeapNode root, HashSet<BinomialHeapNode> visited) {
		if (root != null && visited.add(root)) {

			if (root.sibling != null) {
				dfsTraverseAux(root.sibling, visited);
			}
			if (root.child != null) {
				dfsTraverseAux(root.child, visited);
			}
			if (root.parent != null) {
				dfsTraverseAux(root.parent, visited);
			}
		}
	}

	private void bfsTraverse(BinomialHeapNode nodes) {
		Set<BinomialHeapNode> visited = new HashSet<BinomialHeapNode>();
		List<BinomialHeapNode> worklist = new ArrayList<BinomialHeapNode>();
		if (nodes != null) {
			visited.add(nodes);
			worklist.add(nodes);
			while (!worklist.isEmpty() && visited.size() <= LIMIT) {
				BinomialHeapNode node = worklist.remove(0);

				BinomialHeapNode sibling = node.sibling;
				if (sibling != null && visited.add(sibling)) {
					worklist.add(sibling);
				}
				BinomialHeapNode child = node.child;
				if (child != null && visited.add(child)) {
					worklist.add(child);
				}
				BinomialHeapNode parent = node.parent;
				if (parent != null && visited.add(parent)) {
					worklist.add(parent);
				}
			}
		}
	}

	// 2. Find the minimum key
	/**
	 * @Modifies_Everything
	 * 
	 * @Requires some this.nodes ;
	 * @Ensures ( some x: BinomialHeapNode | x in this.nodes && x.key == return ) && ( all y :
	 *          BinomialHeapNode | ( y in this.nodes && y!=return ) => return <= y.key ) ;
	 */
	private int findMinimum() {
		return Nodes.findMinNode().key;
	}

	// 3. Unite two binomial heaps
	// helper procedure
	private void merge(/* @ nullable @ */BinomialHeapNode binHeap) {
		BinomialHeapNode temp1 = Nodes, temp2 = binHeap;
		while ((temp1 != null) && (temp2 != null)) {
			if (temp1.degree == temp2.degree) {
				BinomialHeapNode tmp = temp2;
				temp2 = temp2.sibling;
				tmp.sibling = temp1.sibling;
				temp1.sibling = tmp;
				temp1 = tmp.sibling;
			} else {
				if (temp1.degree < temp2.degree) {
					if ((temp1.sibling == null) || (temp1.sibling.degree > temp2.degree)) {
						BinomialHeapNode tmp = temp2;
						temp2 = temp2.sibling;
						tmp.sibling = temp1.sibling;
						temp1.sibling = tmp;
						temp1 = tmp.sibling;
					} else {
						temp1 = temp1.sibling;
					}
				} else {
					BinomialHeapNode tmp = temp1;
					temp1 = temp2;
					temp2 = temp2.sibling;
					temp1.sibling = tmp;
					if (tmp == Nodes) {
						Nodes = temp1;
					}
				}
			}
		}

		if (temp1 == null) {
			temp1 = Nodes;
			while (temp1.sibling != null) {
				temp1 = temp1.sibling;
			}
			temp1.sibling = temp2;
		}

	}

	// another helper procedure
	private void unionNodes(/* @ nullable @ */BinomialHeapNode binHeap) {
		merge(binHeap);

		BinomialHeapNode prevTemp = null, temp = Nodes, nextTemp = Nodes.sibling;

		while (nextTemp != null) {
			if ((temp.degree != nextTemp.degree)
					|| ((nextTemp.sibling != null) && (nextTemp.sibling.degree == temp.degree))) {
				prevTemp = temp;
				temp = nextTemp;
			} else {
				if (temp.key <= nextTemp.key) {
					temp.sibling = nextTemp.sibling;
					nextTemp.parent = temp;
					nextTemp.sibling = temp.child;
					temp.child = nextTemp;
					temp.degree++;
				} else {
					if (prevTemp == null) {
						Nodes = nextTemp;
					} else {
						prevTemp.sibling = nextTemp;
					}
					temp.parent = nextTemp;
					temp.sibling = nextTemp.child;
					nextTemp.child = temp;
					nextTemp.degree++;
					temp = nextTemp;
				}
			}

			nextTemp = temp.sibling;
		}
	}

	// 4. Insert a node with a specific value
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures some n: BinomialHeapNode | ( n !in @old(this.nodes) && this.nodes
	 *          = @old(this.nodes) @+ n && n.key = value ) ;
	 */
	public void insert(int value) {
		if (value > 0) {
			BinomialHeapNode temp = new BinomialHeapNode(value);
			if (Nodes == null) {
				Nodes = temp;
				size = 1;
			} else {
				unionNodes(temp);
				size++;
			}
		}
	}

	// 5. Extract the node with the minimum key
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures ( @old(this).@old(Nodes)==null => ( this.Nodes==null && return==null ) ) &&
	 *          ( @old(this).@old(Nodes)!=null => ( (return in @old(this.nodes)) && ( all y :
	 *          BinomialHeapNode | ( y in @old(this.nodes.key) && y.key >= return.key ) ) &&
	 *          (this.nodes=@old(this.nodes) @- return ) && (this.nodes.key @+ return.key
	 *          = @old(this.nodes.key) ) ));
	 */
	public/* @ nullable @ */BinomialHeapNode extractMin() {
		if (Nodes == null)
			return null;

		BinomialHeapNode temp = Nodes, prevTemp = null;
		BinomialHeapNode minNode = null;

		minNode = Nodes.findMinNode();
		while (temp.key != minNode.key) {
			prevTemp = temp;
			temp = temp.sibling;
		}

		if (prevTemp == null) {
			Nodes = temp.sibling;
		} else {
			prevTemp.sibling = temp.sibling;
		}
		temp = temp.child;
		BinomialHeapNode fakeNode = temp;
		while (temp != null) {
			temp.parent = null;
			temp = temp.sibling;
		}

		if ((Nodes == null) && (fakeNode == null)) {
			size = 0;
		} else {
			if ((Nodes == null) && (fakeNode != null)) {
				Nodes = fakeNode.reverse(null);
				size--;
			} else {
				if ((Nodes != null) && (fakeNode == null)) {
					// FIX
					size = Nodes.getSize();
					// BUG
					//size--;
				} else {
					unionNodes(fakeNode.reverse(null));
					// FIX
					size = Nodes.getSize();
					// BUG
					//size--;
				}
			}
		}

		return minNode;
	}

	// 6. Decrease a key value
	private void decreaseKeyValue(int old_value, int new_value) {
		BinomialHeapNode temp = Nodes.findANodeWithKey(old_value);
		decreaseKeyNode(temp, new_value);
	}

	/**
	 * 
	 * @Modifies_Everything
	 * 
	 * @Requires node in this.nodes && node.key >= new_value ;
	 * 
	 * @Ensures (some other: BinomialHeapNode | other in this.nodes && other!=node
	 *          && @old(other.key)=@old(node.key)) ? this.nodes.key = @old(this.nodes.key) @+
	 *          new_value : this.nodes.key = @old(this.nodes.key) @- @old(node.key) @+ new_value ;
	 */
	private void decreaseKeyNode(final BinomialHeapNode node, final int new_value) {
		if (node == null)
			return;

		BinomialHeapNode y = node;
		y.key = new_value;
		BinomialHeapNode z = node.parent;

		while ((z != null) && (node.key < z.key)) {
			int z_key = y.key;
			y.key = z.key;
			z.key = z_key;

			y = z;
			z = z.parent;
		}
	}

	// 7. Delete a node with a certain key
	private void delete(int value) {
		if ((Nodes != null) && (Nodes.findANodeWithKey(value) != null)) {
			decreaseKeyValue(value, findMinimum() - 1);
			extractMin();
		}
	}

	private static final int LIMIT = 30;

	/*
	public static void main(String[] args) {
		BinomialHeap X = new BinomialHeap();
		X = (BinomialHeap) Debug.makeSymbolicRefBounded("N", X);
		if (X != null) {
			if (X.repOK_Concrete()) {
				X.genPositiveVectors();
			} else {
				X.genNegativeVectors();
			}
		}
	}
	*/

	/*
	 * public static void main(String[] args) { BinomialHeap X = new BinomialHeap(); // X =
	 * (BinomialHeap) Debug.makeSymbolicRef("X", X); X = (BinomialHeap)
	 * Debug.makeSymbolicRefBounded("X", X); try { BinomialHeapNode n = X.extractMin(); if (X != null)
	 * { X.countStructure(X.Nodes); } } catch (Throwable t) { // ignored! } }
	 */

}