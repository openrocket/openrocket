package info.openrocket.core.formatting;

import java.util.Set;

import com.google.inject.Inject;

import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;

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
