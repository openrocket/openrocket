package info.openrocket.swing.gui.dialogs.preset;


import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Chars;
import net.miginfocom.swing.MigLayout;

import info.openrocket.swing.gui.adaptors.PresetModel;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.util.TableUIPreferences;
import info.openrocket.swing.gui.widgets.SelectColorButton;
import info.openrocket.swing.utils.TableRowTraversalPolicy;

/**
 * Dialog shown for selecting a preset component.
 */
@SuppressWarnings("serial")
public class ComponentPresetChooserDialog extends JDialog {
	private static final String TABLE_ID = "CmpPrst.";
	
	private static final Translator trans = Application.getTranslator();
	
	private final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
	
	private final RocketComponent component;
	
	private final ComponentPresetTable componentSelectionTable;
	private final JTextField filterText;
	private JCheckBox foreDiameterFilterCheckBox;
	private JCheckBox aftDiameterFilterCheckBox;
	private JCheckBox showLegacyCheckBox;

	private ComponentPresetRowFilter legacyFilter;
	private ComponentPresetRowFilter foreDiameterFilter;
	private ComponentPresetRowFilter aftDiameterFilter;
	
	
	/*
	 * outerDiamtereColumnIndex is the index of the column associated with the OUTER_DIAMETER
	 * field.  This index is needed by the matchOuterDiameterCheckBox to implement filtering.
	 */
	int legacyColumnIndex = -1;	
	int aftDiameterColumnIndex = -1;
	int foreDiameterColumnIndex = -1;
	
	private List<ComponentPreset> presets;
	private ComponentPreset.Type presetType;
	private PresetModel presetModel;
	
	
	public ComponentPresetChooserDialog(Window owner, RocketComponent component, PresetModel presetModel) {
		super(owner, trans.get("title"), Dialog.ModalityType.APPLICATION_MODAL);
		this.component = component;
		this.presetType = component.getPresetType();
		this.presetModel = presetModel;
		this.presets = Application.getComponentPresetDao().listForType(component.getPresetType());

		if (owner.getParent() != null) {
			this.setPreferredSize(new Dimension((int)(0.7 * owner.getParent().getWidth()), (int) (0.7 * owner.getParent().getHeight())));
			this.setLocationRelativeTo(owner.getParent());
		}
		List<TypedKey<?>> displayedColumnKeys = Arrays.asList(component.getPresetType().getDisplayedColumns());
		
		{
			final List<TypedKey<?>> columnKeys = ComponentPreset.ORDERED_KEY_LIST;
			int i = 0; // We start at 0 but use preincrement because the first column is favorite.
			for (final TypedKey<?> key : columnKeys) {
				// Note the increment early in the loop.  This really means that initial loop i=1
				// we do it here so the continue below doesn't mess up the counting.
				i++;
				// Don't allow the matching filters if the column is not part of the default set for
				// this kind of preset.
				if (!displayedColumnKeys.contains(key)) {
					continue;
				}
				if (key == ComponentPreset.LEGACY) {
					legacyColumnIndex = i;
				}
				if (key == ComponentPreset.OUTER_DIAMETER || key == ComponentPreset.AFT_OUTER_DIAMETER) {
					aftDiameterColumnIndex = i;
				}
				if (key == ComponentPreset.OUTER_DIAMETER || key == ComponentPreset.FORE_OUTER_DIAMETER) {
					foreDiameterColumnIndex = i;
				}
			}
		}
		
		
		JPanel panel = new JPanel(new MigLayout("fill, ins para"));
		
		/*
		 * Add filter by text.
		 */
		JPanel sub = new JPanel(new MigLayout("fill, ins 0"));
		JLabel filterLabel = new JLabel(trans.get("ComponentPresetChooserDialog.filter.label"));
		sub.add(filterLabel, "gapright para");
		
		filterText = new JTextField();
		sub.add(filterText, "width 50:320, pushx, growx");
		filterText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateFilters();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateFilters();
			}
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateFilters();
			}
		});
		
		panel.add(sub, "growx, pushx, ay 0, gapright para");

		// need to create componentSelectionTable before filter checkboxes,
		// but add to panel after
		componentSelectionTable = new ComponentPresetTable(presetType, presets, displayedColumnKeys);

		// Make the first column (the favorite column) as small as possible
		int w = componentSelectionTable.getRowHeight() + 4;
		XTableColumnModel tm = componentSelectionTable.getXColumnModel();
		TableColumn tc = tm.getColumn(0);
		tc.setPreferredWidth(w);
		tc.setMaxWidth(w);
		tc.setMinWidth(w);

		// The normal left/right and tab/shift-tab key action traverses each cell/column of the table instead of going to the next row.
		TableRowTraversalPolicy.setTableRowTraversalPolicy(componentSelectionTable);

		// Add checkboxes (legacy, match fore diameter, ...)
		panel.add(getFilterCheckboxes(tm, legacyColumnIndex), "wrap para");

		// Load the table UI settings from the preferences
		boolean legacySelected = showLegacyCheckBox.isSelected();
		TableUIPreferences.loadTableUIPreferences(componentSelectionTable, TABLE_ID + component.getComponentName(),
				preferences.getTablePreferences());
		showLegacyCheckBox.setSelected(legacySelected);		// Restore legacy state (may change during UI preference loading)

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(componentSelectionTable);
		panel.add(scrollpane, "grow, pushy, spanx, wrap rel");
		
		panel.add(new StyledLabel(String.format("<html>%s %s</html>", Chars.UP_ARROW, trans.get("lbl.favorites")), -1), "spanx, gapleft 5px, wrap para");

		// When double-clicking a preset row, apply the preset and close this dialog
		componentSelectionTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Don't do anything when double-clicking the first column
				if (e.getClickCount() == 2 && componentSelectionTable.getSelectedColumn() > 0 && applySelectedPreset()) {
					ComponentPresetChooserDialog.this.setVisible(false);
				}
			}
		});

		// Always open this window when creating a new component
		JCheckBox alwaysOpenPreset = new JCheckBox(String.format(trans.get("ComponentPresetChooserDialog.checkbox.alwaysOpenPreset"),
				component.getComponentName()));
		alwaysOpenPreset.setSelected(preferences.getBoolean(component.getComponentName() +  "AlwaysOpenPreset", true));
		alwaysOpenPreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				preferences.putBoolean(component.getComponentName() + "AlwaysOpenPreset", ((JCheckBox) e.getSource()).isSelected());
			}
		});
		panel.add(alwaysOpenPreset, "spanx 2");
		
		// Close buttons
		JButton closeButton = new SelectColorButton(trans.get("dlg.but.close"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TableUIPreferences.storeTableUIPreferences(componentSelectionTable, TABLE_ID + component.getComponentName(),
						preferences.getTablePreferences());
				ComponentPresetChooserDialog.this.setVisible(false);
				applySelectedPreset();
			}
		});
		panel.add(closeButton, "spanx, right, tag close");
		
		this.add(panel);

		GUIUtil.setDisposableDialogOptions(this, closeButton);
		GUIUtil.rememberWindowSize(this);
		this.setLocationByPlatform(true);
		GUIUtil.rememberWindowPosition(this);

		updateFilters();
	}

	/**
	 * Applies the currently selected preset to presetModel.
	 *
	 * @return true if the preset was applied, false if otherwise.
	 */
	private boolean applySelectedPreset() {
		if (presetModel == null) return false;
		ComponentPreset preset = getSelectedComponentPreset();
		if (preset != null) {
			presetModel.setSelectedItem(preset);
			return true;
		}
		return false;
	}
	
	
	private JPanel getFilterCheckboxes(XTableColumnModel tm, int legacyColumnIndex) {
		JPanel panel = new JPanel(new MigLayout("ins 0"));

		/*
		 * Add legacy component filter checkbox
		 */
		TableColumn legacyColumn = tm.getColumn(legacyColumnIndex);
		tm.setColumnVisible(legacyColumn, false);		
		legacyFilter = new ComponentPresetRowFilter(false, legacyColumnIndex);
		showLegacyCheckBox = new JCheckBox();
		showLegacyCheckBox.setText(trans.get("ComponentPresetChooserDialog.checkbox.showLegacyCheckBox"));
		panel.add(showLegacyCheckBox, "wrap");
		
		showLegacyCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e != null && e.getStateChange() == ItemEvent.SELECTED;
				if (tm.isColumnVisible(legacyColumn) == selected) {
					// No change
					return;
				}

				updateFilters();

				tm.setColumnVisible(legacyColumn, selected);

				if (selected) {
					// Let's say the optimal width is 100 (you can adjust this as needed)
					int optimalWidth = 50;
					legacyColumn.setPreferredWidth(optimalWidth);
				}
			}
		});

		// When the legacy column changes visibility (by right-clicking on the column header and toggling the legacy header checkbox),
		// update the main legacy checkbox
		tm.addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				TableColumn column = tm.getColumn(e.getToIndex());
				if (column == legacyColumn) {
					showLegacyCheckBox.setSelected(true);
				}
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				// Use 'getFromIndex' since the column has been removed
				if (e.getFromIndex() == legacyColumnIndex) {
					showLegacyCheckBox.setSelected(false);
				}
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {}

			@Override
			public void columnMarginChanged(ChangeEvent e) {}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {}
		});
		
		if(component instanceof SymmetricComponent) {
			final SymmetricComponent curSym = (SymmetricComponent) component;
			/*
			 * Add filter by fore diameter
			 */
			foreDiameterFilterCheckBox = new JCheckBox(trans.get("ComponentPresetChooserDialog.checkbox.filterForeDiameter"));
			foreDiameterFilterCheckBox.setToolTipText(trans.get("ComponentPresetChooserDialog.checkbox.filterForeDiameter.ttip"));
			final SymmetricComponent prevSym = curSym.getPreviousSymmetricComponent();
			if (prevSym != null && foreDiameterColumnIndex >= 0) {
				foreDiameterFilterCheckBox.setSelected(preferences.isMatchForeDiameter());
				foreDiameterFilter = new ComponentPresetRowFilter(prevSym.getAftRadius() * 2.0, foreDiameterColumnIndex);
				panel.add(foreDiameterFilterCheckBox, "wrap");
				foreDiameterFilterCheckBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						updateFilters();
						preferences.setMatchForeDiameter(foreDiameterFilterCheckBox.isSelected());
					}
				});
			}

			/*
			 * Add filter by aft diameter
			 */
			final SymmetricComponent nextSym;
			if (curSym instanceof NoseCone && ((NoseCone) curSym).isFlipped()) {
				aftDiameterFilterCheckBox = new JCheckBox(trans.get("ComponentPresetChooserDialog.checkbox.filterForeDiameter"));
				aftDiameterFilterCheckBox.setToolTipText(trans.get("ComponentPresetChooserDialog.checkbox.filterForeDiameter.ttip"));
				nextSym = curSym.getPreviousSymmetricComponent();
			} else {
				aftDiameterFilterCheckBox = new JCheckBox(trans.get("ComponentPresetChooserDialog.checkbox.filterAftDiameter"));
				aftDiameterFilterCheckBox.setToolTipText(trans.get("ComponentPresetChooserDialog.checkbox.filterAftDiameter.ttip"));
				nextSym = curSym.getNextSymmetricComponent();
			}
			if (nextSym != null && aftDiameterColumnIndex >= 0) {
				aftDiameterFilterCheckBox.setSelected(preferences.isMatchAftDiameter());
				aftDiameterFilter = new ComponentPresetRowFilter(nextSym.getForeRadius() * 2.0, aftDiameterColumnIndex);
				panel.add(aftDiameterFilterCheckBox, "wrap");
				aftDiameterFilterCheckBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						updateFilters();
						preferences.setMatchAftDiameter(aftDiameterFilterCheckBox.isSelected());
					}
				});
			}
		}
		
		return panel;
	}
	
	/**
	 * Return the motor selected by this chooser dialog, or <code>null</code> if the selection has been aborted.
	 * 
	 * @return	the selected motor, or <code>null</code> if no motor has been selected or the selection was canceled.
	 */
	public ComponentPreset getSelectedComponentPreset() {
		int row = componentSelectionTable.getSelectedRow();
		if (row < 0) {
			// Nothing selected.
			return null;
		}
		row = componentSelectionTable.convertRowIndexToModel(row);
		return presets.get(row);
	}
	
	private void updateFilters() {
		List<RowFilter<TableModel, Object>> filters = new ArrayList<RowFilter<TableModel, Object>>(2);
		String filterTextRegex = filterText.getText();
		if (filterTextRegex != null) {
			try {
				// The "(?iu)" magic turns on case insensitivity with unicode chars
				RowFilter<TableModel, Object> regexFilter = RowFilter.regexFilter("(?iu)" + filterTextRegex);
				filters.add(regexFilter);
			} catch (java.util.regex.PatternSyntaxException e) {
			}
		}
		if ((null != aftDiameterFilterCheckBox) && aftDiameterFilterCheckBox.isSelected()) {
			filters.add(aftDiameterFilter);
		}
		if ((null != foreDiameterFilterCheckBox) && foreDiameterFilterCheckBox.isSelected()) {
			filters.add(foreDiameterFilter);
		}
		if (!showLegacyCheckBox.isSelected()) {
			filters.add(legacyFilter);
		}
		
		componentSelectionTable.setRowFilter(RowFilter.andFilter(filters));
	}
}
