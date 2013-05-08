package net.sf.openrocket.simulation.customexpression;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.unit.FixedUnitGroup;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.ExpressionBuilder;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparsableExpressionException;
import de.congrace.exp4j.Variable;

/**
 * Represents a single custom expression
 * @author Richard Graham
 *
 */
public class CustomExpression implements Cloneable {
	
	private static final Logger log = LoggerFactory.getLogger(CustomExpression.class);
	
	private OpenRocketDocument doc;
	private String name, symbol, unit;
	
	protected String expression;
	private ExpressionBuilder builder;
	private List<CustomExpression> subExpressions = new ArrayList<CustomExpression>();
	
	public CustomExpression(OpenRocketDocument doc) {
		this.doc = doc;
		
		setName("");
		setSymbol("");
		setUnit("");
		setExpression("");
	}
	
	public CustomExpression(OpenRocketDocument doc,
			String name,
			String symbol,
			String unit,
			String expression) {
		this.doc = doc;
		
		setName(name);
		setSymbol(symbol);
		setUnit(unit);
		setExpression(expression);
	}
	
	/*
	 * Sets the long name of this expression, e.g. 'Kinetic energy'  
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/*
	 * Sets the string for the units of the result of this expression.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/*
	 * Sets the symbol string. This is the short, locale independent symbol for this whole expression
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	
	/*
	 * Sets the actual expression string for this expression
	 */
	public void setExpression(String expression) {
		
		// This is the expression as supplied
		this.expression = expression;
		
		// Replace any indexed variables
		subExpressions.clear();
		expression = subTimeIndexes(expression);
		expression = subTimeRanges(expression);
		
		builder = new ExpressionBuilder(expression);
		for (String n : getAllSymbols()) {
			builder.withVariable(new Variable(n));
		}
		for (CustomExpression exp : this.subExpressions) {
			builder.withVariable(new Variable(exp.hash()));
		}
		
		builder.withCustomFunctions(Functions.getInstance().getAllFunction());
		log.info("Built expression " + expression);
	}
	
	/*
	 * Replaces expressions of the form:
	 *   a[x:y]  with a hash and creates an associated RangeExpression from x to y
	 */
	private String subTimeRanges(String str) {
		
		Pattern p = Pattern.compile(variableRegex() + "\\[[^\\]]*:.*?\\]");
		Matcher m = p.matcher(str);
		
		// for each match, make a new custom expression (in subExpressions) with a hashed name
		// and replace the expression and variable in the original expression string with [hash].
		while (m.find()) {
			String match = m.group();
			
			int start = match.indexOf("[");
			int end = match.indexOf("]");
			int colon = match.indexOf(":");
			
			String startTime = match.substring(start + 1, colon);
			String endTime = match.substring(colon + 1, end);
			String variableType = match.substring(0, start);
			
			RangeExpression exp = new RangeExpression(doc, startTime, endTime, variableType);
			subExpressions.add(exp);
			str = str.replace(match, exp.hash());
		}
		return str;
	}
	
	/*
	 * Replaces expressions of the form
	 *   a[x]    with a hash and creates an associated IndexExpression with x
	 */
	private String subTimeIndexes(String str) {
		
		// find any matches of the time-indexed variable notation, e.g. m[1.2] for mass at 1.2 sec
		Pattern p = Pattern.compile(variableRegex() + "\\[[^:]*?\\]");
		Matcher m = p.matcher(str);
		
		// for each match, make a new custom expression (in subExpressions) with a hashed name
		// and replace the expression and variable in the original expression string with [hash].
		while (m.find()) {
			String match = m.group();
			// just the index part (in the square brackets) :
			String indexText = match.substring(match.indexOf("[") + 1, match.length() - 1);
			// just the flight data type
			String typeText = match.substring(0, match.indexOf("["));
			
			// Do the replacement and add a corresponding new IndexExpression to the list
			IndexExpression exp = new IndexExpression(doc, indexText, typeText);
			subExpressions.add(exp);
			str = str.replace(match, exp.hash());
		}
		return str;
	}
	
	/*
	 * Returns a string of the form (t|a| ... ) with all variable symbols available
	 * This is useful for regex evaluation
	 */
	protected String variableRegex() {
		String regex = "(";
		for (String s : getAllSymbols()) {
			regex = regex + s + "|";
		}
		regex = regex.substring(0, regex.length() - 1) + ")";
		return regex;
	}
	
	// get a list of all the names of all the available variables
	protected ArrayList<String> getAllNames() {
		ArrayList<String> names = new ArrayList<String>();
		/*
		for (FlightDataType type : FlightDataType.ALL_TYPES)
			names.add(type.getName());

		if (doc != null){
			List<CustomExpression> expressions = doc.getCustomExpressions();
			for (CustomExpression exp : expressions ){
				if (exp != this)
					names.add(exp.getName());
			}
		}
		*/
		for (FlightDataType type : doc.getFlightDataTypes()) {
			String symb = type.getName();
			if (name == null)
				continue;
			
			if (!name.equals(this.getName())) {
				names.add(symb);
			}
		}
		return names;
	}
	
	// get a list of all the symbols of the available variables ignoring this one
	protected ArrayList<String> getAllSymbols() {
		ArrayList<String> symbols = new ArrayList<String>();
		/*
		for (FlightDataType type : FlightDataType.ALL_TYPES)
			symbols.add(type.getSymbol());
		
		if (doc != null){
			for (CustomExpression exp : doc.getCustomExpressions() ){
				if (exp != this)
					symbols.add(exp.getSymbol());
			}
		}
		*/
		for (FlightDataType type : doc.getFlightDataTypes()) {
			String symb = type.getSymbol();
			if (!symb.equals(this.getSymbol())) {
				symbols.add(symb);
			}
		}
		
		return symbols;
	}
	
	public boolean checkSymbol() {
		if (StringUtil.isEmpty(symbol)) {
			return false;
		}
		
		// No bad characters
		for (char c : "0123456789.,()[]{}<>:#@%^&*$ ".toCharArray())
			if (symbol.indexOf(c) != -1)
				return false;
		
		// No operators (ignoring brackets)
		for (String s : Functions.AVAILABLE_OPERATORS.keySet()) {
			if (symbol.equals(s.trim().replaceAll("\\(|\\)|\\]|\\[|:", "")))
				return false;
		}
		
		// No already defined symbols
		ArrayList<String> symbols = getAllSymbols().clone();
		if (symbols.contains(symbol.trim())) {
			int index = symbols.indexOf(symbol.trim());
			log.info(Markers.USER_MARKER, "Symbol " + symbol + " already exists, found " + symbols.get(index));
			return false;
		}
		
		return true;
	}
	
	public boolean checkName() {
		if (StringUtil.isEmpty(name)) {
			return false;
		}
		
		// No characters that could mess things up saving etc
		for (char c : ",()[]{}<>#$".toCharArray())
			if (name.indexOf(c) != -1)
				return false;
		
		ArrayList<String> names = getAllNames().clone();
		if (names.contains(name.trim())) {
			int index = names.indexOf(name.trim());
			log.info(Markers.USER_MARKER, "Name " + name + " already exists, found " + names.get(index));
			return false;
		}
		
		return true;
	}
	
	// Currently no restrictions on unit
	public boolean checkUnit() {
		return true;
	}
	
	public boolean checkAll() {
		return checkUnit() && checkSymbol() && checkName() && checkExpression();
	}
	
	public String getName() {
		return name;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public String getExpressionString() {
		return expression;
	}
	
	/**
	 * Performs a basic check to see if the current expression string is valid
	 * This includes checking for bad characters and balanced brackets and test
	 * building the expression.
	 */
	public boolean checkExpression() {
		if (StringUtil.isEmpty(expression)) {
			return false;
		}
		
		int round = 0, square = 0; // count of bracket openings
		for (char c : expression.toCharArray()) {
			switch (c) {
			case '(':
				round++;
				break;
			case ')':
				round--;
				break;
			case '[':
				square++;
				break;
			case ']':
				square--;
				break;
			case ':':
				if (square <= 0) {
					log.info(Markers.USER_MARKER, ": found outside range expression");
					return false;
				}
				else
					break;
			case '#':
				return false;
			case '$':
				return false;
			case '=':
				return false;
			}
		}
		if (round != 0 || square != 0) {
			log.info(Markers.USER_MARKER, "Expression has unballanced brackets");
			return false;
		}
		
		
		//// Define the available variables as empty
		// The built in data types
		/*
		for (FlightDataType type : FlightDataType.ALL_TYPES){
			builder.withVariable(new Variable(type.getSymbol()));
		}
		
		for (String symb : getAllSymbols()){
			builder.withVariable(new Variable(symb));
		}
		*/
		for (FlightDataType type : doc.getFlightDataTypes()) {
			builder.withVariable(new Variable(type.getSymbol()));
		}
		
		// Try to build
		try {
			builder.build();
		} catch (Exception e) {
			log.info(Markers.USER_MARKER, "Custom expression " + this.toString() + " invalid : " + e.toString());
			return false;
		}
		
		
		// Otherwise, all OK
		return true;
	}
	
	public Double evaluateDouble(SimulationStatus status) {
		double result = evaluate(status).getDoubleValue();
		if (result == Double.NEGATIVE_INFINITY || result == Double.POSITIVE_INFINITY)
			result = Double.NaN;
		return result;
	}
	
	/*
	 * Builds the expression, done automatically during evaluation. Logs any errors. Returns null in case of error.
	 */
	protected Calculable buildExpression() {
		return buildExpression(builder);
	}
	
	/*
	 * Builds a specified expression, log any errors and returns null in case of error.
	 */
	protected Calculable buildExpression(ExpressionBuilder b) {
		Calculable calc = null;
		try {
			calc = b.build();
		} catch (UnknownFunctionException e1) {
			log.info(Markers.USER_MARKER, "Unknown function. Could not build custom expression " + this.toString());
			return null;
		} catch (UnparsableExpressionException e1) {
			log.info(Markers.USER_MARKER, "Unparsable expression. Could not build custom expression " + this.toString() + ". " + e1.getMessage());
			return null;
		}
		
		return calc;
	}
	
	/*
	 * Evaluate the expression using the last variable values from the simulation status.
	 * Returns NaN on any error.
	 */
	public Variable evaluate(SimulationStatus status) {
		
		Calculable calc = buildExpression(builder);
		if (calc == null) {
			return new Variable("Unknown");
		}
		
		// Evaluate any sub expressions and set associated variables in the calculable
		for (CustomExpression expr : this.subExpressions) {
			calc.setVariable(expr.evaluate(status));
		}
		
		// Set all the built-in variables. Strictly we surely won't need all of them
		// Going through and checking them to include only the ones used *might* give a speedup
		for (FlightDataType type : status.getFlightData().getTypes()) {
			double value = status.getFlightData().getLast(type);
			calc.setVariable(new Variable(type.getSymbol(), value));
		}
		
		double result = Double.NaN;
		try {
			result = calc.calculate().getDoubleValue();
		} catch (java.util.EmptyStackException e) {
			log.info(Markers.USER_MARKER, "Unable to calculate expression " + this.expression + " due to empty stack exception");
		}
		
		return new Variable(name, result);
	}
	
	/*
	 * Returns the new flight data type corresponding to this calculated data
	 * If the unit matches a SI unit string then the datatype will have the corresponding unitgroup.
	 * Otherwise, a fixed unit group will be created
	 */
	public FlightDataType getType() {
		
		
		UnitGroup ug = UnitGroup.SIUNITS.get(unit);
		if (ug == null) {
			log.debug("SI unit not found for " + unit + " in expression " + toString() + ". Making a new fixed unit.");
			ug = new FixedUnitGroup(unit);
		}
		//UnitGroup ug = new FixedUnitGroup(unit);
		
		FlightDataType type = FlightDataType.getType(name, symbol, ug);
		
		//log.debug(this.getClass().getSimpleName()+" returned type "+type.getName()+" (" + type.getSymbol() + ")" );		
		
		return type;
	}
	
	/*
	 * Add this expression to the document if valid and not in document already
	 */
	public void addToDocument() {
		// Abort if exact expression already in
		List<CustomExpression> expressions = doc.getCustomExpressions();
		if (!expressions.isEmpty()) {
			// check if expression already exists
			if (expressions.contains(this)) {
				log.info(Markers.USER_MARKER, "Expression already in document. This unit : " + this.getUnit() + ", existing unit : " + expressions.get(0).getUnit());
				return;
			}
		}
		
		if (this.checkAll()) {
			log.info(Markers.USER_MARKER, "Custom expression added to rocket document");
			doc.addCustomExpression(this);
		}
	}
	
	/*
	 * Removes this expression from the document, replacing it with a given new expression
	 */
	public void overwrite(CustomExpression newExpression) {
		if (!doc.getCustomExpressions().contains(this))
			return;
		else {
			int index = doc.getCustomExpressions().indexOf(this);
			doc.getCustomExpressions().set(index, newExpression);
			log.debug("Overwriting custom expression already in document");
		}
	}
	
	@Override
	public String toString() {
		return "[Expression name=" + this.name.toString() + " expression=" + this.expression + " unit=" + this.unit + "]";
	}
	
	@Override
	/*
	 * Clone method makes a deep copy of everything except the reference to the document.
	 * If you want to apply this to another simulation, set simulation manually after cloning.
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e)
		{
			return new CustomExpression(doc,
					new String(this.getName()),
					new String(this.getSymbol()),
					new String(this.getUnit()),
					new String(this.getExpressionString()));
		}
	}
	
	/*
	 * Returns a simple all upper case string hash code with a proceeding $ mark.
	 * Used for temporary substitution when evaluating index and range expressions.
	 */
	public String hash() {
		Integer hashint = new Integer(this.getExpressionString().hashCode() + symbol.hashCode());
		String hash = "$";
		for (char c : hashint.toString().toCharArray()) {
			if (c == '-')
				c = '0';
			char newc = (char) (c + 17);
			hash = hash + newc;
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		CustomExpression other = (CustomExpression) obj;
		
		return (this.getName().equals(other.getName()) &&
				this.getSymbol().equals(other.getSymbol()) &&
				this.getExpressionString().equals(other.getExpressionString()) && this.getUnit().equals(other.getUnit()));
	}
	
	@Override
	public int hashCode() {
		return hash().hashCode();
	}
}
