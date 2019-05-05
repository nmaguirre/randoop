package randoop.util.heapcanonicalization.candidatevectors;

import randoop.Globals;
import randoop.main.GenInputsAbstract;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public final class NegativeSymbolicVectorsWriter {

  private NegativeSymbolicVectorsWriter() {
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
    if (!isEnabled()) {
      return;
    }

    try {
      GenInputsAbstract.negative_symbolic_vectors_file.write(s);
      GenInputsAbstract.negative_symbolic_vectors_file.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void logLine(String s) {
    if (!isEnabled()) {
      return;
    }

    try {
      GenInputsAbstract.negative_symbolic_vectors_file.write(s);
      GenInputsAbstract.negative_symbolic_vectors_file.write(Globals.lineSep);
      GenInputsAbstract.negative_symbolic_vectors_file.flush();
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(Sequence s) {
    if (!isEnabled()) {
      return;
    }

    try {
      GenInputsAbstract.negative_symbolic_vectors_file.write(Globals.lineSep + Globals.lineSep);
      GenInputsAbstract.negative_symbolic_vectors_file.write(s.toString());
      GenInputsAbstract.negative_symbolic_vectors_file.flush();

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void log(List<TypedOperation> model) {
    if (!isEnabled()) {
      return;
    }

    try {
      GenInputsAbstract.negative_symbolic_vectors_file.write("Statements : " + Globals.lineSep);
      for (TypedOperation t : model) {
        GenInputsAbstract.negative_symbolic_vectors_file.write(t.toString());
        GenInputsAbstract.negative_symbolic_vectors_file.write(Globals.lineSep);
        GenInputsAbstract.negative_symbolic_vectors_file.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isEnabled() {
    return GenInputsAbstract.negative_symbolic_vectors_file != null;
  }
}
