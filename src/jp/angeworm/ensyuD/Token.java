package jp.angeworm.ensyuD;

public class Token {
	private TokenType	type;
	private String 		value;
	private int			lineNumber;
	
	public TokenType getTokenType() {
		return type;
	}
	public void setTokenType(TokenType type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public Token(TokenType tt, String v, int linum) {
		type = tt;
		value = v;
		lineNumber = linum;
	}
}
