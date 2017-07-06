package randoop.util.heapcanonization;

//import java.lang.reflect.Array;

public class DummyHeapRoot {
	/*
	private Object[] heaproot;
		
	public DummyHeapRoot(Object newroot) {
		if (newroot.getClass().isArray()) {
			int l = Array.getLength(newroot);
			heaproot = new Object[l];
			for (int i = 0; i<l; i++) 
				heaproot[i] = Array.get(newroot, i);
		}
		else
			heaproot = new Object[] { newroot };
	}
	*/
	
	private Object theroot;
	
	public DummyHeapRoot(Object theroot) {
		this.theroot = theroot;
	}

}

