package jp.angeworm.ensyuD;

import java.io.*;

public class LexerTest {	
	
	public static void main(String args[]) {
		String ret = "\n";
		String testcase = "" 
				+ "{ SAMPLE : OK }" + ret
				+ "program coverage(output);" + ret
				+ "var	Sum, V: integer;" + ret
				+ "" + ret
				+ "procedure printData;" + ret
				+ "	begin" + ret;
				
		
		if(args.length > 0) {
			try{
				File file = new File(args[0]);
				BufferedReader br = new BufferedReader(new FileReader(file));
				StringBuilder sb = new StringBuilder();
				String str = null;
				
				while((str = br.readLine()) != null){
					sb.append(str + "\n");
				}
				
				testcase = sb.toString();
				br.close();
			}catch(FileNotFoundException e){
				System.out.println(e);
			}catch(IOException e){
				System.out.println(e);
			}
		}
		
		if(args.length > 1) {
			try{
				File file = new File(args[1]);
				FileWriter filewriter = new FileWriter(file);
				
				for(Token t : Lexer.analyze(testcase)) {
					filewriter.write(
							t.getValue()
							+ "\t" + t.getTokenType().name()
							+ "\t" + t.getTokenType().ordinal()
							+ "\t" + t.getLineNumber() + "\n"
							);
				}
		        		
				filewriter.close();
			}catch(IOException e){
				System.out.println(e);
			}
		} else {
			for(Token t : Lexer.analyze(testcase)) {
				System.out.println(
						t.getValue()
						+ "\t" + t.getTokenType().name()
						+ "\t" + t.getTokenType().ordinal()
						+ "\t" + t.getLineNumber()
						);
			}
		}
	}
}
