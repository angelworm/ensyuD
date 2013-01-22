package jp.angeworm.ensyuD.compiler;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.language.*;

public class Compile {
	public static String compile(PascalLike pl) {
		 CompileImpl ci = new CompileImpl();
		 StringBuilder code = new StringBuilder();
		 ci.parse(code, pl);
		 return code.toString();
	}
}

class CompileImpl {
	private LabelGenerater lg; 
	private RegisterStack rs;
	private String spc = "        ";
	
	public CompileImpl() {
		lg = new LabelGenerater(100);
		rs = new RegisterStack(6);
	}
	
	private int takeRegister(StringBuilder code) {
		boolean needPush = rs.needStackPush();
		int index = rs.take() + 1;
		
		if(needPush) {
			code.append(spc + " PUSH 0, GR" + index + "\n");
		}
		
		return index;
	}
	private int freeRegister(StringBuilder code) {
		boolean needPop = rs.needStackPush();
		int index = rs.free() + 1;
		
		if(needPop) {
			code.append(spc + " POP GR" + index + "\n");
		}
		
		return index;
	}
	
	public Environment makeEnvironment(PascalLike pl) {
		List<Variable> vars = pl.vars;
		Environment e = new Environment(lg);
		
		for(Variable v : vars) {
			e.addVariable(v, "VAR");
		}
		
		return e;
	}
	
	public void parse(StringBuilder code, PascalLike pl){
		Environment env = makeEnvironment(pl);
		
		String prgname = lg.makeLabel("ANGEL");
		code.append(prgname + " START MAIN" + "\n");
		
		parse(code, pl.sentence, env);
		
		code.append(spc     + " END" + "\n");
	}
	
	public void parse(StringBuilder code, Sentence s, Environment e) {
		System.out.println(s.toString());
		if (s instanceof AssignSentence) {
			// TODO
			parseValue(code, ((AssignSentence) s).rvalue, e);
		} else if(s instanceof ApplySentence ) {
			// TODO
		} else if(s instanceof BlockSentence) {
			for(Sentence line : ((BlockSentence) s).sentences) {
				parse(code, line , e);
			}
		} else if(s instanceof IfSentence) {
			// TODO
		} else if(s instanceof WhileSentence) {
			// TODO
		}
	}
	public int parseValue(StringBuilder code, Value s, Environment e) {
		if (s instanceof Expression) {
			Expression exp = (Expression)s;
			return parseExpression(code, exp, e);
		} else if(s instanceof VariableAssign) {
			// TODO
		} else if(s instanceof Value) {
			// TODO
			if(s.type.equals(new Type("integer"))) {
				int reg = takeRegister(code);
				code.append(spc + " LAD GR" + reg + ", " + s.value + "\n");
				return reg;
			}
		}
		throw new RuntimeException(s.toString() + " is ?");
	}

	public int parseExpression(StringBuilder code, Expression exp, Environment e) {
		if(exp.operands.size() > 1) {
			Iterator<Value> iter = exp.operands.iterator();
			
			int leftReg = parseValue(code, iter.next(), e);
			while(iter.hasNext()) {
				int rightReg = parseValue(code, iter.next(), e);
				
				handleExpression(code, exp, leftReg, rightReg);
				
				int freedReg = freeRegister(code);
				assert rightReg == freedReg : "regiter free miss";
			}
			return leftReg;
		} else {			
			if(exp.value.equals("+")) {
				int reg = parseValue(code, exp.operands.get(0), e);
				code.append(spc + " ; nothing to do(single append operation)" + "\n");
				return reg;
			} else if(exp.value.equals("-")) {
				int leftReg  = takeRegister(code);
				int rightReg = parseValue(code, exp.operands.get(0), e);
				code.append(spc + " SUB GR" + leftReg + ", GR" + rightReg + "\n");
				freeRegister(code);
				return leftReg;
			}  if(exp.value.equals("not")) {
				int reg = parseValue(code, exp.operands.get(0), e);
				code.append(spc + " NOT GR" + reg + "\n");
				return reg;
			} 
		}
		return -1;
	}
	
	public void handleExpression(StringBuilder code, Expression exp, int leftReg, int rightReg) {
		if(exp.value.equals("+")) {
			code.append(spc + " ADD GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("-")){
			code.append(spc + " SUB GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("and")){
			code.append(spc + " AND GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("or")){
			code.append(spc + " OR GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("=")){
			String jpfalse = lg.makeLabel("EQ");
			String jpend = lg.makeLabel("EQ");
			code.append(spc     + " CPL GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JNZ " + jpfalse + "\n");
			code.append(spc     + " LAD GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("<>")){
			String jpfalse = lg.makeLabel("NE");
			String jpend = lg.makeLabel("NE");
			code.append(spc     + " CPL GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JZE " + jpfalse + "\n");
			code.append(spc     + " LAD GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("<")){
			String jpfalse = lg.makeLabel("LT");
			String jpend = lg.makeLabel("LT");
			code.append(spc     + " CPA GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JMI " + jpfalse + "\n");
			code.append(spc     + " LAD GR" + leftReg + ", 0" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD GR" + leftReg + ", -1" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals(">=")){
			String jpfalse = lg.makeLabel("GE");
			String jpend = lg.makeLabel("GE");
			code.append(spc     + " CPA GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JMI " + jpfalse + "\n");
			code.append(spc     + " LAD GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("<")){
			String jpfalse = lg.makeLabel("GT");
			String jpend = lg.makeLabel("GT");
			code.append(spc     + " CPA GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JPL " + jpfalse + "\n");
			code.append(spc     + " LAD GR" + leftReg + ", 0" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD GR" + leftReg + ", -1" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals(">=")){
			String jpfalse = lg.makeLabel("LE");
			String jpend = lg.makeLabel("LE");
			code.append(spc     + " CPA GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JPL " + jpfalse + "\n");
			code.append(spc     + " LAD GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("*")){
			code.append(spc + " PUSH 0, GR1" + "\n");
			code.append(spc + " PUSH 0, GR2" + "\n");
			code.append(spc + " LAD  GR1, 0, GR" + leftReg  +"\n");
			code.append(spc + " LAD  GR2, 0, GR" + rightReg +"\n");
			code.append(spc + " CALL MUL" + "\n");
			code.append(spc + " LAD  GR" + leftReg+ ", 0, GR2" +"\n");
			code.append(spc + " POP  GR2" + "\n");
			code.append(spc + " POP  GR1" + "\n");
		} else if(exp.value.equals("/") || exp.value.equals("div")){
			// TODO
			code.append(spc + " PUSH 0, GR1" + "\n");
			code.append(spc + " PUSH 0, GR2" + "\n");
			code.append(spc + " LAD  GR1, 0, GR" + leftReg  +"\n");
			code.append(spc + " LAD  GR2, 0, GR" + rightReg +"\n");
			code.append(spc + " CALL MUL" + "\n");
			code.append(spc + " LAD  GR" + leftReg+ ", 0, GR2" +"\n");
			code.append(spc + " POP  GR2" + "\n");
			code.append(spc + " POP  GR1" + "\n");
		} else if(exp.value.equals("mod")){
			// TODO
			code.append(spc + " PUSH 0, GR1" + "\n");
			code.append(spc + " PUSH 0, GR2" + "\n");
			code.append(spc + " LAD  GR1, 0, GR" + leftReg  +"\n");
			code.append(spc + " LAD  GR2, 0, GR" + rightReg +"\n");
			code.append(spc + " CALL MUL" + "\n");
			code.append(spc + " LAD  GR" + leftReg+ ", 0, GR2" +"\n");
			code.append(spc + " POP  GR2" + "\n");
			code.append(spc + " POP  GR1" + "\n");
		} else {
			throw new RuntimeException(exp.value + " is ?");
		}
	}
	
}