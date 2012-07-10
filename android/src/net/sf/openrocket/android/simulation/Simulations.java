package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.util.AndroidLogWrapper;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
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

	}

	@Override
	public void onResume() {
		super.onResume();
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
	            SimulationListItem listItemView = (SimulationListItem) convertView;

	            if (listItemView == null) {
	            	listItemView = new SimulationListItem(parent.getContext());
	            }

				Simulation sim = this.getItem(position);
				listItemView.setSimulation(sim);

	            return listItemView;
			}

		};
		simulationList.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView l, View v, int position, long id) {
				Simulation sim = CurrentRocketHolder.getCurrentRocket().getRocketDocument().getSimulation(position);
				// Check if there is data for this simulation.
				if ( sim.getSimulatedData() == null || sim.getSimulatedData().getBranchCount() == 0 ) {
					openEditor(position);
				} else if (listener != null ) {
					listener.onSimulationSelected(position);
				}
			}

		});
		simulationList.setOnItemLongClickListener( new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				openEditor(position);

				return true;
			}
			
		});
		simulationList.setAdapter(sims);

	}
	
	private void openEditor( int position ) {
		final SimulationEditFragment f = SimulationEditFragment.newInstance(position);
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.add(f, wizardFrag);
		ft.commit();
	}

	private void addSimulation() {
		CurrentRocketHolder.getCurrentRocket().addNewSimulation(getActivity());
	}
	
}
