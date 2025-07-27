package info.openrocket.core.simulation.extension.example;

import java.io.File;
import java.io.PrintStream;
import java.lang.Exception;
import java.util.Iterator;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
import info.openrocket.core.simulation.listeners.AbstractSimulationListener;

public class CSVSave extends AbstractSimulationExtension {
	private static final Logger log = LoggerFactory.getLogger(CSVSave.class);

	// This listener has no configuration. The flight data variables to be logged
	// must be set here
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
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ORIENTATION_THETA);
			}
		},
		PHI {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ORIENTATION_PHI);
			}
		},
		AOA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_AOA);
			}
		},
		ROLLRATE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ROLL_RATE);
			}
		},
		PITCHRATE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_PITCH_RATE);
			}
		},

		PITCHMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_PITCH_MOMENT_COEFF);
			}
		},
		YAWMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_YAW_MOMENT_COEFF);
			}
		},
		ROLLMOMENT {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_ROLL_MOMENT_COEFF);
			}
		},
		NORMALFORCE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_NORMAL_FORCE_COEFF);
			}
		},
		SIDEFORCE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_SIDE_FORCE_COEFF);
			}
		},
		AXIALFORCE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_DRAG_FORCE);
			}
		},
		WINDSPEED {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_WIND_VELOCITY);
			}
		},
		WINDDIRECTION {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_WIND_DIRECTION);
			}
		},
		PITCHDAMPING {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_PITCH_DAMPING_MOMENT_COEFF);
			}
		},
		CA {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_AXIAL_DRAG_COEFF);
			}
		},
		CD {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_DRAG_COEFF);
			}
		},
		CDpressure {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_PRESSURE_DRAG_COEFF);
			}
		},
		CDfriction {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_FRICTION_DRAG_COEFF);
			}
		},
		CDbase {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_BASE_DRAG_COEFF);
			}
		},
		MACH {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_MACH_NUMBER);
			}
		},
		RE {
			@Override
			public double getValue(SimulationStatus status) {
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_REYNOLDS_NUMBER);
			}
		},

		CONTROL_ANGLE {
			@Override
			public double getValue(SimulationStatus status) {
				Iterator<RocketComponent> iterator = status.getConfiguration().getRocket().iterator();
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
				return status.getFlightDataBranch().getLast(FlightDataType.TYPE_MASS);
			}
		}

		;

		public abstract double getValue(SimulationStatus status);
	}

	// format of CSV file name
	public static final String FILENAME_FORMAT = "simulation-%03d.csv";

	private PrintStream output = null;

	// Description shown when blue "i" button is clicked
	@Override
	public String getDescription() {
		return "dump a CSV file with a predetermined set of flight variables to a CSV file while running.  Note: this is an example of reading simulation variables from an extension, not a practical way of exporting them. If your actual goal is to dump the simulation variables, use the Export Data tab in the Edit simulation dialog ";
	}

	// 
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(new CSVSaveListener());
	}

	// The actual listener that does all the work
	private class CSVSaveListener extends AbstractSimulationListener {

		// when we start the simulation we'll create the .csv file and write the column headings
		@Override
		public void startSimulation(SimulationStatus status) throws SimulationException {
			// Just in case a prior run somehow failed to close the file.
			if (output != null) {
				log.warn("WARNING: Ending simulation logging to CSV file " +
						 "(SIMULATION_END not encountered).");
				output.close();
				output = null;
			}
			
			File file = null;
			try {
				// Construct filename, atomically create and open file
				int n = 1;
				do {
					file = new File(String.format(FILENAME_FORMAT, n));
					n++;
				} while (!file.createNewFile());
				log.info("CSV file name is " + file.getName());
					
				output = new PrintStream(file);
				
				// Write column headers
				final Types[] types = Types.values();
				StringBuilder s = new StringBuilder("# " + types[0].toString());
				for (int i = 1; i < types.length; i++) {
					s.append("," + types[i].toString());
				}

				output.println(s);
				output.flush();
				
			} catch (Exception e) {
				log.error("ERROR OPENING FILE: " + e);
				JOptionPane.showMessageDialog(null,
											  "Error Opening File:\n" + e.getMessage(),
											  "Error Opening File: " + file,
											  JOptionPane.ERROR_MESSAGE);
			}
			
		}

		// Log an event
		@Override
		public boolean handleFlightEvent(SimulationStatus status, FlightEvent event) throws SimulationException {
			
			// ALTITUDE events are special in the sense that they are just used to schedule simulation steps.
			// We want to hear about all the others
			if ((null != output) && (event.getType() != FlightEvent.Type.ALTITUDE)) {
				log.info("logging event " + event + " to CSV file");
				output.println("# Event " + event);
				output.flush();
			}

			return true;
		}

		// Log data at end of sim step.
		@Override
		public void postStep(SimulationStatus status) throws SimulationException {

			final Types[] types = Types.values();
			StringBuilder s;

			if (output != null) {
				log.info("logging data to CSV file");
				s = new StringBuilder("" + types[0].getValue(status));
				for (int i = 1; i < types.length; i++) {
					s.append("," + types[i].getValue(status));
				}
				output.println(s);
				output.flush();

			}
		}

		// Close log file at end of simulation
		@Override
		public void endSimulation(SimulationStatus status, SimulationException exception) {
			if (output != null) {
				log.info("Closing CSV file");
				output.close();
				output = null;
			}
		}
	}
}
