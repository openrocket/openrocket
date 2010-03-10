package net.sf.openrocket.gui.dialogs;


import java.awt.Dialog;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.unit.ValueComparator;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Prefs;

public class MotorChooserDialog extends JDialog {
	
	private static final int SHOW_ALL = 0;
	private static final int SHOW_SMALLER = 1;
	private static final int SHOW_EXACT = 2;
	private static final String[] SHOW_DESCRIPTIONS = {
		"Show all motors",
		"Show motors with diameter less than that of the motor mount",
		"Show motors with diameter equal to that of the motor mount"
	};
	private static final int SHOW_MAX = 2;

	private final JTextField searchField; 
	private String[] searchTerms = new String[0];

	private final double diameter;

	private Motor selectedMotor = null;
	private double selectedDelay = 0;

	private JTable table;
	private TableRowSorter<TableModel> sorter;
	private JComboBox delayBox;
	private MotorDatabaseModel model;
	
	private boolean okClicked = false;

	
	public MotorChooserDialog(double diameter) {
		this(null,5,diameter,null);
	}
	
	public MotorChooserDialog(Motor current, double delay, double diameter) {
		this(current,delay,diameter,null);
	}
	
	public MotorChooserDialog(Motor current, double delay, double diameter, Window owner) {
		super(owner, "Select a rocket motor", Dialog.ModalityType.APPLICATION_MODAL);
		
		JButton button;

		this.selectedMotor = current;
		this.selectedDelay = delay;
		this.diameter = diameter;
		
		JPanel panel = new JPanel(new MigLayout("fill", "[grow][]"));

		// Label
		JLabel label = new JLabel("Select a rocket motor:");
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		panel.add(label,"growx");
		
		label = new JLabel("Motor mount diameter: " +
				UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(diameter));
		panel.add(label,"gapleft para, wrap paragraph");
		
		
		// Diameter selection
		JComboBox combo = new JComboBox(SHOW_DESCRIPTIONS);
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				int sel = cb.getSelectedIndex();
				if ((sel < 0) || (sel > SHOW_MAX))
					sel = SHOW_ALL;
				switch (sel) {
				case SHOW_ALL:
					sorter.setRowFilter(new MotorRowFilterAll());
					break;
					
				case SHOW_SMALLER:
					sorter.setRowFilter(new MotorRowFilterSmaller());
					break;
					
				case SHOW_EXACT:
					sorter.setRowFilter(new MotorRowFilterExact());
					break;
					
				default:
					assert(false) : "Should not occur.";	
				}
				Prefs.putChoise("MotorDiameterMatch", sel);
				setSelectionVisible();
			}
		});
		panel.add(combo,"growx 1000");

		
		
		label = new JLabel("Search:");
		panel.add(label, "gapleft para, split 2");
		
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
				ArrayList<String> list = new ArrayList<String>();
				for (String s: split) {
					s = s.trim().toLowerCase();
					if (s.length() > 0) {
						list.add(s);
					}
				}
				searchTerms = list.toArray(new String[0]);
				sorter.sort();
			}
		});
		panel.add(searchField, "growx 1, wrap");
		
		
		
		// Table, overridden to show meaningful tooltip texts
		model = new MotorDatabaseModel(current);
		table = new JTable(model) {
			@Override
			public String getToolTipText(MouseEvent e) {
		        java.awt.Point p = e.getPoint();
		        int colIndex = columnAtPoint(p);
		        int viewRow = rowAtPoint(p);
		        if (viewRow < 0)
		        	return null;
		        int rowIndex = convertRowIndexToModel(viewRow);
		        Motor motor = model.getMotor(rowIndex);

		        if (colIndex < 0 || colIndex >= MotorColumns.values().length)
		        	return null;

		        return MotorColumns.values()[colIndex].getToolTipText(motor);
			}
		};
		
		// Set comparators and widths
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sorter = new TableRowSorter<TableModel>(model);
		for (int i=0; i < MotorColumns.values().length; i++) {
			MotorColumns column = MotorColumns.values()[i];
			sorter.setComparator(i, column.getComparator());
			table.getColumnModel().getColumn(i).setPreferredWidth(column.getWidth());
		}
		table.setRowSorter(sorter);

		// Set selection and double-click listeners
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = table.getSelectedRow();
				if (row >= 0) {
					row = table.convertRowIndexToModel(row);
					Motor m = model.getMotor(row);
					// TODO: HIGH: equals or == ?
					if (!m.equals(selectedMotor)) {
						selectedMotor = model.getMotor(row);
						setDelays(true);  // Reset delay times
					}
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					okClicked = true;
					MotorChooserDialog.this.setVisible(false);
				}
			}
		});
		// (Current selection and scrolling performed later)
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(table);
		panel.add(scrollpane,"spanx, grow, width :700:, height :300:, wrap paragraph");
		
		
		// Ejection delay
		panel.add(new JLabel("Select ejection charge delay:"), "spanx, split 3, gap rel");
		
		delayBox = new JComboBox();
		delayBox.setEditable(true);
		delayBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				String sel = (String)cb.getSelectedItem();
				if (sel.equalsIgnoreCase("None")) {
					selectedDelay = Motor.PLUGGED;
				} else {
					try {
						selectedDelay = Double.parseDouble(sel);
					} catch (NumberFormatException ignore) { }
				}
				setDelays(false);
			}
		});
		panel.add(delayBox,"gapright unrel");
		panel.add(new StyledLabel("(Number of seconds or \"None\")", -1), "wrap para");
		setDelays(false);
		
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				okClicked = true;
				MotorChooserDialog.this.setVisible(false);
			}
		});
		panel.add(okButton,"spanx, split, tag ok");

		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MotorChooserDialog.this.setVisible(false);
			}
		});
		panel.add(button,"tag cancel");

				
		// Sets the filter:
		int showMode = Prefs.getChoise("MotorDiameterMatch", SHOW_MAX, SHOW_EXACT);
		combo.setSelectedIndex(showMode);
		
		
		this.add(panel);
		this.pack();
//		this.setAlwaysOnTop(true);

		this.setLocationByPlatform(true);
		GUIUtil.setDisposableDialogOptions(this, okButton);
		
		// Table can be scrolled only after pack() has been called
		setSelectionVisible();
		
		// Focus the search field
		searchField.grabFocus();
	}
	
	private void setSelectionVisible() {
		if (selectedMotor != null) {
			int index = table.convertRowIndexToView(model.getIndex(selectedMotor));
			table.getSelectionModel().setSelectionInterval(index, index);
			Rectangle rect = table.getCellRect(index, 0, true);
			rect = new Rectangle(rect.x,rect.y-100,rect.width,rect.height+200);
			table.scrollRectToVisible(rect);
		}
	}
	
	
	/**
	 * Set the values in the delay combo box.  If <code>reset</code> is <code>true</code>
	 * then sets the selected value as the value closest to selectedDelay, otherwise
	 * leaves selection alone.
	 */
	private void setDelays(boolean reset) {
		if (selectedMotor == null) {
			
			delayBox.setModel(new DefaultComboBoxModel(new String[] { "None" }));
			delayBox.setSelectedIndex(0);
			
		} else {
			
			double[] delays = selectedMotor.getStandardDelays();
			String[] delayStrings = new String[delays.length];
			double currentDelay = selectedDelay;  // Store current setting locally
			
			for (int i=0; i < delays.length; i++) {
				delayStrings[i] = Motor.getDelayString(delays[i], "None");
			}
			delayBox.setModel(new DefaultComboBoxModel(delayStrings));
			
			if (reset) {

				// Find and set the closest value
				double closest = Double.NaN;
				for (int i=0; i < delays.length; i++) {
					// if-condition to always become true for NaN
					if (!(Math.abs(delays[i] - currentDelay) > 
						  Math.abs(closest - currentDelay))) {
						closest = delays[i];
					}
				}
				if (!Double.isNaN(closest)) {
					selectedDelay = closest;
					delayBox.setSelectedItem(Motor.getDelayString(closest, "None"));
				} else {
					delayBox.setSelectedItem("None");
				}

			} else {
				
				selectedDelay = currentDelay;
				delayBox.setSelectedItem(Motor.getDelayString(currentDelay, "None"));
				
			}
			
		}
	}

	
	
	public Motor getSelectedMotor() {
		if (!okClicked)
			return null;
		return selectedMotor;
	}
	
	
	public double getSelectedDelay() {
		return selectedDelay;
	}
	

	
	
	////////////////  JTable elements  ////////////////
	
	
	/**
	 * Enum defining the table columns.
	 */
	private enum MotorColumns {
		MANUFACTURER("Manufacturer",100) {
			@Override
			public String getValue(Motor m) {
				return m.getManufacturer().getDisplayName();
			}
			@Override
			public Comparator<?> getComparator() {
				return Collator.getInstance();
			}
		},
		DESIGNATION("Designation") {
			@Override
			public String getValue(Motor m) {
				return m.getDesignation();
			}
			@Override
			public Comparator<?> getComparator() {
				return Motor.getDesignationComparator();
			}
		},
		TYPE("Type") {
			@Override
			public String getValue(Motor m) {
				return m.getMotorType().getName();
			}
			@Override
			public Comparator<?> getComparator() {
				return Collator.getInstance();
			}
		},
		DIAMETER("Diameter") {
			@Override
			public Object getValue(Motor m) {
				return new Value(m.getDiameter(), UnitGroup.UNITS_MOTOR_DIMENSIONS);
			}
			@Override
			public Comparator<?> getComparator() {
				return ValueComparator.INSTANCE;
			}
		},
		LENGTH("Length") {
			@Override
			public Object getValue(Motor m) {
				return new Value(m.getLength(), UnitGroup.UNITS_MOTOR_DIMENSIONS);
			}
			@Override
			public Comparator<?> getComparator() {
				return ValueComparator.INSTANCE;
			}
		},
		IMPULSE("Impulse") {
			@Override
			public Object getValue(Motor m) {
				return new Value(m.getTotalImpulse(), UnitGroup.UNITS_IMPULSE);
			}
			@Override
			public Comparator<?> getComparator() {
				return ValueComparator.INSTANCE;
			}
		},
		TIME("Burn time") {
			@Override
			public Object getValue(Motor m) {
				return new Value(m.getAverageTime(), UnitGroup.UNITS_SHORT_TIME);
			}
			@Override
			public Comparator<?> getComparator() {
				return ValueComparator.INSTANCE;
			}
		};
		
		
		private final String title;
		private final int width;
		
		MotorColumns(String title) {
			this(title, 50);
		}
		
		MotorColumns(String title, int width) {
			this.title = title;
			this.width = width;
		}
		
		
		public abstract Object getValue(Motor m);
		public abstract Comparator<?> getComparator();

		public String getTitle() {
			return title;
		}
		
		public int getWidth() {
			return width;
		}
		
		public String getToolTipText(Motor m) {
			String tip = "<html>";
			tip += "<b>" + m.toString() + "</b>";
			tip += " (" + m.getMotorType().getDescription() + ")<br><hr>";
			
			String desc = m.getDescription().trim();
			if (desc.length() > 0) {
				tip += "<i>" + desc.replace("\n", "<br>") + "</i><br><hr>";
			}
			
			tip += ("Diameter: " + 
					UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(m.getDiameter()) +
					"<br>");
			tip += ("Length: " + 
					UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(m.getLength()) +
					"<br>");
			tip += ("Maximum thrust: " + 
					UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(m.getMaxThrust()) +
					"<br>");
			tip += ("Average thrust: " + 
					UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(m.getAverageThrust()) +
					"<br>");
			tip += ("Burn time: " + 
					UnitGroup.UNITS_SHORT_TIME.getDefaultUnit()
					.toStringUnit(m.getAverageTime()) + "<br>");
			tip += ("Total impulse: " +
					UnitGroup.UNITS_IMPULSE.getDefaultUnit()
					.toStringUnit(m.getTotalImpulse()) + "<br>");
			tip += ("Launch mass: " + 
					UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(m.getMass(0)) +
					"<br>");
			tip += ("Empty mass: " + 
					UnitGroup.UNITS_MASS.getDefaultUnit()
					.toStringUnit(m.getMass(Double.MAX_VALUE)));
			return tip;
		}
		
	}
	
	
	/**
	 * The JTable model.  Includes an extra motor, given in the constructor,
	 * if it is not already in the database.
	 */
	private class MotorDatabaseModel extends AbstractTableModel {
		private final Motor extra;
		
		public MotorDatabaseModel(Motor current) {
			if (Databases.MOTOR.contains(current))
				extra = null;
			else
				extra = current;
		}
		
		@Override
		public int getColumnCount() {
			return MotorColumns.values().length;
		}

		@Override
		public int getRowCount() {
			if (extra == null)
				return Databases.MOTOR.size();
			else
				return Databases.MOTOR.size()+1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			MotorColumns column = getColumn(columnIndex);
			if (extra == null) {
				return column.getValue(Databases.MOTOR.get(rowIndex));
			} else {
				if (rowIndex == 0)
					return column.getValue(extra);
				else
					return column.getValue(Databases.MOTOR.get(rowIndex - 1));
			}
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return getColumn(columnIndex).getTitle();
		}
		
		
		public Motor getMotor(int rowIndex) {
			if (extra == null) {
				return Databases.MOTOR.get(rowIndex);
			} else {
				if (rowIndex == 0)
					return extra;
				else
					return Databases.MOTOR.get(rowIndex-1);
			}
		}
		
		public int getIndex(Motor m) {
			if (extra == null) {
				return Databases.MOTOR.indexOf(m);
			} else {
				if (extra.equals(m))
					return 0;
				else
					return Databases.MOTOR.indexOf(m)+1;
			}
		}
		
		private MotorColumns getColumn(int index) {
			return MotorColumns.values()[index];
		}
	}

	
	////////  Row filters
	
	/**
	 * Abstract adapter class.
	 */
	private abstract class MotorRowFilter extends RowFilter<TableModel,Integer> {
		@Override
		public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			int index = entry.getIdentifier();
			Motor m = model.getMotor(index);
			return filterByDiameter(m) && filterByString(m);
		}
		
		public abstract boolean filterByDiameter(Motor m);
		
		
		public boolean filterByString(Motor m) {
			main: for (String s : searchTerms) {
				for (MotorColumns col : MotorColumns.values()) {
					String str = col.getValue(m).toString().toLowerCase();
					if (str.indexOf(s) >= 0)
						continue main;
				}
				return false;
			}
			return true;
		}
	}
	
	/**
	 * Show all motors.
	 */
	private class MotorRowFilterAll extends MotorRowFilter {
		@Override
		public boolean filterByDiameter(Motor m) {
			return true;
		}
	}
	
	/**
	 * Show motors smaller than the mount.
	 */
	private class MotorRowFilterSmaller extends MotorRowFilter {
		@Override
		public boolean filterByDiameter(Motor m) {
			return (m.getDiameter() <= diameter + 0.0004);
		}
	}
	
	/**
	 * Show motors that fit the mount.
	 */
	private class MotorRowFilterExact extends MotorRowFilter {
		@Override
		public boolean filterByDiameter(Motor m) {
			return ((m.getDiameter() <= diameter + 0.0004) &&
					(m.getDiameter() >= diameter - 0.0015));
		}
	}
	
}
