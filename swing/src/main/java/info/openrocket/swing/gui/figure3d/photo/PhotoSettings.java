package info.openrocket.swing.gui.figure3d.photo;

import info.openrocket.swing.gui.figure3d.photo.exhaust.FlameRenderer.FlameSettings;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Mountains;
import info.openrocket.core.util.AbstractChangeSource;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.MathUtil;

public class PhotoSettings extends AbstractChangeSource implements FlameSettings {
	private double roll = 3.14;
	private double yaw = 0;
	private double pitch = 2.05;
	private double advance = 0;
	
	private double viewAlt = -0.23;
	private double viewAz = 2.08;
	private double viewDistance = .44;
	private double fov = 1.4;
	
	private double lightAlt = .35;
	private double lightAz = -1;
	private ORColor sunlight = new ORColor(255, 255, 255);
	private double ambiance = .3f;
	
	private ORColor skyColor = new ORColor(55, 95, 155);
	private double skyColorOpacity = 1.0;
	
	
	private boolean motionBlurred = false;
	private boolean flame = false;
	private ORColor flameColor = new ORColor(255, 100, 50);
	private boolean smoke = false;
	private ORColor smokeColor = new ORColor(230, 230, 230, 102);
	private boolean sparks = false;
	private double exhaustScale = 1.0;
	private double flameAspectRatio = 1.0;
	
	private double sparkConcentration = 0.2;
	private double sparkWeight = 0;
	
	private Sky sky = Mountains.instance;
	
	public double getRoll() {
		return roll;
	}
	
	public void setRoll(double roll) {
		this.roll = roll;
		fireChangeEvent();
	}
	
	public double getYaw() {
		return yaw;
	}
	
	public void setYaw(double yaw) {
		this.yaw = yaw;
		fireChangeEvent();
	}
	
	public double getPitch() {
		return pitch;
	}
	
	public void setPitch(double pitch) {
		this.pitch = pitch;
		fireChangeEvent();
	}

	public double getAdvance() {
		return advance;
	}
	
	public void setAdvance(double advance) {
		this.advance = advance;
		fireChangeEvent();
	}
	
	public double getViewAlt() {
		return viewAlt;
	}
	
	public void setViewAltAz(double viewAlt, double viewAz){
		this.viewAz = viewAz;
		this.viewAlt = MathUtil.clamp(viewAlt, -Math.PI/2, Math.PI/2);
		fireChangeEvent();
	}
	public void setViewAlt(double viewAlt) {
		this.viewAlt = MathUtil.clamp(viewAlt, -Math.PI/2, Math.PI/2);
		fireChangeEvent();
	}
	
	public double getViewAz() {
		return viewAz;
	}
	
	public void setViewAz(double viewAz) {
		this.viewAz = viewAz;
		fireChangeEvent();
	}
	
	public double getViewDistance() {
		return viewDistance;
	}
	
	public void setViewDistance(double viewDistance) {
		this.viewDistance = Math.max(viewDistance, 0);
		fireChangeEvent();
	}
	
	public double getFov() {
		return fov;
	}
	
	public void setFov(double fov) {
		this.fov = MathUtil.clamp(fov, 0, Math.PI);
		fireChangeEvent();
	}
	
	public double getLightAlt() {
		return lightAlt;
	}
	
	public void setLightAlt(double lightAlt) {
		this.lightAlt = lightAlt;
		fireChangeEvent();
	}
	
	public double getLightAz() {
		return lightAz;
	}
	
	public void setLightAz(double lightAz) {
		this.lightAz = lightAz;
		fireChangeEvent();
	}
	
	public boolean isMotionBlurred() {
		return motionBlurred;
	}
	
	public void setMotionBlurred(boolean motionBlurred) {
		this.motionBlurred = motionBlurred;
		fireChangeEvent();
	}
	
	public boolean isFlame() {
		return flame;
	}
	
	public void setFlame(boolean flame) {
		this.flame = flame;
		fireChangeEvent();
	}
	
	public boolean isSmoke() {
		return smoke;
	}
	
	public void setSmoke(boolean smoke) {
		this.smoke = smoke;
		fireChangeEvent();
	}
	
	public ORColor getSunlight() {
		return sunlight;
	}
	
	public void setSunlight(ORColor sunlight) {
		this.sunlight = sunlight;
		fireChangeEvent();
	}
	
	public double getAmbiance() {
		return ambiance;
	}
	
	public void setAmbiance(double ambiance) {
		this.ambiance = ambiance;
		fireChangeEvent();
	}
	
	public ORColor getSkyColor() {
		return skyColor;
	}
	
	public void setSkyColor(ORColor skyColor) {
		this.skyColor = skyColor;
		this.skyColorOpacity = skyColor.getAlpha() / 255f;
		fireChangeEvent();
	}

	public double getSkyColorOpacity() {
		return skyColorOpacity;
	}

	public void setSkyColorOpacity(double skyColorOpacity) {
		this.skyColorOpacity = skyColorOpacity;
		skyColor.setAlpha((int) (skyColorOpacity * 255));
		fireChangeEvent();
	}

	public ORColor getFlameColor() {
		return flameColor;
	}
	
	public void setFlameColor(ORColor flameColor) {
		this.flameColor = flameColor;
		fireChangeEvent();
	}
	
	public ORColor getSmokeColor() {
		return smokeColor;
	}
	
	public void setSmokeColor(ORColor smokeColor) {
		this.smokeColor = smokeColor;
		fireChangeEvent();
	}

	
	public void setSmokeAlpha(double alpha) {
		smokeColor.setAlpha((int) (alpha * 255));
		fireChangeEvent();
	}

	public double getSmokeOpacity() {
		return smokeColor.getAlpha() / 255f;
	}

	public void setSmokeOpacity(double smokeOpacity) {
		setSmokeAlpha(smokeOpacity);
	}
	
	public boolean isSparks() {
		return sparks;
	}
	
	public void setSparks(boolean sparks) {
		this.sparks = sparks;
		fireChangeEvent();
	}
	
	public double getExhaustScale() {
		return exhaustScale;
	}
	
	public void setExhaustScale(double exhaustScale) {
		this.exhaustScale = exhaustScale;
		fireChangeEvent();
	}
	
	public double getFlameAspectRatio() {
		return flameAspectRatio;
	}
	
	public void setFlameAspectRatio(double flameAspectRatio) {
		this.flameAspectRatio = flameAspectRatio;
		fireChangeEvent();
	}
	
	public Sky getSky() {
		return sky;
	}
	
	public void setSky(Sky sky) {
		this.sky = sky;
		fireChangeEvent();
	}

	public double getSparkConcentration() {
		return sparkConcentration;
	}

	public void setSparkConcentration(double sparkConcentration) {
		this.sparkConcentration = sparkConcentration;
		fireChangeEvent();
	}

	public double getSparkWeight() {
		return sparkWeight;
	}

	public void setSparkWeight(double sparkWeight) {
		this.sparkWeight = sparkWeight;
		fireChangeEvent();
	}
}