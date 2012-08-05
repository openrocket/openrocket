package net.sf.openrocket.simulation.customexpression;

import java.util.List;

import de.congrace.exp4j.Calculable;
import de.congrace.exp4j.Variable;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.LinearInterpolator;

public class IndexExpression extends CustomExpression {

	FlightDataType type;
	private static final LogHelper log = Application.getLogger();
	
	public IndexExpression(OpenRocketDocument doc, String indexText, String typeText){
		super(doc);
		
		setExpression(indexText);
		this.setName("");
		this.setSymbol(typeText);
		
	}
	
	@Override
	public Variable evaluate(SimulationStatus status){
		
		Calculable calc = buildExpression();
		if (calc == null){
			return new Variable("Unknown");
		}
		
		// From the given datatype, get the time and function values and make an interpolator
		FlightDataType type = getType();
		List<Double> data = status.getFlightData().get(type);
		List<Double> time = status.getFlightData().get(FlightDataType.TYPE_TIME);
		LinearInterpolator interp = new LinearInterpolator(time, data); 
		
		// Evaluate this expression to get the t value
		try{
			double tvalue = calc.calculate().getDoubleValue();
			return new Variable(hash(), interp.getValue( tvalue ) );
		}
		catch (java.util.EmptyStackException e){
			log.user("Unable to calculate time index for indexed expression "+getExpressionString()+" due to empty stack exception");
			return new Variable("Unknown");
		}
		
	}
}
