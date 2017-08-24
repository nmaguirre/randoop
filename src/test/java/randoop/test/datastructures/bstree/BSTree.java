//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package randoop.test.datastructures.bstree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import randoop.CheckRep;


/**
 * 
 * @Invariant all n : Node | n in this.root.*(left @+ right ) => 
 *                      (
 *                         ( n !in n.^(left @+ right) ) &&
 *                         ( all m: Node | m in n.left.*(left @+ right) => m.key < n.key ) &&
 *                         ( all m: Node | m in n.right.*(left @+ right) => n.key < m.key )
 *                      ) ;
 *                      
 * @SpecField nodes : set Node from this.root, this.root.left, this.root.right  | 
 *                    this.nodes = this.root.*(left @+ right) @- null ;
 *
 */
public class BSTree{

	private Node root;
	
	private int size;

	public BSTree() {
		root = null;
		size = 0;
	}


	private Node newNode(int n) {
		Node res = new Node();
		res.key = n;
		res.left = null;
		res.right = null;
		return res;
	}
	
	/**
	 * @Modifies_Everything
	 * @Ensures some n: Node | @old(this.nodes) @+ n = this.nodes && n.key==x ;
	 */
	public void add(int x) {
		Node current = root;

		if (root == null) {
			root = newNode(x);
			size++;
			return;
		}

		while (current.key != x) {
			if (x < current.key) {
				if (current.left == null) {
					current.left = newNode(x);
					size++;
				} else {
					current = current.left;
				}
			} else {
				if (current.right == null) {
					current.right = newNode(x);
					size++;
				} else {
					current = current.right;
				}
			}
		}
	}

	/**
	 * @Modifies_Everything
	 * @Ensures return==true <=> (some n: Node | n in this.nodes && n.key == x ) ; 
	 */
	public /*@ pure @*/ boolean find(int x) {
		Node current = root;

		while (current != null) {

			if (current.key == x) {
				return true;
			}

			if (x < current.key) {
				current = current.left;
			} else {
				current = current.right;
			}
		}

		return false;
	}

	
	public void remove(int key) {
	    root = removeNode(root, key);
	}
	
	private Node removeNode(Node n, int key) {
	    if (n == null) {
	        return null;
	    }
	    
	    if (key ==  n.key) {
	        // n is the node to be removed
	        if (n.left == null && n.right == null) {
	        	size--;
	            return null;
	        }
	        if (n.left == null) {
	        	size--;
	            return n.right;
	        }
	        if (n.right == null) {
	        	size--;
	        	//size--;
	            return n.left;
	        }
	        int smallVal = smallest(n.right);
	        n.key = smallVal;
	        n.right = removeNode(n.right, smallVal);
	        return n; 
	    }
	    else if (key < n.key) {
	        n.left = removeNode(n.left, key);
	        return n;
	    }
	    else {
	        n.right = removeNode(n.right, key);
	        return n;
	    }
	}
	
	
	private int smallest(Node n)
	{
	    if (n.left == null) {
	        return n.key;
	    } else {
	        return smallest(n.left);
	    }
	}
	
	
	
	
	/**
	 * @Modifies_Everything
	 * @Ensures all n: Node | (n in this.nodes <=> (n in @old(this.nodes) && n.key!=x))
	 */
/*	
	public boolean remove(int x) {
		Node current = root;
		Node parent = null;
		boolean branch = true; //true =left, false =right

		while (current != null) {

			if (current.key == x) {
				Node bigson = current;
				while (bigson.left != null || bigson.right != null) {
					parent = bigson;
					if (bigson.right != null) {
						bigson = bigson.right;
						branch = false;
					} else {
						bigson = bigson.left;
						branch = true;
					}
				}

				if (parent != null) {
					if (branch) {
						parent.left = null;
					} else {
						parent.right = null;
					}
				}

				if (bigson != current) {
					current.key = bigson.key;
				} else {
				}
				
				size--;
				return true;
			}

			parent = current;

			if (current.key > x) {
				current = current.left;
				branch = true;
			} else {
				current = current.right;
				branch = false;
			}
		}

		return false;
	}
	*/
	
	
	@CheckRep
    public boolean repOK() {
        // checks that empty tree has size zero
        if (root == null)
            //return true;
        	return size == 0;
        // checks that the input is a tree
        if (!isAcyclic())
            return false;
        // checks that size is consistent
        if (numNodes(root) != size)
            return false;
        // checks that data is ordered
        if (!isOrdered(root))
            return false; 
        return true;
    }

    private boolean isAcyclic() {
        Set visited = new HashSet();
        visited.add(root);
        LinkedList workList = new LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();
            if (current.left != null) {
                // checks that the tree has no cycle
                if (!visited.add(current.left))
                    return false;
                workList.add(current.left);
            }
            if (current.right != null) {
                // checks that the tree has no cycle
                if (!visited.add(current.right))
                    return false;
                workList.add(current.right);
            }
        }
        return true;
    }

    private int numNodes(Node n) {
        if (n == null)
            return 0;
        return 1 + numNodes(n.left) + numNodes(n.right);
    }

    private boolean isOrdered(Node n) {
        return isOrdered(n, null, null);
    }

    private boolean isOrdered(Node n, Integer min, Integer max) {
        // if (n.key == null)
        // return false;
        /*if (n.key == -1)
            return false;*/
        // if ((min != null && n.key.compareTo(min) <= 0)
        // || (max != null && n.key.compareTo(max) >= 0))
        if ((min != null && n.key <= (min)) || (max != null && n.key >= (max)))
            return false;
        if (n.left != null)
            if (!isOrdered(n.left, min, n.key))
                return false;
        if (n.right != null)
            if (!isOrdered(n.right, n.key, max))
                return false;
        return true;
    }
    
    

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if (root != null)
            buf.append(root.toString());
        buf.append("}");
        return buf.toString();
    }
   
	
	public BSTree deepcopy() {
	
		LinkedList<Node> visited = new LinkedList<Node>();
		ArrayList<Node> nodes = new ArrayList<Node>(size);		
		ArrayList<Node> newnodes = new ArrayList<Node>(size);
		
		int ind = 0;
		
		if (root == null) {
			BSTree res = new BSTree();
			res.root = null;
			res.size = size;
			return res;
		}
		else {
			visited.add(root);
		
			while (!visited.isEmpty()) {
				Node currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.left != null) 
					visited.add(currNode.left);
				if (currNode.right != null) 
					visited.add(currNode.right);
			}
			
			for (int i=0; i<nodes.size();i++) {
				Node newnode = new Node();
				newnodes.add(newnode);
			}
	
			for (int i=0; i<nodes.size();i++) {
				Node currnode = nodes.get(i);
				Node newnode = newnodes.get(i);
				
				newnode.key = currnode.key;
				
				if (currnode.left != null)
					newnode.left = newnodes.get(currnode.left._index);
				else
					newnode.left = null;
	
				if (currnode.right != null)
					newnode.right = newnodes.get(currnode.right._index);
				else
					newnode.right = null;
				
			}
			BSTree res = new BSTree();
			res.root = newnodes.get(root._index);
			res.size = size;
			return res;

		}
		
	}
	
	
	public String toJava() {
		
		LinkedList<Node> visited = new LinkedList<Node>();
		ArrayList<Node> nodes = new ArrayList<Node>(size);		
		String res;
		
		res = "BSTree S0 = (BSTree) BSTree.class.newInstance();\n";
		res += "Field rootF = BSTree.class.getDeclaredField(\"root\");\n";
		res += "rootF.setAccessible(true);\n";
		res += "Field sizeF = BSTree.class.getDeclaredField(\"size\");\n";
		res += "sizeF.setAccessible(true);\n";
		res += "Field rightF = Node.class.getDeclaredField(\"right\");\n";
		res += "rightF.setAccessible(true);\n";
		res += "Field leftF = Node.class.getDeclaredField(\"left\");\n";
		res += "leftF.setAccessible(true);\n";
		res += "Field keyF = Node.class.getDeclaredField(\"key\");\n";
		res += "keyF.setAccessible(true);\n\n";

		int ind = 0;
		
		if (root != null) {
			visited.add(root);
		
			while (!visited.isEmpty()) {
				Node currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.left != null) 
					visited.add(currNode.left);
				if (currNode.right != null) 
					visited.add(currNode.right);
			}
			
			for (int i=0; i<nodes.size();i++) {
				res += "Node N" + nodes.get(i)._index + " = (Node) Node.class.newInstance();\n";
			}

			res += "\n";
			
			for (int i=0; i<nodes.size();i++) {
				Node currnode = nodes.get(i);
			
				res += "keyF.set(N" + currnode._index + ", " + currnode.key + ");\n";
				
				if (currnode.left != null)
					res += "leftF.set(N" + currnode._index + ", N" + currnode.left._index + ");\n"; 
				else
					res += "leftF.set(N" + currnode._index + ", null);\n";
	
				if (currnode.right != null)
					res += "rightF.set(N" + currnode._index + ", N" + currnode.right._index + ");\n"; 
				else
					res += "rightF.set(N" + currnode._index + ", null);\n";

				res += "\n";
			}
		}

		res += "\n";
		
		if (root == null)
			res += "rootF.set(S0, null);\n";
		else 
			res += "rootF.set(S0, " + "N" + root._index + ");\n";
		res += "sizeF.set(S0, " + this.size + ");\n";
		
		res += "return S0;\n";
		return res;
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
		BSTree other = (BSTree) obj;
		
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
		
		BSTree tree = new BSTree();
		tree.add(1);
		tree.add(2);
		tree.add(3);
		tree.add(0);
		System.out.println(tree.toString());

		
		BSTree copy = tree.deepcopy();
		tree.remove(0);
		System.out.println(tree.toString());
		
		System.out.println(copy.toString());
		copy.add(5);
		System.out.println(copy.toString());
		System.out.println(tree.toString());
	}
	*/
	
	
	
	
	
	
	
	

}
