package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public class Streamer extends RecoveryDevice {
	
	public static final double DEFAULT_CD = 0.6;
	
	public static final double MAX_COMPUTED_CD = 0.4;
	
	private static final Translator trans = Application.getTranslator();

	private double stripLength;
	private double stripWidth;
	
	
	public Streamer() {
		this.stripLength = 0.5;
		this.stripWidth = 0.05;
		super.displayOrder_side = 10;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 8;		// Order for displaying the component in the 2D back view
	}
	
	
	public double getStripLength() {
		return stripLength;
	}
	
	public void setStripLength(double stripLength) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Streamer) {
				((Streamer) listener).setStripLength(stripLength);
			}
		}

		if (MathUtil.equals(this.stripLength, stripLength))
			return;
		this.stripLength = stripLength;
//		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public double getStripWidth() {
		return stripWidth;
	}
	
	public void setStripWidth(double stripWidth) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Streamer) {
				((Streamer) listener).setStripWidth(stripWidth);
			}
		}

		if (MathUtil.equals(this.stripWidth, stripWidth))
			return;
		this.stripWidth = stripWidth;

		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public double getAspectRatio() {
		if (stripWidth > 0.0001)
			return stripLength / stripWidth;
		return 1000;
	}
	
	public void setAspectRatio(double ratio) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Streamer) {
				((Streamer) listener).setAspectRatio(ratio);
			}
		}

		if (MathUtil.equals(getAspectRatio(), ratio))
			return;
		
		ratio = Math.max(ratio, 0.01);
		double area = getArea();
		stripWidth = MathUtil.safeSqrt(area / ratio);
		stripLength = ratio * stripWidth;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public double getArea() {
		return stripWidth * stripLength;
	}
	
	public void setArea(double area) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Streamer) {
				((Streamer) listener).setArea(area);
			}
		}

		if (MathUtil.equals(getArea(), area))
			return;
		
		double ratio = Math.max(getAspectRatio(), 0.01);
		stripWidth = MathUtil.safeSqrt(area / ratio);
		stripLength = ratio * stripWidth;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	

	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.STREAMER;
	}


	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if ( preset.has(ComponentPreset.LENGTH)) {
			this.stripLength = preset.get(ComponentPreset.LENGTH);
		}
		if ( preset.has(ComponentPreset.WIDTH)) {
			this.stripWidth = preset.get(ComponentPreset.WIDTH);
		}
		//	Set Cd value when preset is selected after manual Cd change
		double density = this.getMaterial().getDensity();
		double cd;
		cd = 0.034 * ((density + 0.025) / 0.105) * (stripLength + 1) / stripLength;
		cd = MathUtil.min(cd, MAX_COMPUTED_CD);
 		this.cd = cd;
		this.cdAutomatic = true;

		super.loadFromPreset(preset);
		// Fix the length to the stripWidth since RocketComponent assigns ComponentPreset.LENGTH to length.
		this.length = this.stripWidth;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}


	@Override
	public double getComponentCD(double mach) {
		double density = this.getMaterial().getDensity();
		double cd;	
		cd = 0.034 * ((density + 0.025) / 0.105) * (stripLength + 1) / stripLength;
		cd = MathUtil.min(cd, MAX_COMPUTED_CD);
		return cd;
	}
	
	@Override
	public String getComponentName() {
		return trans.get ("Streamer.Streamer");
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}
}
