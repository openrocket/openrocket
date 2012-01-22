package net.sf.openrocket.android.rocket;


import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.util.TabsAdapter;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.Configuration;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class OpenRocketViewer extends FragmentActivity
implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private static final String TAG = "OpenRocketViewer";

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

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

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

	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// just in case the user changed the units, we redraw.
		//	TODO = 	updateContents();  redraw all children..
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rocket_viewer_option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch ( item.getItemId() ) {
		/*		case android.R.id.home:
			Intent i = new Intent( this, Main.class );
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			return true;
		 */
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG,"onMenuItemSelected" + item.getItemId());
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

}
