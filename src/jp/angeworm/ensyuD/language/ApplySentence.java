package jp.angeworm.ensyuD.language;

import java.util.ArrayList;
import java.util.List;

public class ApplySentence implements Sentence {
	public String callee;
	public List<Value> value;
	
	public ApplySentence(String callee_, List<Value> args) {
		callee = callee_;
		value = args;
	}
	public ApplySentence(String callee_) {
		callee = callee_;
		value = new ArrayList<Value>();
	}
}
