package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Set;

import randoop.generation.AbstractGenerator;


/* 
 * class HeapCanonizer
 * 
 * Builds a canonical representation of the heap 
 * and creates field extensions with it
 * 
 * Author: Pablo Ponzio.
 * Date: December 2016.
 */

public class HeapCanonizerRuntimeEfficient {
	
	public FieldExtensionsStrings readableExtensions = null;
	
	private boolean extendedExtensions;

	protected boolean ignorePrimitive;
	
	public CanonicalHeapStore store;
	
	private static final int DEFAULT_MAX_OBJECTS = 100000;
	private static final int DEFAULT_MAX_ARRAY = 10000;
	private int maxObjects;
	private int maxArray;
		
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive) {
		this(ignorePrimitive, null, DEFAULT_MAX_OBJECTS, DEFAULT_MAX_ARRAY);
	}

	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		this(ignorePrimitive, null, DEFAULT_MAX_OBJECTS, DEFAULT_MAX_ARRAY);
	}
	
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive, int maxObjects, int maxArray) {
		this(ignorePrimitive, null, maxObjects, maxArray);
	}
	
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames, int maxObjects, int maxArray) {
		this.ignorePrimitive = ignorePrimitive;
		this.store = new CanonicalHeapStore(maxObjects);
		if (fieldBasedGenClassnames != null)
			store.setFieldBasedGenByClasses(fieldBasedGenClassnames);
		
		if (AbstractGenerator.field_based_gen_differential_runtime_checks)
			activateReadableExtensions();
		
		this.maxObjects = maxObjects;
		this.maxArray = maxArray;
	}
	
	
	public FieldExtensionsIndexes getExtensions() {
		return store.extensions; 
	}

	
	public void activateReadableExtensions() {
		readableExtensions = new FieldExtensionsStrings();
	}
	
	
	public FieldExtensionsStrings getReadableExtensions() {
		return readableExtensions; 
	}
	
	
    protected boolean addToReadableExtensions(CanonizerObject src, CanonizerObject tgt, CanonizerField fld) {
    	String srccls = src.cc.name;
    	Integer srcInd = src.index;
    	String srcString = srccls + srcInd;     

    	String tgtString = null;
   		if (tgt.obj == null)
   			tgtString = store.getNullRepresentation();
   		else if (tgt.primitive()) {
			tgtString = tgt.obj.toString();
				// trim string values to length maxStringSize
			if (tgt.cc.cls == String.class && tgtString.length() > CanonicalRepresentation.MAX_STRING_SIZE)
				tgtString = tgtString.substring(0, CanonicalRepresentation.MAX_STRING_SIZE);
    	}
    	else {
    		String tgtcls = tgt.cc.name;
   			tgtString = tgtcls + tgt.index; 
   		}

    	return readableExtensions.addPairToField(fld.name, srcString, tgtString);
    }
	
	
    
    // TODO: This method belongs to the store?
	// Returns a pair (b1, b2) where b1 iff the extensions were enlarged by this call, and
	// b2 iff tgt is an object visited for the first time in this call
	protected boolean addToExtensions(CanonizerObject src, CanonizerObject tgt, CanonizerField fld) {
		
		if (ignorePrimitive && (tgt.primitive() || tgt.isNull()))
			return false;
		
		boolean newRes = store.extensions.addPairToField(fld, src, tgt);

		if (readableExtensions != null) {
			boolean oldRes = addToReadableExtensions(src, tgt, fld); 
			
			if (oldRes != newRes) { 
				System.out.println("ERROR: Differential test failed when adding to the extensions");
				throw new RuntimeException("ERROR: Differential test failed when adding to the extensions");
			}
				
		}	

		return newRes;
	}

	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public boolean traverseBreadthFirstAndEnlargeExtensions(Object root) {
		store.clear();
		extendedExtensions = false;
  	
  		CanonizerObject croot = store.addObject(root);
		if (croot.ignored() || croot.primitive() || croot.isNull()) 
			return false;

  		LinkedList<CanonizerObject> toVisit = new LinkedList<>();
  		toVisit.addLast(croot);

  		while (!toVisit.isEmpty()) {
  			// New obj is already in store, use its fields to enlarge extensions
  			CanonizerObject cobj = toVisit.pop();

  			if (cobj.isArray()) {
				// Process an array object. Add a dummy field for each of its elements
				int length = Array.getLength(cobj.obj);
				if (length >= maxArray) {
					length = maxArray;
					String message = "> FIELD BASED GENERATION WARNING: Array length limit (" + maxArray + ") exceeded for class " + cobj.cc.name;
					System.out.println(message);
					if (FieldBasedGenLog.isLoggingOn()) 
						FieldBasedGenLog.logLine(message);
				}
				for (int i = 0; i < length; i++) {
					
					Object target = Array.get(cobj.obj, i);
					CanonizerObject newcobj = store.addObject(target);
					// Max number of stored objects for the class exceeded
					if (newcobj == null) continue;

					if (!newcobj.ignored() && !newcobj.visited() && !newcobj.primitive() && !newcobj.isNull())
						toVisit.add(newcobj);

					CanonizerField arrayDummy = store.canonizeArrayField(cobj.cc, i);
					if (!newcobj.ignored() && addToExtensions(cobj, newcobj, arrayDummy))
						extendedExtensions = true; 
				}
			}
			else {
				for (CanonizerField cf: cobj.fields()) {
					// Did this in field creation
					// cf.fld.setAccessible(true);
					Object target;
					try {
						target = cf.fld.get(cobj.obj);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println("FAILURE in field: " + cf.fld.toString());
						// Cannot happen
						throw new RuntimeException("ERROR: Illegal access to an object field during canonization");
					}

					CanonizerObject newcobj = store.addObject(target);
					// Max number of stored objects for the class exceeded
					if (newcobj == null) continue;
					
					if (!newcobj.ignored() && !newcobj.visited() && !newcobj.primitive() && !newcobj.isNull())
						toVisit.add(newcobj);

					if (!newcobj.ignored() && addToExtensions(cobj, newcobj, cf))
						extendedExtensions = true; 
				}
			}
		}
  		
		if (readableExtensions != null) {
			
			if (!readableExtensions.equals(store.extensions.toFieldExtensionsStrings())) {
				System.out.println("ERROR: Differential test failed. Different versions of the extensions differ");
				throw new RuntimeException("ERROR: Differential test failed. Different versions of the extensions differ");
			}
		}	
  		
  		return extendedExtensions;
	}
	
	
	// Returns true iff the last canonization enlarged the extensions
	public boolean extensionsExtended() {
		return extendedExtensions;
	}
	
	
	

}
