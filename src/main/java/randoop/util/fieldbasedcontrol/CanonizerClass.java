package randoop.util.fieldbasedcontrol;

import java.util.LinkedList;
import java.util.List;

public class CanonizerClass {
	
	public Class cls;
	public String name;
	public Integer index;
	public boolean primitive;
	// ignored means no objects of the class will be saved during heap canonization
	// primitive values' classes are always ignored 
	public boolean ignored;
	public List<CanonizerField> classFields;

	public CanonizerClass(Class cls, String name, Integer index, boolean primitive) {
		this.cls = cls;
		this.name = name;
		this.index = index;
		this.primitive = primitive;
		classFields = new LinkedList<>();
	}
	
	public void addField(CanonizerField f) {
		classFields.add(f);
	}
	
}
