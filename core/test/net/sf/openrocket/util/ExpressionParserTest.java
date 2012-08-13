package net.sf.openrocket.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExpressionParserTest {
	
	private static final double EPS = 1e-10;
	
	private ExpressionParser parser = new ExpressionParser();
	
	@Test
	public void testPlainNumber() throws InvalidExpressionException {
		assertEquals(1.0, parser.parse("1"), EPS);
		assertEquals(1.0, parser.parse("\t 1 "), EPS);
		assertEquals(0.9, parser.parse(".9"), EPS);
		assertEquals(1.0, parser.parse("1."), EPS);
		assertEquals(1.2, parser.parse("1.2"), EPS);
		assertEquals(1.2, parser.parse("01.200"), EPS);
	}
	
	@Test
	public void testNegativeNumber() throws InvalidExpressionException {
		assertEquals(-1.0, parser.parse("-1"), EPS);
		assertEquals(-15.0, parser.parse("-15"), EPS);
		assertEquals(-0.9, parser.parse("-.9"), EPS);
		assertEquals(-1.0, parser.parse("-1."), EPS);
		assertEquals(-1.2, parser.parse("-1.2"), EPS);
		assertEquals(-1.2, parser.parse("-01.200"), EPS);
	}
	
	
	@Test
	public void testDecimalComma() throws InvalidExpressionException {
		assertEquals(1.0, parser.parse("1,"), EPS);
		assertEquals(1.2, parser.parse("1,2"), EPS);
		assertEquals(1.2, parser.parse("01,200"), EPS);
		assertEquals(0.9, parser.parse(",9"), EPS);
	}
	
	
	@Test
	public void testSimpleExpression() throws InvalidExpressionException {
		assertEquals(3.0, parser.parse("1+2"), EPS);
		assertEquals(6.0, parser.parse("1+2.5*2"), EPS);
		assertEquals(7.0, parser.parse("(1+2.5) * 2"), EPS);
		assertEquals(1.0 + 2.0 / 3.0, parser.parse("1+2/3"), EPS);
	}
	
	@Test
	public void testFraction() throws InvalidExpressionException {
		assertEquals(1.5, parser.parse("1 1/2"), EPS);
		assertEquals(11 + 11.0 / 22.0, parser.parse("11 11/22"), EPS);
		assertEquals(-11 - 11.0 / 22.0, parser.parse("-11 11/22"), EPS);
		assertEquals(1.5, parser.parse("  1    1 / 2"), EPS);
		assertEquals(11 + 11.0 / 22.0, parser.parse("  11    11 / 22"), EPS);
		assertEquals(2.0 + 3.0 / 7.0, parser.parse("1 + 1 3/7"), EPS);
		assertEquals(2.0 + 3.0 / 7.0, parser.parse("1 + 1 3/7"), EPS);
		assertEquals(3.0, parser.parse("1 1/2* 2"), EPS);
		assertEquals(3.0, parser.parse("1 1/2* 2"), EPS);
	}
	
	@Test
	public void testCharConversion() throws InvalidExpressionException {
		assertEquals(1 + 1.0 / 9.0, parser.parse("1 \u2081 \u2044 \u2089"), EPS);
	}
	
	@Test
	public void testInvalidExpression() {
		expectInvalid("1+");
		expectInvalid("1+2/");
		expectInvalid("1 2");
		expectInvalid("12 2.5");
		expectInvalid("1 2.5/4");
		expectInvalid("11 22.55/44");
		expectInvalid("1 2/4.1");
		expectInvalid("11 22/44.11");
		expectInvalid("1.2 3/4");
		expectInvalid("12.23 34/45");
		
		expectInvalid("1. 2");
		expectInvalid("1 .2");
	}
	
	private void expectInvalid(String exp) {
		try {
			double value = parser.parse(exp);
			fail("Expression '" + exp + "' evaluated to " + value + ", expected failure");
		} catch (InvalidExpressionException e) {
			// expected
		}
	}
}
