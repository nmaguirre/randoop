package randoop.util.fieldbasedcontrol;


public class CanonizerObject {
	
	public Object obj;
	private CanonizerClass cls;
	public Integer index;
	public boolean added;

	public CanonizerObject(Object obj, CanonizerClass cls, Integer index) {
		this.obj = obj;
		this.cls = cls;
		this.index = index;
	}
	
	public boolean primitive() {
		return cls.primitive;
	}
	
	public boolean ignored() {
		return cls.ignored;
	}

}
