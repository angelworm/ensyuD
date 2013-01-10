package jp.angeworm.ensyuD.language;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BlockSentence implements Sentence {
	public List<Sentence> sentences; 
	
	public BlockSentence(Sentence s) {
		sentences = new LinkedList<Sentence>();
		sentences.add(s);
	}
	
	public BlockSentence(List<Sentence> ss) {
		sentences = new LinkedList<Sentence>(ss);
	}

}
