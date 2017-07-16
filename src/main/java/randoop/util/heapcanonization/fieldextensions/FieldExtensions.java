package randoop.util.heapcanonization.fieldextensions;

import java.io.IOException;

public interface FieldExtensions {
	
	public boolean addPairToField(String field, String src, String tgt);

	public boolean pairBelongsToField(String field, String src, String tgt);
	
	public void toFile(String filename) throws IOException;

	public int size(); 

}
