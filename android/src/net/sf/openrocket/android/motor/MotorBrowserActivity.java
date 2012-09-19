package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.ActivityHelpers;
import net.sf.openrocket.android.PreferencesActivity;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MotorBrowserActivity extends SherlockFragmentActivity
implements MotorListFragment.OnMotorSelectedListener
{

	MotorListFragment motorList;

	private final static int DOWNLOAD_REQUEST_CODE = 1;
	private final static String MOTOR_LIST_FRAGMENT = "motor_list";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motorbrowser);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.motorbrowsertitle);
		// Only create the motorBrowser fragment if it doesn't already exist.
		Fragment motorBrowser = getSupportFragmentManager().findFragmentByTag(MOTOR_LIST_FRAGMENT);
		if ( motorBrowser == null ) {
			getSupportFragmentManager()
			.beginTransaction()
			.add( R.id.motorBrowserList, new MotorListFragment(), MOTOR_LIST_FRAGMENT)
			.commit();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		int motorCount = 0;

		DbAdapter mDbHelper = new DbAdapter(this);
		mDbHelper.open();
		try {
			Cursor motorCounter = mDbHelper.getMotorDao().fetchAllMotors();
			motorCount = motorCounter.getCount();
			motorCounter.close();
		} finally {
			mDbHelper.close();
		}

		if ( motorCount == 0 ) {
			ActivityHelpers.downloadFromThrustcurve(this,DOWNLOAD_REQUEST_CODE);
		}


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.motor_browser_option_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		AndroidLogWrapper.d(MotorBrowserActivity.class,"onMenuItemSelected" + item.getItemId());
		switch(item.getItemId()) {
		case android.R.id.home:
			// we implement home in the motor browser as "back" since then it will return to
			// either main or the viewer.
			finish();
			return true;
		case R.id.download_from_thrustcurve_menu_option:
			ActivityHelpers.downloadFromThrustcurve(this,DOWNLOAD_REQUEST_CODE);
			return true;
		case R.id.preference_menu_option:
			Intent intent = new Intent().setClass(this, PreferencesActivity.class);
			this.startActivity(intent);
			return true;
		case R.id.menu_about:
			ActivityHelpers.showAbout(this);
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
