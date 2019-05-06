package randoop.util.heapcanonicalization;

import symbolicheap.bounded.TreeSet;

public class DummySymbolicTSet extends TreeSet implements IDummySymbolic {

	private static DummySymbolicTSet obj;

	public static DummySymbolicTSet getObject() {
		if (obj == null)
			obj = new DummySymbolicTSet();
			
		return obj;
	}
	
	private DummySymbolicTSet() {
		super(-1);
	}
	
}

