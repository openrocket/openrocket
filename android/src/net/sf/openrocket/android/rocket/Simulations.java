package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Simulations extends Fragment {

	public interface OnSimulationSelectedListener {
		public void onSimulationSelected( int simulationId );
	}
	
	private ListView simulationList;
	private OnSimulationSelectedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.rocket_simulations, container, false);
		simulationList = (ListView) v.findViewById(R.id.openrocketviewerSimulationList);

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if ( activity instanceof OnSimulationSelectedListener ) {
			listener = (OnSimulationSelectedListener) activity;
		}
	}


	public void setListener(OnSimulationSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();
		Log.d("sim","activity = " + this.getActivity());

		ArrayAdapter<Simulation> sims = new ArrayAdapter<Simulation>(this.getActivity(),android.R.layout.simple_list_item_2,rocketDocument.getSimulations()) {

			@Override
			public View getView(int position, View convertView,	ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = getActivity().getLayoutInflater();
					v = li.inflate(android.R.layout.simple_list_item_2,null);
				}
				Simulation sim = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( sim.getName() );
				StringBuilder sb = new StringBuilder();
				sb.append("motors: ").append(sim.getConfiguration().getMotorConfigurationDescription());
				Unit distanceUnit = UnitGroup.UNITS_DISTANCE.getDefaultUnit();
				sb.append(" apogee: ").append( distanceUnit.toStringUnit(sim.getSimulatedData().getMaxAltitude()));
				sb.append(" time: ").append(sim.getSimulatedData().getFlightTime()).append("s");
				((TextView)v.findViewById(android.R.id.text2)).setText( sb.toString() );
				return v;
			}

		};
		simulationList.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView l, View v, int position, long id) {
				if (listener != null ) {
					listener.onSimulationSelected(position);
				}
			}

		});
		simulationList.setAdapter(sims);

	}

}
