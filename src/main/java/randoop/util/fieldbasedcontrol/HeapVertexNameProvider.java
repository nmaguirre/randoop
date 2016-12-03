package randoop.util.fieldbasedcontrol;

import org.jgrapht.ext.StringNameProvider;

public class HeapVertexNameProvider<V> extends StringNameProvider<V> {

	public String getVertexName(V vertex) {
		HeapVertex v = (HeapVertex) vertex;
		if (v.getObject() == null) 
			return vertex.toString();
		else if (v.getObject() instanceof String) {
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
		else if (v.getObject().getClass().isArray()) {
			String name = vertex.toString();
			name = name.replace("[", "");
			name = name.replace("]", "");
			return "ARR_" + name;		
		}
		/*else if (v.getObject().getClass() == java.util.Vector.class) {
			String name = vertex.toString();
			name = name.replace("[", "");
			name = name.replace("]", "");
			return "VEC_" + name;		
		}*/		
		else
			return vertex.toString();
	}
	
	

}
