package randoop.util.fieldbasedcontrol;

/**
 * Simple generic class that captures tuples of objects.
 *
 * @author Nazareno Aguirre
 *
 * @param <T1> is the type of the first component of the tuple
 * @param <T2> is the type of the second component of the tuple
 */
public class Tuple<T1, T2> {

  /**
   * first component
   */
  private T1 first;

  /**
   * second component
   */
  private T2 second;

  /**
   * Constructor, that receives values of first and second components to setup a tuple.
   * @param first is the value for the first component
   * @param second is the value for the second component
   */
  public Tuple(T1 first, T2 second) {
    super();
    this.first = first;
    this.second = second;
  }

  /**
   * Returns the value of the first component of a tuple.
   * @return the first component of the tuple.
   */
  public T1 getFirst() {
    return first;
  }

  /**
   * Sets the value of the first component of the tuple.
   * @param first is the value to be set as first component of the tuple.
   */
  public void setFirst(T1 first) {
    this.first = first;
  }

  /**
   * Returns the value of the second component of a tuple.
   * @return the second component of the tuple.
   */
  public T2 getSecond() {
    return second;
  }

  /**
   * Sets the value of the second component of the tuple.
   * @param second is the value to be set as second component of the tuple.
   */
  public void setSecond(T2 second) {
    this.second = second;
  }
}
