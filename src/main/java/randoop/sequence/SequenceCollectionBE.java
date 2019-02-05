package randoop.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import randoop.Globals;
import randoop.SubTypeSet;
import randoop.main.GenInputsAbstract;
import randoop.types.Type;
import randoop.util.ArrayListSimpleList;
import randoop.util.ListOfLists;
import randoop.util.Log;
import randoop.util.SimpleList;

/**
 * A collection of sequences that makes its efficient to ask for all the
 * sequences that create a value of a given type.
 *
 * <p>
 * RANDOOP IMPLEMENTATION NOTE.
 * <p>
 *
 * When creating new sequences, Randoop often needs to search for all the
 * previously-generated sequences that create one or more values of a given
 * type. Since this set can contain thousands of sequences, finding these
 * sequences can can be time-consuming and a bottleneck in generation (as we
 * discovered during profiling).
 *
 * <p>
 *
 * This class makes the above search faster by maintaining two data structures:
 *
 * <ul>
 * <li>A map from types to the sets of all sequences that create one or more
 * values of exactly the given type.
 *
 * <li>A set of all the types that can be created with the existing set of
 * sequences. The set is maintained as a {@link SubTypeSet} that allows for
 * quick queries about can-be-used-as relationships among the types in the set.
 * </ul>
 *
 * To find all the sequences that create values of a given type, Randoop first
 * uses the <code>SubTypeSet</code> to find the set <code>S</code> of feasible
 * subtypes in set of sequences, and returns the range of <code>S</code> in the
 * sequence map.
 */

@Deprecated
public class SequenceCollectionBE extends SequenceCollection {


  /**
   * Add a sequence to this collection. This method takes into account the
   * active indices in the sequence. If sequence[i] creates a values of type T,
   * and sequence[i].isActive==true, then the sequence is seen as creating a
   * useful value at index i. More precisely, the method/constructor at that
   * index is said to produce a useful value (and if the user later queries for
   * all sequences that create a T, the sequence will be in the collection
   * returned by the query). How a value is deemed useful or not is left up to
   * the client.
   *
   * @param sequence  the sequence to add to this collection
   */
  public void add(Sequence sequence) {
    List<Type> formalTypes = sequence.getTypesForLastStatement();
    List<Variable> arguments = sequence.getVariablesOfLastStatement();
    assert formalTypes.size() == arguments.size();
    for (int i = 0; i < formalTypes.size(); i++) {
      Variable argument = arguments.get(i);
      assert formalTypes.get(i).isAssignableFrom(argument.getType())
          : formalTypes.get(i).getName()
              + " should be assignable from "
              + argument.getType().getName();
//      if (sequence.isActive(argument.getDeclIndex())) {
        Type type = formalTypes.get(i);
        if (!type.isPrimitive()) {
			typeSet.add(type);
			updateCompatibleMap(sequence, type);
			sequence.addVariableForType(type, argument);
        }
//    }
    }
    checkRep();
  }



}
