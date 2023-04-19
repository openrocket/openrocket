package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.MathUtil;

public class Parachute extends RecoveryDevice {
	private static final Translator trans = Application.getTranslator();
	private final double DEFAULT_DIAMETER = 0.3;
	private double diameter;
	public static double DEFAULT_CD = 0.8;
	private final Material DEFAULT_LINE_MATERIAL;
	private Material lineMaterial;
	private final int DEFAULT_LINE_COUNT = 6;
	private int lineCount;
	private final double DEFAULT_LINE_LENGTH = 0.3;
	private double lineLength;

	public Parachute() {
		this.diameter = DEFAULT_DIAMETER;
		lineCount = DEFAULT_LINE_COUNT;
		lineLength = DEFAULT_LINE_LENGTH;
		this.lineMaterial = Application.getPreferences().getDefaultComponentMaterial(Parachute.class, Material.Type.LINE);
		DEFAULT_LINE_MATERIAL = lineMaterial;
		super.displayOrder_side = 11;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 9;		// Order for displaying the component in the 2D back view
	}

	@Override
	public String getComponentName() {
		//// Parachute
		return trans.get("Parachute.Parachute");
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
	public double getComponentCD(double mach) {
		return cd; // TODO: HIGH:  Better parachute CD estimate?
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
		if (getLineCount() != 0) {
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
			clearPreset();
		} else {
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
		}
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
		if (getLineCount() != 0) {
			clearPreset();
			fireComponentChangeEvent(ComponentChangeEvent.MASS_CHANGE);
		}
		else
			fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	@Override
	public double getComponentMass() {
		return super.getComponentMass() +
				getLineCount() * getLineLength() * getLineMaterial().getDensity();
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

		// SUBSTITUTE preset parachute values for existing component values
		//	//	Set preset parachute description
		if (preset.has(ComponentPreset.DESCRIPTION)) {
			String temporaryName = preset.get(ComponentPreset.DESCRIPTION);
			int size = temporaryName.length();
			if (size > 0) {
				this.name = preset.get(ComponentPreset.DESCRIPTION);
			} else {
				this.name = getComponentName();
			}
		} else {
			this.name = getComponentName();
		}
		//	//	Set preset parachute diameter
		if ((preset.has(ComponentPreset.DIAMETER)) && preset.get(ComponentPreset.DIAMETER) > 0) {
			this.diameter = preset.get(ComponentPreset.DIAMETER);
		} else {
			this.diameter = DEFAULT_DIAMETER;
		}
		//	//	Set preset parachute drag coefficient
		 if ((preset.has(ComponentPreset.CD)) && preset.get(ComponentPreset.CD) > 0){
		 		cdAutomatic = false;
		 		cd = preset.get(ComponentPreset.CD);
		 } else {
			 cdAutomatic = true;
			 cd = Parachute.DEFAULT_CD;
		 }
		//	//	Set preset parachute line count
		if ((preset.has(ComponentPreset.LINE_COUNT)) && preset.get(ComponentPreset.LINE_COUNT) > 0) {
			this.lineCount = preset.get(ComponentPreset.LINE_COUNT);
		} else {
			this.lineCount = DEFAULT_LINE_COUNT;
		}
		//	//	Set preset parachute line length
		if ((preset.has(ComponentPreset.LINE_LENGTH)) && preset.get(ComponentPreset.LINE_LENGTH) > 0) {
			this.lineLength = preset.get(ComponentPreset.LINE_LENGTH);
		} else {
			this.lineLength = DEFAULT_LINE_LENGTH;
		}
		//	//	Set preset parachute line material
			//	NEED a better way to set preset if field is empty ----
		if ((preset.has(ComponentPreset.LINE_MATERIAL))) {
			String lineMaterialEmpty = preset.get(ComponentPreset.LINE_MATERIAL).toString();
			int count = lineMaterialEmpty.length();
			if (count > 12 ) {
				this.lineMaterial = preset.get(ComponentPreset.LINE_MATERIAL);
			} else {
				this.lineMaterial = DEFAULT_LINE_MATERIAL;
			}
		} else {
			this.lineMaterial = DEFAULT_LINE_MATERIAL;
		}

		//	//	Set preset parachute packed length
		if ((preset.has(ComponentPreset.PACKED_LENGTH)) && preset.get(ComponentPreset.PACKED_LENGTH) > 0) {
			length = preset.get(ComponentPreset.PACKED_LENGTH);
		}
		//	// Set preset parachute packed diameter
		if ((preset.has(ComponentPreset.PACKED_DIAMETER)) && preset.get(ComponentPreset.PACKED_DIAMETER) > 0) {
			radius = preset.get(ComponentPreset.PACKED_DIAMETER) / 2;
		}
		//	// Size parachute packed diameter within parent inner diameter
		if (length > 0 && radius > 0) {
			setRadiusAutomatic(true);
		}

		// SUBSTITUTE / ACTIVATE Override Mass Preset
		if ((preset.has(ComponentPreset.MASS))&& (preset.get(ComponentPreset.MASS)) > 0){
			this.overrideMass = (preset.get(ComponentPreset.MASS));
			massOverridden = true;
		} else {
			this.overrideMass = 0;
			massOverridden = false;
		}

		super.loadFromPreset(preset);
	}

	@Override
	public Type getPresetType() {
		return Type.PARACHUTE;
	}
}
