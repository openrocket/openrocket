package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.PreferencesActivity;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.db.MotorDao;
import net.sf.openrocket.android.thrustcurve.TCQueryActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;


public class MotorHierarchicalBrowser
extends PersistentExpandableListActivity
implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = "MotorHierarchicalBrowser";

	private static final int ACTIVITY_DOWNLOAD=0;

	private static final int CONTEXTMENU_DELETE = Menu.FIRST+1;

	private String groupColumnPreferenceKey;
	private String groupColumn = MotorDao.CASE_INFO;

	private static final String[] groupColumns = new String[] {
		MotorDao.CASE_INFO,
		MotorDao.DIAMETER,
		MotorDao.IMPULSE_CLASS,
		MotorDao.MANUFACTURER
	};

	private CursorTreeAdapter mAdapter;

	private DbAdapter mDbHelper;

	public class MotorHierarchicalListAdapter extends SimpleCursorTreeAdapter
	{

		// Note that the constructor does not take a Cursor. This is done to avoid querying the 
		// database on the main thread.
		public MotorHierarchicalListAdapter(Context context, Cursor cursor, int groupLayout,
				int childLayout, String[] groupFrom, int[] groupTo, String[] childrenFrom,
				int[] childrenTo) {

			super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childrenFrom,
					childrenTo);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor arg0) {
			Log.d(TAG,"getChildrenCursor");
			String group = arg0.getString(arg0.getColumnIndex(groupColumn));
			Log.d(TAG,"  for: "+ groupColumn + " = " + group);
			Cursor c = mDbHelper.getMotorDao().fetchAllInGroups(groupColumn,group);
			Log.d(TAG,"  got cursor");
			startManagingCursor(c);
			return c;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if ( groupColumnPreferenceKey.equals(arg1) ) {
			setGroupColumnFromPreferences(arg0);
			refreshData();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper = new DbAdapter(this);
		mDbHelper.open();

		Resources resources = this.getResources();
		groupColumnPreferenceKey = resources.getString(R.string.PreferenceMotorBrowserGroupingOption);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		setGroupColumnFromPreferences(pref);

		pref.registerOnSharedPreferenceChangeListener(this);

		refreshData();

		registerForContextMenu(getExpandableListView());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.motor_browser_option_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG,"onMenuItemSelected" + item.getItemId());
		switch(item.getItemId()) {
		case R.id.download_from_thrustcurve_menu_option:
			tcDownload();
			return true;
		case R.id.preference_menu_option:
			Intent intent = new Intent().setClass(this, PreferencesActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.d(TAG,"onCreateContextMenu " + menuInfo);
		Log.d(TAG, "v.getId() = " + v.getId());
		Log.d(TAG, "motorListView = " + R.id.motorListView);
		//    	if (v.getId() == R.id.motorListView) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
		menu.setHeaderTitle("context menu");
		menu.add(Menu.NONE,CONTEXTMENU_DELETE,CONTEXTMENU_DELETE,"Delete");
		//    	}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		long motorId = info.id;
		Log.d(TAG,"ContextMenu: " + motorId);
		switch(item.getItemId()) {
		case CONTEXTMENU_DELETE:
			mDbHelper.getMotorDao().deleteMotor(motorId);
			refreshData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		refreshData();
	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		super.onChildClick(parent, v, groupPosition, childPosition, id);
		Motor m = mDbHelper.getMotorDao().fetchMotor(id);
		//Intent i = new Intent(this, BurnPlotActivity.class);
		Intent i = new Intent(this,MotorDetails.class);
		i.putExtra("Motor", m);
		startActivity(i);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		pref.unregisterOnSharedPreferenceChangeListener(this);

		// Null out the group cursor. This will cause the group cursor and all of the child cursors
		// to be closed.
		mAdapter.changeCursor(null);
		mAdapter = null;

		mDbHelper.close();
	}

	private void tcDownload() {
		Intent i = new Intent(this, TCQueryActivity.class);
		startActivityForResult(i, ACTIVITY_DOWNLOAD);
	}

	private void setGroupColumnFromPreferences( SharedPreferences prefs ) {
		String indexStr = prefs.getString(groupColumnPreferenceKey, "1");
		int index;
		//Dirty hack, you can't use integer-array in ListPreferences
		try {
			index = Integer.parseInt(indexStr);
		} catch ( Exception e ) {
			index = 1;
		}
		if ( index >= groupColumns.length ) {
			index = 1;
		}
		groupColumn = groupColumns[index];

	}
	private void refreshData() {
		if (mAdapter != null ) {
			mAdapter.changeCursor(null);
		}
		Cursor motorCursor = mDbHelper.getMotorDao().fetchGroups(groupColumn);
		startManagingCursor(motorCursor);
		// Set up our adapter
		mAdapter = new MotorHierarchicalListAdapter( 
				this,
				motorCursor,
				R.layout.motor_list_group,
				R.layout.motor_list_child,
				new String[] { groupColumn }, // Name for group layouts
				new int[] { R.id.motorGroup },
				new String[] { MotorDao.MANUFACTURER, MotorDao.NAME, MotorDao.TOTAL_IMPULSE }, // Number for child layouts
				new int[] { R.id.motorChildManu, R.id.motorChildName, R.id.motorChildImpulse });
		setListAdapter(mAdapter);
	}
}
