package net.sf.openrocket.gui.main.componenttree;

import javax.swing.DropMode;
import javax.swing.ToolTipManager;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.components.BasicTree;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


@SuppressWarnings("serial")
public class ComponentTree extends BasicTree {
	
	public ComponentTree(OpenRocketDocument document) {
		super();
		this.setModel(new ComponentTreeModel(document.getRocket(), this));

		addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) { }

			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_A) && ((e.getModifiersEx() & KeyEvent.META_DOWN_MASK) != 0)) {
					setSelectionInterval(1, getRowCount());		// Don't select the rocket (row 0)
				}
			}

			@Override
			public void keyReleased(KeyEvent e) { }
		});
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
