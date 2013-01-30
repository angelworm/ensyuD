package jp.angeworm.ensyuD.language;

import java.util.LinkedList;
import java.util.List;

public class FunctionType extends Type {
	public List<Type> args;
	
	public FunctionType(List<Type> args2) {
		super("VOID");
		args = new LinkedList<Type>(args2);
	}
	public FunctionType(String ret) {
		super(ret);
		args = new LinkedList<Type>();
	}
	public FunctionType(String ret, List<Type> args_) {
		super(ret);
		args = new LinkedList<Type>(args_);
	}

}
