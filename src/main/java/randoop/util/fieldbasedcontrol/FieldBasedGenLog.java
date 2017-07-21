package randoop.util.fieldbasedcontrol;

import randoop.Globals;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public final class FieldBasedGenLog {

  private FieldBasedGenLog() {
    throw new IllegalStateException("no instance");
  }

  public static final ByteArrayOutputStream bos;
  public static final PrintStream systemOutErrStream;
  public static final PrintStream err;
  public static final PrintStream out;

  static {
    bos = new ByteArrayOutputStream();
    systemOutErrStream = new PrintStream(bos);
    err = System.err;
    out = System.out;
  }

  public static void log(String s) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.fbg_log.write(s);
      GenInputsAbstract.fbg_log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void logLine(String s) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.fbg_log.write(s);
      GenInputsAbstract.fbg_log.write(Globals.lineSep);
      GenInputsAbstract.fbg_log.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(Sequence s) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.fbg_log.write(Globals.lineSep + Globals.lineSep);
      GenInputsAbstract.fbg_log.write(s.toString());
      GenInputsAbstract.fbg_log.flush();

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(List<TypedOperation> model) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      GenInputsAbstract.fbg_log.write("Statements : " + Globals.lineSep);
      for (TypedOperation t : model) {
        GenInputsAbstract.fbg_log.write(t.toString());
        GenInputsAbstract.fbg_log.write(Globals.lineSep);
        GenInputsAbstract.fbg_log.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isLoggingOn() {
    return GenInputsAbstract.fbg_log != null;
  }
}
