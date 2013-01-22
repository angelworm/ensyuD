package jp.angeworm.ensyuD.language;

public class Variable {
	public String name;
	public Type   type;

	public Variable(String name2, Type type2) {
		name = name2;
		type = type2;
	}
	public Variable(String name2, String type2) {
		name = name2;
		type = new Type(type2);
	}
	public Variable() {
		name = null;
		type = null;
	}
	
	public String getName() {
		return name;
	}
}
