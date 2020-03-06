package randoop.util.heapcanonicalization;

import symbolicheap.bounded.BinomialHeap;

public class DummySymbolicBHeap extends BinomialHeap implements IDummySymbolic {

	private static DummySymbolicBHeap obj;

	public static DummySymbolicBHeap getObject() {
		if (obj == null)
			obj = new DummySymbolicBHeap();
			
		return obj;
	}
	
	private DummySymbolicBHeap() {
		super();
	}
	
}

