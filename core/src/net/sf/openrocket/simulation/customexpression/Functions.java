package net.sf.openrocket.simulation.customexpression;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayUtils;

import de.congrace.exp4j.CustomFunction;
import de.congrace.exp4j.InvalidCustomFunctionException;
import de.congrace.exp4j.Variable;

/*
 * This is a singleton class which contains all the functions for custom expressions not provided by exp4j
 */
public class Functions {
	private static Functions instance = null;
	
	private static final Logger log = LoggerFactory.getLogger(Functions.class);
	private static final Translator trans = Application.getTranslator();
	
	private List<CustomFunction> allFunctions = new ArrayList<CustomFunction>();

	public static Functions getInstance() {
		if(instance == null) {
			try {
				instance = new Functions();
			} catch (InvalidCustomFunctionException e) {
				log.error("Invalid custom function.");
			}
		}
		return instance;
	}
	
	public List<CustomFunction> getAllFunction(){
		return allFunctions;
	}
	
	// A map of available operator strings (keys) and description of function (value)
	public static final SortedMap<String, String> AVAILABLE_OPERATORS = new TreeMap<String, String>() {{
	    put("+"       	, trans.get("Operator.plus"));
	    put("-"			, trans.get("Operator.minus"));
	    put("*"			, trans.get("Operator.star"));
	    put("/"			, trans.get("Operator.div"));
	    put("%"			, trans.get("Operator.mod"));
	    put("^"			, trans.get("Operator.pow"));
	    put("abs()"		, trans.get("Operator.abs"));
	    put("ceil()"	, trans.get("Operator.ceil"));
	    put("floor()"	, trans.get("Operator.floor"));
	    put("sqrt()"	, trans.get("Operator.sqrt"));
	    put("cbrt()"	, trans.get("Operator.cbrt"));
	    put("exp()"		, trans.get("Operator.exp"));
	    put("log()"		, trans.get("Operator.ln"));
	    put("sin()"		, trans.get("Operator.sin"));
	    put("cos()"		, trans.get("Operator.cos"));
	    put("tan()"		, trans.get("Operator.tan"));
	    put("asin()"	, trans.get("Operator.asin"));
	    put("acos()"	, trans.get("Operator.acos"));
	    put("atan()"	, trans.get("Operator.atan"));
	    put("sinh()"	, trans.get("Operator.hsin"));
	    put("cosh()"	, trans.get("Operator.hcos"));
	    put("tanh()"	, trans.get("Operator.htan"));
	    put("log10()"	, trans.get("Operator.log10"));
	    put("round()"	, trans.get("Operator.round"));
	    put("random()"	, trans.get("Operator.random"));
	    put("expm1()"	, trans.get("Operator.expm1"));
	    put("mean([:])"	, trans.get("Operator.mean"));
	    put("min([:])"	, trans.get("Operator.min"));
	    put("max([:])"	, trans.get("Operator.max"));
	    put("var([:])"	, trans.get("Operator.var"));
	    put("rms([:])"	, trans.get("Operator.rms"));
	    put("stdev([:])", trans.get("Operator.stdev"));
	    put("lclip(,)"  , trans.get("Operator.lclip"));
	    put("uclip(,)"  , trans.get("Operator.uclip"));
	    put("binf([:],,)"	, trans.get("Operator.binf"));
	    put("trapz([:])"	, trans.get("Operator.trapz"));
	    put("tnear([:],)"	, trans.get("Operator.tnear"));
	}}; 
	
	
	protected Functions() throws InvalidCustomFunctionException {
		
		CustomFunction meanFn = new CustomFunction("mean") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double[] vals;
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		        return new Variable("double MEAN result, ", ArrayUtils.mean(vals));
			}	
		};
		allFunctions.add(meanFn);
		
		CustomFunction minFn = new CustomFunction("min") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double[] vals;
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	return new Variable("double MIN result, ", ArrayUtils.min(vals));
			}
		};
		allFunctions.add(minFn);
		
		CustomFunction maxFn = new CustomFunction("max") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double[] vals;
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	return new Variable("double MAX result, ", ArrayUtils.max(vals));
			}
		};
		allFunctions.add(maxFn);
	
		CustomFunction varFn = new CustomFunction("var") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double[] vals;
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	return new Variable("double VAR result, ", ArrayUtils.variance(vals));
			}
		};
		allFunctions.add(varFn);
		
		CustomFunction stdevFn = new CustomFunction("stdev") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double[] vals;
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	return new Variable("double STDEV result, ", ArrayUtils.stdev(vals));
			}
		};
		allFunctions.add(stdevFn);
		
		CustomFunction rmsFn = new CustomFunction("rms") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double[] vals;
		    	try{
		    		vals = vars.get(0).getArrayValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	return new Variable("double RMS result, ", ArrayUtils.rms(vals));
			}
		};
		allFunctions.add(rmsFn);
		
		CustomFunction lclipFn = new CustomFunction("lclip",2) {
			@Override
			public Variable applyFunction(List<Variable> vars) {
		    	double val, clip;
		    	try{
		    		val = vars.get(0).getDoubleValue();
		    		clip = vars.get(1).getDoubleValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	if (val < clip){
		    		val = clip;
		    	}
		    	return new Variable("double LCLIP result, ", val);
			}
		};
		allFunctions.add(lclipFn);
		
		CustomFunction uclipFn = new CustomFunction("uclip",2) {
			@Override
			public Variable applyFunction(List<Variable> vars) {
				double val, clip;
		    	try{
		    		val = vars.get(0).getDoubleValue();
		    		clip = vars.get(1).getDoubleValue();
		    	} catch (Exception e) {
		    		return new Variable("Invalid");
		    	}
		    	if (val > clip){
		    		val = clip;
		    	}
		    	return new Variable("double UCLIP result, ", val);
			}
		};
		allFunctions.add(uclipFn);
		
		CustomFunction binfFn = new CustomFunction("binf", 3) {
			@Override
			public Variable applyFunction(List<Variable> vars) {
				double[] range;
				double min, max;
				try{
					range = vars.get(0).getArrayValue();
					min = vars.get(1).getDoubleValue();
					max = vars.get(2).getDoubleValue();
				} catch (Exception e) {
					return new Variable("Invalid");
				}
				
				int ins = 0;
				for (double x: range){
					if (x < max && x > min){
						ins++;
					}
				}
				return new Variable("double BINF result", (double) ins/ (double) range.length);
			}
		};
		allFunctions.add(binfFn);
		
		CustomFunction rombintFn = new CustomFunction("trapz") {
			@Override
			public Variable applyFunction(List<Variable> vars) {
				double[] range;
				double dt = 0;
				try{
					range = vars.get(0).getArrayValue();
					dt = vars.get(0).getStep();
				} catch (Exception e) {
					return new Variable("Invalid");
				}
				
				return new Variable("double TRAPZ result", ArrayUtils.trapz(range, dt) );
			}
		};
		allFunctions.add(rombintFn);
		
		CustomFunction tnearFn = new CustomFunction("tnear", 2) {
			@Override
			public Variable applyFunction(List<Variable> vars) {
				double[] range;
				double dt = 0;
				double start = 0;
				double near = 0;
				try{
					range = vars.get(0).getArrayValue();
					dt = vars.get(0).getStep();
					start = vars.get(0).getStart();
					near = vars.get(1).getDoubleValue();
				} catch (Exception e) {
					return new Variable("Invalid");
				}
				
				return new Variable("double TNEAR result", ArrayUtils.tnear(range, near, start, dt) );
			} 
		};
		allFunctions.add(tnearFn);
	}
}
