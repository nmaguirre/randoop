package randoop.generation;

import randoop.fieldextensions.IBuildersManager;
import randoop.fieldextensions.IRedundancyStrategy;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.util.Log;
import randoop.util.ProgressDisplay;
import randoop.util.ReflectionExecutor;

import java.util.List;

/**
 * Algorithm template for implementing a test generator.
 *
 * The main generation loop is defined in method <code>explore()</code>, which
 * repeatedly generates a new sequence, determines if it a failing sequence, and
 * stops the process when the time or sequence limit expires. The process of
 * generating a new sequences is left abstract.
 *
 * @see ForwardGenerator
 */
public abstract class AbstractGeneratorBE extends AbstractGenerator {
	
	protected IRedundancyStrategy redundancyStrat;
	protected IBuildersManager buildersManager;
	
	
	public AbstractGeneratorBE(
			List<TypedOperation> operations,
			long timeMillis,
			int maxGeneratedSequences,
			int maxOutSequences,
			ComponentManager componentManager,
			IStopper stopper,
			RandoopListenerManagerFactory listenerManagerFact) {

		super(operations,timeMillis, maxGeneratedSequences, maxOutSequences, componentManager, stopper, 
				listenerManagerFact.getGenerationManager());
		assert operations != null;
	}

  /**
   * Creates and executes new sequences until stopping criteria is met.
   *
   * @see AbstractGeneratorBE#stop()
   * @see AbstractGeneratorBE#step()
   */
  public void explore() {
    if (checkGenerator == null) {
      throw new Error("Generator not properly initialized - must have a TestCheckGenerator");
    }
    Log.log(this.operations);

    timer.startTiming();

    if (!GenInputsAbstract.noprogressdisplay) {
      progressDisplay = new ProgressDisplay(this, listenerMgr, ProgressDisplay.Mode.MULTILINE, 200);
      progressDisplay.start();
    }

    if (Log.isLoggingOn()) {
      Log.logLine("Initial sequences (seeds):");
      for (Sequence s : componentManager.getAllGeneratedSequences()) {
        Log.logLine(s.toString());
      }
    }



    long startTime = System.currentTimeMillis();

    gen(); 

    if (!GenInputsAbstract.noprogressdisplay) {
    	System.out.println();
    	long elapsedTime = (System.currentTimeMillis() - startTime);  	
    	System.out.println();
    	System.out.println("Bounded exhaustive generation time: " + elapsedTime + "ms");
    }


    if (!GenInputsAbstract.noprogressdisplay && progressDisplay != null) {
      progressDisplay.display();
      progressDisplay.shouldStop = true;
    }
    
    if (!GenInputsAbstract.noprogressdisplay) {
      System.out.println();
      System.out.println("Normal method executions:" + ReflectionExecutor.normalExecs());
      System.out.println("Exceptional method executions:" + ReflectionExecutor.excepExecs());
      System.out.println();
      System.out.println(
          "Average method execution time (normal termination):      "
              + String.format("%.3g", ReflectionExecutor.normalExecAvgMillis()));
      System.out.println(
          "Average method execution time (exceptional termination): "
              + String.format("%.3g", ReflectionExecutor.excepExecAvgMillis()));
    }

  }

 protected abstract void gen();
}
