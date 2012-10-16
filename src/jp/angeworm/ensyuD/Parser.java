package jp.angeworm.ensyuD;

import java.util.LinkedList;
import java.util.List;

public class Parser {
	public static boolean parse(List<Token> tokens) {
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
			var_names();
			expectToken(TokenType.SCOLON);
			type();
			expectToken(TokenType.SSEMICOLON);
		} while(testToken(TokenType.SIDENTIFIER));
	}
	
	private void var_names() {
		do {
			expectToken(TokenType.SIDENTIFIER);
		} while(whenToken(TokenType.SCOMMA));
	}
	
	private void type() {
		if(testStandardTypes()) {
			nextToken();
			return;
		} else if (whenToken(TokenType.SARRAY)) {
			expectToken(TokenType.SLBRACKET);
			number();
			expectToken(TokenType.SRANGE);
			expectToken(TokenType.SCONSTANT);
			expectToken(TokenType.SRBRACKET);
			expectToken(TokenType.SOF);
			if(testStandardTypes()) {
				nextToken();
			} else {
				fail(data.get(0));
			}
		} else {
			Token e = data.get(0);
			fail(e);
		}
	}
	
	private void number(){
		whenToken(new TokenType[]{TokenType.SPLUS, TokenType.SMINUS});
		expectToken(TokenType.SCONSTANT);
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
	}
	
	private void procedure_head() {
		expectToken(TokenType.SPROCEDURE);
		expectToken(TokenType.SIDENTIFIER);
		formal_arguments();
		expectToken(TokenType.SSEMICOLON);
	}
	
	private void formal_arguments() {
		if(whenToken(TokenType.SLPAREN)) {
			formal_arguments_sequence();
			expectToken(TokenType.SRPAREN);
		}
	}

	private void formal_arguments_sequence() {
		do {
			formal_arguments_name_sequence();
			expectToken(TokenType.SCOLON);
			expectToken(STANDARD_TYPES);
		} while(whenToken(TokenType.SSEMICOLON));
	}
	
	private void formal_arguments_name_sequence() {
		do {
			expectToken(TokenType.SIDENTIFIER);
		} while(whenToken(TokenType.SCOMMA));
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

		Token t = popToken();
		switch(t.getTokenType()) {
		case SIF:
			expression();
			expectToken(TokenType.STHEN);
			compound();
			if(testToken(TokenType.SELSE)) {
				expectToken(TokenType.SELSE);
				compound();
			}
			break;
		case SWHILE:
			expression();
			expectToken(TokenType.SDO);
			compound();
			break;
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
	
	private void term() {
		TokenType[] op = new TokenType[]{
				TokenType.SSTAR, TokenType.SDIVD, TokenType.SMOD, TokenType.SAND};
		
		factor();
		while(whenToken(op)) {
			factor();
		}
	}
	
	private void factor() {
//		System.out.println("factor " + data.get(0).getTokenType().name() + ":" + data.get(0).getValue());

		Token t = popToken();
		switch(t.getTokenType()) {
		case SCONSTANT:
		case SSTRING:
		case SFALSE:
		case STRUE:
//			constant();
			break;
		case SIDENTIFIER:
			pushToken(t);
			variable();
			break;
		case SLPAREN:
			expression();
			expectToken(TokenType.SRPAREN);
			break;
		case SNOT:
			factor();
		default:
			fail(t);
		}
	}

	private void constant() {
		TokenType[] ct = new TokenType[]{
				TokenType.SINTEGER, TokenType.SSTRING,
				TokenType.SFALSE, TokenType.STRUE
		};
		expectToken(ct);
	}
	
	public boolean parse() {
		program();
		return this.data.isEmpty();
	}
}
