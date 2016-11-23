package randoop.util.fieldexhaustivecontrol;

import java.util.HashMap;
import java.util.Map;

public class FieldExtensions {

	private Map<String, FieldExtension> extension = new HashMap<String, FieldExtension>();
	

	
	
	
	
	/*
  private HashMap<String, HashMap<Integer, Collection<Integer>>> objectFieldExtensions =
      new HashMap<String, HashMap<Integer, Collection<Integer>>>();
  private HashMap<String, HashMap<Integer, Collection<Object>>> primitiveFieldExtensions =
      new HashMap<String, HashMap<Integer, Collection<Object>>>();

  private boolean haveSeenNull = false;

  private int maxDepth;
  private int maxArrayLength;
  private String[] ignoreList;

  public FieldExtensions() {
    this.maxDepth = 1000;
    this.maxArrayLength = 1000;
    ignoreList = new String[0];
  }

  public boolean add(Object newObject) {
    if (newObject == null) {
      if (haveSeenNull) {
        return false;
      } else {
        haveSeenNull = true;
        return true;
      }
    }
    if (HeapDump.isPrimitive(newObject)) return false;
    HeapDump newObjectDump =
        new HeapDump(newObject, this.maxDepth, this.maxArrayLength, this.ignoreList);
    HashMap<String, HashMap<Integer, Integer>> newObjectExts =
        newObjectDump.getObjectFieldExtensions();
    HashMap<String, HashMap<Integer, Object>> newPrimitiveExts =
        newObjectDump.getPrimitiveFieldExtensions();
    boolean addedNewFieldValue = false;
    for (String key : newObjectExts.keySet()) {
      if (!objectFieldExtensions.containsKey(key)) {
        HashMap<Integer, Collection<Integer>> extsForKey =
            new HashMap<Integer, Collection<Integer>>();
        for (Integer source : newObjectExts.get(key).keySet()) {
          HashSet<Integer> target = new HashSet<Integer>();
          target.add(newObjectExts.get(key).get(source));
          extsForKey.put(source, target);
        }
        objectFieldExtensions.put(key, extsForKey);
        addedNewFieldValue = true;
      } else {
        for (Entry<Integer, Integer> entry : newObjectExts.get(key).entrySet()) {
          Integer source = entry.getKey();
          Integer target = entry.getValue();
          if (!objectFieldExtensions.get(key).containsKey(source)) {
            HashMap<Integer, Collection<Integer>> extsForKey = objectFieldExtensions.get(key);
            HashSet<Integer> targets = new HashSet<Integer>();
            targets.add(target);
            extsForKey.put(source, targets);
            objectFieldExtensions.put(key, extsForKey);
            addedNewFieldValue = true;
          } else {
            Collection<Integer> targets = objectFieldExtensions.get(key).get(source);
            if (!targets.contains(target)) {
              targets.add(target);
              objectFieldExtensions.get(key).put(source, targets);
              addedNewFieldValue = true;
            }
          }
        }
      }
    }
    for (String key : newPrimitiveExts.keySet()) {
      if (!primitiveFieldExtensions.containsKey(key)) {
        HashMap<Integer, Collection<Object>> extsForKey =
            new HashMap<Integer, Collection<Object>>();
        for (Integer source : newPrimitiveExts.get(key).keySet()) {
          HashSet<Object> target = new HashSet<Object>();
          target.add(newPrimitiveExts.get(key).get(source));
          extsForKey.put(source, target);
        }
        primitiveFieldExtensions.put(key, extsForKey);
        addedNewFieldValue = true;
      } else {
        for (Entry<Integer, Object> entry : newPrimitiveExts.get(key).entrySet()) {
          Integer source = entry.getKey();
          Object target = entry.getValue();
          if (!primitiveFieldExtensions.get(key).containsKey(source)) {
            HashMap<Integer, Collection<Object>> extsForKey = primitiveFieldExtensions.get(key);
            HashSet<Object> targets = new HashSet<Object>();
            targets.add(target);
            extsForKey.put(source, targets);
            primitiveFieldExtensions.put(key, extsForKey);
            addedNewFieldValue = true;
          } else {
            Collection<Object> targets = primitiveFieldExtensions.get(key).get(source);
            if (!targets.contains(target)) {
              targets.add(target);
              primitiveFieldExtensions.get(key).put(source, targets);
              addedNewFieldValue = true;
            }
          }
        }
      }
    }
    return addedNewFieldValue;
  }

  public void reset() {
    objectFieldExtensions.clear();
  }
  */
}
