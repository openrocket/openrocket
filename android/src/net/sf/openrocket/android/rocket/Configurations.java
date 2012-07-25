package net.sf.openrocket.android.rocket;

import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.db.DbAdapter;
import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.android.motor.MotorDelayDialogFragment;
import net.sf.openrocket.android.motor.MotorListDialogFragment;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.android.util.ExpandableListFragment;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Configurations extends ExpandableListFragment {

	private final static String wizardFrag = "wizardFrag";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.rocket_configurations, container, false);

		return v;
	}

	@Override
	public void onResume() {
		setup();
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.rocket_viewer_configurations_option_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.menu_add:
			addConfiguration();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void refreshConfigsList() {
		setup();
	}

	private void addConfiguration() {
		CurrentRocketHolder.getCurrentRocket().addNewMotorConfig(getActivity());
	}
	
	private void removeConfiguration( String config ) {
		CurrentRocketHolder.getCurrentRocket().deleteMotorConfig( getActivity(), config );
	}
	
	private static class MotorMountInfo {

		private RocketComponent mmt;
		private String config;
		private ExtendedThrustCurveMotor motor;
		private double delay;

		String getMotorMountDescription() {
			String mmtDesc = mmt.getComponentName();
			mmtDesc += " (" + UnitGroup.UNITS_MOTOR_DIMENSIONS.toStringUnit( ((MotorMount)mmt).getMotorMountDiameter()) + ")";
			return mmtDesc;
		}

		String getMotorDescription() {
			return motor.getManufacturer().getDisplayName() + " " + motor.getDesignation();
		}

	}

	class ChildViewHolder {
		MotorMountInfo info;
		TextView motorMountName;
		Button motorDescription;
		Button motorDelay;
		void setMotor( ExtendedThrustCurveMotor motor ) {
			this.info.motor = motor;
			((MotorMount)info.mmt).setMotor(info.config, motor);
		}
		void setDelay( double delay ) {
			this.info.delay = delay;
			((MotorMount)info.mmt).setMotorDelay(info.config, delay);
		}
	}

	private void setup() {
		final OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		ExpandableListAdapter configurationAdapter = new BaseExpandableListAdapter() {

			// Note: the magic 1 you see below is so the "no motors" configuration
			// does not appear in the configuration list.
			List<MotorMount> mmts = rocketDocument.getRocket().getMotorMounts();

			@Override
			public int getGroupCount() {
				// don't show the "no motors" configuration, so we have one less than the
				// array length.
				return rocketDocument.getRocket().getMotorConfigurationIDs().length-1;
			}

			@Override
			public int getChildrenCount(int groupPosition) {
				return mmts.size();
			}

			@Override
			public Object getGroup(int groupPosition) {
				// Skip over the "no motors" configuration
				String config = rocketDocument.getRocket().getMotorConfigurationIDs()[groupPosition+1];
				return config;
			}

			@Override
			public Object getChild(int groupPosition, int childPosition) {
				MotorMountInfo info = new MotorMountInfo();
				info.mmt = (RocketComponent)(mmts.get(childPosition));

				String config = (String) getGroup(groupPosition);
				info.config = config;
				info.motor = (ExtendedThrustCurveMotor) ((MotorMount)info.mmt).getMotor(config);

				if ( info.motor != null ) {
					info.delay = ((MotorMount)info.mmt).getMotorDelay(config);
				} else {
					info.delay = -1;
				}

				return info;
			}

			@Override
			public long getGroupId(int groupPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long getChildId(int groupPosition, int childPosition) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public boolean hasStableIds() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
				if ( convertView == null ) {
					convertView = getActivity().getLayoutInflater().inflate(android.R.layout.simple_expandable_list_item_1,null);
				}

				String configDescription = rocketDocument.getRocket().getMotorConfigurationNameOrDescription((String) getGroup(groupPosition));
				((TextView)convertView.findViewById(android.R.id.text1)).setText( configDescription );
				return convertView;
			}

			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {
				if ( convertView == null ) {
					convertView = getActivity().getLayoutInflater().inflate(R.layout.motor_config_item,null);
					ChildViewHolder holder = new ChildViewHolder();
					holder.motorMountName = (TextView) convertView.findViewById(R.id.motor_config_motor_mount_name);
					holder.motorDescription = (Button) convertView.findViewById(R.id.motor_config_motor_desc);
					holder.motorDelay = (Button) convertView.findViewById(R.id.motor_config_motor_delay);
					holder.info = (MotorMountInfo) getChild(groupPosition,childPosition);
					convertView.setTag(holder);
				}

				ChildViewHolder cvHolder = (ChildViewHolder) convertView.getTag();

				cvHolder.motorMountName.setText(cvHolder.info.getMotorMountDescription());
				cvHolder.motorDescription.setOnClickListener( new MotorWizardOnClickListener() );
				if ( cvHolder.info.motor == null ) {
					cvHolder.motorDelay.setClickable(false);
					cvHolder.motorDelay.setOnClickListener(null);
					cvHolder.motorDescription.setText(R.string.select_motor);
				} else {
					cvHolder.motorDelay.setClickable(true);
					cvHolder.motorDelay.setOnClickListener( new MotorDelayOnClickListener(cvHolder.info.motor) );
					cvHolder.motorDescription.setText(cvHolder.info.getMotorDescription());
				}
				if( cvHolder.info.delay >=0 ) {
					if( cvHolder.info.delay == Motor.PLUGGED ) {
						cvHolder.motorDelay.setText("P");
					} else {
						cvHolder.motorDelay.setText( String.valueOf(Math.round(cvHolder.info.delay)));
					}
				} else {
					cvHolder.motorDelay.setText(R.string.select_delay);
				}

				return convertView;
			}

			@Override
			public boolean isChildSelectable(int groupPosition,	int childPosition) {
				return false;
			}

		};

		setListAdapter(configurationAdapter);
	}

	@Override
	public boolean onListItemLongClick(ListView l, View v, int position, long id) {
		
		Object o = getExpandableListAdapter().getGroup(position);
		
		if ( o == null || ! (o instanceof String) ) {
			return false;
		}
		final String motorConfigId = (String)o;
		
		AlertDialog.Builder b = new AlertDialog.Builder( getActivity() );
		b.setTitle(R.string.DeleteConfigTitle);
		b.setCancelable(true);
		b.setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Configurations.this.removeConfiguration(motorConfigId);
			}
			
		});
		
		Dialog dialog = b.create();
		
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return true;
	}

	private class MotorWizardOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			final ViewGroup parent = (ViewGroup) v.getParent();
			final ChildViewHolder cvHolder = (ChildViewHolder) parent.getTag();
			final MotorListDialogFragment f = new MotorListDialogFragment();
			f.setMotorSelectedListener( new MotorListDialogFragment.OnMotorSelectedListener() {

				@Override
				public void onMotorSelected(long motorId) {
					DbAdapter mdbHelper = new DbAdapter(getActivity());
					mdbHelper.open();
					try {
						ExtendedThrustCurveMotor motor = mdbHelper.getMotorDao().fetchMotor(motorId);
						cvHolder.setMotor( motor );
						((BaseExpandableListAdapter)Configurations.this.getExpandableListAdapter()).notifyDataSetInvalidated();
					} catch (Exception ex) {
						AndroidLogWrapper.d(Configurations.class, "BlewUp looking for motor", ex);
					} finally {
						mdbHelper.close();
					}
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					ft.remove(f);
					ft.commit();

				}
			});
			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			ft.add(f, wizardFrag);
			ft.commit();
		}

	}

	private class MotorDelayOnClickListener implements View.OnClickListener {

		double[] standardDelays;

		public MotorDelayOnClickListener(ExtendedThrustCurveMotor motor) {
			super();
			this.standardDelays = motor.getStandardDelays();
		}

		@Override
		public void onClick(View v) {
			final View parent = (View) v.getParent();
			final ChildViewHolder cvHolder = (ChildViewHolder) parent.getTag();
			final MotorDelayDialogFragment f = MotorDelayDialogFragment.newInstance(standardDelays);
			f.setDelaySelectedListener( new MotorDelayDialogFragment.OnDelaySelectedListener() {

				@Override
				public void onDelaySelected(double delay) {
					cvHolder.setDelay( delay );
					((BaseExpandableListAdapter)Configurations.this.getExpandableListAdapter()).notifyDataSetInvalidated();
					FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
					ft.remove(f);
					ft.commit();

				}
			});
			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			ft.add(f, wizardFrag);
			ft.commit();
		}

	}
}
