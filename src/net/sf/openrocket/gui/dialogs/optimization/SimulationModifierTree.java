package net.sf.openrocket.gui.dialogs.optimization;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.openrocket.gui.components.BasicTree;
import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A tree that displays the simulation modifiers in a tree structure.
 * <p>
 * All nodes in the model are instances of DefaultMutableTreeNode.  The user objects
 * within are either of type RocketComponent, String or SimulationModifier.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationModifierTree extends BasicTree {
	
	/**
	 * Sole constructor.
	 * 
	 * @param rocket				the rocket.
	 * @param simulationModifiers	the simulation modifiers, ordered and mapped by components
	 */
	public SimulationModifierTree(Rocket rocket, Map<Object, List<SimulationModifier>> simulationModifiers) {
		super(createModifierTree(rocket, simulationModifiers));
		
		this.setCellRenderer(new ComponentModifierTreeRenderer());
		expandComponents();
	}
	
	

	private static DefaultMutableTreeNode createModifierTree(Rocket rocket,
			Map<Object, List<SimulationModifier>> simulationModifiers) {
		
		DefaultMutableTreeNode baseNode = new DefaultMutableTreeNode(rocket);
		populateTree(baseNode, rocket, simulationModifiers);
		
		return baseNode;
	}
	
	
	private static void populateTree(DefaultMutableTreeNode node, RocketComponent component,
			Map<Object, List<SimulationModifier>> simulationModifiers) {
		
		// Add modifiers (if any)
		List<SimulationModifier> modifiers = simulationModifiers.get(component);
		if (modifiers != null) {
			DefaultMutableTreeNode modifierNode;
			
			if (component.getChildCount() > 0) {
				modifierNode = new DefaultMutableTreeNode("Optimization parameters");
				node.add(modifierNode);
			} else {
				modifierNode = node;
			}
			
			for (SimulationModifier m : modifiers) {
				modifierNode.add(new DefaultMutableTreeNode(m));
			}
		}
		
		// Add child components
		for (RocketComponent c : component.getChildren()) {
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(c);
			node.add(newNode);
			populateTree(newNode, c, simulationModifiers);
		}
		
	}
	
	
	@SuppressWarnings("rawtypes")
	private void expandComponents() {
		DefaultMutableTreeNode baseNode = (DefaultMutableTreeNode) this.getModel().getRoot();
		
		Enumeration enumeration = baseNode.breadthFirstEnumeration();
		
		while (enumeration.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
			Object object = node.getUserObject();
			if (object instanceof RocketComponent) {
				this.makeVisible(new TreePath(node.getPath()));
			}
		}
	}
	
	


	public class ComponentModifierTreeRenderer extends DefaultTreeCellRenderer {
		private Font componentFont;
		private Font stringFont;
		private Font modifierFont;
		
		@Override
		public Component getTreeCellRendererComponent(
				JTree tree,
				Object value,
				boolean sel,
				boolean expanded,
				boolean leaf,
				int row,
				boolean hasFocus2) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus2);
			
			if (componentFont == null) {
				makeFonts();
			}
			

			// Customize based on line type
			
			Object object = ((DefaultMutableTreeNode) value).getUserObject();
			
			// Set icon (for rocket components, null for others)
			setIcon(ComponentIcons.getSmallIcon(object.getClass()));
			

			// Set text color/style
			if (object instanceof RocketComponent) {
				setForeground(Color.GRAY);
				setFont(componentFont);
			} else if (object instanceof String) {
				setForeground(Color.GRAY);
				setFont(stringFont);
			} else if (object instanceof SimulationModifier) {
				setForeground(Color.BLACK);
				setFont(modifierFont);
				setText(((SimulationModifier) object).getName());
			}
			
			return this;
		}
		
		private void makeFonts() {
			Font font = getFont();
			componentFont = font.deriveFont(Font.ITALIC);
			stringFont = font;
			modifierFont = font.deriveFont(Font.BOLD);
		}
		
	}
	
}
