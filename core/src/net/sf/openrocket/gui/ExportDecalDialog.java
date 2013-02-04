package net.sf.openrocket.gui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

public class ExportDecalDialog extends JDialog {
	
	private final static Translator trans = Application.getTranslator();
	
	private final OpenRocketDocument document;
	
	private JComboBox decalComboBox;
	
	public ExportDecalDialog(Window parent, OpenRocketDocument doc) {
		super(parent, trans.get("ExportDecalDialog.title"), ModalityType.APPLICATION_MODAL);
		
		this.document = doc;
		
		JPanel panel = new JPanel(new MigLayout());
		
		//// decal list
		JLabel label = new JLabel(trans.get("ExportDecalDialog.decalList.lbl"));
		panel.add(label);
		
		Collection<DecalImage> exportableDecals = document.getDecalList();
		
		decalComboBox = new JComboBox(exportableDecals.toArray(new DecalImage[0]));
		decalComboBox.setEditable(false);
		panel.add(decalComboBox, "growx, wrap");
		
		final JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		chooser.setVisible(true);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		
		chooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals(JFileChooser.CANCEL_SELECTION)) {
					ExportDecalDialog.this.dispose();
				} else if (command.equals(JFileChooser.APPROVE_SELECTION)) {
					// Here we copy the bits out.
					
					// FIXME - confirm overwrite?
					DecalImage selectedDecal = (DecalImage) decalComboBox.getSelectedItem();
					File selectedFile = chooser.getSelectedFile();
					
					export(selectedDecal, selectedFile);
					ExportDecalDialog.this.dispose();
				}
			}
		});
		panel.add(chooser, "span, grow");
		
		this.add(panel);
		this.pack();
	}
	
	private void export(DecalImage decal, File selectedFile) {
		
		try {
			decal.exportImage(selectedFile, false);
		} catch (IOException iex) {
			// FIXME - probably want a simple user dialog here since FileIO is not really a bug.
			throw new BugException(iex);
		}
	}
}
