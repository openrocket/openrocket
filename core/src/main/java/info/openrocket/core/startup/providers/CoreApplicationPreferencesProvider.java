package info.openrocket.core.startup.providers;

import info.openrocket.core.preferences.ApplicationPreferences;

import com.google.inject.Provider;

/**
 * Provider for ApplicationPreferences in core-only applications.
 * This provides a basic implementation suitable for non-GUI applications.
 */
public class CoreApplicationPreferencesProvider implements Provider<ApplicationPreferences> {
	
	@Override
	public ApplicationPreferences get() {
		return new ApplicationPreferences();
	}
}