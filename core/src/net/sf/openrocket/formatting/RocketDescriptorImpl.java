package net.sf.openrocket.formatting;

import java.util.Set;

import net.sf.openrocket.rocketcomponent.Rocket;

import com.google.inject.Inject;

public class RocketDescriptorImpl implements RocketDescriptor {
	
	@Inject
	private Set<RocketSubstitutor> substitutors;
	
	@Override
	public String format(Rocket rocket, String configId) {
		String name = rocket.getFlightConfigurationName(configId);
		return format(name, rocket, configId);
	}
	
	@Override
	public String format(String name, Rocket rocket, String configId) {
		for (RocketSubstitutor s : substitutors) {
			while (s.containsSubstitution(name)) {
				name = s.substitute(name, rocket, configId);
			}
		}
		
		return name;
	}
	
}
