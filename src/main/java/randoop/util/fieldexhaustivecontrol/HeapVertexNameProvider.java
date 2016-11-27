package randoop.util.fieldexhaustivecontrol;

import org.jgrapht.ext.StringNameProvider;

public class HeapVertexNameProvider<V> extends StringNameProvider<V> {

	public String getVertexName(V vertex) {
		HeapVertex v = (HeapVertex) vertex;
		if (v.getObject() instanceof String) {
			String name = vertex.toString();
			name = name.replace("!", "");		
			name = name.replace("[", "");
			name = name.replace("]", "");
			name = name.replace(",", "");
			name = name.replace(" ", "");
			name = name.replace("-", "");
			if (name.equals(""))
				name = "emptyStr";
			return name;
		}
		else
			return vertex.toString();
	}
	
	

}
