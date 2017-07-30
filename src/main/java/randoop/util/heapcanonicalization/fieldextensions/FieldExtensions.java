package randoop.util.heapcanonicalization.fieldextensions;

import java.util.Set;

public interface FieldExtensions {
	
	public boolean addAll(FieldExtensions other);
	
	public boolean containsAll(FieldExtensions other);

	public Set<String> getValuesFor(String field, String object);

}
