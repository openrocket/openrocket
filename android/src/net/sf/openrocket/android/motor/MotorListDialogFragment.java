package net.sf.openrocket.android.motor;

import net.sf.openrocket.R;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.db.MotorDao;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.PersistentExpandableListView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class MotorListDialogFragment extends SherlockDialogFragment 
implements ExpandableListView.OnChildClickListener
{

	public interface OnMotorSelectedListener {
		public void onMotorSelected( long motorId );
	}

	private final static String groupColumn = MotorDao.DIAMETER;

	private DbAdapter mDbHelper;

	private ExpandableListView list;

	private OnMotorSelectedListener motorSelectedListener;

	public void setMotorSelectedListener(
			OnMotorSelectedListener motorSelectedListener) {
		this.motorSelectedListener = motorSelectedListener;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		if( motorSelectedListener != null ) {
			motorSelectedListener.onMotorSelected(id);
		}
		return true;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select Motor");
		list = new PersistentExpandableListView(getActivity());
		list.setOnChildClickListener( this );
		refreshData();
		builder.setView( list );
		return builder.create();
	}

	@Override
	public void onPause() {
		super.onPause();
		mDbHelper.close();
	}

	public void refreshData() {
		if ( mDbHelper == null ) {
			mDbHelper = new DbAdapter(getActivity());
		}
		mDbHelper.open();

		Cursor motorCounter = mDbHelper.getMotorDao().fetchAllMotors();
		int motorCount = motorCounter.getCount();
		motorCounter.close();
		
		if ( motorCount == 0 ) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("No Motors Found");
			builder.setMessage("Motors can be downloaded from thrustcurve");
			builder.setCancelable(true);
			AlertDialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
		}

		Cursor motorCursor = mDbHelper.getMotorDao().fetchGroups(groupColumn);
		MotorHierarchicalListAdapter mAdapter = new MotorHierarchicalListAdapter( 
				getActivity(),
				motorCursor,
				R.layout.motor_list_group,
				R.layout.motor_list_child);
		list.setAdapter(mAdapter);
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


}
