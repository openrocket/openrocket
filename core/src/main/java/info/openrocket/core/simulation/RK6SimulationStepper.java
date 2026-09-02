package info.openrocket.core.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.exception.SimulationCalculationException;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MutableCoordinate;
import info.openrocket.core.util.Quaternion;
import info.openrocket.core.util.WorldCoordinate;

public class RK6SimulationStepper extends AbstractRKSimulationStepper {

    private static final Logger log = LoggerFactory.getLogger(RK6SimulationStepper.class);
    private static final Translator trans = Application.getTranslator();

    @Override
    public void step(SimulationStatus status, double maxTimeStep) throws SimulationException {

        status.storeData();

        ////////  Perform RK6 integration:  ////////

        SimulationStatus status2;
        RKParameters k1, k2, k3, k4, k5, k6, k7;

        /*
         * Get the current atmospheric conditions
         */
        calculateFlightConditions(status, store);

        /*
         * Perform RK6 integration.  Decide the time step length after the first step.
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

		store.timeStep = computeTimeStep(status, maxTimeStep, k1);

        // TODO: MEDIUM: Store acceleration etc of entire RK6 step, store should be cloned or something...
        store.storeData(status);
        checkNaN(store.timeStep, "store.timeStep");

        //// Second position, k2 = f(t + h/3, y + 1/3*h*k1)
        double weightk1 = 1.0/3;
        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep / 3);
        status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
                .addScaled(k1.v, store.timeStep * weightk1)
                .toImmutable());
        status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
                .addScaled(k1.a, store.timeStep * weightk1)
                .toImmutable());
        status2.setRocketOrientationQuaternion(status.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k1.rv)
                        .multiply(store.timeStep * weightk1)
                        .toImmutable())));
        status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
                .addScaled(k1.ra, store.timeStep * weightk1)
                .toImmutable());

        k2 = computeParameters(status2, store);

        //// Third position, k3 = f(t + h*2/3, y + 2/3*h*k2)
        double weightk2 = 2.0/3;
        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep * 2/3);
        status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
                .addScaled(k2.v, store.timeStep * weightk2)
                .toImmutable());
        status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
                .addScaled(k2.a, store.timeStep * weightk2)
                .toImmutable());
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k2.rv)
                        .multiply(store.timeStep * weightk2)
                        .toImmutable())));
        status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
                .addScaled(k2.ra, store.timeStep * weightk2)
                .toImmutable());

        k3 = computeParameters(status2, store);

        //// Fourth position, k4 = f(t + h*1/3, y + 1/12*h*k1 + 1/3*h*k2 - 1/12*h*k3)
        weightk1 = 1.0/12;
        weightk2 = 1.0/3;
        double weightk3 = -1.0/12;
        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep*1/3);
        // Chain addScaled calls so all k-values are accumulated (not overwritten)
        status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
                .addScaled(k1.v, store.timeStep * weightk1)
                .addScaled(k2.v, store.timeStep * weightk2)
                .addScaled(k3.v, store.timeStep * weightk3)
                .toImmutable());

        status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
                .addScaled(k1.a, store.timeStep * weightk1)
                .addScaled(k2.a, store.timeStep * weightk2)
                .addScaled(k3.a, store.timeStep * weightk3)
                .toImmutable());

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k1.rv)
                        .multiply(store.timeStep * weightk1)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k2.rv)
                        .multiply(store.timeStep * weightk2)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k3.rv)
                        .multiply(store.timeStep * weightk3)
                        .toImmutable())));

        status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
                .addScaled(k1.ra, store.timeStep * weightk1)
                .addScaled(k2.ra, store.timeStep * weightk2)
                .addScaled(k3.ra, store.timeStep * weightk3)
                .toImmutable());

        k4 = computeParameters(status2, store);

        //// Fifth position, k5 = f(t + h*1/2, y - 1/16*h*k1 + 9/8*h*k2 - 3/16*h*k3 - 3/8*h*k4)
        weightk1 = -1.0/16;
        weightk2 = 9.0/8;
        weightk3 = -3.0/16;
        double weightk4 = -3.0/8;

        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep*1/2);
        status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
                .addScaled(k1.v, store.timeStep * weightk1)
                .addScaled(k2.v, store.timeStep * weightk2)
                .addScaled(k3.v, store.timeStep * weightk3)
                .addScaled(k4.v, store.timeStep * weightk4)
                .toImmutable());

        status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
                .addScaled(k1.a, store.timeStep * weightk1)
                .addScaled(k2.a, store.timeStep * weightk2)
                .addScaled(k3.a, store.timeStep * weightk3)
                .addScaled(k4.a, store.timeStep * weightk4)
                .toImmutable());

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k1.rv)
                        .multiply(store.timeStep * weightk1)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k2.rv)
                        .multiply(store.timeStep * weightk2)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k3.rv)
                        .multiply(store.timeStep * weightk3)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k4.rv)
                        .multiply(store.timeStep * weightk4)
                        .toImmutable())));

        status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
                .addScaled(k1.ra, store.timeStep * weightk1)
                .addScaled(k2.ra, store.timeStep * weightk2)
                .addScaled(k3.ra, store.timeStep * weightk3)
                .addScaled(k4.ra, store.timeStep * weightk4)
                .toImmutable());

        k5 = computeParameters(status2, store);

        //// Sixth position, k6 = f(t + h*1/2, y + 9/8*h*k2 - 3/8*h*k3 - 3/4*h*k4 + 1/2*h*k5)
        weightk2 = 9.0/8;
        weightk3 = -3.0/8;
        weightk4 = -3.0/4;
        double weightk5 = 1.0/2;

        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep*1/2);
        status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
                .addScaled(k2.v, store.timeStep * weightk2)
                .addScaled(k3.v, store.timeStep * weightk3)
                .addScaled(k4.v, store.timeStep * weightk4)
                .addScaled(k5.v, store.timeStep * weightk5)
                .toImmutable());

        status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
                .addScaled(k2.a, store.timeStep * weightk2)
                .addScaled(k3.a, store.timeStep * weightk3)
                .addScaled(k4.a, store.timeStep * weightk4)
                .addScaled(k5.a, store.timeStep * weightk5)
                .toImmutable());

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k2.rv)
                        .multiply(store.timeStep * weightk2)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k3.rv)
                        .multiply(store.timeStep * weightk3)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k4.rv)
                        .multiply(store.timeStep * weightk4)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k5.rv)
                        .multiply(store.timeStep * weightk5)
                        .toImmutable())));

        status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
                .addScaled(k2.ra, store.timeStep * weightk2)
                .addScaled(k3.ra, store.timeStep * weightk3)
                .addScaled(k4.ra, store.timeStep * weightk4)
                .addScaled(k5.ra, store.timeStep * weightk5)
                .toImmutable());

        k6 = computeParameters(status2, store);

        //// Seventh position, k7 = f(t + h, y + 9/44*h*k1 - 9/11*h*k2 + 63/44*h*k3 + 18/11*h*k4 - 16/11*h*k6)
        weightk1 = 9.0/44;
        weightk2 = -9.0/11;
        weightk3 = 63.0/44;
        weightk4 = 18.0/11;
        double weightk6 = -16.0/11;

        status2 = status.clone();
        status2.setSimulationTime(status.getSimulationTime() + store.timeStep);
        status2.setRocketPosition(mutableCoordA.set(status.getRocketPosition())
                .addScaled(k1.v, store.timeStep * weightk1)
                .addScaled(k2.v, store.timeStep * weightk2)
                .addScaled(k3.v, store.timeStep * weightk3)
                .addScaled(k4.v, store.timeStep * weightk4)
                .addScaled(k6.v, store.timeStep * weightk6)
                .toImmutable());

        status2.setRocketVelocity(mutableCoordA.set(status.getRocketVelocity())
                .addScaled(k1.a, store.timeStep * weightk1)
                .addScaled(k2.a, store.timeStep * weightk2)
                .addScaled(k3.a, store.timeStep * weightk3)
                .addScaled(k4.a, store.timeStep * weightk4)
                .addScaled(k6.a, store.timeStep * weightk6)
                .toImmutable());

        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k1.rv)
                        .multiply(store.timeStep * weightk1)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k2.rv)
                        .multiply(store.timeStep * weightk2)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k3.rv)
                        .multiply(store.timeStep * weightk3)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k4.rv)
                        .multiply(store.timeStep * weightk4)
                        .toImmutable())));
        status2.setRocketOrientationQuaternion(status2.getRocketOrientationQuaternion()
                .multiplyLeft(Quaternion.rotation(mutableCoordB.set(k6.rv)
                        .multiply(store.timeStep * weightk6)
                        .toImmutable())));

        status2.setRocketRotationVelocity(mutableCoordA.set(status.getRocketRotationVelocity())
                .addScaled(k1.ra, store.timeStep * weightk1)
                .addScaled(k2.ra, store.timeStep * weightk2)
                .addScaled(k3.ra, store.timeStep * weightk3)
                .addScaled(k4.ra, store.timeStep * weightk4)
                .addScaled(k6.ra, store.timeStep * weightk6)
                .toImmutable());

        k7 = computeParameters(status2, store);

        //// Sum all together,  y(n+1) = y(n) + dt*(11/120*k1 + 27/40*k3 + 27/40*k4 - 4/15*k5 - 4/15*k6 + 11/120*k7)
        CoordinateIF deltaO;
        CoordinateIF deltaVCoord = mutableCoordA.clear()
                .addScaled(k1.a, 11.0/120)
                .addScaled(k3.a, 27.0/40)
                .addScaled(k4.a, 27.0/40)
                .addScaled(k5.a, -4.0/15)
                .addScaled(k6.a, -4.0/15)
                .addScaled(k7.a, 11.0/120)
                .multiply(store.timeStep)
                .toImmutable();
        CoordinateIF deltaPCoord = mutableCoordB.clear()
                .addScaled(k1.v, 11.0/120)
                .addScaled(k3.v, 27.0/40)
                .addScaled(k4.v, 27.0/40)
                .addScaled(k5.v, -4.0/15)
                .addScaled(k6.v, -4.0/15)
                .addScaled(k7.v, 11.0/120)
                .multiply(store.timeStep)
                .toImmutable();
        CoordinateIF deltaRCoord = mutableCoordC.clear()
                .addScaled(k1.ra, 11.0/120)
                .addScaled(k3.ra, 27.0/40)
                .addScaled(k4.ra, 27.0/40)
                .addScaled(k5.ra, -4.0/15)
                .addScaled(k6.ra, -4.0/15)
                .addScaled(k7.ra, 11.0/120)
                .multiply(store.timeStep)
                .toImmutable();
        deltaO = mutableCoordA.clear()
                .addScaled(k1.rv, 11.0/120)
                .addScaled(k3.rv, 27.0/40)
                .addScaled(k4.rv, 27.0/40)
                .addScaled(k5.rv, -4.0/15)
                .addScaled(k6.rv, -4.0/15)
                .addScaled(k7.rv, 11.0/120)
                .multiply(store.timeStep)
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
