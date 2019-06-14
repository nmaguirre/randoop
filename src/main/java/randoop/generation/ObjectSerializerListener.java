package randoop.generation;

import java.util.List;

import randoop.fieldextensions.ObjectSerializer;
import randoop.fieldextensions.Utils;
import randoop.sequence.ExecutableSequence;

public class ObjectSerializerListener implements IEventListener {

	private String serialFile;
	private String serializeClass;
	private ObjectSerializer serializer;

	public ObjectSerializerListener(String serializeClass, String serialFile) {
		this.serialFile = serialFile;
		this.serializeClass = serializeClass;
	}
	
	@Override
	public void explorationStart() {
		this.serializer = new ObjectSerializer(serialFile);
		serializer.open();
	}

	@Override
	public void explorationEnd() {
		serializer.close();
	}

	@Override
	public void generationStepPre() {
		// TODO Auto-generated method stub

	}

	@Override
	public void generationStepPost(ExecutableSequence s) {
		if (!s.genNewObjects()) return;
		
		List<Object> lastStmtObjs = s.getLastStmtObjects();
		
		for (int i: s.getActiveIndexes()) {
			Object o = lastStmtObjs.get(i);
			if (o == null || Utils.isPrimitive(o))
				continue;

			String cls = o.getClass().getName();
			if (cls.equals(serializeClass))
				serializer.serialize(o);
		}
	}

	@Override
	public void progressThreadUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean stopGeneration() {
		// TODO Auto-generated method stub
		return false;
	}

}
