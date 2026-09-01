package info.openrocket.core.simulation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.MutableCoordinate;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.WorldCoordinate;

public class RK4SimulationStepper extends AbstractRKSimulationStepper {

    private static final Logger log = LoggerFactory.getLogger(RK4SimulationStepper.class);
    private static final Translator trans = Application.getTranslator();

    @Override
    public void step(SimulationStatus status, double maxTimeStep) throws SimulationException {

        status.storeData();

        ////////  Perform RK4 integration:  ////////

        SimulationStatus status2;
        RKParameters k1, k2, k3, k4;

        /*
         * Get the current atmospheric conditions
         */
        calculateFlightConditions(status, store);

        /*
         * Perform RK4 integration.  Decide the time step length after the first step.
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
        dt[4] = Math.abs(MAX_ROLL_RATE_CHANGE / store.accelerationData.getRotationalAccelerationRC().getZ());
        dt[5] = Math.abs(MAX_PITCH_YAW_CHANGE /
                         MathUtil.max(Math.abs(store.accelerationData.getRotationalAccelerationRC().getX()),
                                      Math.abs(store.accelerationData.getRotationalAccelerationRC().getY())));
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

        log.trace("Selected time step " + store.timeStep + " (limiting factor " + limitingValue + ")");

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

        // TODO: MEDIUM: Store acceleration etc of entire RK4 step, store should be cloned or something...
        store.storeData(status);
        checkNaN(store.timeStep, "store.timeStep");

        //// Second position, k2 = f(t + h/2, y + k1*h/2)

    status2 = status.clone();
    status2.setSimulationTime(status.getSimulationTime() + store.timeStep / 2);
    status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
        .addScaled(k1.v, store.timeStep / 2)
        .toImmutable());
    status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
        .addScaled(k1.a, store.timeStep / 2)
        .toImmutable());
    status2.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion()
        .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k1.rv)
            .multiply(store.timeStep / 2)
            .toImmutable())));
    status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
        .addScaled(k1.ra, store.timeStep / 2)
        .toImmutable());

        k2 = computeParameters(status2, store);

        //// Third position, k3 = f(t + h/2, y + k2*h/2)

    status2 = status.clone();
    status2.setSimulationTime(status.getSimulationTime() + store.timeStep / 2);
    status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
        .addScaled(k2.v, store.timeStep / 2)
        .toImmutable());
    status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
        .addScaled(k2.a, store.timeStep / 2)
        .toImmutable());
    status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
        .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k2.rv)
            .multiply(store.timeStep / 2)
            .toImmutable())));
    status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
        .addScaled(k2.ra, store.timeStep / 2)
        .toImmutable());

        k3 = computeParameters(status2, store);

        //// Fourth position, k4 = f(t + h, y + k3*h)

    status2 = status.clone();
    status2.setSimulationTime(status.getSimulationTime() + store.timeStep);
    status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
        .addScaled(k3.v, store.timeStep)
        .toImmutable());
    status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
        .addScaled(k3.a, store.timeStep)
        .toImmutable());
    status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
        .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k3.rv)
            .multiply(store.timeStep)
            .toImmutable())));
    status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
        .addScaled(k3.ra, store.timeStep)
        .toImmutable());

        k4 = computeParameters(status2, store);

        //// Sum all together,  y(n+1) = y(n) + h*(k1 + 2*k2 + 2*k3 + k4)/6
    CoordinateIF deltaO;
    CoordinateIF deltaVCoord = mutableCoordA.clear()
        .addScaled(k2.a, 2)
        .addScaled(k3.a, 2)
        .add(k1.a)
        .add(k4.a)
        .multiply(store.timeStep / 6)
        .toImmutable();
    CoordinateIF deltaPCoord = mutableCoordB.clear()
        .addScaled(k2.v, 2)
        .addScaled(k3.v, 2)
        .add(k1.v)
        .add(k4.v)
        .multiply(store.timeStep / 6)
        .toImmutable();
    CoordinateIF deltaRCoord = mutableCoordC.clear()
        .addScaled(k2.ra, 2)
        .addScaled(k3.ra, 2)
        .add(k1.ra)
        .add(k4.ra)
        .multiply(store.timeStep / 6)
        .toImmutable();
    deltaO = mutableCoordA.clear()
        .addScaled(k2.rv, 2)
        .addScaled(k3.rv, 2)
        .add(k1.rv)
        .add(k4.rv)
        .multiply(store.timeStep / 6)
        .toImmutable();

    status.setRocketVelocity(mutableCoordB.set(status.getRocketVelocity())
        .add(deltaVCoord)
        .toImmutable());
    status.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
        .add(deltaPCoord)
        .toImmutable());
    status.setRocketRotationVelocity(mutableCoordC.set(status.getRocketRotationVelocity())
        .add(deltaRCoord)
        .toImmutable());
    status.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion()
        .multiplyLeft(Quaternion.rotation(deltaO)).normalizeIfNecessary());

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
}
