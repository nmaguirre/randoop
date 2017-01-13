package randoop.util.fieldbasedcontrol;

import java.util.ArrayList;
import java.util.List;

public class CanonizerClass {
	
	public Class cls;
	public String name;
	public Integer index;
	public boolean primitive;
	public boolean fieldBasedClasses;
	public boolean fieldBasedClassesAncestors; 
	public boolean ignoredClass;
	public ArrayList<CanonizerField> classFields;

	public CanonizerClass(Class cls, String name) {
		this.cls = cls;
		this.name = name;
		classFields = new ArrayList<>();
	}
	
	public void addField(CanonizerField f) {
		f.fromClass = this;
		f.index = classFields.size();
		classFields.add(f);
	}
	
}
