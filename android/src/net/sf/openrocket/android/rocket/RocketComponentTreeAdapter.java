package net.sf.openrocket.android.rocket;

import net.sf.openrocket.R;
import net.sf.openrocket.android.rocket.RocketComponentTreeAdapter.RocketComponentWithId;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This is a very simple adapter that provides very basic tree view with a
 * checkboxes and simple item description.
 * 
 */
public class RocketComponentTreeAdapter extends AbstractTreeViewAdapter<RocketComponentWithId> {

	static class RocketComponentWithId {
		RocketComponent component;
		long id;
		public RocketComponentWithId( RocketComponent c, long id ) {
			this.component = c;
			this.id = id;
		}
	}
	
	public RocketComponentTreeAdapter(final Activity treeViewListDemo,
			TreeStateManager<RocketComponentWithId> manager,
			final int numberOfLevels) {
		super(treeViewListDemo, manager, numberOfLevels);
	}

	private String getDescription(final RocketComponentWithId id) {
		return  id.component.getName();
	}

	@Override
	public View getNewChildView(final TreeNodeInfo<RocketComponentWithId> treeNodeInfo) {
		final View viewLayout = getActivity().getLayoutInflater().inflate(R.layout.component_list_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public View updateView(final View view,
			final TreeNodeInfo<RocketComponentWithId> treeNodeInfo) {
		final View viewLayout = view;
		final TextView descriptionView = (TextView) viewLayout.findViewById(android.R.id.text1);
		descriptionView.setText(getDescription(treeNodeInfo.getId()));
		return viewLayout;
	}

	@Override
	public void handleItemClick(final View view, final Object id) {
		final RocketComponentWithId longId = (RocketComponentWithId) id;
		final TreeNodeInfo<RocketComponentWithId> info = getManager().getNodeInfo(longId);
		if (info.isWithChildren()) {
			super.handleItemClick(view, id);
		} else {
			final ViewGroup vg = (ViewGroup) view;
			// perform click on child item
		}
	}

	@Override
	public long getItemId(final int position) {
		RocketComponentWithId rcid = getTreeId(position);
		return rcid.id;
	}
}