package net.sf.openrocket.android.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class SimulationPlotConfigDialog extends DialogFragment {
	
	public interface OnConfirmListener {
		public void onConfirm();
	}

	private ListView eventList;
	private Spinner series1Spinner;
	private Spinner series2Spinner;

	private SimulationChart chart;
	private OnConfirmListener listener;

	public static SimulationPlotConfigDialog newInstance( SimulationChart chart ) {
		SimulationPlotConfigDialog d = new SimulationPlotConfigDialog();
		d.chart = chart;
		return d;
	}

	public void setOnConfirmListener(OnConfirmListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null ) {
			chart = (SimulationChart) savedInstanceState.getSerializable("chart");
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.simulationPlotDialogTitle);
		
		final LayoutInflater inflater = getActivity().getLayoutInflater();
		View v = inflater.inflate(R.layout.simulation_plot_config_dialog, null);
		builder.setView(v);

		OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		Button okButton = (Button) v.findViewById(R.id.simulationOkButton);
		okButton.setOnClickListener( new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				List<FlightEvent> eventsToShow = new ArrayList<FlightEvent>();
				{
					SparseBooleanArray eventsSelected = eventList.getCheckedItemPositions();
					List<FlightEvent> flightEvents = chart.getFlightDataBranch(CurrentRocketHolder.getCurrentRocket().getRocketDocument()).getEvents();
					for( int i=0; i< flightEvents.size(); i++ ) {
						if ( eventsSelected.get(i) ) {
							FlightEvent event = flightEvents.get(i);
							eventsToShow.add( event );
						}
					}
				}
				chart.setEvents(eventsToShow);

				chart.setSeries1((FlightDataType)series1Spinner.getSelectedItem());
				chart.setSeries2((FlightDataType)series2Spinner.getSelectedItem());

				if ( listener != null ) {
					listener.onConfirm();
				}
				SimulationPlotConfigDialog.this.dismiss();
			}
			
		});

		series1Spinner = (Spinner) v.findViewById(R.id.simulationSeries1);
		series2Spinner = (Spinner) v.findViewById(R.id.simulationSeries2);

		List<FlightDataType> selectableSeries = new ArrayList<FlightDataType>();
		int index = 0;
		for( FlightDataType fdt : chart.getFlightDataBranch(rocketDocument).getTypes() ) {
			if ( fdt == FlightDataType.TYPE_TIME ) { 
			} else {
				selectableSeries.add(fdt);
			}
			index++;
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
		int selection1 = serieses.getPosition(chart.getSeries1());
		series1Spinner.setSelection(selection1);
		series2Spinner.setAdapter(serieses);
		int selection2 = serieses.getPosition(chart.getSeries2());
		series2Spinner.setSelection(selection2);

		eventList = (ListView) v.findViewById(R.id.simulationEventsList);

		FlightDataBranch data = chart.getFlightDataBranch(CurrentRocketHolder.getCurrentRocket().getRocketDocument());
		// Initialize the eventList
		
		ArrayAdapter<FlightEvent> events = new ArrayAdapter<FlightEvent>(getActivity(),android.R.layout.simple_list_item_1,data.getEvents()) {

			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = inflater;
					v = li.inflate(android.R.layout.simple_list_item_multiple_choice,null);
				}
				FlightEvent event = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( event.getType().toString() + " " + event.getTime() + " (s)" );
				return v;
			}

		};
		eventList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		eventList.setAdapter(events);

		for( FlightEvent evt : chart.getEvents() ) {
			eventList.setItemChecked( events.getPosition(evt), true);
		}
		
		return builder.create();

	}

	@Override
	public void onSaveInstanceState(Bundle arg0) {
		super.onSaveInstanceState(arg0);
		arg0.putSerializable("chart", chart);
	}

}
