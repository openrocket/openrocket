package net.sf.openrocket.android.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

public class SimulationViewer extends Activity {

	private final static String TAG = "SimulationViewer";

	private ListView eventList;
	private Spinner series1Spinner;
	private Spinner series2Spinner;

	private Simulation sim;
	private FlightDataBranch data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate Bundle = "+ String.valueOf(savedInstanceState));
		setContentView(R.layout.simulation_detail);

		Intent i = getIntent();
		int simnumber = i.getIntExtra("Simulation", 0);
		sim = ((Application)this.getApplication()).getRocketDocument().getSimulation(simnumber);
		data = sim.getSimulatedData().getBranch(0);

		TabHost tabs=(TabHost)findViewById(R.id.simulationConfigurationForm);

		tabs.setup();

		TabHost.TabSpec spec=tabs.newTabSpec("tag1");

		spec.setContent(R.id.simulationEventsList);
		spec.setIndicator("Events");
		tabs.addTab(spec);

		spec=tabs.newTabSpec("tag2");
		spec.setContent(R.id.simulationSeriesSelection);
		spec.setIndicator("Series");
		tabs.addTab(spec);	

		eventList = (ListView) findViewById(R.id.simulationEventsList);

		// Initialize the eventList
		ArrayAdapter<FlightEvent> events = new ArrayAdapter<FlightEvent>(this,android.R.layout.simple_list_item_multiple_choice,data.getEvents()) {

			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = getLayoutInflater();
					v = li.inflate(android.R.layout.simple_list_item_multiple_choice,null);
				}
				FlightEvent event = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( event.getType().toString() + " " + event.getTime() + " (s)" );
				return v;
			}

		};
		eventList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		eventList.setAdapter(events);

		series1Spinner = (Spinner) findViewById(R.id.simulationSeries1);
		series2Spinner = (Spinner) findViewById(R.id.simulationSeries2);

		List<FlightDataType> selectableSeries = new ArrayList<FlightDataType>();
		for( FlightDataType fdt : data.getTypes() ) {
			if ( fdt == FlightDataType.TYPE_TIME ) { 

			} else {
				selectableSeries.add(fdt);
			}
		}
		ArrayAdapter<FlightDataType> serieses = new ArrayAdapter<FlightDataType>(this,android.R.layout.simple_spinner_item,selectableSeries) {

			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = getLayoutInflater();
					v = li.inflate(android.R.layout.simple_spinner_item,null);
				}
				FlightDataType fdt = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( fdt.toString() );
				return v;
			}

		};
		series1Spinner.setAdapter(serieses);
		series2Spinner.setAdapter(serieses);

	}

	public void draw( View v ) {
		List<FlightEvent> eventsToShow = new ArrayList<FlightEvent>();
		{
			SparseBooleanArray eventsSelected = eventList.getCheckedItemPositions();
			List<FlightEvent> flightEvents = data.getEvents();
			for( int i=0; i< flightEvents.size(); i++ ) {
				if ( eventsSelected.get(i) ) {
					eventsToShow.add(flightEvents.get(i) );
				}
			}
		}
		FlightDataType series1 = (FlightDataType) series1Spinner.getSelectedItem();
		Log.d(TAG,"sereis1 = " + series1.toString());
		FlightDataType series2 = (FlightDataType) series2Spinner.getSelectedItem();
		Log.d(TAG,"series2 = " + series2.toString());

		SimulationChart chart = new SimulationChart();
		chart.setFlightDataBranch(data);
		chart.setSeries1(series1);
		chart.setSeries2(series2);
		chart.setFlightEvents(eventsToShow);
		
		startActivity(chart.execute(this));
	}

}
