package jp.angeworm.ensyuD;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jp.angeworm.ensyuD.language.PascalLike;

public class Compiler {
	private static String read(String path) {
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
	
	private static void write(String path, String contents) {
		File file = new File(path);
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pw.print(contents);
		pw.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("useage:");
			System.out.println("\tCompiler.jar input.ts output.cas");
			return;
		}
		try {
			List<Token> ts = Lexer.read(args[0]);
			if(!Parser.parse(ts)) {
				System.out.println("NG");
				return;
			}
			if(!Checker.parse(ts)) {
				System.out.print("NG");
				return;
			}
			
			PascalLike pl = jp.angeworm.ensyuD.compiler.Parser.parse(ts);
			//List<String> data = jp.angeworm.ensyuD.compiler.Compiler(pl);
			String code = jp.angeworm.ensyuD.compiler.Compile.compile(pl);
			write(args[1], code);
		} catch (IOException e) {
			System.err.println("no input file "+args[0]+".");
			System.out.println(e.getLocalizedMessage());
		} catch (RuntimeException e) {
			System.out.println("NG");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
