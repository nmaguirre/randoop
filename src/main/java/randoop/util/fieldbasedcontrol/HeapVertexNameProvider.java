package randoop.util.fieldbasedcontrol;

import org.jgrapht.ext.StringNameProvider;

public class HeapVertexNameProvider<V> extends StringNameProvider<V> {

	public String getVertexName(V vertex) {
		HeapVertex v = (HeapVertex) vertex;
		String name;
		if (v.getObject() == null) 
			return vertex.toString();
		else if (v.getObject() instanceof String) {
			name = vertex.toString();
			name = name.replace("$", "");
			name = name.replace(".", "_");		
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
			name = vertex.toString();
			name = name.replace("$", "");
			name = name.replace(".", "_");
			name = name.replace("[", "");
			name = name.replace("]", "");
			name = name.replace(";", "");
			return "ARR_" + name;		
		}
		/*else if (v.getObject().getClass() == java.util.Vector.class) {
			String name = vertex.toString();
			name = name.replace("[", "");
			name = name.replace("]", "");
			return "VEC_" + name;		
		}*/		
		else {
			name = vertex.toString();
			name = name.replace("$", "");
			name = name.replace(".", "_");
			return name;
		}
	}
	
	

}
