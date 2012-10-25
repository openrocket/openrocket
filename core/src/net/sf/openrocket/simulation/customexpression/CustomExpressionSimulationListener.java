package net.sf.openrocket.simulation.customexpression;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;
import net.sf.openrocket.startup.Application;

public class CustomExpressionSimulationListener extends	AbstractSimulationListener {

	private static final Logger log = LoggerFactory.getLogger(CustomExpressionSimulationListener.class);
	private final List<CustomExpression> expressions;
	
	public CustomExpressionSimulationListener(List<CustomExpression> expressions) {
		super();
		this.expressions = expressions;
	}

	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		if ( expressions == null || expressions.size() == 0 ) {
			return;
		}
		// Calculate values for custom expressions
		FlightDataBranch data = status.getFlightData();
		for (CustomExpression expression : expressions ) {
			double value = expression.evaluateDouble(status);
			//log.debug("Setting value of custom expression "+expression.toString()+" = "+value);
			data.setValue(expression.getType(), value);
		}
		

	}

	@Override
	public boolean isSystemListener(){
		return true;
	}
	
}
