package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.dialogs.flightconfiguration.IgnitionSelectionDialog;
import net.sf.openrocket.gui.dialogs.flightconfiguration.MotorMountConfigurationPanel;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration;
import net.sf.openrocket.rocketcomponent.IgnitionConfiguration.IgnitionEvent;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Chars;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Pair;

public class MotorConfigurationPanel extends FlightConfigurablePanel<MotorMount> {

	private static final String NONE = trans.get("edtmotorconfdlg.tbl.None");

	private final JButton selectMotorButton, removeMotorButton, selectIgnitionButton, resetIgnitionButton;

	protected FlightConfigurableTableModel<MotorMount> configurationTableModel;

	MotorConfigurationPanel(final FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(flightConfigurationPanel,rocket);

		{
			//// Select motor mounts
			JPanel subpanel = new JPanel(new MigLayout(""));
			JLabel label = new StyledLabel(trans.get("lbl.motorMounts"), Style.BOLD);
			subpanel.add(label, "wrap");

			MotorMountConfigurationPanel mountConfigPanel = new MotorMountConfigurationPanel(this,rocket) {
				@Override
				public void onDataChanged() {
					MotorConfigurationPanel.this.fireTableDataChanged();

				}
			};
			subpanel.add(mountConfigPanel, "grow");
			this.add(subpanel, "split, w 200lp, growy");
		}

		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "grow, wrap");

		//// Select motor
		selectMotorButton = new JButton(trans.get("MotorConfigurationPanel.btn.selectMotor"));
		selectMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectMotor();
			}
		});
		this.add(selectMotorButton, "split, align right, sizegroup button");

		//// Remove motor button
		removeMotorButton = new JButton(trans.get("MotorConfigurationPanel.btn.removeMotor"));
		removeMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMotor();
			}
		});
		this.add(removeMotorButton, "sizegroup button");

		//// Select Ignition button
		selectIgnitionButton = new JButton(trans.get("MotorConfigurationPanel.btn.selectIgnition"));
		selectIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectIgnition();
			}
		});
		this.add(selectIgnitionButton, "sizegroup button");

		//// Reset Ignition button
		resetIgnitionButton = new JButton(trans.get("MotorConfigurationPanel.btn.resetIgnition"));
		resetIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetIgnition();
			}
		});
		this.add(resetIgnitionButton, "sizegroup button, wrap");

		updateButtonState();

	}

	@Override
	protected JTable initializeTable() {
		//// Motor selection table.
		configurationTableModel = new FlightConfigurableTableModel<MotorMount>(MotorMount.class,rocket) {

			@Override
			protected boolean includeComponent(MotorMount component) {
				return component.isMotorMount();
			}

		};
		JTable configurationTable = new JTable(configurationTableModel);
		configurationTable.setCellSelectionEnabled(true);
		configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configurationTable.setDefaultRenderer(Object.class, new MotorTableCellRenderer());

		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateButtonState();
				int selectedColumn = table.getSelectedColumn();
				if (e.getClickCount() == 2) {
					if (selectedColumn > 0) {
						selectMotor();
					}
				}
			}
		});
		return configurationTable;
	}

	public void fireTableDataChanged() {
		int selected = table.getSelectedRow();
		configurationTableModel.fireTableDataChanged();
		if (selected >= 0) {
			selected = Math.min(selected, table.getRowCount() - 1);
			table.getSelectionModel().setSelectionInterval(selected, selected);
		}
		updateButtonState();
	}

	private void updateButtonState() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount currentMount = getSelectedComponent();
		selectMotorButton.setEnabled(currentMount != null && currentID != null);
		removeMotorButton.setEnabled(currentMount != null && currentID != null);
		selectIgnitionButton.setEnabled(currentMount != null && currentID != null);
		resetIgnitionButton.setEnabled(currentMount != null && currentID != null);
	}


	private void selectMotor() {
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = getSelectedComponent();
		if (id == null || mount == null)
			return;

		MotorConfiguration config = mount.getMotorConfiguration().get(id);

		MotorChooserDialog dialog = new MotorChooserDialog(
				mount,
				id,
				SwingUtilities.getWindowAncestor(flightConfigurationPanel));
		dialog.setVisible(true);
		Motor m = dialog.getSelectedMotor();
		double d = dialog.getSelectedDelay();

		if (m != null) {
			config = new MotorConfiguration();
			config.setMotor(m);
			config.setEjectionDelay(d);
			mount.getMotorConfiguration().set(id, config);
		}

		fireTableDataChanged();
	}

	private void removeMotor() {
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = getSelectedComponent();
		if (id == null || mount == null)
			return;

		mount.getMotorConfiguration().resetDefault(id);

		fireTableDataChanged();
	}

	private void selectIgnition() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount currentMount = getSelectedComponent();
		if (currentID == null || currentMount == null)
			return;

		IgnitionSelectionDialog dialog = new IgnitionSelectionDialog(
				SwingUtilities.getWindowAncestor(this.flightConfigurationPanel),
				rocket,
				currentMount);
		dialog.setVisible(true);

		fireTableDataChanged();
	}


	private void resetIgnition() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount currentMount = getSelectedComponent();
		if (currentID == null || currentMount == null)
			return;

		currentMount.getIgnitionConfiguration().resetDefault(currentID);

		fireTableDataChanged();
	}


	private class MotorTableCellRenderer implements TableCellRenderer {

		private void setSelected( JComponent c, JTable table, boolean isSelected, boolean hasFocus ) {
			c.setOpaque(true);
			if ( isSelected) {
				c.setBackground(table.getSelectionBackground());
				c.setForeground(table.getSelectionForeground());
			} else {
				c.setBackground(table.getBackground());
				c.setForeground(table.getForeground());
			}
			Border b = null;
			if ( hasFocus ) {
				if (isSelected) {
					b = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
				} else {
					b = UIManager.getBorder("Table.focusCellHighligtBorder");
				}
			} else {
				b = new EmptyBorder(1,1,1,1);
			}
			c.setBorder(b);
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus, int row,int column) {
			switch (column) {
			case 0: {
				JLabel label = new JLabel(descriptor.format(rocket, (String) value));
				setSelected(label, table, isSelected, hasFocus);
				return label;
			}
			default: {
				Pair<String, MotorMount> v = (Pair<String, MotorMount>) value;
				String id = v.getU();
				MotorMount component = v.getV();
				JLabel label = format(component, id);
				setSelected(label, table, isSelected, hasFocus);
				return label;
			}
			}
		}

		protected JLabel format(MotorMount mount, String configId) {
			JLabel label = new JLabel();
			label.setLayout(new BoxLayout(label, BoxLayout.X_AXIS));
			MotorConfiguration motorConfig = mount.getMotorConfiguration().get(configId);
			String motorString = getMotorSpecification(mount, motorConfig);
			JLabel motorDescriptionLabel = new JLabel(motorString);
			label.add(motorDescriptionLabel);
			label.add( Box.createRigidArea(new Dimension(10,0)));
			JLabel ignitionLabel = getIgnitionEventString(configId, mount);
			label.add(ignitionLabel);
			label.validate();
			return label;
//			label.setText(motorString + " " + ignitionString);
		}

		private String getMotorSpecification(MotorMount mount, MotorConfiguration motorConfig) {
			Motor motor = motorConfig.getMotor();

			if (motor == null)
				return NONE;

			String str = motor.getDesignation(motorConfig.getEjectionDelay());
			int count = getMountMultiplicity(mount);
			if (count > 1) {
				str = "" + count + Chars.TIMES + " " + str;
			}
			return str;
		}

		private int getMountMultiplicity(MotorMount mount) {
			RocketComponent c = (RocketComponent) mount;
			return c.toAbsolute(Coordinate.NUL).length;
		}

		protected final void shaded(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.ITALIC);
			label.setForeground(Color.GRAY);
		}


		private JLabel getIgnitionEventString(String id, MotorMount mount) {
			IgnitionConfiguration ignitionConfig = mount.getIgnitionConfiguration().get(id);
			IgnitionConfiguration.IgnitionEvent ignitionEvent = ignitionConfig.getIgnitionEvent();

			Double ignitionDelay = ignitionConfig.getIgnitionDelay();
			boolean isDefault = mount.getIgnitionConfiguration().isDefault(id);

			JLabel label = new JLabel();
			String str = trans.get("MotorMount.IgnitionEvent.short." + ignitionEvent.name());
			if (ignitionEvent != IgnitionEvent.NEVER && ignitionDelay > 0.001) {
				str = str + " + " + UnitGroup.UNITS_SHORT_TIME.toStringUnit(ignitionDelay);
			}
			if (isDefault) {
				shaded(label);
				String def = trans.get("MotorConfigurationTableModel.table.ignition.default");
				str = def.replace("{0}", str);
			}
			label.setText(str);
			return label;
		}

	}

}
