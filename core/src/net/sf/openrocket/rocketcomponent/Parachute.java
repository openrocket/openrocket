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
		this.lineLength = 0.3;
	}
	
	
	public double getDiameter() {
		return diameter;
	}
	
	public void setDiameter(double d) {
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
		super.loadFromPreset(preset);
	}


	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.PARACHUTE;
	}
	
}
