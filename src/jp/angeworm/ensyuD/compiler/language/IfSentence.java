package jp.angeworm.ensyuD.compiler.language;

public class IfSentence implements Sentence {
	public Expression condition;
	public Sentence consequence;
	public Sentence alternative;
	
	public IfSentence(Expression condition, Sentence consequence,
			Sentence alternative) {
		this.condition = condition;
		this.consequence = consequence;
		this.alternative = alternative;
	}
	public IfSentence(Expression condition, Sentence consequence) {
		this.condition = condition;
		this.consequence = consequence;
		this.alternative = null;
	}
}
