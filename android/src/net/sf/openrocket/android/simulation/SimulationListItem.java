package net.sf.openrocket.android.simulation;


import net.sf.openrocket.R;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.Simulation.Status;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SimulationListItem extends LinearLayout {

	private int[] SIMULATION_INVALID = { R.attr.simulation_invalid };
	private int[] SIMULATION_STALE = { R.attr.simulation_stale };
	
	private TextView text1;
	private TextView text2;
	private Status simStatus;
	
	public SimulationListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		loadViews();
	}

	public SimulationListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		loadViews();
	}

	public SimulationListItem(Context context) {
		this(context, null);
	}

	public void setSimulation(Simulation sim) {

		text1.setText( sim.getName() );

		StringBuilder sb = new StringBuilder();
		String motorConfig = sim.getOptions().getMotorConfigurationID();
		sb.append("motors: ").append(sim.getRocket().getMotorConfigurationNameOrDescription(motorConfig));
		Unit distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
		FlightData flightData  = sim.getSimulatedData();
		if ( flightData != null ) {
			sb.append(" apogee: ").append( distanceUnit.toStringUnit(flightData.getMaxAltitude()));
			sb.append(" time: ").append(flightData.getFlightTime()).append("s");
		} else {
			sb.append(" No simulation data");
		}
		text2.setText( sb.toString() );
		
		simStatus = sim.getStatus();

		// Refresh the drawable state so that it includes the status if required.
		refreshDrawableState();

	}

	private void loadViews() {
		this.setOrientation(LinearLayout.HORIZONTAL);
		
		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.simulation_list_item, this, true);

		//        setPadding(fiveDPInPixels, fiveDPInPixels, fiveDPInPixels, fiveDPInPixels);
		//        setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, fiftyDPInPixels));
		//        setBackgroundResource(R.drawable.message_list_item_background);

		text1 = (TextView) findViewById(android.R.id.text1);
		text2 = (TextView) findViewById(android.R.id.text2);
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
        // We are going to add extra state.
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if ( simStatus == Status.OUTDATED || simStatus == Status.NOT_SIMULATED ) {
			return mergeDrawableStates(drawableState, SIMULATION_INVALID );
        	
        } else if ( simStatus == Status.LOADED || simStatus == Status.EXTERNAL ) {
			return mergeDrawableStates(drawableState, SIMULATION_STALE);
        } else {
			return super.onCreateDrawableState(extraSpace);
		}
	}

}
