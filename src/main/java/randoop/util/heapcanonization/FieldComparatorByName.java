package randoop.util.heapcanonization;

import java.lang.reflect.Field;
import java.util.Comparator;

public class FieldComparatorByName implements Comparator<Field> {

	@Override
	public int compare(Field f1, Field f2) {
		return f1.getName().compareTo(f2.getName());
	}

}
