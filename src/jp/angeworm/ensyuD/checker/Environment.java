package jp.angeworm.ensyuD.checker;

import java.util.LinkedList;
import java.util.List;

public class Environment {
	private List<Variable> vals;
	private Environment nextEnv;
	
	public Environment() {
		vals = new LinkedList<Variable>();
		nextEnv = null;
	}
	
	public void addVariable(Variable v) {
		vals.add(v);
	}

	public Environment pushEnvironment(){
		Environment ret = new Environment();
		ret.nextEnv = this;
		return ret;
	}
	public Environment popEnvironment(){
		return this.nextEnv;
	}
	
	public Variable find(String name) {
		for(Variable v : vals) {
			if(v.getName().equals(name)) return v;
		}
		
		if(nextEnv == null) return null;
		else return nextEnv.find(name);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		for(Variable i : vals) {
			sb.append(i.getName());
			sb.append(" : ");
			
			if(i.getType().isPrimitive()) {
				sb.append(i.getType());
			} else if(i.getType().isArray()) {
				sb.append(i.getType());
			} else if(i.getType() == VariableType.PROCEDURE) {
				sb.append("PROCEDURE(");
				for(VariableType j : i.getArg()) {
					sb.append(j);
					sb.append(", ");
				}
				sb.append(")");
			}
			sb.append('\n');
		}
		sb.append("}\n");
		if(nextEnv != null) sb.append(nextEnv.toString());
		return sb.toString();
	}
}
