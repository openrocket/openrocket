package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.db.MotorDao;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.PersistentExpandableListFragment;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;


/*
 * TODO - make this work with PersistentExpandableListFragment.
 * 
 */
public class MotorListFragment extends PersistentExpandableListFragment
implements SharedPreferences.OnSharedPreferenceChangeListener
{
	public interface OnMotorSelectedListener {
		public void onMotorSelected( long motorId );
	}

	public static MotorListFragment newInstance( ) {

		MotorListFragment frag = new MotorListFragment();
		return frag;
	}

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

	private OnMotorSelectedListener motorSelectedListener;

	public void setMotorSelectedListener(
			OnMotorSelectedListener motorSelectedListener) {
		this.motorSelectedListener = motorSelectedListener;
	}

	public class MotorHierarchicalListAdapter extends ResourceCursorTreeAdapter
	{

		// Note that the constructor does not take a Cursor. This is done to avoid querying the 
		// database on the main thread.
		public MotorHierarchicalListAdapter(Context context, Cursor cursor, int groupLayout,
				int childLayout ) {

			super(context, cursor, groupLayout, childLayout);
		}

		@Override
		protected Cursor getChildrenCursor(Cursor arg0) {
			AndroidLogWrapper.d(MotorListFragment.class,"getChildrenCursor");
			String group = arg0.getString(arg0.getColumnIndex(groupColumn));
			Cursor c = mDbHelper.getMotorDao().fetchAllInGroups(groupColumn,group);
			return c;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorTreeAdapter#bindChildView(android.view.View, android.content.Context, android.database.Cursor, boolean)
		 */
		@Override
		protected void bindChildView(View arg0, Context arg1, Cursor arg2,
				boolean arg3) {

			TextView manu = (TextView) arg0.findViewById(R.id.motorChildManu);
			manu.setText( arg2.getString(arg2.getColumnIndex(MotorDao.MANUFACTURER)));

			TextView desig = (TextView) arg0.findViewById(R.id.motorChildName);
			desig.setText( arg2.getString(arg2.getColumnIndex(MotorDao.DESIGNATION)));

			TextView delays = (TextView) arg0.findViewById(R.id.motorChildDelays);
			delays.setText( arg2.getString(arg2.getColumnIndex(MotorDao.DELAYS)));

			TextView totImpulse = (TextView) arg0.findViewById(R.id.motorChildImpulse);
			totImpulse.setText( arg2.getString(arg2.getColumnIndex(MotorDao.TOTAL_IMPULSE)));
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorTreeAdapter#bindGroupView(android.view.View, android.content.Context, android.database.Cursor, boolean)
		 */
		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			TextView v = (TextView) view.findViewById(R.id.motorGroup);
			if ( MotorDao.DIAMETER.equals(groupColumn)) {
				double d = cursor.getDouble( cursor.getColumnIndex(groupColumn));
				v.setText( String.valueOf(Math.round(d * 1000.0)) );
			} else {
				v.setText( cursor.getString( cursor.getColumnIndex(groupColumn)));
			}
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		refreshData();

		registerForContextMenu(getExpandableListView());

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();

		Resources resources = this.getResources();
		groupColumnPreferenceKey = resources.getString(R.string.PreferenceMotorBrowserGroupingOption);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		setGroupColumnFromPreferences(pref);

		pref.registerOnSharedPreferenceChangeListener(this);

		if ( activity instanceof OnMotorSelectedListener ) {
			motorSelectedListener = (OnMotorSelectedListener) activity;
		}

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("Motor Operations");
		menu.add(Menu.NONE,CONTEXTMENU_DELETE,CONTEXTMENU_DELETE,"Delete");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
		long motorId = info.id;
		AndroidLogWrapper.d(MotorListFragment.class,"ContextMenu: " + motorId);
		switch(item.getItemId()) {
		case CONTEXTMENU_DELETE:
			mDbHelper.getMotorDao().deleteMotor(motorId);
			refreshData();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		super.onChildClick(parent, v, groupPosition, childPosition, id);
		//Intent i = new Intent(this, BurnPlotActivity.class);
		if( motorSelectedListener != null ) {
			motorSelectedListener.onMotorSelected(id);
		}
		return true;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.unregisterOnSharedPreferenceChangeListener(this);

		// Null out the group cursor. This will cause the group cursor and all of the child cursors
		// to be closed.
		mAdapter.changeCursor(null);
		mAdapter = null;

		mDbHelper.close();
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
		Cursor motorCursor = mDbHelper.getMotorDao().fetchGroups(groupColumn);
		if (mAdapter != null ) {
			mAdapter.changeCursor(motorCursor);
		}
		else {
			// Set up our adapter
			mAdapter = new MotorHierarchicalListAdapter( 
					getActivity(),
					motorCursor,
					R.layout.motor_list_group,
					R.layout.motor_list_child);
			setListAdapter(mAdapter);
		}
	}
	
}
