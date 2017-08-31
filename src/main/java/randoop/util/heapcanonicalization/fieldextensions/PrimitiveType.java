package randoop.util.heapcanonicalization.fieldextensions;

import java.math.BigDecimal;
import java.math.BigInteger;

public enum PrimitiveType {
	INTEGER ("java.lang.Integer"),
	SHORT ("java.lang.Short"),
	CHAR ("java.lang.Character"),
	BYTE ("java.lang.Byte"),
	BOOLEAN ("java.lang.Boolean"),
	LONG ("java.lang.Long"),
	FLOAT ("java.lang.Float"),
	DOUBLE ("java.lang.Double"),
	STRING ("java.lang.String"),
	ENUM (""),
	BIGINTEGER ("java.math.BigInteger"), 
	BIGDECIMAL ("java.lang.BigDecimal");
	
	
    private final String className;       

    private PrimitiveType(String s) {
        className = s;
    }
    
    public String getClassName() {
    	return className;
    }
    
	public static PrimitiveType fromName(String s) {
		switch (s) {
		case "int": 
			return INTEGER;
		case "short":
			return SHORT;
		case "char":
			return CHAR;
		case "byte":
			return BYTE;
		case "boolean":
			return BOOLEAN;
		case "long":
			return LONG;
		case "float":
			return FLOAT;
		case "double":
			return DOUBLE;
		default:
			throw new BugInPrimitiveTypeCanonicalization("Primitive type " + s + " not supported");
		}
	}
	
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
			throw new BugInPrimitiveTypeCanonicalization("Primitive type " + o.getClass() + " not supported");
	}
}
