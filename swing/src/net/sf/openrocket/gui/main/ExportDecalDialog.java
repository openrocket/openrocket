package net.sf.openrocket.gui.main;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class ExportDecalDialog extends JDialog {
	
	private final static Translator trans = Application.getTranslator();
	
	private final OpenRocketDocument document;
	
	private final JComboBox decalComboBox;
	private final JFileChooser chooser;
	
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
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		chooser.setVisible(true);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		
		// Add an item change listener to the decal chooser to detect when the selected image has change
		decalComboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateChoosenDecal();
				}
			}
			
		});
		
		// Call the update to synchronize the boxes on startup.
		updateChoosenDecal();
		
		chooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String command = e.getActionCommand();
				if (command.equals(JFileChooser.CANCEL_SELECTION)) {
					ExportDecalDialog.this.dispose();
				} else if (command.equals(JFileChooser.APPROVE_SELECTION)) {
					// Here we copy the bits out.
					
					DecalImage selectedDecal = (DecalImage) decalComboBox.getSelectedItem();
					File selectedFile = chooser.getSelectedFile();
					
					if (FileHelper.confirmWrite(selectedFile, ExportDecalDialog.this)) {
						export(selectedDecal, selectedFile);
						// If the user doesn't confirm over write, then leave this dialog open.
						ExportDecalDialog.this.dispose();
					}
				}
			}
		});
		panel.add(chooser, "span, grow");
		
		this.add(panel);
		this.pack();
	}
	
	private void export(DecalImage decal, File selectedFile) {
		
		try {
			decal.exportImage(selectedFile);
		} catch (IOException iex) {
			String message = MessageFormat.format(trans.get("ExportDecalDialog.exception"), selectedFile.getAbsoluteFile());
			JOptionPane.showMessageDialog(this, message, "", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void updateChoosenDecal() {
		DecalImage image = (DecalImage) decalComboBox.getSelectedItem();
		if (image == null) {
			return;
		}
		File fullName = new File(image.getName());
		chooser.setSelectedFile(new File(fullName.getName()));
	}
}
