package randoop.heapcanonicalization;

import randoop.Globals;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.fieldextensions.GlobalExtensions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public final class CanonicalizerLog {

  private CanonicalizerLog() {
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
      GlobalExtensions.canonicalizerLog.write(s);
      GlobalExtensions.canonicalizerLog.flush();
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
      GlobalExtensions.canonicalizerLog.write(s);
      GlobalExtensions.canonicalizerLog.write(Globals.lineSep);
      GlobalExtensions.canonicalizerLog.flush();
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
      GlobalExtensions.canonicalizerLog.write(Globals.lineSep + Globals.lineSep);
      GlobalExtensions.canonicalizerLog.write(s.toString());
      GlobalExtensions.canonicalizerLog.flush();

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
      GlobalExtensions.canonicalizerLog.write("Statements : " + Globals.lineSep);
      for (TypedOperation t : model) {
        GlobalExtensions.canonicalizerLog.write(t.toString());
        GlobalExtensions.canonicalizerLog.write(Globals.lineSep);
        GlobalExtensions.canonicalizerLog.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isLoggingOn() {
    return GlobalExtensions.canonicalizerLog != null;
  }
}
