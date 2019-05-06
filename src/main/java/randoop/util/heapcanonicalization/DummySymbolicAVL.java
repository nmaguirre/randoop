package randoop.util.heapcanonicalization;

import symbolicheap.bounded.AvlTree;

public class DummySymbolicAVL extends AvlTree implements IDummySymbolic {

	private static DummySymbolicAVL obj;

	public static DummySymbolicAVL getObject() {
		if (obj == null)
			obj = new DummySymbolicAVL();
			
		return obj;
	}
	
	private DummySymbolicAVL() {
		super(-1);
	}
	
}

