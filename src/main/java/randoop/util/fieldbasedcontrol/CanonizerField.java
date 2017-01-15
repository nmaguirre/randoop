package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;

public class CanonizerField {
	
	public Field fld;
	public CanonizerClass fromClass;
	public String name;
	public Integer index;

	public CanonizerField(Field fld, CanonizerClass fromClass, Integer index) {
		this.fld = fld;
		this.name = fromClass.name + "." + fld.getName();
		this.fromClass = fromClass;
		this.index = index;
	}

}
