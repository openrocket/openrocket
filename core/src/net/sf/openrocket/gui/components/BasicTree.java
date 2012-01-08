package net.sf.openrocket.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class BasicTree extends JTree {
	

	public BasicTree() {
		super();
		setDefaultOptions();
	}
	
	public BasicTree(TreeNode node) {
		super(node);
		setDefaultOptions();
	}
	
	
	private void setDefaultOptions() {
		this.setToggleClickCount(0);
		
		javax.swing.plaf.basic.BasicTreeUI plainUI = new javax.swing.plaf.basic.BasicTreeUI();
		this.setUI(plainUI);
		plainUI.setExpandedIcon(TreeIcon.MINUS);
		plainUI.setCollapsedIcon(TreeIcon.PLUS);
		plainUI.setLeftChildIndent(15);
		

		this.setBackground(Color.WHITE);
		this.setShowsRootHandles(false);
	}
	
	
	/**
	 * Expand the entire tree structure.  All nodes will be visible after the call.
	 */
	public void expandTree() {
		for (int i = 0; i < getRowCount(); i++)
			expandRow(i);
	}
	
	


	@Override
	public void treeDidChange() {
		super.treeDidChange();
		/*
		 * Expand the childless nodes to prevent leaf nodes from looking expandable.
		 */
		expandChildlessNodes();
	}
	
	/**
	 * Expand all nodes in the tree that are visible and have no children.  This can be used
	 * to avoid the situation where a non-leaf node is marked as being expandable, but when
	 * expanding it it has no children.
	 */
	private void expandChildlessNodes() {
		TreeModel model = this.getModel();
		if (model == null) {
			return;
		}
		Object root = model.getRoot();
		expandChildlessNodes(model, new TreePath(root));
	}
	
	private void expandChildlessNodes(TreeModel model, TreePath path) {
		Object object = path.getLastPathComponent();
		if (this.isVisible(path)) {
			int count = model.getChildCount(object);
			if (count == 0) {
				this.expandPath(path);
			}
			for (int i = 0; i < count; i++) {
				expandChildlessNodes(model, path.pathByAddingChild(model.getChild(object, i)));
			}
		}
	}
	
	

	/**
	 * Plain-looking tree expand/collapse icons.
	 */
	private static class TreeIcon implements Icon {
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
		
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			
			// Background
			g2.setColor(Color.WHITE);
			g2.fillRect(x, y, width, height);
			
			// Border
			g2.setColor(Color.DARK_GRAY);
			g2.drawRect(x, y, width, height);
			
			// Horizontal stroke
			g2.setStroke(stroke);
			g2.drawLine(x + 3, y + (height + 1) / 2, x + width - 2, y + (height + 1) / 2);
			
			// Vertical stroke
			if (plus) {
				g2.drawLine(x + (width + 1) / 2, y + 3, x + (width + 1) / 2, y + height - 2);
			}
			
			g2.dispose();
		}
		
		@Override
		public int getIconWidth() {
			return width;
		}
		
		@Override
		public int getIconHeight() {
			return height;
		}
	}
}
