package info.openrocket.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.DoubleUnaryOperator;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.Motor.Type;
import info.openrocket.core.util.BugException;
import org.junit.jupiter.api.Test;

class MotorCorrelationTest {

	private static final double EPSILON = 1e-9;

	@Test
	void identicalMotorsHaveFullSimilarity() {
		StubMotor motor = constantMotor(10.0, 1.5);
		assertEquals(1.0, MotorCorrelation.similarity(motor, motor), EPSILON);
		assertEquals(1.0, MotorCorrelation.crossCorrelation(motor, motor), EPSILON);
	}

	@Test
	void averageThrustDifferenceLimitsSimilarity() {
		StubMotor stronger = constantMotor(10.0, 1.0);
		StubMotor weaker = constantMotor(5.0, 1.0);

		double similarity = MotorCorrelation.similarity(stronger, weaker);
		assertEquals(0.5, similarity, EPSILON);
	}

	@Test
	void crossCorrelationHandlesZeroThrustCurves() {
		StubMotor zeroA = constantMotor(0.0, 1.0);
		StubMotor zeroB = constantMotor(0.0, 1.0);

		assertEquals(1.0, MotorCorrelation.crossCorrelation(zeroA, zeroB), EPSILON);
		assertEquals(1.0, MotorCorrelation.similarity(zeroA, zeroB), EPSILON);
	}

	@Test
	void crossCorrelationRejectsNegativeThrustValues() {
		StubMotor negative = new StubMotor(t -> -1.0, 5.0, 1.0, 5.0);
		StubMotor positive = constantMotor(5.0, 1.0);

		assertThrows(BugException.class, () -> MotorCorrelation.crossCorrelation(negative, positive));
	}

	private static StubMotor constantMotor(double thrustLevel, double burnTime) {
		double totalImpulse = thrustLevel * burnTime;
		return new StubMotor(t -> t <= burnTime ? thrustLevel : 0.0,
				thrustLevel, burnTime, totalImpulse);
	}

	private static class StubMotor implements Motor {
		private final DoubleUnaryOperator thrustFunction;
		private final double averageThrust;
		private final double burnTime;
		private final double totalImpulse;

		StubMotor(DoubleUnaryOperator thrustFunction, double averageThrust, double burnTime, double totalImpulse) {
			this.thrustFunction = thrustFunction;
			this.averageThrust = averageThrust;
			this.burnTime = burnTime;
			this.totalImpulse = totalImpulse;
		}

		@Override
		public Type getMotorType() {
			return Type.SINGLE;
		}

		@Override
		public String getCode() {
			return "stub";
		}

		@Override
		public String getCommonName() {
			return "Stub Motor";
		}

		@Override
		public String getCommonName(double delay) {
			return getCommonName();
		}

		@Override
		public String getDesignation() {
			return "Stub";
		}

		@Override
		public String getDesignation(double delay) {
			return getDesignation();
		}

		@Override
		public String getDescription() {
			return "Test motor";
		}

		@Override
		public double getDiameter() {
			return 0.0;
		}

		@Override
		public double getLength() {
			return 0.0;
		}

		@Override
		public String getDigest() {
			return "digest";
		}

		@Override
		public double getLaunchCGx() {
			return 0.0;
		}

		@Override
		public double getBurnoutCGx() {
			return 0.0;
		}

		@Override
		public double getLaunchMass() {
			return 0.0;
		}

		@Override
		public double getBurnoutMass() {
			return 0.0;
		}

		@Override
		public double getBurnTimeEstimate() {
			return burnTime;
		}

		@Override
		public double getAverageThrustEstimate() {
			return averageThrust;
		}

		@Override
		public double getMaxThrustEstimate() {
			return averageThrust;
		}

		@Override
		public double getTotalImpulseEstimate() {
			return totalImpulse;
		}

		@Override
		public double getBurnTime() {
			return burnTime;
		}

		@Override
		public double getThrust(double motorTime) {
			return thrustFunction.applyAsDouble(motorTime);
		}

		@Override
		public double getTotalMass(double motorTime) {
			return 0.0;
		}

		@Override
		public double getPropellantMass(Double motorTime) {
			return 0.0;
		}

		@Override
		public double getCMx(double motorTime) {
			return 0.0;
		}

		@Override
		public double getUnitIxx() {
			return 0.0;
		}

		@Override
		public double getUnitIyy() {
			return 0.0;
		}

		@Override
		public double getUnitIzz() {
			return 0.0;
		}
	}
}
