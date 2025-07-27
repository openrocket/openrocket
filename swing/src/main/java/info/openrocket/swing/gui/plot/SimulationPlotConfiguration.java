package info.openrocket.swing.gui.plot;

import java.util.EnumSet;
import java.util.Set;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ArrayList;


public class SimulationPlotConfiguration extends PlotConfiguration<FlightDataType, FlightDataBranch> {
	private static final Translator trans = Application.getTranslator();

	public static final SimulationPlotConfiguration[] DEFAULT_CONFIGURATIONS;
	static {
		ArrayList<SimulationPlotConfiguration> configs = new ArrayList<>();
		SimulationPlotConfiguration config;

		//// Vertical motion vs. time
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Verticalmotion"));
		config.addPlotDataType(FlightDataType.TYPE_ALTITUDE, 0);
		config.addPlotDataType(FlightDataType.TYPE_VELOCITY_Z);
		config.addPlotDataType(FlightDataType.TYPE_ACCELERATION_Z);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Total motion vs. time
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Totalmotion"));
		config.addPlotDataType(FlightDataType.TYPE_ALTITUDE, 0);
		config.addPlotDataType(FlightDataType.TYPE_VELOCITY_TOTAL);
		config.addPlotDataType(FlightDataType.TYPE_ACCELERATION_TOTAL);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Flight side profile
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Flightside"), FlightDataType.TYPE_POSITION_X);
		config.addPlotDataType(FlightDataType.TYPE_ALTITUDE);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);


		//// Ground track
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Groundtrack"), FlightDataType.TYPE_POSITION_X);
		config.addPlotDataType(FlightDataType.TYPE_POSITION_Y, 0);
		config.addPlotDataType(FlightDataType.TYPE_ALTITUDE, 1);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Stability vs. time
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Stability"));
		config.addPlotDataType(FlightDataType.TYPE_STABILITY, 0);
		config.addPlotDataType(FlightDataType.TYPE_CP_LOCATION, 1);
		config.addPlotDataType(FlightDataType.TYPE_CG_LOCATION, 1);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Drag coefficients vs. Mach number
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Dragcoef"),
				FlightDataType.TYPE_MACH_NUMBER);
		config.addPlotDataType(FlightDataType.TYPE_DRAG_COEFF, 0);
		config.addPlotDataType(FlightDataType.TYPE_FRICTION_DRAG_COEFF, 0);
		config.addPlotDataType(FlightDataType.TYPE_BASE_DRAG_COEFF, 0);
		config.addPlotDataType(FlightDataType.TYPE_PRESSURE_DRAG_COEFF, 0);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Roll characteristics
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Rollcharacteristics"));
		config.addPlotDataType(FlightDataType.TYPE_ROLL_RATE, 0);
		config.addPlotDataType(FlightDataType.TYPE_ROLL_MOMENT_COEFF, 1);
		config.addPlotDataType(FlightDataType.TYPE_ROLL_FORCING_COEFF, 1);
		config.addPlotDataType(FlightDataType.TYPE_ROLL_DAMPING_COEFF, 1);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.LAUNCHROD, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Angle of attack and orientation vs. time
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Angleofattack"));
		config.addPlotDataType(FlightDataType.TYPE_AOA, 0);
		config.addPlotDataType(FlightDataType.TYPE_ORIENTATION_PHI);
		config.addPlotDataType(FlightDataType.TYPE_ORIENTATION_THETA);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		//// Simulation time step and computation time
		config = new SimulationPlotConfiguration(trans.get("PlotConfiguration.Simulationtime"));
		config.addPlotDataType(FlightDataType.TYPE_TIME_STEP);
		config.addPlotDataType(FlightDataType.TYPE_COMPUTATION_TIME);
		config.setEvent(FlightEvent.Type.IGNITION, true);
		config.setEvent(FlightEvent.Type.BURNOUT, true);
		config.setEvent(FlightEvent.Type.APOGEE, true);
		config.setEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, true);
		config.setEvent(FlightEvent.Type.STAGE_SEPARATION, true);
		config.setEvent(FlightEvent.Type.GROUND_HIT, true);
		config.setEvent(FlightEvent.Type.TUMBLE, true);
		config.setEvent(FlightEvent.Type.EXCEPTION, true);
		config.setEvent(FlightEvent.Type.SIM_WARN, true);
		config.setEvent(FlightEvent.Type.SIM_ABORT, true);
		configs.add(config);

		DEFAULT_CONFIGURATIONS = configs.toArray(new SimulationPlotConfiguration[0]);
	}

	
	private EnumSet<FlightEvent.Type> events = EnumSet.noneOf(FlightEvent.Type.class);
	
	
	public SimulationPlotConfiguration() {
		this(null, FlightDataType.TYPE_TIME);
	}
	
	public SimulationPlotConfiguration(String name) {
		this(name, FlightDataType.TYPE_TIME);
	}
	
	public SimulationPlotConfiguration(String name, FlightDataType domainType) {
		super(name, domainType);
	}
	
	
	/// Events
	public Set<FlightEvent.Type> getActiveEvents() {
		return events.clone();
	}
	
	public void setEvent(FlightEvent.Type type, boolean active) {
		if (active) {
			events.add(type);
		} else {
			events.remove(type);
		}
	}
	
	public boolean isEventActive(FlightEvent.Type type) {
		return events.contains(type);
	}


	@Override
	public SimulationPlotConfiguration clone() {
		SimulationPlotConfiguration copy = (SimulationPlotConfiguration) super.clone();
		copy.events = this.events.clone();

		return copy;
	}
	
}
