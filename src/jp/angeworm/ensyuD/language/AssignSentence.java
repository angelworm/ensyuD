package jp.angeworm.ensyuD.language;

public class AssignSentence implements Sentence {
	public VariableAssign lvalue;
	public Value rvalue;
	
	public AssignSentence(VariableAssign lvalue, Value rvalue) {
		super();
		this.lvalue = lvalue;
		this.rvalue = rvalue;
	}
}
