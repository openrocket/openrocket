package info.openrocket.core.formatting;

import java.util.Map;

import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;

/**
 * A class that allows substitution to occur in a text string.
 */
@Plugin
public interface RocketSubstitutor {

	public boolean containsSubstitution(String str);

	public String substitute(String str, Rocket rocket, FlightConfigurationId configId);

	public Map<String, String> getDescriptions();

}
