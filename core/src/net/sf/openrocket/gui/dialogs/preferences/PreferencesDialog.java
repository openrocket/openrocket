package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.communication.UpdateInfo;
import net.sf.openrocket.communication.UpdateInfoRetriever;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.dialogs.UpdateInfoDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.Named;
import net.sf.openrocket.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.Font;


public class PreferencesDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(PreferencesDialog.class);
	
	private final List<DefaultUnitSelector> unitSelectors = new ArrayList<DefaultUnitSelector>();
	
	private File defaultDirectory = null;
	private static final Translator trans = Application.getTranslator();
	
	private final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
	
	private PreferencesDialog(Window parent) {
		//// Preferences
		super(parent, trans.get("pref.dlg.title.Preferences"), Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill, gap unrel", "[grow]", "[grow][]"));
		
		JTabbedPane tabbedPane = new JTabbedPane();
		panel.add(tabbedPane, "grow, wrap");
		
		//// Units and Default units
		tabbedPane.addTab(trans.get("pref.dlg.tab.Units"), null, unitsPane(),
				trans.get("pref.dlg.tab.Defaultunits"));
		//// Materials and Custom materials
		tabbedPane.addTab(trans.get("pref.dlg.tab.Materials"), null, new MaterialEditPanel(),
				trans.get("pref.dlg.tab.Custommaterials"));
		//// Options and Miscellaneous options
		tabbedPane.addTab(trans.get("pref.dlg.tab.Options"), null, optionsPane(),
				trans.get("pref.dlg.tab.Miscellaneousoptions"));
		//// Decal Editor selection
		tabbedPane.addTab(trans.get("pref.dlg.tab.Graphics"), graphicsOptionsPane());
		
		//// Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
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
				preferences.storeDefaultUnits();
			}
		});
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
	
	private JPanel optionsPane() {
		JPanel panel = new JPanel(new MigLayout("fillx, ins 30lp n n n"));
		
		
		//// Language selector
		Locale userLocale = null;
		{
			String locale = preferences.getString("locale", null);
			userLocale = L10N.toLocale(locale);
		}
		List<Named<Locale>> locales = new ArrayList<Named<Locale>>();
		for (Locale l : SwingPreferences.getSupportedLocales()) {
			locales.add(new Named<Locale>(l, l.getDisplayLanguage(l) + "/" + l.getDisplayLanguage()));
		}
		Collections.sort(locales);
		locales.add(0, new Named<Locale>(null, trans.get("languages.default")));
		
		final JComboBox languageCombo = new JComboBox(locales.toArray());
		for (int i = 0; i < locales.size(); i++) {
			if (Utils.equals(userLocale, locales.get(i).get())) {
				languageCombo.setSelectedIndex(i);
			}
		}
		languageCombo.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				Named<Locale> selection = (Named<Locale>) languageCombo.getSelectedItem();
				Locale l = selection.get();
				preferences.putString(Preferences.USER_LOCAL, l == null ? null : l.toString());
			}
		});
		panel.add(new JLabel(trans.get("lbl.language")), "gapright para");
		panel.add(languageCombo, "wrap rel, growx, sg combos");
		
		panel.add(new StyledLabel(trans.get("PreferencesDialog.lbl.languageEffect"), -3, Style.ITALIC), "span, wrap para*2");
		
		
		//// Position to insert new body components:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Positiontoinsert")), "gapright para");
		panel.add(new JComboBox(new PrefChoiseSelector(Preferences.BODY_COMPONENT_INSERT_POSITION_KEY,
				//// Always ask
				//// Insert in middle
				//// Add to end
				trans.get("pref.dlg.PrefChoiseSelector1"),
				trans.get("pref.dlg.PrefChoiseSelector2"),
				trans.get("pref.dlg.PrefChoiseSelector3"))), "wrap para, growx, sg combos");
		
		//// Confirm deletion of simulations:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Confirmdeletion")));
		panel.add(new JComboBox(new PrefBooleanSelector(Preferences.CONFIRM_DELETE_SIMULATION,
				//// Delete
				//// Confirm
				trans.get("pref.dlg.PrefBooleanSelector1"),
				trans.get("pref.dlg.PrefBooleanSelector2"), true)), "wrap 40lp, growx, sg combos");
		
		//// User-defined thrust curves:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.User-definedthrust")), "spanx, wrap");
		final JTextField field = new JTextField();
		List<File> files = preferences.getUserThrustCurveFiles();
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
				preferences.setUserThrustCurveFiles(list);
			}
		});
		panel.add(field, "w 100px, gapright unrel, spanx, growx, split");
		
		//// Add button
		JButton button = new JButton(trans.get("pref.dlg.but.add"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				SimpleFileFilter filter =
						new SimpleFileFilter(
								//// All thrust curve files (*.eng; *.rse; *.zip; directories)
								trans.get("pref.dlg.Allthrustcurvefiles"),
								true, "eng", "rse", "zip");
				chooser.addChoosableFileFilter(filter);
				//// RASP motor files (*.eng)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.RASPfiles"),
						true, "eng"));
				//// RockSim engine files (*.rse)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.RockSimfiles"),
						true, "rse"));
				//// ZIP archives (*.zip)
				chooser.addChoosableFileFilter(new SimpleFileFilter(trans.get("pref.dlg.ZIParchives"),
						true, "zip"));
				chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (defaultDirectory != null) {
					chooser.setCurrentDirectory(defaultDirectory);
				}
				
				//// Add
				int returnVal = chooser.showDialog(PreferencesDialog.this, trans.get("pref.dlg.Add"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					log.info(Markers.USER_MARKER, "Adding user thrust curve: " + chooser.getSelectedFile());
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
		
		//// Reset button
		button = new JButton(trans.get("pref.dlg.but.reset"));
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// First one sets to the default, but does not un-set the pref
				field.setText(preferences.getDefaultUserThrustCurveFile().getAbsolutePath());
				preferences.setUserThrustCurveFiles(null);
			}
		});
		panel.add(button, "wrap");
		
		//// Add directories, RASP motor files (*.eng), RockSim engine files (*.rse) or ZIP archives separated by a semicolon (;) to load external thrust curves.  Changes will take effect the next time you start OpenRocket.
		DescriptionArea desc = new DescriptionArea(trans.get("pref.dlg.DescriptionArea.Adddirectories"), 3, -3, false);
		desc.setBackground(getBackground());
		panel.add(desc, "spanx, growx, wrap 40lp");
		
		
		
		//// Check for software updates at startup
		final JCheckBox softwareUpdateBox =
				new JCheckBox(trans.get("pref.dlg.checkbox.Checkupdates"));
		softwareUpdateBox.setSelected(preferences.getCheckUpdates());
		softwareUpdateBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setCheckUpdates(softwareUpdateBox.isSelected());
			}
		});
		panel.add(softwareUpdateBox);
		
		//// Check now button
		button = new JButton(trans.get("pref.dlg.but.checknow"));
		//// Check for software updates now
		button.setToolTipText(trans.get("pref.dlg.ttip.Checkupdatesnow"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				checkForUpdates();
			}
		});
		panel.add(button, "right, wrap");
		
		final JCheckBox autoOpenDesignFile = new JCheckBox(trans.get("pref.dlg.but.openlast"));
		autoOpenDesignFile.setSelected(preferences.isAutoOpenLastDesignOnStartupEnabled());
		autoOpenDesignFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoOpenLastDesignOnStartup(autoOpenDesignFile.isSelected());
			}
		});
		panel.add(autoOpenDesignFile);
		
		return panel;
	}
	
	private JPanel unitsPane() {
		JPanel panel = new JPanel(new MigLayout("", "[][]40lp[][]"));
		JComboBox combo;
		
		//// Select your preferred units:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Selectprefunits")), "span, wrap paragraph");
		
		
		//// Rocket dimensions:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Rocketdimensions")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_LENGTH));
		panel.add(combo, "sizegroup boxes");
		
		//// Line density:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Linedensity")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_LINE));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Motor dimensions:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Motordimensions")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_MOTOR_DIMENSIONS));
		panel.add(combo, "sizegroup boxes");
		
		//// Surface density:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Surfacedensity")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_SURFACE));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Distance:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Distance")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DISTANCE));
		panel.add(combo, "sizegroup boxes");
		
		//// Bulk density::
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Bulkdensity")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_DENSITY_BULK));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Velocity:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Velocity")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_VELOCITY));
		panel.add(combo, "sizegroup boxes");
		
		//// Surface roughness:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Surfaceroughness")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ROUGHNESS));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Acceleration:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Acceleration")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ACCELERATION));
		panel.add(combo, "sizegroup boxes");
		
		//// Area:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Area")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_AREA));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Mass:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Mass")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_MASS));
		panel.add(combo, "sizegroup boxes");
		
		//// Angle:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Angle")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ANGLE));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Force:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Force")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_FORCE));
		panel.add(combo, "sizegroup boxes");
		
		//// Roll rate:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Rollrate")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_ROLL));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Total impulse:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Totalimpulse")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_IMPULSE));
		panel.add(combo, "sizegroup boxes");
		
		//// Temperature:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Temperature")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_TEMPERATURE));
		panel.add(combo, "sizegroup boxes, wrap");
		
		//// Moment of inertia:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Momentofinertia")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_INERTIA));
		panel.add(combo, "sizegroup boxes");
		
		//// Pressure:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Pressure")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_PRESSURE));
		panel.add(combo, "sizegroup boxes, wrap");
		
		
		//// Stability:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Stability")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_STABILITY));
		panel.add(combo, "sizegroup boxes");
		
		//// Windspeed:
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Windspeed")));
		combo = new JComboBox(new DefaultUnitSelector(UnitGroup.UNITS_WINDSPEED));
		panel.add(combo, "sizegroup boxes, wrap para");
		
		
		
		
		//// Default metric button
		JButton button = new JButton(trans.get("pref.dlg.but.defaultmetric"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultMetricUnits();
				for (DefaultUnitSelector s : unitSelectors)
					s.fireChange();
			}
		});
		panel.add(button, "spanx, split 2, grow");
		
		//// Default imperial button
		button = new JButton(trans.get("pref.dlg.but.defaultimperial"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UnitGroup.setDefaultImperialUnits();
				for (DefaultUnitSelector s : unitSelectors)
					s.fireChange();
			}
		});
		panel.add(button, "grow, wrap para");
		
		//// The effects will take place the next time you open a window.
		panel.add(new StyledLabel(
				trans.get("pref.dlg.lbl.effect1"), -2, Style.ITALIC),
				"spanx, wrap");
		
		
		return panel;
	}
	
	
	private JPanel graphicsOptionsPane() {
		
		JPanel panel = new JPanel(new MigLayout("fillx"));
		
		panel.add(new JPanel(new MigLayout("fill, ins n n n")) {
			{ //Editor Options		
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.lbl.DecalEditor"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				ButtonGroup execGroup = new ButtonGroup();
				
				JRadioButton showPrompt = new JRadioButton(trans.get("EditDecalDialog.lbl.prompt"));
				showPrompt.setSelected(!preferences.isDecalEditorPreferenceSet());
				showPrompt.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (((JRadioButton) e.getItem()).isSelected()) {
							preferences.clearDecalEditorPreference();
						}
					}
				});
				add(showPrompt, "wrap");
				execGroup.add(showPrompt);
				
				if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.EDIT)) {
					
					JRadioButton systemRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.system"));
					systemRadio.setSelected(preferences.isDecalEditorPreferenceSystem());
					systemRadio.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent e) {
							if (((JRadioButton) e.getItem()).isSelected()) {
								preferences.setDecalEditorPreference(true, null);
							}
						}
					});
					add(systemRadio, "wrap");
					execGroup.add(systemRadio);
					
				}
				
				boolean commandLineIsSelected = preferences.isDecalEditorPreferenceSet() && !preferences.isDecalEditorPreferenceSystem();
				final JRadioButton commandRadio = new JRadioButton(trans.get("EditDecalDialog.lbl.cmdline"));
				commandRadio.setSelected(commandLineIsSelected);
				add(commandRadio, "wrap");
				execGroup.add(commandRadio);
				
				final JTextField commandText = new JTextField();
				commandText.setEnabled(commandLineIsSelected);
				commandText.setText(commandLineIsSelected ? preferences.getDecalEditorCommandLine() : "");
				commandText.getDocument().addDocumentListener(new DocumentListener() {
					
					@Override
					public void insertUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
					@Override
					public void removeUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
					@Override
					public void changedUpdate(DocumentEvent e) {
						preferences.setDecalEditorPreference(false, commandText.getText());
					}
					
				});
				add(commandText, "growx, wrap");
				
				final JButton chooser = new JButton(trans.get("EditDecalDialog.btn.chooser"));
				chooser.setEnabled(commandLineIsSelected);
				chooser.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						JFileChooser fc = new JFileChooser();
						int action = fc.showOpenDialog(SwingUtilities.windowForComponent(PreferencesDialog.this));
						if (action == JFileChooser.APPROVE_OPTION) {
							String commandLine = fc.getSelectedFile().getAbsolutePath();
							commandText.setText(commandLine);
							preferences.setDecalEditorPreference(false, commandLine);
						}
						
					}
					
				});
				add(chooser, "wrap");
				
				
				commandRadio.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						boolean enabled = commandRadio.isSelected();
						commandText.setEnabled(enabled);
						chooser.setEnabled(enabled);
					}
					
				});
			}
		}, "growx, span");
		
		panel.add(new JPanel(new MigLayout("fill, ins n n n")) {
			{/////GL Options
				TitledBorder border = BorderFactory.createTitledBorder(trans.get("pref.dlg.opengl.lbl.title"));
				GUIUtil.changeFontStyle(border, Font.BOLD);
				setBorder(border);
				
				//// The effects will take place the next time you open a window.
				add(new StyledLabel(
						trans.get("pref.dlg.lbl.effect1"), -2, Style.ITALIC),
						"spanx, wrap");
				
				BooleanModel enableGLModel = new BooleanModel(preferences.getBoolean(Preferences.OPENGL_ENABLED, true));
				final JCheckBox enableGL = new JCheckBox(enableGLModel);
				enableGL.setText(trans.get("pref.dlg.opengl.but.enableGL"));
				enableGL.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(Preferences.OPENGL_ENABLED, enableGL.isSelected());
					}
				});
				add(enableGL, "wrap");
				
				final JCheckBox enableAA = new JCheckBox(trans.get("pref.dlg.opengl.but.enableAA"));
				enableAA.setSelected(preferences.getBoolean(Preferences.OPENGL_ENABLE_AA, true));
				enableAA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(Preferences.OPENGL_ENABLE_AA, enableAA.isSelected());
					}
				});
				enableGLModel.addEnableComponent(enableAA);
				add(enableAA, "wrap");
				
				final JCheckBox useFBO = new JCheckBox(trans.get("pref.dlg.opengl.lbl.useFBO"));
				useFBO.setSelected(preferences.getBoolean(Preferences.OPENGL_USE_FBO, false));
				useFBO.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						preferences.putBoolean(Preferences.OPENGL_USE_FBO, useFBO.isSelected());
					}
				});
				enableGLModel.addEnableComponent(useFBO);
				add(useFBO, "wrap");
			}
		}, "growx, span");
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
			return descriptions[preferences.getChoice(preference, descriptions.length, 0)];
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
			
			preferences.putChoice(preference, index);
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
			if (preferences.getBoolean(preference, def)) {
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
				preferences.putBoolean(preference, true);
			} else if (falseDesc.equals(item)) {
				preferences.putBoolean(preference, false);
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
		final JDialog dialog1 = new JDialog(this, ModalityType.APPLICATION_MODAL);
		JPanel panel = new JPanel(new MigLayout());
		
		//// Checking for updates...
		panel.add(new JLabel(trans.get("pref.dlg.lbl.Checkingupdates")), "wrap");
		
		JProgressBar bar = new JProgressBar();
		bar.setIndeterminate(true);
		panel.add(bar, "growx, wrap para");
		
		//// Cancel button
		JButton cancel = new JButton(trans.get("dlg.but.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog1.dispose();
			}
		});
		panel.add(cancel, "right");
		dialog1.add(panel);
		
		GUIUtil.setDisposableDialogOptions(dialog1, cancel);
		
		
		// Timer to monitor progress
		final Timer timer = new Timer(100, null);
		final long startTime = System.currentTimeMillis();
		
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!retriever.isRunning() || startTime + 10000 < System.currentTimeMillis()) {
					timer.stop();
					dialog1.dispose();
				}
			}
		};
		timer.addActionListener(listener);
		timer.start();
		
		
		// Wait for action
		dialog1.setVisible(true);
		
		
		// Check result
		UpdateInfo info = retriever.getUpdateInfo();
		if (info == null) {
			JOptionPane.showMessageDialog(this,
					//// An error occurred while communicating with the server.
					trans.get("pref.dlg.lbl.msg1"),
					//// Unable to retrieve update information
					trans.get("pref.dlg.lbl.msg2"), JOptionPane.WARNING_MESSAGE, null);
		} else if (info.getLatestVersion() == null ||
				info.getLatestVersion().equals("") ||
				BuildProperties.getVersion().equalsIgnoreCase(info.getLatestVersion())) {
			JOptionPane.showMessageDialog(this,
					//// You are running the latest version of OpenRocket.
					trans.get("pref.dlg.lbl.msg3"),
					//// No updates available
					trans.get("pref.dlg.lbl.msg4"), JOptionPane.INFORMATION_MESSAGE, null);
		} else {
			UpdateInfoDialog infoDialog = new UpdateInfoDialog(info);
			infoDialog.setVisible(true);
			if (infoDialog.isReminderSelected()) {
				preferences.putString(SwingPreferences.LAST_UPDATE, "");
			} else {
				preferences.putString(SwingPreferences.LAST_UPDATE, info.getLatestVersion());
			}
		}
		
	}
	
	
	////////  Singleton implementation  ////////
	
	private static PreferencesDialog dialog = null;
	
	public static void showPreferences(Window parent) {
		if (dialog != null) {
			dialog.dispose();
		}
		dialog = new PreferencesDialog(parent);
		dialog.setVisible(true);
	}
	
	
}
