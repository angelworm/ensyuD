package jp.angeworm.ensyuD.compiler.language;

public class WhileSentence implements Sentence {
	public Expression condition;
	public Sentence block;
	
	public WhileSentence(Expression condition, Sentence block) {
		super();
		this.condition = condition;
		this.block = block;
	}
}
