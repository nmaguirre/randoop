package randoop.generation;

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
	
	public AbstractGeneratorBE(
			List<TypedOperation> operations,
			long timeMillis,
			int maxGeneratedSequences,
			int maxOutSequences,
			ComponentManager componentManager,
			IStopper stopper,
			RandoopListenerManager listenerManager) {

		super(operations,timeMillis, maxGeneratedSequences, maxOutSequences, componentManager, stopper, listenerManager);
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

    // Notify listeners that exploration is starting.
    if (listenerMgr != null) {
      listenerMgr.explorationStart();
    }


    gen(); 
    
    
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

    // Notify listeners that exploration is ending.
    if (listenerMgr != null) {
      listenerMgr.explorationEnd();
    }
  }

 protected abstract void gen();
}
