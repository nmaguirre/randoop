package randoop.util.fieldbasedcontrol;

/**
 * Dummy class used to represent objects that are ignored, in the construction of a
 * graph-like representation of the heap held by an object.
 *
 * @author Nazareno Aguirre
 */
public final class Ignored {

  /**
   * Default constructor. Does nothing.
   */
  public Ignored() {}

  /**
   * String representation of the "ignored" object.
   */
  public String toString() {
    return "Ignored";
  }

  /**
   * Compares current object with other. Ignored object only equals another ignored object.
   */
  public boolean equals(Object other) {
    if (other == null) return false;
    if (other instanceof Ignored) return true;
    return false;
  }
}
