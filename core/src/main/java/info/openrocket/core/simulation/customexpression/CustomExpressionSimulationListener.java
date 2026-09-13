package info.openrocket.core.simulation.customexpression;

import java.util.List;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomExpressionSimulationListener extends AbstractSimulationListener {

	private static final Logger log = LoggerFactory.getLogger(CustomExpressionSimulationListener.class);
	private final List<CustomExpression> expressions;

	public CustomExpressionSimulationListener(List<CustomExpression> expressions) {
		super();
		this.expressions = expressions == null ? List.of() : List.copyOf(expressions);
	}

	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		if (expressions.isEmpty()) {
			return;
		}
		// Calculate values for custom expressions
		FlightDataBranch dataBranch = status.getFlightDataBranch();
		for (CustomExpression expression : expressions) {
			double value = expression.evaluateDouble(status);
			// log.debug("Setting value of custom expression "+expression.toString()+" =
			// "+value);
			dataBranch.setValue(expression.getType(), value);
		}
	}

	@Override
	public boolean isSystemListener() {
		return true;
	}

}
