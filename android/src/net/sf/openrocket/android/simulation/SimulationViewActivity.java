/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationViewActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.simulation_graph_activity);
		int simulationNumber = getIntent().getIntExtra("Simulation", 0);

		final OpenRocketDocument rocketDocument = ((Application)getApplication()).getRocketDocument();

		Simulation sim = rocketDocument.getSimulation(simulationNumber);

		SimulationChart chart = new SimulationChart( simulationNumber);
		chart.setSeries1(sim.getSimulatedData().getBranch(0).getTypes()[1]);
		chart.setSeries2(sim.getSimulatedData().getBranch(0).getTypes()[2]);

		Fragment graph = SimulationFragment.newInstance(chart);

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