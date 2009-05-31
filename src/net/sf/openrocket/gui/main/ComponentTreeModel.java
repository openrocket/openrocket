package net.sf.openrocket.gui.main;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;


/**
 * A TreeModel that implements viewing of the rocket tree structure.
 * This transforms the internal view (which has nested Stages) into the user-view
 * (which has parallel Stages).
 * 
 * To view with the internal structure, switch to using BareComponentTreeModel in
 * ComponentTree.java.  NOTE: This class's makeTreePath will still be used, which
 * will create illegal paths, which results in problems with selections. 
 * 
 * TODO: MEDIUM: When converting a component to another component this model given 
 * outdated information, since it uses the components themselves as the nodes.
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
	
	
	public Object getChild(Object parent, int index) {
		RocketComponent component = (RocketComponent)parent;
		
		try {
			return component.getChild(index);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}
	

	public int getChildCount(Object parent) {
		RocketComponent c = (RocketComponent)parent;

		return c.getChildCount();
	}

	
	public int getIndexOfChild(Object parent, Object child) {
		if (parent==null || child==null)
			return -1;
		
		RocketComponent p = (RocketComponent)parent;
		RocketComponent c = (RocketComponent)child;
		
		return p.getChildPosition(c);
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		RocketComponent c = (RocketComponent)node;

		return (c.getChildCount() == 0);
	}

	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	public void removeTreeModelListener(TreeModelListener l) {
		listeners.remove(l);
	}
	
	private void fireTreeNodesChanged() {
		Object[] path = { root };
		TreeModelEvent e = new TreeModelEvent(this,path);
		Object[] l = listeners.toArray();
		for (int i=0; i<l.length; i++)
			((TreeModelListener)l[i]).treeNodesChanged(e);
	}
	
	
	@SuppressWarnings("unused")
	private void printStructure(TreePath p, int level) {
		String indent="";
		for (int i=0; i<level; i++)
			indent += "  ";
		System.out.println(indent+p+
				": isVisible:"+tree.isVisible(p)+
				" isCollapsed:"+tree.isCollapsed(p)+
				" isExpanded:"+tree.isExpanded(p));
		Object parent = p.getLastPathComponent();
		for (int i=0; i<getChildCount(parent); i++) {
			Object child = getChild(parent,i);
			TreePath path = makeTreePath((RocketComponent)child);
			printStructure(path,level+1);
		}
	}
	
	
	private void fireTreeStructureChanged(RocketComponent source) {
		Object[] path = { root };
		
		
		// Get currently expanded path IDs
		Enumeration<TreePath> enumer = tree.getExpandedDescendants(new TreePath(path));
		ArrayList<String> expanded = new ArrayList<String>();
		if (enumer != null) {
			while (enumer.hasMoreElements()) {
				TreePath p = enumer.nextElement();
				expanded.add(((RocketComponent)p.getLastPathComponent()).getID());
			}
		}
		
		// Send structure change event
		TreeModelEvent e = new TreeModelEvent(this,path);
		Object[] l = listeners.toArray();
		for (int i=0; i<l.length; i++)
			((TreeModelListener)l[i]).treeStructureChanged(e);
		
		// Re-expand the paths
		for (String id: expanded) {
			RocketComponent c = root.findComponent(id);
			if (c==null)
				continue;
			tree.expandPath(makeTreePath(c));
		}
		if (source != null) {
			TreePath p = makeTreePath(source);
			tree.makeVisible(p);
			tree.expandPath(p);
		}
	}
	
	public void valueForPathChanged(TreePath path, Object newValue) {
		System.err.println("ERROR: valueForPathChanged called?!");
	}


	public void componentChanged(ComponentChangeEvent e) {
		if (e.isTreeChange() || e.isUndoChange()) {
			// Tree must be fully updated also in case of an undo change 
			fireTreeStructureChanged((RocketComponent)e.getSource());
			if (e.isTreeChange() && e.isUndoChange()) {
				// If the undo has changed the tree structure, some elements may be hidden
				// unnecessarily
				// TODO: LOW: Could this be performed better?
				expandAll();
			}
		} else if (e.isOtherChange()) {
			fireTreeNodesChanged();
		}
	}

	public void expandAll() {
		Iterator<RocketComponent> iterator = root.deepIterator();
		while (iterator.hasNext()) {
			tree.makeVisible(makeTreePath(iterator.next()));
		}
	}

	
	public static TreePath makeTreePath(RocketComponent component) {
		RocketComponent c = component;
	
		List<RocketComponent> list = new ArrayList<RocketComponent>();
		
		while (c != null) {
			list.add(0,c);
			c = c.getParent();
		}
		
		return new TreePath(list.toArray());
	}
	
}
