package jp.angeworm.ensyuD.checker;

public enum VariableType {
	VOID,
	BOOLEAN { public boolean isPrimitive(){return true;} },
	INTEGER { public boolean isPrimitive(){return true;} },
	CHAR    { public boolean isPrimitive(){return true;} },
	BOOLEAN_ARRAY { public boolean isArray(){ return true;}
					public VariableType arrayOf(){ return BOOLEAN; }},
	INTEGER_ARRAY { public boolean isArray(){ return true;}
					public VariableType arrayOf(){ return INTEGER; } },
	CHAR_ARRAY    { public boolean isArray(){ return true;}
					public VariableType arrayOf(){ return CHAR; } },
	STRING_LENGTH1{ public boolean isArray(){ return true; }
					public boolean isPrimitive(){return true;}},
	PROCEDURE;

	public boolean isPrimitive(){return false;}
	public boolean isArray(){ return false;}
	public boolean isString(){ return false;}
	public boolean canConvertChar(){ return false;}
	public VariableType arrayOf(){ return VOID;}
}
