package info.openrocket.core.componentanalysis;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Mutable;

import java.util.ArrayList;
import java.util.List;

public class CAParameters implements Cloneable {
	private final Mutable mutable = new Mutable();

	private final Rocket rocket;
	private final List<CAParametersListener> listeners = new ArrayList<>();

	private double theta;
	private final double initialTheta;
	private double aoa;
	private double mach;
	private double rollRate;

	public CAParameters(Rocket rocket, double initialTheta) {
		this.rocket = rocket;

		setTheta(initialTheta);
		this.initialTheta = this.theta;
		setAOA(0);
		setMach(Application.getPreferences().getDefaultMach());
		setRollRate(0);
	}

	public void addListener(CAParametersListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CAParametersListener listener) {
		listeners.remove(listener);
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		mutable.check();
		this.theta = theta;
		for (CAParametersListener listener : listeners) {
			listener.onThetaChanged(theta);
		}
	}

	public double getInitialTheta() {
		return initialTheta;
	}

	public double getAOA() {
		return aoa;
	}

	public void setAOA(double aoa) {
		mutable.check();
		this.aoa = aoa;
		for (CAParametersListener listener : listeners) {
			listener.onAOAChanged(aoa);
		}
	}

	public double getMach() {
		return mach;
	}

	public void setMach(double mach) {
		mutable.check();
		this.mach = mach;
		for (CAParametersListener listener : listeners) {
			listener.onMachChanged(mach);
		}
	}

	public double getRollRate() {
		return rollRate;
	}

	public void setRollRate(double rollRate) {
		mutable.check();
		this.rollRate = rollRate;
		for (CAParametersListener listener : listeners) {
			listener.onRollRateChanged(rollRate);
		}
	}

	public FlightConfiguration getSelectedConfiguration() {
		return rocket.getSelectedConfiguration();
	}

	public void immute() {
		mutable.immute();
	}

	public boolean isMutable() {
		return mutable.isMutable();
	}

	@Override
	public CAParameters clone() {
		try {
			return (CAParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public interface CAParametersListener {
		void onThetaChanged(double theta);
		void onAOAChanged(double aoa);
		void onMachChanged(double mach);
		void onRollRateChanged(double rollRate);
	}
}
