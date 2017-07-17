package randoop.fieldbased.datastructures;

public class BinaryTree {
	
	public static class Node {
		Node left;
		Node right;
		int value;
		int num_of_sons; 
		
		public Node(int x) {
			value = x;
			left = null;
			right = null;
		}
		
	}

	private Node root;
	private int size;

	public BinaryTree() {
		root = null;
		size = 0;
	}
	
	private Node newNode(int n) {
		Node res = new Node(n);
		return res;
	}
	
	public boolean isEmpty(){
		return size==0;
	}
	
	//insert a new node in the BinaryTree. The node is inserted in the left side, if in the left side there are less nodes
	//than the right side, otherwise the node is inserted in the right side.
	public void add(int x) {
		size++;

		if (root == null) {
			root = newNode(x);
			return;
		}

		Node current = root;
		boolean leaveReached = false;
		while (!leaveReached) {
			//current will have one more son
			current.num_of_sons++;
			if (current.left == null || current.right == null) {
				leaveReached = true;
			} else {
				if (current.left.num_of_sons <= current.right.num_of_sons) {
					// The node must be inserted in the left side
					current = current.left;

				} else {
					// The node must be inserted in the right side
					current = current.right;
				}
			}
		}

		Node n = newNode(x);

		if (current.left == null) {
			// insert in left
			current.left = n;
		} else {
			// insert in right
			current.right = n;
		}
	}
	
	
	
}
