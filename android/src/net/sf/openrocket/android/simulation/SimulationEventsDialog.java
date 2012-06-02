package net.sf.openrocket.android.simulation;

import java.util.List;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

public class SimulationEventsDialog extends DialogFragment {

	private SimulationChart chart;
	private TableLayout eventList;

	public static SimulationEventsDialog newInstance( SimulationChart chart ) {
		SimulationEventsDialog d = new SimulationEventsDialog();
		d.chart = chart;
		return d;
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.simulation_event_dialog, container, false);

		eventList = (TableLayout) v.findViewById(R.id.simulationEventsList);
		eventList.setColumnShrinkable(0, true);

		final OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		List<FlightEvent> events = chart.getFlightDataBranch(rocketDocument).getEvents();
		
		for ( FlightEvent event : events ) {

			View tableRow = inflater.inflate(R.layout.simulation_event_item,null);
			((TextView)tableRow.findViewById(R.id.eventName)).setText( event.getType().toString() );
			((TextView)tableRow.findViewById(R.id.eventTime)).setText( event.getTime() + " (s)" );
			
			FlightDataBranch data = chart.getFlightDataBranch(rocketDocument);
			double vel = MathUtil.interpolate(data.get(FlightDataType.TYPE_TIME), data.get(FlightDataType.TYPE_VELOCITY_TOTAL), event.getTime());
			((TextView)tableRow.findViewById(R.id.eventVelocity)).setText( UnitGroup.UNITS_VELOCITY.getDefaultUnit().toStringUnit(vel) );

			double alt = MathUtil.interpolate(data.get(FlightDataType.TYPE_TIME), data.get(FlightDataType.TYPE_ALTITUDE), event.getTime());
			((TextView)tableRow.findViewById(R.id.eventAltitude)).setText( UnitGroup.UNITS_DISTANCE.getDefaultUnit().toStringUnit(alt) );

			eventList.addView( tableRow );
		}
		
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
