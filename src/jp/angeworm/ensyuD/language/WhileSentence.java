package jp.angeworm.ensyuD.language;

public class WhileSentence implements Sentence {
	public Value condition;
	public Sentence block;
	
	public WhileSentence(Value condition, Sentence block) {
		super();
		this.condition = condition;
		this.block = block;
	}
}
