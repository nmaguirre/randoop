package randoop.util.fieldbasedcontrol;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

	private int maxStringSize = 50;
	private int maxDepth; // = Integer.MAX_VALUE;
	private int maxArrayElements; // = Integer.MAX_VALUE;
	private Set<String> ignoredClasses = new HashSet<String>();
	private Set<String> ignoredFields = new HashSet<String>();
	
	//private HashSet<HeapVertex> vertices = new HashSet<HeapVertex>();
	
	private DirectedPseudograph<HeapVertex, LabeledEdge> heap =
			new DirectedPseudograph<HeapVertex, LabeledEdge>(LabeledEdge.class);
	private HeapVertex root;
	private HashMap<String, Integer> lastIndex = new HashMap<String, Integer>();
	
	private FieldExtensions fieldExtensions;
	
	private boolean extensionsExtended = false;
	
	// private HashMap<String, HashMap<Integer, Object>> primitiveFieldExtensions =
			//new HashMap<String, HashMap<Integer, Object>>();
	
	
	public HeapDump(Object o, FieldExtensions fe) {
		this(o, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null, fe);
	}
	
	
	public HeapDump(Object o) {
		this(o, Integer.MAX_VALUE, Integer.MAX_VALUE, null, null);
	}

	public HeapDump(Object o, int maxDepth, int maxArrayElements) {
		this(o, maxDepth, maxArrayElements, null, null);
	}

	public DirectedPseudograph<HeapVertex, LabeledEdge> getHeap() {
		return heap;
	}

	public HeapDump(Object o, int maxDepth, int maxArrayElements, String[] ignoredClasses, String[] ignoredFields) {
		this(o, maxDepth, maxArrayElements, ignoredClasses, ignoredFields, new FieldExtensions());
	}
	
	
	public HeapDump(Object o, int maxDepth, int maxArrayElements, String[] ignoredClasses, String[] ignoredFields, FieldExtensions ext) {

		this.maxDepth = maxDepth;
		this.maxArrayElements = maxArrayElements;

		if (ignoredClasses != null) {
			for (int i = 0; i < Array.getLength(ignoredClasses); i++) {
				/*int colonIdx = ignoredClasses[i].indexOf(':');
				if (colonIdx == -1) ignoredClasses[i] = ignoredClasses[i] + ":";*/
				this.ignoredClasses.add(ignoredClasses[i]);
			}
		}
		
		if (ignoredFields != null) {
			for (int i = 0; i < Array.getLength(ignoredFields); i++) 
				this.ignoredFields.add(ignoredFields[i]);
		}
		
		fieldExtensions = ext;
		
		buildHeap(o);
		//System.out.println(heap.vertexSet().size());
		extensionsExtended = canonizeBFSAndPopulateExtensions();
		//System.out.println(fieldExtensions.size());
	}
	
	
	
	private Integer getNextObjectIndex(Object obj) {
		String className = CanonicalRepresentation.getClassCanonicalName(obj.getClass());
		Integer c;
		if ((c = lastIndex.get(className)) != null) {
			lastIndex.put(className, ++c);
			return c;
		}

		lastIndex.put(className, 0);
		return 0;
	}

	
	private boolean canonizeBFSAndPopulateExtensions() {
		// a null root does not add anything to field extensions
		if (root.getObject() == null) return false;
		
		boolean extendedExt = false;
  		LinkedList<HeapVertex> toVisit = new LinkedList<HeapVertex>();
  		toVisit.push(root);
  		root.setIndex(0);
  		while (!toVisit.isEmpty()) {
  			HeapVertex source = toVisit.pop();
  			Object sourceObj = source.getObject();

  			String srcstr = CanonicalRepresentation.getVertexCanonicalName(source);
  			// Add the values of primitive objects to the extensions 
			if (CanonicalRepresentation.isPrimitive(sourceObj)) {
				/*if (source.getIndex() == -1) 
					source.setIndex(getNextObjectIndex(sourceObj));*/
/*				try {
					Field currField;
		  			if (CanonicalRepresentation.isEnum(sourceObj)) 
						currField = sourceObj.getClass().getField("name");
					else 				
						currField = sourceObj.getClass().getField("value");
					currField.setAccessible(true);*/
					String fname = /*sourceObj.getClass().getSimpleName() + ".value";*/ CanonicalRepresentation.getClassCanonicalName(sourceObj.getClass()) + ".value";
					String tgtname = sourceObj.toString();
					// trim string values to length maxStringSize
					if (sourceObj.getClass() == String.class && tgtname.length() > maxStringSize) {
						tgtname = tgtname.substring(0, maxStringSize);
					}
					
					if (fieldExtensions.addPairToField(fname, srcstr, tgtname))
						extendedExt = true;
				/*}
	  			catch (Exception e) {
	  				System.out.println(sourceObj.getClass().getName() + "" + "" + sourceObj.toString());
	  				System.exit(1);
	  				// Should never happen.
	  				//e.printStackTrace();
	  			}*/
			}
			else {
				// if currObj is null there's already a vertex in the graph representing the value; there's nothing left to do  			
				TreeSet<LabeledEdge> sortedEdges = new TreeSet<LabeledEdge>(heap.outgoingEdgesOf(source));
				for (LabeledEdge<HeapVertex> currEdge: sortedEdges) {
					//System.out.println(currEdge.getLabel());
					HeapVertex target = currEdge.getV2();
					Object objToTag = target.getObject();
					if (objToTag != null && /* !CanonicalRepresentation.isPrimitive(objToTag) &&*/ target.getIndex() == -1) {
						target.setIndex(getNextObjectIndex(objToTag));
						toVisit.add(target);
					}
					//String srcstr = CanonicalRepresentation.getCanonicalName(source.getObject(), source.getIndex());
					String tgtstr = CanonicalRepresentation.getVertexCanonicalName(target);
					if (fieldExtensions.addPairToField(currEdge.getLabel(), srcstr, tgtstr))
						extendedExt = true;
				}
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


	
  	private void buildHeap(Object rootObj) {

  		// DummyHeapRoot dummyroot = new DummyHeapRoot(rootObj);
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
  					!ignoreClass(CanonicalRepresentation.getClassCanonicalName(currObj.getClass()))) {
				
  				Class currObjClass = currObj.getClass();
  				if (currObjClass.isArray()) {
					/*
					 * Process an array object currObj: 
					 * for index i such that currObj[i] = arrvalue_i, add an edge: 
					 * 		currObj --_array_i--> arravalue_i  
					 */
					String fName = /*currObjClass.getSimpleName() + ".elem";*/ CanonicalRepresentation.getClassCanonicalName(currObjClass) + ".elem";
					int rowCount = maxArrayElements == 0 ? Array.getLength(currObj) : Math.min(maxArrayElements, Array.getLength(currObj));
					for (int j = 0; j < rowCount; j++) {
						Object arrObj = Array.get(currObj, j);
						
						if (arrObj != null && ignoreClass(CanonicalRepresentation.getClassCanonicalName(arrObj.getClass()))) 
							break;
						
						addFieldValueToHeapGraph(currVertex, arrObj, fName + j , toVisit, currDepth);
					}
				} 
				else {
					// Process a non-array field
					while (currObjClass != null && 
							currObjClass != Object.class && 
							!CanonicalRepresentation.isPrimitive(currObj) && 
							!ignoreClass(CanonicalRepresentation.getClassCanonicalName(currObjClass))) {
						
						List<Field> fields = getEnabledFields(currObjClass.getDeclaredFields());
						for (int i = 0; i < fields.size(); i++) {
							Field currField = fields.get(i);
							
							String fName = CanonicalRepresentation.getFieldCanonicalName(currField);
							if (ignoredFields.contains(fName) /*|| Modifier.isTransient(currField.getModifiers())*/)
								continue;
							
							currField.setAccessible(true);										
							Object value = null;
							try {
								value = currField.get(currObj);
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							if (value != null && ignoreClass(CanonicalRepresentation.getClassCanonicalName(value.getClass()))) 
								continue;
							
							addFieldValueToHeapGraph(currVertex, value, fName, toVisit, currDepth);

						}
						currObjClass = currObjClass.getSuperclass();	
					}
					
				}
  			}
  		}
  	}
  		
  		
  	/*
  	private Tuple<Boolean, HeapVertex> addFieldValueToHeapGraph(HeapVertex currVertex, Object value, String fName) {
		Tuple<Boolean, HeapVertex> addVertexRes = addObjectToHeapGraph(value);
		HeapVertex valueVertex = addVertexRes.getSecond();
		addEdgeToHeapGraph(currVertex, valueVertex, fName);
		return addVertexRes;
  	}*/


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
  		//System.out.println("entre " + heap.vertexSet().size());
  		for (HeapVertex v: heap.vertexSet()) {
  			Object vertexObj = v.getObject();
  			/* 
  			// FIXME: Different objects must have different representations in our heap?
  			if (vertexObj == o)
  				return v;  			
  			else if (o != null && CanonicalRepresentation.isPrimitive(o) &&
  					o.getClass() == vertexObj.getClass() && o.equals(vertexObj)) 
  				return v;*/
  			if (vertexObj == null && o == null)
  				return v;
  			
  			if (v.getObject() != null && vertexObj.equals(o)) {
  				if (vertexObj.hashCode() != o.hashCode()) {
  					System.out.println("en grafo:");
  					System.out.println(vertexObj.getClass());
  					System.out.println(vertexObj.toString());
  					System.out.println("otro:");
  					System.out.println(o.getClass());
  					System.out.println(o.toString());
  					throw new RuntimeException("JAVA Contract Violation: Two equal objects must have the same hash code.");
  				}
  				return v;
  			}
  		}
  		
  		return null;
  	}
  

  	private boolean ignoreClass(String className) {
//  		System.out.println(className);
//  		className = className.substring(className.lastIndexOf(".")+1, className.length());
//  		System.out.println(className);
//  		System.out.println(ignoredClasses);
//  		System.out.println(this.ignoredClasses.contains(className));
  		
  		return this.ignoredClasses.contains(className);
  	}
  	
  	private boolean ignoreField(String fieldName) {
  		return this.ignoredFields.contains(fieldName);  		
  	}
  
  	private List<Field> getEnabledFields(Field [] fields) {
  		List<Field> res = new ArrayList<Field>();
  		for (Field f: fields) {
  			String fname = CanonicalRepresentation.getFieldCanonicalName(f);
  			//if (!ignoreField(fname.substring(fname.lastIndexOf(".")+1, fname.length())))
  			if (!ignoreField(fname))
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
	
	public void heapToFile(String filename) {
	    //DOTExporter exporter = new DOTExporter(new StringNameProvider(), null, new StringEdgeNameProvider());
		DOTExporter exporter = new DOTExporter(new HeapVertexNameProvider(), null, new StringEdgeNameProvider());
	    try {
			exporter.export(new FileWriter(filename), heap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void extensionsToFile(String filename) {
	    try {
			fieldExtensions.toFile(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
