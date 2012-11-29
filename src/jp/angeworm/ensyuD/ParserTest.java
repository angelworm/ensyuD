package jp.angeworm.ensyuD;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

public class ParserTest {

	public static void main(String args[]) {
		if(args.length != 1) {
			System.out.println("useage:");
			System.out.println("\tParser.jar input.ts");
			return;
		}
		try {
			if(Parser.parse(Lexer.read(args[0]))){
				System.out.println("OK");
			} else {
				System.out.println("NG");
			}
		} catch (IOException e) {
			System.err.println("no input file "+args[0]+".");
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.out.println("NG");
			e.printStackTrace();
		}
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
		try {
			return Parser.parse(Lexer.analyze(read(path)));
		} catch(RuntimeException e) {
			e.printStackTrace();
			
			return false;
		}
	}
	
	@Test
	public void test() {
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
		
		assertTrue("new07",run("EnshuD-toolkit-2012/subtests/new07.pas"));
		assertTrue("new08",run("EnshuD-toolkit-2012/subtests/new08.pas"));
		assertFalse("new10",run("EnshuD-toolkit-2012/subtests/new10.pas"));
		assertTrue("new11",run("EnshuD-toolkit-2012/subtests/new11.pas"));
		
		assertFalse("test01",run("EnshuD-toolkit-2012/subtests/test01.pas"));
		assertTrue("test02",run("EnshuD-toolkit-2012/subtests/test02.pas"));
		assertTrue("test03",run("EnshuD-toolkit-2012/subtests/test03.pas"));
		assertTrue("test04",run("EnshuD-toolkit-2012/subtests/test04.pas"));
		assertTrue("test05",run("EnshuD-toolkit-2012/subtests/test05.pas"));
		assertTrue("test06",run("EnshuD-toolkit-2012/subtests/test06.pas"));
		assertTrue("test09",run("EnshuD-toolkit-2012/subtests/test09.pas"));
		assertTrue("test12",run("EnshuD-toolkit-2012/subtests/test12.pas"));
		assertTrue("test13",run("EnshuD-toolkit-2012/subtests/test13.pas"));
		assertFalse("test14",run("EnshuD-toolkit-2012/subtests/test14.pas"));

		
	}

}
