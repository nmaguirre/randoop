/**
 * 
 */
package randoop.test.datastructures.binheap;

import java.util.Set;


public class BinomialHeapNode {

	int _index;
	
	// int key; // element in current node
	
	int key;

	int degree; // depth of the binomial tree having the current node as its root

	/*@ nullable @*/BinomialHeapNode parent; // pointer to the parent of the current node

	/*@ nullable @*/BinomialHeapNode sibling; // pointer to the next binomial tree in the list

	/*@ nullable @*/BinomialHeapNode child; // pointer to the first child of the current node

	public BinomialHeapNode() {
	}	
	
	public BinomialHeapNode(int k) {
		//	public BinomialHeapNode(Integer k) {
		key = k;
		degree = 0;
		parent = null;
		sibling = null;
		child = null;
	}

	public int getKey() { // returns the element in the current node
		return key;
	}

	private void setKey(int value) { // sets the element in the current node
		key = value;
	}

	public int getDegree() { // returns the degree of the current node
		return degree;
	}

	private void setDegree(int deg) { // sets the degree of the current node
		degree = deg;
	}

	public BinomialHeapNode getParent() { // returns the father of the current node
		return parent;
	}

	private void setParent(BinomialHeapNode par) { // sets the father of the current node
		parent = par;
	}

	public BinomialHeapNode getSibling() { // returns the next binomial tree in the list
		return sibling;
	}

	private void setSibling(BinomialHeapNode nextBr) { // sets the next binomial tree in the list
		sibling = nextBr;
	}

	public BinomialHeapNode getChild() { // returns the first child of the current node
		return child;
	}

	private void setChild(BinomialHeapNode firstCh) { // sets the first child of the current node
		child = firstCh;
	}

	public int getSize() {
		return (1 + ((child == null) ? 0 : child.getSize()) + ((sibling == null) ? 0
				: sibling.getSize()));
	}

	BinomialHeapNode reverse(BinomialHeapNode sibl) {
		BinomialHeapNode ret;
		if (sibling != null)
			ret = sibling.reverse(this);
		else
			ret = this;
		sibling = sibl;
		return ret;
	}


	BinomialHeapNode findMinNode() {
		BinomialHeapNode x = this, y = this;
		int min = x.key;

		while (x != null) {
			if (x.key < min) {
				y = x;
				min = x.key;
			}
			x = x.sibling;
		}

		return y;
	}

	// Find a node with the given key
	BinomialHeapNode findANodeWithKey(int value) {
		BinomialHeapNode temp = this, node = null;
		while (temp != null) {
			if (temp.key == value) {
				node = temp;
				return node;
			}
			if (temp.child == null)
				temp = temp.sibling;
			else {
				node = temp.child.findANodeWithKey(value);
				if (node == null)
					temp = temp.sibling;
				else
					return node;
			}
		}

		return node;
	}

	
//	public boolean checkDegree(int degree) {
//		for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
//			degree--;
//			if (current.degree != degree)
//				return false;
//			if (!current.checkDegree(degree))
//				return false;
//		}
//		return (degree == 0);
//	}
//
//	public boolean isHeapified() {
//		for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
//			if (!(key <= current.key))
//				return false;
//			if (!current.isHeapified())
//				return false;
//		}
//		return true;
//	}
//
//	public boolean isTree(java.util.Set<BinomialHeapNode> visited,
//			BinomialHeapNode parent) {
//		if (this.parent != parent)
//			return false;
//		for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
//			if (!visited.add(current))
//				return false;
//			if (!current.isTree(visited, this))
//				return false;
//		}
//		return true;
//	}

	
	/*
	public void printHeap() {
		for (BinomialHeapNode current = this; current != null; current = current.sibling) {
			System.out.println(" " + current.key + " ");
			if (current.child != null) current.child.printHeap();
		}
	}
	*/
	
	public String printHeap() {
		String res = "";
		for (BinomialHeapNode current = this; current != null; current = current.sibling) {
			res += " " + current.key + " ";
			if (current.child != null) res += current.child.printHeap();
		}
		return res;
	}

	public String toString() {
		return printHeap();
	}
		
	
	
	// Invoke with accum = 1
	public int hash(int accum) {
		for (BinomialHeapNode current = this; current != null; current = current.sibling) {
			accum = 31*accum + current.key;
			if (current.child != null) accum = current.child.hash(accum);
		}
		return accum;
	}
	
	
	
	
/*	
    // procedures used by Korat
    private boolean repCheckWithRepetitions(int key_, int degree_,
            Object parent_, HashSet<BinomialHeapNode> nodesSet) {

        BinomialHeapNode temp = this;

        int rightDegree = 0;
        if (parent_ == null) {
            while ((degree_ & 1) == 0) {
                rightDegree += 1;
                degree_ /= 2;
            }
            degree_ /= 2;
        } else
            rightDegree = degree_;

        while (temp != null) {
            if ((temp.degree != rightDegree) || (temp.parent != parent_)
                    || (temp.key < key_) || (nodesSet.contains(temp)))
                return false;
            else {
                nodesSet.add(temp);
                if (temp.child == null) {
                    temp = temp.sibling;

                    if (parent_ == null) {
                        if (degree_ == 0)
                            return (temp == null);
                        while ((degree_ & 1) == 0) {
                            rightDegree += 1;
                            degree_ /= 2;
                        }
                        degree_ /= 2;
                        rightDegree++;
                    } else
                        rightDegree--;
                } else {
                    boolean b = temp.child.repCheckWithRepetitions(
                            temp.key, temp.degree - 1, temp, nodesSet);
                    if (!b)
                        return false;
                    else {
                        temp = temp.sibling;

                        if (parent_ == null) {
                            if (degree_ == 0)
                                return (temp == null);
                            while ((degree_ & 1) == 0) {
                                rightDegree += 1;
                                degree_ /= 2;
                            }
                            degree_ /= 2;
                            rightDegree++;
                        } else
                            rightDegree--;
                    }
                }
            }
        }

        return true;
    }

    private boolean repCheckWithoutRepetitions(int key_,
            HashSet<Integer> keysSet, int degree_, // equal keys not allowed
            Object parent_, HashSet<BinomialHeapNode> nodesSet) {
        BinomialHeapNode temp = this;

        int rightDegree = 0;
        if (parent_ == null) {
            while ((degree_ & 1) == 0) {
                rightDegree += 1;
                degree_ /= 2;
            }
            degree_ /= 2;
        } else
            rightDegree = degree_;

        while (temp != null) {
            if ((temp.degree != rightDegree) || (temp.parent != parent_)
                    || (temp.key <= key_) || (nodesSet.contains(temp))
                    || (keysSet.contains(new Integer(temp.key)))) {
                return false;
            } else {
                nodesSet.add(temp);
                keysSet.add(new Integer(temp.key));
                if (temp.child == null) {
                    temp = temp.sibling;

                    if (parent_ == null) {
                        if (degree_ == 0)
                            return (temp == null);
                        while ((degree_ & 1) == 0) {
                            rightDegree += 1;
                            degree_ /= 2;
                        }
                        degree_ /= 2;
                        rightDegree++;
                    } else
                        rightDegree--;
                } else {
                    boolean b = temp.child.repCheckWithoutRepetitions(
                            temp.key, keysSet, temp.degree - 1, temp,
                            nodesSet);
                    if (!b)
                        return false;
                    else {
                        temp = temp.sibling;

                        if (parent_ == null) {
                            if (degree_ == 0)
                                return (temp == null);
                            while ((degree_ & 1) == 0) {
                                rightDegree += 1;
                                degree_ /= 2;
                            }
                            degree_ /= 2;
                            rightDegree++;
                        } else
                            rightDegree--;
                    }
                }
            }
        }

        return true;
    }
*/
//    public boolean repOk(int size) {
//        // replace 'repCheckWithoutRepetitions' with
//        // 'repCheckWithRepetitions' if you don't want to allow equal keys
//        return repCheckWithRepetitions(0, size, null,
//                new HashSet<BinomialHeapNode>());
//    }

    boolean checkDegree(int degree) {
        for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
            degree--;
            if (current.degree != degree)
                return false;
            if (!current.checkDegree(degree))
                return false;
        }
        return (degree == 0);
    }

    boolean isHeapified() {
        for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
            if (!(key <= current.key))
                return false;
            if (!current.isHeapified())
                return false;
        }
        return true;
    }

    boolean isTree(Set<NodeWrapper> visited,
            BinomialHeapNode parent) {
        if (this.parent != parent)
            return false;
        for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
            if (!visited.add(new NodeWrapper(current)))
                return false;
            if (!current.isTree(visited, this))
                return false;
        }
        return true;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinomialHeapNode other = (BinomialHeapNode) obj;
		if (key != other.key)
			return false;
		return true;
	}

        

}
	
	
	
