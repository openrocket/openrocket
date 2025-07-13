package info.openrocket.core.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.SimulationAbort;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.simulation.exception.SimulationCalculationException;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.listeners.SimulationListenerHelper;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.WorldCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;


public class RK6SimulationStepper extends AbstractSimulationStepper {

    private static final Logger log = LoggerFactory.getLogger(RK6SimulationStepper.class);
    private static final Translator trans = Application.getTranslator();


    /** Random value with which to XOR the random seed value */
    private static final int SEED_RANDOMIZATION = 0x23E3A01F;


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
    private static final double MAX_ROLL_STEP_ANGLE = 2 * 28.32 * Math.PI / 180;
    //	private static final double MAX_ROLL_STEP_ANGLE = 8.32 * Math.PI/180;

    private static final double MAX_ROLL_RATE_CHANGE = 2 * Math.PI / 180;
    private static final double MAX_PITCH_YAW_CHANGE = 4 * Math.PI / 180;

    private Random random;
    DataStore store = new DataStore();

    @Override
    public SimulationStatus initialize(SimulationStatus original) {

        SimulationStatus status = new SimulationStatus(original);
        // Copy the existing warnings
        status.setWarnings(original.getWarnings());

        SimulationConditions sim = original.getSimulationConditions();

        store.launchRodDirection = new Coordinate(
                Math.sin(sim.getLaunchRodAngle()) * Math.cos(Math.PI / 2.0 - sim.getLaunchRodDirection()),
                Math.sin(sim.getLaunchRodAngle()) * Math.sin(Math.PI / 2.0 - sim.getLaunchRodDirection()),
                Math.cos(sim.getLaunchRodAngle()));

        this.random = new Random(original.getSimulationConditions().getRandomSeed() ^ SEED_RANDOMIZATION);

        return status;
    }


    @Override
    public void step(SimulationStatus status, double maxTimeStep) throws SimulationException {

        status.storeData();

        ////////  Perform RK4 integration:  ////////

        SimulationStatus status2;
        RK6Parameters k1, k2, k3, k4, k5, k6, k7;

        /*
         * Get the current atmospheric conditions
         */
        calculateFlightConditions(status, store);

		/*
		 * Perform RK6 integration.  Decide the time step length after the first step.
		 *
		 *
		 * RK6 Coefficients from Mechee, M. S., & Rajihy, Y. (2017).
		 * Generalized RK Integrators for Solving Ordinary Differential Equations:
		 * A Survey & Comparison Study. Global Journal of Pure and Applied Mathematics, 13(7), 2923-2949.
		 * https://www.researchgate.net/publication/318284280_Generalized_RK_Integrators_for_Solving_Ordinary_Differential_Equations_A_Survey_Comparison_Study
		 * [Table 2]


		Butcher Tableau for RK6, according to Mechee & Rajihy:

			0   | 0      & 0     & 0     & 0     & 0     & 0      & 0
			1/3 | 1/3    & 0     & 0     & 0     & 0     & 0      & 0
			2/3 | 0      & 2/3   & 0     & 0     & 0     & 0      & 0
			1/3 | 1/12   & 1/3   & -1/12 & 0     & 0     & 0      & 0
			1/2 | -1/16  & 9/8   & -3/16 & -3/8  & 0     & 0      & 0
			1/2 | 0      & 9/8   & -3/8  & -3/4  & 1/2   & 0      & 0
			1   | 9/44   & -9/11 & 63/44 & 18/11 & 0     & -16/11 & 0
			----------------------------------------------------------------
				| 11/120 & 0     & 27/40 & 27/40 & -4/15 & -4/15  & 11/120


		In function calls, it will be:

			k1 = f(t, y)
			k2 = f(t + h/3, y + 1/3*h*k1)
			k3 = f(t + h*2/3, y + 2/3*h*k2)
			k4 = f(t + h*1/3, y + 1/12*h*k1 + 1/3*h*k2 - 1/12*h*k3)
			k5 = f(t + h*1/2, y - 1/16*h*k1 + 9/8*h*k2 - 3/16*h*k3 - 3/8*h*k4)
			k6 = f(t + h*1/2, y + 9/8*h*k2 - 3/8*h*k3 - 3/4*h*k4 + 1/2*h*k5)
			k7 = f(t + h, y + 9/44*h*k1 - 9/11*h*k2 + 63/44*h*k3 + 18/11*h*k4 - 16/11*h*k6)
		*/

        //// First position, k1 = f(t, y)

        k1 = computeParameters(status, store);



        // If maxTimeStep is NaN we'll just record sim params and leave
        if (Double.isNaN(maxTimeStep)) {
            store.timeStep = maxTimeStep;
            store.storeData(status);

            landedValues(status, store);
            return;
        }

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
        dt[4] = Math.abs(MAX_ROLL_RATE_CHANGE / store.accelerationData.getRotationalAccelerationRC().z);
        dt[5] = Math.abs(MAX_PITCH_YAW_CHANGE /
                MathUtil.max(Math.abs(store.accelerationData.getRotationalAccelerationRC().x),
                        Math.abs(store.accelerationData.getRotationalAccelerationRC().y)));
        if (!status.isLaunchRodCleared()) {
            dt[0] /= 5.0;
            dt[6] = status.getSimulationConditions().getLaunchRodLength() / k1.v.length() / 10;
        }
        dt[7] = 1.5 * store.timeStep;

        store.timeStep = Double.MAX_VALUE;
        int limitingValue = -1;
        for (int i = 0; i < dt.length; i++) {
            if (dt[i] < store.timeStep) {
                store.timeStep = dt[i];
                limitingValue = i;
            }
        }


        // If our selected time step is too close to our next scheduled event,
        // (passed in as maxTimeStep) adjust
        double minTimeStep = status.getSimulationConditions().getTimeStep() / 20;

        if (Math.abs(maxTimeStep - store.timeStep) < minTimeStep) {
            store.timeStep = maxTimeStep;
            log.trace("selected time step too close to maxTimeStep; adjusted to " + store.timeStep);
        }

        // If we've wound up with a too-small timestep, increase it avoid numerical instability even at the
        // cost of not being *quite* on an event
        if (store.timeStep < minTimeStep) {
            log.trace("Too small time step " + store.timeStep + " (limiting factor " + limitingValue + "), using " +
                    minTimeStep + " instead.");
            store.timeStep = minTimeStep;
        }

        // TODO: MEDIUM: Store acceleration etc of entire RK6 step, store should be cloned or something...
        store.storeData(status);
        checkNaN(store.timeStep, "store.timeStep");

        //// Second position, k2 = f(t + h/3, y + 1/3*h*k1)
        double weightk1 = 1.0/3;
        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep / 3);
        status2.setRocketPosition(status.getRocketPosition().add(k1.v.multiply(store.timeStep * weightk1)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k1.a.multiply(store.timeStep * weightk1)));
        status2.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k1.rv.multiply(store.timeStep * weightk1))));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k1.ra.multiply(store.timeStep *weightk1)));

        k2 = computeParameters(status2, store);


        //// Third position, k3 = f(t + h*2/3, y + 2/3*h*k2)
        double weightk2 = 2.0/3;
        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep * 2/3);
        status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timeStep * weightk2)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timeStep * weightk2)));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timeStep * weightk2))));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timeStep * weightk2)));

        k3 = computeParameters(status2, store);


        //// Fourth position, k4 = f(t + h*1/3, y + 1/12*h*k1 + 1/3*h*k2 - 1/12*h*k3)
        weightk1 = 1.0/12;
        weightk2 = 1.0/3;
        double weightk3 = -1.0/12;
        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep*1/3);
        status2.setRocketPosition(status.getRocketPosition().add(k1.v.multiply(store.timeStep*weightk1)));
        status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timeStep*weightk2)));
        status2.setRocketPosition(status.getRocketPosition().add(k3.v.multiply(store.timeStep*weightk3)));

        status2.setRocketVelocity(status.getRocketVelocity().add(k1.a.multiply(store.timeStep*weightk1)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timeStep*weightk2)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k3.a.multiply(store.timeStep*weightk3)));

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k1.rv.multiply(store.timeStep*weightk1))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timeStep*weightk2))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k3.rv.multiply(store.timeStep*weightk3))));

        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k1.ra.multiply(store.timeStep*weightk1)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timeStep*weightk2)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k3.ra.multiply(store.timeStep*weightk3)));

        k4 = computeParameters(status2, store);


        //// Fifth position, k5 = f(t + h*1/2, y - 1/16*h*k1 + 9/8*h*k2 - 3/16*h*k3 - 3/8*h*k4)
        weightk1 = -1.0/16;
        weightk2 = 9.0/8;
        weightk3 = -3.0/16;
        double weightk4 = -3.0/8;

        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep*1/2);
        status2.setRocketPosition(status.getRocketPosition().add(k1.v.multiply(store.timeStep*weightk1)));
        status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timeStep*weightk2)));
        status2.setRocketPosition(status.getRocketPosition().add(k3.v.multiply(store.timeStep*weightk3)));
        status2.setRocketPosition(status.getRocketPosition().add(k4.v.multiply(store.timeStep*weightk4)));

        status2.setRocketVelocity(status.getRocketVelocity().add(k1.a.multiply(store.timeStep*weightk1)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timeStep*weightk2)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k3.a.multiply(store.timeStep*weightk3)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k4.a.multiply(store.timeStep*weightk4)));

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k1.rv.multiply(store.timeStep*weightk1))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timeStep*weightk2))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k3.rv.multiply(store.timeStep*weightk3))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k4.rv.multiply(store.timeStep*weightk4))));

        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k1.ra.multiply(store.timeStep*weightk1)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timeStep*weightk2)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k3.ra.multiply(store.timeStep*weightk3)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k4.ra.multiply(store.timeStep*weightk4)));

        k5 = computeParameters(status2, store);



        //// Sixth position, k6 = f(t + h*1/2, y + 9/8*h*k2 - 3/8*h*k3 - 3/4*h*k4 + 1/2*h*k5)
        weightk2 = 9.0/8;
        weightk3 = -3.0/8;
        weightk4 = -3.0/4;
        double weightk5 = 1.0/2;

        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep*1/2);
        status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timeStep*weightk2)));
        status2.setRocketPosition(status.getRocketPosition().add(k3.v.multiply(store.timeStep*weightk3)));
        status2.setRocketPosition(status.getRocketPosition().add(k4.v.multiply(store.timeStep*weightk4)));
        status2.setRocketPosition(status.getRocketPosition().add(k5.v.multiply(store.timeStep*weightk5)));

        status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timeStep*weightk2)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k3.a.multiply(store.timeStep*weightk3)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k4.a.multiply(store.timeStep*weightk4)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k5.a.multiply(store.timeStep*weightk5)));

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timeStep*weightk2))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k3.rv.multiply(store.timeStep*weightk3))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k4.rv.multiply(store.timeStep*weightk4))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k5.rv.multiply(store.timeStep*weightk5))));

        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timeStep*weightk2)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k3.ra.multiply(store.timeStep*weightk3)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k4.ra.multiply(store.timeStep*weightk4)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k5.ra.multiply(store.timeStep*weightk5)));

        k6 = computeParameters(status2, store);


        //// Seventh position, k7 = f(t + h, y + 9/44*h*k1 - 9/11*h*k2 + 63/44*h*k3 + 18/11*h*k4 - 16/11*h*k6)
        weightk1 = 9.0/44;
        weightk2 = -9.0/11;
        weightk3 = 63.0/44;
        weightk4 = 18.0/11;
        double weightk6 = -16.0/11;

        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep);
        status2.setRocketPosition(status.getRocketPosition().add(k1.v.multiply(store.timeStep*weightk1)));
        status2.setRocketPosition(status.getRocketPosition().add(k2.v.multiply(store.timeStep*weightk2)));
        status2.setRocketPosition(status.getRocketPosition().add(k3.v.multiply(store.timeStep*weightk3)));
        status2.setRocketPosition(status.getRocketPosition().add(k4.v.multiply(store.timeStep*weightk4)));
        status2.setRocketPosition(status.getRocketPosition().add(k6.v.multiply(store.timeStep* weightk6)));

        status2.setRocketVelocity(status.getRocketVelocity().add(k1.a.multiply(store.timeStep*weightk1)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k2.a.multiply(store.timeStep*weightk2)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k3.a.multiply(store.timeStep*weightk3)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k4.a.multiply(store.timeStep*weightk4)));
        status2.setRocketVelocity(status.getRocketVelocity().add(k6.a.multiply(store.timeStep* weightk6)));

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k1.rv.multiply(store.timeStep*weightk1))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k2.rv.multiply(store.timeStep*weightk2))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k3.rv.multiply(store.timeStep*weightk3))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k4.rv.multiply(store.timeStep*weightk4))));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(k6.rv.multiply(store.timeStep* weightk6))));

        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k1.ra.multiply(store.timeStep*weightk1)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k2.ra.multiply(store.timeStep*weightk2)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k3.ra.multiply(store.timeStep*weightk3)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k4.ra.multiply(store.timeStep*weightk4)));
        status2.setRocketRotationVelocity(status.getRocketRotationVelocity().add(k6.ra.multiply(store.timeStep*weightk6)));

        k7 = computeParameters(status2, store);

        //// Sum all together,  y(n+1) = y(n) + dt*(11/120*k1 + 27/40*k3 + 27/40*k4 - 4/15*k5 - 4/15*k6 + 11/120*k7)
        Coordinate deltaV, deltaP, deltaR, deltaO;
        deltaV = (k1.a.multiply(11.0/120))
                .add((k3.a.multiply(27.0/40)))
                .add((k4.a.multiply(27.0/40)))
                .add((k5.a.multiply(-4.0/15)))
                .add((k6.a.multiply(-4.0/15)))
                .add((k7.a.multiply(11.0/120)))
                .multiply(store.timeStep);
        deltaP = (k1.v.multiply(11.0/120))
                .add((k3.v.multiply(27.0/40)))
                .add((k4.v.multiply(27.0/40)))
                .add((k5.v.multiply(-4.0/15)))
                .add((k6.v.multiply(-4.0/15)))
                .add((k7.v.multiply(11.0/120)))
                .multiply(store.timeStep);
        deltaR = (k1.ra.multiply(11.0/120))
                .add((k3.ra.multiply(27.0/40)))
                .add((k4.ra.multiply(27.0/40)))
                .add((k5.ra.multiply(-4.0/15)))
                .add((k6.ra.multiply(-4.0/15)))
                .add((k7.ra.multiply(11.0/120)))
                .multiply(store.timeStep);
        deltaO = (k1.rv.multiply(11.0/120))
                .add((k3.rv.multiply(27.0/40)))
                .add((k4.rv.multiply(27.0/40)))
                .add((k5.rv.multiply(-4.0/15)))
                .add((k6.rv.multiply(-4.0/15)))
                .add((k7.rv.multiply(11.0/120)))
                .multiply(store.timeStep);

        status.setRocketVelocity(status.getRocketVelocity().add(deltaV));
        status.setRocketPosition(status.getRocketPosition().add(deltaP));
        status.setRocketRotationVelocity(status.getRocketRotationVelocity().add(deltaR));
        status.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion().multiplyLeft(Quaternion.rotation(deltaO)).normalizeIfNecessary());

        WorldCoordinate w = status.getSimulationConditions().getLaunchSite();
        w = status.getSimulationConditions().getGeodeticComputation().addCoordinate(w, status.getRocketPosition());
        status.setRocketWorldPosition(w);

        if (!(0 <= store.timeStep)) {
            // Also catches NaN
            throw new IllegalArgumentException("Stepping backwards in time, timestep=" + store.timeStep);
        }
        status.setSimulationTime(status.getSimulationTime() + store.timeStep);

        // Verify that values don't run out of range
        if (status.getRocketVelocity().length2() > 1.0e18 ||
                status.getRocketPosition().length2() > 1.0e18 ||
                status.getRocketRotationVelocity().length2() > 1.0e18) {
            throw new SimulationCalculationException(trans.get("error.valuesTooLarge"), status.getFlightDataBranch());
        }
    }

    private RK6Parameters computeParameters(SimulationStatus status, DataStore store)
            throws SimulationException {
        RK6Parameters params = new RK6Parameters();
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
     * @param status					the current simulation status.
     * @param store                     the simulation calculation DataStore (contains acceleration, atmosphere)
     * @return							the average thrust during the time step.
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
    private AccelerationData computeAcceleration(SimulationStatus status, DataStore store) throws SimulationException {
        Coordinate linearAcceleration;
        Coordinate angularAcceleration;

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

        linearAcceleration = new Coordinate(-fN / store.rocketMass.getMass(),
                -fSide / store.rocketMass.getMass(),
                forceZ / store.rocketMass.getMass());

        linearAcceleration = store.thetaRotation.rotateZ(linearAcceleration);

        // Convert into rocket world coordinates
        linearAcceleration = status.getRocketOrientationQuaternion().rotate(linearAcceleration);

        // add effect of gravity
        store.gravity = modelGravity(status);
        linearAcceleration = linearAcceleration.sub(0, 0, store.gravity);

        // add effect of Coriolis acceleration
        store.coriolisAcceleration = status.getSimulationConditions().getGeodeticComputation()
                .getCoriolisAcceleration(status.getRocketWorldPosition(), status.getRocketVelocity());
        linearAcceleration = linearAcceleration.add(store.coriolisAcceleration);

        // If we haven't taken off yet, don't sink into the ground
        if (!status.isLiftoff()) {
            angularAcceleration = Coordinate.NUL;
            if (linearAcceleration.z < 0) {
                linearAcceleration = Coordinate.ZERO;
            }
        } else if (!status.isLaunchRodCleared()) {

            // If still on the launch rod, project acceleration onto launch rod direction and
            // set angular acceleration to zero.

            linearAcceleration = store.launchRodDirection.multiply(linearAcceleration.dot(store.launchRodDirection));
            angularAcceleration = Coordinate.NUL;

        } else {

            // Shift moments to CG
            double Cm = store.forces.getCm() - store.forces.getCN() * store.rocketMass.getCM().x / refLength;
            double Cyaw = store.forces.getCyaw() - store.forces.getCside() * store.rocketMass.getCM().x / refLength;

            // Compute moments
            double momX = -Cyaw * dynP * refArea * refLength;
            double momY = Cm * dynP * refArea * refLength;
            double momZ = store.forces.getCroll() * dynP * refArea * refLength;

            // Compute angular acceleration in rocket coordinates
            angularAcceleration = new Coordinate(momX / store.rocketMass.getLongitudinalInertia(),
                    momY / store.rocketMass.getLongitudinalInertia(),
                    momZ / store.rocketMass.getRotationalInertia());

            angularAcceleration = store.thetaRotation.rotateZ(angularAcceleration);

            // Convert to world coordinates
            angularAcceleration = status.getRocketOrientationQuaternion().rotate(angularAcceleration);
        }

        return new AccelerationData(null, null, linearAcceleration, angularAcceleration, status.getRocketOrientationQuaternion());
    }


    /**
     * Calculate the aerodynamic forces into the data store.  This method also handles
     * whether to include aerodynamic computation warnings or not.
     */
    private void calculateForces(SimulationStatus status, DataStore store) throws SimulationException {

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

        if (null != warnings) {
            // If this doesn't include the sustainer and either isn't stable or is about
            // to deploy a recovery device, don't store open airframe warnings
            boolean sustainer = status.getConfiguration().isStageActive(0);
            boolean stable = store.rocketMass.getCM().x < store.forces.getCP().x;
            boolean recoverySoon = false;
            for (FlightEvent e : status.getEventQueue()) {
                if ((e.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) &&
                        (e.getTime() < status.getSimulationTime() + 0.5)) {
                    recoverySoon = true;
                }
            }

            if (!sustainer && (!stable || recoverySoon)) {
                warnings.filterOut(Warning.OPEN_AIRFRAME_FORWARD);
            }

            status.addWarnings(warnings);
        }

        // Add very small randomization to yaw & pitch moments to prevent over-perfect flight
        // TODO: HIGH: This should rather be performed as a listener
        store.forces.setCm(store.forces.getCm() + (PITCH_YAW_RANDOM * 2 * (random.nextDouble() - 0.5)));
        store.forces.setCyaw(store.forces.getCyaw() + (PITCH_YAW_RANDOM * 2 * (random.nextDouble() - 0.5)));


        // Call post-listeners
        store.forces = SimulationListenerHelper.firePostAerodynamicCalculation(status, store.forces);
    }



    private static class RK6Parameters {
        /** Linear acceleration */
        public Coordinate a;
        /** Linear velocity */
        public Coordinate v;
        /** Rotational acceleration */
        public Coordinate ra;
        /** Rotational velocity */
        public Coordinate rv;


        public String toString() {
            return "----\na: " + a + "\nv:" + v + "\nra:" + ra + "\nrv:" + rv + "\n----";
        }
    }
}