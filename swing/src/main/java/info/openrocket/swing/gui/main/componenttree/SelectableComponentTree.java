package info.openrocket.swing.gui.main.componenttree;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.RocketComponent;

import javax.swing.tree.TreePath;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class SelectableComponentTree extends ComponentTree {

	public SelectableComponentTree(OpenRocketDocument document, List<RocketComponent> enabledComponents,
								   List<RocketComponent> initialSelection) {
		super(document);
		Set<RocketComponent> enabledComponents1 = new HashSet<>(enabledComponents);

		// Use a custom cell renderer that considers the enabled state
		setCellRenderer(new SelectableComponentTreeRenderer(enabledComponents1));

		// Use a custom selection model that allows toggling of selection
		ToggleTreeSelectionModel selectionModel = new ToggleTreeSelectionModel();
		setSelectionModel(selectionModel);

		// Disable component selection for disabled components
		for (RocketComponent component : document.getRocket()) {
			if (!enabledComponents.contains(component)) {
				TreePath path = ComponentTreeModel.makeTreePath(component);
				selectionModel.addDisabledPath(path);
			}
		}

		// Apply initial selection
		selectionModel.setSelectionPath(null);
		if (initialSelection != null && !initialSelection.isEmpty()) {
			for (RocketComponent component : initialSelection) {
				TreePath path = ComponentTreeModel.makeTreePath(component);
				selectionModel.addSelectionPath(path);
			}
		}
	}
}