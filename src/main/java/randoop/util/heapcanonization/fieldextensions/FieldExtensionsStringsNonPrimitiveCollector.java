package randoop.util.heapcanonization.fieldextensions;


public class FieldExtensionsStringsNonPrimitiveCollector extends FieldExtensionsStringsCollector {
	
	@Override
	boolean collectPrimitives() {
		return false;
	}

}
