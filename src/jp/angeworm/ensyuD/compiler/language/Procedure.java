package jp.angeworm.ensyuD.compiler.language;

import java.util.ArrayList;
import java.util.List;

public class Procedure{
	public String name;
	public Sentence sentence;
	public List<Variable> args;
	public List<Variable> vars;
	
	public Procedure(String name, Sentence sentence,
			List<Variable> args, List<Variable> vars) {
		this.name = name;
		this.sentence = sentence;
		this.args = new ArrayList<Variable>(args);
		this.vars = new ArrayList<Variable>(vars);
	}
	public Procedure(String name) {
		this.name = name;
	}
}
