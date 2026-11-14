package info.openrocket.core.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

public class CSVExportTest extends BaseTestCase {
	@Test
	public void commentsKeepNumericDataAndEventIdentifiers() throws IOException {
		Simulation simulation = new Simulation(new Rocket());
		simulation.setName("\u4e2d\u6587\u4eff\u771f");
		FlightDataBranch branch = new FlightDataBranch("Flight", FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 0);
		branch.addPoint();
		branch.setValue(FlightDataType.TYPE_TIME, 0.5);
		branch.setValue(FlightDataType.TYPE_ALTITUDE, 12.5);
		branch.addEvent(new FlightEvent(FlightEvent.Type.LAUNCH, 0));

		String withComments = export(simulation, branch, true);
		String withoutComments = export(simulation, branch, false);

		assertTrue(withComments.contains("# \u4e2d\u6587\u4eff\u771f"), withComments);
		assertTrue(withComments.contains("# 2 data points written for 2 variables."));
		assertTrue(withComments.contains("# Event LAUNCH occurred at t=0 seconds"));
		assertEquals("0,0\n0.5,12.5\n", withoutComments.replace("\r\n", "\n"));
		assertEquals(withoutComments.lines().toList(),
				withComments.lines().filter(line -> !line.startsWith("#")).toList());
	}

	private String export(Simulation simulation, FlightDataBranch branch, boolean comments) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		CSVExport.exportCSV(stream, simulation, branch,
				new FlightDataType[] { FlightDataType.TYPE_TIME, FlightDataType.TYPE_ALTITUDE },
				new Unit[] { UnitGroup.UNITS_LONG_TIME.getUnit("s"), UnitGroup.UNITS_LENGTH.getUnit("m") },
				",", 3, false, "#", comments, false, comments);
		return stream.toString(StandardCharsets.UTF_8);
	}
}
