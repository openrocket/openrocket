/*
 * RocketPrintTree.java
 */
package net.sf.openrocket.gui.print.components;

import net.sf.openrocket.gui.print.OpenRocketPrintable;
import net.sf.openrocket.gui.print.PrintableContext;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A specialized JTree for displaying various rocket items that can be printed.
 */
public class RocketPrintTree extends JTree {
	
	/**
	 * All check boxes are initially set to true (selected).
	 */
	public static final boolean INITIAL_CHECKBOX_SELECTED = true;
	
	/**
	 * The selection model that tracks the check box state.
	 */
	private TreeSelectionModel theCheckBoxSelectionModel;
	
	/**
	 * Constructor.
	 *
	 * @param root the vector of check box nodes (rows) to place into the tree
	 */
	private RocketPrintTree(Vector root) {
		super(root);
		
		//Remove the little down and sideways arrows.  These are not needed because the tree expansion is fixed.
		((javax.swing.plaf.basic.BasicTreeUI) this.getUI()).
				setExpandedIcon(null);
		((javax.swing.plaf.basic.BasicTreeUI) this.getUI()).
				setCollapsedIcon(null);
	}
	
	/**
	 * Factory method to create a specialized JTree.  This version is for rocket's that have more than one stage.
	 *
	 * @param rocketName the name of the rocket
	 * @param stages     the array of all stages
	 *
	 * @return an instance of JTree
	 */
	public static RocketPrintTree create(String rocketName, List<RocketComponent> stages) {
		Vector root = new Vector();
		Vector toAddTo = root;
		
		if (stages != null) {
			if (stages.size() > 1) {
				final Vector parent = new NamedVector(rocketName != null ? rocketName : "Rocket");
				
				root.add(parent);
				toAddTo = parent;
			}
			for (RocketComponent stage : stages) {
				if (stage instanceof Stage) {
					toAddTo.add(createNamedVector(stage.getName(), createPrintTreeNode(true), stage.getStageNumber()));
				}
			}
		}

        List<OpenRocketPrintable> unstaged = OpenRocketPrintable.getUnstaged();
        for (int i = 0; i < unstaged.size(); i++) {
		    toAddTo.add(new CheckBoxNode(unstaged.get(i).getDescription(),
										INITIAL_CHECKBOX_SELECTED));
        }

		RocketPrintTree tree = new RocketPrintTree(root);
		
		tree.addTreeWillExpandListener
				(new TreeWillExpandListener() {
					@Override
					public void treeWillExpand(TreeExpansionEvent e) {
					}
					
					@Override
					public void treeWillCollapse(TreeExpansionEvent e)
							throws ExpandVetoException {
						throw new ExpandVetoException(e, "you can't collapse this JTree");
					}
				});
		
		return tree;
	}
	
	/**
	 * Factory method to create a specialized JTree.  This version is for a rocket with only one stage.
	 *
	 * @param rocketName the name of the rocket
	 *
	 * @return an instance of JTree
	 */
	public static RocketPrintTree create(String rocketName) {
		Vector root = new Vector();
		root.add(new NamedVector(rocketName != null ? rocketName : "Rocket", createPrintTreeNode(false)));
		
		RocketPrintTree tree = new RocketPrintTree(root);
		
		tree.addTreeWillExpandListener
				(new TreeWillExpandListener() {
					@Override
					public void treeWillExpand(TreeExpansionEvent e) {
					}
					
					@Override
					public void treeWillCollapse(TreeExpansionEvent e)
							throws ExpandVetoException {
						throw new ExpandVetoException(e, "you can't collapse this JTree");
					}
				});
		
		return tree;
	}
	
	/**
	 * This tree needs to have access both to the normal selection model (for the textual row) which is managed by the
	 * superclass, as well as the selection model for the check boxes.  This mutator method allows an external class to
	 * set the model back onto this class.  Because of some unfortunate circular dependencies this cannot be set at
	 * construction.
	 * <p/>
	 * TODO: Ensure these circular references get cleaned up properly at dialog disposal so everything can be GC'd.
	 *
	 * @param checkBoxSelectionModel the selection model used to keep track of the check box state
	 */
	public void setCheckBoxSelectionModel(TreeSelectionModel checkBoxSelectionModel) {
		theCheckBoxSelectionModel = checkBoxSelectionModel;
	}
	
	/**
	 * Add a selection path to the internal check box selection model.  The normal JTree selection model is unaffected.
	 * This has the effect of "selecting" the check box, but not highlighting the row.
	 *
	 * @param path the path (row)
	 */
	@Override
	public void addSelectionPath(TreePath path) {
		theCheckBoxSelectionModel.addSelectionPath(path);
	}
	
	/**
	 * Helper to construct a named vector.
	 *
	 * @param name  the name of the vector
	 * @param nodes the array of nodes to put into the vector
	 * @param stage the stage number
	 *
	 * @return a NamedVector suitable for adding to a JTree
	 */
	private static Vector createNamedVector(String name, CheckBoxNode[] nodes, int stage) {
		return new NamedVector(name, nodes, stage);
	}
	
	/**
	 * Helper to construct the set of check box rows for each stage.
	 *
	 * @param onlyStageSpecific  if true then only stage specific OpenRocketPrintable rows are represented (in this part 
	 * of the tree).
	 * 
	 * @return an array of CheckBoxNode
	 */
	private static CheckBoxNode[] createPrintTreeNode(boolean onlyStageSpecific) {
		List<CheckBoxNode> nodes = new ArrayList<CheckBoxNode>();
		OpenRocketPrintable[] printables = OpenRocketPrintable.values();
		for (OpenRocketPrintable openRocketPrintable : printables) {
			if (!onlyStageSpecific || openRocketPrintable.isStageSpecific()) {
				nodes.add(new CheckBoxNode(openRocketPrintable.getDescription(),
											INITIAL_CHECKBOX_SELECTED));
			}
		}
		return nodes.toArray(new CheckBoxNode[nodes.size()]);
	}
	
	/**
	 * Get the set of items to be printed, as selected by the user.
	 * 
	 * @return the things to be printed, returned as an Iterator<PrintableContext>
	 */
	public Iterator<PrintableContext> getToBePrinted() {
		final DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) getModel().getRoot();
		PrintableContext pc = new PrintableContext();
		add(pc, mutableTreeNode);
		return pc.iterator();
	}
	
	/**
	 * Walk a tree, finding everything that has been selected and aggregating it into something that can be iterated upon
	 * This method is recursive.
	 * 
	 * @param pc                  the printable context that aggregates the choices into an iterator
	 * @param theMutableTreeNode  the root node
	 */
	private void add(final PrintableContext pc, final DefaultMutableTreeNode theMutableTreeNode) {
		int children = theMutableTreeNode.getChildCount();
		for (int x = 0; x < children; x++) {
			
			final DefaultMutableTreeNode at = (DefaultMutableTreeNode) theMutableTreeNode.getChildAt(x);
			if (at.getUserObject() instanceof CheckBoxNode) {
				CheckBoxNode cbn = (CheckBoxNode) at.getUserObject();
				if (cbn.isSelected()) {
					final OpenRocketPrintable printable = OpenRocketPrintable.findByDescription(cbn.getText());
					pc.add(
							printable.isStageSpecific() ? ((NamedVector) theMutableTreeNode.getUserObject())
									.getStage() : null,
							printable);
				}
			}
			add(pc, at);
		}
	}
	
}

/**
 * JTree's work off of Vector's (unfortunately).  This class is tailored for use with check boxes in the JTree.
 */
class NamedVector extends Vector<CheckBoxNode> {
	String name;
	
	int stageNumber;
	
	public NamedVector(String theName) {
		name = theName;
	}
	
	public NamedVector(String theName, CheckBoxNode elements[], int stage) {
		this(theName, elements);
		stageNumber = stage;
	}
	
	public NamedVector(String theName, CheckBoxNode elements[]) {
		name = theName;
		for (int i = 0, n = elements.length; i < n; i++) {
			add(elements[i]);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getStage() {
		return stageNumber;
	}
}