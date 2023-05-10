package de.congrace.exp4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import de.congrace.exp4j.FunctionToken.Function;

/**
 * this classed is used to create custom functions for exp4j<br/>
 * <br/>
 * <b>Example</b><br/>
 * <code><pre>{@code 
 * CustomFunction fooFunc = new CustomFunction("foo") {
 * 		public double applyFunction(double value) {
 * 			return value*Math.E;
 * 		}
 * };
 * double varX=12d;
 * Calculable calc = new ExpressionBuilder("foo(x)").withCustomFunction(fooFunc).withVariable("x",varX).build();
 * assertTrue(calc.calculate() == Math.E * varX);
 * }</pre></code>
 * 
 * @author ruckus
 * 
 */
public abstract class CustomFunction extends CalculationToken {
    private int argc=1;

	/**
	 * create a new single value input CustomFunction with a set name
	 * 
	 * @param value
	 *            the name of the function (e.g. foo)
	 */
	protected CustomFunction(String value) throws InvalidCustomFunctionException{
		super(value);
		for (Function f:Function.values()) {
			if (value.equalsIgnoreCase(f.toString())){
				throw new InvalidCustomFunctionException(value + " is already reserved as a function name");
			}
		}
	}

    /**
     * create a new single value input CustomFunction with a set name
     * 
     * @param value
     *            the name of the function (e.g. foo)
     */
    protected CustomFunction(String value,int argumentCount) throws InvalidCustomFunctionException{
        super(value);
        this.argc=argumentCount;
        for (Function f:Function.values()) {
            if (value.equalsIgnoreCase(f.toString())){
                throw new InvalidCustomFunctionException(value + " is already reserved as a function name");
            }
        }
    }

    /**
	 * apply the function to a value
	 * 
	 * @param values
	 *            the values to which the function should be applied.
	 * @return the function value
	 */
	public abstract Variable applyFunction(List<Variable> vars);

    @Override
	void mutateStackForCalculation(Stack<Variable> stack, VariableSet variables) {
	    List<Variable> args = new ArrayList<Variable>(argc);
	    for (int i=0; i < argc; i++) {
	    	args.add(i, stack.pop() );
	    }
	    Collections.reverse(args); // Put elements in logical order
	    
		stack.push(this.applyFunction(args));
	}

	@Override
	void mutateStackForInfixTranslation(Stack<Token> operatorStack, StringBuilder output) {
		operatorStack.push(this);
	}
	public int getArgumentCount() {
	    return argc;
	}
}
