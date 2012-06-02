package net.sf.openrocket.android.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.simulation.FlightDataType;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class SimulationSeriesDialog extends DialogFragment {
	
	public interface OnConfirmListener {
		public void onConfirm();
	}

	private Spinner series1Spinner;
	private Spinner series2Spinner;

	private SimulationChart chart;
	private OnConfirmListener listener;

	public static SimulationSeriesDialog newInstance( SimulationChart chart ) {
		SimulationSeriesDialog d = new SimulationSeriesDialog();
		d.chart = chart;
		return d;
	}

	public void setOnConfirmListener(OnConfirmListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null ) {
			chart = (SimulationChart) savedInstanceState.getSerializable("chart");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable("chart", chart);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.simulation_series_dialog, container, false);
		
		OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		Button okButton = (Button) v.findViewById(R.id.simulationOkButton);
		okButton.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				chart.setSeries1((FlightDataType)series1Spinner.getSelectedItem());
				chart.setSeries2((FlightDataType)series2Spinner.getSelectedItem());

				if ( listener != null ) {
					listener.onConfirm();
				}
				SimulationSeriesDialog.this.dismiss();
			}
			
		});

		series1Spinner = (Spinner) v.findViewById(R.id.simulationSeries1);
		series2Spinner = (Spinner) v.findViewById(R.id.simulationSeries2);

		List<FlightDataType> selectableSeries = new ArrayList<FlightDataType>();
		for( FlightDataType fdt : chart.getFlightDataBranch(rocketDocument).getTypes() ) {
			if ( fdt == FlightDataType.TYPE_TIME ) { 

			} else {
				selectableSeries.add(fdt);
			}
		}
		ArrayAdapter<FlightDataType> serieses = new ArrayAdapter<FlightDataType>(getActivity(),android.R.layout.simple_spinner_item,selectableSeries) {

			@Override
			public View getView(int position, View convertView,	ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = inflater;
					v = li.inflate(android.R.layout.simple_spinner_item,null);
				}
				FlightDataType fdt = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( fdt.toString() );
				return v;
			}

		};
		series1Spinner.setAdapter(serieses);
		series2Spinner.setAdapter(serieses);


		return v;
	}

}
