package jp.angeworm.ensyuD;

import static org.junit.Assert.*;

import java.io.*;

import org.junit.Test;

public class ParserTest {

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
		String ret = "\n";
		String testcase = "{test program for subset pascal compiler }" + ret +
				"{\"saishou jijyouhou no keisann}" + ret +
				"" + ret +
				"program pas104(output);" + ret +
				"" + ret +
				"var     n:integer;" + ret +
				"        max: integer;   { const for the number of data }" + ret +
				"        x:integer;" + ret +
				"        y:integer;" + ret +
				"        sx:integer;" + ret + //10
				"        sy:integer;" + ret +
				"        sxy:integer;" + ret +
				"        sx2:integer;" + ret +
				"        w:integer;" + ret +
				"        a:integer;" + ret +
				"        b:integer;" + ret +
				"        dx:array [1..8] of integer;" + ret +
				"        dy:array [1..8] of integer;" + ret +
				"" + ret +
				"begin" + ret + // 20
				"        max := 8;" + ret +
				"        dx[1]:=96;   dy[1]:=86;" + ret +
				"        dx[2]:=89;   dy[2]:=56;" + ret +
				"        dx[3]:=78;   dy[3]:=81;" + ret +
				"        dx[4]:=68;   dy[4]:=86;" + ret +
				"        dx[5]:=58;   dy[5]:=78;" + ret +
				"        dx[6]:=49;   dy[6]:=56;" + ret +
				"        dx[7]:=39;   dy[7]:=23;" + ret +
				"        dx[8]:=32;   dy[8]:=24;" + ret +
				"" + ret + // 30
				"        n:=1;sx:=0;sy:=0;sxy:=0;sx2:=0;" + ret +
				"        writeln('         No.         x           y          xy          x^2');" + ret +
				"        while n<=max do begin" + ret +
				"                writeln( n, dx[n], dy[n], dx[n]*dy[n], dx[n]*dx[n]);" + ret +
				"                sx:=sx+dx[n];   sy:=sy+dy[n];" + ret +
				"                sxy:=sxy+dx[n]*dy[n];" + ret +
				"                sx2:=sx2+dx[n]*dx[n];" + ret +
				"                n:=n+1" + ret +
				"        end;" + ret +
				"        writeln;" + ret + // 40
				"        writeln( 'Sigma(x)=   ',sx);" + ret +
				"        writeln( 'Sigma(y)=   ',sy);" + ret +
				"        writeln( 'Sigma(xy)=  ',sxy);" + ret +
				"        writeln( 'Sigma(x^2)= ',sx2);" + ret +
				"        w:=max*sx2-sx*sx;" + ret +
				"        if w*sx2=0 then begin" + ret +
				"                writeln( 'Fitting Unsuccessful.')" + ret +
				"	end" + ret +
				"	else begin" + ret + 
				"                b:=(sx2*sy - sxy*sx) div w;" + ret + // 50
				"                a:=(sxy-b*sx) div sx2;" + ret + 
				"                writeln;" + ret +
				"                writeln( 'a=',a,'   b=',b)" + ret +
				"        end" + ret +
				"end" + ".";

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

}
