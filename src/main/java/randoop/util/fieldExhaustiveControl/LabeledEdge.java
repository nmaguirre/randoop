package randoop.util.fieldExhaustiveControl;

import org.jgrapht.graph.DefaultEdge;

/**
 * Class that represents labeled edges in graphs, to be used with jgrapht library.
 * A labeled edge is simply a default edge that holds a string typed label.
 *
 * @author Nazareno Aguirre
 *
 * @param <V> is the type of the vertices connected by the edge.
 */
public class LabeledEdge<V> extends DefaultEdge {

  private static final long serialVersionUID = 1L;

  /**
   * source vertex of edge.
   */
  private V source;

  /**
   * target vertex of edge.
   */
  private V target;

  /**
   * label of edge.
   */
  private String label;

  /**
   * Constructor, that creates an edge with provided vertices and label.
   * @param v1 is the source vertex.
   * @param v2 is the target vertex.
   * @param label is the label of the edge.
   */
  public LabeledEdge(V v1, V v2, String label) {
    if (v1 == null) throw new IllegalArgumentException("null source vertex");
    if (v2 == null) throw new IllegalArgumentException("null target vertex");
    if (label == null) throw new IllegalArgumentException("null label string");
    this.source = v1;
    this.target = v2;
    this.label = label;
  }

  /**
   * Returns the source vertex of the edge.
   * @return the source vertex of the edge.
   */
  public V getV1() {
    return source;
  }

  /**
   * Returns the target vertex of the edge.
   * @return the target vertex of the edge.
   */
  public V getV2() {
    return target;
  }

  /**
   * Returns label of the edge.
   * @return label of the edge.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Produces a string representation of the LabeledEdge object.
   */
  public String toString() {
    return label;
  }
}
