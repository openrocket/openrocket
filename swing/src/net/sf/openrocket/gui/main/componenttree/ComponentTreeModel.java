package net.sf.openrocket.gui.main.componenttree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;


/**
 * A TreeModel that implements viewing of the rocket tree structure.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ComponentTreeModel implements TreeModel, ComponentChangeListener {
	ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();
	
	private final RocketComponent root;
	private final JTree tree;
	
	public ComponentTreeModel(RocketComponent root, JTree tree) {
		this.root = root;
		this.tree = tree;
		root.addComponentChangeListener(this);
	}
	
	
	@Override
	public Object getChild(Object parent, int index) {
		RocketComponent component = (RocketComponent) parent;
		
		try {
			return component.getChild(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	
	
	@Override
	public int getChildCount(Object parent) {
		RocketComponent c = (RocketComponent) parent;
		
		return c.getChildCount();
	}
	
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == null || child == null)
			return -1;
		
		RocketComponent p = (RocketComponent) parent;
		RocketComponent c = (RocketComponent) child;
		
		return p.getChildPosition(c);
	}
	
	@Override
	public Object getRoot() {
		return root;
	}
	
	@Override
	public boolean isLeaf(Object node) {
		return !((RocketComponent) node).allowsChildren();
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}
	
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}
	
	private void fireTreeNodeChanged(RocketComponent node) {
		TreeModelEvent e = new TreeModelEvent(this, makeTreePath(node), null, null);
		Object[] l = listeners.toArray();
		for (int i = 0; i < l.length; i++)
			((TreeModelListener) l[i]).treeNodesChanged(e);
	}
	
	
	private void fireTreeStructureChanged(RocketComponent source) {
		Object[] path = { root };
		

		// Get currently expanded path IDs
		Enumeration<TreePath> enumer = tree.getExpandedDescendants(new TreePath(path));
		ArrayList<String> expanded = new ArrayList<String>();
		if (enumer != null) {
			while (enumer.hasMoreElements()) {
				TreePath p = enumer.nextElement();
				expanded.add(((RocketComponent) p.getLastPathComponent()).getID());
			}
		}
		
		// Send structure change event
		TreeModelEvent e = new TreeModelEvent(this, path);
		Object[] l = listeners.toArray();
		for (int i = 0; i < l.length; i++)
			((TreeModelListener) l[i]).treeStructureChanged(e);
		
		// Re-expand the paths
		for (String id : expanded) {
			RocketComponent c = root.findComponent(id);
			if (c == null)
				continue;
			tree.expandPath(makeTreePath(c));
		}
		if (source != null) {
			TreePath p = makeTreePath(source);
			tree.makeVisible(p);
			tree.expandPath(p);
		}
	}
	
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.err.println("ERROR: valueForPathChanged called?!");
	}
	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if (e.isTreeChange() || e.isUndoChange() || e.isMassChange()) {
			// Tree must be fully updated also in case of an undo change 
			fireTreeStructureChanged(e.getSource());
			if (e.isTreeChange() && e.isUndoChange()) {
				// If the undo has changed the tree structure, some elements may be hidden
				// unnecessarily
				// TODO: LOW: Could this be performed better?
				expandAll();
			}
		} else if (e.isOtherChange()) {
			fireTreeNodeChanged(e.getSource());
		}
	}
	
	public void expandAll() {
		Iterator<RocketComponent> iterator = root.iterator(false);
		while (iterator.hasNext()) {
			tree.makeVisible(makeTreePath(iterator.next()));
		}
	}
	
	
	/**
	 * Return the rocket component that a TreePath object is referring to.
	 * 
	 * @param path	the TreePath
	 * @return		the RocketComponent the path is referring to.
	 * @throws NullPointerException		if path is null
	 * @throws BugException				if the path does not refer to a RocketComponent
	 */
	public static RocketComponent componentFromPath(TreePath path) {
		Object last = path.getLastPathComponent();
		if (!(last instanceof RocketComponent)) {
			throw new BugException("Tree path does not refer to a RocketComponent: " + path.toString());
		}
		return (RocketComponent) last;
	}
	
	
	/**
	 * Return a TreePath corresponding to the specified rocket component.
	 * 
	 * @param component		the rocket component
	 * @return				a TreePath corresponding to this RocketComponent
	 */
	public static TreePath makeTreePath(RocketComponent component) {
		if (component == null) {
			throw new NullPointerException();
		}
		
		RocketComponent c = component;
		
		List<RocketComponent> list = new LinkedList<RocketComponent>();
		
		while (c != null) {
			list.add(0, c);
			c = c.getParent();
		}
		
		return new TreePath(list.toArray());
	}
	
	
	/**
	 * Return a string describing the path, using component normal names.
	 * 
	 * @param treePath	the tree path
	 * @return			a string representation
	 */
	public static String pathToString(TreePath treePath) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (Object o : treePath.getPath()) {
			if (sb.length() > 1) {
				sb.append("; ");
			}
			if (o instanceof RocketComponent) {
				sb.append(((RocketComponent) o).getComponentName());
			} else {
				sb.append(o.toString());
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
