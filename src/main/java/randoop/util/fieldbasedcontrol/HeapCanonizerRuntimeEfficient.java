package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Set;

import randoop.generation.AbstractGenerator;
import randoop.util.heapcanonicalization.ExtendExtensionsResult;


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
	
	protected boolean ignorePrimitive;
	
	public CanonicalHeapStore store;
	
	private static final int DEFAULT_MAX_OBJECTS = 10000;
	private static final int DEFAULT_MAX_CLASS_OBJECTS = 1000;
	private static final int DEFAULT_MAX_ARRAY = 1000;
	private static final int DEFAULT_MAX_STRING = 1000;
	private int maxArray;
	private int maxStringLength;
	private boolean dropTestsExceedingLimits;
	// To canonize non primitive objects
	public FieldExtensionsIndexes extensions;
	// To canonize primitive values 
	public FieldExtensionsIndexes primitiveExtensions;

	public boolean saveToDifferentialExtensions = false;
	
	private boolean arrayWarningShown = false;
	
		
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive) {
		this(ignorePrimitive, null, DEFAULT_MAX_OBJECTS, DEFAULT_MAX_CLASS_OBJECTS, DEFAULT_MAX_STRING, DEFAULT_MAX_ARRAY, false);
	}

	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		this(ignorePrimitive, null, DEFAULT_MAX_OBJECTS, DEFAULT_MAX_CLASS_OBJECTS, DEFAULT_MAX_STRING, DEFAULT_MAX_ARRAY, false);
	}
	
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive, int maxGlobalObjects, int maxClassObjects, int maxStringLength, int maxArray, boolean dropTestsExceedingLimits) {
		this(ignorePrimitive, null, maxGlobalObjects, maxClassObjects, maxStringLength, maxArray, dropTestsExceedingLimits);
	}
	
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames, int maxGlobalObjects, int maxClassObjects, int maxStringLength, int maxArray, boolean dropTestsExceedingLimits) {
		this.ignorePrimitive = ignorePrimitive;
		this.dropTestsExceedingLimits = dropTestsExceedingLimits;
		this.store = new CanonicalHeapStore(maxGlobalObjects, maxClassObjects, maxStringLength, dropTestsExceedingLimits);
		if (fieldBasedGenClassnames != null)
			store.setFieldBasedGenByClasses(fieldBasedGenClassnames);
		
		/*
		if (AbstractGenerator.field_based_gen_differential_runtime_checks)
			activateReadableExtensions();
			*/
		
		this.maxStringLength = maxStringLength;
		this.maxArray = maxArray;
		this.extensions = new FieldExtensionsIndexesMap(store);
		this.primitiveExtensions = new FieldExtensionsIndexesMap(store);
	}
	
	
	public FieldExtensionsIndexes getExtensions() {
		return extensions; 
	}

	public FieldExtensionsIndexes getPrimitiveExtensions() {
		return primitiveExtensions; 
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
				// trim string values to max string length
			if (tgt.cc.cls == String.class && tgtString.length() >= maxStringLength)
				tgtString = tgtString.substring(0, maxStringLength);
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
	protected boolean addToExtensions(CanonizerObject src, CanonizerObject tgt, CanonizerField fld, FieldExtensionsIndexes extensions) {
		
		if (ignorePrimitive && (tgt.primitive() || tgt.isNull()))
			return false;
		
		boolean newRes = extensions.addPairToField(fld, src, tgt);

		if (readableExtensions != null && saveToDifferentialExtensions) {
			boolean oldRes = addToReadableExtensions(src, tgt, fld); 
			
//			if (oldRes != newRes) { 
//				System.out.println("ERROR: Differential test failed when adding to the extensions");
//				throw new RuntimeException("ERROR: Differential test failed when adding to the extensions");
//			}
				
		}	

		return newRes;
	}



	public ExtendExtensionsResult traverseBreadthFirstAndEnlargeExtensions(Object root) {
		return traverseBreadthFirstAndEnlargeExtensions(root, extensions);
	}
	
	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public ExtendExtensionsResult traverseBreadthFirstAndEnlargeExtensions(Object root, FieldExtensionsIndexes extensions) {
		if (root == null) return ExtendExtensionsResult.NOT_EXTENDED;
  	
		ExtendExtensionsResult extended = ExtendExtensionsResult.NOT_EXTENDED;
		/*
		CanonizerObject croot = store.addObject(root);
		if (croot.ignored() || croot.primitive() || croot.isNull()) 
			return false;
		*/
		
		CanonizerObject croot = store.addObject(new DummyHeapRoot(root));
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
					if (!arrayWarningShown) {
						String message = "> FIELD BASED GENERATION WARNING: Array length limit (" + maxArray + ") exceeded for class " + cobj.cc.name;
						System.out.println(message);
						arrayWarningShown = true;
					}
					if (FieldBasedGenLog.isLoggingOn()) {
						String message = "> FIELD BASED GENERATION WARNING: Array length limit (" + maxArray + ") exceeded for class " + cobj.cc.name;
						FieldBasedGenLog.logLine(message);
					}	
					
					if (dropTestsExceedingLimits) {
						store.clear();
						throw new RuntimeException("ERROR IN CANONIZATION: Bounds limit not implemented");
						// return ExtendedExtensionsResult.LIMITS_EXCEEDED;
					}
				}
				for (int i = 0; i < length; i++) {
					
					Object target = Array.get(cobj.obj, i);
					CanonizerObject newcobj = store.addObject(target);
					// Max number of stored objects for the class exceeded
					if (newcobj == null) {
						if (dropTestsExceedingLimits) {
							store.clear();
							throw new RuntimeException("ERROR IN CANONIZATION: Bounds limit not implemented");
							// return ExtendedExtensionsResult.LIMITS_EXCEEDED;
						}
						else
							continue;
					}

					if (!newcobj.ignored() && !newcobj.visited() && !newcobj.primitive() && !newcobj.isNull())
						toVisit.add(newcobj);

					CanonizerField arrDummyFld = store.canonizeArrayField(cobj.cc, i);
					if (!newcobj.ignored() && addToExtensions(cobj, newcobj, arrDummyFld, extensions))
						extended = ExtendExtensionsResult.EXTENDED; 
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
					if (newcobj == null) {
						if (dropTestsExceedingLimits) {
							store.clear();
							throw new RuntimeException("ERROR IN CANONIZATION: Bounds limit not implemented");
							//return ExtendedExtensionsResult.LIMITS_EXCEEDED;
						}
						else
							continue;
					}
					
					if (!newcobj.ignored() && !newcobj.visited() && !newcobj.primitive() && !newcobj.isNull())
						toVisit.add(newcobj);

					if (!newcobj.ignored() && addToExtensions(cobj, newcobj, cf, extensions))
						extended = ExtendExtensionsResult.EXTENDED;
				}
			}
		}
  		
  		/*
		if (readableExtensions != null) {
			
			if (!readableExtensions.equals(store.extensions.toFieldExtensionsStrings())) {
				System.out.println("ERROR: Differential test failed. Different versions of the extensions differ");
				throw new RuntimeException("ERROR: Differential test failed. Different versions of the extensions differ");
			}
		}	
  		*/

		store.clear();
  		
		return extended;
	}
	
	
	/*
	// Returns true iff the last canonization enlarged the extensions
	public boolean extensionsExtended() {
		return extendedExtensions;
	}
	*/
	
	

}
