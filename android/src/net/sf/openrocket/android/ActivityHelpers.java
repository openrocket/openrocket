package net.sf.openrocket.android;

import net.sf.openrocket.android.motor.MotorBrowserActivity;
import net.sf.openrocket.android.thrustcurve.TCQueryActivity;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

public abstract class ActivityHelpers {

	public static void goHome( Activity parent ) {
		Intent i = new Intent(parent, Main.class);
		i.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_NEW_TASK );
		parent.startActivity(i);
	}
	
	public static void browseMotors( Activity parent ) {
		Intent i = new Intent(parent, MotorBrowserActivity.class);
		parent.startActivity(i);
	}

	public static void startPreferences( Activity parent ) {
		Intent intent = new Intent(parent, PreferencesActivity.class);
		parent.startActivity(intent);
	}
	
	public static void downloadFromThrustcurve( Activity parent, int requestCode ) {
		Intent i = new Intent(parent, TCQueryActivity.class);
		parent.startActivityForResult(i, requestCode);
	}

	public static void donate( Activity parent ) {
		String url = "http://sourceforge.net/donate/index.php?group_id=260357";
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData( Uri.parse(url) );
		parent.startActivity(intent);
	}
	
	public static void showAbout( FragmentActivity parent ) {
		AboutDialogFragment frag = AboutDialogFragment.newInstance();
		frag.show(parent.getSupportFragmentManager(), "about");
	}

}
