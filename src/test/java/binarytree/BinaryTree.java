package binarytree;

public class BinaryTree {

	
	Node root;
	
	int size;

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

	public int getSize(){
		return size;
	}

	public Node getRoot(){
		return root;
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

	
	public boolean find(int x) {
		return findNode(root,x);
	}

	private boolean findNode(Node n, int x) {
		if(n==null)
			return false;
		if(n.key==x)
			return true;
		if(findNode(n.left,x))
			return true;
		return findNode(n.right,x);
	}
	
	public void remove(int key) {
		if(find(key)){
			root = removeNode(root, key);	
			size--;
		}
	}
	
	private Node removeNode(Node n, int key) {
		//assume key exists in the tree
	    if (n == null) {
	        return null;
	    }
	    
	    if (key ==  n.key) {
	        // n is the node to be removed
	        if (n.left == null && n.right == null) {
	            return null;
	        }
	        if (n.left == null) {
	            return n.right;
	        }
	        if (n.right == null) {
	            return n.left;
	        }
	        
	        if(n.left.num_of_sons <= n.right.num_of_sons){
	        	n.key = smallest(n.right);	
	        	n.right = removeNode(n.right, n.key);
	        }
	        else{
				n.key = smallest(n.left);
	        	n.left = removeNode(n.left, n.key);
	        }
	        n.num_of_sons--;
	        return n; 
	    }
	    else {
	    	
	    	if (findNode(n.left,key))
		        n.left = removeNode(n.left, key);
		    else
		    	n.right = removeNode(n.right, key);

		    n.num_of_sons--;
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
	
    

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if (root != null)
            buf.append(root.toString());
        buf.append("}");
        return buf.toString();
    }	


}
