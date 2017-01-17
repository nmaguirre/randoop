package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.Set;


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
	
	
	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive) {
		this(ignorePrimitive, null);
	}

	public HeapCanonizerRuntimeEfficient(boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		this.ignorePrimitive = ignorePrimitive;
		this.store = new CanonicalHeapStore();
		if (fieldBasedGenClassnames != null)
			store.setFieldBasedGenByClasses(fieldBasedGenClassnames);
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
		
		if (readableExtensions != null) 
			addToReadableExtensions(src, tgt, fld);
		
		return store.extensions.addPairToField(fld, src, tgt);
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
				for (int i = 0; i < length; i++) {
					
					Object target = Array.get(cobj.obj, i);
					CanonizerObject newcobj = store.addObject(target);
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
					if (!newcobj.ignored() && !newcobj.visited() && !newcobj.primitive() && !newcobj.isNull())
						toVisit.add(newcobj);

					if (!newcobj.ignored() && addToExtensions(cobj, newcobj, cf))
						extendedExtensions = true; 
				}
			}
		}
  		return extendedExtensions;
	}
	
	
	// Returns true iff the last canonization enlarged the extensions
	public boolean extensionsExtended() {
		return extendedExtensions;
	}
	
	
	

}
