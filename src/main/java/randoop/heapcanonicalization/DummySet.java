package randoop.heapcanonicalization;

import java.util.HashSet;

public class DummySet<T> extends HashSet<T> {

	private static final long serialVersionUID = 5991517923340496380L;
	
	public DummySet() {
		
	}

	@Override
	public boolean add(T t) {
		return false;
	}
	
}
