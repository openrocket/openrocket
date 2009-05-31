package net.sf.openrocket.gui.main;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

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
 * This class shows the internal structure of the tree, as opposed to the regular
 * user-side view.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class BareComponentTreeModel implements TreeModel, ComponentChangeListener {
	ArrayList<TreeModelListener> listeners = new ArrayList<TreeModelListener>();

	private final RocketComponent root;
	private final JTree tree;

	public BareComponentTreeModel(RocketComponent root, JTree tree) {
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
		return ((RocketComponent)parent).getChildCount();
	}

	public int getIndexOfChild(Object parent, Object child) {
		RocketComponent p = (RocketComponent)parent;
		RocketComponent c = (RocketComponent)child;
		return p.getChildPosition(c);
	}

	public Object getRoot() {
		return root;
	}

	public boolean isLeaf(Object node) {
		RocketComponent c = (RocketComponent)node;
		return (c.getChildCount()==0);
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
		Iterator<String> iter = expanded.iterator();
		while (iter.hasNext()) {
			RocketComponent c = root.findComponent(iter.next());
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
				// If the undo has changed the tree structure, some elements may be hidden unnecessarily
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
		int count = 0;
		RocketComponent c = component;
		
		while (c != null) {
			count++;
			c = c.getParent();
		}
		
		Object[] list = new Object[count];
		
		count--;
		c=component;
		while (c!=null) {
			list[count] = c;
			count--;
			c = c.getParent();
		}
		
		return new TreePath(list);
	}
	
}
