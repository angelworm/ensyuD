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
		default: throw new RuntimeException("unexpected type "+ t.getValue()+ " near at line " + t.getLineNumber() + ".");
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
			
			for(Variable i : vars) {
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
			expectToken(TokenType.SLBRACKET);
			number();
			expectToken(TokenType.SRANGE);
			expectToken(TokenType.SCONSTANT);
			expectToken(TokenType.SRBRACKET);
			expectToken(TokenType.SOF);
			if(testStandardTypes()) {
				Token t = popToken();
				switch(t.getTokenType()) {
				case SINTEGER: return VariableType.INTEGER_ARRAY;
				case SBOOLEAN: return VariableType.BOOLEAN_ARRAY;
				case SCHAR:    return VariableType.CHAR_ARRAY;
				default: throw new RuntimeException("unexpected type "+ t.getValue()+ " near at line " + t.getLineNumber() + ".");
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
		System.out.print("#####variable:\n"+env);

	}
	
	private void sentence() {
//		System.out.println("sentence " + data.get(0).getTokenType().name() + ":" + data.get(0).getValue());

		Token t = popToken();
		switch(t.getTokenType()) {
		case SBEGIN:
			pushToken(t);
			compound();
			break;
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
			sentence();
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
		int linum = getLineNumber();
		String name = getIdentifer().getName();
		
		Variable v = env.find(name);
		if(v == null) {
			throw new RuntimeException("Undefined Identifer " + name + " at " + linum);
		}
		
		linum = getLineNumber();
		if(testToken(TokenType.SLBRACKET) && !v.getType().isArray()) {
			throw new RuntimeException( name + "is not array at " + linum);
		}
		
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
			break;
		default:
			fail(t);
		}
	}

	public boolean parse() {
		program();
		return this.data.isEmpty();
	}
}
