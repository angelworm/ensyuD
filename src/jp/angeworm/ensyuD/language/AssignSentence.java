package jp.angeworm.ensyuD.language;

public class AssignSentence implements Sentence {
	public Variable lvalue;
	public Expression rvalue;
	
	public AssignSentence(Variable lvalue, Expression rvalue) {
		super();
		this.lvalue = lvalue;
		this.rvalue = rvalue;
	}
}
