package randoop.util.fieldbasedcontrol;

import java.util.ArrayList;
import java.util.List;

public class CanonizerClass {
	
	public Class cls;
	public String name;
	public Integer index;
	public boolean primitive;
	public Boolean fieldBasedClasses = null;
	public Boolean fieldBasedClassesAncestors = null; 
	public Boolean ignoredClass = null;
	public ArrayList<CanonizerField> classFields;

	public CanonizerClass(Class cls, String name, Integer index, boolean primitive) {
		this.cls = cls;
		this.name = name;
		this.index = index;
		this.primitive = primitive;
		classFields = new ArrayList<>();
	}
	
	public void addField(CanonizerField f) {
		f.index = classFields.size();
		classFields.add(f);
	}
	
}
