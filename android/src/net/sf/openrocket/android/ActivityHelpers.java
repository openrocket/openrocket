package net.sf.openrocket.android;

import net.sf.openrocket.android.motor.MotorHierarchicalBrowser;
import android.app.Activity;
import android.content.Intent;

public abstract class ActivityHelpers {

	
	public static void browseMotors( Activity parent ) {
		Intent i = new Intent(parent, MotorHierarchicalBrowser.class);
		parent.startActivity(i);
		
	}

	public static void startPreferences( Activity parent ) {
		Intent intent = new Intent(parent, PreferencesActivity.class);
		parent.startActivity(intent);

	}
}
