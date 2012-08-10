package de.congrace.exp4j;

import java.util.List;

public class Example {

	public static void main(String[] args) throws UnknownFunctionException, UnparsableExpressionException, InvalidCustomFunctionException {

		// Test 1
		// ======
		
		Calculable calc1 = new ExpressionBuilder("x * y - 2").withVariableNames("x", "y").build();
		calc1.setVariable(new Variable("x", 1.2));
		calc1.setVariable(new Variable("y", 2.2));
		
		System.out.println(calc1.calculate().toString());
		//double result = calc1.calculate().getDoubleValue();
		//System.out.println(result);
			
		// Test 2
		// ======
		
		// A function which calculates the mean of an array and scales it
		CustomFunction meanFn = new CustomFunction("mean",2) {
		    public Variable applyFunction(List<Variable> vars) {
		        
		    	double[] vals;
		    	double scale;
		    	
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    		scale = vars.get(1).getDoubleValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	
		    	double subtotal = 0;
		    	for (int i = 0; i < vals.length; i++ ){
		    		subtotal += vals[i];
		    	}
		    	
		    	subtotal = scale * subtotal / vals.length;
		        return new Variable("double MEAN result, ", subtotal);
		        
		    }
		};
				
		ExpressionBuilder b = new ExpressionBuilder("mean(x,y)");
		b.withCustomFunction(meanFn);
		b.withVariable(new Variable("x", new double[] {1.1,2,10,3,2.4,10.2}));
		b.withVariable(new Variable("y", 2));
		Calculable calc2 = b.build();
		
		System.out.println( calc2.calculate().toString() );
		
		// Test 3
		// ======
		
		Calculable calc3 = new ExpressionBuilder("x * y - 2").withVariableNames("x", "y").build();
		calc3.setVariable(new Variable("x", new double[]{1.2, 10, 20, 15}));
		calc3.setVariable(new Variable("y", new double[]{2.2, 5.2, 12, 9 }));
		
		//double result3 = calc3.calculate().getDoubleValue();
		System.out.println(calc3.calculate().toString());
		

		// Test 4
		// ======
				
		Calculable calc4 = new ExpressionBuilder("log10(sqrt(x) * abs(y))").withVariableNames("x", "y").build();
		calc4.setVariable(new Variable("x", new double[]{1.2, 10, 10, 15}));
		calc4.setVariable(new Variable("y", new double[]{2.2, -5.2, 5.2, 9 }));
		
		//double result3 = calc3.calculate().getDoubleValue();
		System.out.println(calc4.calculate().toString());		
	}
}
