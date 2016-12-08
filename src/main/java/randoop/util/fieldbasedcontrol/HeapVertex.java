package randoop.util.fieldbasedcontrol;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return obj.equals(((HeapVertex)obj).getObject());
	}
	
	public String toString() {
		return CanonicalRepresentation.getVertexCanonicalName(this);
	}
	
}
