package net.sf.openrocket.android;

import net.sf.openrocket.R;
import net.sf.openrocket.unit.UnitGroup;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PreferencesActivity extends android.preference.PreferenceActivity 
implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		initializePreferences(getApplication(), PreferenceManager.getDefaultSharedPreferences(this));
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

	@Override
	protected void onStop() {
		initializePreferences(getApplication(), PreferenceManager.getDefaultSharedPreferences(this));
		super.onStop();
	}
	
	
	
}
