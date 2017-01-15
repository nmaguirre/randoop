package randoop.util.fieldbasedcontrol;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
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

public abstract class HeapCanonizerObjectIndexes extends HeapCanonizer {
	

	public HeapCanonizerObjectIndexes(FieldExtensions extensions, boolean ignorePrimitive) {
		super(extensions, ignorePrimitive);
	}
	
	
	public HeapCanonizerObjectIndexes(FieldExtensions extensions, boolean ignorePrimitive,
			Set<String> fieldBasedGenClassnames) {
		super(extensions, ignorePrimitive, fieldBasedGenClassnames);
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
	
		/*
		if (!test)
			return new Tuple<Boolean, Boolean>(
				extensions.addPairToField(fieldname, srcString, tgtString),
				isTgtNew);
		else 
			return new Tuple<Boolean, Boolean>(
				!extensions.pairBelongsToField(fieldname, srcString, tgtString),
				isTgtNew);*/
	}
	
}
