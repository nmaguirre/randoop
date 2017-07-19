package randoop.util.heapcanonicalization.fieldextensions;


public class FieldExtensionsStringsNonPrimitiveCollector extends FieldExtensionsStringsCollector {
	
	@Override
	boolean collectPrimitives() {
		return false;
	}

}
