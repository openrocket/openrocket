package net.sf.openrocket.formatting;

import java.util.Map;

import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.rocketcomponent.Rocket;

/**
 * A class that allows substitution to occur in a text string.
 */
@Plugin
public interface RocketSubstitutor {
	
	public boolean containsSubstitution(String str);
	
	public String substitute(String str, Rocket rocket, String configId);
	
	public Map<String, String> getDescriptions();
	
}
