package randoop.util.fieldbasedcontrol;

import java.util.LinkedList;
import java.util.List;

public class CanonizerClass {
	
	public Class<?> cls;
	public CanonizerClass ancestor;
	public String name;
	public Integer index;
	public boolean primitive;
	// ignored means no objects of the class will be saved during heap canonization
	// primitive values' classes are always ignored (never saved)
	public boolean ignored;
	public List<CanonizerField> classFields;
	public List<CanonizerField> allFields;

	public CanonizerClass(Class<?> cls, String name, Integer index, boolean primitive) {
		this.cls = cls;
		this.name = name;
		this.index = index;
		this.primitive = primitive;
		classFields = new LinkedList<>();
	}
	
	public void addField(CanonizerField f) {
		classFields.add(f);
	}
	
	public String toString() {
		return cls.toString();
	}
	
	public List<CanonizerField> getAllFields() {
		if (allFields != null) return allFields;
		
		allFields = new LinkedList<>(classFields);
		CanonizerClass anc = ancestor;
		while (anc != null) {
			allFields.addAll(anc.classFields);
			anc = anc.ancestor;
		}
		return allFields;
	}
	
}
