package randoop.fieldbased.datastructures;

/**
 * Class Singly Linked List 
 * @author 
 */
public class SinglyLinkedList {

	private Node header;
	private int size;

	public SinglyLinkedList(){
		header = new Node();
		header.value = 0;
		size = 0;
	}

	public boolean isEmpty(){
		return header.next== null;
	}

	public Node getheader(){
		return header;
	}

	public int getSize(){
		return size;
	}

	public boolean contains(int value){
		Node current = header.next;	
		while(current!=null){
			if(current.value == value)
				return true;
			current = current.next;
		} 
		return false;
	}

	public void add(int value, int position){
		if(position<0 || position>size)
			throw new IllegalArgumentException();

		Node current = header.next;
		Node previous = header;
		int pos = 0;
		while(pos<position){
			pos++;
			previous = current;
			current = current.next;
		}
		Node n = new Node();
		n.value = value;
		n.next = current;
		previous.next = n;
		size++;
	}

	public void remove(int value){
		Node current = header.next;	
		Node previous = header;

		while(current!=null && current.value != value){
			previous = current;		
			current = current.next;
		}

		if (current!=null) {
			previous.next = current.next;
			size--;
		}

	}

	public String toString() {
		String res = "{";
		if (header != null) {
			Node cur = header.next;
			while (cur != null) {
				res += cur.toString();
				cur = cur.next;
			}
		}
		return res + "}";
	}

	@Override
	public int hashCode() {
		int result = 1;
		Node thisItr = header.next;
		while (thisItr != null) {
			result = 31*result + thisItr.value;
			thisItr=thisItr.next;
		}

		//result = prime * result + size;
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
		SinglyLinkedList otherList = (SinglyLinkedList) obj;

		if (size != otherList.size) 
			return false;

			Node thisItr = header.next;
			Node otherItr = otherList.header.next;
    	while (thisItr != null) {
    		if (otherItr == null) 
    			return false;
    		if (thisItr.value != otherItr.value) 
    			return false;
    		otherItr = otherItr.next;
    		thisItr = thisItr.next;
    	}

    	return otherItr == null;

	}
   	
}//End Class

