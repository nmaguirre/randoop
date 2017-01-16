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

// TODO: Change its name to heap traversal
public class HeapCanonizerRuntimeEfficient {
	
	protected FieldExtensions extensions;
	
	private boolean extendedExtensions;

	protected boolean ignorePrimitive;
	
	private CanonicalHeapStore store;
	
	public HeapCanonizerRuntimeEfficient(FieldExtensions extensions, boolean ignorePrimitive) {
		this(extensions, ignorePrimitive, null);
	}

	public HeapCanonizerRuntimeEfficient(FieldExtensions extensions, boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		this.extensions = extensions;
		this.ignorePrimitive = ignorePrimitive;
		store = CanonicalHeapStore.getInstance();
		if (fieldBasedGenClassnames != null)
			store.setFieldBasedGenByClasses(fieldBasedGenClassnames);
	}
	

	public FieldExtensions getExtensions() {
		return extensions; 
	}

	
	// Returns a pair (b1, b2) where b1 iff the extensions were enlarged by this call, and
	// b2 iff tgt is an object visited for the first time in this call
	protected boolean addToExtensions(CanonizerObject src, CanonizerObject tgt, CanonizerField fld) {
		String srccls = src.cc.name;
		Integer srcInd = src.index;
		String srcString = srccls + srcInd;	
		
		String tgtString;
		if (tgt == null)
			tgtString = store.getNullRepresentation();
		else if (tgt.primitive()) {
			if (ignorePrimitive) 
				tgtString = store.getDummyObjectRepresentation();
			else {
				tgtString = tgt.obj.toString();
				// trim string values to length maxStringSize
				if (tgt.cc.cls == String.class && tgtString.length() > CanonicalRepresentation.MAX_STRING_SIZE)
					tgtString = tgtString.substring(0, CanonicalRepresentation.MAX_STRING_SIZE);
			}
		}
		else if (tgt.ignored())		
			//tgtString = CanonicalRepresentation.getDummyObjectRepresentation();
			return false;
		else {
			String tgtcls =	tgt.cc.name;
			tgtString = tgtcls + tgt.index; 
		}
		
		return extensions.addPairToField(fld.name, srcString, tgtString);
	}

	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public boolean traverseBreadthFirstAndEnlargeExtensions(Object root) {
		store.clear();
		extendedExtensions = false;
  	
  		CanonizerObject croot = store.addObject(root);
		if (croot == null || croot.ignored()) 
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
					if (newcobj != null && !newcobj.ignored())
						toVisit.add(newcobj);

					CanonizerField arrayDummy = new CanonizerField(store.canonizeArrayField(cobj.cc, i));
					if (addToExtensions(cobj, newcobj, arrayDummy))
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
					if (newcobj != null && !newcobj.ignored())
						toVisit.add(newcobj);

					if (addToExtensions(cobj, newcobj, cf))
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
