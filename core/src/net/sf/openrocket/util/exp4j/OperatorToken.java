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

import java.util.Map;
import java.util.Stack;

/**
 * {@link Token} for Operations like +,-,*,/,% and ^
 * 
 * @author fas@congrace.de
 */
class OperatorToken extends CalculationToken {

	/**
	 * the valid {@link Operation}s for the {@link OperatorToken}
	 * 
	 * @author fas@congrace.de
	 */
	enum Operation {
		ADDITION(1, true), SUBTRACTION(1, true), MULTIPLICATION(2, true), DIVISION(2, true), MODULO(2, true), EXPONENTIATION(3, false), UNARY_MINUS(4, false), UNARY_PLUS(
				4, false);
		private final int precedence;
		private final boolean leftAssociative;

		private Operation(int precedence, boolean leftAssociative) {
			this.precedence = precedence;
			this.leftAssociative = leftAssociative;
		}
	}

	/**
	 * return a corresponding {@link Operation} for a symbol
	 * 
	 * @param c
	 *            the symbol of the operation
	 * @return the corresponding {@link Operation}
	 */
	static Operation getOperation(char c) {
		switch (c) {
		case '+':
			return Operation.ADDITION;
		case '-':
			return Operation.SUBTRACTION;
		case '*':
			return Operation.MULTIPLICATION;
		case '/':
			return Operation.DIVISION;
		case '^':
			return Operation.EXPONENTIATION;
		case '#':
			return Operation.UNARY_MINUS;
		case '%':
			return Operation.MODULO;
		default:
			return null;
		}
	}

	static boolean isOperator(char c) {
		return getOperation(c) != null;
	}

	private final Operation operation;

	/**
	 * construct a new {@link OperatorToken}
	 * 
	 * @param value
	 *            the symbol (e.g.: '+')
	 * @param operation
	 *            the {@link Operation} of this {@link Token}
	 */
	OperatorToken(String value, Operation operation) {
		super(value);
		this.operation = operation;
	}

	/**
	 * apply the {@link Operation}
	 * 
	 * @param values
	 *            the doubles to operate on
	 * @return the result of the {@link Operation}
	 */
	double applyOperation(double... values) {
		switch (operation) {
		case ADDITION:
			return values[0] + values[1];
		case SUBTRACTION:
			return values[0] - values[1];
		case MULTIPLICATION:
			return values[0] * values[1];
		case EXPONENTIATION:
			return Math.pow(values[0], values[1]);
		case DIVISION:
			return values[0] / values[1];
		case UNARY_MINUS:
			return -values[0];
		case UNARY_PLUS:
			return values[0];
		case MODULO:
			return values[0] % values[1];
		default:
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OperatorToken) {
			final OperatorToken t = (OperatorToken) obj;
			return t.getValue().equals(this.getValue());
		}
		return false;
	}

	int getOperandCount() {
		switch (operation) {
		case ADDITION:
		case SUBTRACTION:
		case MULTIPLICATION:
		case DIVISION:
		case EXPONENTIATION:
		case MODULO:
			return 2;
		case UNARY_MINUS:
		case UNARY_PLUS:
			return 1;
		default:
			return 0;
		}
	}

	/**
	 * get the {@link Operation} of this {@link Token}
	 * 
	 * @return the {@link Operation}
	 */
	Operation getOperation() {
		return operation;
	}

	int getPrecedence() {
		return operation.precedence;
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	/**
	 * check if the operation is left associative
	 * 
	 * @return true if left associative, otherwise false
	 */
	boolean isLeftAssociative() {
		return operation.leftAssociative;
	}

	@Override
	void mutateStackForCalculation(Stack<Double> stack, Map<String, Double> variableValues) {
		if (this.getOperandCount() == 2) {
			final double n2 = stack.pop();
			final double n1 = stack.pop();
			stack.push(this.applyOperation(n1, n2));
		} else if (this.getOperandCount() == 1) {
			final double n1 = stack.pop();
			stack.push(this.applyOperation(n1));
		}
	}

	@Override
	void mutateStackForInfixTranslation(Stack<Token> operatorStack, StringBuilder output) {
		Token before;
		while (!operatorStack.isEmpty() && (before = operatorStack.peek()) != null && (before instanceof OperatorToken || before instanceof FunctionToken)) {
			if (before instanceof FunctionToken) {
				operatorStack.pop();
				output.append(before.getValue()).append(" ");
			} else {
				final OperatorToken stackOperator = (OperatorToken) before;
				if (this.isLeftAssociative() && this.getPrecedence() <= stackOperator.getPrecedence()) {
					output.append(operatorStack.pop().getValue()).append(" ");
				} else if (!this.isLeftAssociative() && this.getPrecedence() < stackOperator.getPrecedence()) {
					output.append(operatorStack.pop().getValue()).append(" ");
				} else {
					break;
				}
			}
		}
		operatorStack.push(this);
	}
}
