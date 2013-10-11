package net.sf.openrocket.gui.dialogs.preset;


import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Chars;

/**
 * Dialog shown for selecting a preset component.
 */
public class ComponentPresetChooserDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private final RocketComponent component;
	
	private ComponentPresetTable componentSelectionTable;
	private JTextField filterText;
	private JCheckBox foreDiameterFilterCheckBox;
	private JCheckBox aftDiameterFilterCheckBox;
	
	private ComponentPresetRowFilter foreDiameterFilter;
	private ComponentPresetRowFilter aftDiameterFilter;
	
	
	/*
	 * outerDiamtereColumnIndex is the index of the column associated with the OUTER_DIAMETER
	 * field.  This index is needed by the matchOuterDiameterCheckBox to implement filtering.
	 */
	int aftDiameterColumnIndex = -1;
	int foreDiameterColumnIndex = -1;
	
	private List<ComponentPreset> presets;
	private ComponentPreset.Type presetType;
	
	private boolean okClicked = false;
	
	
	public ComponentPresetChooserDialog(Window owner, RocketComponent component) {
		super(owner, trans.get("title"), Dialog.ModalityType.APPLICATION_MODAL);
		this.component = component;
		this.presetType = component.getPresetType();
		this.presets = Application.getComponentPresetDao().listForType(component.getPresetType());
		
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
		sub.add(filterText, "growx");
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
		
		panel.add(sub, "growx, ay 0, gapright para");
		
		
		panel.add(getFilterCheckboxes(), "wrap para");
		
		componentSelectionTable = new ComponentPresetTable(presetType, presets, displayedColumnKeys);
		//		GUIUtil.setAutomaticColumnTableWidths(componentSelectionTable, 20);
		int w = componentSelectionTable.getRowHeight() + 4;
		TableColumn tc = componentSelectionTable.getColumnModel().getColumn(0);
		tc.setPreferredWidth(w);
		tc.setMaxWidth(w);
		tc.setMinWidth(w);
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(componentSelectionTable);
		panel.add(scrollpane, "grow, width 700lp, height 300lp, spanx, wrap rel");
		
		panel.add(new JLabel(Chars.UP_ARROW + " " + trans.get("lbl.favorites")), "spanx, gapleft 5px, wrap para");
		
		
		// OK / Cancel buttons
		JButton okButton = new JButton(trans.get("dlg.but.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close(true);
			}
		});
		panel.add(okButton, "tag ok, spanx, split");
		
		//// Cancel button
		JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close(false);
			}
		});
		panel.add(cancelButton, "tag cancel");
		
		this.add(panel);
		
		GUIUtil.rememberWindowSize(this);
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}
	
	
	private JPanel getFilterCheckboxes() {
		SymmetricComponent sc;
		
		JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
		
		/*
		 * Add show all compatible check box.
		 */
		final List<ComponentPreset.Type> compatibleTypes = component.getPresetType().getCompatibleTypes();
		final ComponentPreset.Type nativeType = component.getPresetType();
		if (compatibleTypes != null && compatibleTypes.size() > 0) {
			JCheckBox showAll = new JCheckBox();
			showAll.setText(trans.get("ComponentPresetChooserDialog.checkbox.showAllCompatible"));
			panel.add(showAll, "wrap");
			showAll.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (((JCheckBox) e.getItem()).isSelected()) {
						presets = Application.getComponentPresetDao().listForTypes(compatibleTypes);
					} else {
						presets = Application.getComponentPresetDao().listForType(nativeType);
					}
					componentSelectionTable.updateData(presets);
				}
			});
		}
		
		/*
		 * Add filter by fore diameter
		 */
		foreDiameterFilterCheckBox = new JCheckBox(trans.get("ComponentPresetChooserDialog.checkbox.filterForeDiameter"));
		sc = getPreviousSymmetricComponent();
		if (sc != null && foreDiameterColumnIndex >= 0) {
			foreDiameterFilter = new ComponentPresetRowFilter(sc.getAftRadius() * 2.0, foreDiameterColumnIndex);
			panel.add(foreDiameterFilterCheckBox, "wrap");
			foreDiameterFilterCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateFilters();
				}
			});
		}
		
		/*
		 * Add filter by aft diameter
		 */
		aftDiameterFilterCheckBox = new JCheckBox(trans.get("ComponentPresetChooserDialog.checkbox.filterAftDiameter"));
		sc = getNextSymmetricComponent();
		if (sc != null && aftDiameterColumnIndex >= 0) {
			aftDiameterFilter = new ComponentPresetRowFilter(sc.getForeRadius() * 2.0, aftDiameterColumnIndex);
			panel.add(aftDiameterFilterCheckBox, "wrap");
			aftDiameterFilterCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					updateFilters();
				}
			});
		}
		
		return panel;
	}
	
	/**
	 * Return the motor selected by this chooser dialog, or <code>null</code> if the selection has been aborted.
	 * 
	 * @return	the selected motor, or <code>null</code> if no motor has been selected or the selection was canceled.
	 */
	public ComponentPreset getSelectedComponentPreset() {
		if (!okClicked)
			return null;
		int row = componentSelectionTable.getSelectedRow();
		if (row < 0) {
			// Nothing selected.
			return null;
		}
		row = componentSelectionTable.convertRowIndexToModel(row);
		return presets.get(row);
	}
	
	public void close(boolean ok) {
		okClicked = ok;
		this.setVisible(false);
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
		if (aftDiameterFilterCheckBox.isSelected()) {
			filters.add(aftDiameterFilter);
		}
		if (foreDiameterFilterCheckBox.isSelected()) {
			filters.add(foreDiameterFilter);
		}
		
		componentSelectionTable.setRowFilter(RowFilter.andFilter(filters));
	}
	
	
	private SymmetricComponent getPreviousSymmetricComponent() {
		RocketComponent c = component;
		while (c != null) {
			c = c.getPreviousComponent();
			if (c instanceof SymmetricComponent) {
				return (SymmetricComponent) c;
			}
		}
		return null;
	}
	
	
	private SymmetricComponent getNextSymmetricComponent() {
		RocketComponent c = component;
		while (c != null) {
			c = c.getNextComponent();
			if (c instanceof SymmetricComponent) {
				return (SymmetricComponent) c;
			}
		}
		return null;
	}
}
