package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Mutable;
import info.openrocket.swing.gui.scalefigure.RocketPanel;

public class ComponentAnalysisParameters implements Cloneable {
	private final Mutable mutable = new Mutable();

	private final Rocket rocket;
	private final RocketPanel rocketPanel;

	private double theta;
	private final double initialTheta;
	private double aoa;
	private double mach;
	private double rollRate;

	public ComponentAnalysisParameters(Rocket rocket, RocketPanel rocketPanel) {
		this.rocket = rocket;
		this.rocketPanel = rocketPanel;

		setTheta(rocketPanel.getFigure().getRotation());
		this.initialTheta = this.theta;
		setAOA(0);
		setMach(Application.getPreferences().getDefaultMach());
		setRollRate(0);
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		mutable.check();
		this.theta = theta;
		this.rocketPanel.setCPTheta(theta);
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
		this.rocketPanel.setCPAOA(aoa);
	}

	public double getMach() {
		return mach;
	}

	public void setMach(double mach) {
		mutable.check();
		this.mach = mach;
		this.rocketPanel.setCPMach(mach);
	}

	public double getRollRate() {
		return rollRate;
	}

	public void setRollRate(double rollRate) {
		mutable.check();
		this.rollRate = rollRate;
		this.rocketPanel.setCPRoll(rollRate);
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
	public ComponentAnalysisParameters clone() {
		try {
			return (ComponentAnalysisParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
