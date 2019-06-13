package randoop.generation;

public class DefaultListenerManagerFactory implements RandoopListenerManagerFactory {

	protected RandoopListenerManager genMgr = new RandoopListenerManager();
	protected RandoopListenerManager assertMgr = new RandoopListenerManager();
	
	@Override
	public RandoopListenerManager getGenerationManager() {
		return genMgr;
	}

	@Override
	public RandoopListenerManager getAssertionManager() {
		return assertMgr;
	}

}
