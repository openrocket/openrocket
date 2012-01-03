package net.sf.openrocket.android.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TabHost;
import android.widget.TextView;

public class SimulationViewer extends FragmentActivity
implements SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerOpenListener {

	private final static String TAG = "MotorDetails";

	private SlidingDrawer slidingDrawer;
	private ImageView handle;

	private ListView eventList;
	private ListView seriesList;

	private SimulationPlotFragment simPlot;

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

		simPlot = (SimulationPlotFragment) getSupportFragmentManager().findFragmentById(R.id.simulationPlotFragment);

		slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);

		slidingDrawer.setOnDrawerOpenListener(this);
		slidingDrawer.setOnDrawerCloseListener(this);

		handle = (ImageView) findViewById(R.id.handle);

		TabHost tabs=(TabHost)findViewById(R.id.simulationConfigurationForm);

		tabs.setup();

		TabHost.TabSpec spec=tabs.newTabSpec("tag1");

		spec.setContent(R.id.simulationEventsList);
		spec.setIndicator("Events");
		tabs.addTab(spec);

		spec=tabs.newTabSpec("tag2");
		spec.setContent(R.id.simulationSeriesList);
		spec.setIndicator("Series");
		tabs.addTab(spec);	

		eventList = (ListView) findViewById(R.id.simulationEventsList);

		seriesList = (ListView) findViewById(R.id.simulationSeriesList);

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

		List<FlightDataType> selectableSeries = new ArrayList<FlightDataType>();
		for( FlightDataType fdt : data.getTypes() ) {
			if ( fdt == FlightDataType.TYPE_TIME ) { 

			} else {
				selectableSeries.add(fdt);
			}
		}
		ArrayAdapter<FlightDataType> serieses = new ArrayAdapter<FlightDataType>(this,android.R.layout.simple_list_item_multiple_choice,selectableSeries) {

			@Override
			public View getView(int position, View convertView,
					ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = getLayoutInflater();
					v = li.inflate(android.R.layout.simple_list_item_multiple_choice,null);
				}
				FlightDataType fdt = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( fdt.toString() );
				return v;
			}

		};
		seriesList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		seriesList.setAdapter(serieses);
		redraw();

	}

	@Override
	public void onDrawerOpened() {
		handle.setImageResource(R.drawable.arrow_down_float);
	}

	@Override
	public void onDrawerClosed() {
		handle.setImageResource(R.drawable.arrow_up_float);
		redraw();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.motor_details_option_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case R.id.save:
			// Extract form data to Motor.
			// Save motor.
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void redraw() {
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
		FlightDataType selectedSeries = null;
		{
			int selected = seriesList.getCheckedItemPosition();
			if ( selected >= 0 ) {
				selectedSeries = (FlightDataType) seriesList.getAdapter().getItem(selected);
			}
		}

		simPlot.init(data, selectedSeries, eventsToShow );

	}

}
