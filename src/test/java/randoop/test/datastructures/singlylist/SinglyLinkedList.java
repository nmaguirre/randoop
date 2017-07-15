package randoop.test.datastructures.singlylist;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;



/**
 * Class  StrictlySortedSinglyLinkedList defines Strictly Sorted, Singly linked List 
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
    
    
    
    
    public boolean repOK() {
        if (!repOkCommon())
            return false;

        // check for sorted
        if (header.next != null) {
        	for (Node current = header.next; current.next != null; current = current.next) {
    			if (current.value > current.next.value)
    				return false;
        	}
        }
        
        return true;
    }

    
    public boolean repOkCommon() {
        if (header == null)
            return false;

        if (header.value != 0)
            return false;

        Set<Node> visited = new HashSet<Node>();
        visited.add(header);
        Node current = header;

        while (true) {
            Node next = current.next;
            if (next == null)
                break;

            if (!visited.add(next))
                return false;

            current = next;
        }

        if (visited.size() - 1 != size)
            return false;

        return true;
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
 
   	public SinglyLinkedList deepcopy() {
   		
		LinkedList<Node> visited = new LinkedList<Node>();
		ArrayList<Node> nodes = new ArrayList<Node>(size);		
		ArrayList<Node> newnodes = new ArrayList<Node>(size);
		
		int ind = 0;
		
		if (header == null) {
			SinglyLinkedList res = new SinglyLinkedList();
			res.header = null;
			res.size = size;
			return res;
		}
		else {
			visited.add(header);
		
			while (!visited.isEmpty()) {
				Node currNode = visited.removeFirst();
	
				nodes.add(currNode);
				currNode._index = ind;
				ind++;
				
				if (currNode.next != null) 
					visited.add(currNode.next);
			}
			
			for (int i=0; i<nodes.size();i++) {
				Node newnode = new Node();
				newnodes.add(newnode);
			}
	
			for (int i=0; i<nodes.size();i++) {
				Node currnode = nodes.get(i);
				Node newnode = newnodes.get(i);
				
				newnode.value = currnode.value;
				
				if (currnode.next != null)
					newnode.next = newnodes.get(currnode.next._index);
				else
					newnode.next = null;	
			}
			
			SinglyLinkedList res = new SinglyLinkedList();
			res.header = newnodes.get(header._index);
			res.size = size;
			return res;

		}
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

