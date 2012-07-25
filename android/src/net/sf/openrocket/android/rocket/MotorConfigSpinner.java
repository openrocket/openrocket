package net.sf.openrocket.android.rocket;

import net.sf.openrocket.rocketcomponent.Rocket;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MotorConfigSpinner extends Spinner {

	public MotorConfigSpinner(Context context, AttributeSet attrs,
			int defStyle, int mode) {
		super(context, attrs, defStyle, mode);
	}

	public MotorConfigSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MotorConfigSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MotorConfigSpinner(Context context, int mode) {
		super(context, mode);
	}

	public MotorConfigSpinner(Context context) {
		super(context);
	}

	public void createAdapter(Rocket rocket ) {
	
		setAdapter(new MotorConfigSpinnerAdapter(this.getContext(), rocket) );
		
	}
	
	public void setSelectedConfiguration( String configId ) {
		this.setSelection( ((MotorConfigSpinnerAdapter)getAdapter()).getConfigurationPosition( configId ));
	}
	
	public String getSelectedConfiguration() {
		return ((MotorConfigSpinnerAdapter)getAdapter()).getConfiguration( this.getSelectedItemPosition() );
	}
	
	public class MotorConfigSpinnerAdapter extends ArrayAdapter<String> {

		private String[] motorConfigs;

		public MotorConfigSpinnerAdapter(Context context, Rocket rocket) {
			super(context, android.R.layout.simple_spinner_item);
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			motorConfigs = rocket.getMotorConfigurationIDs();

			for( String config: motorConfigs ) {
				this.add(rocket.getMotorConfigurationNameOrDescription(config));
			}

		}

		public int getConfigurationPosition(String configId) {

			int selectedIndex = 0;

			if ( configId == null ) {
				return selectedIndex;
			}

			for( String s : motorConfigs ) {
				// Note - s may be null since it is a valid id.
				if ( configId.equals(s) ) {
					break;
				}
				selectedIndex++;
			}
			if( selectedIndex >= motorConfigs.length ) {
				selectedIndex = 0;
			}

			return selectedIndex;
		}
		
		public String getConfiguration( int position ) {
			return motorConfigs[position];
		}
	}
}
