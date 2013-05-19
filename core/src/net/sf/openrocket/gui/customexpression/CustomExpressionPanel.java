package net.sf.openrocket.gui.customexpression;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.startup.Application;

public class CustomExpressionPanel extends JPanel {
	
	private static final Logger log = LoggerFactory.getLogger(CustomExpressionPanel.class);
	private static final Translator trans = Application.getTranslator();
	
	private JPanel expressionSelectorPanel;
	private OpenRocketDocument doc;
	
	public CustomExpressionPanel(final OpenRocketDocument doc, final JDialog parentDialog) {
		super(new MigLayout("fill"));
		this.doc = doc;
		
		expressionSelectorPanel = new JPanel(new MigLayout("gapy rel"));
		expressionSelectorPanel.setToolTipText(trans.get("customExpressionPanel.lbl.CalcNote"));
		
		JScrollPane scroll = new JScrollPane(expressionSelectorPanel);
		
		//Border bdr = BorderFactory.createTitledBorder(trans.get("customExpressionPanel.lbl.CustomExpressions"));
		//scroll.setBorder(bdr);
		//expressionSelectorPanel.add(scroll);
		
		//this.add(expressionSelectorPanel, "spany 1, height 10px, wmin 600lp, grow 100, gapright para");
		this.add(scroll, "hmin 200lp, wmin 700lp, grow 100, wrap");
		
		//DescriptionArea desc = new DescriptionArea(trans.get("customExpressionPanel.lbl.UpdateNote")+"\n\n"+trans.get("customExpressionPanel.lbl.CalcNote"), 8, -2f);
		//desc.setViewportBorder(BorderFactory.createEmptyBorder());
		//this.add(desc, "width 1px, growx 1, wrap unrel, wrap");
		
		//// New expression
		JButton button = new JButton(trans.get("customExpressionPanel.but.NewExpression"));
		button.setToolTipText(trans.get("customExpressionPanel.but.ttip.NewExpression"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Open window to configure expression
				log.info("Opening window to configure new expression");
				Window parent = SwingUtilities.getWindowAncestor(CustomExpressionPanel.this);
				new ExpressionBuilderDialog(parent, doc).setVisible(true);
				updateExpressions();
			}
		});
		this.add(button, "split 4, width :100:200");
		
		//// Import
		final JButton importButton = new JButton(trans.get("customExpressionPanel.but.Import"));
		importButton.setToolTipText(trans.get("customExpressionPanel.but.ttip.Import"));
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				//Create a file chooser
				final JFileChooser fc = new JFileChooser();
				if (doc.getFile() != null) {
					fc.setCurrentDirectory(doc.getFile().getParentFile());
				}
				fc.setFileFilter(new FileNameExtensionFilter("Openrocket file", "ork"));
				fc.setAcceptAllFileFilterUsed(false);
				
				int returnVal = fc.showOpenDialog(CustomExpressionPanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File importFile = fc.getSelectedFile();
					log.info("User selected a file to import expressions from " + fc.getSelectedFile().toString());
					
					//TODO: This should probably be somewhere else and ideally we would use an alternative minimal rocket loader. Still, it doesn't seem particularly slow this way.
					
					// Load expressions from selected document
					GeneralRocketLoader loader = new GeneralRocketLoader(importFile);
					try {
						OpenRocketDocument importedDocument = loader.load();
						for (CustomExpression exp : importedDocument.getCustomExpressions()) {
							doc.addCustomExpression(exp);
						}
					} catch (RocketLoadException e1) {
						log.info(Markers.USER_MARKER, "Error opening document to import expressions from.");
					}
					updateExpressions();
				}
			}
		});
		this.add(importButton, "width :100:200");
		
		//// Close button
		final JButton closeButton = new JButton(trans.get("dlg.but.close"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parentDialog.dispose();
			}
		});
		this.add(new JPanel(), "growx");
		this.add(closeButton, "width :100:200");
		
		updateExpressions();
	}
	
	/*
	 * Update the expressionSelectorPanel
	 */
	private void updateExpressions() {
		
		expressionSelectorPanel.removeAll();
		int totalExpressions = doc.getCustomExpressions().size();
		for (int i = 0; i < totalExpressions; i++) {
			SingleExpression se = new SingleExpression(doc.getCustomExpressions().get(i), i != 0, i != totalExpressions - 1);
			expressionSelectorPanel.add(se, "wrap");
		}
		
		expressionSelectorPanel.revalidate();
		expressionSelectorPanel.repaint();
	}
	
	private void deleteExpression(CustomExpression expression) {
		doc.getCustomExpressions().remove(expression);
	}
	
	/**
	 * Moves an expression up or down in the expression list
	 * @param expression
	 * @param move integer - +1 to move down, -1 to move up
	 */
	private void moveExpression(CustomExpression expression, int move) {
		List<CustomExpression> expressions = doc.getCustomExpressions();
		int i = expressions.indexOf(expression);
		if (i + move == expressions.size() || i + move < 0)
			return;
		else
			Collections.swap(expressions, i, i + move);
	}
	
	
	/*
	 * A JPanel which configures a single expression
	 */
	private class SingleExpression extends JPanel {
		
		// Convenience method to make the labels consistent
		private JLabel setLabelStyle(JLabel l) {
			l.setBackground(Color.WHITE);
			l.setOpaque(true);
			l.setBorder(BorderFactory.createRaisedBevelBorder());
			l.setText(" " + l.getText() + " ");
			return l;
		}
		
		private SingleExpression(final CustomExpression expression, boolean showUp, boolean showDown) {
			super(new MigLayout("ins 0"));
			//                      name:    aName    symbol:  a      Unit:  m/s
			//super(new MigLayout("","[::100][:200:400][::100][:100:200][::100][:100:200]",""));
			
			JLabel nameLabel = new JLabel(trans.get("customExpression.Name") + " :");
			JLabel name = new JLabel(expression.getName());
			name = setLabelStyle(name);
			JLabel symbolLabel = new JLabel(trans.get("customExpression.Symbol") + " :");
			JLabel symbol = new JLabel(expression.getSymbol());
			symbol = setLabelStyle(symbol);
			symbol.setBackground(Color.WHITE);
			
			JLabel unitLabel = new JLabel(trans.get("customExpression.Units") + " :");
			UnitSelector unitSelector = new UnitSelector(expression.getType().getUnitGroup());
			//JLabel unitSelector = new JLabel ( expression.getUnit() );
			//unitSelector = setLabelStyle(unitSelector);
			//unitSelector.setBackground(Color.WHITE);
			
			JButton editButton = new JButton(Icons.EDIT);
			editButton.setToolTipText(trans.get("customExpression.Units.but.ttip.Edit"));
			editButton.setBorderPainted(false);
			editButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Window parent = SwingUtilities.getWindowAncestor(CustomExpressionPanel.this);
					new ExpressionBuilderDialog(parent, doc, expression).setVisible(true);
					updateExpressions();
				}
			});
			
			JButton upButton = new JButton(Icons.UP);
			upButton.setToolTipText(trans.get("customExpression.Units.but.ttip.MoveUp"));
			upButton.setBorderPainted(false);
			upButton.setVisible(showUp);
			upButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					moveExpression(expression, -1);
					updateExpressions();
				}
			});
			
			JButton downButton = new JButton(Icons.DOWN);
			downButton.setToolTipText(trans.get("customExpression.Units.but.ttip.MoveDown"));
			downButton.setBorderPainted(false);
			downButton.setVisible(showDown);
			downButton.addActionListener(new ActionListener() {
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
