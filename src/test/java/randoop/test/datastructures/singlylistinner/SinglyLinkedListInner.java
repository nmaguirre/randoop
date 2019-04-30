package randoop.test.datastructures.singlylistinner;


/**
 * Class  StrictlySortedSinglyLinkedList defines Strictly Sorted, Singly linked List 
 * @author 
 */
public class SinglyLinkedListInner {
	
	public  class Node{

		int value;

		Node next;

		public String toString() {
			return "[" + value + "]";
		}
		
	}
	

    private Node header;
    private int size;
   


	public SinglyLinkedListInner(){
		header = new Node();
		header.value = 0;
		size = 0;
   }	

	
    public boolean contains(int value){
    	Node current = header.next;	
    	while(current!=null && current.value<=value){
    			if(current.value == value)
    				return true;
    			current = current.next;
       	} 
    	return false;
    }
    
    public void add(int value){
		Node current = header.next;	
		Node previous = header;
			
	   	while(current!=null && current.value < value){
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
    
    
   
    
    
    /**
 	 * Checks whether or not the current list has not values.
     * @return true iff the current list is empty, false otherwise.
     */
    
    public boolean isEmpty(){
    	return header.next== null;
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
		SinglyLinkedListInner otherList = (SinglyLinkedListInner) obj;
		
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


	/*
	static public void main(String [] args) {
	    SinglyLinkedList singlyLinkedList0 = new SinglyLinkedList();
	    singlyLinkedList0.add(3);
	    singlyLinkedList0.add(3);
	    //singlyLinkedList0.remove(3);
	    

	    
	    SinglyLinkedList singlyLinkedList1 = new SinglyLinkedList();
	    singlyLinkedList1.add(3);
	    //singlyLinkedList1.remove(3);
	    
	    System.out.println("equals " + singlyLinkedList0.equals(singlyLinkedList1));
	    System.out.println("hashcode " + (singlyLinkedList0.hashCode() == singlyLinkedList1.hashCode()));
	    
   	}
   	*/
   	
}//End Class

