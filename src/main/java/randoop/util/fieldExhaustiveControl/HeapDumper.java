package randoop.util.fieldExhaustiveControl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.graph.*;

public class HeapDumper {
	private static HeapDumper instance = new HeapDumper();

	protected static HeapDumper getInstance() {
		return instance;
	}

	class DumpContext {
		int maxDepth = 0;
		int maxArrayElements = 0;
		int currentIndex = 0;
		HashMap<String, String> ignoreList = new HashMap<String, String>();
		HashMap<Object, Integer> visited = new HashMap<Object, Integer>();
		LinkedList<Tuple<Object,Integer>> toVisit = new LinkedList<Tuple<Object,Integer>>();
		DefaultDirectedGraph<Object,LabeledEdge<Object>> heap = new DefaultDirectedGraph(LabeledEdge.class);
		HashMap<String,HashMap<Integer,Integer>> objectFieldExtensions = new HashMap<String,HashMap<Integer,Integer>>();
		HashMap<String,HashMap<Integer,Object>> primitiveFieldExtensions = new HashMap<String,HashMap<Integer,Object>>();
		HashMap<String,HashMap<Object,Integer>> objectIndex = new HashMap<String,HashMap<Object,Integer>>();
	}

	public static Tuple<HashMap<String,HashMap<Integer,Integer>>,HashMap<String,HashMap<Integer,Object>>> dump(Object o) {
		return dump(o, 0, 0, null);
	}

	public static Tuple<HashMap<String,HashMap<Integer,Integer>>,HashMap<String,HashMap<Integer,Object>>> dump(Object o, int maxDepth, int maxArrayElements, String[] ignoreList) {
		DumpContext ctx = HeapDumper.getInstance().new DumpContext();
		ctx.maxDepth = maxDepth;
		ctx.maxArrayElements = maxArrayElements;

		if (ignoreList != null) {
			for (int i = 0; i < Array.getLength(ignoreList); i++) {
				int colonIdx = ignoreList[i].indexOf(':');
				if (colonIdx == -1)
					ignoreList[i] = ignoreList[i] + ":";
				ctx.ignoreList.put(ignoreList[i], ignoreList[i]);
			}
		}
		ctx.toVisit.push(new Tuple<Object,Integer>(o, 0));
		buildHeap(ctx);
		buildExtensions(o, ctx);
		return new Tuple<HashMap<String,HashMap<Integer,Integer>>,HashMap<String,HashMap<Integer,Object>>>(ctx.objectFieldExtensions,ctx.primitiveFieldExtensions);
	}

	private static void buildExtensions(Object root, DumpContext ctx) {
		DefaultDirectedGraph<Object,LabeledEdge<Object>> heap = ctx.heap;
		LinkedList<Object> opened = new LinkedList<Object>();
		if (root!=null) opened.offer(root);
		while (!opened.isEmpty()) {
			Object current = opened.poll();
			if (!isPrimitive(current)) {
				Class currentClass = current.getClass();
				String currClassSimpleName = getSimpleNameWithoutArrayQualifier(currentClass);
				int currentIndex = 0;
				if (ctx.objectIndex.containsKey(currClassSimpleName)) {
					if (ctx.objectIndex.get(currClassSimpleName).containsKey(current)) {
						currentIndex = ctx.objectIndex.get(currClassSimpleName).get(current);
					}
					else {
						currentIndex = ctx.objectIndex.get(currClassSimpleName).size()+1;
						ctx.objectIndex.get(currClassSimpleName).put(current, currentIndex);
					}
				}							
				else {
					HashMap<Object, Integer> extension = new HashMap<Object, Integer>();
					ctx.objectIndex.put(currClassSimpleName, extension);
					currentIndex = ctx.objectIndex.get(currClassSimpleName).size()+1;
					ctx.objectIndex.get(currClassSimpleName).put(current, currentIndex);
				}
				
				Set<LabeledEdge<Object>> outgoingEdges = null;
				if (heap.containsVertex(current)) outgoingEdges = heap.outgoingEdgesOf(current);
				else outgoingEdges = new HashSet<LabeledEdge<Object>>();
				for (LabeledEdge<Object> currEdge: outgoingEdges) {
					Object target = currEdge.getV2();
					String field = currEdge.getLabel();
					if (target !=null) {
						if (!isPrimitive(target)) {
							Class targetClass = target.getClass();
							String tarClassSimpleName = getSimpleNameWithoutArrayQualifier(targetClass);
							int targetIndex = 0;
							if (ctx.objectIndex.containsKey(tarClassSimpleName)) {
								if (ctx.objectIndex.get(tarClassSimpleName).containsKey(target)) {
									targetIndex = ctx.objectIndex.get(tarClassSimpleName).get(target);
								}
								else {
									targetIndex = ctx.objectIndex.get(tarClassSimpleName).size()+1;
									ctx.objectIndex.get(tarClassSimpleName).put(target, targetIndex);
									opened.offer(target);
								}
							}							
							else {
								HashMap<Object, Integer> extension = new HashMap<Object, Integer>();
								ctx.objectIndex.put(tarClassSimpleName, extension);
								targetIndex = ctx.objectIndex.get(tarClassSimpleName).size()+1;
								ctx.objectIndex.get(tarClassSimpleName).put(target, targetIndex);
								opened.offer(target);
							}						
							if (ctx.objectFieldExtensions.containsKey(field)) {
								ctx.objectFieldExtensions.get(field).put(currentIndex, targetIndex);
							}
							else {
								HashMap<Integer,Integer> extension = new HashMap<Integer,Integer>();
								ctx.objectFieldExtensions.put(field, extension);
								ctx.objectFieldExtensions.get(field).put(currentIndex, targetIndex);
							}
						}
						else {
							// is primitive
							Class targetClass = target.getClass();
							String tarClassSimpleName = getSimpleNameWithoutArrayQualifier(targetClass);
							if (ctx.primitiveFieldExtensions.containsKey(field)) {
								ctx.primitiveFieldExtensions.get(field).put(currentIndex, target);
							}
							else {
								HashMap<Integer,Object> extension = new HashMap<Integer,Object>();
								ctx.primitiveFieldExtensions.put(field, extension);
								ctx.primitiveFieldExtensions.get(field).put(currentIndex, target);
							}						
						}
					}
				}
			}
		}
	}

	private static void buildHeap(DumpContext ctx) {
		while (!ctx.toVisit.isEmpty()) {
			Tuple<Object,Integer> t = ctx.toVisit.pop();
			Object o = t.getFirst();
			int currDepth = t.getSecond().intValue();
			if (currDepth<ctx.maxDepth && o!=null) {
				if (!isPrimitive(o)) {
					Class oClass = o.getClass();
					String oSimpleName = getSimpleNameWithoutArrayQualifier(oClass);
					if (ctx.ignoreList.get(oSimpleName + ":") == null && !ctx.visited.containsKey(o)) {
						ctx.visited.put(o, ctx.currentIndex);
						ctx.currentIndex++;
						if (oClass.isArray()) {
							int rowCount = ctx.maxArrayElements == 0 ? Array.getLength(o) : Math.min(ctx.maxArrayElements, Array.getLength(o));
							for (int i = 0; i < rowCount; i++) {
								try {
									Object value = Array.get(o, i);
									ctx.heap.addVertex(o);
									ctx.heap.addVertex(value);
									LabeledEdge<Object> edge = new LabeledEdge<Object>(o, value, "at("+i+")");
									ctx.heap.addEdge(o, value, edge);
									if (value!=null && !isPrimitive(value)) 
										ctx.toVisit.push(new Tuple<Object,Integer>(value,currDepth+1));
								} catch (Exception e) {
								}
							}
							if (rowCount < Array.getLength(o)) {
								//exceeded max length, do nothing
							}
						} else {
							while (oClass != null && oClass != Object.class) {
								Field[] fields = oClass.getDeclaredFields();

								if (ctx.ignoreList.get(oClass.getSimpleName()) == null) {
									if (oClass != o.getClass()) {                    	
									} 
									for (int i = 0; i < fields.length; i++) {

										String fSimpleName = getSimpleNameWithoutArrayQualifier(fields[i].getType());
										String fName = fields[i].getName();

										fields[i].setAccessible(true);
										if (ctx.ignoreList.get(":" + fName) == null &&
												ctx.ignoreList.get(fSimpleName + ":" + fName) == null &&
												ctx.ignoreList.get(fSimpleName + ":") == null) {
											try {
												Object value = fields[i].get(o);
												ctx.heap.addVertex(o);
												ctx.heap.addVertex(value);
												LabeledEdge<Object> edge = new LabeledEdge<Object>(o, value, fName);
												ctx.heap.addEdge(o, value, edge);
												if (value!=null && !isPrimitive(value)) 
													ctx.toVisit.push(new Tuple<Object,Integer>(value,currDepth+1));
											} catch (Exception e) {
											}
										}
										else {
											Ignored ignored = new Ignored();
											ctx.heap.addVertex(ignored);
											LabeledEdge<Object> edge = new LabeledEdge<Object>(o, ignored, fName);
											ctx.heap.addEdge(o, ignored, edge);                                                        	
										}
									}
									oClass = oClass.getSuperclass();
									oSimpleName = oClass.getSimpleName();
								}
								else {
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

	public static boolean isPrimitive (Object value) {
		return (value.getClass().isPrimitive() ||
				value.getClass() == java.lang.Short.class ||
				value.getClass() == java.lang.Long.class ||
				value.getClass() == java.lang.String.class ||
				value.getClass() == java.lang.Integer.class ||
				value.getClass() == java.lang.Float.class ||
				value.getClass() == java.lang.Byte.class ||
				value.getClass() == java.lang.Character.class ||
				value.getClass() == java.lang.Double.class ||
				value.getClass() == java.lang.Boolean.class ||
				value.getClass() == java.util.Date.class ||
				value.getClass().isEnum());    	
	}

	private static String getSimpleNameWithoutArrayQualifier(Class clazz) {
		String simpleName = clazz.getSimpleName();
		int indexOfBracket = simpleName.indexOf('['); 
		if (indexOfBracket != -1)
			return simpleName.substring(0, indexOfBracket);
		return simpleName;
	}
}