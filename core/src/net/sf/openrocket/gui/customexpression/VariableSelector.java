package net.sf.openrocket.gui.customexpression;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dialog to select from available custom variables
 * @author Richard Graham
 *
 */

public class VariableSelector extends JDialog {

	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(VariableSelector.class);
	
	private final JTable table;
	private final VariableTableModel tableModel;
	private final ExpressionBuilderDialog parentBuilder;

	public VariableSelector(Window parent, final ExpressionBuilderDialog parentBuilder, final OpenRocketDocument doc){

		super(parent, trans.get("CustomVariableSelector.title"), JDialog.ModalityType.DOCUMENT_MODAL);

		this.parentBuilder = parentBuilder;
		final JButton insertButton = new JButton(trans.get("ExpressionBuilderDialog.InsertVariable"));

		JPanel mainPanel = new JPanel(new MigLayout());

		//// Table of variables and model
		tableModel = new VariableTableModel(doc);
		table = new JTable(tableModel);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		int width = table.getColumnModel().getTotalColumnWidth();
		table.getColumnModel().getColumn(0).setPreferredWidth( (int) (.7 * width));
		table.getColumnModel().getColumn(1).setPreferredWidth( (int) (.15 * width));
		table.getColumnModel().getColumn(2).setPreferredWidth( (int) (.15 * width));
		table.setAutoCreateRowSorter(true);

		table.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e){
				if (e.getClickCount() == 2){
					log.debug("Selected variable by double clicking.");
					selectVariable();
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
		} );
		
		InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = table.getActionMap();
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		inputMap.put(enter, "select");
		actionMap.put("select", new AbstractAction(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				log.debug("Selected variable by enter key");
				selectVariable();
			}
		});
		

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

		mainPanel.add(scrollPane, "wrap, push, grow");

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
				selectVariable();
			}
		});
		insertButton.setEnabled(false); // disabled by default, only enable when a variable selected
		mainPanel.add(insertButton, "right, width :100:200, wrap");

		this.add(mainPanel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);	
	}
	
	private void selectVariable(){
		int row = table.getSelectedRow();
		String str = table.getValueAt(row, 1).toString();
		parentBuilder.pasteIntoExpression(str);
		VariableSelector.this.dispose();
	}
	
}
