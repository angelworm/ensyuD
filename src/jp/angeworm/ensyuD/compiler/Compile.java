package jp.angeworm.ensyuD.compiler;

import java.util.ArrayList;
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
		rs = new RegisterStack(7);
	}
	
	private int takeRegister(StringBuilder code) {
		boolean needPush = rs.needStackPush();
		int index = rs.take() + 1;
		
		if(needPush) {
			code.append(spc + " PUSH 0, GR" + index + "\n");
		}
		
		return index;
	}
	private int takeRegister(StringBuilder code, int index) {
		boolean needPush = rs.needStackPush(index - 1);
		int i = rs.take(index - 1) + 1;
		
		if(needPush) {
			code.append(spc + " PUSH 0, GR" + i + "\n");
		}
		
		return index;
	}
	private int freeRegister(StringBuilder code) {
		boolean needPop = rs.needStackPush();
		int index = rs.free() + 1;
		
		if(needPop) {
			code.append(spc + " POP  GR" + index + "\n");
		}
		
		return index;
	}
	
	public Environment makeEnvironment(PascalLike pl) {
		List<Variable> vars = pl.vars;
		Environment e = new Environment(lg);
		
		Variable wb = new Variable();
		wb.type = new ArrayType("integer", 0, 255);
		wb.name = "WRBUFFER";
		e.addVariable(wb, "WRBUFFER");
		
		for(Variable v : vars) {
			e.addVariable(v, "VAR");
		}
		
		return e;
	}
	
	private int getValue(List<Integer> l, int index) {
		if(l == null) return 0;
		if(index >= l.size() || 0 > index) return 0;
		return l.get(index);
	}
	
	public void writeEnvironment(StringBuilder code, Environment env) {
		for(Environment.EnvironmentEntry ent : env) {
			if(ent.loc.type != Location.LocationType.HeapLocation) continue;
			
			if(ent.val.type instanceof ArrayType) {
				ArrayType t = (ArrayType) ent.val.type;
				int min = t.min;
				
				for(; min < 0; min++)
					code.append(spc + " DC 0" + "\n");
				code.append(ent.loc.getLabel() + " DC " + getValue(ent.defaultValue, min) + "\n");	
				min++;
				for(; min <= t.max; min++)
					code.append(spc + " DC " + getValue(ent.defaultValue, min) + "\n");	
			} else if (ent.val.type instanceof FunctionType) {
				continue;
			} else {
				code.append(ent.loc.getLabel() + " DC "+ getValue(ent.defaultValue, 0) + "\n");
			}
		}
	}
	
	public void parse(StringBuilder code, PascalLike pl){
		Environment env = makeEnvironment(pl);
		
		String prgname = lg.makeLabel("ANGEL");
		code.append(prgname + " START" + "\n");
		
		parse(code, pl.sentence, env);
		
		code.append(spc     + " RET" + "\n");
		
		writeEnvironment(code, env);
		
		code.append(spc     + " END" + "\n");
	}
	
	public void parse(StringBuilder code, Sentence s, Environment e) {
		System.out.println(s.toString());
		if (s instanceof AssignSentence) {
			AssignSentence s2 = (AssignSentence) s;
			Environment.EnvironmentEntry ent = e.find(s2.lvalue.value);
			
			VariableAssign lvalue = s2.lvalue; 
			int rvalue = parseValue(code, s2.rvalue, e);
			
			if(lvalue.index != null) {
				int indexReg = parseValue(code, lvalue.index, e);
				code.append(spc + " ST   GR" + rvalue + ", " + ent.loc.getLabel() + ", GR" + indexReg + "\n");
				freeRegister(code);
			} else {
				code.append(spc + " ST   GR" + rvalue + ", " + ent.loc.getLabel() + "\n");
			}
			freeRegister(code);
		} else if(s instanceof ApplySentence ) {
			// TODO
			ApplySentence s3 = ((ApplySentence) s);
			System.out.println(s3.callee);
			if(s3.callee.equals("writeln")) {
				parseWrite(code, s3, e);
			}
		} else if(s instanceof BlockSentence) {
			for(Sentence line : ((BlockSentence) s).sentences) {
				parse(code, line , e);
			}
		} else if(s instanceof IfSentence) {
			IfSentence s4 = (IfSentence) s;
			int preg = parseValue(code, s4.condition, e);
			String label1 = lg.makeLabel("IF");
			String label2 = lg.makeLabel("IF");
			code.append(spc + " AND GR" + preg + ", GR" + preg + "\n");
			freeRegister(code);
			code.append(spc + " JZE " + label1 + "\n");
			parse(code, s4.consequence, e);
			
			if(s4.alternative != null) {
				code.append(spc    + " JUMP " + label2 + "\n");
				code.append(label1 + " NOP" + "\n");
				parse(code, s4.alternative, e);
				code.append(label2 + " NOP" + "\n");
			} else {
				code.append(label1 + " NOP" + "\n");
			}
		} else if(s instanceof WhileSentence) {
			WhileSentence s5 = (WhileSentence) s;
			String labelhead = lg.makeLabel("WHL");
			String labelend  = lg.makeLabel("WHL");
			code.append(labelhead + " NOP" + "\n");
			int preg = parseValue(code, s5.condition, e);
			code.append(labelhead + " AND GR" + preg + ", GR" + preg + "\n");
			code.append(spc + " JZE " + labelend + "\n");
			
			parse(code, s5.block, e);
			
			code.append(spc       + " JUMP " + labelhead + "\n");
			code.append(labelend  + " NOP" + "\n");
		}
	}
	public int parseValue(StringBuilder code, Value s, Environment e) {
		System.out.println(s.toString());
		if (s instanceof Expression) {
			Expression exp = (Expression)s;
			return parseExpression(code, exp, e);
		} else if(s instanceof VariableAssign) {
			Environment.EnvironmentEntry ent = e.find(s.value);
			
			if(ent.val.type instanceof ArrayType) {
				int valReg   = takeRegister(code);
				int indexReg = takeRegister(code);
				
				code.append(spc + " LAD  GR" + indexReg + ", " + ((VariableAssign) s).index + "\n");
				code.append(spc + " LD   GR" + valReg + ", " + ent.loc.getLabel() + ", GR" + indexReg + "\n");

				freeRegister(code);
				return valReg;
			} else {
				int valReg   = takeRegister(code);
				
				code.append(spc + " LD   GR" + valReg + ", " + ent.loc.getLabel() + "\n");
				
				return valReg;
			}
		} else if(s instanceof ConstantValue) {
			if(s.type.type.equals("integer")) {
				int reg = takeRegister(code);
				code.append(spc + " LAD  GR" + reg + ", " + s.value + "\n");
				return reg;
			}
			if(s.type.type.equals("boolean")) {
				int reg = takeRegister(code);
				int value = (s.value.equals("true") ? -1 : 0);
				code.append(spc + " LAD  GR" + reg + ", " + value + "\n");
				return reg;
			}
			if(s.type.type.equals("char")){
				int reg = takeRegister(code);
				if(s.value.length() == 1) {
					int value = s.value.codePointAt(0);
					code.append(spc + " LAD  GR" + reg + ", " + value + "\n");
				} else {
					Variable v = new Variable();
					List<Integer> def = new ArrayList<Integer>(s.value.length());
					for(byte b : s.value.getBytes()) {
						def.add((int) b);
					}
					v.type = new ArrayType("integer", 0, s.value.length());
					v.name = this.lg.makeLabel("TMP");
					e.addVariableWithDefault(v, def);
					
					String label = e.find(v.name).loc.getLabel();
					code.append(spc + " LAD  GR" + reg + ", " + label + "\n");
				}
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
				code.append(spc + " LAD  GR" + leftReg + ", 0" + "\n");
				code.append(spc + " SUBA GR" + leftReg + ", GR" + rightReg + "\n");
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
			code.append(spc + " ADDA GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("-")){
			code.append(spc + " SUBA GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("and")){
			code.append(spc + " AND  GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("or")){
			code.append(spc + " OR   GR" + leftReg + ", GR" + rightReg + "\n");
		} else if(exp.value.equals("=")){
			String jpfalse = lg.makeLabel("EQ");
			String jpend = lg.makeLabel("EQ");
			code.append(spc     + " CPL  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JNZ  " + jpfalse + "\n");
			code.append(spc     + " LAD  GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("<>")){
			String jpfalse = lg.makeLabel("NE");
			String jpend = lg.makeLabel("NE");
			code.append(spc     + " CPL  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JZE  " + jpfalse + "\n");
			code.append(spc     + " LAD  GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("<")){
			String jpfalse = lg.makeLabel("LT");
			String jpend = lg.makeLabel("LT");
			code.append(spc     + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JMI  " + jpfalse + "\n");
			code.append(spc     + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", -1" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals(">=")){
			String jpfalse = lg.makeLabel("GE");
			String jpend = lg.makeLabel("GE");
			code.append(spc     + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JMI  " + jpfalse + "\n");
			code.append(spc     + " LAD  GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("<")){
			String jpfalse = lg.makeLabel("GT");
			String jpend = lg.makeLabel("GT");
			code.append(spc     + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JPL  " + jpfalse + "\n");
			code.append(spc     + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", -1" + "\n");
			code.append(jpend   + " NOP  " + "\n");
		} else if(exp.value.equals(">=")){
			String jpfalse = lg.makeLabel("LE");
			String jpend = lg.makeLabel("LE");
			code.append(spc     + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc     + " JPL  " + jpfalse + "\n");
			code.append(spc     + " LAD  GR" + leftReg + ", -1" + "\n");
			code.append(spc     + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend   + " NOP" + "\n");
		} else if(exp.value.equals("*")){
			code.append(spc + " PUSH 0, GR" + leftReg  +"\n");
			code.append(spc + " PUSH 0, GR" + rightReg +"\n");
			code.append(spc + " CALL MULT" + "\n");
			code.append(spc + " POP  GR0" + "\n");
			code.append(spc + " POP  GR" + leftReg + "\n");

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
	public void parseWrite(StringBuilder code, ApplySentence s, Environment e) {
		System.out.println(s.toString());
		
		List<Value> args = s.value;
		
		takeRegister(code, 6);
		takeRegister(code, 7);
		code.append(spc + " LAD  GR6, 0" + "\n");
		code.append(spc + " LAD  GR7, WRBUFFER" + "\n");
		
		for(Value arg : args) {
			Type t = arg.type;
			if(arg instanceof VariableAssign) {
				t = e.find(arg.value).val.type;
			}
			
			if(t instanceof ArrayType && t.type != "char") {
				throw new RuntimeException("Compile Error: write: Array except String appried.");
			} else if (t instanceof ArrayType){
				ArrayType ta = (ArrayType) t;
				int sizeReg  = takeRegister(code, 1);
				code.append(spc + " LAD  GR" + sizeReg + ", " + (ta.max - ta.min + 1) + "\n");
				int writeReg = takeRegister(code, 2);
				int valueReg = parseValue(code, arg, e);
				code.append(spc + " LD  GR" + writeReg + ", GR" + valueReg + "\n");
				code.append(spc + " CALL WRTSTR" + "\n");
				
				freeRegister(code);
				freeRegister(code);
				freeRegister(code);
				
			} else if(t.type.equals("char"))  {
				int valueReg = parseValue(code, arg, e);
				int writeReg  = takeRegister(code, 2);
				code.append(spc + " LD  GR" + writeReg + ", GR" + valueReg + "\n");
				code.append(spc + " CALL WRTCHAR" + "\n");
				freeRegister(code);
				freeRegister(code);
			} else if(t.type.equals("integer")) {
				int valueReg = parseValue(code, arg, e);
				int writeReg  = takeRegister(code, 2);
				code.append(spc + " LD  GR" + writeReg + ", GR" + valueReg + "\n");
				code.append(spc + " CALL WRTINT" + "\n");
				freeRegister(code);
				freeRegister(code);
			} else {
				throw new RuntimeException("unhandled format");
			}
		}
		
		code.append(spc + " CALL WRTLN" + "\n");
		freeRegister(code);
		freeRegister(code);
	}
}