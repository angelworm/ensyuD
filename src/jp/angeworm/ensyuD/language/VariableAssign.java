package jp.angeworm.ensyuD.language;

public class VariableAssign extends Value{
	public Value index;
	
	public VariableAssign(String name2, Type type2, Value index2) {
		super(name2, type2);
		index = index2;
	}
	public VariableAssign(String name2, String type2, Value index2) {
		super(name2, type2);
		index = index2;
	}
	public VariableAssign(String name2, String type2) {
		super(name2, type2);
		index = null;
	}
	@Override
	public String toString() {
		return super.toString() + (index == null ? "" : "[" + index + "]");
	}
	
}
