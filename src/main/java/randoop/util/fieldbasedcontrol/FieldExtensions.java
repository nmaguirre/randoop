package randoop.util.fieldbasedcontrol;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

public interface FieldExtensions {
	
	public boolean addPairToField(String field, String src, String tgt);

	public boolean pairBelongsToField(String field, String src, String tgt);
	
	public void toFile(String filename) throws IOException;

	public int size(); 

}
