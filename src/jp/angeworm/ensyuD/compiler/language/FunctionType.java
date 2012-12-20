package jp.angeworm.ensyuD.compiler.language;

import java.util.LinkedList;
import java.util.List;

public class FunctionType extends Type {
	public List<String> args;
	
	public FunctionType(List<String> args_) {
		super("VOID");
		args = new LinkedList<String>(args_);
	}
	public FunctionType(String ret) {
		super(ret);
		args = new LinkedList<String>();
	}
	public FunctionType(String ret, List<String> args_) {
		super(ret);
		args = new LinkedList<String>(args_);
	}

}
