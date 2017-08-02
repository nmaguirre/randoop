package binarytree;

import java.util.HashSet;
import java.util.Set;

public class Node {
	
     Node left; // left child

     Node right; // right child

     int key; // data

     int num_of_sons; // number of sons 

    public Node(Node left, Node right, int key) {
        this.left = left;
        this.right = right;
        this.key = key;
        this.num_of_sons = 0;
        if(left!=null)
            this.num_of_sons += left.num_of_sons;
        if(right!=null) 
            this.num_of_sons += right.num_of_sons;
    }

    public Node(int key) {
        this.key = key;
    }

    public Node() {

    }

    public Node getLeft(){
        return left;
    }

    public Node getRight(){
        return right;
    }    

    public int getKey(){
        return key;
    } 

    public int getNumOfSons(){
        return num_of_sons;
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
        if (this.key != (n.key))
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
        
        return b && (num_of_sons==n.num_of_sons);
    }

}