package net.sf.openrocket.gui.customexpression;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.customexpression.ExpressionBuilderDialog;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.simulation.CustomExpression;
import net.sf.openrocket.startup.Application;

public class CustomExpressionPanel extends JPanel {
	
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();
	
	private JPanel expressionSelectorPanel;
	private Simulation simulation;
	
	public CustomExpressionPanel(final Simulation simulation) {
		super(new MigLayout("fill"));
		this.simulation = simulation;

		expressionSelectorPanel = new JPanel(new MigLayout("gapy rel"));
		JScrollPane scroll = new JScrollPane(expressionSelectorPanel);
		this.add(scroll, "spany 2, height 10px, wmin 400lp, grow 100, gapright para");
		
		DescriptionArea desc = new DescriptionArea(trans.get("customExpressionPanel.lbl.UpdateNote")+"\n\n"+trans.get("customExpressionPanel.lbl.CalcNote"), 8, -2f);
		desc.setViewportBorder(BorderFactory.createEmptyBorder());
		this.add(desc, "width 1px, growx 1, wrap unrel");
		
		//// New expression
		JButton button = new JButton(trans.get("customExpressionPanel.but.NewExpression"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open window to configure expression
				log.debug("Opening window to configure new expression");
				Window parent = SwingUtilities.getWindowAncestor(CustomExpressionPanel.this);
				new ExpressionBuilderDialog(parent, simulation).setVisible(true);
				updateExpressions();
			}
		});
				
		this.add(button, "left");
		
		updateExpressions();
	}
	
	/*
	 * Update the expressionSelectorPanel
	 */
	private void updateExpressions(){
		
		expressionSelectorPanel.removeAll();
		for (CustomExpression expression : simulation.getCustomExpressions()){
			SingleExpression se = new SingleExpression(expression);
			expressionSelectorPanel.add(se, "wrap");
		}

		//TODO: High : Find out why repaint method not working properly here.
		//expressionSelectorPanel.repaint();
		expressionSelectorPanel.updateUI(); // Not the correct method to use but works
		
		
	}
	
	private void deleteExpression(CustomExpression expression){
		simulation.getCustomExpressions().remove(expression);
	}
	
	/**
	 * Moves an expression up or down in the expression list
	 * @param expression
	 * @param move integer - +1 to move down, -1 to move up
	 */
	private void moveExpression(CustomExpression expression, int move){
		ArrayList<CustomExpression> expressions = simulation.getCustomExpressions();
		int i = expressions.indexOf(expression);
		if (i+move == expressions.size() || i+move < 0)
			return;
		else
			Collections.swap(expressions, i, i+move);
	}

	
	/*
	 * A JPanel which configures a single expression
	 */
	private class SingleExpression extends JPanel {
		
		// Convenience method to make the labels consistent
		private JLabel setLabelStyle(JLabel l){
			l.setBackground(Color.WHITE);
			l.setOpaque(true);
			l.setBorder(BorderFactory.createRaisedBevelBorder() );
			l.setText(" " + l.getText() + " ");
			return l;
		}
		
		private SingleExpression(final CustomExpression expression) {
			super(new MigLayout("ins 0"));
			//                      name:    aName    symbol:  a      Unit:  m/s
			//super(new MigLayout("","[::100][:200:400][::100][:100:200][::100][:100:200]",""));
			
			JLabel nameLabel = new JLabel( trans.get("customExpression.Name")+ " :");
			JLabel name = new JLabel ( expression.getName() );
			name = setLabelStyle(name);
			JLabel symbolLabel = new JLabel( trans.get("customExpression.Symbol")+ " :" );
			JLabel symbol = new JLabel ( expression.getSymbol());
			symbol = setLabelStyle(symbol);
			symbol.setBackground(Color.WHITE);
			
			JLabel unitLabel = new JLabel( trans.get("customExpression.Units")+ " :");
			UnitSelector unitSelector = new UnitSelector(expression.getType().getUnitGroup());
			
			JButton editButton = new JButton(Icons.EDIT);
			editButton.setToolTipText(trans.get("customExpression.Units.but.ttip.Edit"));
			editButton.setBorderPainted(false);
			editButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e){
					Window parent = SwingUtilities.getWindowAncestor(CustomExpressionPanel.this);
					expression.editExpression(parent);
					updateExpressions();
				}
			});
			
			JButton upButton = new JButton(Icons.UP);
			upButton.setToolTipText(trans.get("customExpression.Units.but.ttip.MoveUp"));
			upButton.setBorderPainted(false);
			upButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveExpression(expression, -1);
					updateExpressions();
				}
			});
			
			JButton downButton = new JButton(Icons.DOWN);
			downButton.setToolTipText(trans.get("customExpression.Units.but.ttip.MoveDown"));
			downButton.setBorderPainted(false);
			downButton.addActionListener( new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveExpression(expression, 1);
					updateExpressions();
				}
			});
			
			
			JButton deleteButton = new JButton(Icons.DELETE);
			//// Remove this expression
			deleteButton.setToolTipText(trans.get("customExpression.Units.but.ttip.Remove"));
			deleteButton.setBorderPainted(false);
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteExpression(expression);
					updateExpressions();
				}
			});
			
			this.add(nameLabel);
			this.add(name, "width 200:200:400, growx");
			this.add(new JPanel());
			this.add(symbolLabel);
			this.add(symbol, "width :50:200");
			this.add(new JPanel());
			this.add(unitLabel);
			this.add(unitSelector, "width :50:100");
			this.add(new JPanel(), "growx");
			this.add(upButton, "right");
			this.add(downButton, "right");
			this.add(editButton, "right");
			this.add(deleteButton, "right");
		}
	}
}
