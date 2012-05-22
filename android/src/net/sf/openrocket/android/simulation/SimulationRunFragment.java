
package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.rocket.MotorConfigSpinnerAdapter;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.unit.UnitGroup;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationRunFragment extends DialogFragment {
	
	int simulationId;

	EditText windspeedField;
	EditText rodlengthField;
	EditText rodangleField;
	Spinner motorSpinner;

	public static SimulationRunFragment newInstance( int simulationId ) {
		SimulationRunFragment frag = new SimulationRunFragment();
		Bundle b = new Bundle();
		b.putInt("simulationId", simulationId);
		frag.setArguments(b);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE,getTheme());

		if ( savedInstanceState != null ) {
			simulationId = savedInstanceState.getInt("simulationId");
		} else {
			Bundle b = getArguments();
			simulationId = b.getInt("simulationId");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.simulation_condition_dialog, container, false);
		windspeedField = (EditText) v.findViewById(R.id.simulation_condition_windspeed);
		rodlengthField = (EditText) v.findViewById(R.id.simulation_condition_rodlength);
		rodangleField = (EditText) v.findViewById(R.id.simulation_condition_rodangle);
		
		motorSpinner = (Spinner) v.findViewById(R.id.simulationConditionConfigurationSpinner);
		
		OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();

		Simulation sim = rocketDocument.getSimulation(simulationId);
		
		windspeedField.setText( UnitGroup.UNITS_VELOCITY.toString(sim.getSimulatedConditions().getWindSpeedAverage()) );
		rodlengthField.setText( UnitGroup.UNITS_LENGTH.toString(sim.getSimulatedConditions().getLaunchRodLength()));
		rodangleField.setText( String.valueOf( sim.getSimulatedConditions().getLaunchRodLength() ));

		MotorConfigSpinnerAdapter spinnerAdapter = new MotorConfigSpinnerAdapter(getActivity(),rocketDocument.getRocket());
		motorSpinner.setAdapter(spinnerAdapter);
		/* TODO - enable saving.
		((Button) v.findViewById(R.id.motorDetailsSaveButton)).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						MotorDetailsFragment.this.saveChanges();
					}
				});
				*/
		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("simulationId", simulationId);

	}
	
}