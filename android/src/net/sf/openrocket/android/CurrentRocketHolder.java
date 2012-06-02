package net.sf.openrocket.android;


public abstract class CurrentRocketHolder {

	private static CurrentRocket currentRocket = new CurrentRocket();
	
	public static CurrentRocket getCurrentRocket() {
		return currentRocket;
	}
	
}
