package net.sf.openrocket.simulation;

import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.FixedUnitGroup;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;


/**
 * Represents a single custom expression
 * @author Richard Graham
 *
 */
public class CustomExpression implements Cloneable{
	
	private static final LogHelper log = Application.getLogger();
	
	private String name, symbol, unit, expression;
	private ExpressionBuilder builder;
	private Simulation sim = null;
	
	// A map of available operator strings (keys) and description of function (value)
	public static final SortedMap<String, String> AVAILABLE_OPERATORS = new TreeMap<String, String>() {{
	    put("+"       	, "Addition");
	    put("-"			, "Subtraction");
	    put("*"			, "Multiplication");
	    put("/"			, "Divison");
	    put("%"			, "Modulo");
	    put("^"			, "Exponentiation");
	    put("abs()"		, "Absolute value");
	    put("ceil()"	, "Ceiling (next integer value");
	    put("floor()"	, "Floor (previous integer value");
	    put("sqrt()"	, "Square root");
	    put("cbrt()"	, "Cubic root");
	    put("exp()"		, "Euler\'s number raised to the value (e^x)");
	    put("log()"		, "Natural logarithm");
	    put("sin()"		, "Sine");
	    put("cos()"		, "Cosine");
	    put("tan()"		, "Tangent");
	    put("asin()"	, "Arc sine");
	    put("acos()"	, "Arc cosine");
	    put("atan()"	, "Arc tangent");
	    put("sinh()"	, "Hyerbolic sine");
	    put("cosh()"	, "Hyperbolic cosine");
	    put("tanh()"	, "Hyperbolic tangent");
	}};  
	
	
	public CustomExpression(){
		setName("");
		setSymbol("");
		setUnit("");
		setExpression("");
	}
	
	public CustomExpression(Simulation sim){
		this();
		setSimulation(sim);
	}
	
	public CustomExpression(Simulation sim, String name, String symbol, String unit, String expression) {
		
		setName(name);
		setSymbol(symbol);
		setUnit(unit);
		setExpression(expression);
		setSimulation(sim);
	}
	
	/*
	 * Use this to update the simulation this is associated with
	 */
	public void setSimulation(Simulation sim){
		this.sim = sim;
	}
	
	public Simulation getSimulation() {
		return this.sim;
	}
	
	/*
	 * Returns the flight data branch 0 for this simulation, or an empty branch
	 * if no simulated data exists
	 */
	private FlightDataBranch getBranch() {
		if ( 	sim == null || sim.getSimulatedData() == null || sim.getSimulatedData().getBranchCount() == 0){
			return new FlightDataBranch();
		}
		else {
			System.out.println("Using existing branch");
			return sim.getSimulatedData().getBranch(0);
		}
	}
	
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setUnit(String unit){
		this.unit = unit;
	}
	
	public void setSymbol(String symbol){
		this.symbol = symbol;
	}
	
	public void setExpression(String expression){
		this.expression = expression;
		builder = new ExpressionBuilder(expression);
	}
	
	// get a list of all the names of all the available variables
	private ArrayList<String> getAllNames(){
		ArrayList<String> names = new ArrayList<String>();
		for (FlightDataType type : FlightDataType.ALL_TYPES)
			names.add(type.getName());
		for (CustomExpression exp : sim.getCustomExpressions() ){
			if (exp != this)
				names.add(exp.getName());
		}
		return names;
	}
	
	// get a list of all the symbols of the available variables ignoring this one
	private ArrayList<String> getAllSymbols(){
		ArrayList<String> symbols = new ArrayList<String>();
		for (FlightDataType type : FlightDataType.ALL_TYPES)
			symbols.add(type.getSymbol());
		for (CustomExpression exp : sim.getCustomExpressions() ){
			if (exp != this)
				symbols.add(exp.getSymbol());
		}
		return symbols;
	}
	
	public boolean checkSymbol(){
		if (symbol.trim().isEmpty())
			return false;
		
		// No bad characters
		for (char c : "0123456789.,()[]{}<> ".toCharArray())
			if (symbol.indexOf(c) != -1 )
				return false;
		
		// No operators (ignoring brackets)
		for (String s : CustomExpression.AVAILABLE_OPERATORS.keySet()){
			if (symbol.contains(s.replaceAll("\\(|\\)", "")))
				return false;
		}
		
		// No already defined symbols
		ArrayList<String> symbols = getAllSymbols().clone();
		if (symbols.contains(symbol.trim())){
			int index = symbols.indexOf(symbol.trim());
			log.user("Symbol "+symbol+" already exists, found "+symbols.get(index));
			return false;
		}
		
		return true;
	}
	
	public boolean checkName(){
		if (name.trim().isEmpty())
			return false;
		
		// No characters that could mess things up saving etc
		for (char c : ",()[]{}<>".toCharArray())
			if (name.indexOf(c) != -1 )
				return false;
		
		ArrayList<String> names = getAllNames().clone();
		if (names.contains(name.trim())){
			int index = names.indexOf(name.trim());
			log.user("Symbol "+symbol+" already exists, found "+names.get(index));
			return false;
		}
		
		return true;
	}
	
	// Currently no restrictions on unit
	public boolean checkUnit(){
		return true;
	}
	
	public boolean checkAll(){
		return checkUnit() && checkSymbol() && checkName();
	}
	
	public String getName(){
		return name;
	}
	
	public String getSymbol(){
		return symbol;
	}
	
	public String getUnit(){
		return unit;
	}
	
	public String getExpressionString(){
		return expression;
	}
	
	
	/*
	 * Check if the current expression is valid
	 */
	public boolean checkExpression(){
		
		if (expression.trim().isEmpty()){
			return false;
		}
		
		// Define the available variables as 0
		for (FlightDataType type : getBranch().getTypes()){
			builder.withVariable(type.getSymbol(), 0.0);
		}
		
		for (String symb : getAllSymbols()){
			builder.withVariable(symb, 0.0);
		}
		
		// Try to build
		try {
			builder.build();
		} catch (Exception e) {
			log.user("Custom expression invalid : " + e.toString());
			return false;
		}
		
		// Otherwise, all OK
		return true;
	}
	
	/*
	 * Evaluate the expression using the last variable values from the simulation status.
	 * Returns NaN on any error.
	 */
	public Double evaluate(SimulationStatus status){
		
		for (FlightDataType type : status.getFlightData().getTypes()){
			builder.withVariable(type.getSymbol(), status.getFlightData().getLast(type) );
		}
		
		Calculable calc;
		try {
			calc = builder.build();
			return new Double(calc.calculate());
		} catch (Exception e) {
			log.user("Could not calculate custom expression "+name);
			return Double.NaN;
		}
	}

	/*
	 * Returns the new flight data type corresponding to this calculated data
	 */
	public FlightDataType getType(){
		
		UnitGroup ug = new FixedUnitGroup(unit);
		FlightDataType type =  FlightDataType.getType(name, symbol, ug);
		
		// If in a simulation, figure out priority from order in array so that customs expressions are always at the top
		if (sim != null && sim.getCustomExpressions().contains(this)){
			int totalExpressions = sim.getCustomExpressions().size();
			int p = -1*(totalExpressions-sim.getCustomExpressions().indexOf(this));
			type.setPriority(p);
		}
		
		return type;
	}
	
	/*
	 * Add this expression to the simulation if not already added
	 */
	public void addToSimulation(){
		if (! sim.getCustomExpressions().contains(this))
			sim.addCustomExpression( this );
	}
	
	/*
	 * Removes this expression from the simulation, replacing it with a given new expression
	 */
	public void overwrite(CustomExpression newExpression){
		if (!sim.getCustomExpressions().contains(this)) 
			return;
		else {
			int index = sim.getCustomExpressions().indexOf(this);
			sim.getCustomExpressions().set(index, newExpression);
		}
	}
	
	@Override
	public String toString(){
		return "Custom expression : "+this.name.toString()+ " " + this.expression.toString();
	}
	
	@Override
	/*
	 * Clone method makes a deep copy of everything except the simulation
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
	      try {
	          return super.clone();
	      }
	      catch( CloneNotSupportedException e )
	      {
	    	  return new CustomExpression(	sim  , 
	      			new String(this.getName()), 
	    			new String(this.getSymbol()),
	    			new String(this.getUnit()),
	    			new String(this.getExpressionString()));
	      }
	  } 
	
}
