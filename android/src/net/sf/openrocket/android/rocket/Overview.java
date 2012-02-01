package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.masscalc.BasicMassCalculator;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketUtils;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class Overview extends Fragment {

	/* Calculation of CP and CG */
	private AerodynamicCalculator aerodynamicCalculator = new BarrowmanCalculator();
	private MassCalculator massCalculator  = new BasicMassCalculator();

	private Spinner configurationSpinner;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		AndroidLogWrapper.d(Overview.class, "Created View");
		View v = inflater.inflate(R.layout.rocket_overview, container, false);
		configurationSpinner = (Spinner) v.findViewById(R.id.openrocketviewerConfigurationSpinner);

		return v;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();
		final Configuration rocketConfiguration = rocketDocument.getDefaultConfiguration();
		Rocket rocket = rocketDocument.getRocket();

		String[] motorConfigs = rocket.getMotorConfigurationIDs();
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item);
		for( String config: motorConfigs ) {
			spinnerAdapter.add(rocket.getMotorConfigurationNameOrDescription(config));
		}

		AndroidLogWrapper.d(Overview.class, "spinnerAdapter = " + spinnerAdapter);
		AndroidLogWrapper.d(Overview.class, "configurationSpinner = " + configurationSpinner);
		
		configurationSpinner.setAdapter(spinnerAdapter);
		configurationSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
			 */
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				String selectedConfigId = rocketDocument.getRocket().getMotorConfigurationIDs()[arg2];
				rocketConfiguration.setMotorConfigurationID(selectedConfigId);
				Coordinate cp = aerodynamicCalculator.getWorstCP(rocketConfiguration,
						new FlightConditions(rocketConfiguration),
						new WarningSet());

				Coordinate cg = massCalculator.getCG(rocketConfiguration, MassCalcType.LAUNCH_MASS);

				Unit lengthUnit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
				Unit massUnit = UnitGroup.UNITS_MASS.getDefaultUnit();
				Unit stabilityUnit = UnitGroup.stabilityUnits(rocketConfiguration).getDefaultUnit();

				((TextView)getActivity().findViewById(R.id.openrocketviewerCP)).setText(lengthUnit.toStringUnit(cp.x));
				((TextView)getActivity().findViewById(R.id.openrocketviewerCG)).setText(lengthUnit.toStringUnit(cg.x));
				((TextView)getActivity().findViewById(R.id.openrocketviewerLiftOffWeight)).setText(massUnit.toStringUnit(cg.weight));
				((TextView)getActivity().findViewById(R.id.openrocketviewerStabilityMargin)).setText(stabilityUnit.toStringUnit(cp.x-cg.x));

			}

			/* (non-Javadoc)
			 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
			 */
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				((TextView)getActivity().findViewById(R.id.openrocketviewerCP)).setText("");
				((TextView)getActivity().findViewById(R.id.openrocketviewerCG)).setText("");
				((TextView)getActivity().findViewById(R.id.openrocketviewerLiftOffWeight)).setText("");
				((TextView)getActivity().findViewById(R.id.openrocketviewerStabilityMargin)).setText("");
			}

		});

		Unit lengthUnit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		Unit massUnit = UnitGroup.UNITS_MASS.getDefaultUnit();

		Coordinate cg = RocketUtils.getCG(rocket, MassCalcType.NO_MOTORS);
		double length = RocketUtils.getLength(rocket);
		((TextView)getActivity().findViewById(R.id.openrocketviewerDesigner)).setText(rocket.getDesigner());
		((TextView)getActivity().findViewById(R.id.openrocketviewerLength)).setText(lengthUnit.toStringUnit(length));
		((TextView)getActivity().findViewById(R.id.openrocketviewerMass)).setText(massUnit.toStringUnit(cg.weight));
		((TextView)getActivity().findViewById(R.id.openrocketviewerStageCount)).setText(String.valueOf(rocket.getStageCount()));


	}


}
