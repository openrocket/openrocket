package net.sf.openrocket.android.rocket;


import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.simulation.SimulationChart;
import net.sf.openrocket.android.simulation.SimulationViewActivity;
import net.sf.openrocket.android.simulation.SimulationViewFragment;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class OpenRocketViewer extends OpenRocketLoaderActivity
implements Simulations.OnSimulationSelectedListener, OpenRocketSaverFragment.OnOpenRocketFileSaved
{

	private final static int OVERVIEW_POS = 0;
	private final static int COMPONENT_POS = 1;
	private final static int SIMS_POS = 2;
	private final static int CONFIGS_POS = 3;
	private final static int TABSIZE = 4;

	private OpenRocketViewerPagerAdapter viewPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// If the application sleeps for a long time, the CurrentRocketHolder might get cleaned
		// up.  When this happens, we cannot restore this state, so instead we just
		// go home.
		OpenRocketDocument rocDoc = CurrentRocketHolder.getCurrentRocket().getRocketDocument();
		if ( rocDoc == null ) {
			AndroidLogWrapper.d(OpenRocketViewer.class, "No document - go home");
			ActivityHelpers.goHome(this);
			finish();
			return;
		}
		setTitle(rocDoc.getRocket().getName());
		getSupportActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.openrocketviewer);
		ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
		viewPagerAdapter = new OpenRocketViewerPagerAdapter( this.getSupportFragmentManager() );
		viewPager.setAdapter( viewPagerAdapter );
	}

	@Override
	protected void onPause() {
		CurrentRocketHolder.getCurrentRocket().setHandler(null);
		super.onPause();
	}

	@Override
	protected void onResume() {
		RocketChangedEventHandler handler = new RocketChangedEventHandler();
		CurrentRocketHolder.getCurrentRocket().setHandler(handler);
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.rocket_viewer_option_menu, menu);
		MenuItem saveAction = menu.findItem(R.id.menu_save);
		if ( CurrentRocketHolder.getCurrentRocket().isModified() ) {
			saveAction.setVisible(true);
			saveAction.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );
		} else {
			saveAction.setVisible(false);
			saveAction.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
		}
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AndroidLogWrapper.d(OpenRocketViewer.class,"onMenuItemSelected" + item.getItemId());
		switch(item.getItemId()) {
		case R.id.menu_load:
			// FIXME - Might want to prompt for save here.
			pickOrkFiles();
			return true;
		case R.id.menu_save:
			// FIXME - Probably want to open a dialog here.
			getSupportFragmentManager().beginTransaction()
			.add( OpenRocketSaverFragment.newInstance(), "saver")
			.commitAllowingStateLoss();
			return true;
		case android.R.id.home:
			ActivityHelpers.goHome(this);
			return true;
		case R.id.motor_list_menu_option:
			ActivityHelpers.browseMotors(this);
			return true;
		case R.id.preference_menu_option:
			ActivityHelpers.startPreferences(this);
			return true;
		case R.id.menu_about:
			ActivityHelpers.showAbout(this);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onSimulationSelected(int simulationId) {

		Simulation sim = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getSimulation(simulationId);
		// Check if there is data for this simulation.
		if ( sim.getSimulatedData() == null || sim.getSimulatedData().getBranchCount() == 0 ) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("The selected simulation does not have saved data.");
			builder.setCancelable(true);
			builder.show();
			return;
		}

		View sidepane = findViewById(R.id.sidepane);
		if ( /* if multi pane */ sidepane != null ) {
			SimulationChart chart = new SimulationChart(simulationId);

			Fragment graph = SimulationViewFragment.newInstance(chart);

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

	@Override
	public void onOpenRocketFileSaved(Boolean result) {
		invalidateOptionsMenu();
	}

	private class RocketChangedEventHandler extends net.sf.openrocket.android.RocketChangedEventHandler {

		@Override
		protected void doSimsChanged() {
			invalidateOptionsMenu();
			Simulations sims = (Simulations) viewPagerAdapter.getFragmentAtPos(SIMS_POS);
			if ( sims != null ) {
				sims.refreshSimulationList();
			}
		}

		@Override
		protected void doMotorConfigsChanged() {
			invalidateOptionsMenu();
			Configurations configs = (Configurations) viewPagerAdapter.getFragmentAtPos(CONFIGS_POS);
			if ( configs != null ) {
				configs.refreshConfigsList();
			}
		}

	};


	private class OpenRocketViewerPagerAdapter extends FragmentPagerAdapter {

		public OpenRocketViewerPagerAdapter( FragmentManager fm ) {
			super(fm);
		}
		@Override
		public int getCount() {
			return TABSIZE;
		}
		@Override
		public Fragment getItem( int position ) {
			switch (position) {
			case OVERVIEW_POS:
				return new Overview();
			case COMPONENT_POS:
				return new Component();
			case SIMS_POS:
				return new Simulations();
			case CONFIGS_POS:
				return new Configurations();
			}
			return null;
		}
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case OVERVIEW_POS:
				return "Overview";
			case COMPONENT_POS:
				return "Components";
			case SIMS_POS:
				return "Simulations";
			case CONFIGS_POS:
				return "Configurations";
			}
			return null;
		}

		public Fragment getFragmentAtPos( int pos ) {
			String tag = "android:switcher:"+R.id.pager+":"+pos;
			Fragment f = OpenRocketViewer.this.getSupportFragmentManager().findFragmentByTag(tag);
			return f;
		}
	}

}
