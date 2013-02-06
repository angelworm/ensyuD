package jp.angeworm.ensyuD.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.compiler.Environment.EnvironmentEntry;
import jp.angeworm.ensyuD.compiler.Location.LocationType;
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

	private void traceRegister(StringBuilder code) {
		code.append(spc + " ; Stack: ");
		for (int i : rs.stack) {
			code.append((i + 1) + " ");
		}
		code.append('\n');
		code.append(spc + " ; Queue: ");
		for (int i : rs.lastappend) {
			code.append((i + 1) + " ");
		}
		code.append('\n');
		code.append(spc + " ; Register: ");
		for (int i = 0; i < rs.register.size(); i++) {
			code.append(" GR" + (i + 1) + ":" + rs.register.get(i));
		}
		code.append('\n');
	}

	private int takeRegister(StringBuilder code) {
		boolean needPush = rs.needStackPush();
		int index = rs.take() + 1;

		traceRegister(code);

		if (needPush) {
			code.append(spc + " PUSH 0, GR" + index + "\n");
		}

		return index;
	}

	private boolean willPushRegister(int index) {
		return rs.needStackPush(index - 1);
	}

	private int takeRegister(StringBuilder code, int index) {
		boolean needPush = willPushRegister(index);
		int i = rs.take(index - 1) + 1;

		traceRegister(code);

		if (needPush) {
			code.append(spc + " PUSH 0, GR" + i + "\n");
		}

		return i;
	}

	private int freeRegister(StringBuilder code) {
		boolean needPop = rs.needStackPop();
		int index = rs.free() + 1;

		traceRegister(code);

		if (needPop) {
			code.append(spc + " POP  GR" + index + "\n");
		}

		return index;
	}

	private int freeRegister(StringBuilder code, int i) {
		boolean needPop = rs.needStackPop(i - 1);
		int index = rs.free(i - 1) + 1;

		traceRegister(code);

		if (needPop) {
			code.append(spc + " POP  GR" + index + "\n");
		}

		return index;
	}
	
	private int useRegister(StringBuilder code, int i) {
		boolean needPop = rs.needStackPop(i - 1);
		int index = rs.use(i - 1) + 1;

		traceRegister(code);

		if (needPop) {
			code.append(spc + " POP  GR" + index + "\n");
		}

		return index;
	}

	private int getSize(Variable arg) {
		if (arg.type instanceof ArrayType) {
			ArrayType at = (ArrayType) arg.type;
			return at.max - at.min + 1;
		} else {
			return 1;
		}
	}

	private int getSize(List<Variable> args) {
		int st = 0;
		for (Variable v : args) {
			st += getSize(v);
		}
		return st;
	}

	public Environment makeEnvironment(PascalLike pl) {
		List<Variable> vars = pl.vars;
		Environment e = new Environment(lg);

		Variable wb = new Variable();
		wb.type = new ArrayType("integer", 0, 255);
		wb.name = "WRBUFFER";
		e.addVariable(wb, "WRBUFFER");

		Variable wbs = new Variable();
		wbs.type = new Type("integer");
		wbs.name = "WRBUFFER";
		e.addVariable(wbs, "WRBUFLEN");

		for (Variable v : vars) {
			e.addVariable(v, "VAR");
		}

		for (Procedure p : pl.procs) {
			Variable vp = new Variable();

			List<Type> args = new LinkedList<Type>();
			for (Variable v : p.args)
				args.add(v.type);
			vp.type = new FunctionType(args);
			vp.name = p.name;
			e.addVariable(vp, "PROC");
		}

		return e;
	}

	public Environment makeEnvironment(Procedure pl, Environment env) {
		Environment e = env.pushEnvironment();

		int argsSize = getSize(pl.args);
		int varsSize = getSize(pl.vars);
		int stackSize = argsSize + varsSize + 1;
		int stackSum = 0;

		for (Variable arg : pl.args) {
			stackSum += getSize(arg);
			int arrayLabel = 0;
			if (arg.type instanceof ArrayType)
				arrayLabel = ((ArrayType) (arg.type)).min;
			e.addVariable(arg, Location.LocationType.StackLocation, stackSize
					- stackSum - arrayLabel);
		}

		stackSum = 0;
		for (Variable var : pl.vars) {
			stackSum += getSize(var);
			int arrayLabel = 0;
			if (var.type instanceof ArrayType)
				arrayLabel = ((ArrayType) (var.type)).min;
			e.addVariable(var, Location.LocationType.StackLocation, varsSize
					- stackSum - arrayLabel);
		}

		return e;
	}

	private int getValue(List<Integer> l, int index) {
		if (l == null)
			return 0;
		if (index >= l.size() || 0 > index)
			return 0;
		return l.get(index);
	}

	public void writeEnvironment(StringBuilder code, Environment env) {
		for (Environment.EnvironmentEntry ent : env) {
			if (ent.loc.type != Location.LocationType.HeapLocation)
				continue;

			if (ent.val.type instanceof ArrayType) {
				ArrayType t = (ArrayType) ent.val.type;
				int min = Math.min(t.min, 0);
				int i = t.min;

				for (; min <= t.max; min++) {
					String lb = (min == 0 ? ent.loc.getLabel() : spc);
					String cmt = (min == t.min ? "; " + ent.val.name : "");

					code.append(lb + " DC "
							+ getValue(ent.defaultValue, min - i) + cmt + "\n");
				}
			} else if (ent.val.type instanceof FunctionType) {
				continue;
			} else {
				code.append(ent.loc.getLabel() + " DC "
						+ getValue(ent.defaultValue, 0) + "; " + ent.val.name
						+ "\n");
			}
		}
	}

	public void parse(StringBuilder code, PascalLike pl) {
		Environment env = makeEnvironment(pl);

		String prgname = lg.makeLabel("ANGEL");
		code.append(prgname + " START" + "\n");

		parse(code, pl.sentence, env);

		code.append(spc + " RET" + "\n");

		StringBuilder outer = new StringBuilder();
		for (Procedure p : pl.procs) {
			parse(code, p, env, outer);
		}

		writeEnvironment(code, env);

		code.append(outer.toString());

		code.append(spc + " END" + "\n");
	}

	public void parse(StringBuilder code, Procedure pl, Environment e,
			StringBuilder outercode) {
		Environment env = makeEnvironment(pl, e);
		RegisterStack tmp = rs;
		rs = new RegisterStack(7, true);

		String prgname = e.find(pl.name).loc.getLabel();
		code.append(prgname + " LAD  GR8, " + -getSize(pl.vars) + ", GR8 ; "
				+ pl.name + "\n");

		code.append(spc + " ; Environment:" + "\n");
		for (EnvironmentEntry v : env) {
			code.append(spc + " ; " + v.val.name + ":" + v.val.type + ":"
					+ " GR8," + v.loc.getId() + "\n");
		}

		parse(code, pl.sentence, env);

		code.append(spc + " LAD  GR8, " + getSize(pl.vars) + ", GR8" + "\n");
		code.append(spc + " RET" + "\n");

		writeEnvironment(outercode, env);

		rs = tmp;
	}

	public void parse(StringBuilder code, Sentence s, Environment e) {
		if (s instanceof AssignSentence) {
			AssignSentence s2 = (AssignSentence) s;
			Environment.EnvironmentEntry ent = e.find(s2.lvalue.value);

			VariableAssign lvalue = s2.lvalue;
			int rvalue = parseValue(code, s2.rvalue, e);

			if (ent.loc.type.equals(LocationType.HeapLocation)) {
				if (lvalue.index != null) {
					int indexReg = parseValue(code, lvalue.index, e);
					code.append(spc + " ST   GR" + rvalue + ", "
							+ ent.loc.getLabel() + ", GR" + indexReg + " ; "
							+ lvalue.value + "[] := " + "\n");
					freeRegister(code);
				} else {
					code.append(spc + " ST   GR" + rvalue + ", "
							+ ent.loc.getLabel() + " ; " + lvalue.value
							+ " := " + "\n");
				}
			} else if (ent.loc.type.equals(LocationType.StackLocation)) {
				if (lvalue.index != null) {
					int indexReg = parseValue(code, lvalue.index, e);
					code.append(spc + " LAD  GR" + indexReg + ", "
							+ (ent.loc.getId() + rs.getStackHeight()) + ", GR"
							+ indexReg + "\n");
					code.append(spc + " ADDL GR" + indexReg + ", GR8" + "\n");
					code.append(spc + " ST   GR" + rvalue + ", 0, " + ", GR"
							+ indexReg + " ; " + lvalue.value + "[] := " + "\n");
					freeRegister(code);
				} else {
					int indexReg = takeRegister(code);
					code.append(spc + " LAD  GR" + indexReg + ", "
							+ (ent.loc.getId() + rs.getStackHeight()) + "\n");
					code.append(spc + " ADDL GR" + indexReg + ", GR8" + "\n");
					code.append(spc + " ST   GR" + rvalue + ", 0, GR"
							+ indexReg + " ; " + lvalue.value + " := " + "\n");
					freeRegister(code);
				}
			} else {
				throw new RuntimeException("unknown Location Type"
						+ ent.loc.type);
			}
			freeRegister(code);
		} else if (s instanceof ApplySentence) {
			ApplySentence s3 = ((ApplySentence) s);
			if (s3.callee.equals("writeln")) {
				parseWrite(code, s3, e);
			} else if (s3.callee.equals("readln")) {
				parseRead(code, s3, e);
//				throw new RuntimeException("readln is unimplemented!");
			} else {
				int size = 0;
				for (Value t : s3.value) {
					int reg = parseValue(code, t, e);
					if (rs.needStackPop()) {
						code.append(spc + " LD   GR0, 0, GR8" + "\n");
						code.append(spc + " ST   GR" + reg + ", 0, GR8" + "\n");
						code.append(spc + " LD   GR" + reg + ", GR0" + "\n");
						// rs.free();
						traceRegister(code);
						reg = 0;
					} else {
						// freeRegister(code);
						code.append(spc + " PUSH 0, GR" + reg + "\n");
					}
					size++;
				}
				code.append(spc + " CALL " + e.find(s3.callee).loc.getLabel()
						+ "\n");
				for (Value t : s3.value) {
					if (rs.needStackPop()) {
						rs.free();
					}
				}
				code.append(spc + " LAD GR8, " + size + ", GR8" + "\n");
			}
		} else if (s instanceof BlockSentence) {
			for (Sentence line : ((BlockSentence) s).sentences) {
				parse(code, line, e);
			}
		} else if (s instanceof IfSentence) {
			IfSentence s4 = (IfSentence) s;
			int preg = parseValue(code, s4.condition, e);
			String label1 = lg.makeLabel("IF");
			String label2 = lg.makeLabel("IF");
			code.append(spc + " AND GR" + preg + ", GR" + preg + "\n");
			freeRegister(code);
			code.append(spc + " JZE " + label1 + "\n");
			parse(code, s4.consequence, e);

			if (s4.alternative != null) {
				code.append(spc + " JUMP " + label2 + "\n");
				code.append(label1 + " NOP" + "\n");
				parse(code, s4.alternative, e);
				code.append(label2 + " NOP" + "\n");
			} else {
				code.append(label1 + " NOP" + "\n");
			}
		} else if (s instanceof WhileSentence) {
			WhileSentence s5 = (WhileSentence) s;
			String labelhead = lg.makeLabel("WHL");
			String labelend = lg.makeLabel("WHL");
			code.append(labelhead + " NOP" + "\n");
			int preg = parseValue(code, s5.condition, e);
			code.append(spc + " AND GR" + preg + ", GR" + preg + "\n");
			freeRegister(code);
			code.append(spc + " JZE " + labelend + "\n");

			parse(code, s5.block, e);

			code.append(spc + " JUMP " + labelhead + "\n");
			code.append(labelend + " NOP" + "\n");
		}
	}

	public int parseValue(StringBuilder code, Value s, Environment e) {
		if (s instanceof Expression) {
			Expression exp = (Expression) s;
			return parseExpression(code, exp, e);
		} else if (s instanceof VariableAssign) {
			Environment.EnvironmentEntry ent = e.find(s.value);

			if (ent.loc.type.equals(Location.LocationType.HeapLocation)) {
				if (ent.val.type instanceof ArrayType) {
					VariableAssign vas = (VariableAssign) s;
					if (vas.index != null) {
						int valReg = takeRegister(code);
						int indexReg = parseValue(code,
								((VariableAssign) s).index, e);

						code.append(spc + " LD   GR" + valReg + ", "
								+ ent.loc.getLabel() + ", GR" + indexReg
								+ " ; " + s.value + "\n");

						freeRegister(code);
						return valReg;
					} else {
						int valReg = takeRegister(code);

						code.append(spc + " LAD  GR" + valReg + ", "
								+ ent.loc.getLabel() + " ; " + s.value + "\n");
						code.append(spc + " LAD  GR" + valReg + ", "
								+ ((ArrayType) ent.val.type).min + ", GR"
								+ valReg + " ; " + s.value + "\n");

						freeRegister(code);
						return valReg;
					}
				} else {
					int valReg = takeRegister(code);

					code.append(spc + " LD   GR" + valReg + ", "
							+ ent.loc.getLabel() + " ; " + s.value + "\n");

					return valReg;
				}
			} else if (ent.loc.type.equals(Location.LocationType.StackLocation)) {
				if (ent.val.type instanceof ArrayType) {
					int valReg = takeRegister(code);
					int indexReg = parseValue(code, ((VariableAssign) s).index,
							e);

					code.append(spc + " LAD  GR" + valReg + ", "
							+ (ent.loc.getId() + rs.getStackHeight()) + ", GR8"
							+ " ; " + s.value + "\n");
					code.append(spc + " SUBA GR" + valReg + ", GR" + indexReg
							+ "\n");
					code.append(spc + " LD   GR" + valReg + ", 0, GR" + valReg
							+ "\n");

					freeRegister(code);
					return valReg;
				} else {
					int valReg = takeRegister(code);

					code.append(spc + " LAD  GR" + valReg + ", "
							+ (ent.loc.getId() + rs.getStackHeight()) + ", GR8"
							+ " ; " + s.value + "\n");
					code.append(spc + " LD   GR" + valReg + ", 0, GR" + valReg
							+ " ; " + s.value + "\n");

					return valReg;
				}
			} else {
				throw new RuntimeException("?");
			}
		} else if (s instanceof ConstantValue) {
			if (s.type.type.equals("integer")) {
				int reg = takeRegister(code);
				code.append(spc + " LAD  GR" + reg + ", " + s.value + "\n");
				return reg;
			}
			if (s.type.type.equals("boolean")) {
				int reg = takeRegister(code);
				int value = (s.value.equals("true") ? 1 : 0);
				code.append(spc + " LAD  GR" + reg + ", " + value + "\n");
				return reg;
			}
			if (s.type.type.equals("char")) {
				int reg = takeRegister(code);
				if (s.value.length() == 1) {
					int value = s.value.codePointAt(0);
					code.append(spc + " LAD  GR" + reg + ", " + value + "\n");
				} else {
					Variable v = new Variable();
					List<Integer> def = new ArrayList<Integer>(s.value.length());
					for (byte b : s.value.getBytes()) {
						def.add((int) b);
					}
					v.type = new ArrayType("integer", 0, s.value.length() - 1);
					v.name = this.lg.makeLabel("TMP");
					e.addVariableWithDefault(v, def);

					String label = e.find(v.name).loc.getLabel();
					code.append(spc + " LAD  GR" + reg + ", " + label + "\n");
				}
				return reg;
			}
		} else {
			throw new RuntimeException(s + " is ?");
		}
		throw new RuntimeException(s + " is ?!");
	}

	public int parseExpression(StringBuilder code, Expression exp, Environment e) {
		if (exp.operands.size() > 1) {
			Iterator<Value> iter = exp.operands.iterator();

			int leftReg = parseValue(code, iter.next(), e);
			while (iter.hasNext()) {
				int rightReg = parseValue(code, iter.next(), e);

				handleExpression(code, exp, leftReg, rightReg);

				int freedReg = freeRegister(code);
				assert rightReg == freedReg : "regiter free miss";
			}
			return leftReg;
		} else {
			if (exp.value.equals("+")) {
				int reg = parseValue(code, exp.operands.get(0), e);
				code.append(spc + " ; nothing to do(single append operation)"
						+ "\n");
				return reg;
			} else if (exp.value.equals("-")) {
				int leftReg = takeRegister(code);
				int rightReg = parseValue(code, exp.operands.get(0), e);
				code.append(spc + " LAD  GR" + leftReg + ", 0" + "\n");
				code.append(spc + " SUBA GR" + leftReg + ", GR" + rightReg
						+ "\n");
				freeRegister(code);
				return leftReg;
			}
			if (exp.value.equals("not")) {
				int reg = parseValue(code, exp.operands.get(0), e);
				code.append(spc + " XOR  GR" + reg + ", =#ffff" + "\n");
				return reg;
			}
		}
		return -1;
	}

	public void handleExpression(StringBuilder code, Expression exp,
			int leftReg, int rightReg) {
		if (exp.value.equals("+")) {
			code.append(spc + " ADDA GR" + leftReg + ", GR" + rightReg + "\n");
		} else if (exp.value.equals("-")) {
			code.append(spc + " SUBA GR" + leftReg + ", GR" + rightReg + "\n");
		} else if (exp.value.equals("and")) {
			code.append(spc + " AND  GR" + leftReg + ", GR" + rightReg + "\n");
		} else if (exp.value.equals("or")) {
			code.append(spc + " OR   GR" + leftReg + ", GR" + rightReg + "\n");
		} else if (exp.value.equals("=")) {
			String jpfalse = lg.makeLabel("EQ");
			String jpend = lg.makeLabel("EQ");
			code.append(spc + " CPL  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc + " JNZ  " + jpfalse + "\n");
			code.append(spc + " LAD  GR" + leftReg + ", 1" + "\n");
			code.append(spc + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend + " NOP" + "\n");
		} else if (exp.value.equals("<>")) {
			String jpfalse = lg.makeLabel("NE");
			String jpend = lg.makeLabel("NE");
			code.append(spc + " CPL  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc + " JZE  " + jpfalse + "\n");
			code.append(spc + " LAD  GR" + leftReg + ", 1" + "\n");
			code.append(spc + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend + " NOP" + "\n");
		} else if (exp.value.equals("<")) {
			String jpfalse = lg.makeLabel("LT");
			String jpend = lg.makeLabel("LT");
			code.append(spc + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc + " JMI  " + jpfalse + "\n");
			code.append(spc + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(spc + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 1" + "\n");
			code.append(jpend + " NOP" + "\n");
		} else if (exp.value.equals(">=")) {
			String jpfalse = lg.makeLabel("GE");
			String jpend = lg.makeLabel("GE");
			code.append(spc + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc + " JMI  " + jpfalse + "\n");
			code.append(spc + " LAD  GR" + leftReg + ", 1" + "\n");
			code.append(spc + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend + " NOP" + "\n");
		} else if (exp.value.equals(">")) {
			String jpfalse = lg.makeLabel("GT");
			String jpend = lg.makeLabel("GT");
			code.append(spc + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc + " JPL  " + jpfalse + "\n");
			code.append(spc + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(spc + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 1" + "\n");
			code.append(jpend + " NOP  " + "\n");
		} else if (exp.value.equals("<=")) {
			String jpfalse = lg.makeLabel("LE");
			String jpend = lg.makeLabel("LE");
			code.append(spc + " CPA  GR" + leftReg + ", GR" + rightReg + "\n");
			code.append(spc + " JPL  " + jpfalse + "\n");
			code.append(spc + " LAD  GR" + leftReg + ", 1" + "\n");
			code.append(spc + " JUMP " + jpend + "\n");
			code.append(jpfalse + " LAD  GR" + leftReg + ", 0" + "\n");
			code.append(jpend + " NOP" + "\n");
		} else if (exp.value.equals("*")) {
			code.append(spc + " PUSH 0, GR" + leftReg + "\n");
			code.append(spc + " PUSH 0, GR" + rightReg + "\n");
			code.append(spc + " CALL MULT" + "\n");
			code.append(spc + " POP  GR0" + "\n");
			code.append(spc + " POP  GR" + leftReg + "\n");

		} else if (exp.value.equals("/") || exp.value.equals("div")) {
			code.append(spc + " PUSH 0, GR" + leftReg + "\n");
			code.append(spc + " PUSH 0, GR" + rightReg + "\n");
			code.append(spc + " CALL DIV" + "\n");
			code.append(spc + " POP  GR" + leftReg + "\n");
			code.append(spc + " POP  GR0" + "\n");
		} else if (exp.value.equals("mod")) {
			code.append(spc + " PUSH 0, GR" + leftReg + "\n");
			code.append(spc + " PUSH 0, GR" + rightReg + "\n");
			code.append(spc + " CALL DIV" + "\n");
			code.append(spc + " POP  GR0" + "\n");
			code.append(spc + " POP  GR" + leftReg + "\n");
		} else {
			throw new RuntimeException(exp.value + " is ?");
		}
	}

	public void parseWrite(StringBuilder code, ApplySentence s, Environment e) {
		List<Value> args = s.value;

		boolean gr6taken = willPushRegister(6);
		takeRegister(code, 6);
		boolean gr7taken = willPushRegister(7);
		takeRegister(code, 7);
		code.append(spc + " LAD  GR6, 0" + "\n");
		code.append(spc + " LAD  GR7, WRBUFFER" + "\n");

		for (Value arg : args) {
			Type t = arg.type;
			if (arg instanceof VariableAssign) {
				t = e.find(arg.value).val.type;
			}

			if (t instanceof ArrayType && t.type.equals("char")) {
				ArrayType ta = (ArrayType) t;
				boolean sizeRegT = willPushRegister(1);
				int sizeReg = takeRegister(code, 1);
				code.append(spc + " LAD  GR" + sizeReg + ", "
						+ (ta.max - ta.min + 1) + "\n");
				boolean writeRegT = willPushRegister(2);
				int writeReg = takeRegister(code, 2);

				int valueReg = parseValue(code, arg, e);
				code.append(spc + " LD   GR" + writeReg + ", GR" + valueReg
						+ "\n");
				freeRegister(code);

				code.append(spc + " CALL WRTSTR" + "\n");

				// if(writeRegT)freeRegister(code);
				// if(sizeRegT) freeRegister(code);
				freeRegister(code, 2);
				freeRegister(code, 1);
			} else if (t.type.equals("char")) {
				boolean writeRegT = willPushRegister(2);
				int writeReg = takeRegister(code, 2);

				int valueReg = parseValue(code, arg, e);
				code.append(spc + " LD   GR" + writeReg + ", GR" + valueReg
						+ "\n");
				freeRegister(code);

				code.append(spc + " CALL WRTCH" + "\n");
				freeRegister(code, 2);
			} else if (t.type.equals("integer")) {
				boolean writeRegT = willPushRegister(2);
				int writeReg = takeRegister(code, 2);

				int valueReg = parseValue(code, arg, e);
				code.append(spc + " LD   GR" + writeReg + ", GR" + valueReg
						+ "\n");
				freeRegister(code);

				code.append(spc + " CALL WRTINT" + "\n");
				freeRegister(code, 2);
			} else {
				throw new RuntimeException("unhandled format");
			}
		}

		// code.append(spc + " CALL WRTLN" + "\n");
		code.append(spc + " ST   GR6, WRBUFLEN" + "\n");
		code.append(spc + " OUT  WRBUFFER, WRBUFLEN" + "\n");
		freeRegister(code, 7);
		freeRegister(code, 6);
	}

	public void parseRead(StringBuilder code, ApplySentence s, Environment e) {
		List<Value> args = s.value;

		if (args.size() < 1) {
			code.append(spc + " CALL RDLN" + "\n");
			return;
		} else {

			int rdReg = takeRegister(code, 2);

			for (Value arg : args) {
				Type t = arg.type;
				if (arg instanceof VariableAssign) {
					t = e.find(arg.value).val.type;
				} else {
					throw new RuntimeException(
							"reader has only handls variables!");
				}
				EnvironmentEntry ee = e.find(arg.value);

				if (t instanceof ArrayType && t.type.equals("char")) {
					ArrayType ta = (ArrayType) t;

					if(ee.loc.type == LocationType.StackLocation) {
						int loc = ee.loc.getId() + rs.getStackHeight();
						code.append(spc + " LAD  GR" + rdReg + ", " + loc + ", GR8"+ "\n");
					} else if (ee.loc.type == LocationType.HeapLocation) {
						code.append(spc + " LAD  GR" + rdReg + ", " + ee.loc.getLabel() + "\n");
						code.append(spc + " LAD  GR" + rdReg + ", " + ta.min + ", GR" + rdReg + "\n");
					}
					
					int indexReg = takeRegister(code, 1);
					code.append(spc + " LAD  GR" + indexReg + ", " + (ta.max - ta.min) + "\n");
				} else {
					if(ee.loc.type == LocationType.StackLocation) {
						int loc = ee.loc.getId() + rs.getStackHeight();
						code.append(spc + " LAD  GR" + rdReg + ", " + loc + ", GR8"+ "\n");
					} else if (ee.loc.type == LocationType.HeapLocation) {
						code.append(spc + " LAD  GR" + rdReg + ", " + ee.loc.getLabel() + "\n");
					}
				}
				
				if (t instanceof ArrayType && t.type.equals("char")) {
					code.append(spc + " CALL RDSTR" + "\n");
					freeRegister(code, 1);
				} else if (t.type.equals("char")) {
					code.append(spc + " CALL RDCH" + "\n");
				} else if (t.type.equals("integer")) {
					code.append(spc + " CALL RDINT" + "\n");
				} else {
					throw new RuntimeException("unhandled format");
				}
			}
			freeRegister(code, 2);
			return;
		}
	}
}