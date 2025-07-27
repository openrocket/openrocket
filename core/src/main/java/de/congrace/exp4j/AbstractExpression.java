/*
   Copyright 2011 frank asseg

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package de.congrace.exp4j;

import java.text.NumberFormat;

/**
 * Abstract base class for mathematical expressions
 * 
 * @author fas@congrace.de
 */
abstract class AbstractExpression {
	private final String expression;
	private final Token[] tokens;
	private final String[] variableStrings;
	private final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Construct a new {@link AbstractExpression}
	 * 
	 * @param expression
	 *            the mathematical expression to be used
	 * @param tokens
	 *            the {@link Token}s in the expression
	 * @param variableNames
	 *            an array of variable names which are used in the expression
	 */
	AbstractExpression(String expression, Token[] tokens, String[] variableStrings) {
		this.expression = expression;
		this.tokens = tokens;
		this.variableStrings = variableStrings;
	}

	/**
	 * get the mathematical expression {@link String}
	 * 
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * get the used {@link NumberFormat}
	 * 
	 * @return the used {@link NumberFormat}
	 */
	public NumberFormat getNumberFormat() {
		return numberFormat;
	}

	/**
	 * get the {@link Token}s
	 * 
	 * @return the array of {@link Token}s
	 */
	Token[] getTokens() {
		return tokens;
	}
	
}
