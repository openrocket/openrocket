package de.congrace.exp4j;

/*
 * Represents a generic variable which can have double or array values.
 * Optionally the start and step values corresponding to each array index can be specified for array values
 * Tries to do something sensible if you try and apply a regular function / operator to an array
 * and vice-versa.
 */
public class Variable {
	
	// The primary or preferred representation 
	public enum Primary {DOUBLE, ARRAY, PLACEHOLDER};
	private final Primary primary;
	
	private final String name;
	
	private final double doubleValue;  
	private final double[] arrayValue; 
	
	private final double start, step;
	
	/*
	 * Initialize a new variable with a name only. This can be used as a place holder
	 */
	public Variable(String name){
		this.name = name;
		this.primary = Primary.PLACEHOLDER;
		this.doubleValue = Double.NaN;
		this.arrayValue = new double[] {Double.NaN};
		this.start = Double.NaN;
		this.step = Double.NaN;
	}
	
	/*
	 * Initialize a new double variable
	 */
	public Variable(String name, double d){
		this.doubleValue = d;
		this.arrayValue = new double[] {d};
		this.name = name;
		this.primary = Primary.DOUBLE;
		this.start = Double.NaN;
		this.step = Double.NaN;
	}
	
	/*
	 * Initialize a new array variable
	 */
	public Variable(String name, double[] d){
		this.arrayValue = d;
		this.doubleValue = d[0];
		this.name = name;
		this.primary = Primary.ARRAY;
		this.start = Double.NaN;
		this.step = Double.NaN;
	}
	
	/*
	 * Initialize a new array variable, specifying the start and step values
	 */
	public Variable(String name, double[] d, double start, double step){
		this.arrayValue = d;
		this.doubleValue = d[0];
		this.name = name;
		this.primary = Primary.ARRAY;
		this.start = start;
		this.step = step;
	}
	
	public String getName(){
		return name;
	}
	
	public Primary getPrimary(){
		return this.primary;
	}
	
	public double getDoubleValue(){
		return doubleValue;
	}
	
	public double[] getArrayValue(){
		return arrayValue;
	}
	
	public double getStep(){
		return step;
	}
	
	public double getStart(){
		return start;
	}
	
	public String toString(){
		if ( arrayValue.length > 1 ){
			String out = name + " is Array (length " + new Integer(arrayValue.length).toString() + ") : {";
			for (double x : arrayValue){
				out = out + x + ",";
			}
			out = out.substring(0, out.length()-1);
			return out + "}";
		}
		else{
			return name + " is double : {" + new Double(doubleValue).toString() + "}";
		}
	}
}