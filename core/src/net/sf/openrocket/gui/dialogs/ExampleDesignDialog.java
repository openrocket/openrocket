package net.sf.openrocket.gui.dialogs;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.JarUtil;

public class ExampleDesignDialog extends JDialog {
	
	private static final String DIRECTORY = "datafiles/examples/";
	private static final String PATTERN = ".*\\.[oO][rR][kK]$";
	private static final Translator trans = Application.getTranslator();

	private static final FilenameFilter FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.matches(PATTERN);
		}
	};
	

	private boolean open = false;
	private final JList designSelection;
	
	private ExampleDesignDialog(ExampleDesign[] designs, Window parent) {
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
		
		ExampleDesign[] designs;
		
		designs = getJarFileNames();
		if (designs == null || designs.length == 0) {
			designs = getDirFileNames();
		}
		if (designs == null || designs.length == 0) {
			//// Example designs could not be found.
			JOptionPane.showMessageDialog(parent, trans.get("exdesigndlg.lbl.Exampledesignsnotfound"),
					//// Examples not found
					trans.get("exdesigndlg.lbl.Examplesnotfound"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		Arrays.sort(designs);
		
		ExampleDesignDialog dialog = new ExampleDesignDialog(designs, parent);
		dialog.setVisible(true);
		
		if (!dialog.open) {
			return null;
		}
		
		Object[] selected = dialog.designSelection.getSelectedValues();
		URL[] urls = new URL[selected.length];
		for (int i=0; i<selected.length; i++) {
			urls[i] = ((ExampleDesign)selected[i]).getURL();
		}
		return urls;
	}
	
	
	
	
	
	private static ExampleDesign[] getDirFileNames() {
		
		// Try to find directory as a system resource
		File dir;
		URL url = ClassLoader.getSystemResource(DIRECTORY);
		
		try {
			dir = JarUtil.urlToFile(url);
		} catch (Exception e1) {
			dir = new File(DIRECTORY);
		}

		// Get the list of files
		File[] files = dir.listFiles(FILTER);
		if (files == null)
			return null;
		
		ExampleDesign[] designs = new ExampleDesign[files.length];
		
		for (int i=0; i<files.length; i++) {
			String name = files[i].getName();
			try {
				designs[i] = new ExampleDesign(files[i].toURI().toURL(), 
						name.substring(0, name.length()-4));
			} catch (MalformedURLException e) {
				throw new BugException(e);
			}
		}
		return designs;
	}

	
	
	private static ExampleDesign[] getJarFileNames() {
		
		ArrayList<ExampleDesign> list = new ArrayList<ExampleDesign>();
		int dirLength = DIRECTORY.length();

		// Find and open the jar file this class is contained in
		File file = JarUtil.getCurrentJarFile();
		if (file == null)
			return null;
		

		// Generate URL pointing to JAR file
		URL fileUrl;
		try {
			fileUrl = file.toURI().toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			throw new BugException(e1);
		}
		
		// Iterate over JAR entries searching for designs
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);

			// Loop through JAR entries searching for files to load
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(DIRECTORY) && FILTER.accept(null, name)) {
					String urlName = "jar:" + fileUrl + "!/" + name;
					URL url = new URL(urlName);
					list.add(new ExampleDesign(url, 
							name.substring(dirLength, name.length()-4)));
				}
			}

		} catch (IOException e) {
			// Could be normal condition if not package in JAR
			return null;
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return list.toArray(new ExampleDesign[0]);
	}
	
	
	
	/**
	 * Data holder class.
	 */
	private static class ExampleDesign implements Comparable<ExampleDesign> {
		
		private final URL url;
		private final String name;
		
		public ExampleDesign(URL url, String name) {
			this.url = url;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public URL getURL() {
			return url;
		}

		@Override
		public int compareTo(ExampleDesign o) {
			return this.name.compareTo(o.name);
		}
	}
	
}
