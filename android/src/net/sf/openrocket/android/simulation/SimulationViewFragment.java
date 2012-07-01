
package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.document.OpenRocketDocument;

import org.achartengine.GraphicalView;
import org.achartengine.chart.XYChart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationViewFragment extends SherlockFragment implements SimulationPlotConfigDialog.OnConfirmListener {
	
	SimulationChart chart;

	ViewGroup container;
	
	/** The encapsulated graphical view. */
	private GraphicalView mView;
	/** The chart to be drawn. */
	private XYChart mChart;

	public static SimulationViewFragment newInstance( SimulationChart chart ) {
		SimulationViewFragment frag = new SimulationViewFragment();
		frag.chart = chart;
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
		setHasOptionsMenu(true);
		OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		this.container = container;
		if (savedInstanceState != null ) {
			chart = (SimulationChart) savedInstanceState.getSerializable("chart");
		}
		mChart = chart.buildChart(rocketDocument);
		mView = new GraphicalView(container.getContext(), mChart);
		return mView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.simulation_option_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.simulation_config_plot_menu_option:
			SimulationPlotConfigDialog seriesDialog = SimulationPlotConfigDialog.newInstance(chart);
			seriesDialog.show(getFragmentManager(), "AbraCadaver");
			seriesDialog.setOnConfirmListener(this);
			return true;
		case R.id.simulation_view_events_menu_option:
			SimulationEventsDialog eventsDialog = SimulationEventsDialog.newInstance(chart);
			eventsDialog.show(getFragmentManager(), "AbraCadaver");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfirm() {
		OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();

		mChart = chart.buildChart(rocketDocument);
		ViewGroup parent = (ViewGroup) mView.getParent();
		parent.removeView(mView);
		mView = new GraphicalView(container.getContext(), mChart);
		parent.addView(mView);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("chart", chart);

	}
	
}