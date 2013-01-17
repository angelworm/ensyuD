package jp.angeworm.ensyuD.compiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.Token;
import jp.angeworm.ensyuD.TokenType;
import jp.angeworm.ensyuD.language.*;

public class Parser {
	public static PascalLike parse(List<Token> tokens) {
		return (new ParserImpl(tokens)).parse();
	}
}

class ParserImpl {
	private LinkedList<Token> data;
	
	private final TokenType[] STANDARD_TYPES = new TokenType[]{
			TokenType.SINTEGER,
			TokenType.SBOOLEAN,
			TokenType.SCHAR};
	
	public ParserImpl(List<Token> tokens) {
		data = new LinkedList<Token>(tokens);
	}
	
	private void fail(Token t) {
		throw new RuntimeException("unexpected " + t.getTokenType().name() 
				+ " " + t.getValue()
				+ " near at line " + t.getLineNumber());
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
		if(data.isEmpty()) throw new RuntimeException("unexpected EOF.");
		data.pop();
	}
	
	private void expectToken(TokenType t){
		if(data.isEmpty()) throw new RuntimeException("unexpected EOF.");
		Token e = data.pop();
		
		if(e.getTokenType() != t)
			fail(e);
	}
	private void expectToken(TokenType[] ts){
		if(data.isEmpty()) throw new RuntimeException("unexpected EOF.");
		
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
	
	private Token getTokenWhen(TokenType tt) {
		Token t = data.pop();
		if(t.getTokenType() != tt) fail(t);
		return t;
	}
	private Token getTokenWhen(TokenType[] tts) {
		Token t = data.pop();
		for(TokenType tt : tts){
			if(t.getTokenType() != tt) fail(t);
		}
		return t;
	}
	
	private PascalLike program(){
		PascalLike ret = new PascalLike();
		
		expectToken(TokenType.SPROGRAM);
		expectToken(TokenType.SIDENTIFIER);
		expectToken(TokenType.SLPAREN);
		names();
		expectToken(TokenType.SRPAREN);
		expectToken(TokenType.SSEMICOLON);
		ret.vars = var_definition();
		ret.procs = procedure_definitions();
		ret.sentence = compound();
		expectToken(TokenType.SDOT);
		
		return ret;
	}
	
	private void names() {
		do {
			expectToken(TokenType.SIDENTIFIER);
		} while(whenToken(TokenType.SCOMMA));
	}
	
	private List<Variable> var_definition() {
		if(testToken(TokenType.SVAR)) {
			List<Variable> ret = new ArrayList<Variable>();
			do {
				List<String> names = var_names();
				expectToken(TokenType.SCOLON);
				Type type = type();
				expectToken(TokenType.SSEMICOLON);
				
				for(String name : names) {
					ret.add(new Variable(name, type));
				}
			} while(testToken(TokenType.SIDENTIFIER));
			return ret;
		} else {
			return new LinkedList<Variable>();
		}
	}
	
	private List<String> var_names() {
		List<String> ret = new ArrayList<String>();
		do {
			ret.add(getTokenWhen(TokenType.SIDENTIFIER).getValue());
		} while(whenToken(TokenType.SCOMMA));
		return ret;
	}
	
	private Type type() {
		if(testStandardTypes()) {
			Token t = popToken();
			return new Type(t.getValue());
		} else if (whenToken(TokenType.SARRAY)) {
			expectToken(TokenType.SLBRACKET);
			int min = number();
			expectToken(TokenType.SRANGE);
			int max = number();
			expectToken(TokenType.SRBRACKET);
			expectToken(TokenType.SOF);
			
			if(!testStandardTypes()) {
				fail(data.get(0));
			}
			Token t = popToken();
			return new ArrayType(t.getValue(), min, max);
		} else {
			Token e = data.get(0);
			fail(e);
		}
		return null;
	}
	
	private int number(){
		int ret = 1;
		
		if(whenToken(TokenType.SMINUS)) ret *= -1;
		else if(whenToken(TokenType.SPLUS)) ret *= 1;
		Token t = getTokenWhen(TokenType.SCONSTANT); 
		
		return ret * Integer.valueOf(t.getValue());
	}
	
	private List<Procedure> procedure_definitions() {
		List<Procedure> ret = new LinkedList<Procedure>();
		while(testToken(TokenType.SPROCEDURE)) {
			ret.add(procedure());
			expectToken(TokenType.SSEMICOLON);
		}
		return ret;
	}
	
	private Procedure procedure(){
		Procedure proc = procedure_head();
		proc.vars = var_definition();
		proc.sentence = compound();
		return proc;
	}
	
	private Procedure procedure_head() {
		Procedure proc;
		Token t = getTokenWhen(TokenType.SIDENTIFIER);
		expectToken(TokenType.SPROCEDURE);
		List<Variable> args = formal_arguments();
		expectToken(TokenType.SSEMICOLON);
		
		proc = new Procedure(t.getValue());
		proc.args = args;
		return proc;
	}
	
	private List<Variable> formal_arguments() {
		List<Variable> ret = new LinkedList<Variable>();
		if(whenToken(TokenType.SLPAREN)) {
			ret = formal_arguments_sequence();
			expectToken(TokenType.SRPAREN);
		}
		return ret;
	}

	private List<Variable> formal_arguments_sequence() {
		List<Variable> ret = new LinkedList<Variable>();
		do {
			List<String> names = formal_arguments_name_sequence();
			expectToken(TokenType.SCOLON);
			Token t = getTokenWhen(STANDARD_TYPES);
			
			for(String name : names) {
				ret.add(new Variable(name, t.getValue()));
			}
		} while(whenToken(TokenType.SSEMICOLON));
		return ret;
	}
	
	private List<String> formal_arguments_name_sequence() {
		List<String> ret = new LinkedList<String>();
		do {
			ret.add(getTokenWhen(TokenType.SIDENTIFIER).getValue());
		} while(whenToken(TokenType.SCOMMA));
		return ret;
	}
	
	private Sentence compound(){
		List<Sentence> ret = new LinkedList<Sentence>();
		
		expectToken(TokenType.SBEGIN);
		do {
			ret.add(sentence());
		} while(whenToken(TokenType.SSEMICOLON));
		expectToken(TokenType.SEND);
		
		return new BlockSentence(ret);
	}
	
	private Sentence sentence() {
		Value cond;
		
		Token t = popToken();
		switch(t.getTokenType()) {
		case SBEGIN:
			pushToken(t);
			return compound();
		case SIF:
			cond = expression();
			expectToken(TokenType.STHEN);
			Sentence cons = compound()
					, alter = null;
			if(testToken(TokenType.SELSE)) {
				expectToken(TokenType.SELSE);
				alter = compound();
			}
			return new IfSentence(cond, cons, alter);
		case SWHILE:
			cond = expression();
			expectToken(TokenType.SDO);
			Sentence whileblock = sentence();
			return new WhileSentence(cond, whileblock);
		case SIDENTIFIER:
			if(whenToken(TokenType.SLPAREN)) {
				List<Value> args = expressions();
				expectToken(TokenType.SRPAREN);
				
				return new ApplySentence(t.getValue(), args);
			} else if (testToken(new TokenType[]{TokenType.SASSIGN, TokenType.SLBRACKET})){
				pushToken(t);
				VariableAssign va = variable();
				expectToken(TokenType.SASSIGN);
				Value rexpr = expression();
				return new AssignSentence(va, rexpr);
			} else {
				return new ApplySentence(t.getValue());
			}
		case SREADLN:
			if(!whenToken(TokenType.SLPAREN)) break;
			List<String> va = var_names();
			List<Value>  arg_r = new LinkedList<Value>();
			for(String i : va) {
				arg_r.add(new VariableAssign(i, "variable"));
			}
			
			expectToken(TokenType.SRPAREN);
			return new ApplySentence(t.getValue(), arg_r);
		case SWRITELN:
			if(!whenToken(TokenType.SLPAREN)) break;
			List<Value> arg_w = expressions();
			expectToken(TokenType.SRPAREN);
			return new ApplySentence(t.getValue(), arg_w);
		default:
			fail(t);
			return null;
		}
		return null;
	}
	
	private VariableAssign variable() {
		Token t = getTokenWhen(TokenType.SIDENTIFIER);
		Value index = null;
		if(whenToken(TokenType.SLBRACKET)) {
			index = expression();
			expectToken(TokenType.SRBRACKET);
		}
		return new VariableAssign(t.getValue(), "variable", index);
	}
	
	private List<Value> expressions() {
		List<Value> ret = new LinkedList<Value>();
		ret.add(expression());
		while(whenToken(TokenType.SCOMMA)) {
			ret.add(expression());
		}
		return ret;
	}

	
	private Value expression() {
		TokenType[] op = new TokenType[]{
				TokenType.SEQUAL,TokenType.SNOTEQUAL,
				TokenType.SLESS,TokenType.SLESSEQUAL,
				TokenType.SGREAT,TokenType.SGREATEQUAL};
		
		Value ret = simple_expression();
		if(testToken(op)){
			Token t = getTokenWhen(op);
			Value rexp = simple_expression();
			
			List<Value> operands = new LinkedList<Value>();
			operands.add(ret);
			operands.add(rexp);
			ret = new Expression(t.getValue(), operands, new Type("boolean"));
		}
		return ret;
	}
	
	private Value simple_expression_r() {
		TokenType[] op = new TokenType[]{
				TokenType.SPLUS, TokenType.SMINUS, TokenType.SOR};

		Value ret = term();
		if(testToken(op)) {
			Token t = getTokenWhen(op);
			Value rexp = simple_expression_r();
			
			List<Value> operands = new LinkedList<Value>();
			operands.add(ret);
			operands.add(rexp);
			if(t.getTokenType() == TokenType.SOR){
				ret = new Expression(t.getValue(), operands, new Type("boolean"));
			} else {
				ret = new Expression(t.getValue(), operands, new Type("integer"));
			}
		}
		return ret;
	}
	
	private Value simple_expression() {
		
		Value ret = null;
		if(testToken(new TokenType[]{TokenType.SPLUS, TokenType.SMINUS})) {
			Token t = getTokenWhen(new TokenType[]{TokenType.SPLUS, TokenType.SMINUS});
			
			ret = simple_expression_r();
			
			if(ret instanceof Expression) {
				Value v = ((Expression) ret).operands.get(0);
				List<Value> operands = new LinkedList<Value>();
				operands.add(v);
				((Expression) ret).operands.set(0, new Expression(t.getValue(), operands, new Type("integer")));
			} else{
				List<Value> operands = new LinkedList<Value>();
				operands.add(ret);
				ret = new Expression(t.getValue(), operands, new Type("integer"));
			}
		} else {
			ret = simple_expression_r();
		}
		
		return ret;
	}
	
	private Value term() {
		TokenType[] op = new TokenType[]{
				TokenType.SSTAR, TokenType.SDIVD, TokenType.SMOD, TokenType.SAND};
		
		Value ret = factor();
		if(testToken(op)) {
			Token t = getTokenWhen(op);
			Value rexp = term();
			
			List<Value> operands = new LinkedList<Value>();
			operands.add(ret);
			operands.add(rexp);
			if(t.getTokenType() == TokenType.SAND){
				ret = new Expression(t.getValue(), operands, new Type("boolean"));
			} else {
				ret = new Expression(t.getValue(), operands, new Type("integer"));
			}
		}
		return ret;

	}
	
	private Value factor() {
//		System.out.println("factor " + data.get(0).getTokenType().name() + ":" + data.get(0).getValue());

		Token t = popToken();
		switch(t.getTokenType()) {
		case SCONSTANT:
			return new Value(t.getValue(), new Type("integer"));
		case SSTRING:
			return new Value(t.getValue(), new Type("string"));
		case SFALSE:
		case STRUE:
			return new Value(t.getValue(), new Type("boolean"));
		case SIDENTIFIER:
			pushToken(t);
			return variable();
		case SLPAREN:
			Value pt = expression();
			expectToken(TokenType.SRPAREN);
			return pt;
		case SNOT:
			List<Value> operands = new LinkedList<Value>();
			operands.add(factor());
			return new Expression(t.getValue(), operands, new Type("boolean"));
		default:
			fail(t);
		}
		return null;
	}

	public PascalLike parse() {
		return program();
	}
}
