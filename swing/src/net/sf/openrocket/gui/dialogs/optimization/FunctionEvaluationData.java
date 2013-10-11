package net.sf.openrocket.gui.dialogs.optimization;

import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.unit.Value;

/**
 * Value object for function evaluation information.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FunctionEvaluationData {
	
	private final Point point;
	private final Value[] state;
	private final Value domainReference;
	private final Value parameterValue;
	private final double goalValue;
	
	
	public FunctionEvaluationData(Point point, Value[] state, Value domainReference, Value parameterValue, double goalValue) {
		this.point = point;
		this.state = state.clone();
		this.domainReference = domainReference;
		this.parameterValue = parameterValue;
		this.goalValue = goalValue;
	}
	
	
	/**
	 * Return the function evaluation point (in 0...1 range).
	 */
	public Point getPoint() {
		return point;
	}
	
	
	/**
	 * Return the function evaluation state in SI units + units.
	 */
	public Value[] getState() {
		return state;
	}
	
	
	/**
	 * Return the domain description.
	 */
	public Value getDomainReference() {
		return domainReference;
	}
	
	
	/**
	 * Return the optimization parameter value (or NaN is outside of domain).
	 */
	public Value getParameterValue() {
		return parameterValue;
	}
	
	
	/**
	 * Return the function goal value.
	 */
	public double getGoalValue() {
		return goalValue;
	}
}
