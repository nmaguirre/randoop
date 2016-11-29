package randoop.util.fieldexhaustivecontrol;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.StringEdgeNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DirectedPseudograph;


/**
 * Produces graph-like representations of heaps originating in given objects, as well as
 * heap representations given as sets of tuples.
 *
 * @author Pablo Ponzio, Nazareno Aguirre.
 */
public class HeapDump {

	private int maxDepth; // = Integer.MAX_VALUE;
	private int maxArrayElements; // = Integer.MAX_VALUE;
	private HashMap<String, String> ignoredClasses = new HashMap<String, String>();
	private Set<String> ignoredFields = new HashSet<String>();
	
	private DirectedPseudograph<HeapVertex, LabeledEdge> heap =
			new DirectedPseudograph<HeapVertex, LabeledEdge>(LabeledEdge.class);
	private HeapVertex root;
	private HashMap<String, Integer> lastIndex = new HashMap<String, Integer>();
	
	private FieldExtensions fieldExtensions;
	
	private boolean extensionsExtended = false;
	
	// private HashMap<String, HashMap<Integer, Object>> primitiveFieldExtensions =
			//new HashMap<String, HashMap<Integer, Object>>();
	
	
	public HeapDump(Object o, FieldExtensions fe) throws IllegalArgumentException, IllegalAccessException {
		this(o, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null, fe);
	}
	
	
	public HeapDump(Object o) throws IllegalArgumentException, IllegalAccessException {
		this(o, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null);
	}

	public HeapDump(Object o, int maxDepth, int maxArrayElements) throws IllegalArgumentException, IllegalAccessException {
		this(o, maxDepth, maxArrayElements, null, null);
	}

	public DirectedPseudograph<HeapVertex, LabeledEdge> getHeap() {
		return heap;
	}

	public HeapDump(Object o, int maxDepth, int maxArrayElements, String[] ignoredClasses, String[] ignoredFields) throws IllegalArgumentException, IllegalAccessException {
		this(o, maxDepth, maxArrayElements, ignoredClasses, ignoredFields, new FieldExtensions());
	}
	
	
	public HeapDump(Object o, int maxDepth, int maxArrayElements, String[] ignoredClasses, String[] ignoredFields, FieldExtensions ext) throws IllegalArgumentException, IllegalAccessException {

		this.maxDepth = maxDepth;
		this.maxArrayElements = maxArrayElements;

		if (ignoredClasses != null) {
			for (int i = 0; i < Array.getLength(ignoredClasses); i++) {
				int colonIdx = ignoredClasses[i].indexOf(':');
				if (colonIdx == -1) ignoredClasses[i] = ignoredClasses[i] + ":";
				this.ignoredClasses.put(ignoredClasses[i], ignoredClasses[i]);
			}
		}
		
		if (ignoredFields != null) {
			for (int i = 0; i < Array.getLength(ignoredFields); i++) 
				this.ignoredFields.add(ignoredFields[i]);
		}
		
		fieldExtensions = ext;
		
		buildHeap(o);
		extensionsExtended = canonizeBFSAndPopulateExtensions();
	}
	
	
	
	private Integer getNextObjectIndex(Object obj) {
		String className = obj.getClass().getSimpleName();
		Integer c;
		if ((c = lastIndex.get(className)) != null) {
			lastIndex.put(className, ++c);
			return c;
		}

		lastIndex.put(className, 0);
		return 0;
	}

	
	private boolean canonizeBFSAndPopulateExtensions() {
		boolean extendedExt = false;
  		LinkedList<HeapVertex> toVisit = new LinkedList<HeapVertex>();
  		toVisit.push(root);
  		root.setIndex(0);
  		while (!toVisit.isEmpty()) {
  			HeapVertex source = toVisit.pop();
			// if currObj is null or isPrimitive(currObj) there's already a vertex in the graph representing the value; there's nothing left to do  			
  			TreeSet<LabeledEdge> sortedEdges = new TreeSet<LabeledEdge>(heap.outgoingEdgesOf(source));
  			for (LabeledEdge<HeapVertex> currEdge: sortedEdges) {
  				//System.out.println(currEdge.getLabel());
  				HeapVertex target = currEdge.getV2();
  				Object objToTag = target.getObject();
  				if (objToTag != null && !CanonicalRepresentation.isPrimitive(objToTag) && target.getIndex() == -1) {
  					target.setIndex(getNextObjectIndex(objToTag));
  					toVisit.add(target);
  				}
  				
  				String srcstr = CanonicalRepresentation.getCanonicalName(source.getObject(), source.getIndex());
  				String tgtstr = CanonicalRepresentation.getCanonicalName(target.getObject(), target.getIndex());
  				if (fieldExtensions.addPairToField(currEdge.getLabel(), srcstr, tgtstr))
  					extendedExt = true;
  			}
  		}
  		
  		return extendedExt;
	}
	

	public FieldExtensions getFieldExtensions() {
		return fieldExtensions;
	}

	/*public HashMap<String, HashMap<Integer, Object>> getPrimitiveFieldExtensions() {
		return primitiveFieldExtensions;
	}*/


	
	
  	private void buildHeap(Object rootObj) throws IllegalArgumentException, IllegalAccessException {

  		// DummyHeapRoot dummyroot = new DummyHeapRoot(rootObj);j
  		LinkedList<Tuple<HeapVertex,Integer>> toVisit = new LinkedList<Tuple<HeapVertex,Integer>>();
  		// root = new HeapVertex(dummyroot);
  		root = new HeapVertex(rootObj);
  		toVisit.push(new Tuple<HeapVertex,Integer>(root, 0));
  		heap.addVertex(root);
 		
  		while (!toVisit.isEmpty()) {
  			Tuple<HeapVertex,Integer> t = toVisit.pop();
  			HeapVertex currVertex = t.getFirst(); 
  			Object currObj = currVertex.getObject();
  			int currDepth = t.getSecond();
			// if currObj is null or isPrimitive(currObj) there's already a vertex in the graph representing the value; there's nothing left to do  			
  			if (currDepth < maxDepth && currObj != null &&
  					!CanonicalRepresentation.isPrimitive(currObj) &&  					
  					currObj.getClass() != Object.class &&
  					!ignoreClass(currObj.getClass().getSimpleName())) {
				
  				Class currObjClass = currObj.getClass();  			
				if (currObjClass.isArray()) {
					/*
					 * Process an array object currObj: 
					 * for index i such that currObj[i] = arrvalue_i, add an edge: 
					 * 		currObj --_array_i--> arravalue_i  
					 */
					String fName = "ARR_" + CanonicalRepresentation.getSimpleNameWithoutArrayQualifier(currObjClass) + ".elem";
					int rowCount = maxArrayElements == 0 ? Array.getLength(currObj) : Math.min(maxArrayElements, Array.getLength(currObj));
					for (int j = 0; j < rowCount; j++) {
						addFieldValueToHeapGraph(currVertex, Array.get(currObj, j), fName + j , toVisit, currDepth);
					}
				} 
				else {
					// Process a non-array field
					while (currObjClass != null && 
							currObjClass != Object.class && 
							!CanonicalRepresentation.isPrimitive(currObj) && 
							!ignoreClass(currObjClass.getSimpleName())) {
						
						List<Field> fields = getEnabledFields(currObjClass.getDeclaredFields());
						for (int i = 0; i < fields.size(); i++) {
							Field currField = fields.get(i);
							if (ignoredFields.contains(currField.getName())) 
								continue;
							
							String fName = currObjClass.getSimpleName() + "." + currField.getName();
							currField.setAccessible(true);										
							Object value = currField.get(currObj);
							addFieldValueToHeapGraph(currVertex, value, fName, toVisit, currDepth);

						}
						currObjClass = currObjClass.getSuperclass();					
					}
					
				}
  			}
  		}
  	}
  		
  		
  	private Tuple<Boolean, HeapVertex> addFieldValueToHeapGraph(HeapVertex currVertex, Object value, String fName) {
		Tuple<Boolean, HeapVertex> addVertexRes = addObjectToHeapGraph(value);
		HeapVertex valueVertex = addVertexRes.getSecond();
		addEdgeToHeapGraph(currVertex, valueVertex, fName);
		return addVertexRes;
  	}


  	private void addFieldValueToHeapGraph(HeapVertex currVertex, Object value, String fName, LinkedList<Tuple<HeapVertex,Integer>> toVisit, int currDepth) {
		Tuple<Boolean, HeapVertex> addVertexRes = addObjectToHeapGraph(value);
		HeapVertex valueVertex = addVertexRes.getSecond();
		addEdgeToHeapGraph(currVertex, valueVertex, fName);
		if (addVertexRes.getFirst())
			toVisit.push(new Tuple<HeapVertex, Integer>(valueVertex, currDepth + 1));
  	}
  	
  	private void addEdgeToHeapGraph(HeapVertex currVertex, HeapVertex valueVertex, String fName) {
		heap.addEdge(currVertex, valueVertex, new LabeledEdge<HeapVertex>(currVertex, valueVertex, fName));
  	}
  	
  	private Tuple<Boolean, HeapVertex> addObjectToHeapGraph(Object obj) {
  		boolean newNodeAdded = false;
  		HeapVertex valueVertex = getVertexContainingObject(obj);
		if (valueVertex == null) {
			valueVertex = new HeapVertex(obj);
			heap.addVertex(valueVertex);
			newNodeAdded = true;
		}
		return new Tuple<Boolean, HeapVertex>(newNodeAdded, valueVertex);
  	}
  	
  	private HeapVertex getVertexContainingObject(Object o) {
  		for (HeapVertex v: heap.vertexSet()) {
  			if (v.getObject() == o)
  				return v;
  		}
  		return null;
  	}
  

  	private boolean ignoreClass(String className) {
  		return this.ignoredClasses.get(className) != null;
  	}
  	
  	private boolean ignoreField(String fieldName, String fieldType) {
  		return !(this.ignoredClasses.get(":" + fieldName) == null
				&& this.ignoredClasses.get(fieldType + ":" + fieldName) == null
				&& this.ignoredClasses.get(fieldType + ":") == null);  		
  	}
  
  	private List<Field> getEnabledFields(Field [] fields) {
  		List<Field> res = new ArrayList<Field>();
  		for (Field f: fields) {
  			if (!ignoreField(f.getName(), CanonicalRepresentation.getSimpleNameWithoutArrayQualifier(f.getType())))
  				res.add(f);
  		}
  		return res;
  	}

	public boolean extensionsExtended() {
		return extensionsExtended;
	}

	public String heapToString() {
		StringWriter outputWriter = new StringWriter();
	    DOTExporter exporter = new DOTExporter(new HeapVertexNameProvider(), null, new StringEdgeNameProvider());
	    exporter.export(outputWriter, heap);

	    return outputWriter.toString();
	}
	
	public void heapToFile(String filename) throws IOException {
	    //DOTExporter exporter = new DOTExporter(new StringNameProvider(), null, new StringEdgeNameProvider());
		DOTExporter exporter = new DOTExporter(new HeapVertexNameProvider(), null, new StringEdgeNameProvider());
	    exporter.export(new FileWriter(filename), heap);
	}
	
	public void extensionsToFile(String filename) throws IOException {
	    fieldExtensions.toFile(filename);
	}
	
}
