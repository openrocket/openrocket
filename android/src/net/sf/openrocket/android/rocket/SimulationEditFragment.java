
package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.unit.UnitGroup;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockDialogFragment;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationEditFragment extends SherlockDialogFragment {

	private int simulationId;

	private EditText windspeedField;
	private EditText rodlengthField;
	private EditText rodangleField;
	private EditText roddirectionField;
	private MotorConfigSpinner motorSpinner;

	public static SimulationEditFragment newInstance( int simulationId ) {
		SimulationEditFragment frag = new SimulationEditFragment();
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
		
		Button deleteButton = (Button) v.findViewById(R.id.simulationConditionDelete);
		deleteButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				onDelete();
			}
			
		});
		windspeedField = (EditText) v.findViewById(R.id.simulation_condition_windspeed);
		rodlengthField = (EditText) v.findViewById(R.id.simulation_condition_rodlength);
		rodangleField = (EditText) v.findViewById(R.id.simulation_condition_rodangle);
		roddirectionField = (EditText) v.findViewById(R.id.simulation_condition_roddirection);

		motorSpinner = (MotorConfigSpinner) v.findViewById(R.id.simulationConditionConfigurationSpinner);

		OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();

		motorSpinner.createAdapter(rocketDocument.getRocket());

		Simulation sim = rocketDocument.getSimulation(simulationId);

		SimulationOptions options = sim.getOptions();
		if ( options != null ) {
			windspeedField.setText( UnitGroup.UNITS_VELOCITY.toString( options.getWindSpeedAverage() ));
			rodlengthField.setText( UnitGroup.UNITS_LENGTH.toString( options.getLaunchRodLength() ));
			rodangleField.setText( String.valueOf( options.getLaunchRodLength() ));
			roddirectionField.setText( String.valueOf( options.getLaunchRodDirection() ));
			motorSpinner.setSelectedConfiguration(options.getMotorConfigurationID());
		}

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("simulationId", simulationId);

	}

	public void onDelete( ) {
		((Application)getActivity().getApplication()).deleteSimulation(simulationId);
		getDialog().dismiss();
	}
	
}