package net.sf.openrocket.gui.customexpression;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * Dialog to select from available custom variables
 * @author Richard Graham
 *
 */

public class VariableSelector extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	private static final LogHelper log = Application.getLogger();

	private final Window parentWindow;
	private final Simulation simulation;
	
	public VariableSelector(Window parent, final ExpressionBuilderDialog parentBuilder, final Simulation simulation){
		
		super(parent, trans.get("CustomVariableSelector.title"), JDialog.ModalityType.DOCUMENT_MODAL);
		
		this.parentWindow = parent;
		this.simulation = simulation;
		
		final JButton insertButton = new JButton(trans.get("ExpressionBuilderDialog.InsertVariable"));
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		//// Table of variables and model
		final VariableTableModel tableModel = new VariableTableModel(simulation);
		final JTable table = new JTable(tableModel);
		
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		int width = table.getColumnModel().getTotalColumnWidth();
		table.getColumnModel().getColumn(0).setPreferredWidth( (int) (.7 * width));
		table.getColumnModel().getColumn(1).setPreferredWidth( (int) (.15 * width));
		table.getColumnModel().getColumn(2).setPreferredWidth( (int) (.15 * width));
		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e){
					if (table.getSelectedRowCount() == 1){
						insertButton.setEnabled(true);
					}
					else {
						insertButton.setEnabled(false);
					}
				}
			});
		
		mainPanel.add(scrollPane, "wrap");
		
		//// Cancel button
		final JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				VariableSelector.this.dispose();
			}
		});
		mainPanel.add(cancelButton, "right, width :100:200, split 2");
		
		//// Insert button
		insertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				String str = tableModel.getSymbolAt(row);
				parentBuilder.pasteIntoExpression(str);
				VariableSelector.this.dispose();
			}
		});
		insertButton.setEnabled(false); // disabled by default, only enable when a variable selected
		mainPanel.add(insertButton, "right, width :100:200, wrap");

		this.add(mainPanel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);	
	}
}
