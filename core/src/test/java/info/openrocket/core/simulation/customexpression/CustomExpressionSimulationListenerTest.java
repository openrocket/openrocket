package info.openrocket.core.simulation.customexpression;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;

class CustomExpressionSimulationListenerTest extends BaseTestCase {

	@Test
	void snapshotsExpressionsAtConstruction() throws SimulationException {
		CustomExpression originalExpression = mock(CustomExpression.class);
		CustomExpression laterExpression = mock(CustomExpression.class);
		List<CustomExpression> expressions = new ArrayList<>();
		expressions.add(originalExpression);
		CustomExpressionSimulationListener listener = new CustomExpressionSimulationListener(expressions);

		expressions.clear();
		expressions.add(laterExpression);

		SimulationStatus status = mock(SimulationStatus.class);
		FlightDataBranch dataBranch = mock(FlightDataBranch.class);
		when(status.getFlightDataBranch()).thenReturn(dataBranch);
		when(originalExpression.evaluateDouble(status)).thenReturn(12.5);
		when(originalExpression.getType()).thenReturn(FlightDataType.TYPE_TIME);

		listener.postStep(status);

		verify(originalExpression).evaluateDouble(status);
		verify(dataBranch).setValue(FlightDataType.TYPE_TIME, 12.5);
		verifyNoInteractions(laterExpression);
	}
}
