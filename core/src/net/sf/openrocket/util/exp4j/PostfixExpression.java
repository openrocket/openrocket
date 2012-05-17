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
package net.sf.openrocket.util.exp4j;

import java.util.HashMap;
import java.util.Map;
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
		String[] variables = null;
		int posStart, posEnd;
		if ((posStart = expression.indexOf('=')) > 0) {
			String functionDef = expression.substring(0, posStart);
			expression = expression.substring(posStart + 1);
			if ((posStart = functionDef.indexOf('(')) > 0 && (posEnd = functionDef.indexOf(')')) > 0) {
				variables = functionDef.substring(posStart + 1, posEnd).split(",");
			}
		}
		return new PostfixExpression(InfixTranslator.toPostfixExpression(expression, variables, customFunctions), variables, customFunctions);
	}

	private final Map<String, Double> variableValues = new HashMap<String, Double>();

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
	private PostfixExpression(String expression, String[] variableNames, Set<CustomFunction> customFunctions) throws UnparsableExpressionException,
			UnknownFunctionException {
		super(expression, new Tokenizer(variableNames, customFunctions).tokenize(expression), variableNames);
	}

	/**
	 * delegate the calculation of a simple expression without variables
	 * 
	 * @return the result
	 */
	public double calculate() {
		return calculate(null);
	}

	/**
	 * calculate the result of the expression and substitute the variables by
	 * their values beforehand
	 * 
	 * @param values
	 *            the variable values to be substituted
	 * @return the result of the calculation
	 * @throws IllegalArgumentException
	 *             if the variables are invalid
	 */
	public double calculate(double... values) throws IllegalArgumentException {
		if (getVariableNames() == null && values != null) {
			throw new IllegalArgumentException("there are no variables to set values");
		} else if (getVariableNames() != null && values == null && variableValues.isEmpty()) {
			throw new IllegalAccessError("variable values have to be set");
		} else if (values != null && values.length != getVariableNames().length) {
			throw new IllegalArgumentException("The are an unequal number of variables and arguments");
		}
		int i = 0;
		if (getVariableNames() != null && values != null) {
			for (double val : values) {
				variableValues.put(getVariableNames()[i++], val);
			}
		}
		final Stack<Double> stack = new Stack<Double>();
		for (final Token t : getTokens()) {
			((CalculationToken) t).mutateStackForCalculation(stack, variableValues);
		}
		return stack.pop();
	}

	public void setVariable(String name, double value) {
		variableValues.put(name, value);
	}
}
