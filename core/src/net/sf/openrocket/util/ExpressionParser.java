package net.sf.openrocket.util;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;

public class ExpressionParser {
	
	
	public double parse(String expression) throws InvalidExpressionException {
		try {
			ExpressionBuilder builder = new ExpressionBuilder(modify(expression));
			Calculable calc = builder.build();
			return calc.calculate();
		} catch (java.lang.NumberFormatException e) {
			throw new InvalidExpressionException("Invalid expression: " + expression, e);
		} catch (UnknownFunctionException e) {
			throw new InvalidExpressionException("Invalid expression: " + expression, e);
		} catch (UnparsableExpressionException e) {
			throw new InvalidExpressionException("Invalid expression: " + expression, e);
		} catch (java.util.EmptyStackException e) {
			throw new InvalidExpressionException("Invalid expression: " + expression, e);
		}
	}
	
	private String modify(String exp) throws InvalidExpressionException {
		char[] chars = exp.toCharArray();
		for( int i = 0; i< chars.length; i++ ) {
			int value = Character.getNumericValue(chars[i]);
			if ( value >= 0 && value < 10 ) {
				chars[i] = Character.toChars(48 + value)[0];
			}
			if ( chars[i] == '\u2044') {
				chars[i] = '/';
			}
		}
		exp = String.copyValueOf(chars);
		exp = exp.replaceAll("(\\d+)\\s+(\\d+)\\s*/\\s*(\\d+)", "($1+$2/$3)");
		exp = exp.replace(',', '.');
		// Disallow spaces between numbers - default is to remove spaces!
		if (exp.matches(".*[0-9.]\\s+[0-9.].*")) {
			throw new InvalidExpressionException("Invalid expression: " + exp);
		}
		return exp;
	}
	
}
