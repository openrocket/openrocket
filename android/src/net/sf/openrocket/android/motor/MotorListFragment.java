package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.db.MotorDao;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.PersistentExpandableListFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;


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
	private static final int CONTEXTMENU_EDIT = Menu.FIRST+2;

	private String groupColumnPreferenceKey;
	private String groupColumn = MotorDao.CASE_INFO;

	private static final String[] groupColumns = new String[] {
		MotorDao.CASE_INFO,
		MotorDao.DIAMETER,
		MotorDao.IMPULSE_CLASS,
		MotorDao.MANUFACTURER
	};

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
				v.setText( String.valueOf(Math.round(d * 1000.0)) + " mm");
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

		registerForContextMenu(getExpandableListView());

	}

	
	@Override
	public void onResume() {

		Resources resources = this.getResources();
		groupColumnPreferenceKey = resources.getString(R.string.PreferenceMotorBrowserGroupingOption);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		setGroupColumnFromPreferences(pref);

		pref.registerOnSharedPreferenceChangeListener(this);

		Activity activity = getActivity();
		if ( activity instanceof OnMotorSelectedListener ) {
			motorSelectedListener = (OnMotorSelectedListener) activity;
		}

		refreshData();

		super.onResume();

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle("Motor Operations");
		menu.add(Menu.NONE,CONTEXTMENU_DELETE,CONTEXTMENU_DELETE,"Delete");
		menu.add(Menu.NONE,CONTEXTMENU_EDIT,CONTEXTMENU_EDIT,"Edit");
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
		case CONTEXTMENU_EDIT:
			// DialogFragment.show() will take care of adding the fragment
			// in a transaction.  We also want to remove any currently showing
			// dialog, so make our own transaction and take care of that here.
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment prev = getFragmentManager().findFragmentByTag("dialog");
			if (prev != null) {
				ft.remove(prev);
			}
			ft.addToBackStack(null);

			// Create and show the dialog.
			DialogFragment newFragment = MotorDetailsFragment.newInstance(motorId);
			newFragment.show(ft, "dialog");
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		super.onChildClick(parent, v, groupPosition, childPosition, id);
		if( motorSelectedListener != null ) {
			motorSelectedListener.onMotorSelected(id);
		}
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		pref.unregisterOnSharedPreferenceChangeListener(this);

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

	public void refreshData() {
		if ( mDbHelper == null ) {
			mDbHelper = new DbAdapter(getActivity());
		}
		mDbHelper.open();

		Cursor motorCursor = mDbHelper.getMotorDao().fetchGroups(groupColumn);
		MotorHierarchicalListAdapter mAdapter = new MotorHierarchicalListAdapter( 
				getActivity(),
				motorCursor,
				R.layout.motor_list_group,
				R.layout.motor_list_child);
		setListAdapter(mAdapter);
		onContentChanged();
	}
}
