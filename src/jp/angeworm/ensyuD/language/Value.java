package jp.angeworm.ensyuD.language;

public class Value {
	public String value;
	public Type   type;
	
	public Value(String value, Type type) {
		super();
		this.value = value;
		this.type  = type;
	}
	
	public Value(String value, String type) {
		super();
		this.value = value;
		this.type  = new Type(type);
	}
}
