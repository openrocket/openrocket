package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.simulation.RK4SimulationStepper;
import net.sf.openrocket.simulation.listeners.SimulationListener;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GeodeticComputationStrategy;

public class SimulationPreferencesPanel extends PreferencesPanel {

	/*
	 * private GeodeticComputationStrategy geodeticComputation =
	 * GeodeticComputationStrategy.SPHERICAL;
	 */

	public SimulationPreferencesPanel() {
		super(new MigLayout("fill"));

		// Confirm deletion of simulations:
		final JCheckBox confirmDelete = new JCheckBox(
				trans.get("pref.dlg.lbl.Confirmdeletion"));
		confirmDelete.setSelected(preferences.getConfirmSimDeletion());
		confirmDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoRunSimulations(confirmDelete.isSelected());
			}
		});
		this.add(confirmDelete, "wrap, growx, sg combos ");

		// Automatically run all simulation out-dated by design changes.
		final JCheckBox automaticallyRunSimsBox = new JCheckBox(
				trans.get("pref.dlg.checkbox.Runsimulations"));
		automaticallyRunSimsBox
				.setSelected(preferences.getAutoRunSimulations());
		automaticallyRunSimsBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.setAutoRunSimulations(automaticallyRunSimsBox
						.isSelected());
			}
		});
		this.add(automaticallyRunSimsBox, "wrap, growx, sg combos ");

		GeodeticComputationStrategy geodeticComputation = GeodeticComputationStrategy.SPHERICAL;

		JPanel sub, subsub;
		String tip;
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		UnitSelector unit;
		BasicSlider slider;

		// // Simulation options
		sub = new JPanel(new MigLayout("fill"));
		// // Simulator options
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.border.Simopt")));
		this.add(sub, "growx, growy, aligny 0");

		// Separate panel for computation methods, as they use a different
		// layout
		subsub = new JPanel(new MigLayout("insets 0, fill", "[grow][min!][min!][]"));

		// // Calculation method:
		tip = trans.get("simedtdlg.lbl.ttip.Calcmethod");
		label = new JLabel(trans.get("simedtdlg.lbl.Calcmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");

		// // Extended Barrowman
		label = new JLabel(trans.get("simedtdlg.lbl.ExtBarrowman"));
		label.setToolTipText(tip);
		subsub.add(label, "growx, span 3, wrap");

		// Simulation method
		tip = trans.get("simedtdlg.lbl.ttip.Simmethod1")
				+ trans.get("simedtdlg.lbl.ttip.Simmethod2");
		label = new JLabel(trans.get("simedtdlg.lbl.Simmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");

		label = new JLabel("6-DOF Runge-Kutta 4");
		label.setToolTipText(tip);
		subsub.add(label, "growx, span 3, wrap");

		// // Geodetic calculation method:
		label = new JLabel(trans.get("simedtdlg.lbl.GeodeticMethod"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.GeodeticMethodTip"));
		subsub.add(label, "gapright para");

		EnumModel<GeodeticComputationStrategy> gcsModel = new EnumModel<GeodeticComputationStrategy>(
				preferences, "GeodeticComputation");
		final JComboBox gcsCombo = new JComboBox(gcsModel);
		ActionListener gcsTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeodeticComputationStrategy gcs = (GeodeticComputationStrategy) gcsCombo
						.getSelectedItem();
				gcsCombo.setToolTipText(gcs.getDescription());
			}
		};
		gcsCombo.addActionListener(gcsTTipListener);
		gcsTTipListener.actionPerformed(null);
		subsub.add(gcsCombo, "span 3, wrap");


		// // Time step:
		label = new JLabel(trans.get("simedtdlg.lbl.Timestep"));
		tip = trans.get("simedtdlg.lbl.ttip.Timestep1")
				+ trans.get("simedtdlg.lbl.ttip.Timestep2")
				+ " "
				+ UnitGroup.UNITS_TIME_STEP
						.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP)
				+ ".";
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");

		m = new DoubleModel(preferences, "TimeStep", UnitGroup.UNITS_TIME_STEP,
				0, 1);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		subsub.add(spin, "");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		subsub.add(unit, "");
		slider = new BasicSlider(m.getSliderModel(0, 0.2));
		slider.setToolTipText(tip);
		subsub.add(slider, "w 100");

		sub.add(subsub, "spanx, wrap para");

		// Reset to default button
		JButton button = new JButton(trans.get("simedtdlg.but.resettodefault"));
		// Reset the time step to its default value (
		button.setToolTipText(trans.get("simedtdlg.but.ttip.resettodefault")
				+ UnitGroup.UNITS_SHORT_TIME
						.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP)
				+ ").");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences
						.setTimeStep(RK4SimulationStepper.RECOMMENDED_TIME_STEP);
				preferences
						.setGeodeticComputation(GeodeticComputationStrategy.SPHERICAL);
			}
		});

		sub.add(button, "align left");

		/*
		 * // // Simulation listeners sub = new JPanel(new
		 * MigLayout("fill, gap 0 0")); // // Simulator listeners
		 * sub.setBorder(BorderFactory.createTitledBorder(trans
		 * .get("simedtdlg.border.Simlist"))); this.add(sub, "growx, growy");
		 * 
		 * DescriptionArea desc = new DescriptionArea(5); // //
		 * <html><i>Simulation listeners</i> is an advanced feature that //
		 * allows user-written code to listen to and interact with the //
		 * simulation. // // For details on writing simulation listeners, see
		 * the OpenRocket // technical documentation.
		 * desc.setText(trans.get("simedtdlg.txt.longA1") +
		 * trans.get("simedtdlg.txt.longA2")); sub.add(desc,
		 * "aligny 0, growx, wrap para");
		 * 
		 * // // Current listeners: label = new
		 * JLabel(trans.get("simedtdlg.lbl.Curlist")); sub.add(label,
		 * "spanx, wrap rel");
		 * 
		 * final ListenerListModel listenerModel = new ListenerListModel();
		 * final JList list = new JList(listenerModel); list.setCellRenderer(new
		 * ListenerCellRenderer()); JScrollPane scroll = new JScrollPane(list);
		 * // scroll.setPreferredSize(new Dimension(1,1)); sub.add(scroll,
		 * "height 1px, grow, wrap rel");
		 * 
		 * // // Add button button = new
		 * JButton(trans.get("simedtdlg.but.add")); button.addActionListener(new
		 * ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { String
		 * previous = Application.getPreferences().getString(
		 * "previousListenerName", ""); String input = (String)
		 * JOptionPane.showInputDialog(
		 * SwingUtilities.getRoot(SimulationPreferencesPanel.this), new Object[]
		 * { // // Type the full Java class name of the // simulation listener,
		 * for example:
		 * "Type the full Java class name of the simulation listener, for example:"
		 * , "<html><tt>" + CSVSaveListener.class.getName() + "</tt>" }, // //
		 * Add simulation listener trans.get("simedtdlg.lbl.Addsimlist"),
		 * JOptionPane.QUESTION_MESSAGE, null, null, previous); if (input ==
		 * null || input.equals("")) return;
		 * 
		 * Application.getPreferences().putString("previousListenerName",
		 * input); // preferences.getSimulationListeners().add(input);
		 * listenerModel.fireContentsChanged(); } }); sub.add(button,
		 * "split 2, sizegroup buttons, alignx 50%, gapright para");
		 * 
		 * // // Remove button button = new
		 * JButton(trans.get("simedtdlg.but.remove"));
		 * button.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent e) { int[] selected
		 * = list.getSelectedIndices(); Arrays.sort(selected); for (int i =
		 * selected.length - 1; i >= 0; i--) { //
		 * simulation.getSimulationListeners().remove(selected[i]); }
		 * listenerModel.fireContentsChanged(); } }); sub.add(button,
		 * "sizegroup buttons, alignx 50%");
		 */
	}

	private class ListenerCellRenderer extends JLabel implements
			ListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			String s = value.toString();
			setText(s);

			// Attempt instantiating, catch any exceptions
			Exception ex = null;
			try {
				Class<?> c = Class.forName(s);
				@SuppressWarnings("unused")
				SimulationListener l = (SimulationListener) c.newInstance();
			} catch (Exception e) {
				ex = e;
			}

			if (ex == null) {
				setIcon(Icons.SIMULATION_LISTENER_OK);
				// // Listener instantiated successfully.
				setToolTipText("Listener instantiated successfully.");
			} else {
				setIcon(Icons.SIMULATION_LISTENER_ERROR);
				// // <html>Unable to instantiate listener due to exception:<br>
				setToolTipText("<html>Unable to instantiate listener due to exception:<br>"
						+ ex.toString());
			}

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setOpaque(true);
			return this;
		}
	}

	/*
	 * private class ListenerListModel extends AbstractListModel {
	 * 
	 * @Override public String getElementAt(int index) { if (index < 0 || index
	 * >= getSize()) return null; return
	 * simulation.getSimulationListeners().get(index); }
	 * 
	 * @Override public int getSize() { return
	 * simulation.getSimulationListeners().size(); }
	 * 
	 * public void fireContentsChanged() { super.fireContentsChanged(this, 0,
	 * getSize()); } }
	 */
}
