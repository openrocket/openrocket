package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.simulation.FlightEvent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SimulationEventsDialog extends DialogFragment {

	private SimulationChart chart;
	private ListView eventList;

	public static SimulationEventsDialog newInstance( SimulationChart chart ) {
		SimulationEventsDialog d = new SimulationEventsDialog();
		d.chart = chart;
		return d;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.simulation_event_dialog, container, false);

		eventList = (ListView) v.findViewById(R.id.simulationEventsList);

		OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();
		// Initialize the eventList
		ArrayAdapter<FlightEvent> events = new ArrayAdapter<FlightEvent>(
				getActivity(),
				android.R.layout.simple_list_item_1,
				chart.getFlightDataBranch(rocketDocument).getEvents() ) {

			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = inflater;
					v = li.inflate(android.R.layout.simple_list_item_1,null);
				}
				FlightEvent event = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( event.getType().toString() + " " + event.getTime() + " (s)" );
				return v;
			}

		};
		// Events are not selectable for plotting right now.
		//eventList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		eventList.setAdapter(events);
		return v;
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


}
