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
 * Class for calculating values from a RPN postfix expression.<br/>
 * The default way to create a new instance of {@link PostfixExpression} is by
 * using the static factory method fromInfix()
 * 
 * @author fas@congrace.de
 */
public final class PostfixExpression extends AbstractExpression implements Calculable {
	
	private VariableSet variables = new VariableSet();
	
	/**
	 * Factory method for creating {@link PostfixExpression}s from human
	 * readable infix expressions
	 * 
	 * @param expression
	 *            the infix expression to be used
	 * @return an equivalent {@link PostfixExpression}
	 * @throws UnparsableExpressionException
	 *             if the expression was invalid
	 * @throws UnknownFunctionException
	 *             if an unknown function has been used
	 * @deprecated please use {@link ExpressionBuilder} API
	 */
    @Deprecated
	public static PostfixExpression fromInfix(String expression) throws UnparsableExpressionException, UnknownFunctionException {
		return fromInfix(expression, null);
	}

	/**
	 * Factory method for creating {@link PostfixExpression}s from human
	 * readable infix expressions
	 * 
	 * @param expression
	 *            the infix expression to be used
	 * @param customFunctions
	 *            the CustomFunction implementations used
	 * @return an equivalent {@link PostfixExpression}
	 * @throws UnparsableExpressionException
	 *             if the expression was invalid
	 * @throws UnknownFunctionException
	 *             if an unknown function has been used
	 * @deprecated please use {@link ExpressionBuilder}
	 */
    @Deprecated
	public static PostfixExpression fromInfix(String expression, Set<CustomFunction> customFunctions) throws UnparsableExpressionException,
			UnknownFunctionException {
		String[] variableStrings = null;
		int posStart, posEnd;
		if ((posStart = expression.indexOf('=')) > 0) {
			String functionDef = expression.substring(0, posStart);
			expression = expression.substring(posStart + 1);
			if ((posStart = functionDef.indexOf('(')) > 0 && (posEnd = functionDef.indexOf(')')) > 0) {
				variableStrings = functionDef.substring(posStart + 1, posEnd).split(",");
			}
		}
		return new PostfixExpression(InfixTranslator.toPostfixExpression(expression, variableStrings, customFunctions), variableStrings, customFunctions);
	}
	
	/**
	 * Construct a new simple {@link PostfixExpression}
	 * 
	 * @param expression
	 *            the postfix expression to be calculated
	 * @param variableNames
	 *            the variable names in the expression
	 * @param customFunctions
	 *            the CustomFunction implementations used
	 * @throws UnparsableExpressionException
	 *             when expression is invalid
	 * @throws UnknownFunctionException
	 *             when an unknown function has been used
	 */
	private PostfixExpression(String expression, String[] variableStrings, Set<CustomFunction> customFunctions) throws UnparsableExpressionException,
			UnknownFunctionException {
		super(expression, new Tokenizer(variableStrings, customFunctions).tokenize(expression), variableStrings);
	}

	/**
	 * delegate the calculation of a simple expression 
	 */
	public Variable calculate() throws IllegalArgumentException {

		final Stack<Variable> stack = new Stack<Variable>();
		for (final Token t : getTokens()) {
			((CalculationToken) t).mutateStackForCalculation(stack, variables);
		}
		return stack.pop();

	}

	public void setVariable(Variable value) {
		variables.add(value);
	}
}
