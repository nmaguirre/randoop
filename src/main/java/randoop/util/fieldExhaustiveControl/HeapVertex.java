package randoop.util.fieldExhaustiveControl;

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
		HeapVertex other = (HeapVertex) obj;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (object != other.object)
			return false;
		return true;
	}
	
	public String toString() {
		if (object == null) return "null";
		return object.toString();
	}
  
}
