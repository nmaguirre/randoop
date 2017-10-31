package randoop;

import java.io.File;
import java.io.PrintStream;
import java.util.List;


import randoop.sequence.ExecutableSequence;

/**
 * JunitFileWriter is a class that for a collection of sequences, outputs Java
 * files containing one JUnit4 test method per sequence. An object manages the
 * information for a suite of tests (name, package, and directory) and is used
 * by first running writeJUnitTestFiles and then writeSuiteFile or
 * writeDriverFile. Alternatively, a single test file can be written using the
 * static method writeJUnitTestFile.
 */
public class JunitFileWriterComputeExtensions extends JunitFileWriter {

  public JunitFileWriterComputeExtensions(String junitDirName, String packageName, String masterTestClassName) {
	  super(junitDirName, packageName, masterTestClassName);
  }

  /**
   * writeTestClass writes a code sequence as a JUnit4 test class to a .java
   * file. Tests are executed in ascending alphabetical order by test method
   * name.
   *
   * @param sequences
   *          list of executable sequences for method bodies.
   * @param testClassName
   *          name of test class.
   * @return the File object for generated java file
   */
  @Override
  protected File writeTestClass(List<ExecutableSequence> sequences, String testClassName) {

    File file = new File(getDir(), testClassName + ".java");
    PrintStream out = createTextOutputStream(file);

    NameGenerator methodNameGen = new NameGenerator("test", 1, numDigits(sequences.size()));

    try {
      outputPackageName(out, packageName);
      out.println();
      out.println("import org.junit.FixMethodOrder;");
      out.println("import org.junit.Test;");
      out.println("import org.junit.runners.MethodSorters;");
      out.println("import org.junit.BeforeClass;");
      out.println("import org.junit.AfterClass;");
      out.println();
      out.println("@FixMethodOrder(MethodSorters.NAME_ASCENDING)");
      out.println("public class " + testClassName + " {");
      out.println();
      out.println("  public static boolean debug = false;");
      out.println();
      
      out.println("  @BeforeClass");
      out.println("  public static void beforeClass() {");
      out.println("    randoop.fieldextensions.GlobalExtensions.writeTotal(false);");
      out.println("  }");
      out.println();

      out.println("  @AfterClass");
      out.println("  public static void afterClass() {");
      out.println("    randoop.fieldextensions.GlobalExtensions.writeTotal(true);");
      out.println("  }");
      out.println();

      for (ExecutableSequence s : sequences) {
        if (includeParsableString) {
          out.println("/*");
          out.println(s.sequence.toString());
          out.println("*/");
        }

        writeTest(out, testClassName, methodNameGen.next(), s);
        out.println();
      }
      out.println("}");
      classMethodCounts.put(testClassName, methodNameGen.nameCount());
    } finally {
      if (out != null) out.close();
    }

    return file;
  }

  /*
   *
   */
  /**
   * Writes a test method to the output stream for the sequence s.
   *
   * @param out
   *          the output stream for test class file.
   * @param className
   *          the name of test class.
   * @param methodName
   *          the name of test method.
   * @param s
   *          the {@link ExecutableSequence} for test method.
   */
  @Override
  protected void writeTest(
      PrintStream out, String className, String methodName, ExecutableSequence s) {
    out.println("  @Test");
    out.println("  public void " + methodName + "() throws Throwable {");
    out.println();
    out.println(
        indent(
            "if (debug) { System.out.format(\"%n%s%n\",\""
                + className
                + "."
                + methodName
                + "\"); }"));
    out.println();
    out.println(indent(s.toCountExtensionsCodeString()));
    out.println("  }");
  }

}
