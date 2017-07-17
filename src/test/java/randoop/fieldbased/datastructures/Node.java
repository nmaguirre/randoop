package randoop.fieldbased.datastructures;

public  class Node{

	public int value;
	public Node next;
	
	public Node() {
		value = 0;
		next = null;
	}

	public int getValue(){
		return value;
	}

	public Node getNext(){
		return next;
	}

	public String toString() {
		return "[" + value + "]";
	}	
	
}
