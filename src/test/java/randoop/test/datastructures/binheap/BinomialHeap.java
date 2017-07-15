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
package randoop.test.datastructures.binheap;


//import rfm.testingtool.structures.binheap.BinomialHeapNode.NodeWrapper;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;



//import rfm.testingtool.structures.binheap.BinomialHeapNode;


/**
 * @SpecField nodes: set BinomialHeapNode from this.Nodes, this.nodes.child, this.nodes.sibling, this.nodes.parent  
 *                   | this.nodes = this.Nodes.*(child @+ sibling) @- null ;
 */
/**
 * @Invariant ( all n: BinomialHeapNode | ( n in this.Nodes.*(sibling @+ child) @- null => (
 *		            ( n.parent!=null  => n.key >=  n.parent.key )  &&   
 *		            ( n.child!=null   => n !in n.child.*(sibling @+ child) @- null ) && 
 *		            ( n.sibling!=null => n !in n.sibling.*(sibling @+ child) @- null ) && 
 *		            ( ( n.child !=null && n.sibling!=null ) => (no m: BinomialHeapNode | ( m in n.child.*(child @+ sibling) @- null && m in n.sibling.*(child @+ sibling) @- null )) ) && 
 *		            ( n.degree >= 0 ) && 
 *		            ( n.child=null => n.degree = 0 ) && 
 *		            ( n.child!=null =>n.degree=#(n.child.*sibling @- null) )  && 
 *		            ( #( ( n.child @+ n.child.child.*(child @+ sibling) ) @- null ) = #( ( n.child @+ n.child.sibling.*(child @+ sibling)) @- null )  ) && 
 *		            ( n.child!=null => ( all m: BinomialHeapNode | ( m in n.child.*sibling@-null =>  m.parent = n  ) ) ) && 
 *		            ( ( n.sibling!=null && n.parent!=null ) => ( n.degree > n.sibling.degree ) )
 * ))) && 
 * ( this.size = #(this.Nodes.*(sibling @+ child) @- null) ) &&
 * ( all n: BinomialHeapNode | n in this.Nodes.*sibling @- null => ( 
 *  ( n.sibling!=null => n.degree < n.sibling.degree ) && 
 *  ( n.parent=null ) 
 *  )) ;
 */
public class BinomialHeap {

	private /*@ nullable @*/ BinomialHeapNode Nodes;

	private int size;

	public BinomialHeap() {
		Nodes = null;
		size = 0;
	}

	// 2. Find the minimum key
	/**
	 * @Modifies_Everything
	 * 
	 * @Requires some this.nodes ; 
	 * @Ensures ( some x: BinomialHeapNode | x in this.nodes && x.key == return ) && 
	 *          ( all y : BinomialHeapNode | ( y in this.nodes && y!=return ) => return <= y.key ) ;  
	 */
	public int findMinimum() {
		if (Nodes == null) return -1;
		return Nodes.findMinNode().key;
	}

	// 3. Unite two binomial heaps
	// helper procedure
	private void merge(/*@ nullable @*/BinomialHeapNode binHeap) {
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
					if ((temp1.sibling == null)
							|| (temp1.sibling.degree > temp2.degree)) {
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
	private void unionNodes(/*@ nullable @*/BinomialHeapNode binHeap) {
		merge(binHeap);

		BinomialHeapNode prevTemp = null, temp = Nodes , nextTemp = Nodes.sibling;
		
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
	 * @Ensures some n: BinomialHeapNode | (
	 *            n !in @old(this.nodes) &&
	 *            this.nodes = @old(this.nodes) @+ n &&
	 *            n.key = value ) ;
	 */
	public void insert(int value) {
			BinomialHeapNode temp = new BinomialHeapNode(value);
			if (Nodes == null) {
				Nodes = temp;
				size = 1;
			} else {
				unionNodes(temp);
				size++;
			}
	}

	// 5. Extract the node with the minimum key
	/**
	 * @Modifies_Everything
	 * 
	 * @Ensures ( @old(this).@old(Nodes)==null => ( this.Nodes = null && return = null ) ) 
	 *       && ( @old(this).@old(Nodes)!=null => ( (return in @old(this.nodes)) &&
	 *                                              ( all y : BinomialHeapNode | ( y in @old(this.nodes.key) && y.key >= return.key ) ) && 
	 *                                              (this.nodes = @old(this.nodes) @- return ) &&
	 *                                              (this.nodes.key  @+ return.key = @old(this.nodes.key) )
	 *                                             ));
	 */
	public /*@ nullable @*/BinomialHeapNode extractMin() {
		if (Nodes == null)
			return null;

		BinomialHeapNode temp = Nodes, prevTemp = null;
		BinomialHeapNode minNode = Nodes.findMinNode();
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
				// FIX
				size = Nodes.getSize();
				// BUG
				// size--;
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
	public void decreaseKeyValue(int old_value, int new_value) {
		if (Nodes != null) {
			BinomialHeapNode temp = Nodes.findANodeWithKey(old_value);
			decreaseKeyNode(temp, new_value);
		}
	}

	/**
	 * 
	 * @Modifies_Everything
	 * 
	 * @Requires node in this.nodes && node.key >= new_value ;
	 * 
	 * @Ensures (some other: BinomialHeapNode | other in this.nodes && other!=node && @old(other.key)=@old(node.key))
	 *          ? this.nodes.key = @old(this.nodes.key) @+ new_value
	 *          : this.nodes.key = @old(this.nodes.key) @- @old(node.key) @+ new_value ;  
	 */
	public void decreaseKeyNode(BinomialHeapNode node, int new_value) {
		if (node == null)
			return;
		node.key = new_value;
		BinomialHeapNode tempParent = node.parent;

		while ((tempParent != null) && (node.key < tempParent.key)) {
			int z = node.key;
			node.key = tempParent.key;
			tempParent.key = z;

			node = tempParent;
			tempParent = tempParent.parent;
		}
	}

	// 7. Delete a node with a certain key
	public void delete(int value) {
		if ((Nodes != null) && (Nodes.findANodeWithKey(value) != null)) {
			decreaseKeyValue(value, findMinimum() - 1);
			extractMin();
		}
	}
	 
	
    /*
	public void delete(int value) {
		if ((Nodes != null) && (Nodes.findANodeWithKey(value) != null)) {
			int min = findMinimum();
			if (min > Integer.MIN_VALUE) min--;
			decreaseKeyValue(value, min);
			extractMin();
		}
	}*/
	

    public String toString() {
        if (Nodes == null)
            return size + "\n()\n";
        else
            return size + "\n" + Nodes.toString();
    }
    
    public void printHeap() {
        if (Nodes == null)
            System.out.println(size + "\n()\n");
        else
        	Nodes.printHeap();
        	System.out.println(size + "\n()\n");
    }



    boolean checkDegrees() {
        int degree_ = size;
        int rightDegree = 0;
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (degree_ == 0)
                return false;
            while ((degree_ & 1) == 0) {
                rightDegree++;
                // degree_ /= 2;
                degree_ = degree_ / 2;
            }
            if (current.degree != rightDegree)
                return false;
            if (!current.checkDegree(rightDegree))
                return false;
            rightDegree++;
            //degree_ /= 2;
            degree_ = degree_ / 2;
        }
        return (degree_ == 0);
    }

    boolean checkHeapified() {
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (!current.isHeapified())
                return false;
        }
        return true;
    }

    public boolean repOK() {
        if (size == 0)
            return (Nodes == null);
        if (Nodes == null)
        	return false;
        // checks that list of trees has no cycles
        Set<NodeWrapper> visited = new HashSet<NodeWrapper>();
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            // checks that the list has no cycle
            if (!visited.add(new NodeWrapper(current)))
                return false;
            if (!current.isTree(visited, null))
                return false;
        }
        // checks that the total size is consistent
        if (visited.size() != size)
            return false;
        // checks that the degrees of all trees are binomial
        if (!checkDegrees())
            return false;
        // checks that keys are heapified
        if (!checkHeapified())
            return false;
        return true;
    }
    
  

	
	
	public BinomialHeap deepcopy() {
		
		LinkedList<BinomialHeapNode> visited = new LinkedList<BinomialHeapNode>();
		ArrayList<BinomialHeapNode> nodes = new ArrayList<BinomialHeapNode>(size);		
		ArrayList<BinomialHeapNode> newnodes = new ArrayList<BinomialHeapNode>(size);
		
		int ind = 0;
		
		if (Nodes == null) {
			BinomialHeap res = new BinomialHeap();
			res.Nodes = null;
			res.size = size;
			return res;
		}
		else {
			visited.add(Nodes);
		
			while (!visited.isEmpty()) {
				BinomialHeapNode currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.child != null) 
					visited.add(currNode.child);
				if (currNode.sibling != null) 
					visited.add(currNode.sibling);
			}
			
			for (int i=0; i<nodes.size();i++) {
				BinomialHeapNode newnode = new BinomialHeapNode();
				newnodes.add(newnode);
			}
	
			for (int i=0; i<nodes.size();i++) {
				BinomialHeapNode currnode = nodes.get(i);
				BinomialHeapNode newnode = newnodes.get(i);

				newnode.key = currnode.key;
				newnode.degree = currnode.degree;
				
				if (currnode.child != null)
					newnode.child = newnodes.get(currnode.child._index);
				else
					newnode.child = null;
	
				if (currnode.sibling != null)
					newnode.sibling = newnodes.get(currnode.sibling._index);
				else
					newnode.sibling = null;
				
				if (currnode.parent != null)
					newnode.parent = newnodes.get(currnode.parent._index);
				else
					newnode.parent = null;
				
			}
			
			BinomialHeap res = new BinomialHeap();
			res.Nodes = newnodes.get(Nodes._index);
			res.size = size;
			return res;

		}	
	}
	
	
	/*
	private String nodeToString(BinomialHeapNode n) {
		if (n == null) {
			return "null";
		}
		else {
			return "N" + n._index;
		}
	}
	
	public void writeToFile(Path filename) throws IOException {
		
		LinkedList<BinomialHeapNode> visited = new LinkedList<BinomialHeapNode>();
		ArrayList<BinomialHeapNode> nodes = new ArrayList<BinomialHeapNode>(size);
		ArrayList<BinomialHeapNode> newnodes = new ArrayList<BinomialHeapNode>(size);
		
		LinkedList<String> element = new LinkedList<String>();
		LinkedList<String> degree = new LinkedList<String>();
		LinkedList<String> sibling = new LinkedList<String>();
		LinkedList<String> child = new LinkedList<String>();
		LinkedList<String> parent = new LinkedList<String>();
		
		int ind = 0;
		
		if (Nodes == null) {
			BinomialHeap res = new BinomialHeap();
			res.Nodes = null;
			res.size = size;
		}
		else {
			visited.add(Nodes);
		
			while (!visited.isEmpty()) {
				BinomialHeapNode currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.child != null) 
					visited.add(currNode.child);
				if (currNode.sibling != null) 
					visited.add(currNode.sibling);
			}
			
			for (int i=0; i<nodes.size();i++) {
				BinomialHeapNode newnode = new BinomialHeapNode();
				newnodes.add(newnode);
			}
	
			for (int i=0; i<nodes.size();i++) {
				BinomialHeapNode currnode = nodes.get(i);
				BinomialHeapNode newnode = newnodes.get(i);

				newnode.key = currnode.key;
				newnode.degree = currnode.degree;
				
				if (currnode.child != null)
					newnode.child = newnodes.get(currnode.child._index);
				else
					newnode.child = null;
	
				if (currnode.sibling != null)
					newnode.sibling = newnodes.get(currnode.sibling._index);
				else
					newnode.sibling = null;
				
				if (currnode.parent != null)
					newnode.parent = newnodes.get(currnode.parent._index);
				else
					newnode.parent = null;
				
			}
			
			BinomialHeap res = new BinomialHeap();
			res.Nodes = newnodes.get(Nodes._index);
			res.size = size;

			BufferedWriter b = Files.newBufferedWriter(filename, Charset.defaultCharset(), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
			
			b.write("nodes=");
			for (int i = 0; i < nodes.size(); i++) {
				b.write(nodeToString(nodes.get(i)));
				if (i < nodes.size()-1) {
					b.write(",");
				}
			}
			b.write("\n");
			
			b.write("QF.head_0=BinHeapIntVar:" + nodeToString(Nodes));
			b.write("\n");
			
			b.write("QF.size_0=BinHeapIntVar:" + size);
			b.write("\n");
			
			b.write("element=");
			for (int i = 0; i < nodes.size(); i++) {
				b.write(nodeToString(nodes.get(i)));
				if (i < nodes.size()-1) {
					b.write(",");
				}
			}
			b.write("\n");		
			
			
			b.write("degree=");
			
			
			b.write("fsibling=");
			b.write("bsibling=");
			
			
			b.write("fchild=");
			b.write("bchild=");
			
			
			b.write("fparent=");
			b.write("bparent=");
			
			b.close();
		}	
	}
	*/
	
	
	
	
	
	public String toJava() {
		
		LinkedList<BinomialHeapNode> visited = new LinkedList<BinomialHeapNode>();
		ArrayList<BinomialHeapNode> nodes = new ArrayList<BinomialHeapNode>(size);
		String res;
		
		res = "BinomialHeap S0 = (BinomialHeap) BinomialHeap.class.newInstance();\n";
		
		res += "Field degreeF = BinomialHeapNode.class.getDeclaredField(\"degree\");\n";
		res += "degreeF.setAccessible(true);\n";
		
		res += "Field NodesF = BinomialHeap.class.getDeclaredField(\"Nodes\");\n";
		res += "NodesF.setAccessible(true);\n";
		
		res += "Field parentF = BinomialHeapNode.class.getDeclaredField(\"parent\");\n";
		res += "parentF.setAccessible(true);\n";

		res += "Field keyF = BinomialHeapNode.class.getDeclaredField(\"key\");\n";
		res += "keyF.setAccessible(true);\n";
	
		res += "Field childF = BinomialHeapNode.class.getDeclaredField(\"child\");\n";
		res += "childF.setAccessible(true);\n";	

		res += "Field sizeF = BinomialHeap.class.getDeclaredField(\"size\");\n";
		res += "sizeF.setAccessible(true);\n";			

		res += "Field siblingF = BinomialHeapNode.class.getDeclaredField(\"sibling\");\n";
		res += "siblingF.setAccessible(true);\n";	
		
		int ind = 0;
		
		if (Nodes != null) {
			visited.add(Nodes);
		
			while (!visited.isEmpty()) {
				BinomialHeapNode currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.child != null) 
					visited.add(currNode.child);
				if (currNode.sibling != null) 
					visited.add(currNode.sibling);
			}
			
			for (int i=0; i<nodes.size();i++) {
				res += "BinomialHeapNode N" + nodes.get(i)._index + " = (BinomialHeapNode) BinomialHeapNode.class.newInstance();\n";
			}

			res += "\n";
			
			for (int i=0; i<nodes.size();i++) {
				BinomialHeapNode currnode = nodes.get(i);
			
				res += "keyF.set(N" + currnode._index + ", " + currnode.key + ");\n";
				res += "degreeF.set(N" + currnode._index + ", " + currnode.degree + ");\n";
				
				if (currnode.child != null)
					res += "childF.set(N" + currnode._index + ", N" + currnode.child._index + ");\n"; 
				else
					res += "childF.set(N" + currnode._index + ", null);\n";
	
				if (currnode.sibling != null)
					res += "siblingF.set(N" + currnode._index + ", N" + currnode.sibling._index + ");\n"; 
				else
					res += "siblingF.set(N" + currnode._index + ", null);\n";

				if (currnode.parent != null)
					res += "parentF.set(N" + currnode._index + ", N" + currnode.parent._index + ");\n"; 
				else
					res += "parentF.set(N" + currnode._index + ", null);\n";
				
				res += "\n";
			}
		}

		res += "\n";
		
		if (Nodes == null)
			res += "NodesF.set(S0, null);\n";
		else 
			res += "NodesF.set(S0, " + "N" + Nodes._index + ");\n";
		res += "sizeF.set(S0, " + this.size + ");\n";
		
		/*
		res += "return S0;\n";
		res += "}\n\n";
		*/
		return res;
	}


	
	
	
	
	
	
	/*
	public String getNamesAtRuntime() {
		String testname = new Object(){}.getClass().getEnclosingMethod().getName();
		String classname = this.getClass().getSimpleName();
		System.out.println(classname + "-" + testname);
		return "";
	}
	*/
	
	
	/*
	public static void main(String [] args) {
		Class c = new Object(){}.getClass();
		String name = c.getEnclosingMethod().getName();	
		BinomialHeap b = new BinomialHeap();
		b.getNamesAtRuntime();
	}
	/*
	
	
	
	
	
	
	
	
	
/*	
	public static void main(String [] args) {
	    BinomialHeap binomialHeap0 = new BinomialHeap();
	    binomialHeap0.delete(1);
	    binomialHeap0.delete(2);
	    boolean b0 = binomialHeap0.equals((Object)10.0d);
	    binomialHeap0.delete(2);
	    binomialHeap0.insert(1);
	    binomialHeap0.insert(4);
	    BinomialHeap binomialHeap1 = new BinomialHeap();
	    binomialHeap1.delete(1);
	    binomialHeap1.delete(2);
	    boolean b2 = binomialHeap1.equals((Object)false);
	    BinomialHeap binomialHeap2 = new BinomialHeap();
	    binomialHeap2.insert(3);
	    boolean b3 = binomialHeap2.equals((Object)(-1.0f));
	    binomialHeap2.insert(2);
	    boolean b4 = binomialHeap1.equals((Object)2);
	    BinomialHeapNode binomialHeapNode0 = binomialHeap1.extractMin();
	    binomialHeap1.delete(0);
	    BinomialHeap binomialHeap3 = new BinomialHeap();
	    binomialHeap3.delete(1);
	    BinomialHeap binomialHeap4 = new BinomialHeap();
	    binomialHeap4.delete(1);
	    binomialHeap4.delete(2);
	    binomialHeap4.delete(4);
	    boolean b5 = binomialHeap4.equals((Object)(short)100);
	    boolean b6 = binomialHeap3.equals((Object)binomialHeap4);
	    BinomialHeap binomialHeap5 = new BinomialHeap();
	    binomialHeap5.delete(1);
	    binomialHeap5.delete(2);
	    boolean b8 = binomialHeap5.equals((Object)false);
	    BinomialHeap binomialHeap6 = new BinomialHeap();
	    boolean b9 = binomialHeap5.equals((Object)binomialHeap6);
	    binomialHeap5.delete(4);
	    binomialHeap5.delete(4);
	    BinomialHeapNode binomialHeapNode1 = binomialHeap5.extractMin();
	    boolean b10 = binomialHeap3.equals((Object)binomialHeap5);
	    boolean b11 = binomialHeap1.equals((Object)binomialHeap5);
	    binomialHeap5.delete(1);
	    boolean b12 = binomialHeap0.equals((Object)binomialHeap5);
	    binomialHeap0.delete(3);
	    binomialHeap0.insert(4);
	    binomialHeap0.insert(1);
	    BinomialHeap binomialHeap7 = new BinomialHeap();
	    binomialHeap7.delete(1);
	    binomialHeap7.insert(4);
	    binomialHeap7.insert(4);
	    binomialHeap7.delete(1);
	    BinomialHeapNode binomialHeapNode2 = binomialHeap7.extractMin();
	    BinomialHeap binomialHeap8 = new BinomialHeap();
	    binomialHeap8.delete(1);
	    binomialHeap8.insert(4);
	    binomialHeap8.insert(1);
	    BinomialHeapNode binomialHeapNode3 = binomialHeap8.extractMin();
	    boolean b13 = binomialHeap7.equals((Object)binomialHeap8);
	    binomialHeap8.insert(4);
	    binomialHeap8.insert(4);
	    binomialHeap8.insert(1);
	    boolean b14 = binomialHeap0.equals((Object)1);
	    BinomialHeapNode binomialHeapNode4 = binomialHeap0.extractMin();
	    
 
	    
	    HashBag theBag = new HashBag();
	    binomialHeap0.Nodes.toBag(theBag);
	    System.out.println(theBag.toString());

	    theBag = new HashBag();
	    binomialHeap8.Nodes.toBag(theBag);
	    System.out.println(theBag.toString());
	    
	    System.out.println(binomialHeap0.equals(binomialHeap8));
	    System.out.println(binomialHeap0.hashCode() == binomialHeap8.hashCode());
	    
	    System.out.println(binomialHeap8.equals(binomialHeap0));
	    System.out.println(binomialHeap0.hashCode() == binomialHeap8.hashCode());

		    // Checks the contract:  equals-hashcode on binomialHeap0 and binomialHeap8
	    org.junit.Assert.assertTrue("Contract failed: equals-hashcode on binomialHeap0 and binomialHeap8", binomialHeap0.equals(binomialHeap8) ? binomialHeap0.hashCode() == binomialHeap8.hashCode() : true);
	    
		
		
		
		
		
		
		BinomialHeap tree = new BinomialHeap();
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.insert(0);
		tree.printHeap();

		
		BinomialHeap copy = tree.deepcopy();
		tree.delete(0);
		tree.printHeap();
		
		copy.printHeap();
		copy.insert(5);
		copy.printHeap();
		tree.printHeap();

	}
	*/	
	
}
// end of class BinomialHeap
