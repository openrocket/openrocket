package info.openrocket.swing.gui.widgets;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.UnitValue;
import info.openrocket.swing.gui.components.CsvOptionPanel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class CSVExportPanel<T extends UnitValue> extends JPanel {
	private static final long serialVersionUID = 1L;
	protected static final Translator trans = Application.getTranslator();
	protected static final String SPACE = "SPACE";
	protected static final String TAB = "TAB";
	private static Color ALTERNATE_ROW_COLOR;

	protected final List<DataTypeRow> dataTypeRows = new ArrayList<>();
	protected final CsvOptionPanel csvOptions;
	protected final JLabel selectedCountLabel;

	protected final T[] types;
	protected boolean[] selected;
	protected Unit[] units;

	static {
		initColors();
	}

	public CSVExportPanel(T[] types, boolean[] selected, CsvOptionPanel csvOptions, boolean separateRowForOptions,
						  Component... extraComponents) {
		super(new MigLayout("fill, wrap 2", "[grow,fill][]", "[grow,fill][]"));

		this.types = types;
		this.selected = selected;
		this.csvOptions = csvOptions;
		this.units = new Unit[types.length];

		for (int i = 0; i < types.length; i++) {
			units[i] = types[i].getUnitGroup().getDefaultUnit();
		}

		JPanel exportPanel = new JPanel(new MigLayout("ins 0, fill"));

		boolean addExtras = createExtraComponent(types[0], 0) != null;
		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);		// No padding
		c.fill = GridBagConstraints.BOTH;

		// Header
		c.gridy = 0;
		c.weightx = 0;
		addHeaderRowLabel(contentPanel, "CSVExportPanel.lbl.Export", c, 0);
		c.weightx = 1;
		addHeaderRowLabel(contentPanel, "CSVExportPanel.lbl.Variable", c, 1);
		c.weightx = 0;
		addHeaderRowLabel(contentPanel, "CSVExportPanel.lbl.Unit", c, 2);
		if (addExtras) {
			c.weightx = 1;
			addHeaderRowLabel(contentPanel, getExtraColumnLabelKey(), c, 3);
		}

		// Data rows
		for (int i = 0; i < types.length; i++) {
			DataTypeRow row = new DataTypeRow(types[i], selected[i], units[i], i);
			dataTypeRows.add(row);

			Color bgColor = i % 2 == 0 ? UIManager.getColor("Table.background") : ALTERNATE_ROW_COLOR;

			c.gridy = i + 1;

			addDataRowWidget(contentPanel, row.exportCheckBox, c, bgColor, 0);
			addDataRowWidget(contentPanel, row.nameLabel, c, bgColor, 1);
			addDataRowWidget(contentPanel, row.unitSelector, c, bgColor, 2);
			if (addExtras) {
				addDataRowWidget(contentPanel, createExtraComponent(types[i], i), c, bgColor, 3);
			}
		}

		JScrollPane scrollPane = new JScrollPane(contentPanel);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setPreferredSize(new Dimension(300, 400));
		exportPanel.add(scrollPane, "grow, wrap");

		// Select all/none buttons
		JPanel buttonPanel = new JPanel(new MigLayout("insets 0"));
		JButton selectAllButton = new JButton(trans.get("CSVExportPanel.but.Selectall"));
		selectAllButton.addActionListener(e -> selectAll());
		buttonPanel.add(selectAllButton, "split 2, growx");

		JButton selectNoneButton = new JButton(trans.get("CSVExportPanel.but.Selectnone"));
		selectNoneButton.addActionListener(e -> selectNone());
		buttonPanel.add(selectNoneButton, "growx");

		exportPanel.add(buttonPanel, "growx, wrap");

		// Selected count label
		selectedCountLabel = new JLabel();
		updateSelectedCount();
		exportPanel.add(selectedCountLabel, "growx");
		add(exportPanel, "grow, gapright para" + (separateRowForOptions ? ", spanx, wrap" : ""));

		// CSV options and extra components
		if (separateRowForOptions) {
			add(csvOptions, "growx");
			for (Component comp : extraComponents) {
				add(comp, "growx, top");
			}
		} else {
			JPanel optionsPanel = new JPanel(new MigLayout("insets 0"));
			optionsPanel.add(csvOptions, "growx, wrap");
			for (Component comp : extraComponents) {
				optionsPanel.add(comp, "growx, wrap");
			}
			add(optionsPanel, "growx, top");
		}
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(CSVExportPanel::updateColors);
	}

	public static void updateColors() {
		ALTERNATE_ROW_COLOR = GUIUtil.getUITheme().getRowBackgroundLighterColor();
	}

	private void addHeaderRowLabel(JPanel contentPanel, String lblKey, GridBagConstraints c, int x) {
		c.gridx = x;

		JPanel panel = new JPanel(new MigLayout("fill, ins 4 5 4 5"));
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Table.gridColor")));
		panel.add(new JLabel("<html><b>" + trans.get(lblKey) + "</b></html>"), "growx");
		contentPanel.add(panel, c);
	}

	private void addDataRowWidget(JPanel contentPanel, Component widget, GridBagConstraints c, Color bgColor, int x) {
		c.gridx = x;

		JPanel panel = new JPanel(new MigLayout("fill, ins 4 5 4 5"));
		panel.setBackground(bgColor);
		if (widget instanceof JPanel) {
			widget.setBackground(bgColor);
		}
		panel.add(widget, "growx");
		contentPanel.add(panel, c);
	}

	protected String getExtraColumnLabelKey() {
		return "CSVExportPanel.lbl.Extra";
	}

	protected Component createExtraComponent(T type, int index) {
		return null;
	}

	protected class DataTypeRow {
		final JCheckBox exportCheckBox;
		final JLabel nameLabel;
		final UnitSelector unitSelector;
		final int index;

		DataTypeRow(T type, boolean isSelected, Unit unit, int index) {
			this.index = index;
			exportCheckBox = new JCheckBox();
			exportCheckBox.setSelected(isSelected);
			exportCheckBox.addActionListener(e -> {
				selected[index] = exportCheckBox.isSelected();
				updateSelectedCount();
			});

			nameLabel = new JLabel(getDisplayName(type));

			unitSelector = new UnitSelector(type.getUnitGroup());
			unitSelector.setSelectedUnit(unit);
			unitSelector.addItemListener(e -> units[index] = unitSelector.getSelectedUnit());
		}
	}

	protected String getDisplayName(T type) {
		return type.toString();
	}

	protected void selectAll() {
		for (DataTypeRow row : dataTypeRows) {
			row.exportCheckBox.setSelected(true);
			selected[row.index] = true;
		}
		updateSelectedCount();
	}

	protected void selectNone() {
		for (DataTypeRow row : dataTypeRows) {
			row.exportCheckBox.setSelected(false);
			selected[row.index] = false;
		}
		updateSelectedCount();
	}

	protected void updateSelectedCount() {
		int total = selected.length;
		int n = 0;
		for (boolean b : selected) {
			if (b) n++;
		}

		String str;
		if (n == 1) {
			str = trans.get("CSVExportPanel.ExportingVar.desc1") + " " + total + ".";
		} else {
			str = trans.get("CSVExportPanel.ExportingVar.desc2") + " " + n + " " +
					trans.get("CSVExportPanel.ExportingVar.desc3") + " " + total + ".";
		}

		selectedCountLabel.setText(str);
	}

	public boolean doExport() {
		throw new UnsupportedOperationException("Export not implemented in base class");
	}
}