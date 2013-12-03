package net.sf.openrocket.gui.figure3d.photo;

import net.sf.openrocket.gui.figure3d.photo.exhaust.FlameRenderer.FlameSettings;
import net.sf.openrocket.gui.figure3d.photo.sky.Sky;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Mountains;
import net.sf.openrocket.util.AbstractChangeSource;
import net.sf.openrocket.util.Color;

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
	private Color sunlight = new Color(255, 255, 255);
	private double ambiance = .3f;
	
	private Color skyColor = new Color(55, 95, 155);
	
	
	private boolean motionBlurred = false;
	private boolean flame = false;
	private Color flameColor = new Color(255, 100, 50);
	private boolean smoke = true;
	private Color smokeColor = new Color(230, 230, 230, 204);
	private boolean sparks = false;
	private double exhaustScale = 1.0;
	private double flameAspectRatio = 1.0;
	
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
	
	public void setPitchYawRoll(double pitch, double yaw, double roll) {
		this.roll = roll;
		this.yaw = yaw;
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
	
	public void setViewAlt(double viewAlt) {
		this.viewAlt = viewAlt;
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
		this.viewDistance = viewDistance;
		fireChangeEvent();
	}
	
	public double getFov() {
		return fov;
	}
	
	public void setFov(double fov) {
		this.fov = fov;
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
	
	public Color getSunlight() {
		return sunlight;
	}
	
	public void setSunlight(Color sunlight) {
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
	
	public Color getSkyColor() {
		return skyColor;
	}
	
	public void setSkyColor(Color skyColor) {
		this.skyColor = skyColor;
		fireChangeEvent();
	}
	
	public Color getFlameColor() {
		return flameColor;
	}
	
	public void setFlameColor(Color flameColor) {
		this.flameColor = flameColor;
		fireChangeEvent();
	}
	
	public Color getSmokeColor() {
		return smokeColor;
	}
	
	public void setSmokeColor(Color smokeColor) {
		smokeColor.setAlpha(this.smokeColor.getAlpha());
		this.smokeColor = smokeColor;
		fireChangeEvent();
	}
	
	public double getSmokeAlpha() {
		return smokeColor.getAlpha() / 255f;
	}
	
	public void setSmokeAlpha(double alpha) {
		smokeColor.setAlpha((int) (alpha * 255));
		fireChangeEvent();
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
}