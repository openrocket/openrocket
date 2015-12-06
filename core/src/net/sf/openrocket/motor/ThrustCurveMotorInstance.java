package net.sf.openrocket.motor;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Utils;

public class ThrustCurveMotorInstance extends MotorInstance {
	//	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorInstance.class);
	
	private int timeIndex = -1;
	
	protected MotorMount mount = null;
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
	
	public ThrustCurveMotorInstance(final ThrustCurveMotor source) {
		//log.debug( Creating motor instance of " + ThrustCurveMotor.this);
		this.motor = source;
		this.reset();
		
		unitRotationalInertia = Inertia.filledCylinderRotational(source.getDiameter() / 2);
		unitLongitudinalInertia = Inertia.filledCylinderLongitudinal(source.getDiameter() / 2, source.getLength());
		
		this.id = MotorInstanceId.ERROR_ID;
	}
	
	@Override
	public double getTime() {
		return prevTime;
	}
	
	@Override
	public Coordinate getCG() {
		return stepCG;
	}
	
	@Override
	public Coordinate getCM() {
		return stepCG;
	}
	
	@Override
	public double getPropellantMass(){
		return (motor.getLaunchCG().weight - motor.getEmptyCG().weight);
	}
	
	@Override
	public Coordinate getOffset( ){
		if( null == mount ){
			return Coordinate.NaN;
		}else{
			RocketComponent comp = (RocketComponent) mount;
			double delta_x = comp.getLength() + mount.getMotorOverhang() - this.motor.getLength();
			return new Coordinate(delta_x, 0, 0);
		}
	}
	
	@Override
	public double getLongitudinalInertia() {
		return unitLongitudinalInertia * stepCG.weight;
	}
	
	@Override
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

	@Override
	public void setMotor(Motor motor) {
		if( !( motor instanceof ThrustCurveMotor )){
			return;
		}
		if (Utils.equals(this.motor, motor)) {
			return;
		}
		
		this.motor = (ThrustCurveMotor)motor;
		
		fireChangeEvent();
	}
	
	@Override
	public Motor getMotor(){
		return this.motor;
	}

	@Override
	public boolean isEmpty(){
		return false;
	}
	
	@Override
	public MotorMount getMount() {
		return this.mount;
	}
	
	@Override
	public void setMount(final MotorMount _mount) {
		this.mount = _mount;
		
	}
	
	@Override
	public void setEjectionDelay(double delay) {
		if (MathUtil.equals(ejectionDelay, delay)) {
			return;
		}
		this.ejectionDelay = delay;
		fireChangeEvent();
	}
	
	
	@Override
	public void step(double nextTime, double acceleration, AtmosphericConditions cond) {
		if (MathUtil.equals(prevTime, nextTime)) {
			return;
		}
		
		modID++;
		
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
	
	@Override
	public MotorInstance clone() {
		ThrustCurveMotorInstance clone = new ThrustCurveMotorInstance( this.motor);
		
		clone.id = this.id;
		clone.mount = this.mount;
		clone.ignitionEvent = this.ignitionEvent;
		clone.ignitionDelay = this.ignitionDelay;
		clone.ejectionDelay = this.ejectionDelay;
		clone.position = this.position;
		clone.ignitionTime = Double.POSITIVE_INFINITY;
		//clone.ignitionTime = this.ignitionTime;
	
		return clone;
	}

	@Override
	public void reset(){
		timeIndex = 0;
		prevTime = 0;
		instThrust = 0;
		stepThrust = 0;
		instCG = motor.getLaunchCG();
		stepCG = instCG;
	}
	
	@Override
	public String toDebug(){
		String prefix = "    ";
		StringBuilder sb = new StringBuilder();
		final RocketComponent mountComp = (RocketComponent)this.mount;
		
		sb.append(String.format("%sMotor= %s(%s)(in %s)\n", prefix, this.motor.getDesignation(), this.id.toShortKey(), mountComp.getName()));
		sb.append(String.format("%s    Ignite: %s at %+f\n",prefix, this.ignitionEvent.name, this.ignitionDelay));
		//sb.append(String.format("%s    Eject at: %+f\n",prefix, this.ejectionDelay));
		sb.append(String.format("%s    L:%f W:%f @:%f mm\n",prefix, motor.getLength(), motor.getDiameter(), this.position.multiply(1000).x ));
		sb.append(String.format("%s    currentTimem: %f\n", prefix, this.prevTime)); 
		sb.append("\n");
		return sb.toString();
	}

	@Override
	public String toString(){
		return this.id.toString();
	}
	
}
	