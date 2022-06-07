package net.sf.openrocket.gui.main.componenttree;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.openrocket.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

/**
 * A TransferHandler that handles dragging components from and to a ComponentTree.
 * Supports both moving and copying (only copying when dragging to a different rocket).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@SuppressWarnings("serial")
public class ComponentTreeTransferHandler extends TransferHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ComponentTreeTransferHandler.class);
	
	private final OpenRocketDocument document;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param document	the document this handler will drop to, used for undo actions.
	 */
	public ComponentTreeTransferHandler(OpenRocketDocument document) {
		this.document = document;
	}
	
	@Override
	public int getSourceActions(JComponent comp) {
		return COPY_OR_MOVE;
	}
	
	@Override
	public Transferable createTransferable(JComponent treeComp) {
		if (!(treeComp instanceof JTree)) {
			throw new BugException("TransferHandler called with component " + treeComp);
		}
		
		JTree tree = (JTree) treeComp;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			return null;
		}
		
		List<RocketComponent> components = new ArrayList<>(ComponentTreeModel.componentsFromPaths(paths));
		components.sort(Comparator.comparing(c -> -c.getParent().getChildPosition(c)));

		// When the parent of a child is in the selection, don't include the child in components
		for (RocketComponent component : new ArrayList<>(components)) {
			if (RocketComponent.listContainsParent(components, component)) {
				components.remove(component);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < components.size(); i++) {
			if (components.get(i) instanceof Rocket) {
				log.info("Attempting to create transferable from Rocket");
				return null;
			}
			sb.append(components.get(i).getComponentName());
			if (i < components.size() - 1) {
				sb.append(", ");
			}
		}

		log.info("Creating transferable from component " + sb.toString());
		return new RocketComponentTransferable(components);
	}
	
	
	
	
	@Override
	public void exportDone(JComponent comp, Transferable trans, int action) {
		// Removal from the old place is implemented already in import, so do nothing
	}
	
	
	
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		List<SourceTarget> targets = getSourceAndTarget(support);

		if (targets == null || targets.size() == 0) {
			return false;
		}

		for (SourceTarget target : targets) {
			Boolean allowed = canImportTarget(support, target);
			if (allowed == null || !allowed) {
				return false;
			}
		}
		return true;
	}

	private Boolean canImportTarget(TransferHandler.TransferSupport support, SourceTarget target) {
		if (target == null) {
			return null;
		}

		boolean allowed = target.destParent.isCompatible(target.child);
		log.trace("Checking validity of drag-drop " + target.toString() + " allowed:" + allowed);

		// Ensure we're not dropping a component onto a child component
		RocketComponent path = target.destParent;
		while (path != null) {
			if (path.equals(target.child)) {
				log.trace("Drop would cause cycle in tree, disallowing.");
				allowed = false;
				break;
			}
			path = path.getParent();
		}

		// If drag-dropping to another rocket always copy
		if (support.getDropAction() == MOVE && target.srcParent.getRoot() != target.destParent.getRoot()) {
			support.setDropAction(COPY);
		}
		return allowed;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		
		// We currently only support drop, not paste
		if (!support.isDrop()) {
			log.warn("Import action is not a drop action");
			return false;
		}
		
		// Sun JRE silently ignores any RuntimeExceptions in importData, yeech!
		try {
			List<SourceTarget> targets = getSourceAndTarget(support);
			if (targets == null || targets.size() == 0) {
				return false;
			}
			
			// Check what action to perform
			int action = support.getDropAction();

			int destIndex = targets.get(0).destIndex;

			// Add undo positions
			switch (action) {
				case MOVE:
					if (targets.size() == 1) {
						document.startUndo("Move component");
					} else {
						document.startUndo("Move components");
					}
					break;
				case COPY:
					if (targets.size() == 1) {
						document.startUndo("Copy component");
					} else {
						document.startUndo("Copy components");
					}
					break;
				default:
					log.warn("Unknown transfer action " + action);
					return false;
			}

			for (SourceTarget target : targets) {
				int targetAction = action;

				if (target.srcParent.getRoot() != target.destParent.getRoot()) {
					// If drag-dropping to another rocket always copy
					log.info("Performing DnD between different rockets, forcing copy action");
					targetAction = TransferHandler.COPY;
				}

				if (!checkImportAction(targetAction, target, destIndex)) {
					document.stopUndo();
					return false;
				}

				destIndex = importDataTarget(targetAction, target, destIndex);
			}
			document.stopUndo();
			return true;

		} catch (final RuntimeException e) {
			// Open error dialog later if an exception has occurred
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					Application.getExceptionHandler().handleErrorCondition(e);
				}
			});
			return false;
		}
	}

	/**
	 * Checks
	 * @param action
	 * @param target
	 * @return
	 */
	private boolean checkImportAction(int action, SourceTarget target, int destIndex) {
		// Check whether move action would be a no-op
		if ((action == MOVE) && (target.srcParent == target.destParent) &&
				(destIndex == target.srcIndex || destIndex == target.srcIndex + 1)) {
			log.info(Markers.USER_MARKER, "Dropped component at the same place as previously: " + target);
			return false;
		}

		return true;
	}

	/**
	 * Moves or copies a RocketComponent in target.
	 *
	 * @param action action to perform
	 * @param target target object containing the RocketComponent to edit
	 * @param destIndex destination index for the component
	 * @return new destination index
	 */
	private int importDataTarget(int action, SourceTarget target, int destIndex) {
		switch (action) {
			case MOVE:
				log.info(Markers.USER_MARKER, "Performing DnD move operation: " + target);

				// If parents are the same, check whether removing the child changes the insert position
				if (target.srcParent == target.destParent && target.srcIndex < destIndex) {
					destIndex--;
				}

				// Freeze rocket.  src and dest are in same rocket, need to freeze only one
				try {
					target.srcParent.getRocket().freeze();
					target.srcParent.removeChild(target.child);
					target.destParent.addChild(target.child, destIndex);
				} finally {
					target.srcParent.getRocket().thaw();	// Unfreeze
				}
				break;
			case COPY:
				log.info(Markers.USER_MARKER, "Performing DnD copy operation: " + target);
				RocketComponent copy = target.child.copy();
				target.destParent.addChild(copy, target.destIndex);
				break;
		}
		return destIndex;
	}


	/**
	 * Fetch the sources and targets for the DnD action.  This method does not perform
	 * checks on whether this action is allowed based on component positioning rules.
	 * 
	 * @param support	the transfer support
	 * @return			list of sources and targets, or <code>null</code> if invalid.
	 */
	private List<SourceTarget> getSourceAndTarget(TransferHandler.TransferSupport support) {
		// We currently only support drop, not paste
		if (!support.isDrop()) {
			log.warn("Import action is not a drop action");
			return null;
		}
		
		// we only import RocketComponentTransferable
		if (!support.isDataFlavorSupported(RocketComponentTransferable.ROCKET_COMPONENT_DATA_FLAVOR)) {
			log.debug("Attempting to import data with data flavors " +
					Arrays.toString(support.getTransferable().getTransferDataFlavors()));
			return null;
		}
		
		// Fetch the drop location and convert it to work around bug 6560955
		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		if (dl.getPath() == null) {
			log.debug("No drop path location available");
			return null;
		}
		MyDropLocation location = convertDropLocation((JTree) support.getComponent(), dl);
		
		
		// Fetch the transferred component (child component)
		Transferable transferable = support.getTransferable();
		List<RocketComponent> children;
		
		try {
			RocketComponentTransferable transfer = (RocketComponentTransferable) transferable.getTransferData(
					RocketComponentTransferable.ROCKET_COMPONENT_DATA_FLAVOR);
			children = transfer.getComponents();
			if (children == null || children.size() == 0) {
				return null;
			}
		} catch (IOException | UnsupportedFlavorException e) {
			throw new BugException(e);
		}

		List<SourceTarget> targets = new LinkedList<>();

		for (RocketComponent child : children) {
			// Get the source component & index
			RocketComponent srcParent = child.getParent();
			if (srcParent == null) {
				log.debug("Attempting to drag root component");
				return null;
			}
			int srcIndex = srcParent.getChildPosition(child);


			// Get destination component & index
			RocketComponent destParent = ComponentTreeModel.componentFromPath(location.path);
			int destIndex = location.index;
			if (destIndex < 0) {
				destIndex = 0;
			}

			targets.add(new SourceTarget(srcParent, srcIndex, destParent, destIndex, child));
		}
		
		return targets;
	}
	
	private class SourceTarget {
		private final RocketComponent srcParent;
		private final int srcIndex;
		private final RocketComponent destParent;
		private final int destIndex;
		private final RocketComponent child;
		
		public SourceTarget(RocketComponent srcParent, int srcIndex, RocketComponent destParent, int destIndex,
				RocketComponent child) {
			this.srcParent = srcParent;
			this.srcIndex = srcIndex;
			this.destParent = destParent;
			this.destIndex = destIndex;
			this.child = child;
		}
		
		@Override
		public String toString() {
			return "[" +
					"srcParent=" + srcParent.getComponentName() +
					", srcIndex=" + srcIndex +
					", destParent=" + destParent.getComponentName() +
					", destIndex=" + destIndex +
					", child=" + child.getComponentName() +
					"]";
		}
		
	}
	
	
	
	/**
	 * Convert the JTree drop location in order to work around bug 6560955
	 * (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6560955).
	 * <p>
	 * This method analyzes whether the user is dropping on top of the last component
	 * of a subtree or the next item in the tree.  The case to fix must fulfill the following
	 * requirements:
	 * <ul>
	 * 	<li> The node before the current insertion node is not a leaf node
	 *  <li> The drop point is on top of the last node of that node
	 * </ul>
	 * <p>
	 * This does not fix the visual clue provided to the user, but fixes the actual drop location.
	 * 
	 * @param tree		the JTree in question
	 * @param location	the original drop location
	 * @return			the updated drop location
	 */
	private MyDropLocation convertDropLocation(JTree tree, JTree.DropLocation location) {
		
		final TreePath originalPath = location.getPath();
		final int originalIndex = location.getChildIndex();
		
		if (originalPath == null || originalIndex <= 0) {
			return new MyDropLocation(location);
		}
		
		// Check whether previous node is a leaf node
		TreeModel model = tree.getModel();
		Object previousNode = model.getChild(originalPath.getLastPathComponent(), originalIndex - 1);
		if (model.isLeaf(previousNode)) {
			return new MyDropLocation(location);
		}
		
		// Find node on top of which the drop occurred
		Point point = location.getDropPoint();
		TreePath dropPath = tree.getPathForLocation(point.x, point.y);
		if (dropPath == null) {
			return new MyDropLocation(location);
		}
		
		// Check whether previousNode is in the ancestry of the actual drop location
		boolean inAncestry = false;
		for (Object o : dropPath.getPath()) {
			if (o == previousNode) {
				inAncestry = true;
				break;
			}
		}
		if (!inAncestry) {
			return new MyDropLocation(location);
		}
		
		// The bug has occurred - insert after the actual drop location
		TreePath correctInsertPath = dropPath.getParentPath();
		int correctInsertIndex = model.getIndexOfChild(correctInsertPath.getLastPathComponent(),
				dropPath.getLastPathComponent()) + 1;
		
		log.trace("Working around Sun JRE bug 6560955: " +
				"converted path=" + ComponentTreeModel.pathToString(originalPath) + " index=" + originalIndex +
				" into path=" + ComponentTreeModel.pathToString(correctInsertPath) +
				" index=" + correctInsertIndex);
		
		return new MyDropLocation(correctInsertPath, correctInsertIndex);
	}
	
	private class MyDropLocation {
		private final TreePath path;
		private final int index;
		
		public MyDropLocation(JTree.DropLocation location) {
			this(location.getPath(), location.getChildIndex());
		}
		
		public MyDropLocation(TreePath path, int index) {
			this.path = path;
			this.index = index;
		}
		
	}
}
