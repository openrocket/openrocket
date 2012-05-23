package net.sf.openrocket.android;

import net.sf.openrocket.R;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.unit.UnitGroup;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class PreferencesActivity extends SherlockPreferenceActivity 
{

	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource(R.xml.preferences);
		
		PreferenceManager prefManager = getPreferenceManager();
		{
			String key = getApplication().getResources().getString(R.string.PreferenceUnitLengthOption);
			Preference pref = prefManager.findPreference(key);
			new UnitPreferenceListener(pref, UnitGroup.UNITS_LENGTH, "Current value ");
			
		}
		{
			String key = getApplication().getResources().getString(R.string.PreferenceUnitMassOption);
			Preference pref = prefManager.findPreference(key);
			new UnitPreferenceListener(pref, UnitGroup.UNITS_MASS, "Current value ");
			
		}
		{
			String key = getApplication().getResources().getString(R.string.PreferenceUnitVelocityOption);
			Preference pref = prefManager.findPreference(key);
			new UnitPreferenceListener(pref, UnitGroup.UNITS_VELOCITY, "Current value ");
			
		}
		{
			String key = getApplication().getResources().getString(R.string.PreferenceUnitDistanceOption);
			Preference pref = prefManager.findPreference(key);
			new UnitPreferenceListener(pref, UnitGroup.UNITS_DISTANCE, "Current value ");
			
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * This method is to be called from Application setup to pull the saved preference
	 * values into the various datastructures used in OpenRocket.
	 * This method is located in this class because it is probably best to have as much
	 * of the code in the same place as possible.
	 * @param sharedPreferences
	 */
	public static void initializePreferences( android.app.Application app, SharedPreferences sharedPreferences ) {
		
		String unitLength = app.getResources().getString(R.string.PreferenceUnitLengthOption);
		String len = sharedPreferences.getString(unitLength, "cm");
		UnitGroup.UNITS_LENGTH.setDefaultUnit( len );
		
		AndroidLogWrapper.d(PreferencesActivity.class, "Chaning mass");
		String unitMass = app.getResources().getString(R.string.PreferenceUnitMassOption);
		String mass = sharedPreferences.getString(unitMass, "g");
		UnitGroup.UNITS_MASS.setDefaultUnit( mass );
		
		String unitVelocity = app.getResources().getString(R.string.PreferenceUnitVelocityOption);
		String velocity = sharedPreferences.getString(unitVelocity, "m/s");
		UnitGroup.UNITS_VELOCITY.setDefaultUnit( velocity );
		
		String unitDistance = app.getResources().getString(R.string.PreferenceUnitDistanceOption);
		String distance = sharedPreferences.getString(unitDistance, "m");
		UnitGroup.UNITS_DISTANCE.setDefaultUnit( distance );
		
	}

	private class UnitPreferenceListener implements Preference.OnPreferenceChangeListener {

		private UnitGroup matchedGroup;
		private String message;
		private Preference pref;
		
		private UnitPreferenceListener( Preference pref, UnitGroup unit, String message) {
			this.pref = pref;
			this.matchedGroup = unit;
			this.message = message;
			pref.setSummary(message + unit.getDefaultUnit().getUnit());
			// todo figure out how to setSummary - need to get initial value.
			pref.setOnPreferenceChangeListener(this);
		}
		
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			matchedGroup.setDefaultUnit((String)newValue);
			preference.setSummary(message + newValue);
			return true;
		}
		
	}
	
}
