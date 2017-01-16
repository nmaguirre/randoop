package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;

public class CanonizerField {
	
	public Field fld;
	public CanonizerClass fromClass;
	public String name;
	public Integer index;
	public boolean isArray = false;

	public CanonizerField(Field fld, CanonizerClass fromClass, Integer index) {
		this.fld = fld;
		this.fld.setAccessible(true);
		this.name = fromClass.name + "." + fld.getName();
		this.fromClass = fromClass;
		this.index = index;
	}

	
	// Hack for simplifying the creating of dummy array fields
	public CanonizerField(String name) {
		this.name = name;
		isArray = true;
	}
	
	
	public String toString() {
		return fld.toString();
	}
	
}
