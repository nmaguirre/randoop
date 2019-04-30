package randoop.test.datastructures.bstree;

import java.util.HashSet;
import java.util.Set;

public class Node {
	
	// Required for making deep copies of BSTree objects
	transient int _index;
	
    Node left; // left child

    Node right; // right child

    int key; // data

    Node(Node left, Node right, int key) {
        this.left = left;
        this.right = right;
        this.key = key;
    }

    Node(int key) {
        this.key = key;
    }

    public Node() {

    }

    public String toString() {
        Set visited = new HashSet();
        visited.add(this);
        return toString(visited);
    }

    private String toString(Set visited) {
        StringBuffer buf = new StringBuffer();
        // buf.append(" ");
        // buf.append(System.identityHashCode(this));
        buf.append(" {");
        if (left != null)
            if (visited.add(left))
                buf.append(left.toString(visited));
            else
                buf.append("!tree");

        buf.append("" + this.key + "");

        if (right != null)
            if (visited.add(right))
                buf.append(right.toString(visited));
            else
                buf.append("!tree");
        buf.append("} ");
        return buf.toString();
    }

    public boolean equals(Object that) {
        if (!(that instanceof Node))
            return false;
        Node n = (Node) that;
        // if (this.key.compareTo(n.key) != 0)
        if (this.key > (n.key))
            return false;
        boolean b = true;
        if (left == null)
            b = b && (n.left == null);
        else
            b = b && (left.equals(n.left));
        if (right == null)
            b = b && (n.right == null);
        else
            b = b && (right.equals(n.right));
        return b;
    }

    // Call with theSet = empty
    public void toSet(Set theSet) {
    	theSet.add(this.key);
    	if (this.left != null)
    		this.left.toSet(theSet);
     	if (this.right != null)
    		this.right.toSet(theSet);
    }
    

}
