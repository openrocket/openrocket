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

import java.util.Set;
import java.util.Stack;

/**
 * Translate a mathematical expression in human readable infix notation to a
 * Reverse Polish Notation (postfix) expression for easier parsing. by
 * implementing the shunting yard algorithm by dijkstra
 * 
 * @author fas@congrace.de
 */
class InfixTranslator {

	private static String substituteUnaryOperators(String expr) {
		final StringBuilder exprBuilder = new StringBuilder(expr.length());
		final char[] data = expr.toCharArray();
		char lastChar = ' ';
		for (int i = 0; i < expr.length(); i++) {
			if (exprBuilder.length() > 0) {
				lastChar = exprBuilder.charAt(exprBuilder.length() - 1);
			}
			final char c = data[i];
			switch (c) {
			case '+':
				if (i > 0 && lastChar != '(' && !(OperatorToken.isOperator(lastChar))) {
					exprBuilder.append(c);
				}
				break;
			case '-':
				if (i > 0 && lastChar != '(' && !(OperatorToken.isOperator(lastChar))) {
					exprBuilder.append(c);
				} else {
					exprBuilder.append('#');
				}
				break;
			default:
				if (!Character.isWhitespace(c)) {
					exprBuilder.append(c);
				}
			}
		}
		return exprBuilder.toString();
	}

	/**
	 * Delegation method for simple expression without variables or custom
	 * functions
	 * 
	 * @param infixExpression
	 *            the infix expression to be translated
	 * @return translated RNP postfix expression
	 * @throws UnparsableExpressionException
	 *             when the expression is invalid
	 * @throws UnknownFunctionException
	 *             when an unknown function has been used in the input.
	 */
	static String toPostfixExpression(String infixExpression) throws UnparsableExpressionException, UnknownFunctionException {
		return toPostfixExpression(infixExpression, null, null);
	}

	/**
	 * implement the shunting yard algorithm
	 * 
	 * @param infixExpression
	 *            the human readable expression which should be translated to
	 *            RPN
	 * @param variableNames
	 *            the variable names used in the expression
	 * @param customFunctions
	 *            the CustomFunction implementations used
	 * @return the expression in postfix format
	 * @throws UnparsableExpressionException
	 *             if the expression could not be translated to RPN
	 * @throws UnknownFunctionException
	 *             if an unknown function was encountered
	 */
	static String toPostfixExpression(String infixExpression, String[] variableStrings, Set<CustomFunction> customFunctions)
			throws UnparsableExpressionException, UnknownFunctionException {
		infixExpression = substituteUnaryOperators(infixExpression);
		final Token[] tokens = new Tokenizer(variableStrings, customFunctions).tokenize(infixExpression);
		final StringBuilder output = new StringBuilder(tokens.length);
		final Stack<Token> operatorStack = new Stack<Token>();
		for (final Token token : tokens) {
			token.mutateStackForInfixTranslation(operatorStack, output);
		}
		// all tokens read, put the rest of the operations on the output;
		while (operatorStack.size() > 0) {
			output.append(operatorStack.pop().getValue()).append(" ");
		}
		return output.toString().trim();
	}
}
