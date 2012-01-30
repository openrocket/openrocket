package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.PreferencesActivity;
import net.sf.openrocket.android.simulation.SimulationChart;
import net.sf.openrocket.android.simulation.SimulationFragment;
import net.sf.openrocket.android.simulation.SimulationViewActivity;
import net.sf.openrocket.android.thrustcurve.TCQueryActivity;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.Simulation;
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
    		motorList = MotorListFragment.newInstance();
    		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    		ft.add(android.R.id.content, motorList);
    		ft.commit();
        }
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
			ActivityHelpers.downloadFromThrustcurve(this);
			return true;
		case R.id.preference_menu_option:
			Intent intent = new Intent().setClass(this, PreferencesActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onMotorSelected(long motorId) {
		
		View sidepane = findViewById(R.id.sidepane);
		if ( /* if multi pane */ sidepane != null ) {
			/*
			Simulation sim = app.getRocketDocument().getSimulation(simulationId);
			SimulationChart chart = new SimulationChart(simulationId);

			Fragment graph = SimulationFragment.newInstance(chart);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// probably only want to update back stack for first time.
			ft.addToBackStack("simulationplot");
			ft.replace(R.id.sidepane, graph);
			ft.show(graph);
			ft.commit();
*/

		} else {
			Intent i = new Intent(this,MotorDetailsActivity.class);
			i.putExtra("Motor", motorId);
			startActivity(i);
		}

	}

}
