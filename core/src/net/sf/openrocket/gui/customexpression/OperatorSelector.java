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
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

public class OperatorSelector extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	private static final LogHelper log = Application.getLogger();

	private final Window parentWindow;
	
	public OperatorSelector(Window parent, final ExpressionBuilderDialog parentBuilder){
		
		super(parent, trans.get("CustomOperatorSelector.title"), JDialog.ModalityType.DOCUMENT_MODAL);
		
		this.parentWindow = parent;
		
		final JButton insertButton = new JButton(trans.get("ExpressionBuilderDialog.InsertOperator"));
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		//// Table of variables and model
		final OperatorTableModel tableModel = new OperatorTableModel();
		final JTable table = new JTable(tableModel);
		
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		int width = table.getColumnModel().getTotalColumnWidth();
		table.getColumnModel().getColumn(0).setPreferredWidth( (int) (.2 * width));
		table.getColumnModel().getColumn(1).setPreferredWidth( (int) (.8 * width));
		
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
				OperatorSelector.this.dispose();
			}
		});
		mainPanel.add(cancelButton, "right, width :100:200, split 2");
		
		//// Insert button
		insertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int row = table.getSelectedRow();
				String str = tableModel.getOperatorAt(row);
				parentBuilder.pasteIntoExpression(str);
				OperatorSelector.this.dispose();
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
