package net.sf.openrocket.rocketcomponent;

public interface FlightConfigurable<T> {

	public T getFlightConfiguration( String configId );
	
	public void setFlightConfiguration( String configId, T config );
	
	public void cloneFlightConfiguration( String oldConfigId, String newConfigId );
	
	public T getDefaultFlightConfiguration();
	
	public void setDefaultFlightConfiguration( T config );
	
}
