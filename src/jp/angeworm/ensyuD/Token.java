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
	public Token(TokenType tt, String v) {
		type = tt;
		value = v;
		lineNumber = 0;
	}
	public Token(TokenType tt) {
		type = tt;
		value = "";
		lineNumber = 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return 	value
				+ "\t" + type.name()
				+ "\t" + type.ordinal()
				+ "\t" + lineNumber;
	}
}
