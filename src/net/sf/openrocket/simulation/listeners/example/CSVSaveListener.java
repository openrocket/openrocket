package net.sf.openrocket.simulation.listeners.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;

import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;


public class CSVSaveListener extends AbstractSimulationListener {
	
	private static enum Types {
		TIME {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getSimulationTime();
			}
		},
		POSITION_X {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getRocketPosition().x;
			}
		},
		POSITION_Y {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getRocketPosition().y;
			}
		},
		ALTITUDE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getRocketPosition().z;
			}
		},
		VELOCITY_X {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getRocketVelocity().x;
			}
		},
		VELOCITY_Y {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getRocketVelocity().y;
			}
		},
		VELOCITY_Z {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getRocketVelocity().z;
			}
		},
		THETA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_ORIENTATION_THETA);
			}
		},
		PHI {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_ORIENTATION_PHI);
			}
		},
		AOA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_AOA);
			}
		},
		ROLLRATE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_ROLL_RATE);
			}
		},
		PITCHRATE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_PITCH_RATE);
			}
		},
		
		PITCHMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_PITCH_MOMENT_COEFF);
			}
		},
		YAWMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_YAW_MOMENT_COEFF);
			}
		},
		ROLLMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_ROLL_MOMENT_COEFF);
			}
		},
		NORMALFORCE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_NORMAL_FORCE_COEFF);
			}
		},
		SIDEFORCE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_SIDE_FORCE_COEFF);
			}
		},
		AXIALFORCE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_DRAG_FORCE);
			}
		},
		WINDSPEED {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_WIND_VELOCITY);
			}
		},
		PITCHDAMPING {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF);
			}
		},
		CA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_AXIAL_DRAG_COEFF);
			}
		},
		CD {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_DRAG_COEFF);
			}
		},
		CDpressure {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_PRESSURE_DRAG_COEFF);
			}
		},
		CDfriction {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_FRICTION_DRAG_COEFF);
			}
		},
		CDbase {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_BASE_DRAG_COEFF);
			}
		},
		MACH {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_MACH_NUMBER);
			}
		},
		RE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_REYNOLDS_NUMBER);
			}
		},
		
		CONTROL_ANGLE {
			@Override
			public double getValue(SimulationStatus status) {
				Iterator<RocketComponent> iterator =
						status.getConfiguration().getRocket().iterator();
				FinSet fin = null;
				
				while (iterator.hasNext()) {
					RocketComponent c = iterator.next();
					if (c instanceof FinSet && c.getName().equals("CONTROL")) {
						fin = (FinSet) c;
						break;
					}
				}
				if (fin == null)
					return 0;
				return fin.getCantAngle();
			}
		},
		
		MASS {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightData().getLast(FlightDataType.TYPE_MASS);
			}
		}

		;
		
		public abstract double getValue(SimulationStatus status);
	}
	
	
	public static final String FILENAME_FORMAT = "simulation-%03d.csv";
	
	private File file;
	private PrintStream output = null;
	
	
	@Override
	public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
		
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
			
			System.err.println("Opening file " + file + " for CSV output.");
			try {
				output = new PrintStream(file);
			} catch (FileNotFoundException e) {
				System.err.println("ERROR OPENING FILE: " + e);
			}
			
			final Types[] types = Types.values();
			StringBuilder s = new StringBuilder("# " + types[0].toString());
			for (int i = 1; i < types.length; i++) {
				s.append("," + types[i].toString());
			}
			output.println(s);
			
		} else if (event.getType() == FlightEvent.Type.SIMULATION_END && output != null) {
			
			System.err.println("Ending simulation logging to CSV file: " + file);
			output.close();
			output = null;
			
		} else if (event.getType() != FlightEvent.Type.ALTITUDE) {
			
			if (output != null) {
				output.println("# Event " + event);
			} else {
				System.err.println("WARNING: Event " + event + " encountered without open file");
			}
			
		}
		
		return true;
	}
	
	@Override
	public void postStep(SimulationStatus status) throws SimulationException {
		
		final Types[] types = Types.values();
		StringBuilder s;
		
		if (output != null) {
			
			s = new StringBuilder("" + types[0].getValue(status));
			for (int i = 1; i < types.length; i++) {
				s.append("," + types[i].getValue(status));
			}
			output.println(s);
			
		} else {
			
			System.err.println("WARNING: stepTaken called with no open file " +
					"(t=" + status.getSimulationTime() + ")");
		}
		
	}
}
