package info.openrocket.swing.gui.main.componenttree;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;

public class ToggleTreeSelectionModel extends DefaultTreeSelectionModel {
	private final Set<TreePath> disabledPaths;

	public ToggleTreeSelectionModel() {
		setSelectionMode(DISCONTIGUOUS_TREE_SELECTION);
		disabledPaths = new HashSet<>();
	}

	@Override
	public void setSelectionPath(TreePath path) {
		if (isPathEnabled(path)) {
			if (isPathSelected(path)) {
				removeSelectionPath(path);
			} else {
				addSelectionPath(path);
			}
		}
	}

	@Override
	public void addSelectionPath(TreePath path) {
		if (isPathEnabled(path)) {
			super.addSelectionPath(path);
		}
	}

	@Override
	public void removeSelectionPath(TreePath path) {
		if (isPathEnabled(path)) {
			super.removeSelectionPath(path);
		}
	}

	public void addDisabledPath(TreePath path) {
		disabledPaths.add(path);
		removeSelectionPath(path);
	}

	public void removeDisabledPath(TreePath path) {
		disabledPaths.remove(path);
	}

	public boolean isPathEnabled(TreePath path) {
		return !disabledPaths.contains(path);
	}
}