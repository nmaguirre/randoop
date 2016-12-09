package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Field;
import java.util.Comparator;

public class FieldByNameComp implements Comparator<Field> {

	@Override
	public int compare(Field o1, Field o2) {
		return CanonicalRepresentation.getFieldCanonicalName(o1).compareTo(CanonicalRepresentation.getFieldCanonicalName(o2));
	}

}  
