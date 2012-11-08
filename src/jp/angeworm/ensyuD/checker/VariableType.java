package jp.angeworm.ensyuD.checker;

public enum VariableType {
	VOID,
	BOOLEAN { public boolean isPrimitive(){return true;} },
	INTEGER { public boolean isPrimitive(){return true;} },
	CHAR    { public boolean isPrimitive(){return true;} },
	BOOLEAN_ARRAY { public boolean isArray(){ return true;} },
	INTEGER_ARRAY { public boolean isArray(){ return true;} },
	CHAR_ARRAY    { public boolean isArray(){ return true;} },
	PROCEDURE;

	public boolean isPrimitive(){return false;}
	public boolean isArray(){ return false;}
}
