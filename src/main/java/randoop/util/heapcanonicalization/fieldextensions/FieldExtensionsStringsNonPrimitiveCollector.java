package randoop.util.heapcanonicalization.fieldextensions;


public class FieldExtensionsStringsNonPrimitiveCollector extends FieldExtensionsStringsCollector {
	
	public FieldExtensionsStringsNonPrimitiveCollector() {
		super();
	}

	
	public FieldExtensionsStringsNonPrimitiveCollector(int maxStrLen) {
		super(maxStrLen);
	}
	
	
	@Override
	boolean collectPrimitives() {
		return false;
	}

}
