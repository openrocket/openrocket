package info.openrocket.core.simulation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.SimulationListenerHelper;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.MutableCoordinate;

public abstract class AbstractRKSimulationStepper extends AbstractSimulationStepper {

    private static final Logger log = LoggerFactory.getLogger(AbstractRKSimulationStepper.class);
    private static final Translator trans = Application.getTranslator();

    /** Random value with which to XOR the random seed value */
    protected static final int SEED_RANDOMIZATION = 0x23E3A01F;

    /**
     * A recommended reasonably accurate time step.
     */
    public static final double RECOMMENDED_TIME_STEP = 0.05;

    /**
     * A recommended reasonable maximum simulation time (in seconds).
     */
    public static final double RECOMMENDED_MAX_TIME = 1200;

    /**
     * A recommended maximum angle step value.
     */
    public static final double RECOMMENDED_ANGLE_STEP = 3 * Math.PI / 180;

    /**
     * A random amount that is added to pitch and yaw coefficients, plus or minus.
     */
    public static final double PITCH_YAW_RANDOM = 0.0005;

    /**
     * Maximum roll step allowed.  This is selected as an uneven division of the full
     * circle so that the simulation will sample the most wind directions
     */
    protected static final double MAX_ROLL_STEP_ANGLE = 2 * 28.32 * Math.PI / 180;
    //  protected static final double MAX_ROLL_STEP_ANGLE = 8.32 * Math.PI/180;

    protected static final double MAX_ROLL_RATE_CHANGE = 2 * Math.PI / 180;
    protected static final double MAX_PITCH_YAW_CHANGE = 4 * Math.PI / 180;

    protected Random random;
    DataStore store = new DataStore();

    protected final MutableCoordinate mutableCoordA = new MutableCoordinate();
    protected final MutableCoordinate mutableCoordB = new MutableCoordinate();
    protected final MutableCoordinate mutableCoordC = new MutableCoordinate();

    @Override
    public SimulationStatus initialize(SimulationStatus original) {

        SimulationStatus status = new SimulationStatus(original);
        // Copy the existing warnings
        status.setWarnings(original.getWarnings());

        SimulationConditions sim = original.getSimulationConditions();

        store.launchRodDirection = new Coordinate(Math.sin(sim.getLaunchRodAngle()) * Math.cos(Math.PI / 2.0 - sim.getLaunchRodDirection()),
                                                  Math.sin(sim.getLaunchRodAngle()) * Math.sin(Math.PI / 2.0 - sim.getLaunchRodDirection()),
                                                  Math.cos(sim.getLaunchRodAngle()));

        this.random = new Random(original.getSimulationConditions().getRandomSeed() ^ SEED_RANDOMIZATION);

        return status;
    }

	protected double computeTimeStep(SimulationStatus status, double maxTimeStep, RKParameters k1) {

		double timeStep;
		
        /*
         * Select the actual time step to use.  It is the minimum of the following:
         *  dt[0]:  the user-specified time step (or 1/5th of it if still on the launch rod)
         *  dt[1]:  the value of maxTimeStep
         *  dt[2]:  the maximum pitch step angle limit
         *  dt[3]:  the maximum roll step angle limit
         *  dt[4]:  the maximum roll rate change limit
         *  dt[5]:  the maximum pitch change limit
         *  dt[6]:  1/10th of the launch rod length if still on the launch rod
         *  dt[7]:  1.50 times the previous time step
         *
         * The limits #5 and #6 are required since near the steady-state roll rate the roll rate
         * may oscillate significantly even between the sub-steps of the RK4 integration.
         *
         * The step is still at least 1/20th of the user-selected time step.
         */
        double[] dt = new double[8];
        Arrays.fill(dt, Double.MAX_VALUE);

        // If the user selected a really small timestep, use MIN_TIME_STEP instead.
        dt[0] = MathUtil.max(status.getSimulationConditions().getTimeStep(), MIN_TIME_STEP);
        dt[1] = maxTimeStep;
        dt[2] = status.getSimulationConditions().getMaximumAngleStep() / store.lateralPitchRate;
        dt[3] = Math.abs(MAX_ROLL_STEP_ANGLE / store.flightConditions.getRollRate());
        dt[4] = Math.abs(MAX_ROLL_RATE_CHANGE / store.accelerationData.getRotationalAccelerationRC().getZ());
        dt[5] = Math.abs(MAX_PITCH_YAW_CHANGE /
                         MathUtil.max(Math.abs(store.accelerationData.getRotationalAccelerationRC().getX()),
                                      Math.abs(store.accelerationData.getRotationalAccelerationRC().getY())));
        if (!status.isLaunchRodCleared()) {
            dt[0] /= 5.0;
            dt[6] = status.getSimulationConditions().getLaunchRodLength() / k1.v.length() / 10;
        }
        dt[7] = 1.5 * store.timeStep;

        timeStep = Double.MAX_VALUE;
        int limitingValue = -1;
        for (int i = 0; i < dt.length; i++) {
            if (dt[i] < timeStep) {
                timeStep = dt[i];
                limitingValue = i;
            }
        }

        log.trace("Selected time step " + timeStep + " (limiting factor " + limitingValue + ")");

        // If our selected time step is too close to our next scheduled event,
        // (passed in as maxTimeStep) adjust
        double minTimeStep = status.getSimulationConditions().getTimeStep() / 20;

        if (Math.abs(maxTimeStep - timeStep) < minTimeStep) {
            timeStep = maxTimeStep;
            log.trace("selected time step too close to maxTimeStep; adjusted to " + timeStep);
        }

        // If we've wound up with a too-small timestep, increase it avoid numerical instability even at the
        // cost of not being *quite* on an event
        if (timeStep < minTimeStep) {
            log.trace("Too small time step " + store.timeStep + " (limiting factor " + limitingValue + "), using " +
                    minTimeStep + " instead.");
            timeStep = minTimeStep;
        }

		return timeStep;
	}
	
    protected RKParameters computeParameters(SimulationStatus status, DataStore store)
            throws SimulationException {
        RKParameters params = new RKParameters();

        calculateAcceleration(status, store);

        params.a = store.accelerationData.getLinearAccelerationWC();
        params.ra = store.accelerationData.getRotationalAccelerationWC();
        params.v = status.getRocketVelocity();
        params.rv = status.getRocketRotationVelocity();

        checkNaN(params.a, "params.a");
        checkNaN(params.ra, "params.ra");
        checkNaN(params.v, "params.v");
        checkNaN(params.rv, "params.rv");

        return params;
    }

    @Override
    void calculateAcceleration(SimulationStatus status, DataStore store) throws SimulationException {

        // Call pre-listeners
        store.accelerationData = SimulationListenerHelper.firePreAccelerationCalculation(status);

        // Calculate acceleration (if not overridden by pre-listeners)
        if (store.accelerationData == null) {
            store.accelerationData = computeAcceleration(status, store);
        }

        // Call post-listeners
        store.accelerationData = SimulationListenerHelper.firePostAccelerationCalculation(status, store.accelerationData);

    }

    /**
     * Calculate the thrust produced by the motors in the current
     * configuration, at the current simulation time, allowing listeners to override
     * TODO: HIGH:  This method does not take into account any moments generated by off-center motors.
     *
     * @param status                    the current simulation status.
     * @param store                     the simulation calculation DataStore (contains acceleration, atmosphere)
     * @return                          the average thrust during the time step.
     */
    protected double calculateThrust(SimulationStatus status,
                                     DataStore store) throws SimulationException {
        double thrust;

        // Pre-listeners
        thrust = SimulationListenerHelper.firePreThrustCalculation(status);
        if (!Double.isNaN(thrust)) {
            return thrust;
        }

        thrust = 0;
        Collection<MotorClusterState> activeMotorList = status.getActiveMotors();
        for (MotorClusterState currentMotorState : activeMotorList ) {
            thrust += currentMotorState.getThrust( status.getSimulationTime() );
        }

        // Post-listeners
        thrust = SimulationListenerHelper.firePostThrustCalculation(status, thrust);

        checkNaN(thrust, "thrust");

        return thrust;
    }

    /**
     * Calculate the linear and angular acceleration at the given status.  The results
     * are stored in the fields {@link #linearAcceleration} and {@link #angularAcceleration}.
     *
     * @param status   the status of the rocket.
     * @throws SimulationException
     */
    protected AccelerationData computeAcceleration(SimulationStatus status, DataStore store) throws SimulationException {
        MutableCoordinate linearAcceleration;
        MutableCoordinate angularAcceleration;

        // Calculate mass data
        RigidBody structureMassData = calculateStructureMass(status);

        store.motorMass = calculateMotorMass(status);
        store.rocketMass = structureMassData.add( store.motorMass );

        if (store.rocketMass.getMass() < MathUtil.EPSILON) {
            status.abortSimulation(SimulationAbort.Cause.ACTIVE_MASS_ZERO);
        }

        // Compute the forces affecting the rocket
        calculateForces(status, store);

        // Calculate the forces from the aerodynamic coefficients

        double dynP = (0.5 * store.flightConditions.getAtmosphericConditions().getDensity() *
					   MathUtil.pow2(store.flightConditions.getVelocity()));
        double refArea = store.flightConditions.getRefArea();
        double refLength = store.flightConditions.getRefLength();

        // Linear forces in rocket coordinates
        store.dragForce = store.forces.getCDaxial() * dynP * refArea;
        double fN = store.forces.getCN() * dynP * refArea;
        double fSide = store.forces.getCside() * dynP * refArea;

        store.thrustForce = calculateThrust(status, store);
        double forceZ =  store.thrustForce - store.dragForce;

        linearAcceleration = new MutableCoordinate(-fN / store.rocketMass.getMass(),
                -fSide / store.rocketMass.getMass(),
                forceZ / store.rocketMass.getMass());

        store.thetaRotation.rotateZInPlace(linearAcceleration);

        // Convert into rocket world coordinates
        status.getRocketOrientationQuaternion().rotateInPlace(linearAcceleration);

        // add effect of gravity
        store.gravity = modelGravity(status);
        linearAcceleration.sub(0, 0, store.gravity);

        // add effect of Coriolis acceleration
        store.coriolisAcceleration = status.getSimulationConditions().getGeodeticComputation()
                .getCoriolisAcceleration(status.getRocketWorldPosition(), status.getRocketVelocity());
        linearAcceleration.add(store.coriolisAcceleration);

        // If we haven't taken off yet, don't sink into the ground
        if (!status.isLiftoff()) {
            angularAcceleration = new MutableCoordinate();
            if (linearAcceleration.getZ() < 0) {
                linearAcceleration.clear();
            }
        } else if (!status.isLaunchRodCleared()) {

            // If still on the launch rod, project acceleration onto launch rod direction and
            // set angular acceleration to zero.

            double projection = linearAcceleration.dot(store.launchRodDirection);
            CoordinateIF rodDirection = store.launchRodDirection;
            linearAcceleration.set(rodDirection.getX() * projection,
                    rodDirection.getY() * projection,
                    rodDirection.getZ() * projection,
                    0.0);
            angularAcceleration = new MutableCoordinate();

        } else {

            // Shift moments to CG
            double Cm = store.forces.getCm() - store.forces.getCN() * store.rocketMass.getCM().getX() / refLength;
            double Cyaw = store.forces.getCyaw() - store.forces.getCside() * store.rocketMass.getCM().getX() / refLength;

            // Compute moments
            double momX = -Cyaw * dynP * refArea * refLength;
            double momY = Cm * dynP * refArea * refLength;
            double momZ = store.forces.getCroll() * dynP * refArea * refLength;

            // Compute angular acceleration in rocket coordinates
            angularAcceleration = new MutableCoordinate(momX / store.rocketMass.getLongitudinalInertia(),
                    momY / store.rocketMass.getLongitudinalInertia(),
                    momZ / store.rocketMass.getRotationalInertia());

            store.thetaRotation.rotateZInPlace(angularAcceleration);

            // Convert to world coordinates
            status.getRocketOrientationQuaternion().rotateInPlace(angularAcceleration);
        }

        return new AccelerationData(null, null, linearAcceleration, angularAcceleration, status.getRocketOrientationQuaternion());
    }

    /**
     * Calculate the aerodynamic forces into the data store.  This method also handles
     * whether to include aerodynamic computation warnings or not.
     */
    protected void calculateForces(SimulationStatus status, DataStore store) throws SimulationException {

        // Call pre-listeners
        store.forces = SimulationListenerHelper.firePreAerodynamicCalculation(status);
        if (store.forces != null) {
            return;
        }

        // Compute flight conditions
        calculateFlightConditions(status, store);

        /*
         * Check whether to store warnings or not.  Warnings are ignored when on the
         * launch rod or 0.25 seconds after departure, and when the velocity has dropped
         * below 20% of the max. velocity.
         */
        WarningSet warnings = status.recordWarnings() ? new WarningSet() : null;

        // Calculate aerodynamic forces
        store.forces = status.getSimulationConditions().getAerodynamicCalculator()
                .getAerodynamicForces(status.getConfiguration(), store.flightConditions, warnings);
        status.addWarnings(warnings);

        // Add very small randomization to yaw & pitch moments to prevent over-perfect flight
        // TODO: HIGH: This should rather be performed as a listener
        store.forces.setCm(store.forces.getCm() + (PITCH_YAW_RANDOM * 2 * (random.nextDouble() - 0.5)));
        store.forces.setCyaw(store.forces.getCyaw() + (PITCH_YAW_RANDOM * 2 * (random.nextDouble() - 0.5)));

        // Call post-listeners
        store.forces = SimulationListenerHelper.firePostAerodynamicCalculation(status, store.forces);
    }

    protected static class RKParameters {
        /** Linear acceleration */
        public CoordinateIF a;
        /** Linear velocity */
        public CoordinateIF v;
        /** Rotational acceleration */
        public CoordinateIF ra;
        /** Rotational velocity */
        public CoordinateIF rv;
    }
		
}
	
	
