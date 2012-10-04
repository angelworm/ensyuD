package jp.angeworm.ensyuD;

public class LexerTest {	
	
	public static void main(String args[]) {
		
		String ret = "\n";
		String testcase1 = "" 
				+ "{ SAMPLE : OK }" + ret
				+ "program coverage(output);" + ret
				+ "var	Sum, V : integer;" + ret
				+ "" + ret
				+ "procedure printData;" + ret
				+ "	begin" + ret;
				
		
		for(Token t : (new Lexer(testcase1)).doLex()) {
			System.out.println(
					t.getValue()
					+ "\t" + t.getTokenType().name()
					+ "\t" + t.getTokenType().ordinal()
					+ "\t" + t.getLineNumber()
					);
		}
	}
}
