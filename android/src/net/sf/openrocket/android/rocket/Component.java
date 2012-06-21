package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.CurrentRocketHolder;
import net.sf.openrocket.android.rocket.RocketComponentTreeAdapter.RocketComponentWithId;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

public class Component extends Fragment {

	private TreeViewList componentTree;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.rocket_component, container, false);
		componentTree = (TreeViewList) v.findViewById(R.id.openrocketviewerComponentTree);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		final OpenRocketDocument rocketDocument = CurrentRocketHolder.getCurrentRocket().getRocketDocument();
		componentTree.setAdapter( buildAdapter( rocketDocument.getRocket() ) );
	}

	private ListAdapter buildAdapter( Rocket rocket ) {

		TreeStateManager<RocketComponentWithId> manager = new InMemoryTreeStateManager<RocketComponentWithId>();
		TreeBuilder<RocketComponentWithId> treeBuilder = new TreeBuilder<RocketComponentWithId>(manager);

		int depth = buildRecursive( rocket, treeBuilder, 0 );
		return new RocketComponentTreeAdapter(this.getActivity(), manager, depth+1);
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
