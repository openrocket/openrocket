package net.sf.openrocket.gui.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JTree;

import net.sf.openrocket.rocketcomponent.RocketComponent;


public class ComponentTree extends JTree {
	private static final long serialVersionUID = 1L;

	public ComponentTree(RocketComponent root) {
		super();
		this.setModel(new ComponentTreeModel(root,this));
		this.setToggleClickCount(0);
		
		javax.swing.plaf.basic.BasicTreeUI ui = new javax.swing.plaf.basic.BasicTreeUI();
		this.setUI(ui);

		ui.setExpandedIcon(TreeIcon.MINUS);
		ui.setCollapsedIcon(TreeIcon.PLUS);
		
		ui.setLeftChildIndent(15);
		

		setBackground(Color.WHITE);
		setShowsRootHandles(false);
		
		setCellRenderer(new ComponentTreeRenderer());
		
		// Expand whole tree by default
		expandTree();
	}
	
	
	public void expandTree() {
		for (int i=0; i<getRowCount(); i++)
			expandRow(i);
		
	}
	
	private static class TreeIcon implements Icon{
		public static final Icon PLUS = new TreeIcon(true);
		public static final Icon MINUS = new TreeIcon(false);
		
		// Implementation:
		
	    private final static int width = 9;
	    private final static int height = 9;
	    private final static BasicStroke stroke = new BasicStroke(2);
	    private boolean plus;
	    
	    private TreeIcon(boolean plus) {
	    	this.plus = plus;
	    }
	    
	    public void paintIcon(Component c, Graphics g, int x, int y) {
	        Graphics2D g2 = (Graphics2D)g.create();
	        
	        g2.setColor(Color.WHITE);
	        g2.fillRect(x,y,width,height);
	        
	        g2.setColor(Color.DARK_GRAY);
	        g2.drawRect(x,y,width,height);
	        
	        g2.setStroke(stroke);
	        g2.drawLine(x+3, y+(height+1)/2, x+width-2, y+(height+1)/2);
	        if (plus)
	        	g2.drawLine(x+(width+1)/2, y+3, x+(width+1)/2, y+height-2);
	        
	        g2.dispose();
	    }
	    
	    public int getIconWidth() {
	        return width;
	    }
	    
	    public int getIconHeight() {
	        return height;
	    }
	}
	
}


