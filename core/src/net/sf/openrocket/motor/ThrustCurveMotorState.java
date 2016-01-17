package net.sf.openrocket.motor;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.simulation.MotorState;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.MathUtil;

public class ThrustCurveMotorState implements MotorState {
	//	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorInstance.class);
	
	private int timeIndex = -1;
	
	protected MotorMount mount = null;
	protected MotorInstanceId id = null;
	private double ignitionTime = -1;
	private double ignitionDelay;
	private IgnitionEvent ignitionEvent;
	private double ejectionDelay;
	
	
	protected ThrustCurveMotor motor = null;
	
	// Previous time step value
	private double prevTime = Double.NaN;
	
	// Average thrust during previous step
	private double stepThrust = Double.NaN;
	// Instantaneous thrust at current time point
	private double instThrust = Double.NaN;
	
	// Average CG during previous step
	private Coordinate stepCG = Coordinate.ZERO;
	// Instantaneous CG at current time point
	private Coordinate instCG = Coordinate.ZERO;
	
	private final double unitRotationalInertia;
	private final double unitLongitudinalInertia;
	
	//  // please use the Motor Constructor below, instead.
	//	@SuppressWarnings("unused")
	//	private ThrustCurveMotorInstance() {
	//		unitRotationalInertia = Double.NaN;
	//		unitLongitudinalInertia = Double.NaN;
	//	}
	
	public ThrustCurveMotorState(final ThrustCurveMotor source) {
		//log.debug( Creating motor instance of " + ThrustCurveMotor.this);
		this.motor = source;
		this.reset();
		
		unitRotationalInertia = Inertia.filledCylinderRotational(source.getDiameter() / 2);
		unitLongitudinalInertia = Inertia.filledCylinderLongitudinal(source.getDiameter() / 2, source.getLength());
		
	}
	
	@Override
	public ThrustCurveMotorState clone() {
		try {
			return (ThrustCurveMotorState) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException", e);
		}
	}

	@Override
	public double getIgnitionTime() {
		return ignitionTime;
	}

	@Override
	public void setIgnitionTime(double ignitionTime) {
		this.ignitionTime = ignitionTime;
	}

	@Override
	public MotorMount getMount() {
		return mount;
	}

	@Override
	public void setMount(MotorMount mount) {
		this.mount = mount;
	}

	@Override
	public IgnitionEvent getIgnitionEvent() {
		return ignitionEvent;
	}

	@Override
	public void setIgnitionEvent(IgnitionEvent event) {
		this.ignitionEvent = event;
	}

	@Override
	public double getIgnitionDelay() {
		return ignitionDelay;
	}

	@Override
	public void setIgnitionDelay(double delay) {
		this.ignitionDelay = delay;
	}

	@Override
	public double getEjectionDelay() {
		return ejectionDelay;
	}

	@Override
	public void setEjectionDelay(double delay) {
		this.ejectionDelay = delay;
	}

	@Override
	public void setId(MotorInstanceId id) {
		this.id = id;
	}

	@Override
	public MotorInstanceId getID() {
		return id;
	}

	public double getTime() {
		return prevTime;
	}
	
	public Coordinate getCG() {
		return stepCG;
	}
	
	public Coordinate getCM() {
		return stepCG;
	}
	
	public double getPropellantMass(){
		return (motor.getLaunchCG().weight - motor.getEmptyCG().weight);
	}
	
	public double getLongitudinalInertia() {
		return unitLongitudinalInertia * stepCG.weight;
	}
	
	public double getRotationalInertia() {
		return unitRotationalInertia * stepCG.weight;
	}
	
	@Override
	public double getThrust() {
		return stepThrust;
	}
	
	@Override
	public boolean isActive() {
		return prevTime < motor.getCutOffTime();
	}
	
	public Motor getMotor(){
		return this.motor;
	}
	
	public boolean isEmpty(){
		return false;
	}
	
	@Override
	public void step(double nextTime, double acceleration, AtmosphericConditions cond) {
		if (MathUtil.equals(prevTime, nextTime)) {
			return;
		}
		
		double[] time = motor.getTimePoints();
		double[] thrust = motor.getThrustPoints();
		Coordinate[] cg = motor.getCGPoints();
		
		if (timeIndex >= (motor.getDataSize() - 1)) {
			// Thrust has ended
			prevTime = nextTime;
			stepThrust = 0;
			instThrust = 0;
			stepCG = motor.getEmptyCG();
			return;
		}
		
		
		// Compute average & instantaneous thrust
		if (nextTime < time[timeIndex + 1]) {
			
			// Time step between time points
			double nextF = MathUtil.map(nextTime, time[timeIndex], time[timeIndex + 1],
					thrust[timeIndex], thrust[timeIndex + 1]);
			stepThrust = (instThrust + nextF) / 2;
			instThrust = nextF;
			
		} else {
			
			// Portion of previous step
			stepThrust = (instThrust + thrust[timeIndex + 1]) / 2 * (time[timeIndex + 1] - prevTime);
			
			// Whole steps
			timeIndex++;
			while ((timeIndex < time.length - 1) && (nextTime >= time[timeIndex + 1])) {
				stepThrust += (thrust[timeIndex] + thrust[timeIndex + 1]) / 2 *
						(time[timeIndex + 1] - time[timeIndex]);
				timeIndex++;
			}
			
			// End step
			if (timeIndex < time.length - 1) {
				instThrust = MathUtil.map(nextTime, time[timeIndex], time[timeIndex + 1],
						thrust[timeIndex], thrust[timeIndex + 1]);
				stepThrust += (thrust[timeIndex] + instThrust) / 2 *
						(nextTime - time[timeIndex]);
			} else {
				// Thrust ended during this step
				instThrust = 0;
			}
			
			stepThrust /= (nextTime - prevTime);
			
		}
		
		// Compute average and instantaneous CG (simple average between points)
		Coordinate nextCG;
		if (timeIndex < time.length - 1) {
			nextCG = MathUtil.map(nextTime, time[timeIndex], time[timeIndex + 1],
					cg[timeIndex], cg[timeIndex + 1]);
		} else {
			nextCG = cg[cg.length - 1];
		}
		stepCG = instCG.add(nextCG).multiply(0.5);
		instCG = nextCG;
		
		// Update time
		prevTime = nextTime;
	}
	
	public void reset(){
		timeIndex = 0;
		prevTime = 0;
		instThrust = 0;
		stepThrust = 0;
		instCG = motor.getLaunchCG();
		stepCG = instCG;
	}
	
	@Override
	public String toString(){
		return this.motor.getDesignation();
	}
	
}
