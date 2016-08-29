package randoop.util.fieldExhaustiveControl;

public final class Ignored {
	
	public Ignored() { }
	
	public String toString() {
		return "Ignored";
	}
	
	public boolean equals(Object other) {
		if (other==null) return false;
		if (other instanceof Ignored) return true;
		return false;
	}
}
