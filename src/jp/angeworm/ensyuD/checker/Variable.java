package jp.angeworm.ensyuD.checker;

import java.util.List;

public class Variable {
	private String name;
	private VariableType type;
	private List<VariableType> arg;
	
	public Variable(String name, VariableType type, List<VariableType> arg) {
		this.name = name;
		this.type = type;
		this.arg  = arg;
	}
	
	public Variable(String name, VariableType type) {
		this.name = name;
		this.type = type;
		this.arg  = null;
	}
	
	public Variable(String name) {
		this.name = name;
		this.type = VariableType.VOID;
		this.arg  = null;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public VariableType getType() {
		return type;
	}
	public void setType(VariableType type) {
		this.type = type;
	}
	public List<VariableType> getArg() {
		return arg;
	}
	public void setArg(List<VariableType> arg) {
		this.arg = arg;
	}
}
