package randoop.util.fieldbasedcontrol;

import java.io.IOException;

public interface FieldExtensionsIndexes {

	public void addField();
	
	public boolean addPairToField(CanonizerField field, CanonizerObject o1, CanonizerObject o2);
			
	public boolean addPairToField(Integer fieldIndex, Integer c1Index, Integer c2Index, Integer o1Index, String o2);
	
	public boolean fieldContainsPair(Integer fieldIndex, Integer c1Index, Integer c2Index, Integer o1Index, String o2);
	
	public FieldExtensionsStrings toFieldExtensionsStrings();	
	
	public void toFile(String filename) throws IOException;

	public int size();
	
	public boolean addAllPairs(FieldExtensionsIndexes currHeapExt);
	
	public boolean testEnlarges(FieldExtensionsIndexes other);
	
	public String toIndexesString();

}
