package net.sf.openrocket.plugin.example;

import java.awt.Component;

public class AirStartSimulationExtension extends OpenRocketSimulationListener {
	
	@Override
	public String getName() {
		return "Air-start";
	}
	
	@Override
	public String[] getMenuPosition() {
		return null;
	}
	
	@Override
	public void loadFromXML(Object... objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void saveToXML(Object... objects) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isConfigurable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Component getConfigurationComponent() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
