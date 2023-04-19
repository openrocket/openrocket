package net.sf.openrocket.formatting;

import java.util.Set;

import com.google.inject.Inject;

import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;

public class RocketDescriptorImpl implements RocketDescriptor {
	
	@Inject
	private Set<RocketSubstitutor> substitutors;
	
	@Override
	public String format(final Rocket rocket, final FlightConfigurationId configId) {
		String name = rocket.getFlightConfiguration(configId).getName();
		return format(name, rocket, configId);
	}
	
	@Override
	public String format(String name, final Rocket rocket, final FlightConfigurationId configId) {
		for (RocketSubstitutor s : substitutors) {
			while (s.containsSubstitution(name)) {
				name = s.substitute(name, rocket, configId);
			}
		}
		
		return name;
	}
	
}
