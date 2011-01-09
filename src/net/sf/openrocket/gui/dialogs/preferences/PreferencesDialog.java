package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.main.SimpleFileFilter;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

public class PreferencesDialog extends JDialog {
	private static final LogHelper log = Application.getLogger();
	
	private final List<DefaultUnitSelector> unitSelectors = new ArrayList<DefaultUnitSelector>();
	
	private File defaultDirectory = null;
	
	private PreferencesDialog() {
		super((Window) null, "Preferences", Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill, gap unrel", "[grow]", "[grow][]"));
		
		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "grow, wrap");
		

		tabbedPane.addTab("Units", null, unitsPane(), "Default units");
		tabbedPane.addTab("Materials", null, new MaterialEditPanel(), "Custom materials");
		tabbedPane.addTab("Options", null, optionsPane(), "Miscellaneous options");
		

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				PreferencesDialog.this.setVisible(false);
				PreferencesDialog.this.dispose();
			}
		});
		panel.add(close, "span, right, tag close");
		
		this.setContentPane(panel);
		pack();
		this.setLocationRelativeTo(null);
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Prefs.storeDefaultUnits();
			}
		});
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
	
	private JPanel optionsPane() {
		JPanel panel = new JPanel(new MigLayout("fillx, ins 30lp n n n"));
		

		panel.add(new JLabel("Position to insert new body components:"), "gapright para");
		panel.add(new JComboBox(new PrefChoiseSelector(Prefs.BODY_COMPONENT_INSERT_POSITION_KEY,
				"Always ask", "Insert in middle", "Add to end")), "wrap para, growx, sg combos");
		
		panel.add(new JLabel("Confirm deletion of simulations:"));
		panel.add(new JComboBox(new PrefBooleanSelector(Prefs.CONFIRM_DELETE_SIMULATION,
				"Delete", "Confirm", true)), "wrap 40lp, growx, sg combos");
		

		panel.add(new JLabel("User-defined thrust curves:"), "spanx, wrap");
		final JTextField field = new JTextField();
		List<File> files = Prefs.getUserThrustCurveFiles();
		String str = "";
		for (File file : files) {
			if (str.length() > 0) {
				str += ";";
			}
			str += file.getAbsolutePath();
		}
		field.setText(str);
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				changed();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				changed();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				changed();
			}
			
			private void changed() {
				String text = field.getText();
				List<File> list = new ArrayList<File>();
				for (String s : text.split(";")) {
					s = s.trim();
					if (s.length() > 0) {
						list.add(new File(s));
					}
				}
				Prefs.setUserThrustCurveFiles(list);
			}
		});
		panel.add(field, "w 100px, gapright unrel, spanx, growx, split");
		
		JButton button = new JButton("Add");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				SimpleFileFilter filter = new SimpleFileFilter("All thrust curve files (*.eng; *.rse; *.zip; directories)",
						true, "eng", "rse", "zip");
				chooser.addChoosableFileFilter(filter);
				chooser.addChoosableFileFilter(new SimpleFileFilter("RASP motor files (*.eng)",
						true, "eng"));
				chooser.addChoosableFileFilter(new SimpleFileFilter("RockSim engine files (*.rse)",
						true, "rse"));
				chooser.addChoosableFileFilter(new SimpleFileFilter("ZIP archives (*.zip)",
						true, "zip"));
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (defaultDirectory != null) {
					chooser.setCurrentDirectory(defaultDirectory);
				}
				
				int returnVal = chooser.showDialog(PreferencesDialog.this, "Add");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					log.user("Adding user thrust curve: " + chooser.getSelectedFile());
					defaultDirectory = chooser.getCurrentDirectory();
					String text = field.getText().trim();
					if (text.length() > 0) {
						text += ";";
					}
					text += chooser.getSelectedFile().getAbsolutePath();
					field.setText(text);
				}
			}
		});
		panel.add(button, "gapright unrel");
		
		button = new JButton("Reset");
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// First one sets to the default, but does not un-set the pref
				field.setText(Prefs.getDefaultUserThrustCurveFile().getAbsolutePath());
				Prefs.setUserThrustCurveFiles(null);
			}
		});
		panel.add(button, "wrap");
		
		DescriptionArea desc = new DescriptionArea("Add directories, RASP motor files (*.eng), " +
				"RockSim engine files (*.rse) or ZIP archives separated by a semicolon (;) to load external " +
				"thrust curves.  Changes will take effect the next time you start OpenRocket.", 3, -3, false);
		desc.setBackground(getBackground());
		panel.add(desc, "spanx, growx, wrap 40lp");
		



		final JCheckBox softwareUpdateBox = new JCheckBox("Check for software updates at startup");
		softwareUpdateBox.setSelected(Prefs.getCheckUpdates());
		softwareUpdateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Prefs.setCheckUpdates(softwareUpdateBox.isSelected());
			}
		});
		panel.add(softwareUpdateBox);
		
		button = new JButton("Check now");
		button.setToolTipText("Check for software updates now");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkForUpdates();
			}
		});
		panel.add(button, "right, wrap");
		

		return panel;
	}
	
	private JPanel unitsPane() {
		JPanel panel = new JPanel(new MigLayout("", "[][]40lp[][]"));
		JComboBox combo;
		
		panel.add(new JLabel("Select your preferred units:"), "span, wrap paragraph");
		

		panel.add(new JLabel("Rocket dimensions:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_LENGTH));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Line density:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_LINE));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Motor dimensions:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_MOTOR_DIMENSIONS));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Surface density:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_SURFACE));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Distance:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DISTANCE));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Bulk density::"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_BULK));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Velocity:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_VELOCITY));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Surface roughness:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ROUGHNESS));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Acceleration:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ACCELERATION));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Area:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_AREA));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Mass:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_MASS));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Angle:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ANGLE));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Force:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_FORCE));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Roll rate:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ROLL));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Total impulse:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_IMPULSE));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Temperature:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_TEMPERATURE));
		panel.add(combo, "sizegroup boxes, wrap");
		


		panel.add(new JLabel("Moment of inertia:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_INERTIA));
		panel.add(combo, "sizegroup boxes");
		
		panel.add(new JLabel("Pressure:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_PRESSURE));
		panel.add(combo, "sizegroup boxes, wrap");
		

		panel.add(new JLabel("Stability:"));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_STABILITY));
		panel.add(combo, "sizegroup boxes, wrap para");
		



		JButton button = new JButton("Default metric");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultMetricUnits();
				for (DefaultUnitSelector s : unitSelectors)
					s.fireChange();
			}
		});
		panel.add(button, "spanx, split 2, grow");
		
		button = new JButton("Default imperial");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultImperialUnits();
				for (DefaultUnitSelector s : unitSelectors)
					s.fireChange();
			}
		});
		panel.add(button, "grow, wrap para");
		

		panel.add(new StyledLabel("The effects will take place the next time you open a window.", -2),
				"spanx, wrap");
		

		return panel;
	}
	
	



	private class DefaultUnitSelector extends AbstractListModel implements ComboBoxModel {
		
		private final UnitGroup group;
		
		public DefaultUnitSelector(UnitGroup group) {
			this.group = group;
			unitSelectors.add(this);
		}
		
		@Override
		public Object getSelectedItem() {
			return group.getDefaultUnit();
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (item == null) {
				// Clear selection - huh?
				return;
			}
			if (!(item instanceof Unit)) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			group.setDefaultUnit(group.getUnitIndex((Unit) item));
		}
		
		@Override
		public Object getElementAt(int index) {
			return group.getUnit(index);
		}
		
		@Override
		public int getSize() {
			return group.getUnitCount();
		}
		
		
		public void fireChange() {
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}
	
	

	private class PrefChoiseSelector extends AbstractListModel implements ComboBoxModel {
		private final String preference;
		private final String[] descriptions;
		
		public PrefChoiseSelector(String preference, String... descriptions) {
			this.preference = preference;
			this.descriptions = descriptions;
		}
		
		@Override
		public Object getSelectedItem() {
			return descriptions[Prefs.getChoise(preference, descriptions.length, 0)];
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (item == null) {
				// Clear selection - huh?
				return;
			}
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			int index;
			for (index = 0; index < descriptions.length; index++) {
				if (((String) item).equalsIgnoreCase(descriptions[index]))
					break;
			}
			if (index >= descriptions.length) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			
			Prefs.putChoise(preference, index);
		}
		
		@Override
		public Object getElementAt(int index) {
			return descriptions[index];
		}
		
		@Override
		public int getSize() {
			return descriptions.length;
		}
	}
	
	
	private class PrefBooleanSelector extends AbstractListModel implements ComboBoxModel {
		private final String preference;
		private final String trueDesc, falseDesc;
		private final boolean def;
		
		public PrefBooleanSelector(String preference, String falseDescription,
				String trueDescription, boolean defaultState) {
			this.preference = preference;
			this.trueDesc = trueDescription;
			this.falseDesc = falseDescription;
			this.def = defaultState;
		}
		
		@Override
		public Object getSelectedItem() {
			if (Prefs.NODE.getBoolean(preference, def)) {
				return trueDesc;
			} else {
				return falseDesc;
			}
		}
		
		@Override
		public void setSelectedItem(Object item) {
			if (item == null) {
				// Clear selection - huh?
				return;
			}
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
			
			if (trueDesc.equals(item)) {
				Prefs.NODE.putBoolean(preference, true);
			} else if (falseDesc.equals(item)) {
				Prefs.NODE.putBoolean(preference, false);
			} else {
				throw new IllegalArgumentException("Illegal argument " + item);
			}
		}
		
		@Override
		public Object getElementAt(int index) {
			switch (index) {
			case 0:
				return def ? trueDesc : falseDesc;
				
			case 1:
				return def ? falseDesc : trueDesc;
				
			default:
				throw new IndexOutOfBoundsException("Boolean asked for index=" + index);
			}
		}
		
		@Override
		public int getSize() {
			return 2;
		}
	}
	
	
	private void checkForUpdates() {
		final UpdateInfoRetriever retriever = new UpdateInfoRetriever();
		retriever.start();
		

		// Progress dialog
		final JDialog dialog = new JDialog(this, ModalityType.APPLICATION_MODAL);
		JPanel panel = new JPanel(new MigLayout());
		
		panel.add(new JLabel("Checking for updates..."), "wrap");
		
		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		panel.add(bar, "growx, wrap para");
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		panel.add(cancel, "right");
		dialog.add(panel);
		
		GUIUtil.setDisposableDialogOptions(dialog, cancel);
		

		// Timer to monitor progress
		final Timer timer = new Timer(100, null);
		final long startTime = System.currentTimeMillis();
		
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!retriever.isRunning() || startTime + 10000 < System.currentTimeMillis()) {
					timer.stop();
					dialog.dispose();
				}
			}
		};
		timer.addActionListener(listener);
		timer.start();
		

		// Wait for action
		dialog.setVisible(true);
		

		// Check result
		UpdateInfo info = retriever.getUpdateInfo();
		if (info == null) {
			JOptionPane.showMessageDialog(this,
					"An error occurred while communicating with the server.",
					"Unable to retrieve update information", JOptionPane.WARNING_MESSAGE, null);
		} else if (info.getLatestVersion() == null ||
				info.getLatestVersion().equals("") ||
				Prefs.getVersion().equalsIgnoreCase(info.getLatestVersion())) {
			JOptionPane.showMessageDialog(this,
					"You are running the latest version of OpenRocket.",
					"No updates available", JOptionPane.INFORMATION_MESSAGE, null);
		} else {
			UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
			infoDialog.setVisible(true);
			if (infoDialog.isReminderSelected()) {
				Prefs.putString(Prefs.LAST_UPDATE, "");
			} else {
				Prefs.putString(Prefs.LAST_UPDATE, info.getLatestVersion());
			}
		}
		
	}
	
	
	////////  Singleton implementation  ////////
	
	private static PreferencesDialog dialog = null;
	
	public static void showPreferences() {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new PreferencesDialog();
		dialog.setVisible(true);
	}
	

}
