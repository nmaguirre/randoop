package randoop.generation;

public class SerializerListenerManagerFactory extends DefaultListenerManagerFactory {

	private String inputsFile;
	private String assertsFile;
	private String serializeClass;
	
	public SerializerListenerManagerFactory(String serializeClass, String inputsFile, String assertsFile) {
		super();
		this.inputsFile = inputsFile;
		this.assertsFile = assertsFile;
		this.serializeClass = serializeClass;
		
		genMgr.addListener(new ObjectSerializerListener(serializeClass, inputsFile));
		assertMgr.addListener(new ObjectSerializerListener(serializeClass, assertsFile));
	}
	

	
	

}
