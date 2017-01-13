package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;

public class CanonizerField {
	
	public Field fld;
	public CanonizerClass fromClass;
	public String name;
	public Integer index;

	public CanonizerField(Field fld, String name) {
		this.fld = fld;
		this.name = name;
	}

}
