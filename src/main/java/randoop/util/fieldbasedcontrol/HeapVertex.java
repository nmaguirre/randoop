package randoop.util.fieldbasedcontrol;

import java.util.Objects;

public class HeapVertex {

	private Object object;

	private Integer index;

	public HeapVertex(Object object) {
		super();
		this.object = object;
		this.index = -1;
	}

	
	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
	

	public Integer getIndex() {
		return index;
	}


	public void setIndex(Integer index) {
		this.index = index;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(object);
	}
	
	@Override
	public boolean equals(Object obj) {
		return Objects.equals(object, obj);
	}
	
	public String toString() {
		return CanonicalRepresentation.getVertexCanonicalName(this);
	}
	
}
