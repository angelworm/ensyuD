package jp.angeworm.ensyuD.language;

public class IfSentence implements Sentence {
	public Value condition;
	public Sentence consequence;
	public Sentence alternative;
	
	public IfSentence(Value cond, Sentence consequence,
			Sentence alternative) {
		this.condition = cond;
		this.consequence = consequence;
		this.alternative = alternative;
	}
	public IfSentence(Value condition, Sentence consequence) {
		this.condition = condition;
		this.consequence = consequence;
		this.alternative = null;
	}
	@Override
	public String toString() {
		return "if " + condition + " then " + consequence + " else " + alternative;
	}
}
