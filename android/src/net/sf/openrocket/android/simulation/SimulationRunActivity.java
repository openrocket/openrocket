package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationRunActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int simulationNumber = getIntent().getIntExtra("Simulation", -1);

		Fragment graph = SimulationRunFragment.newInstance(simulationNumber);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, graph);
		ft.commit();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preference_menu_option:
			ActivityHelpers.startPreferences(this);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem prefItem = menu.add(Menu.NONE, R.id.preference_menu_option, Menu.CATEGORY_SYSTEM, R.string.Preferences);
		prefItem.setIcon(R.drawable.ic_menu_preferences);
		return true;
	}

}