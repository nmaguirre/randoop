package randoop.util.fieldExhaustiveControl;

public class Tuple<T1,T2> {

	public Tuple(T1 first, T2 second) {
		super();
		this.first = first;
		this.second = second;
	}
	
	private T1 first;
	private T2 second;
	
	public T1 getFirst() {
		return first;
	}
	public void setFirst(T1 first) {
		this.first = first;
	}
	public T2 getSecond() {
		return second;
	}
	public void setSecond(T2 second) {
		this.second = second;
	}
}
