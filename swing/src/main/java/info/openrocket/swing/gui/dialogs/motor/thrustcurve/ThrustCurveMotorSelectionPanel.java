package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.swing.gui.plot.Util;
import info.openrocket.swing.gui.theme.UITheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BugException;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.dialogs.motor.CloseableDialog;
import info.openrocket.swing.gui.dialogs.motor.MotorSelector;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;

public class ThrustCurveMotorSelectionPanel extends JPanel implements MotorSelector {
	private static final long serialVersionUID = -8737784181512143155L;

	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorSelectionPanel.class);

	private static final Translator trans = Application.getTranslator();

	private ThrustCurveMotorSetDatabase database;

	private CloseableDialog dialog = null;

	private final ThrustCurveMotorDatabaseModel model;
	private final JTable table;
	private final TableRowSorter<TableModel> sorter;
	private final MotorRowFilter rowFilter;
	private final JCheckBox hideUnavailableBox;
	
	private final JLabel nrOfMotorsLabel;

	private final JTextField searchField;

	JTabbedPane rightSide;

	private final MotorFilterPanel motorFilterPanel;
	private final MotorInformationPanel motorInformationPanel;

	private ThrustCurveMotorSet selectedMotorSet;

	private static Color dimTextColor;

	static {
		initColors();
	}

	public ThrustCurveMotorSelectionPanel( final FlightConfigurationId fcid, MotorMount mount ) {
		this();
		setMotorMountAndConfig( fcid, mount );

	}

	public ThrustCurveMotorSelectionPanel() {
		super(new MigLayout("fill", "[grow][]"));

		database = Application.getThrustCurveMotorSetDatabase();

		model = new ThrustCurveMotorDatabaseModel(database);
		rowFilter = new MotorRowFilter(model);
		motorInformationPanel = new MotorInformationPanel();

		//// MotorFilter
		{
			// Find all the manufacturers:
			Set<Manufacturer> allManufacturers = new HashSet<>();
			for (ThrustCurveMotorSet s : database) {
				allManufacturers.add(s.getManufacturer());
			}

			motorFilterPanel = new MotorFilterPanel(allManufacturers, rowFilter) {
				private static final long serialVersionUID = 8441555209804602238L;

				@Override
				public void onSelectionChanged() {
					sorter.sort();
					scrollSelectionVisible();
				}
			};

		}

		////  GUI
		JPanel panel = new JPanel(new MigLayout("fill","[][grow]"));
		
		//// Hide unavailable motors
		{
			hideUnavailableBox = new JCheckBox(trans.get("TCMotorSelPan.checkbox.hideUnavailable"));
			GUIUtil.changeFontSize(hideUnavailableBox, -1);
			hideUnavailableBox.setSelected(Application.getPreferences().getBoolean(ApplicationPreferences.MOTOR_HIDE_UNAVAILABLE, true));
			hideUnavailableBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Application.getPreferences().putBoolean(ApplicationPreferences.MOTOR_HIDE_UNAVAILABLE, hideUnavailableBox.isSelected());
					motorFilterPanel.setHideUnavailable(hideUnavailableBox.isSelected());
				}
			});
			panel.add(hideUnavailableBox, "gapleft para, spanx, growx, wrap");
			
		}

		//// Motor name column
		{
			JLabel motorNameColumn = new JLabel(trans.get("TCMotorSelPan.lbl.motorNameColumn"));
			motorNameColumn.setToolTipText(trans.get("TCMotorSelPan.lbl.motorNameColumn.ttip"));
			JRadioButton commonName = new JRadioButton(trans.get("TCMotorSelPan.btn.commonName"));
			JRadioButton designation = new JRadioButton(trans.get("TCMotorSelPan.btn.designation"));
			ButtonGroup bg = new ButtonGroup();
			bg.add(commonName);
			bg.add(designation);
			commonName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					((SwingPreferences) Application.getPreferences()).setMotorNameColumn(false);
					int selectedRow = table.getSelectedRow();
					model.fireTableDataChanged();
					if (selectedRow >= 0) {
						table.setRowSelectionInterval(selectedRow, selectedRow);
					}
				}
			});
			designation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					((SwingPreferences) Application.getPreferences()).setMotorNameColumn(true);
					int selectedRow = table.getSelectedRow();
					model.fireTableDataChanged();
					if (selectedRow >= 0) {
						table.setRowSelectionInterval(selectedRow, selectedRow);
					}
				}
			});

			boolean initValue = ((SwingPreferences) Application.getPreferences()).getMotorNameColumn();
			commonName.setSelected(!initValue);
			designation.setSelected(initValue);

			panel.add(motorNameColumn, "gapleft para");
			panel.add(commonName);
			panel.add(designation, "spanx, growx, wrap");
		}

		//// Motor selection table
		{
			table = new JTable(model);

			// Set comparators and widths
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			sorter = new TableRowSorter<>(model);
			for (int i = 0; i < ThrustCurveMotorColumns.values().length; i++) {
				ThrustCurveMotorColumns column = ThrustCurveMotorColumns.values()[i];
				sorter.setComparator(i, column.getComparator());
				table.getColumnModel().getColumn(i).setPreferredWidth(column.getWidth());
			}
			table.setRowSorter(sorter);
			// force initial sort order to by diameter, total impulse, manufacturer
			{
				RowSorter.SortKey[] sortKeys = {
						new RowSorter.SortKey(ThrustCurveMotorColumns.DIAMETER.ordinal(), SortOrder.ASCENDING),
						new RowSorter.SortKey(ThrustCurveMotorColumns.TOTAL_IMPULSE.ordinal(), SortOrder.ASCENDING),
						new RowSorter.SortKey(ThrustCurveMotorColumns.MANUFACTURER.ordinal(), SortOrder.ASCENDING)
				};
				sorter.setSortKeys(Arrays.asList(sortKeys));
			}

			sorter.setRowFilter(rowFilter);

			// Set selection and double-click listeners
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					selectMotorSetFromTable();
				}
			});
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
						if (dialog != null) {
							dialog.close(true);
						}
					}
				}
			});

			JScrollPane scrollpane = new JScrollPane();
			scrollpane.setViewportView(table);
			panel.add(scrollpane, "grow, width :500:, spanx, push, wrap");

		}

		// Number of motors
		{
			nrOfMotorsLabel = new StyledLabel(-2.0f, StyledLabel.Style.ITALIC);
			nrOfMotorsLabel.setToolTipText(trans.get("TCMotorSelPan.lbl.ttip.nrOfMotors"));
			updateNrOfMotors();
			nrOfMotorsLabel.setForeground(dimTextColor);
			panel.add(nrOfMotorsLabel, "gapleft para, spanx, wrap");
			sorter.addRowSorterListener(new RowSorterListener() {
				@Override
				public void sorterChanged(RowSorterEvent e) {
					updateNrOfMotors();
				}
			});
			rowFilter.addChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(EventObject e) {
					updateNrOfMotors();
				}
			});
		}

		// Search field
		{
			//// Search:
			StyledLabel label = new StyledLabel(trans.get("TCMotorSelPan.lbl.Search"));
			panel.add(label);
			searchField = new JTextField();
			searchField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					update();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					clearMotorSetSelection();
					update();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					update();
				}
				private void update() {
					String text = searchField.getText().trim();
					String[] split = text.split("\\s+");
					rowFilter.setSearchTerms(Arrays.asList(split));
					sorter.sort();
					scrollSelectionVisible();
				}
			});
			panel.add(searchField, "span, growx");
		}
		this.add(panel, "grow");

		// Vertical split
		this.add(new JSeparator(JSeparator.VERTICAL), "growy, gap para para");

		rightSide = new JTabbedPane();
		rightSide.add(trans.get("TCMotorSelPan.btn.filter"), motorFilterPanel);
		rightSide.add(trans.get("TCMotorSelPan.btn.details"), motorInformationPanel);

		this.add(rightSide, "growy");

		hideUnavailableBox.getActionListeners()[0].actionPerformed(null);
		
		// Update the panel data
		updateData();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(ThrustCurveMotorSelectionPanel::updateColors);
	}

	public static void updateColors() {
		dimTextColor = GUIUtil.getUITheme().getDimTextColor();
	}

	public void setMotorMountAndConfig( final FlightConfigurationId fcid,  MotorMount mountToEdit ) {
		if ( null == fcid ){
			throw new NullPointerException(" attempted to set mount with a null FCID. bug.  ");
		}else if ( null == mountToEdit ){
			throw new NullPointerException(" attempted to set mount with a null mount. bug. ");
		}
		motorFilterPanel.setMotorMount(mountToEdit);
		
		MotorConfiguration curMotorInstance = mountToEdit.getMotorConfig(fcid);
		
		ThrustCurveMotor motorToSelect = null;
		double delay = 0;
		ThrustCurveMotorSet motorSetToSelect = null;
		
		if (curMotorInstance.hasMotor()) {
			motorToSelect = (ThrustCurveMotor) (curMotorInstance.getMotor());
			motorSetToSelect = database.findMotorSet(motorToSelect);
			delay = curMotorInstance.getEjectionDelay();

			motorInformationPanel.setMotor(motorSetToSelect, motorToSelect, delay);
			setMotorSet(motorSetToSelect);
		} else {
			clearMotorSetSelection();
		}


		scrollSelectionVisible();

	}

	@Override
	public Motor getSelectedMotor() {
		return motorInformationPanel.getSelectedMotor();
	}


	@Override
	public double getSelectedDelay() {
		return motorInformationPanel.getSelectedDelay();
	}


	@Override
	public JComponent getDefaultFocus() {
		return searchField;
	}

	@Override
	public void selectedMotor(Motor motorSelection) {
		if (!(motorSelection instanceof ThrustCurveMotor)) {
			log.error("Received argument that was not ThrustCurveMotor: " + motorSelection);
			return;
		}

		ThrustCurveMotor motor = (ThrustCurveMotor) motorSelection;
		ThrustCurveMotorSet set = database.findMotorSet(motor);
		if (set == null) {
			log.error("Could not find set for motor:" + motorSelection);
			return;
		}

		// Store selected motor in preferences node, set all others to false
		Preferences prefs = ((SwingPreferences) Application.getPreferences()).getNode(ApplicationPreferences.PREFERRED_THRUST_CURVE_MOTOR_NODE);
		for (ThrustCurveMotor m : set.getMotors()) {
			String digest = m.getDigest();
			prefs.putBoolean(digest, m == motor);
		}
	}

	public void setCloseableDialog(CloseableDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * Clear the current motor set selection
	 */
	public void clearMotorSetSelection() {
		selectedMotorSet = null;
		updateData();
	}

	/**
	 * Clear the search field
	 */
	public void clearSearch() {
		searchField.setText("");
	}
	
	/**
	 * Called when a motor set is selected
	 */
	private void setMotorSet(ThrustCurveMotorSet motorSet) {
		if (selectedMotorSet == motorSet || motorSet == null)
			return;

		selectedMotorSet = motorSet;
		motorInformationPanel.setMotor(selectedMotorSet, null, 0);
		updateData();

		scrollSelectionVisible();
	}

	private void updateData() {

		if (selectedMotorSet == null) {
			// No motor selected
			motorInformationPanel.clearData();
			table.clearSelection();
			rightSide.setSelectedComponent(motorFilterPanel);
			return;
		}

		motorInformationPanel.updateData();
		rightSide.setSelectedComponent(motorInformationPanel);
	}

	private void updateNrOfMotors() {
		if (table != null && nrOfMotorsLabel != null) {
			int rowCount = table.getRowCount();
			String motorCount = trans.get("TCMotorSelPan.lbl.nrOfMotors.None");
			if (rowCount > 0) {
				motorCount = String.valueOf(rowCount);
			}
			nrOfMotorsLabel.setText(trans.get("TCMotorSelPan.lbl.nrOfMotors") + " " + motorCount);
		}
	}


	private void scrollSelectionVisible() {
		if (selectedMotorSet != null) {
			int index = table.convertRowIndexToView(model.getIndex(selectedMotorSet));
			table.getSelectionModel().setSelectionInterval(index, index);
			Rectangle rect = table.getCellRect(index, 0, true);
			rect = new Rectangle(rect.x, rect.y - 100, rect.width, rect.height + 200);
			table.scrollRectToVisible(rect);
		}
	}


	/**
	 * Selects a new motor set based on the selection in the motor table
	 */
	public void selectMotorSetFromTable() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			row = table.convertRowIndexToModel(row);
			ThrustCurveMotorSet motorSet = model.getMotorSet(row);
			log.info(Markers.USER_MARKER, "Selected table row " + row + ": " + motorSet);
			if (motorSet != selectedMotorSet) {
				setMotorSet(motorSet);
			}
		} else {
			log.info(Markers.USER_MARKER, "Selected table row " + row + ", nothing selected");
		}
	}


	//////////////////////

}
