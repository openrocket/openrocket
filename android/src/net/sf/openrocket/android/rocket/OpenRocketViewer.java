package net.sf.openrocket.android.rocket;


import java.io.File;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.android.PreferencesActivity;
import net.sf.openrocket.android.motor.MotorHierarchicalBrowser;
import net.sf.openrocket.android.rocket.RocketComponentTreeAdapter.RocketComponentWithId;
import net.sf.openrocket.android.simulation.SimulationViewer;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketUtils;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

public class OpenRocketViewer extends Activity
implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private static final String TAG = "OpenRocketViewer";

	private ProgressDialog progress;

	private Spinner configurationSpinner;
	private TreeViewList componentTree;
	private ListView simulationList;

	private Application app;

	private final static int PICK_ORK_FILE_RESULT = 1;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG,"In onCreate");

		app = (Application) this.getApplication();

		setContentView(R.layout.openrocketviewer);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		TabHost tabs=(TabHost)findViewById(R.id.openrocketviewerTabHost);

		tabs.setup();

		TabHost.TabSpec spec=tabs.newTabSpec("tag1");

		spec.setContent(R.id.openrocketviewerOverview);
		spec.setIndicator("Overview");
		tabs.addTab(spec);

		spec=tabs.newTabSpec("tag2");
		spec.setContent(R.id.openrocketviewerComponentTree);
		spec.setIndicator("Components");
		tabs.addTab(spec);	

		spec=tabs.newTabSpec("tag3");
		spec.setContent(R.id.openrocketviewerSimulationList);
		spec.setIndicator("Simulations");
		tabs.addTab(spec);	

		configurationSpinner = (Spinner) findViewById(R.id.openrocketviewerConfigurationSpinner);
		componentTree = (TreeViewList) findViewById(R.id.openrocketviewerComponentTree);
		simulationList = (ListView) findViewById(R.id.openrocketviewerSimulationList);

		Intent i = getIntent();
		Uri file = i.getData();

		if ( file == null ) {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			startActivityForResult(intent,PICK_ORK_FILE_RESULT);

		} else {
			loadOrkFile(file);
		}
	}

	@Override
	protected void onDestroy() {
		if ( progress != null ) {
			if ( progress.isShowing() ) {
				progress.dismiss();
			}
			progress = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode){
		case PICK_ORK_FILE_RESULT:
			if(resultCode==RESULT_OK){
				Uri file = data.getData();
				loadOrkFile(file);
			}
			break;
		}
	}

	private void loadOrkFile( Uri file ) {
		Log.d(TAG,"Use ork file: " + file);
		String path = file.getPath();
		File orkFile = new File(path);
		progress = ProgressDialog.show(this, "Loading file", "");

		final OpenRocketLoaderTask task = new OpenRocketLoaderTask() {

			/* (non-Javadoc)
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(OpenRocketDocument result) {
				super.onPostExecute(result);
				app.setRocketDocument( result );
				updateContents();
			}

		};

		task.execute(orkFile);

	}

	/* (non-Javadoc)
	 * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// just in case the user changed the units, we redraw.
		PreferencesActivity.initializePreferences(getApplication(), PreferenceManager.getDefaultSharedPreferences(this));
		updateContents();
	}

	private void updateContents() {

		OpenRocketDocument rocketDocument = app.getRocketDocument();
		Rocket rocket = rocketDocument.getRocket();

		setTitle(rocket.getName());

		String[] motorConfigs = rocket.getMotorConfigurationIDs();
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item);
		for( String config: motorConfigs ) {
			spinnerAdapter.add(rocket.getMotorConfigurationNameOrDescription(config));
		}
		
		configurationSpinner.setAdapter(spinnerAdapter);
		
		Unit LengthUnit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		Unit MassUnit = UnitGroup.UNITS_MASS.getDefaultUnit();

		Coordinate cg = RocketUtils.getCG(rocket, MassCalcType.NO_MOTORS);
		double length = RocketUtils.getLength(rocket);
		((TextView)findViewById(R.id.openrocketviewerDesigner)).setText(rocket.getDesigner());
		((TextView)findViewById(R.id.openrocketviewerCG)).setText(LengthUnit.toStringUnit(cg.x) );
		((TextView)findViewById(R.id.openrocketviewerLength)).setText(LengthUnit.toStringUnit(length));
		((TextView)findViewById(R.id.openrocketviewerMass)).setText(MassUnit.toStringUnit(cg.weight));
		((TextView)findViewById(R.id.openrocketviewerStageCount)).setText(String.valueOf(rocket.getStageCount()));
		((TextView)findViewById(R.id.openrocketviewerComment)).setText(rocket.getComment());

		ArrayAdapter<Simulation> sims = new ArrayAdapter<Simulation>(this,android.R.layout.simple_list_item_2,rocketDocument.getSimulations()) {

			@Override
			public View getView(int position, View convertView,	ViewGroup parent) {
				View v = convertView;
				if ( v == null ) {
					LayoutInflater li = getLayoutInflater();
					v = li.inflate(android.R.layout.simple_list_item_2,null);
				}
				Simulation sim = this.getItem(position);
				((TextView)v.findViewById(android.R.id.text1)).setText( sim.getName() );
				((TextView)v.findViewById(android.R.id.text2)).setText( "motors: " + sim.getConfiguration().getMotorConfigurationDescription() + " apogee: " + sim.getSimulatedData().getMaxAltitude() + "m  time: " + sim.getSimulatedData().getFlightTime() + "s");
				return v;
			}

		};
		simulationList.setOnItemClickListener( new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView l, View v, int position, long id) {
				Intent i = new Intent(OpenRocketViewer.this, SimulationViewer.class);
				Log.d(TAG,"onItemClick simulation number " + id );
				i.putExtra("Simulation",(int)id);
				startActivityForResult(i, 1/*magic*/);
			}

		});
		simulationList.setAdapter(sims);

		componentTree.setAdapter( buildAdapter( rocket ) );

		if ( progress.isShowing() ) {
			progress.dismiss();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.rocket_viewer_option_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG,"onMenuItemSelected" + item.getItemId());
		switch(item.getItemId()) {
		case R.id.motor_list_menu_option:
			startMotorBrowser();
			return true;
		case R.id.preference_menu_option:
			Intent intent = new Intent().setClass(this, PreferencesActivity.class);
			this.startActivity(intent);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void startMotorBrowser() {
		Log.d(TAG,"motorBrowserButton clicked");
		Intent i = new Intent(OpenRocketViewer.this, MotorHierarchicalBrowser.class);
		startActivity(i);
	}

	private ListAdapter buildAdapter( Rocket rocket ) {
		/*
		final int[] DEMO_NODES = new int[] { 0, 0, 1, 1, 1, 2, 2, 1,
				1, 2, 1, 0, 0, 0, 1, 2, 3, 2, 0, 0, 1, 2, 0, 1, 2, 0, 1 };
		final int LEVEL_NUMBER = 4;

		TreeStateManager<Long> manager = new InMemoryTreeStateManager<Long>();
		final TreeBuilder<Long> treeBuilder = new TreeBuilder<Long>(manager);
		for (int i = 0; i < DEMO_NODES.length; i++) {
			treeBuilder.sequentiallyAddNextNode((long) i, DEMO_NODES[i]);
		}

		return new SimpleStandardAdapter(this, manager, LEVEL_NUMBER);
		*/
		
		TreeStateManager<RocketComponentWithId> manager = new InMemoryTreeStateManager<RocketComponentWithId>();
		TreeBuilder<RocketComponentWithId> treeBuilder = new TreeBuilder<RocketComponentWithId>(manager);
		
		int depth = buildRecursive( rocket, treeBuilder, 0 );
		return new RocketComponentTreeAdapter(this, manager, depth+1);
	}
	
	long id = 0;
	private int buildRecursive( RocketComponent comp, TreeBuilder<RocketComponentWithId> builder, int depth ) {
		
		
		int maxDepth = depth;
		
		RocketComponentWithId rcid = new RocketComponentWithId(comp, id++);
		
		// Add this component.
		builder.sequentiallyAddNextNode(rcid, depth);
		
		if ( comp.allowsChildren() ) {
			
			for( RocketComponent child : comp.getChildren() ) {
				int childDepth = buildRecursive( child, builder, depth+1);
				if ( childDepth > maxDepth) {
					maxDepth = childDepth;
				}
			}
			
		}
		
		return maxDepth;
	}


}
