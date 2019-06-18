package com.yoloho.dao.api.filter;

import java.util.HashMap;
import java.util.Map;

public class FieldCommand implements QueryCommand {
    public static enum Operator {
        equal("EQ"), 
        notEqual("NEQ"), 
        lessThan("LT"), 
        greatThan("GT"), 
        lessOrEqual("LE"), 
        greatOrEqual("GE"),
        inJoinString("INJOINSTR"), 
        like("LK"), 
        startsWith("LFK"), 
        endsWith("RHK"), 
        isNull("NULL"), 
        isNotNull("NOTNULL"), 
        in("IN"), 
        notIn("NOTIN"), 
        lessOrEqualTimestamp("LETS"), 
        greatOrEqualTimestamp("GETS");
        static final private Map<String, Operator> mapping = new HashMap<>();
        static {
            for (Operator op : Operator.values()) {
                mapping.put(op.getName(), op);
            }
        }
        String name;
        private Operator(String val) {
            name = val;
        }
        public String getName() {
            return name;
        }
        public static Operator fromString(String name) {
            if (!mapping.containsKey(name)) {
                throw new RuntimeException("illegal name for Operator: " + name);
            }
            return mapping.get(name);
        }
    }
    public static enum Type {
        String("S"),
        Number("NUMBER"),
        BigDecimal("BD"),
        Date("D"),
        beginOfDay("DL"),
        endOfDay("DG"),
        List("LIST"),
        Collection("COLLECTION"),
        Expression("EXPR");
        
        static final private Map<String, Type> mapping = new HashMap<>();
        static {
            for (Type type : Type.values()) {
                mapping.put(type.getName(), type);
            }
        }
        String name;
        private Type(String val) {
            name = val;
        }
        public String getName() {
            return name;
        }
        public static Type fromString(String name) {
            if (!mapping.containsKey(name)) {
                throw new RuntimeException("illegal name for Type: " + name);
            }
            return mapping.get(name);
        }
    }
    private static final long serialVersionUID = 6319129308206484932L;
    private String property;
	private Object value;
	private Operator operation;

	public FieldCommand(String property, Object value, Operator operation, DynamicQueryFilter filter) {
		this.property = property;
		this.value = value;
		this.operation = operation;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Operator getOperation() {
		return operation;
	}

	public void setOperation(Operator operation) {
		this.operation = operation;
	}
}
