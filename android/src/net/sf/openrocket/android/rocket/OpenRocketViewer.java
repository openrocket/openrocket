package net.sf.openrocket.android.rocket;


import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.actionbarcompat.ActionBarFragmentActivity;
import net.sf.openrocket.android.simulation.SimulationChart;
import net.sf.openrocket.android.simulation.SimulationFragment;
import net.sf.openrocket.android.simulation.SimulationViewActivity;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.Simulation;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class OpenRocketViewer extends ActionBarFragmentActivity
implements Simulations.OnSimulationSelectedListener
{

	private Application app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (Application) this.getApplication();
		setContentView(R.layout.openrocketviewer);
		ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
		viewPager.setAdapter( new OpenRocketViewerPager( this.getSupportFragmentManager()));
		
		setTitle(app.getRocketDocument().getRocket().getName());
		
		getActionBarHelper().setDisplayHomeAsUpEnabled(true);
		
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
		case android.R.id.home:
			finish();
			return true;
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

	private class OpenRocketViewerPager extends FragmentPagerAdapter {

		public OpenRocketViewerPager( FragmentManager fm ) {
			super(fm);
		}
		@Override
		public int getCount() {
			return 3;
		}
		@Override
		public Fragment getItem( int position ) {
			switch (position) {
			case 0:
				return new Overview();
			case 1:
				return new Component();
			case 2:
				return new Simulations();
			}
			return null;
		}
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Overview";
			case 1:
				return "Components";
			case 2:
				return "Simulations";
			}
			return null;
		}
	}

}
