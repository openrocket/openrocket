package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public class Parachute extends RecoveryDevice {
	private static final Translator trans = Application.getTranslator();
	
	public static final double DEFAULT_CD = 0.8;
	
	private double diameter;
	
	private Material lineMaterial;
	private int lineCount = 6;
	private double lineLength = 0.3;
	
	
	public Parachute() {
		this.diameter = 0.3;
		this.lineMaterial = Application.getPreferences().getDefaultComponentMaterial(Parachute.class, Material.Type.LINE);
		super.displayOrder_side = 11;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 9;		// Order for displaying the component in the 2D back view
	}
	
	
	public double getDiameter() {
		return diameter;
	}
	
	public void setDiameter(double d) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Parachute) {
				((Parachute) listener).setDiameter(d);
			}
		}

		if (MathUtil.equals(this.diameter, d))
			return;
		this.diameter = d;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	public final Material getLineMaterial() {
		return lineMaterial;
	}
	
	public final void setLineMaterial(Material mat) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Parachute) {
				((Parachute) listener).setLineMaterial(mat);
			}
		}

		if (mat.getType() != Material.Type.LINE) {
			throw new IllegalArgumentException("Attempted to set non-line material " + mat);
		}
		if (mat.equals(lineMaterial))
			return;
		this.lineMaterial = mat;
		if (getLineCount() != 0)
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	public final int getLineCount() {
		return lineCount;
	}
	
	public final void setLineCount(int n) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Parachute) {
				((Parachute) listener).setLineCount(n);
			}
		}

		if (this.lineCount == n)
			return;
		this.lineCount = n;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
	}
	
	public final double getLineLength() {
		return lineLength;
	}
	
	public final void setLineLength(double length) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Parachute) {
				((Parachute) listener).setLineLength(length);
			}
		}

		if (MathUtil.equals(this.lineLength, length))
			return;
		this.lineLength = length;
		if (getLineCount() != 0)
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	
	@Override
	public double getComponentCD(double mach) {
		return DEFAULT_CD; // TODO: HIGH:  Better parachute CD estimate?
	}
	
	@Override
	public double getArea() {
		return Math.PI * MathUtil.pow2(diameter / 2);
	}
	
	public void setArea(double area) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof Parachute) {
				((Parachute) listener).setArea(area);
			}
		}

		if (MathUtil.equals(getArea(), area))
			return;
		diameter = MathUtil.safeSqrt(area / Math.PI) * 2;
		clearPreset();
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public double getComponentMass() {
		return super.getComponentMass() +
				getLineCount() * getLineLength() * getLineMaterial().getDensity();
	}
	
	@Override
	public String getComponentName() {
		//// Parachute
		return trans.get("Parachute.Parachute");
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}


	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		if( preset.has( ComponentPreset.DIAMETER )) {
			this.diameter = preset.get( ComponentPreset.DIAMETER );
		}
		if( preset.has( ComponentPreset.LINE_COUNT )) {
			this.lineCount = preset.get( ComponentPreset.LINE_COUNT );
		}
		if( preset.has( ComponentPreset.LINE_LENGTH )) {
			this.lineLength = preset.get( ComponentPreset.LINE_LENGTH );
		}
		if( preset.has( ComponentPreset.LINE_MATERIAL )) {
			this.lineMaterial = preset.get( ComponentPreset.LINE_MATERIAL );
		}

		// BEGIN Activate Override Mass
		if (preset.has(ComponentPreset.MASS)) {
			this.overrideMass = (preset.get(ComponentPreset.MASS));
			if (overrideMass > 0.0) {       // If the preset mass value > 0
				massOverridden = true;
			} else {                        // If the preset mass value <= 0
				this.overrideMass = 0;
				massOverridden = false;
			}
		} else {                            // If the preset mass value field does not exist
			this.overrideMass = 0;
			massOverridden = false;
		}
		// END Activate Preset Override Mass

		//BEGIN Substitute parachute description for component name
		if (preset.has(ComponentPreset.DESCRIPTION)) {
			String temporaryName = preset.get(ComponentPreset.DESCRIPTION);
			int size = temporaryName.length();
			if (size > 0) {
				this.name = preset.get(ComponentPreset.DESCRIPTION);
			}
		}
		//END Substitute parachute description for component name

		super.loadFromPreset(preset);
	}


	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.PARACHUTE;
	}
	
}
