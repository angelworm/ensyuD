package jp.angeworm.ensyuD.compiler;

import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.language.Variable;

public class Environment {
	private List<Variable> vals;
	private List<String>   labels;
	private Environment nextEnv;
	private LabelGenerater lg;
	
	public Environment() {
		vals = new LinkedList<Variable>();
		labels = new LinkedList<String>();
		nextEnv = null;
		lg = new LabelGenerater();
	}
	
	public Environment(LabelGenerater lg2) {
		vals = new LinkedList<Variable>();
		labels = new LinkedList<String>();
		nextEnv = null;
		lg = lg2;
	}

	public void addVariable(Variable v) {
		vals.add(v);
		labels.add(lg.makeLabel("V"));
	}
	public void addVariable(Variable v, String prefix) {
		vals.add(v);
		labels.add(lg.makeLabel(prefix));
	}

	public boolean hasDefinedInCurrentEnv(String name) {
		for(Variable v : vals) {
			if(v.name.equals(name)) return true;
		}
		return false;
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
			if(v.name.equals(name)) return v;
		}
		
		if(nextEnv == null) return null;
		else return nextEnv.find(name);
	}
	/*
	public String toString() {
		return ""l
	}*/
}
