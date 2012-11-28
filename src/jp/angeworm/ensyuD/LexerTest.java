package jp.angeworm.ensyuD;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.util.List;

import org.junit.Test;

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
	@Test
	public void readerAndWriter() {
		//fail("Not yet implemented");
		assertTrue("001",run("EnshuD-toolkit-2012/testdata/001.pas"));
		assertTrue("002",run("EnshuD-toolkit-2012/testdata/002.pas"));
		assertTrue("003",run("EnshuD-toolkit-2012/testdata/003.pas"));
		assertTrue("004",run("EnshuD-toolkit-2012/testdata/004.pas"));
		assertTrue("005",run("EnshuD-toolkit-2012/testdata/005.pas"));
		assertTrue("006",run("EnshuD-toolkit-2012/testdata/006.pas"));
		assertTrue("007",run("EnshuD-toolkit-2012/testdata/007.pas"));
		assertTrue("008",run("EnshuD-toolkit-2012/testdata/008.pas"));
		assertTrue("009",run("EnshuD-toolkit-2012/testdata/009.pas"));
		assertTrue("010",run("EnshuD-toolkit-2012/testdata/010.pas"));
		assertTrue("011",run("EnshuD-toolkit-2012/testdata/011.pas"));
		assertTrue("012",run("EnshuD-toolkit-2012/testdata/012.pas"));
		assertTrue("013",run("EnshuD-toolkit-2012/testdata/013.pas"));
	}

	private String read(String path) {
		try {
			File file = new File(path);
			FileReader filereader = new FileReader(file);
			BufferedReader br = new BufferedReader(filereader);
			
			String line = "";
			StringBuilder b = new StringBuilder();
			while((line = br.readLine()) != null) {
				b.append(line);
				b.append('\n');
			}
			return b.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	

	private boolean run(String path) {
		String infix = path.substring(0, path.lastIndexOf('.'));
		try {
			System.out.println("#######################");
			System.out.println(Lexer.read(infix + ".ts"));
			System.out.println("-----------------------");
			System.out.println(Lexer.analyze(read(path)));
			return Lexer.read(infix + ".ts").containsAll(Lexer.analyze(read(path)));
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	

}
