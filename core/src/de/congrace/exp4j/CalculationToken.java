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

import java.util.Arrays;
import java.util.Stack;

abstract class CalculationToken extends Token {

	CalculationToken(String value) {
		super(value);
	}

	abstract void mutateStackForCalculation(Stack<Variable> stack, VariableSet variables);

	/*
	 * Given an array of variables, check if any are arrays and if so expand any other of the given variables to arrays of the same length.
	 * Doubles are turned into arrays of all the same value as original. Arrays of other lengths are padded with zeros. 
	 */
	public Variable[] expandVariables(Variable[] values){
		// Check if any variables have preferred representation as arrays
		int maxLength = 0;
		for (Variable v : values){
			if (v.getPrimary() == Variable.Primary.ARRAY && v.getArrayValue().length > maxLength){
				maxLength = v.getArrayValue().length;
			}
		}
		
		// if necessary, expand any non-array variables to maximum length
		if (maxLength > 0) {
			for (int n = 0; n<values.length; n++){
				Variable v = values[n];
				if (v.getPrimary() == Variable.Primary.DOUBLE){
					double[] a = new double[maxLength];
					Arrays.fill(a, v.getDoubleValue());
					values[n] = new Variable(v.getName(), a);
				}
				else if (v.getPrimary() == Variable.Primary.ARRAY){
					// inlining Arrays.copyOf to provide compatibility with Froyo
					double[] a = new double[maxLength];
					int i = 0;
					double[] vArrayValues = v.getArrayValue();
					while( i < vArrayValues.length && i < maxLength ) {
						a[i] = vArrayValues[i];
						i++;
					}
					while ( i< maxLength ) {
						a[i] = 0.0;
						i++;
					}
					values[n] = new Variable(v.getName(), a);
				} 
				else {
					// Should not happen, if it does return invalid variable
					return new Variable[] { new Variable("Invalid")};
				}
			}
		}
		
		return values;
	}
}
