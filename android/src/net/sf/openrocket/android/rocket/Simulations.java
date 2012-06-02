package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class Simulations extends SherlockFragment
implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private final static String wizardFrag = "wizardFrag";

	public interface OnSimulationSelectedListener {
		public void onSimulationSelected( int simulationId );
	}

	private ListView simulationList;
	private OnSimulationSelectedListener listener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.rocket_simulations, container, false);
		simulationList = (ListView) v.findViewById(R.id.openrocketviewerSimulationList);
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.rocket_viewer_simulation_option_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.menu_add:
			addSimulation();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);

		setup();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if ( this.isVisible() ) {
			setup();
		}
	}


	public void refreshSimulationList() {
		setup();
	}
	
	private void setup() {
		final OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();
		AndroidLogWrapper.d(Simulations.class,"activity = {0}", this.getActivity());

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
				FlightData flightData  = sim.getSimulatedData();
				if ( flightData != null ) {
					sb.append(" apogee: ").append( distanceUnit.toStringUnit(flightData.getMaxAltitude()));
					sb.append(" time: ").append(flightData.getFlightTime()).append("s");
					((TextView)v.findViewById(android.R.id.text2)).setText( sb.toString() );
				} else {
					((TextView)v.findViewById(android.R.id.text2)).setText("No simulation data");
				}
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
		simulationList.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final SimulationEditFragment f = SimulationEditFragment.newInstance(position);
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				ft.add(f, wizardFrag);
				ft.commit();

				return true;
			}
			
		});
		simulationList.setAdapter(sims);

	}

	private void addSimulation() {
		CurrentRocketHolder.getCurrentRocket().addNewSimulation();
	}
	
}
