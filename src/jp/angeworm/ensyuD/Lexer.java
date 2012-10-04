package jp.angeworm.ensyuD;

import java.util.LinkedList;
import java.util.List;

public class Lexer {
	private char[] str;
	private int index;
	private int linenum;
	
	public Lexer(String str_) {
		str = str_.toCharArray();
	}
	
	private boolean isAscii(char c) {
		return Character.isUpperCase(c) || Character.isLowerCase(c);
	}
	
	private String identifer() {
		if(index >= str.length
				|| !Character.isLetter(str[index])) 
			return null;
		
		StringBuilder b = new StringBuilder();
		
		while(index < str.length && Character.isLetter(str[index])) {
			b.append(str[index++]);
		}
		
		return b.toString();
	}
	
	private Token getReservedOrIdentifer() {
		String value = identifer();
		Token t = new Token(TokenType.SIDENTIFER, value, linenum);
		
		if(value.equals("and")) 	{t.setTokenType(TokenType.SAND);}
		if(value.equals("array"))	{t.setTokenType(TokenType.SARRAY);}
		if(value.equals("begin"))	{t.setTokenType(TokenType.SBEGIN);}
		if(value.equals("boolean")) {t.setTokenType(TokenType.SBOOLEAN);}
		if(value.equals("char")) 	{t.setTokenType(TokenType.SCHAR);}
		if(value.equals("div")) 	{t.setTokenType(TokenType.SDIVD);}
		if(value.equals("do")) 		{t.setTokenType(TokenType.SDO);}
		if(value.equals("else"))	{t.setTokenType(TokenType.SELSE);}
		if(value.equals("end"))		{t.setTokenType(TokenType.SEND);}
		if(value.equals("false")) 	{t.setTokenType(TokenType.SFALSE);}
		if(value.equals("if")) 		{t.setTokenType(TokenType.SIF);}
		if(value.equals("integer")) {t.setTokenType(TokenType.SINTEGER);}
		if(value.equals("mod")) 	{t.setTokenType(TokenType.SMOD);}
		if(value.equals("not")) 	{t.setTokenType(TokenType.SNOT);}
		if(value.equals("of")) 		{t.setTokenType(TokenType.SOF);}
		if(value.equals("or")) 		{t.setTokenType(TokenType.SOR);}
		if(value.equals("procedure")) {t.setTokenType(TokenType.SPROCEDURE);}
		if(value.equals("program")) {t.setTokenType(TokenType.SPROCEDURE);}
		if(value.equals("readln")) 	{t.setTokenType(TokenType.SREADLN);}
		if(value.equals("then")) 	{t.setTokenType(TokenType.STHEN);}
		if(value.equals("true")) 	{t.setTokenType(TokenType.STRUE);}
		if(value.equals("var")) 	{t.setTokenType(TokenType.SVAR);}
		if(value.equals("while")) 	{t.setTokenType(TokenType.SWHILE);}
		if(value.equals("writeln")) {t.setTokenType(TokenType.SWRITELN);}
		
		return t;
	}
	
	public List<Token> doLex() {
		int size = str.length;
		LinkedList<Token> ret = new LinkedList<Token>();
		
		index = 0;
		linenum = 1;
		
		while(index < size) {
			char c = str[index];
			
			if(Character.isWhitespace(c)) {
				if(c == '\n') linenum++;
				index++;
			} else if(isAscii(c)) {
				Token tmp = getReservedOrIdentifer();
				if(tmp == null) throw new RuntimeException();
				ret.add(tmp);
			} else if(c == '{') {
				while(index < size && str[index] != '}') {
					if(str[index++] == '\n') linenum++;
				}
				index++;
			} else {
				Token t = new Token(TokenType.SIDENTIFER, String.valueOf(c), linenum);
				index += 1;
				switch(c) {
				case '=': t.setTokenType(TokenType.SEQUAL); break;
				case '+': t.setTokenType(TokenType.SPLUS); break;
				case '-': t.setTokenType(TokenType.SMINUS); break;
				case '*': t.setTokenType(TokenType.SSTAR); break;
				case '/': t.setTokenType(TokenType.SDIVD); break;
				case '(': t.setTokenType(TokenType.SLPAREN); break;
				case ')': t.setTokenType(TokenType.SRPAREN); break;
				case '[': t.setTokenType(TokenType.SLBRACKET); break;
				case ']': t.setTokenType(TokenType.SRBRACKET); break;
				case ';': t.setTokenType(TokenType.SSEMICOKON); break;
				case ',': t.setTokenType(TokenType.SCOMMA); break;
				case '<':
					t.setTokenType(TokenType.SLESS);
					if(index >= size) break;
					if(str[index] == '>') {
						t = new Token(TokenType.SNOTEQUAL, "<>", linenum);
						index++;
					}
					if(str[index] == '=') {
						t = new Token(TokenType.SLESSEQUAL, "<=", linenum);
						index++;
					}
					break;
				case '>':
					t.setTokenType(TokenType.SGREAT);
					if(index >= size) break;
					if(str[index] == '=') {
						t = new Token(TokenType.SGREATEQUAL, ">=", linenum);
						index++;
					}
					break;
				case '.':
					t.setTokenType(TokenType.SDOT);
					if(index >= size) break;
					if(str[index] == '.') {
						t = new Token(TokenType.SRANGE, "..", linenum);
						index++;
					}
					break;
				case ':':
					t.setTokenType(TokenType.SCOLON);
					if(index >= size) break;
					if(str[index] == '=') {
						t = new Token(TokenType.SASSIGN, ":=", linenum);
						index++;
					}
					break;
				case '\'':
					StringBuilder b = new StringBuilder();
					while(++index >= size || str[index] != '\'' || str[index] != '\n') {
						b.append(str[index]);
					}
					
					if(str[index] == '\n') 
						throw new RuntimeException("unexpected return code : line = "+linenum);
					if(index >= size) 
						throw new RuntimeException("unexpected end of file : line = "+linenum);
					
					t = new Token(TokenType.SSTRING, b.toString(), linenum);
				default:
					throw new RuntimeException("unexpected token : " + str[index-1] + ": line = "+linenum);
				}
				ret.add(t);
			}
		}
		
		return ret;
	}
}
