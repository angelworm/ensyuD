package jp.angeworm.ensyuD.language;

import java.util.LinkedList;
import java.util.List;

public class Expression extends Value{
	public List<Value> operands;
	
	public Expression(String value, String type) {
		super(value, type);
		this.operands = new LinkedList<Value>();
	}
	public Expression(String value, Type type) {
		super(value, type);
		this.operands = new LinkedList<Value>();
	}
	public Expression(String value, Expression exp, Type type) {
		super(value, type);
		this.operands = new LinkedList<Value>();
		this.operands.add(exp);
	}
	public Expression(String value, List<Value> exp, Type type) {
		super(value, type);
		this.operands = exp;
	}
}
