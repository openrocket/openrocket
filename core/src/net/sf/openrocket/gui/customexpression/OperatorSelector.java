package net.sf.openrocket.gui.customexpression;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;

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
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperatorSelector extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(OperatorSelector.class);
	
	private final Window parentWindow;
	
	private final JTable table;
	private final OperatorTableModel tableModel;
	private final ExpressionBuilderDialog parentBuilder;
	
	public OperatorSelector(Window parent, final ExpressionBuilderDialog parentBuilder) {
		
		super(parent, trans.get("CustomOperatorSelector.title"), JDialog.ModalityType.DOCUMENT_MODAL);
		
		this.parentWindow = parent;
		this.parentBuilder = parentBuilder;
		
		final JButton insertButton = new JButton(trans.get("ExpressionBuilderDialog.InsertOperator"));
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		//// Table of variables and model
		tableModel = new OperatorTableModel();
		table = new JTable(tableModel);
		
		table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		int width = table.getColumnModel().getTotalColumnWidth();
		table.getColumnModel().getColumn(0).setPreferredWidth((int) (.1 * width));
		table.getColumnModel().getColumn(1).setPreferredWidth((int) (.9 * width));
		table.setAutoCreateRowSorter(true);
		
		table.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				int col = table.columnAtPoint(p);
				if (col == 1 && row > -1) {
					String description = String.valueOf(table.getValueAt(row, 1));
					description = wrap(description, 60);
					table.setToolTipText(description);
				} else {
					table.setToolTipText(null);
				}
			}
		});
		
		table.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					log.debug("Selected operator by double clicking.");
					selectOperator();
				}
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
		
		InputMap inputMap = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = table.getActionMap();
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		inputMap.put(enter, "select");
		actionMap.put("select", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				log.debug("Selected operator by enter key");
				selectOperator();
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (table.getSelectedRowCount() == 1) {
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
				OperatorSelector.this.dispose();
			}
		});
		mainPanel.add(cancelButton, "right, width :100:200, split 2");
		
		//// Insert button
		insertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectOperator();
			}
		});
		insertButton.setEnabled(false); // disabled by default, only enable when a variable selected
		mainPanel.add(insertButton, "right, width :100:200, wrap");
		
		this.add(mainPanel);
		this.validate();
		this.pack();
		this.setLocationByPlatform(true);
	}
	
	private void selectOperator() {
		int row = table.getSelectedRow();
		String str = table.getValueAt(row, 0).toString();
		parentBuilder.pasteIntoExpression(str);
		OperatorSelector.this.dispose();
	}
	
	
	/*
	 * Returns a word-wrapped version of given input string using HTML syntax, wrapped to len characters.
	 */
	private String wrap(String in, int len) {
		in = in.trim();
		if (in.length() < len)
			return in;
		if (in.substring(0, len).contains("\n"))
			return in.substring(0, in.indexOf("\n")).trim() + "\n\n" + wrap(in.substring(in.indexOf("\n") + 1), len);
		int place = Math.max(Math.max(in.lastIndexOf(" ", len), in.lastIndexOf("\t", len)), in.lastIndexOf("-", len));
		return "<html>" + in.substring(0, place).trim() + "<br>" + wrap(in.substring(place), len);
	}
	
}
