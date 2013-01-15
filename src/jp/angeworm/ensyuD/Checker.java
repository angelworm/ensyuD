package jp.angeworm.ensyuD;

import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.checker.*;

public class Checker {
	public static boolean parse(List<Token> tokens) {
		return (new CheckerImpl(tokens)).parse();
	}
}

class CheckerImpl {
	private LinkedList<Token> data;
	
	private Environment env = new Environment();
	
	private final TokenType[] STANDARD_TYPES = new TokenType[]{
			TokenType.SINTEGER,
			TokenType.SBOOLEAN,
			TokenType.SCHAR};
	
	public CheckerImpl(List<Token> tokens) {
		data = new LinkedList<Token>(tokens);
	}
	
	private void fail(Token t) {
		throw new RuntimeException("Line\t\t\t\t" +  t.getLineNumber() + " : syntax error: unexpected token " + t.getTokenType().name() 
				+ " " + t.getValue());
	}
	
	private void failType(Token t, VariableType actual, VariableType expected) {
		throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : type error " + "\n"
				+ "\t actual : " + actual + "\n"
				+ "\t expected : " + expected);
	}
	private void failType(int linum, VariableType actual, VariableType expected) {
		throw new RuntimeException("Line\t\t\t\t" + linum + " : type error" + "\n"
				+ "\t actual : " + actual + "\n"
				+ "\t expected : " + expected);
	}
	
	private Token popToken(){
		return data.pop();
	}
	private void pushToken(Token t){
		data.push(t);
	}
	
	private boolean testToken(TokenType t) {
		if(data.isEmpty()) return false;
		Token e = data.get(0);
		
		return e.getTokenType() == t;
	}
	private boolean testToken(TokenType[] ts) {
		boolean ret = false;
		Token e;
		
		if(data.isEmpty()) return false;
		e = data.get(0);
		
		for(TokenType i : ts) {
			ret = ret || (e.getTokenType() == i);
		}

		return ret;
	}
	private void nextToken() {
		if(data.isEmpty()) throw new RuntimeException("Line\t\t\t\t" + "unexpected EOF.");
		data.pop();
	}
	
	private void expectToken(TokenType t){
		if(data.isEmpty()) throw new RuntimeException("Line\t\t\t\t" + "unexpected EOF.");
		Token e = data.pop();
		
		if(e.getTokenType() != t)
			fail(e);
	}
	private void expectToken(TokenType[] ts){
		if(data.isEmpty()) throw new RuntimeException("Line\t\t\t\t" + "unexpected EOF.");
		
		if(testToken(ts)) {
			popToken();
		} else {
			fail(data.get(0));
		}
	}

	private boolean whenToken(TokenType t){
		if(testToken(t)) {
			data.pop();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean whenToken(TokenType[] t){
		if(testToken(t)) {
			data.pop();
			return true;
		} else {
			return false;
		}
	}
	
	private boolean testStandardTypes() {
		return testToken(STANDARD_TYPES);
	}
	
	private int getLineNumber() {
		if(data.isEmpty()) return -1;
		return data.get(0).getLineNumber();
	}
	
	private Variable getIdentifer() {
		Token t = popToken();
		Variable v = new Variable(t.getValue());
		return v;
	}
	
	private VariableType getValType(){
		Token t = popToken();
		switch(t.getTokenType()) {
		case SINTEGER: return VariableType.INTEGER;
		case SBOOLEAN: return VariableType.BOOLEAN;
		case SCHAR:    return VariableType.CHAR;
		default: throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() +" : unexpected type "+ t.getValue()+ ".");
		}
	}
	
	private void program(){
		expectToken(TokenType.SPROGRAM);
		expectToken(TokenType.SIDENTIFIER);
		expectToken(TokenType.SLPAREN);
		names();
		expectToken(TokenType.SRPAREN);
		expectToken(TokenType.SSEMICOLON);
		block();
		compound();
		expectToken(TokenType.SDOT);
	}
	
	private void names() {
		do {
			expectToken(TokenType.SIDENTIFIER);
		} while(whenToken(TokenType.SCOMMA));
	}
	
	private void block(){
		var_definition();
		procedure_definitions();
	}
	
	private void var_definition() {
		if(whenToken(TokenType.SVAR)) {
			variables();
		}
	}
	
	private void variables() {
		do {
			List<Variable> vars = var_names();
			expectToken(TokenType.SCOLON);
			
			VariableType type = type();
			
			int linum = getLineNumber();
			for(Variable i : vars) {
				if(env.hasDefinedInCurrentEnv(i.getName()))
					throw new RuntimeException("Line\t\t\t\t" + linum + " : " + i.getName() + " has already defined");
				i.setType(type);
				env.addVariable(i);
			}

			expectToken(TokenType.SSEMICOLON);
		} while(testToken(TokenType.SIDENTIFIER));
	}
	
	private List<Variable> var_names() {
		List<Variable> ret = new LinkedList<Variable>();
		do {
			ret.add(getIdentifer());
		} while(whenToken(TokenType.SCOMMA));
		return ret;
	}
	
	private VariableType type() {
		if(testStandardTypes()) {
			return getValType();
		} else if (whenToken(TokenType.SARRAY)) {
			int linum = getLineNumber();
			expectToken(TokenType.SLBRACKET);
			int lvalue = number();
			expectToken(TokenType.SRANGE);
			int rvalue = number();
			expectToken(TokenType.SRBRACKET);
			expectToken(TokenType.SOF);
			
			if(lvalue > rvalue) 
				throw new RuntimeException("Line\t\t\t\t" + linum + " : invailed array range");
			
			if(testStandardTypes()) {
				Token t = popToken();
				switch(t.getTokenType()) {
				case SINTEGER: return VariableType.INTEGER_ARRAY;
				case SBOOLEAN: return VariableType.BOOLEAN_ARRAY;
				case SCHAR:    return VariableType.CHAR_ARRAY;
				default: throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : unexpected type "+ t.getValue() + ".");
				}
			} else {
				fail(data.get(0));
			}
		} else {
			Token e = data.get(0);
			fail(e);
		}
		return null;
	}
	
	private int number(){
		int ret = 1;
		if(whenToken(TokenType.SMINUS)) {
			ret = -1;
		} else {
			whenToken(TokenType.SPLUS);
		}
		Variable v = getIdentifer();
		return ret * Integer.parseInt(v.getName());
	}
	
	private void procedure_definitions() {
		while(testToken(TokenType.SPROCEDURE)) {
			procedure();
			expectToken(TokenType.SSEMICOLON);
		}
	}
	
	private void procedure(){
		procedure_head();
		var_definition();
		compound();
		
		env = env.popEnvironment();
	}
	
	private void procedure_head() {
		Environment oldenv = env;
		expectToken(TokenType.SPROCEDURE);
		
		Variable v = getIdentifer();
		v.setType(VariableType.PROCEDURE);
		
		env = env.pushEnvironment();
		
		List<VariableType> args = formal_arguments();
		v.setArg(args);
		oldenv.addVariable(v);
		
		expectToken(TokenType.SSEMICOLON);
	}
	
	private List<VariableType> formal_arguments() {
		List<VariableType> types = new LinkedList<VariableType>();
		if(whenToken(TokenType.SLPAREN)) {
			types.addAll(formal_arguments_sequence());
			expectToken(TokenType.SRPAREN);
		}
		return types;
	}

	private List<VariableType> formal_arguments_sequence() {
		List<VariableType> types = new LinkedList<VariableType>();
		do {
			List<Variable> vals = formal_arguments_name_sequence();
			expectToken(TokenType.SCOLON);
			VariableType type = getValType();
			
			for(Variable i : vals) {
				i.setType(type);
				env.addVariable(i);
				types.add(type);
			}
		} while(whenToken(TokenType.SSEMICOLON));
		return types;
	}
	
	private List<Variable> formal_arguments_name_sequence() {
		List<Variable> ret = new LinkedList<Variable>();
		do {
			ret.add(getIdentifer());
		} while(whenToken(TokenType.SCOMMA));
		return ret;
	}
	
	private void compound(){
		expectToken(TokenType.SBEGIN);
		do {
			//System.out.println(data.get(0).getTokenType().name() + ":" + data.get(0).getValue());
			sentence();
			//System.out.println(data.get(0).getTokenType().name() + ":" + data.get(0).getValue());
		} while(whenToken(TokenType.SSEMICOLON));
		expectToken(TokenType.SEND);
	}
	
	private void sentence() {
//		System.out.println("sentence " + data.get(0).getTokenType().name() + ":" + data.get(0).getValue());
		VariableType type;
		Token t = popToken();
		switch(t.getTokenType()) {
		case SBEGIN:
			pushToken(t);
			compound();
			break;
		case SIF:
			type = expression();
			if(type != VariableType.BOOLEAN)
				throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : invalid predicate type " + type + ". it must be bool.");
			expectToken(TokenType.STHEN);
			compound();
			if(testToken(TokenType.SELSE)) {
				expectToken(TokenType.SELSE);
				compound();
			}
			break;
		case SWHILE:
			type = expression();
			if(type != VariableType.BOOLEAN)
				throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + "invalid predicate type " + type + ". it must be bool.");
			expectToken(TokenType.SDO);
			sentence();
			break;
		case SIDENTIFIER:
			if(whenToken(TokenType.SLPAREN)) {
				List<VariableType> args= expressions();
				expectToken(TokenType.SRPAREN);
				Variable p = env.find(t.getValue());
				if(p == null) {
					throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : undefined procedure " + t.getValue());
				} else if(p.getType() != VariableType.PROCEDURE) {
					throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : " + p.getName() + " is not procedure.");
				} else if(!args.equals(p.getArg())){
					for(int i = 0, size = args.size(); i < size; i++) {
						if(!args.get(i).canConvert(p.getArg().get(i)))
							throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : type mismatch" + "\n" +
									"actual:  " + p.getName() + "(" + args + ")" + "\n" +
									"expected:" + p.getName() + "(" + p.getArg() + ")");
					}
				}
			} else if (testToken(new TokenType[]{TokenType.SASSIGN, TokenType.SLBRACKET})){
				pushToken(t);
				VariableType ltype = variable();
				expectToken(TokenType.SASSIGN);
				VariableType rtype = expression();
				if(ltype.isArray()) {
					throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : " + "type mismatch at assginment(left cant be array)" + "\n" +
							"left: " + ltype + "\n" +
							"right:" + rtype);
				}
				if(!ltype.canConvert(rtype)) {
					throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : " + "type mismatch at assginment" + "\n" +
							"left: " + ltype + "\n" +
							"right:" + rtype);
				}
			} else {
				//non argument function call;
				Variable p = env.find(t.getValue());
				if(p == null) {
					throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : undefined procedure " + t.getValue());
				} else if(p.getType() != VariableType.PROCEDURE) {
					throw new RuntimeException("Line\t\t\t\t" + t.getLineNumber() + " : " + p.getName() + " is not procedure.");
				}
			}
			break;
		case SREADLN:
			if(!whenToken(TokenType.SLPAREN)) break;
			var_names();
			expectToken(TokenType.SRPAREN);
			break;
		case SWRITELN:
			if(!whenToken(TokenType.SLPAREN)) break;
			expressions();
			expectToken(TokenType.SRPAREN);
			break;
		default:
			fail(t);	
		}
	}
	
	private VariableType variable() {
		int linum = getLineNumber();
		String name = getIdentifer().getName();
		VariableType type;
				
		Variable v = env.find(name);
		if(v == null) {
			throw new RuntimeException("Line\t\t\t\t" + linum + " : Undefined Variable " + name);
		} else if(v.getType() == VariableType.PROCEDURE) {
			throw new RuntimeException("Line\t\t\t\t" + linum + " : Referencing Procedure as variable : " + name);
		}
		
		type = v.getType();
		
		if(!testToken(TokenType.SLBRACKET)) {
			return type;
		}
		
		//process array
		
		linum = getLineNumber();
		if(!v.getType().isArray()) {
			throw new RuntimeException("Line\t\t\t\t" + linum + " : " + name + " is not array");
		}
		
		if(whenToken(TokenType.SLBRACKET)) {
			VariableType itype = expression();
			if(itype != VariableType.INTEGER) 
				throw new RuntimeException("Line\t\t\t\t" + linum + " : Array index must be integer : " + name);
			expectToken(TokenType.SRBRACKET);
		}
		return v.getType().arrayOf();
	}
	
	private List<VariableType> expressions() {
		List<VariableType> ret = new LinkedList<VariableType>();
		ret.add(expression());
		while(whenToken(TokenType.SCOMMA)) {
			ret.add(expression());
		}
		return ret;
	}

	private VariableType expression() {
		TokenType[] op = new TokenType[]{
				TokenType.SEQUAL,TokenType.SNOTEQUAL,
				TokenType.SLESS,TokenType.SLESSEQUAL,
				TokenType.SGREAT,TokenType.SGREATEQUAL};
		VariableType opl;
		int linum = getLineNumber();
		
		opl = simple_expression();
		if(whenToken(op)){
			VariableType opr = simple_expression();
			if(!opl.canConvert(opr)) {
				throw new RuntimeException("Line\t\t\t\t" + linum + " : type mismatch in compare operator" + "\n"
						+ "\t actual : " + opl + "\n"
						+ "\t expected : " + opr);
			} else {
				opl = VariableType.BOOLEAN;
			}
		}
		return opl;
	}
	
	private VariableType simple_expression() {
		TokenType[] op = new TokenType[]{
				TokenType.SPLUS, TokenType.SMINUS, TokenType.SOR};
		VariableType ltype, rtype = VariableType.VOID;
		
		if(whenToken(new TokenType[]{TokenType.SPLUS, TokenType.SMINUS}) ){
			int linum = getLineNumber();
			ltype = term();
			if(ltype != VariableType.INTEGER) 
				throw new RuntimeException("Line\t\t\t\t" + linum + " : type error at" + "\n"
					+ "\t actual : " + ltype + "\n"
					+ "\t expected : " + VariableType.INTEGER);
		} else {
			ltype = term();
		}
		while(testToken(op)) {
			int linum = getLineNumber();
			if(whenToken(new TokenType[]{TokenType.SPLUS, TokenType.SMINUS})) {
				rtype = term();
				if(ltype != VariableType.INTEGER || rtype != VariableType.INTEGER)
					throw new RuntimeException("Line\t\t\t\t" + linum + " : type mismatch" + "\n"
							+ "\t left : " + ltype + "\n"
							+ "\t right : " + rtype + "\n"
							+ "\t expected : " + VariableType.INTEGER);
			} else if(whenToken(TokenType.SOR)) {
				rtype = term();
				if(ltype != VariableType.BOOLEAN || rtype != VariableType.BOOLEAN)
					throw new RuntimeException("Line\t\t\t\t" + linum + " : type mismatch" + "\n"
							+ "\t left : " + ltype + "\n"
							+ "\t right : " + rtype + "\n"
							+ "\t expected : " + VariableType.BOOLEAN);
			} else {
				new RuntimeException("Line\t\t\t\t" + "unknown error at" + linum);
			}
			ltype = rtype;
		}
		return ltype;
	}
	
	private VariableType term() {
		TokenType[] op = new TokenType[]{
				TokenType.SSTAR, TokenType.SDIVD, TokenType.SMOD, TokenType.SAND};
		VariableType ltype, rtype = VariableType.VOID;
		
		ltype = factor();
		
		while(testToken(op)) {
			if(whenToken(new TokenType[]{TokenType.SSTAR, TokenType.SDIVD, TokenType.SMOD})) {
				int linum = getLineNumber();
				rtype = factor();
				if(ltype != VariableType.INTEGER || rtype != VariableType.INTEGER)
					throw new RuntimeException("Line\t\t\t\t" + linum + " : type mismatch" + "\n"
							+ "\t left : " + ltype + "\n"
							+ "\t right : " + rtype + "\n"
							+ "\t expected : " + VariableType.INTEGER);
			} else if(whenToken(TokenType.SAND)) {
				int linum = getLineNumber();
				rtype = factor();
				if(ltype != VariableType.BOOLEAN || rtype != VariableType.BOOLEAN)
					throw new RuntimeException("Line\t\t\t\t" + linum + " : type mismatch" + "\n"
							+ "\t left : " + ltype + "\n"
							+ "\t right : " + rtype + "\n"
							+ "\t expected : " + VariableType.BOOLEAN);
			} else {
				int linum = getLineNumber();
				throw new RuntimeException("Line\t\t\t\t" + linum + " : unknown error");
			}
		}
		return ltype;
	}
	
	private VariableType factor() {
		VariableType type;
		Token t = popToken();
		switch(t.getTokenType()) {
		case SCONSTANT:
			return VariableType.INTEGER;
		case SSTRING:
			if(t.getValue().length() == 3) {
				return VariableType.STRING_LENGTH1;
			} else {
				return VariableType.CHAR_ARRAY;
			}
		case SFALSE:
		case STRUE:
			return VariableType.BOOLEAN;
		case SIDENTIFIER:
			pushToken(t);
			return variable();
		case SLPAREN:
			type = expression();
			expectToken(TokenType.SRPAREN);
			return type;
		case SNOT:
			type = factor();
			if(type != VariableType.BOOLEAN) failType(t, type, VariableType.BOOLEAN);
			return type;
		default:
			fail(t);
		}
		return null;
	}

	public boolean parse() {
		program();
		return this.data.isEmpty();
	}
}
