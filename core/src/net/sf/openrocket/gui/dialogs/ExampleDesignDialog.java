package net.sf.openrocket.gui.dialogs;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.main.ExampleDesignFile;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class ExampleDesignDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private boolean open = false;
	private final JList designSelection;
	
	private ExampleDesignDialog(ExampleDesignFile[] designs, Window parent) {
		//// Open example design
		super(parent, trans.get("exdesigndlg.lbl.Openexampledesign"), Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		//// Select example designs to open:
		panel.add(new JLabel(trans.get("exdesigndlg.lbl.Selectexample")), "wrap");
		
		designSelection = new JList(designs);
		designSelection.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		designSelection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					open = true;
					ExampleDesignDialog.this.setVisible(false);
				}
			}
		});
		panel.add(new JScrollPane(designSelection), "grow, wmin 300lp, wrap para");
		
		//// Open button
		JButton openButton = new JButton(trans.get("exdesigndlg.but.open"));
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open = true;
				ExampleDesignDialog.this.setVisible(false);
			}
		});
		panel.add(openButton, "split 2, sizegroup buttons, growx");
		
		//// Cancel button
		JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				open = false;
				ExampleDesignDialog.this.setVisible(false);
			}
		});
		panel.add(cancelButton, "sizegroup buttons, growx");
		
		this.add(panel);
		this.pack();
		this.setLocationByPlatform(true);
		
		GUIUtil.setDisposableDialogOptions(this, openButton);
	}
	
	
	/**
	 * Open a dialog to allow opening the example designs.
	 * 
	 * @param parent	the parent window of the dialog.
	 * @return			an array of URL's to open, or <code>null</code> if the operation
	 * 					was cancelled.
	 */
	public static URL[] selectExampleDesigns(Window parent) {
		
		ExampleDesignFile[] designs = ExampleDesignFile.getExampleDesigns();
		
		ExampleDesignDialog dialog = new ExampleDesignDialog(designs, parent);
		dialog.setVisible(true);
		
		if (!dialog.open) {
			return null;
		}
		
		Object[] selected = dialog.designSelection.getSelectedValues();
		URL[] urls = new URL[selected.length];
		int i = 0;
		for (Object obj : selected) {
			ExampleDesignFile file = (ExampleDesignFile) obj;
			urls[i++] = file.getURL();
		}
		return urls;
	}
	
}
