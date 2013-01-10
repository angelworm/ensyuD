package jp.angeworm.ensyuD.language;

import java.util.LinkedList;
import java.util.List;

public class Expression extends Value{
	public List<Expression> operands;
	
	public Expression(String value, String type) {
		super(value, type);
		this.operands = new LinkedList<Expression>();
	}
	public Expression(String value, Type type) {
		super(value, type);
		this.operands = new LinkedList<Expression>();
	}
	public Expression(String value, Expression exp, Type type) {
		super(value, type);
		this.operands = new LinkedList<Expression>();
		this.operands.add(exp);
	}
	public Expression(String value, List<Expression> exp, Type type) {
		super(value, type);
		this.operands = exp;
	}
}
