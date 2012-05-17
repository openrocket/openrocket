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
 * A {@link Token} for functions
 * 
 * @author fas@congrace.de
 * 
 */
class FunctionToken extends CalculationToken {
	/**
	 * the functionNames that can be used in an expression
	 * 
	 * @author ruckus
	 * 
	 */
	enum Function {
		ABS, ACOS, ASIN, ATAN, CBRT, CEIL, COS, COSH, EXP, EXPM1, FLOOR, LOG, SIN, SINH, SQRT, TAN, TANH
	}

	private Function function;

	/**
	 * construct a new {@link FunctionToken}
	 * 
	 * @param value
	 *            the name of the function
	 * @throws UnknownFunctionException
	 *             if an unknown function name is encountered
	 */
	FunctionToken(String value) throws UnknownFunctionException {
		super(value);
		try {
			function = Function.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new UnknownFunctionException(value);
		}
		if (function == null) {
			throw new UnknownFunctionException(value);
		}
	}

	/**
	 * apply a function to a value x
	 * 
	 * @param x
	 *            the value the function should be applied to
	 * @return the result of the function
	 */
	double applyFunction(double x) {
		switch (function) {
		case ABS:
			return Math.abs(x);
		case ACOS:
			return Math.acos(x);
		case ASIN:
			return Math.asin(x);
		case ATAN:
			return Math.atan(x);
		case CBRT:
			return Math.cbrt(x);
		case CEIL:
			return Math.ceil(x);
		case COS:
			return Math.cos(x);
		case COSH:
			return Math.cosh(x);
		case EXP:
			return Math.exp(x);
		case EXPM1:
			return Math.expm1(x);
		case FLOOR:
			return Math.floor(x);
		case LOG:
			return Math.log(x);
		case SIN:
			return Math.sin(x);
		case SINH:
			return Math.sinh(x);
		case SQRT:
			return Math.sqrt(x);
		case TAN:
			return Math.tan(x);
		case TANH:
			return Math.tanh(x);
		default:
			return Double.NaN; // should not happen ;)
		}
	}

	/**
	 * get the {@link Function}
	 * 
	 * @return the correspoding {@link Function}
	 */
	Function getFunction() {
		return function;
	}

	@Override
	void mutateStackForCalculation(Stack<Double> stack, Map<String, Double> variableValues) {
		stack.push(this.applyFunction(stack.pop()));
	}

	@Override
	void mutateStackForInfixTranslation(Stack<Token> operatorStack, StringBuilder output) {
		operatorStack.push(this);
	}
}
