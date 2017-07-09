package randoop.util.heapcanonization;

public class CanonicalObject {

	private final Object object;
	private final CanonicalClass clazz;
	private int index;

	public CanonicalObject(Object obj, CanonicalClass clazz, int index) {
		this.object = obj;
		this.clazz = clazz;
		this.index = index;
	}

	public Object getObject() {
		return object;
	}

	public CanonicalClass getCanonicalClass() {
		return clazz;
	}

	public int getIndex() {
		return index;
	}
	
	public boolean isNull() {
		return object == null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + index;
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
		CanonicalObject other = (CanonicalObject) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	
	public boolean isPrimitive() {
		return getCanonicalClass().isPrimitive();
	}

	public String toString() {
		if (isNull())
			return "[type=null,index=" + index + "]";
		else if (isPrimitive())
			return "[type=" + clazz.getName() + ",value=" + getObject().toString() + ",hash=" + getObject().hashCode() + "]";
		else
			return "[type=" + clazz.getName() + ",index=" + index + "]";
	}
	
	
}
