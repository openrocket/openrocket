package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.rocketcomponent.Rocket;
import android.content.Context;
import android.widget.ArrayAdapter;

public class MotorConfigSpinnerAdapter extends ArrayAdapter<String> {

	public MotorConfigSpinnerAdapter(Context context, Rocket rocket) {
		super(context, android.R.layout.simple_spinner_item);
		
		String[] motorConfigs = rocket.getMotorConfigurationIDs();

		for( String config: motorConfigs ) {
			this.add(rocket.getMotorConfigurationNameOrDescription(config));
		}
		
	}

}
