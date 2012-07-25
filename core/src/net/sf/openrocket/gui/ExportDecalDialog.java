package net.sf.openrocket.gui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.FileUtils;

public class ExportDecalDialog extends JDialog {

	private final OpenRocketDocument document;

	private JComboBox decalComboBox;


	public ExportDecalDialog(Window parent,OpenRocketDocument doc) {
		// FIXME i18n
		// FIXME add buttons.
		super(parent, "title", ModalityType.APPLICATION_MODAL);

		this.document = doc;

		JPanel panel = new JPanel(new MigLayout());

		//// decal list
		JLabel label = new JLabel("Decal");
		panel.add(label);

		Set<String> allDecals = document.getDecalList();
		List<String> exportableDecals = new ArrayList<String>();
		for ( String decal : allDecals ) {
			if ( document.getDecalRegistry().isExportable(decal) ) {
				exportableDecals.add(decal);
			}
		}

		decalComboBox = new JComboBox( exportableDecals.toArray( new String[0] ) );
		decalComboBox.setEditable(false);
		panel.add(decalComboBox, "growx, wrap");

		final JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		chooser.setVisible(true);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);

		chooser.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = (JFileChooser) e.getSource();
				String command = e.getActionCommand();
				if ( command.equals(JFileChooser.CANCEL_SELECTION) ) {
					ExportDecalDialog.this.dispose();
				} else if ( command.equals(JFileChooser.APPROVE_SELECTION)) {
					// Here we copy the bits out.

					String selectedDecal = (String) decalComboBox.getSelectedItem();
					File selectedFile = chooser.getSelectedFile();

					export(selectedDecal,selectedFile);
					ExportDecalDialog.this.dispose();
				}
			}
		});
		panel.add(chooser, "span, grow");

		this.add(panel);
		this.pack();
	}

	private void export( String decalName, File selectedFile ) {

		try {
			document.getDecalRegistry().exportDecal(decalName,selectedFile);
		}
		catch (IOException iex) {
			throw new BugException(iex);
		}
	}

}
