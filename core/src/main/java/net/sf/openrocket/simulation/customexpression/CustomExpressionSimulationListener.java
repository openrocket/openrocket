package net.sf.openrocket.simulation.customexpression;

import java.util.List;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExpressionSimulationListener extends AbstractSimulationListener {
	
	private static final Logger log = LoggerFactory.getLogger(CustomExpressionSimulationListener.class);
	private final List<CustomExpression> expressions;
	
	public CustomExpressionSimulationListener(List<CustomExpression> expressions) {
		super();
		this.expressions = expressions;
	}
	
	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		if (expressions == null || expressions.size() == 0) {
			return;
		}
		// Calculate values for custom expressions
		FlightDataBranch data = status.getFlightData();
		for (CustomExpression expression : expressions) {
			double value = expression.evaluateDouble(status);
			//log.debug("Setting value of custom expression "+expression.toString()+" = "+value);
			data.setValue(expression.getType(), value);
		}
	}
	
	@Override
	public boolean isSystemListener() {
		return true;
	}
	
}
