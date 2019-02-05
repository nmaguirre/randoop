package randoop.generation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import randoop.operation.TypedClassOperation;
import randoop.operation.TypedOperation;
import randoop.sequence.ClassLiterals;
import randoop.sequence.PackageLiterals;
import randoop.sequence.Sequence;
import randoop.sequence.SequenceCollection;
import randoop.sequence.SequenceCollectionBE;
import randoop.types.ClassOrInterfaceType;
import randoop.types.JavaTypes;
import randoop.types.PrimitiveType;
import randoop.types.Type;
import randoop.util.ListOfLists;
import randoop.util.SimpleList;

/**
 * Stores and provides means to access the component sequences generated during
 * a run of Randoop. "Component sequences" are sequences that Randoop uses to
 * create larger sequences.
 *
 * This class manages different collections of component sequences:
 *
 * <ul>
 * <li>General components that can be used as input to any method in any class.
 * <li>Class literals: components representing literal values that apply only to
 * a specific class and should not be used as inputs to other classes.
 * <li>Package literals: analogous to class literals but at the package level.
 * </ul>
 *
 * SEED SEQUENCES. Seed sequences are sequences that were not created during the
 * generation process but obtained via other means. They include (1) sequences
 * passed via the constructor, (2) class literals, and (3) package literals. The
 * only different treatment of seed sequences is during calls to the
 * clearGeneratedSequences() method, which removes only general, non-seed
 * components from the collection.
 */

@Deprecated
public class ComponentManagerBE extends ComponentManager {

  /**
   * Create an empty component manager, with an empty seed sequence set.
   */
  public ComponentManagerBE() {
    gralComponents = new SequenceCollectionBE();
    gralSeeds = Collections.unmodifiableSet(Collections.<Sequence>emptySet());
  }

}