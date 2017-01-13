package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;

public class CanonizerArrayField {
	
	public Integer arrindex;
	public Class cls;
	public String name;
	public Integer index;

	public CanonizerArrayField(Class cls, Integer arrindex, String name, Integer index) {
		this.cls = cls;
		this.arrindex = arrindex;
		this.name = name;
		this.index = index;
	}

}
