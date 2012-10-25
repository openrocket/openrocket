package net.sf.openrocket.util;

import java.text.DecimalFormatSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;

public class ExpressionParser {
	private static final Logger log = LoggerFactory.getLogger(ExpressionParser.class);
	
	private static final char DECIMAL_SEPARATOR;
	private static final char MINUS_SIGN;
	static {
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
		DECIMAL_SEPARATOR = symbols.getDecimalSeparator();
		MINUS_SIGN = symbols.getMinusSign();
	}
	
	public double parse(String expression) throws InvalidExpressionException {
		
		String modified = null;
		try {
			modified = modify(expression);
			ExpressionBuilder builder = new ExpressionBuilder(modified);
			Calculable calc = builder.build();
			double n = calc.calculate().getDoubleValue();
			log.debug("Evaluated expression '" + expression + "' (modified='" + modified + "') to " + n);
			return n;
		} catch (Exception e) {
			log.warn("Unable to parse expression '" + expression + "' (modified='" + modified + "')", e);
			throw new InvalidExpressionException("Invalid expression: " + expression, e);
		}
	}
	
	private String modify(String exp) throws InvalidExpressionException {
		
		// Normalize digit equivalents, fraction sign, decimal separators and minus sign
		char[] chars = exp.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			int value = Character.getNumericValue(chars[i]);
			if (value >= 0 && value < 10) {
				chars[i] = Character.toChars(48 + value)[0];
			}
			if (chars[i] == Chars.FRACTION) {
				chars[i] = '/';
			}
			if (chars[i] == DECIMAL_SEPARATOR || chars[i] == ',') {
				chars[i] = '.';
			}
			if (chars[i] == MINUS_SIGN) {
				chars[i] = '-';
			}
		}
		exp = String.copyValueOf(chars);
		
		// Replace fraction equivalents "1 3/4" with "(1+3/4)"
		exp = exp.replaceAll("(?<![\\d.])(\\d+)\\s+(\\d+)\\s*/\\s*(\\d+)(?![\\d.])", "($1+$2/$3)");
		
		// Disallow spaces between numbers - default is to remove spaces!
		if (exp.matches(".*[0-9.]\\s+[0-9.].*")) {
			throw new InvalidExpressionException("Expression contains excess space: " + exp);
		}
		return exp;
	}
}
