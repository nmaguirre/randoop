package randoop.util.fieldbasedcontrol;

import java.util.LinkedList;
import java.util.List;

public class CanonizerClass {
	
	public Class cls;
	public String name;
	public Integer index;
	public boolean primitive;
	public Boolean fieldBasedClasses = null;
	public Boolean fieldBasedClassesAncestors = null; 
	public Boolean ignoredClass = null;
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
