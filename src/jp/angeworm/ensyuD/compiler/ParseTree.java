package jp.angeworm.ensyuD.compiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.*;

public class ParseTree {
	private Token token;
	private List<ParseTree> leaves;

	public ParseTree(Token t) {
		this.token = t;
		this.leaves = new LinkedList<ParseTree>();
	}
	public ParseTree(Token t, ParseTree pt) {
		this.token = t;
		this.leaves = new ArrayList<ParseTree>();
		this.leaves.add(pt);
	}
	public ParseTree(Token t, List<ParseTree> pt) {
		this.token = t;
		this.leaves = new ArrayList<ParseTree>(pt);
	}
	public Token getToken() {
		return token;
	}
	public void setToken(Token token) {
		this.token = token;
	}
	public List<ParseTree> getLeaves() {
		return leaves;
	}
}
