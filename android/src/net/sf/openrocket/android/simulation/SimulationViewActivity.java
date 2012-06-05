package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationViewActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.simulation_graph_activity);
		int simulationNumber = getIntent().getIntExtra("Simulation", 0);

		final OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		Simulation sim = rocketDocument.getSimulation(simulationNumber);

		SimulationChart chart = new SimulationChart( simulationNumber);
		chart.setSeries1(sim.getSimulatedData().getBranch(0).getTypes()[1]);
		chart.setSeries2(sim.getSimulatedData().getBranch(0).getTypes()[2]);

		Fragment graph = SimulationViewFragment.newInstance(chart);

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