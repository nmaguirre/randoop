package randoop.generation;

import java.util.List;
import java.util.Set;

import randoop.fieldextensions.ExtensionsCollectorInOutVisitor;
import randoop.fieldextensions.ExtensionsCollectorInVisitor;
import randoop.fieldextensions.ExtensionsCollectorOutVisitor;
import randoop.main.GenInputsAbstract;
import randoop.main.GenInputsAbstract.FieldBasedGen;
import randoop.operation.TypedOperation;

public class ForwardGeneratorFactory {

	public static AbstractGenerator create(FieldBasedGen field_based_gen, List<TypedOperation> model,
			Set<TypedOperation> observers, long timelimit, int inputlimit, int outputlimit, ComponentManager componentMgr,
			RandoopListenerManager listenerMgr) {
		// TODO Auto-generated method stub
		if (field_based_gen == FieldBasedGen.DISABLED)
			return new ForwardGenerator(
					model, observers, timelimit, inputlimit, outputlimit, componentMgr, listenerMgr);
		
		FBForwardGenerator res = new FBForwardGenerator(
				model, observers, timelimit, inputlimit, outputlimit, componentMgr, listenerMgr);

		
		// FIXME: Very ugly code to set up the execution visitor corresponding to the different approaches. Refactor.
		setFBBehavior(field_based_gen, res);

		return res;
	}

	public static void setFBBehavior(FieldBasedGen field_based_gen, FBForwardGenerator res) {
		// get the names of the clases to be tested using field based generation
		Set<String> fbgClasses = null;
		if (GenInputsAbstract.fbg_classlist != null)
			fbgClasses = GenInputsAbstract.getClassnamesFBG();

		switch (field_based_gen) {
		case GENFILTER: 
			res.setGenerator(new FBGeneratorApproach());
			res.setFilter(new FBInputFilter());

			if (GenInputsAbstract.fbg_precise_observer_detection) 
				res.addExecutionVisitor(new ExtensionsCollectorInOutVisitor(fbgClasses, 
						GenInputsAbstract.fbg_max_objects, 
						GenInputsAbstract.fbg_max_arr_objects, 
						GenInputsAbstract.fbg_max_field_distance,
						true,
						GenInputsAbstract.fbg_observer_after_tests));
			else
				res.addExecutionVisitor(new ExtensionsCollectorInOutVisitor(fbgClasses, 
						GenInputsAbstract.fbg_max_objects, 
						GenInputsAbstract.fbg_max_arr_objects, 
						GenInputsAbstract.fbg_max_field_distance));	
			break;
		case GEN:
			res.setGenerator(new FBGeneratorApproach());
			res.setFilter(new RandoopInputFilter());

			if (GenInputsAbstract.fbg_precise_observer_detection) 
				res.addExecutionVisitor(new ExtensionsCollectorOutVisitor(fbgClasses, 
						GenInputsAbstract.fbg_max_objects, 
						GenInputsAbstract.fbg_max_arr_objects, 
						GenInputsAbstract.fbg_max_field_distance,
						true,
						GenInputsAbstract.fbg_observer_after_tests));
			else
				res.addExecutionVisitor(new ExtensionsCollectorOutVisitor(fbgClasses, 
						GenInputsAbstract.fbg_max_objects, 
						GenInputsAbstract.fbg_max_arr_objects, 
						GenInputsAbstract.fbg_max_field_distance));	
			break;
		case FILTER:			
			res.setGenerator(new RandoopGeneratorApproach());
			res.setFilter(new FBInputFilter());

			res.addExecutionVisitor(new ExtensionsCollectorInVisitor(fbgClasses, 
					GenInputsAbstract.fbg_max_objects, 
					GenInputsAbstract.fbg_max_arr_objects, 
					GenInputsAbstract.fbg_max_field_distance));	
			break;
		}
	}

}
