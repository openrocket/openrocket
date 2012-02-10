package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.PreferencesActivity;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MotorBrowserActivity extends FragmentActivity
implements MotorListFragment.OnMotorSelectedListener
{

	MotorListFragment motorList;
	
	private final static int DOWNLOAD_REQUEST_CODE = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motorbrowser);
		getSupportFragmentManager().beginTransaction().add( R.id.motorBrowserList, new MotorListFragment()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.motor_browser_option_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AndroidLogWrapper.d(MotorBrowserActivity.class,"onMenuItemSelected" + item.getItemId());
		switch(item.getItemId()) {
		case R.id.download_from_thrustcurve_menu_option:
			ActivityHelpers.downloadFromThrustcurve(this,DOWNLOAD_REQUEST_CODE);
			return true;
		case R.id.preference_menu_option:
			Intent intent = new Intent().setClass(this, PreferencesActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		if ( requestCode == DOWNLOAD_REQUEST_CODE ) {
			MotorListFragment frag = (MotorListFragment) getSupportFragmentManager().findFragmentById(R.id.motorBrowserList);
			frag.refreshData();
		}
	}

	@Override
	public void onMotorSelected(long motorId) {
		
		View sidepane = findViewById(R.id.sidepane);
		if ( /* if multi pane */ sidepane != null ) {
			
			Fragment graph = BurnPlotFragment.newInstance(motorId);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// probably only want to update back stack for first time.
			ft.addToBackStack("burnplot");
			ft.replace(R.id.sidepane, graph);
			ft.show(graph);
			ft.commit();

		} else {
			Intent i = new Intent(this,BurnPlotActivity.class);
			i.putExtra("Motor", motorId);
			startActivity(i);
		}

	}

}
