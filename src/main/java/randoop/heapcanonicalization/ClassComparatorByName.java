package randoop.heapcanonicalization;

import java.util.Comparator;

public class ClassComparatorByName implements Comparator<Class<?>> {

	@Override
	public int compare(Class<?> c1, Class<?> c2) {
		return c1.getName().compareTo(c2.getName());
	}

}
