package singlylist;

import java.io.Serializable;

public class Node implements Serializable {

	private static final long serialVersionUID = -7468562023002691227L;
	
	int value;

	Node next;


	public String toString() {
		return "[" + value + "]";
	}
 
}