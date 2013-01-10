package jp.angeworm.ensyuD.compiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.angeworm.ensyuD.Token;
import jp.angeworm.ensyuD.TokenType;
import jp.angeworm.ensyuD.language.*;

public class Parser {
	public static ParseTree parse(List<Token> tokens) {
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
		Expression cond;
		
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
				expressions();
				expectToken(TokenType.SRPAREN);
			} else if (testToken(new TokenType[]{TokenType.SASSIGN, TokenType.SLBRACKET})){
				pushToken(t);
				variable();
				expectToken(TokenType.SASSIGN);
				expression();
			} else {
				//non argument function call;
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
	
	private void variable() {
		expectToken(TokenType.SIDENTIFIER);
		if(whenToken(TokenType.SLBRACKET)) {
			expression();
			expectToken(TokenType.SRBRACKET);
		}
	}
	
	private void expressions() {
		expression();
		while(whenToken(TokenType.SCOMMA)) {
			expression();
		}
	}

	private void expression() {
		TokenType[] op = new TokenType[]{
				TokenType.SEQUAL,TokenType.SNOTEQUAL,
				TokenType.SLESS,TokenType.SLESSEQUAL,
				TokenType.SGREAT,TokenType.SGREATEQUAL};
		
		simple_expression();
		if(whenToken(op)){
			simple_expression();
		}
	}
	
	private void simple_expression() {
		TokenType[] op = new TokenType[]{
				TokenType.SPLUS, TokenType.SMINUS, TokenType.SOR};
		
		whenToken(new TokenType[]{TokenType.SPLUS, TokenType.SMINUS});
		term();
		while(whenToken(op)) {
			term();
		}
	}
	
	private ParseTree term() {
		TokenType[] op = new TokenType[]{
				TokenType.SSTAR, TokenType.SDIVD, TokenType.SMOD, TokenType.SAND};
		
		factor();
		while(whenToken(op)) {
			factor();
		}
	}
	
	private ParseTree factor() {
//		System.out.println("factor " + data.get(0).getTokenType().name() + ":" + data.get(0).getValue());

		Token t = popToken();
		switch(t.getTokenType()) {
		case SCONSTANT:
		case SSTRING:
		case SFALSE:
		case STRUE:
			return new ParseTree(t);
		case SIDENTIFIER:
			pushToken(t);
			return variable();
		case SLPAREN:
			ParseTree pt = new ParseTree(t, expression());
			expectToken(TokenType.SRPAREN);
			return pt;
		case SNOT:
			return new ParseTree(t, factor());
			break;
		default:
			fail(t);
		}
	}

	public ParseTree parse() {
		return program();
	}
}
