package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jfree.chart.ChartColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.dialogs.motor.CloseableDialog;
import net.sf.openrocket.gui.dialogs.motor.MotorSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.utils.MotorCorrelation;

public class ThrustCurveMotorSelectionPanel extends JPanel implements MotorSelector {
	private static final long serialVersionUID = -8737784181512143155L;

	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotorSelectionPanel.class);

	private static final Translator trans = Application.getTranslator();

	private static final double MOTOR_SIMILARITY_THRESHOLD = 0.95;

	private static final Paint[] CURVE_COLORS = ChartColor.createDefaultPaintArray();

	private static final ThrustCurveMotorComparator MOTOR_COMPARATOR = new ThrustCurveMotorComparator();

	private List<ThrustCurveMotorSet> database;

	private CloseableDialog dialog = null;

	private final ThrustCurveMotorDatabaseModel model;
	private final JTable table;
	private final TableRowSorter<TableModel> sorter;
	private final MotorRowFilter rowFilter;

	private final JCheckBox hideSimilarBox;
	private final JCheckBox hideUnavailableBox;

	private final JTextField searchField;

	private final JLabel curveSelectionLabel;
	private final JComboBox<MotorHolder> curveSelectionBox;
	private final DefaultComboBoxModel<MotorHolder> curveSelectionModel;
	private final JComboBox<String> delayBox;

	private final MotorInformationPanel motorInformationPanel;
	private final MotorFilterPanel motorFilterPanel;

	private ThrustCurveMotor selectedMotor;
	private ThrustCurveMotorSet selectedMotorSet;
	private double selectedDelay;

	public ThrustCurveMotorSelectionPanel( final FlightConfigurationId fcid, MotorMount mount ) {
		this();
		setMotorMountAndConfig( fcid, mount );

	}
	
	/**
	 * Sole constructor.
	 * 
	 * @param current	the currently selected ThrustCurveMotor, or <code>null</code> for none.
	 * @param delay		the currently selected ejection charge delay.
	 * @param diameter	the diameter of the motor mount.
	 */
	public ThrustCurveMotorSelectionPanel() {
		super(new MigLayout("fill", "[grow][]"));

		// Construct the database (adding the current motor if not in the db already)
		database = Application.getThrustCurveMotorSetDatabase().getMotorSets();

		model = new ThrustCurveMotorDatabaseModel(database);
		rowFilter = new MotorRowFilter(model);
		motorInformationPanel = new MotorInformationPanel();

		//// MotorFilter
		{
			// Find all the manufacturers:
			Set<Manufacturer> allManufacturers = new HashSet<Manufacturer>();
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

		//// Select thrust curve:
		{
			curveSelectionLabel = new JLabel(trans.get("TCMotorSelPan.lbl.Selectthrustcurve"));
			panel.add(curveSelectionLabel);

			curveSelectionModel = new DefaultComboBoxModel<MotorHolder>();
			curveSelectionBox = new JComboBox<MotorHolder>(curveSelectionModel);
			@SuppressWarnings("unchecked")
			ListCellRenderer<MotorHolder> lcr = (ListCellRenderer<MotorHolder>) curveSelectionBox.getRenderer(); 
			curveSelectionBox.setRenderer(new CurveSelectionRenderer(lcr));
			curveSelectionBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MotorHolder value = (MotorHolder)curveSelectionBox.getSelectedItem();
					if (value != null) {
						select(((MotorHolder) value).getMotor());
					}
				}
			});
			panel.add(curveSelectionBox, "growx, wrap");
		}

		// Ejection charge delay:
		{
			panel.add(new JLabel(trans.get("TCMotorSelPan.lbl.Ejectionchargedelay")));

			delayBox = new JComboBox<String>();
			delayBox.setEditable(true);
			delayBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					
					String sel = (String) delayBox.getSelectedItem();
					//// None
					if (sel.equalsIgnoreCase(trans.get("TCMotorSelPan.equalsIgnoreCase.None"))) {
						selectedDelay = Motor.PLUGGED_DELAY;
					} else {
						try {
							selectedDelay = Double.parseDouble(sel);
						} catch (NumberFormatException ignore) {
						}
					}
					setDelays(false);
				}
			});
			panel.add(delayBox, "growx,wrap");
			//// (Number of seconds or \"None\")
			panel.add(new StyledLabel(trans.get("TCMotorSelPan.lbl.NumberofsecondsorNone"), -3), "skip, wrap");
			setDelays(false);
		}

		//// Hide very similar thrust curves
		{
			hideSimilarBox = new JCheckBox(trans.get("TCMotorSelPan.checkbox.hideSimilar"));
			GUIUtil.changeFontSize(hideSimilarBox, -1);
			hideSimilarBox.setSelected(Application.getPreferences().getBoolean(net.sf.openrocket.startup.Preferences.MOTOR_HIDE_SIMILAR, true));
			hideSimilarBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Application.getPreferences().putBoolean(net.sf.openrocket.startup.Preferences.MOTOR_HIDE_SIMILAR, hideSimilarBox.isSelected());
					updateData();
				}
			});
			panel.add(hideSimilarBox, "gapleft para, spanx, growx, wrap");
		}
		
		//// Hide unavailable motors
		{
			hideUnavailableBox = new JCheckBox(trans.get("TCMotorSelPan.checkbox.hideUnavailable"));
			GUIUtil.changeFontSize(hideUnavailableBox, -1);
			hideUnavailableBox.setSelected(Application.getPreferences().getBoolean(net.sf.openrocket.startup.Preferences.MOTOR_HIDE_UNAVAILABLE, true));
			hideUnavailableBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Application.getPreferences().putBoolean(net.sf.openrocket.startup.Preferences.MOTOR_HIDE_UNAVAILABLE, hideUnavailableBox.isSelected());
					motorFilterPanel.setHideUnavailable(hideUnavailableBox.isSelected());
				}
			});
			panel.add(hideUnavailableBox, "gapleft para, spanx, growx, wrap");
			
		}

		//// Motor selection table
		{
			table = new JTable(model);

			// Set comparators and widths
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			sorter = new TableRowSorter<TableModel>(model);
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
					int row = table.getSelectedRow();
					if (row >= 0) {
						row = table.convertRowIndexToModel(row);
						ThrustCurveMotorSet motorSet = model.getMotorSet(row);
						log.info(Markers.USER_MARKER, "Selected table row " + row + ": " + motorSet);
						if (motorSet != selectedMotorSet) {
							select(selectMotor(motorSet));
						}
					} else {
						log.info(Markers.USER_MARKER, "Selected table row " + row + ", nothing selected");
					}
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
			panel.add(scrollpane, "grow, width :500:, spanx, wrap");

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

		JTabbedPane rightSide = new JTabbedPane();
		rightSide.add(trans.get("TCMotorSelPan.btn.filter"), motorFilterPanel);
		rightSide.add(trans.get("TCMotorSelPan.btn.details"), motorInformationPanel);

		this.add(rightSide);

		// Update the panel data
		updateData();
		setDelays(false);

	}

	public void setMotorMountAndConfig( final FlightConfigurationId _fcid,  MotorMount mountToEdit ) {
		if ( null == _fcid ){
			throw new NullPointerException(" attempted to set mount with a null FCID. bug.  ");
		}else if ( null == mountToEdit ){
			throw new NullPointerException(" attempted to set mount with a null mount. bug. ");
		}
		motorFilterPanel.setMotorMount(mountToEdit);
		
		MotorConfiguration curMotorInstance = mountToEdit.getMotorConfig(_fcid);
		selectedMotor = null;
		selectedMotorSet = null;
		selectedDelay = 0;
		ThrustCurveMotor motorToSelect = null;
		if ( curMotorInstance.hasMotor()){ 
			motorToSelect = (ThrustCurveMotor) curMotorInstance.getMotor();
			selectedDelay = curMotorInstance.getEjectionDelay();
		}
		
		// If current motor is not found in db, add a new ThrustCurveMotorSet containing it
		if (motorToSelect != null) {
			ThrustCurveMotorSet motorSetToSelect = null;
			motorSetToSelect = findMotorSet(motorToSelect);
			if (motorSetToSelect == null) {
				database = new ArrayList<ThrustCurveMotorSet>(database);
				ThrustCurveMotorSet extra = new ThrustCurveMotorSet();
				extra.addMotor(motorToSelect);
				database.add(extra);
				Collections.sort(database);
			}
			
			select(motorToSelect);

		}
		motorFilterPanel.setMotorMount(mountToEdit);
		scrollSelectionVisible();
	}

	@Override
	public Motor getSelectedMotor() {
		return selectedMotor;
	}


	@Override
	public double getSelectedDelay() {
		return selectedDelay;
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
		ThrustCurveMotorSet set = findMotorSet(motor);
		if (set == null) {
			log.error("Could not find set for motor:" + motorSelection);
			return;
		}

		// Store selected motor in preferences node, set all others to false
		Preferences prefs = ((SwingPreferences) Application.getPreferences()).getNode(net.sf.openrocket.startup.Preferences.PREFERRED_THRUST_CURVE_MOTOR_NODE);
		for (ThrustCurveMotor m : set.getMotors()) {
			String digest = m.getDigest();
			prefs.putBoolean(digest, m == motor);
		}
	}

	public void setCloseableDialog(CloseableDialog dialog) {
		this.dialog = dialog;
	}



	/**
	 * Called when a different motor is selected from within the panel.
	 */
	private void select(ThrustCurveMotor motor) {
		if (selectedMotor == motor || motor == null)
			return;

		ThrustCurveMotorSet set = findMotorSet(motor);
		if (set == null) {
			throw new BugException("Could not find motor from database, motor=" + motor);
		}

		boolean updateDelays = (selectedMotorSet != set);

		selectedMotor = motor;
		selectedMotorSet = set;
		updateData();
		if (updateDelays) {
			setDelays(true);
		}
		scrollSelectionVisible();
	}


	private void updateData() {

		if (selectedMotorSet == null) {
			// No motor selected
			curveSelectionModel.removeAllElements();
			curveSelectionBox.setEnabled(false);
			curveSelectionLabel.setEnabled(false);
			motorInformationPanel.clearData();
			table.clearSelection();
			return;
		}

		// Check which thrust curves to display
		List<ThrustCurveMotor> motors = getFilteredCurves();
		final int index = motors.indexOf(selectedMotor);

		// Update the thrust curve selection box
		curveSelectionModel.removeAllElements();
		for (int i = 0; i < motors.size(); i++) {
			curveSelectionModel.addElement(new MotorHolder(motors.get(i), i));
		}
		curveSelectionBox.setSelectedIndex(index);

		if (motors.size() > 1) {
			curveSelectionBox.setEnabled(true);
			curveSelectionLabel.setEnabled(true);
		} else {
			curveSelectionBox.setEnabled(false);
			curveSelectionLabel.setEnabled(false);
		}

		motorInformationPanel.updateData(motors, selectedMotor);

	}

	List<ThrustCurveMotor> getFilteredCurves() {
		List<ThrustCurveMotor> motors = selectedMotorSet.getMotors();
		if (hideSimilarBox.isSelected()  && selectedMotor != null) {
			List<ThrustCurveMotor> filtered = new ArrayList<ThrustCurveMotor>(motors.size());
			for (int i = 0; i < motors.size(); i++) {
				ThrustCurveMotor m = motors.get(i);
				if (m.equals(selectedMotor)) {
					filtered.add(m);
					continue;
				}
				double similarity = MotorCorrelation.similarity(selectedMotor, m);
				log.debug("Motor similarity: " + similarity);
				if (similarity < MOTOR_SIMILARITY_THRESHOLD) {
					filtered.add(m);
				}
			}
			motors = filtered;
		}

		Collections.sort(motors, MOTOR_COMPARATOR);

		return motors;
	}


	private void scrollSelectionVisible() {
		if (selectedMotorSet != null) {
			int index = table.convertRowIndexToView(model.getIndex(selectedMotorSet));
			//System.out.println("index=" + index);
			table.getSelectionModel().setSelectionInterval(index, index);
			Rectangle rect = table.getCellRect(index, 0, true);
			rect = new Rectangle(rect.x, rect.y - 100, rect.width, rect.height + 200);
			table.scrollRectToVisible(rect);
		}
	}


	public static Color getColor(int index) {
		return (Color) CURVE_COLORS[index % CURVE_COLORS.length];
	}


	/**
	 * Find the ThrustCurveMotorSet that contains a motor.
	 * 
	 * @param motor		the motor to look for.
	 * @return			the ThrustCurveMotorSet, or null if not found.
	 */
	private ThrustCurveMotorSet findMotorSet(ThrustCurveMotor motor) {
		for (ThrustCurveMotorSet set : database) {
			if (set.getMotors().contains(motor)) {
				return set;
			}
		}

		return null;
	}



	/**
	 * Select the default motor from this ThrustCurveMotorSet.  This uses primarily motors
	 * that the user has previously used, and secondarily a heuristic method of selecting which
	 * thrust curve seems to be better or more reliable.
	 * 
	 * @param set	the motor set
	 * @return		the default motor in this set
	 */
	private ThrustCurveMotor selectMotor(ThrustCurveMotorSet set) {
		if (set.getMotorCount() == 0) {
			throw new BugException("Attempting to select motor from empty ThrustCurveMotorSet: " + set);
		}
		if (set.getMotorCount() == 1) {
			return set.getMotors().get(0);
		}


		// Find which motor has been used the most recently
		List<ThrustCurveMotor> list = set.getMotors();
		Preferences prefs = ((SwingPreferences) Application.getPreferences()).getNode(net.sf.openrocket.startup.Preferences.PREFERRED_THRUST_CURVE_MOTOR_NODE);
		for (ThrustCurveMotor m : list) {
			String digest = m.getDigest();
			if (prefs.getBoolean(digest, false)) {
				return m;
			}
		}

		// No motor has been used
		Collections.sort(list, MOTOR_COMPARATOR);
		return list.get(0);
	}


	/**
	 * Set the values in the delay combo box.  If <code>reset</code> is <code>true</code>
	 * then sets the selected value as the value closest to selectedDelay, otherwise
	 * leaves selection alone.
	 */
	private void setDelays(boolean reset) {
		if (selectedMotor == null) {

			//// None
			delayBox.setModel(new DefaultComboBoxModel<String>(new String[] { trans.get("TCMotorSelPan.delayBox.None") }));
			delayBox.setSelectedIndex(0);

		} else {

			List<Double> delays = selectedMotorSet.getDelays();
			String[] delayStrings = new String[delays.size()];
			double currentDelay = selectedDelay; // Store current setting locally

			for (int i = 0; i < delays.size(); i++) {
				//// None
				delayStrings[i] = ThrustCurveMotor.getDelayString(delays.get(i), trans.get("TCMotorSelPan.delayBox.None"));
			}
			delayBox.setModel(new DefaultComboBoxModel<String>(delayStrings));

			if (reset) {

				// Find and set the closest value
				double closest = Double.NaN;
				for (int i = 0; i < delays.size(); i++) {
					// if-condition to always become true for NaN
					if (!(Math.abs(delays.get(i) - currentDelay) > Math.abs(closest - currentDelay))) {
						closest = delays.get(i);
					}
				}
				if (!Double.isNaN(closest)) {
					selectedDelay = closest;
					//// None
					delayBox.setSelectedItem(ThrustCurveMotor.getDelayString(closest, trans.get("TCMotorSelPan.delayBox.None")));
				} else {
					delayBox.setSelectedItem("None");
				}

			} else {

				selectedDelay = currentDelay;
				//// None
				delayBox.setSelectedItem(ThrustCurveMotor.getDelayString(currentDelay, trans.get("TCMotorSelPan.delayBox.None")));

			}

		}
	}

	//////////////////////


	private class CurveSelectionRenderer implements ListCellRenderer<MotorHolder> {

		private final ListCellRenderer<MotorHolder> renderer;

		public CurveSelectionRenderer(ListCellRenderer<MotorHolder> renderer) {
			this.renderer = renderer;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends MotorHolder> list, MotorHolder value, int index,
				boolean isSelected, boolean cellHasFocus) {

			Component c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof MotorHolder) {
				MotorHolder m = (MotorHolder) value;
				c.setForeground(getColor(m.getIndex()));
			}

			return c;
		}

	}


}
