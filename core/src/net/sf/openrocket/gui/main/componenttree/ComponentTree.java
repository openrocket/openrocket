package net.sf.openrocket.gui.main.componenttree;

import javax.swing.DropMode;
import javax.swing.ToolTipManager;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.components.BasicTree;


public class ComponentTree extends BasicTree {
	
	public ComponentTree(OpenRocketDocument document) {
		super();
		this.setModel(new ComponentTreeModel(document.getRocket(), this));
		
		this.setCellRenderer(new ComponentTreeRenderer());
		
		this.setDragEnabled(true);
		this.setDropMode(DropMode.INSERT);
		this.setTransferHandler(new ComponentTreeTransferHandler(document));
		
		// Expand whole tree by default
		expandTree();
		
		// Enable tooltips for this component
		ToolTipManager.sharedInstance().registerComponent(this);
		
	}
	


}
