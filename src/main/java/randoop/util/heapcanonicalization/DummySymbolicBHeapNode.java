package randoop.util.heapcanonicalization;

import symbolicheap.bounded.BinomialHeapNode;

public class DummySymbolicBHeapNode extends BinomialHeapNode implements IDummySymbolic {

	private static DummySymbolicBHeapNode obj;

	public static DummySymbolicBHeapNode getObject() {
		if (obj == null)
			obj = new DummySymbolicBHeapNode();
			
		return obj;
	}
	
	private DummySymbolicBHeapNode() {
		super(-1);
	}
	
}

