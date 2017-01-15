package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


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
	
	private CanonicalRepresentationEfficient store;
	
	public HeapCanonizerRuntimeEfficient(FieldExtensions extensions, boolean ignorePrimitive) {
		this(extensions, ignorePrimitive, null);
	}

	public HeapCanonizerRuntimeEfficient(FieldExtensions extensions, boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		this.extensions = extensions;
		this.ignorePrimitive = ignorePrimitive;
		store = CanonicalRepresentationEfficient.getInstance();
		if (fieldBasedGenClassnames != null)
			store.setFieldBasedGenByClasses(fieldBasedGenClassnames);
	}
	

	public FieldExtensions getExtensions() {
		return extensions; 
	}

	
	// Returns a pair (b1, b2) where b1 iff the extensions were enlarged by this call, and
	// b2 iff tgt is an object visited for the first time in this call
	protected Tuple<Boolean, Boolean> addToExtensions(Object src, Object tgt, Tuple<String, Integer> ftuple) {
		String srccls = CanonicalRepresentation.getClassCanonicalName(src.getClass());
		Integer srcInd = getObjectIndex(src);
		String srcString = srccls + srcInd;
		
		String tgtString;
		Class tgtClass = (tgt == null) ? null : tgt.getClass();
		boolean isTgtNew = false;
		if (tgt == null)
			tgtString = CanonicalRepresentation.getNullRepresentation();
		else if (isIgnoredClass(tgtClass) || (fieldBasedGenByClasses && !belongsToFieldBasedClasses(tgtClass)))
			//tgtString = CanonicalRepresentation.getDummyObjectRepresentation();
			return new Tuple<Boolean, Boolean>(false, false);
		else if (CanonicalRepresentation.isClassPrimitive(tgtClass)) {
			if (ignorePrimitive) 
				tgtString = CanonicalRepresentation.getDummyObjectRepresentation();
			else {
				tgtString = tgt.toString();
				// trim string values to length maxStringSize
				if (tgtClass == String.class && tgtString.length() > CanonicalRepresentation.MAX_STRING_SIZE)
					tgtString = tgtString.substring(0, CanonicalRepresentation.MAX_STRING_SIZE);
			}
		}
		else {
			String tgtcls = CanonicalRepresentation.getClassCanonicalName(tgtClass);
			Tuple<Integer, Integer> indt = assignIndexToObject(tgt);
			isTgtNew = indt.getFirst() == -1;
			tgtString = tgtcls + indt.getSecond();
		}
		
		return new Tuple<Boolean, Boolean>(
				extensions.addPairToField(ftuple.getFirst(), srcString, tgtString),
				isTgtNew);
	}

	
	// Canonize the heap in a breadth first manner, starting at root,
	// and enlarge the extensions during the process. 
	// Returns true iff at least an element is added to the extensions.
	public boolean traverseBreadthFirstAndEnlargeExtensions(Object root) {

		store.clear();
		extendedExtensions = false;
		
  		LinkedList<CanonizerObject> toVisit = new LinkedList<CanonizerObject>();
  	
  		CanonizerObject croot = store.addObject(root);
  		if (croot == null) return false;
  		
  		
  		toVisit.addLast(root);
  		while (!toVisit.isEmpty()) {
  			
  			Object obj = toVisit.removeFirst();
  			
  			// save obj in store
  			// if it was not visited (saved) before then
  				// for all tgt = obj.f for some field f
  					// put tgt in toVisit queue
  					// enlarge extensions with obj and tgt
  				
  			
  			
  			
  			
  			
  			
  			// CanonizerObject co = canonicalRep.canonizeObject(obj);
  			
  			Class objClass = obj.getClass();

  			if (obj == null ||
  					CanonicalRepresentation.classIndexPrimitive.get(index) ||
  					CanonicalRepresentation.isIgnoredClass(
  				continue;
  			

  			if (objClass.isArray()) {
				// Process an array object. Add a dummy field for each of its elements
				int length = Array.getLength(obj);
				for (int i = 0; i < length; i++) {
					Object target = Array.get(obj, i);
					
					Tuple<String, Integer> ftuple = CanonicalRepresentation.getArrayFieldCanonicalNameAndIndex(objClass, i);
					Tuple<Boolean, Boolean> addRes = addToExtensions(obj, target, ftuple);
					
					extendedExtensions |= addRes.getFirst();

					if (addRes.getSecond())
						// target is an object of a non ignored class that was encountered for the first time.
						// Enqueue it for its fields to be inspected in a following iteration.
						toVisit.addLast(target);
				}
			}
			else {
				// Process a non array, non primitive object.
				// Use its field values to enlarge the extensions
				// FIXME: Go back to the unsorted version for better performance
				for (Field fld: getAllClassFieldsSortedByName(objClass)) {
				//for (Field fld: getAllClassFields(objClass)) {
					fld.setAccessible(true);
					Object target;
					try {
						target = fld.get(obj);
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println("FAILURE in field: " + fld.toString());
						// Cannot happen
						throw new RuntimeException("ERROR: Illegal access to an object field during canonization");
					}

					Tuple<String, Integer> ftuple = CanonicalRepresentation.getFieldCanonicalNameAndIndex(fld);
					Tuple<Boolean, Boolean> addRes = addToExtensions(obj, target, ftuple);
					
					extendedExtensions |= addRes.getFirst();
					
					if (addRes.getSecond())
						// target is an object of a non ignored class that was encountered for the first time.
						// Enqueue it for its fields to be inspected in a following iteration.
						toVisit.addLast(target);
					// System.out.println(CanonicalRepresentation.getFieldCanonicalName(fld));
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
