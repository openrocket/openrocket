package net.sf.openrocket.gui.print;

import java.awt.Window;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;

public class ConceptPrintDialog extends JDialog {
	
	public ConceptPrintDialog() {
		super((Window) null, "Print");
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		JList list = new JList(new Object[] {
				"Model name",
				"Parts detail",
				"Fin templates",
				"Design report"
		});
		panel.add(new JScrollPane(list), "spanx, growx, wrap");
		
		JCheckBox checkbox = new JCheckBox("Show by stage");
		panel.add(checkbox, "");
		
		JButton button = new JButton("Settings");
		panel.add(button, "right, wrap para");
		
		JLabel label = new JLabel("<html>Printer: LaserJet 6L<br>Paper size: A4 Portrait");
		panel.add(label);
		
		button = new JButton("Change");
		panel.add(button, "right, wrap 20lp");
		
		panel.add(new JButton("Save as PDF"), "split, spanx, right");
		panel.add(new JButton("Preview"), "right");
		panel.add(new JButton("Print"), "right");
		panel.add(new JButton("Close"), "right");
		

		this.add(panel);
		
	}
	
	

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				GUIUtil.setBestLAF();
				JDialog dialog = new ConceptPrintDialog();
				GUIUtil.setDisposableDialogOptions(dialog, null);
				dialog.setSize(450, 350);
				dialog.setVisible(true);
			}
		});
	}
	
}
