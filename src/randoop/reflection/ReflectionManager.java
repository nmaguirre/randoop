package randoop.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import randoop.util.Log;

/**
 * ReflectionManager reflectively visits a {@link Class<?>} instance to apply a set of 
 * {@link ClassVisitor} objects to the class members. Uses a {@link ReflectionPredicate} 
 * and heuristics to determine which classes and class members to visit.
 * 
 * For a non-enum class visits:
 * - all methods satisfying predicate.
 * - all constructors satisfying predicate.
 * - all fields that satisfy predicate and are not hidden. (A hidden field is a member of 
 *   superclass with field of same name in current class. These are accessible via reflection.).
 * - inner enums satisfying predicate.
 * 
 * For an enum visits:
 *  - all enum constants.
 *  - methods of the enum satisfying predicate other than <code>values</code> and <code>valueOf</code>.
 *  - methods defined for enum constants that satisfy predicate.
 *  
 * @author bjkeller
 *
 */
public class ReflectionManager {

  private ReflectionPredicate predicate;
  private ArrayList<ClassVisitor> visitors;

  /**
   * ReflectionManager(ReflectionPredicate) creates a manager object that uses the
   * given predicate to determine which classes, methods and constructors should
   * be visited. The list of visitors is initially empty.
   * 
   * @param predicate is used to determine whether class and its members should be visited.
   */
  public ReflectionManager(ReflectionPredicate predicate) {
    this.predicate = predicate;
    this.visitors = new ArrayList<>();
  }

  /**
   * add(ClassVisitor) registers a {@link ClassVisitor} for use by the 
   * {@link ReflectionManager#apply(Class)} method.
   * 
   * @param visitor a {@link ClassVisitor} object.
   */
  public void add(ClassVisitor visitor) {
    visitors.add(visitor);
  }

  /**
   * apply applies the registered {@link ClassVisitor} objects of this object to the
   * given class.
   *  
   * @param c a {@link Class} object to be visited.
   */
  public void apply(Class<?> c) {
    
    if (predicate.canUse(c)) {

      if (Log.isLoggingOn()) Log.logLine("Applying visitors to class " + c.getName());

      visitBefore(c); //perform any previsit steps
      
      if (c.isEnum()) { //treat enum classes differently
        applyEnum(c);
      } else {

        for (Method m : c.getMethods()) {
          if (Log.isLoggingOn()) {
            Log.logLine(String.format("Considering method %s", m));
          }
          if (predicate.canUse(m)) {
            visitMethod(m);
          }
        }

        for (Constructor<?> co : c.getDeclaredConstructors()) {
          if (predicate.canUse(co)) {
            visitConstructor(co);
          }   
        }

        for (Class<?> ic : c.getDeclaredClasses()) { //look for inner enums
          if (ic.isEnum() && predicate.canUse(ic)) {
            applyEnum(ic);
          }
        }

        applyField(c);

      }
      
      visitAfter(c);
    }
    
  }



  /**
   * applyEnum applies the visitors to the constants and methods of the given enum. 
   * A method is included if it satisfies the predicate, and either is declared in the enum, 
   * or in the anonymous class of some constant.
   * Note that methods will either belong to the enum itself, or to an anonymous class
   * attached to a constant. Ordinarily, the type of the constant is the enum, but when there
   * is an anonymous class for constant e, e.getClass() returns the anonymous class. This is
   * used to check for method overrides (could include Object methods) within the constant.
   * 
   * Heuristically exclude methods <code>values</code> and <code>valueOf</code>
   * since their definition is implicit, and we aren't testing Java enum implementation.
   *
   * @param c enum class object from which constants and methods are extracted
   * @see Enum
   */
  private void applyEnum(Class<?> c) {
    Set<String> overrideMethods = new HashSet<String>();
    for (Object obj : c.getEnumConstants()) {
      Enum<?> e = (Enum<?>)obj;
      visitEnum(e);
      if (!e.getClass().equals(c)) { //does constant have an anonymous class?
        for (Method m : e.getClass().getDeclaredMethods()) {
          overrideMethods.add(m.getName()); //collect any potential overrides
        }
      }
    }
    //get methods that are explicitly declared in the enum
    for (Method m : c.getDeclaredMethods()) {
      if (predicate.canUse(m)) {
        if (!m.getName().equals("values") && !m.getName().equals("valueOf")) {
          visitMethod(m);
        }
      }
    }
    //get any inherited methods also declared in anonymous class of some constant
    for (Method m : c.getMethods()) { 
      if (predicate.canUse(m) && overrideMethods.contains(m.getName())) {
        visitMethod(m);
      }
    }
  }
  
  /**
   * applyField(Class) determines which fields of the given class the visitors
   * will be applied to. Only excludes fields hidden by inheritance that are
   * otherwise still accessible via reflection.
   * 
   * @param c
   */
  private void applyField(Class<?> c) {
    //The set of fields declared in class c is needed to ensure we don't collect
    //inherited fields that are hidden by local declaration
    Set<String> declaredNames = new TreeSet<>(); //get names of fields declared
    for (Field f : c.getDeclaredFields()) {
      declaredNames.add(f.getName());
    }
    for (Field f : c.getFields()) { //for all public fields
      //keep a field that satisfies filter, and is not inherited and hidden by local declaration
      if (predicate.canUse(f) && (!declaredNames.contains(f.getName()) || c.equals(f.getDeclaringClass()))) {
        visitField(f);
      }
    }
  }

  /*
   * visit methods - each applies all of the visitors to its parameter object.
   */

  private void visitField(Field f) {
    for (ClassVisitor v : visitors) {
      v.visit(f);
    }
  }

  private void visitConstructor(Constructor<?> co) {
    for (ClassVisitor v : visitors) {
      v.visit(co);
    }
  }

  private void visitMethod(Method m) {
    for (ClassVisitor v : visitors) {
      v.visit(m);
    }
  }
  
  private void visitEnum(Enum<?> e) {
    for (ClassVisitor v : visitors) {
      v.visit(e);
    }
  }

  private void visitAfter(Class<?> c) {
    for (ClassVisitor v : visitors) {
      v.visitAfter(c);
    }
  }

  private void visitBefore(Class<?> c) {
    for (ClassVisitor v : visitors) {
      v.visitBefore(c);
    }
  }
  
}
