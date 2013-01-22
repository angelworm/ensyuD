package jp.angeworm.ensyuD;

import static org.junit.Assert.*;
import jp.angeworm.ensyuD.compiler.Compile;
import jp.angeworm.ensyuD.compiler.Parser;
import jp.angeworm.ensyuD.compiler.RegisterStack;

import org.junit.Test;

public class CompilerTest {
	@Test
	public void registerStackTest() {
		RegisterStack rs = new RegisterStack(3);
		
		assertEquals("1: take 0 stack push", rs.needStackPush(), false);
		assertEquals("1: take 0 stack pop" , rs.needStackPop(),  false);
		assertEquals("1: take 0", rs.take(), 0);
		assertEquals("2: take 1 stack push", rs.needStackPush(), false);
		assertEquals("2: take 1 stack pop" , rs.needStackPop(),  false);
		assertEquals("2: take 1", rs.take(), 1);
		assertEquals("3: take 2 stack push", rs.needStackPush(), false);
		assertEquals("3: take 2 stack pop" , rs.needStackPop(),  false);
		assertEquals("3: take 2", rs.take(), 2);
		assertEquals("4: take 0 stack push", rs.needStackPush(), true);
		assertEquals("4: take 0 stack pop" , rs.needStackPop(),  false);
		assertEquals("3: free 2", rs.free(), 2);
		assertEquals("3: take 2 stack push", rs.needStackPush(), false);
		assertEquals("3: take 2 stack pop" , rs.needStackPop(),  false);
		assertEquals("2: free 1", rs.free(), 1);
		assertEquals("2: take 1 stack push", rs.needStackPush(), false);
		assertEquals("2: take 1 stack pop" , rs.needStackPop(),  false);
		assertEquals("1: free 0", rs.free(), 0);
		assertEquals("1: take 0 stack push", rs.needStackPush(), false);
		assertEquals("1: take 0 stack pop" , rs.needStackPop(),  false);
	}
	@Test
	public void registerStackTest2() {
		RegisterStack rs = new RegisterStack(3);
		
		assertEquals("take 0", rs.take(), 0);
		assertEquals("take 1", rs.take(), 1);
		assertEquals("take 2", rs.take(), 2);
		assertEquals("take 0", rs.take(), 0);
		assertEquals("4: take 0 stack pop" , rs.needStackPop(),  true);
		assertEquals("take 1", rs.take(), 1);
		assertEquals("take 2", rs.take(), 2);
		assertEquals("take 0", rs.take(), 0);
		assertEquals("take 1", rs.take(), 1);
		assertEquals("take 2", rs.take(), 2);
		assertEquals("free 2", rs.free(), 2);
		assertEquals("free 1", rs.free(), 1);
		assertEquals("free 0", rs.free(), 0);
		assertEquals("free 2", rs.free(), 2);
		assertEquals("free 1", rs.free(), 1);
		assertEquals("free 0", rs.free(), 0);
		assertEquals("free 2", rs.free(), 2);
		assertEquals("free 1", rs.free(), 1);
		assertEquals("free 0", rs.free(), 0);
	}

	@Test
	public void ExpressionTest() {
		String pascal = "" +
				"		program pas102(output);" +
				"" +
				"var     n:integer;"+
				"        dy:array [-12..8] of integer;"+
				""+
				"begin"+
				"  n := 12 * 2 + 3 - 1"+
				"end.";
		System.out.println(Compile.compile(Parser.parse(Lexer.analyze(pascal))));
		//assertEquals(Parser.parse(Lexer.analyze(pascal) ), "");
	}
}
