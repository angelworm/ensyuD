package jp.angeworm.ensyuD.compiler;

import java.util.*;

import jp.angeworm.ensyuD.language.*;

public class Optimize {
	static PascalLike run(PascalLike pl) {
		return pl;
	}
}

class OptimizeImpl {
	private PascalLike pl;
	private Environment env;

	public OptimizeImpl(PascalLike pl) {
		this.pl = pl;
		this.env = new Environment();
	}
	
	public PascalLike run() {
		optimize(pl);
		return this.pl;
	}
	
	void optimize(PascalLike pl) {
		for(Procedure p : pl.procs) {
			optimize(p);
		}
		
		optimize(pl.sentence);
	}
	
	void optimize(Procedure p) {
		optimize(p.sentence);
	}
	
	void optimize(Sentence s) {
		if(s instanceof ApplySentence) {
			for(Value v : ((ApplySentence)s).value)
				optimize(v);
		} else if(s instanceof AssignSentence) {
			optimize(((AssignSentence)s).rvalue);
		} else if(s instanceof IfSentence) {
			optimize(((IfSentence)s).condition);
			optimize(((IfSentence)s).consequence);
			if(((IfSentence)s).alternative != null)
				optimize(((IfSentence)s).alternative);
		} else if(s instanceof WhileSentence) {
			optimize(((WhileSentence)s).condition);
			optimize(((WhileSentence)s).block);
		} else if(s instanceof BlockSentence) {
			for(Sentence sub : ((BlockSentence) s).sentences){
				optimize(sub);
			}
		}
	}
	
	void optimize(Value s) {
		optimizeValue(s);
	}
	Value optimizeValue(Value s) {
		if(s instanceof ConstantValue) {
			
		} else if(s instanceof VariableAssign) {
			
		} else if(s instanceof Expression) {
			Expression exp = (Expression) s;
			
			if(exp.value.equals("+")) {
				List<Value> opr = new LinkedList<Value>();
				boolean firstConst = false;
				
				for(int i = 0; i < exp.operands.size(); i++) {
					exp.operands.set(i, optimizeValue(exp.operands.get(i)));
				}
				
				for(Value v : exp.operands) {
					if(v instanceof ConstantValue) {
						if(firstConst) {
							int value = Integer.valueOf(v.value)
										+ Integer.valueOf(opr.get(0).value);
							opr.set(0, new ConstantValue(Integer.toString(value), v.type));
							firstConst = true;
						} else {
							opr.add(0, v);
						}
					} else {
						
					}
				}
			}
		}
		return s;
	}
}