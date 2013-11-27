package net.sf.openrocket.gui.figure3d.photo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Trackball {
	
	private static final Logger log = LoggerFactory.getLogger(Trackball.class);
	
	private double pitch, yaw, roll;
	
	public Trackball() {
		
	}
	
	public void setPitchYawRoll(final double pitch, final double yaw, final double roll) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.roll = roll;
	}
	
	public double getPitch() {
		return pitch;
	}
	
	public double getYaw() {
		return yaw;
	}
	
	public double getRoll() {
		return roll;
	}
	
	public void swipe
			(final double p1x, final double p1y, final double p2x, final double p2y, final double vAz, final double vAlt) {
		
	}
	
}
