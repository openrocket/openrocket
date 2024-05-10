package info.openrocket.core.simulation.customexpression;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.Variable;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.simulation.customexpression.CustomExpression;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.util.LinearInterpolator;

public class IndexExpression extends CustomExpression {

	FlightDataType type;
	private static final Logger log = LoggerFactory.getLogger(IndexExpression.class);

	public IndexExpression(OpenRocketDocument doc, String indexText, String typeText) {
		super(doc);

		setExpression(indexText);
		this.setName("");
		this.setSymbol(typeText);
	}

	@Override
	public Variable evaluate(SimulationStatus status) {
		Calculable calc = buildExpression();
		if (calc == null) {
			return new Variable("Unknown");
		}

		// From the given datatype, get the time and function values and make an
		// interpolator

		// Note: must get in a way that flight data system will figure out units.
		// Otherwise there will be a type conflict when we get the new data.
		FlightDataType myType = FlightDataType.getType(null, getSymbol(), null);

		FlightDataBranch dataBranch = status.getFlightDataBranch();
		List<Double> data = dataBranch.get(myType);
		List<Double> time = dataBranch.get(FlightDataType.TYPE_TIME);
		LinearInterpolator interp = new LinearInterpolator(time, data);

		// Set the variables in the expression to evaluate
		for (FlightDataType etype : dataBranch.getTypes()) {
			double value = dataBranch.getLast(etype);
			calc.setVariable(new Variable(etype.getSymbol(), value));
		}

		// Evaluate this expression to get the t value
		// System.out.println("Evaluating expression to get t value
		// "+this.getExpressionString());
		try {
			double tvalue = calc.calculate().getDoubleValue();
			// System.out.println("t = "+tvalue);
			return new Variable(hash(), interp.getValue(tvalue));
		} catch (java.util.EmptyStackException e) {
			log.info(Markers.USER_MARKER, "Unable to calculate time index for indexed expression "
					+ getExpressionString() + " due to empty stack exception");
			return new Variable("Unknown");
		}
	}
}
