package randoop.util.heapcanonicalization.fieldextensions;

import java.math.BigDecimal;
import java.math.BigInteger;

public enum PrimitiveType {
	INTEGER,
	SHORT,
	CHAR,
	BYTE,
	BOOLEAN,
	LONG,
	FLOAT,
	DOUBLE,
	STRING,
	ENUM, 
	BIGINTEGER, 
	BIGDECIMAL;
	
	public static PrimitiveType fromObject(Object o) {
		if (o.getClass() == int.class || o.getClass() == Integer.class)
			return INTEGER;
		else if (o.getClass() == short.class || o.getClass() == Short.class)
			return SHORT;
		else if (o.getClass() == char.class || o.getClass() == Character.class) 
			return CHAR;
		else if (o.getClass() == byte.class || o.getClass() == Byte.class)
			return BYTE;
		else if (o.getClass() == boolean.class || o.getClass() == Boolean.class) 
			return BOOLEAN;
		else if (o.getClass() == long.class || o.getClass() == Long.class)
			return LONG;
		else if (o.getClass() == float.class || o.getClass() == Float.class) 
			return FLOAT;
		else if (o.getClass() == double.class || o.getClass() == Double.class)
			return DOUBLE;
		else if (o.getClass() == String.class) 
			return STRING;
		else if (o.getClass().isEnum()) 
			return ENUM;
		else if (o.getClass() == BigInteger.class)
			return BIGINTEGER;
		else if (o.getClass() == BigDecimal.class)
			return BIGDECIMAL;
		else 
			throw new BugInFieldExtensionsCanonicalization("Bitwise canonization of " + o.getClass() + " not supported");
	}
}
