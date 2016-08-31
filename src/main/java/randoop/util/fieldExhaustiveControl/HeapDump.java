package randoop.util.fieldExhaustiveControl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.graph.*;

/**
 * Produces graph-like representations of heaps originating in given objects, as well as
 * heap representations given as sets of tuples.
 *
 * @author Nazareno Aguirre
 */
public class HeapDump {

  private int maxDepth = 0;
  private int maxArrayElements = 0;
  private int currentIndex = 0;
  private HashMap<String, String> ignoreList = new HashMap<String, String>();
  private HashMap<Object, Integer> visited = new HashMap<Object, Integer>();
  private LinkedList<Tuple<Object, Integer>> toVisit = new LinkedList<Tuple<Object, Integer>>();
  private DefaultDirectedGraph<Object, LabeledEdge<Object>> heap =
      new DefaultDirectedGraph(LabeledEdge.class);
  private HashMap<String, HashMap<Integer, Integer>> objectFieldExtensions =
      new HashMap<String, HashMap<Integer, Integer>>();

  private HashMap<String, HashMap<Integer, Object>> primitiveFieldExtensions =
      new HashMap<String, HashMap<Integer, Object>>();
  private HashMap<String, HashMap<Object, Integer>> objectIndex =
      new HashMap<String, HashMap<Object, Integer>>();

  public HeapDump(Object o) {
    this(o, 0, 0, null);
  }

  public HeapDump(Object o, int maxDepth, int maxArrayElements, String[] ignoreList) {
    this.maxDepth = maxDepth;
    this.maxArrayElements = maxArrayElements;

    if (ignoreList != null) {
      for (int i = 0; i < Array.getLength(ignoreList); i++) {
        int colonIdx = ignoreList[i].indexOf(':');
        if (colonIdx == -1) ignoreList[i] = ignoreList[i] + ":";
        this.ignoreList.put(ignoreList[i], ignoreList[i]);
      }
    }
    this.toVisit.push(new Tuple<Object, Integer>(o, 0));
    buildHeap();
    buildExtensions(o);
  }

  public HashMap<String, HashMap<Integer, Integer>> getObjectFieldExtensions() {
    return objectFieldExtensions;
  }

  public HashMap<String, HashMap<Integer, Object>> getPrimitiveFieldExtensions() {
    return primitiveFieldExtensions;
  }

  private void buildExtensions(Object root) {
    DefaultDirectedGraph<Object, LabeledEdge<Object>> heap = this.heap;
    LinkedList<Object> opened = new LinkedList<Object>();
    if (root != null) opened.offer(root);
    while (!opened.isEmpty()) {
      Object current = opened.poll();
      if (!isPrimitive(current)) {
        Class currentClass = current.getClass();
        String currClassSimpleName = getSimpleNameWithoutArrayQualifier(currentClass);
        int currentIndex = 0;
        if (this.objectIndex.containsKey(currClassSimpleName)) {
          if (this.objectIndex.get(currClassSimpleName).containsKey(current)) {
            currentIndex = this.objectIndex.get(currClassSimpleName).get(current);
          } else {
            currentIndex = this.objectIndex.get(currClassSimpleName).size() + 1;
            this.objectIndex.get(currClassSimpleName).put(current, currentIndex);
          }
        } else {
          HashMap<Object, Integer> extension = new HashMap<Object, Integer>();
          this.objectIndex.put(currClassSimpleName, extension);
          currentIndex = this.objectIndex.get(currClassSimpleName).size() + 1;
          this.objectIndex.get(currClassSimpleName).put(current, currentIndex);
        }

        Set<LabeledEdge<Object>> outgoingEdges = null;
        if (heap.containsVertex(current)) outgoingEdges = heap.outgoingEdgesOf(current);
        else outgoingEdges = new HashSet<LabeledEdge<Object>>();
        for (LabeledEdge<Object> currEdge : outgoingEdges) {
          Object target = currEdge.getV2();
          String field = currEdge.getLabel();
          if (target != null) {
            if (!isPrimitive(target)) {
              Class targetClass = target.getClass();
              String tarClassSimpleName = getSimpleNameWithoutArrayQualifier(targetClass);
              int targetIndex = 0;
              if (this.objectIndex.containsKey(tarClassSimpleName)) {
                if (this.objectIndex.get(tarClassSimpleName).containsKey(target)) {
                  targetIndex = this.objectIndex.get(tarClassSimpleName).get(target);
                } else {
                  targetIndex = this.objectIndex.get(tarClassSimpleName).size() + 1;
                  this.objectIndex.get(tarClassSimpleName).put(target, targetIndex);
                  opened.offer(target);
                }
              } else {
                HashMap<Object, Integer> extension = new HashMap<Object, Integer>();
                this.objectIndex.put(tarClassSimpleName, extension);
                targetIndex = this.objectIndex.get(tarClassSimpleName).size() + 1;
                this.objectIndex.get(tarClassSimpleName).put(target, targetIndex);
                opened.offer(target);
              }
              if (this.objectFieldExtensions.containsKey(field)) {
                this.objectFieldExtensions.get(field).put(currentIndex, targetIndex);
              } else {
                HashMap<Integer, Integer> extension = new HashMap<Integer, Integer>();
                this.objectFieldExtensions.put(field, extension);
                this.objectFieldExtensions.get(field).put(currentIndex, targetIndex);
              }
            } else {
              // is primitive
              Class targetClass = target.getClass();
              String tarClassSimpleName = getSimpleNameWithoutArrayQualifier(targetClass);
              if (this.primitiveFieldExtensions.containsKey(field)) {
                this.primitiveFieldExtensions.get(field).put(currentIndex, target);
              } else {
                HashMap<Integer, Object> extension = new HashMap<Integer, Object>();
                this.primitiveFieldExtensions.put(field, extension);
                this.primitiveFieldExtensions.get(field).put(currentIndex, target);
              }
            }
          }
        }
      }
    }
  }

  private void buildHeap() {
    while (!this.toVisit.isEmpty()) {
      Tuple<Object, Integer> t = this.toVisit.pop();
      Object o = t.getFirst();
      int currDepth = t.getSecond().intValue();
      if (currDepth < this.maxDepth && o != null) {
        if (!isPrimitive(o)) {
          Class oClass = o.getClass();
          String oSimpleName = getSimpleNameWithoutArrayQualifier(oClass);
          if (this.ignoreList.get(oSimpleName + ":") == null && !this.visited.containsKey(o)) {
            this.visited.put(o, this.currentIndex);
            this.currentIndex++;
            if (oClass.isArray()) {
              int rowCount =
                  this.maxArrayElements == 0
                      ? Array.getLength(o)
                      : Math.min(this.maxArrayElements, Array.getLength(o));
              for (int i = 0; i < rowCount; i++) {
                try {
                  Object value = Array.get(o, i);
                  this.heap.addVertex(o);
                  this.heap.addVertex(value);
                  LabeledEdge<Object> edge = new LabeledEdge<Object>(o, value, "at(" + i + ")");
                  this.heap.addEdge(o, value, edge);
                  if (value != null && !isPrimitive(value))
                    this.toVisit.push(new Tuple<Object, Integer>(value, currDepth + 1));
                } catch (Exception e) {
                }
              }
              if (rowCount < Array.getLength(o)) {
                //exceeded max length, do nothing
              }
            } else {
              while (oClass != null && oClass != Object.class) {
                Field[] fields = oClass.getDeclaredFields();

                if (this.ignoreList.get(oClass.getSimpleName()) == null) {
                  if (oClass != o.getClass()) {}
                  for (int i = 0; i < fields.length; i++) {

                    String fSimpleName = getSimpleNameWithoutArrayQualifier(fields[i].getType());
                    String fName = fields[i].getName();

                    fields[i].setAccessible(true);
                    if (this.ignoreList.get(":" + fName) == null
                        && this.ignoreList.get(fSimpleName + ":" + fName) == null
                        && this.ignoreList.get(fSimpleName + ":") == null) {
                      try {
                        Object value = fields[i].get(o);
                        this.heap.addVertex(o);
                        this.heap.addVertex(value);
                        LabeledEdge<Object> edge = new LabeledEdge<Object>(o, value, fName);
                        this.heap.addEdge(o, value, edge);
                        if (value != null && !isPrimitive(value))
                          this.toVisit.push(new Tuple<Object, Integer>(value, currDepth + 1));
                      } catch (Exception e) {
                      }
                    } else {
                      Ignored ignored = new Ignored();
                      this.heap.addVertex(ignored);
                      LabeledEdge<Object> edge = new LabeledEdge<Object>(o, ignored, fName);
                      this.heap.addEdge(o, ignored, edge);
                    }
                  }
                  oClass = oClass.getSuperclass();
                  oSimpleName = oClass.getSimpleName();
                } else {
                  oClass = null;
                  oSimpleName = "";
                }
              }
            }
          }
        }
      }
    }
  }

  public static boolean isPrimitive(Object value) {
    return (value.getClass().isPrimitive()
        || value.getClass() == java.lang.Short.class
        || value.getClass() == java.lang.Long.class
        || value.getClass() == java.lang.String.class
        || value.getClass() == java.lang.Integer.class
        || value.getClass() == java.lang.Float.class
        || value.getClass() == java.lang.Byte.class
        || value.getClass() == java.lang.Character.class
        || value.getClass() == java.lang.Double.class
        || value.getClass() == java.lang.Boolean.class
        || value.getClass() == java.util.Date.class
        || value.getClass().isEnum());
  }

  private static String getSimpleNameWithoutArrayQualifier(Class clazz) {
    String simpleName = clazz.getSimpleName();
    int indexOfBracket = simpleName.indexOf('[');
    if (indexOfBracket != -1) return simpleName.substring(0, indexOfBracket);
    return simpleName;
  }
}
