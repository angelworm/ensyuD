package jp.angeworm.ensyuD.language;

import java.util.Iterator;
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(this.value);
		sb.append(" ");
		Iterator<Value> iter = operands.iterator();
		
		if(iter.hasNext())
			sb.append(iter.next());
		while (iter.hasNext()) {
			sb.append(" ");
			sb.append(iter.next());
		}
		sb.append(")");
		return sb.toString();
	}
}
