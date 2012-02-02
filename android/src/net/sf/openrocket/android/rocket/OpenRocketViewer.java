package net.sf.openrocket.android.rocket;


import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.simulation.SimulationChart;
import net.sf.openrocket.android.simulation.SimulationFragment;
import net.sf.openrocket.android.simulation.SimulationViewActivity;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.TabsAdapter;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.rocketcomponent.Configuration;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

public class OpenRocketViewer extends FragmentActivity
implements Simulations.OnSimulationSelectedListener
{

	OpenRocketDocument rocketDocument;
	Configuration rocketConfiguration;

	private Application app;

	TabHost mTabHost;
	ViewPager  mViewPager;
	TabsAdapter mTabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (Application) this.getApplication();

		setContentView(R.layout.openrocketviewer);

		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

		mTabsAdapter.addTab(mTabHost.newTabSpec("overview").setIndicator("Overview"),
				Overview.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec("components").setIndicator("Components"),
				Component.class, null);
		mTabsAdapter.addTab(mTabHost.newTabSpec("simulations").setIndicator("Simulations"),
				Simulations.class, null);

		if (savedInstanceState != null) {
			mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rocket_viewer_option_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AndroidLogWrapper.d(OpenRocketViewer.class,"onMenuItemSelected" + item.getItemId());
		switch(item.getItemId()) {
		case R.id.motor_list_menu_option:
			ActivityHelpers.browseMotors(this);
			return true;
		case R.id.preference_menu_option:
			ActivityHelpers.startPreferences(this);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onSimulationSelected(int simulationId) {
		View sidepane = findViewById(R.id.sidepane);
		if ( /* if multi pane */ sidepane != null ) {
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


		} else {
			Intent i = new Intent(this, SimulationViewActivity.class);
			i.putExtra("Simulation",simulationId);
			startActivity(i);
		}
	}

}
