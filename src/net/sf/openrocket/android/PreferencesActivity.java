package net.sf.openrocket.android;

import net.sf.openrocket.R;
import android.os.Bundle;

public class PreferencesActivity extends android.preference.PreferenceActivity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource(R.xml.preferences);
	}
	
}
