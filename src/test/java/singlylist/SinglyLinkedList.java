package singlylist;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;

/**
 * Class  StrictlySortedSinglyLinkedList defines Strictly Sorted, Singly linked List 
 * @author 
 */
public class SinglyLinkedList implements Serializable {
	
		/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
		private Node header;
    private int size;
    
	public SinglyLinkedList(){
		header = new Node();
		header.value = 0;
		size = 0;
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
    
    public void add(int value){
		Node current = header.next;	
		Node previous = header;
			
	   	while(current!=null){
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
    
    public static IFinitization finSinglyLinkedList(int size) {
        return finSinglyLinkedList(size, 0, size, 0, size-1);
    }
            
    public static IFinitization finSinglyLinkedList(int numNodes, int minSize, int maxSize,
            int minData, int maxData) {

        IFinitization f = FinitizationFactory.create(SinglyLinkedList.class);

        IObjSet entries = f.createObjSet(Node.class);
        entries.setNullAllowed(true);
        entries.addClassDomain(f.createClassDomain(Node.class, numNodes));

        
        IIntSet values = f.createIntSet(minData, maxData);
        IIntSet sizes = f.createIntSet(minSize, maxSize);

        f.set("header", entries);
        f.set("size", sizes);
        f.set("Node.value", values);
        f.set("Node.next", entries);

        return f;

    }
*/
}//End Class

