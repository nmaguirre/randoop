package randoop.util.fieldbasedcontrol;

import java.util.List;

public class CanonizerObject {
	
	public Object obj;
	public CanonizerClass cc;
	public Integer index;
	public boolean visited;

	public CanonizerObject(Object obj, CanonizerClass cls, Integer index) {
		this.obj = obj;
		this.cc = cls;
		this.index = index;
	}
	
	public boolean primitive() {
		return cc.primitive;
	}
	
	public boolean ignored() {
		return cc.ignored;
	}
	
	public boolean visited() {
		return visited;
	}
	
	public boolean isArray() {
		return cc.cls.isArray();
	}
	
	public List<CanonizerField> fields() {
		return cc.getAllFields();  
	}

	public Class<?> getObjClass() {
		return cc.cls;
	}

	public String toString() {
		return cc.name + "," + index;
	}
	
	
}
