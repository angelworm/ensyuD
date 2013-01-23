package jp.angeworm.ensyuD.compiler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.language.Variable;

public class Environment implements Iterable<Environment.EnvironmentEntry> {
	static public class EnvironmentEntry {
		public Variable val;
		public Location loc;
	}
	
	private List<EnvironmentEntry> ent;
	private Environment nextEnv;
	private LabelGenerater lg;
	
	public Environment() {
		ent = new LinkedList<EnvironmentEntry>();
		nextEnv = null;
		lg = new LabelGenerater();
	}
	
	public Environment(LabelGenerater lg2) {
		ent = new LinkedList<EnvironmentEntry>();
		nextEnv = null;
		lg = lg2;
	}

	public void addVariable(Variable v) {
		EnvironmentEntry e = new EnvironmentEntry();
		e.val = v;
		e.loc = new Location(lg.makeLabel("V"));
		ent.add(e);
	}
	public void addVariable(Variable v, String prefix) {
		EnvironmentEntry e = new EnvironmentEntry();
		e.val = v;
		e.loc = new Location(lg.makeLabel(prefix));
		ent.add(e);
	}
	
	public void addVariable(Variable v, Location.LocationType type, int id) {
		EnvironmentEntry e = new EnvironmentEntry();
		e.val = v;
		e.loc = new Location(type ,id);
		ent.add(e);
	}

	public boolean hasDefinedInCurrentEnv(String name) {
		for(EnvironmentEntry v : ent) {
			if(v.val.name.equals(name)) return true;
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
	
	public EnvironmentEntry find(String name) {
		for(EnvironmentEntry v : ent) {
			if(v.val.name.equals(name)) return v;
		}
		
		if(nextEnv == null) return null;
		else return nextEnv.find(name);
	}

	@Override
	public Iterator<EnvironmentEntry> iterator() {
		return this.ent.iterator();
	}
	
	/*
	public String toString() {
		return ""l
	}*/
}
