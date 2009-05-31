package net.sf.openrocket.simulation.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;

import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;


public class RollSaveListener extends AbstractSimulationListener {

	private static enum Types {
		TIME {
			@Override
			public double getValue(SimulationStatus status) {
				return status.time;
			}
		},
		ALTITUDE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.position.z;
			}
		},
		VELOCITY_Z {
			@Override
			public double getValue(SimulationStatus status) {
				return status.velocity.z;
			}
		},
		THETA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.flightData.getLast(FlightDataBranch.TYPE_ORIENTATION_THETA);
			}
		},
		AOA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.flightData.getLast(FlightDataBranch.TYPE_AOA);
			}
		},
		ROLLRATE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.flightData.getLast(FlightDataBranch.TYPE_ROLL_RATE);
			}
		},
		PITCHRATE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.flightData.getLast(FlightDataBranch.TYPE_PITCH_RATE);
			}
		},
		ROLLMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.flightData.getLast(FlightDataBranch.TYPE_ROLL_MOMENT_COEFF);
			}
		},
		MACH {
			@Override
			public double getValue(SimulationStatus status) {
				return status.flightData.getLast(FlightDataBranch.TYPE_MACH_NUMBER);
			}
		},

		;
		
		public abstract double getValue(SimulationStatus status);
	}
	
	
	public static final String FILENAME_FORMAT = "simulation-%03d.csv";
	
	private File file;
	private PrintStream output = null;
	
		
	
	@Override
	public Collection<FlightEvent> handleEvent(FlightEvent event,
			SimulationStatus status) {

		if (event.getType() == FlightEvent.Type.LAUNCH) {
			int n = 1;

			if (output != null) {
				System.err.println("WARNING: Ending simulation logging to CSV file " +
						"(SIMULATION_END not encountered).");
				output.close();
				output = null;
			}
			
			do {
				file = new File(String.format(FILENAME_FORMAT, n));
				n++;
			} while (file.exists());
			
			System.err.println("Opening file "+file+" for CSV output.");
			try {
				output = new PrintStream(file);
			} catch (FileNotFoundException e) {
				System.err.println("ERROR OPENING FILE: "+e);
			}
			
			final Types[] types = Types.values();
			StringBuilder s = new StringBuilder("# " + types[0].toString());
			for (int i=1; i<types.length; i++) {
				s.append("," + types[i].toString());
			}
			output.println(s);
			
		} else if (event.getType() == FlightEvent.Type.SIMULATION_END && output != null) {
			
			System.err.println("Ending simulation logging to CSV file: "+file);
			output.close();
			output = null;
			
		} else if (event.getType() != FlightEvent.Type.ALTITUDE){
			
			if (output != null) {
				output.println("# Event "+event);
			} else {
				System.err.println("WARNING: Event "+event+" encountered without open file");
			}
			
		}
		
		return null;
	}

	
	@Override
	public Collection<FlightEvent> stepTaken(SimulationStatus status) {

		final Types[] types = Types.values();
		StringBuilder s;
		
		if (output != null) {
			
			s = new StringBuilder("" + types[0].getValue(status));
			for (int i=1; i<types.length; i++) {
				s.append("," + types[i].getValue(status));
			}
			output.println(s);
		
		} else {
			
			System.err.println("WARNING: stepTaken called with no open file " +
					"(t="+status.time+")");
		}

		return null;
	}
}
