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
	public boolean canConvert(VariableType b){ 
		if(this.equals(b)) return true;
		if(this.equals(VariableType.STRING_LENGTH1)) {
			if(b.equals(VariableType.CHAR)) return true;
			if(b.equals(VariableType.CHAR_ARRAY)) return true;
		}
		if(b.equals(VariableType.STRING_LENGTH1)) {
			if(this.equals(VariableType.CHAR)) return true;
			if(this.equals(VariableType.CHAR_ARRAY)) return true;
		}
		return false;
	}
	public VariableType arrayOf(){ return VOID;}
}
