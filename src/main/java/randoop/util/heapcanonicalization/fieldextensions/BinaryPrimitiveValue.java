package randoop.util.heapcanonicalization.fieldextensions;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BinaryPrimitiveValue {

	private Map<Integer, Set<Character>> bitwiseValue = new HashMap<>();
	
	
	private String padLeftWithZeros(String str, int n) {
		return String.format("%0"+(n-str.length())+"d%s", 0, str); 
	}
	
	public BinaryPrimitiveValue() { }
	
	public BinaryPrimitiveValue(Object o) {
		int n;
		switch (PrimitiveType.fromObject(o)) {
		case INTEGER:
			addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString((int) o), 32));
			break;
		case SHORT:
			addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString((int) o), 16));
			break;
		case CHAR:	
			n = (char) o;
			addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString(n), 8));
			break;
		case BYTE:
			n = (byte) o;
			addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString(n), 8));
			break;
		case BOOLEAN:
			if (((boolean) o) == true)
				n = 1;
			else
				n = 0;
			addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString(n), 1));
			break;
		case LONG:
			addFromBitwiseString(padLeftWithZeros(Long.toBinaryString((long) o), 64));
			break;
		case FLOAT:
			addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString(Float.floatToRawIntBits((float) o)), 32));
			break;
		case DOUBLE:
			addFromBitwiseString(padLeftWithZeros(Long.toBinaryString(Double.doubleToRawLongBits((double) o)), 64));
			break;
		case STRING:
			String str = (String) o;
			for (int i = 0; i < str.length(); i++) {
				n = str.charAt(i);
				addFromBitwiseString(padLeftWithZeros(Integer.toBinaryString(n), 8));
			}
			break;
		case ENUM:
			addFromBitwiseString(Integer.toBinaryString(((Enum<?>)o).ordinal()));
			break;
		}
		/* TODO: Implement support for these types
	  				|| clazz == BigInteger.class
	  				|| clazz == BigDecimal.class
					*/ 
	}
	
	private void addFromBitwiseString(String str) {
		int currSize = bitwiseValue.size();
		for (int i = 0; i < str.length(); i++) {
			int currInd = currSize + i;
			Set<Character> newSet = new HashSet<>();
			newSet.add(str.charAt(i));
			bitwiseValue.put(currInd, newSet);
		}
	}
	
	public boolean union(BinaryPrimitiveValue other) {
		boolean extended = false;
		int i;
		for (i = 0; i < bitwiseValue.size(); i++) {
			if (other.bitwiseValue.get(i) == null)
				return extended;
			extended |= bitwiseValue.get(i).addAll(other.bitwiseValue.get(i));
		}

		while (i < other.bitwiseValue.size()) {
			bitwiseValue.put(i, new HashSet<>(other.bitwiseValue.get(i)));
			extended = true;
			i++;
		}
		
		return extended;
	}
	
	public String toString() {
		return bitwiseValue.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bitwiseValue == null) ? 0 : bitwiseValue.hashCode());
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
		BinaryPrimitiveValue other = (BinaryPrimitiveValue) obj;
		if (bitwiseValue == null) {
			if (other.bitwiseValue != null)
				return false;
		} else if (!bitwiseValue.equals(other.bitwiseValue))
			return false;
		return true;
	}

	public boolean contains(BinaryPrimitiveValue other) {
		if (this.bitwiseValue.size() != other.bitwiseValue.size())
			return false;
		
		for (int i = 0; i < bitwiseValue.size(); i++) 
			if (!bitwiseValue.get(i).containsAll(other.bitwiseValue.get(i)))
				return false;
	
		return true;
	}
	
	
}
