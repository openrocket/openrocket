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
import javax.swing.table.TableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.InternalComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.startup.Application;

public class ComponentPresetChooserDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private final RocketComponent component;
	
	private ComponentPresetTable componentSelectionTable;
	private final JTextField filterText;
	private final JCheckBox foreDiameterFilterCheckBox;
	private final JCheckBox aftDiameterFilterCheckBox;
	
	/*
	 * outerDiamtereColumnIndex is the index of the column associated with the OUTER_DIAMETER
	 * field.  This index is needed by the matchOuterDiameterCheckBox to implement filtering.
	 */
	int aftDiameterColumnIndex = -1;
	int foreDiameterColumnIndex = -1;

	private List<ComponentPreset> presets;
	
	private boolean okClicked = false;
	
	
	public ComponentPresetChooserDialog(Window owner, RocketComponent component) {
		super(owner, trans.get("title"), Dialog.ModalityType.APPLICATION_MODAL);
		this.component = component;
		
		final TypedKey<?>[] columnKeys = component.getPresetType().getDisplayedColumns();

		presets = Application.getComponentPresetDao().listForType(component.getPresetType());

		for (int i = 0; i < columnKeys.length; i++) {
			final TypedKey<?> key = columnKeys[i];
			if ( key == ComponentPreset.OUTER_DIAMETER ) {
				// magic +1 is because we have inserted the column for favorites above.
				aftDiameterColumnIndex = i+1;
			}
			if ( key == ComponentPreset.FORE_OUTER_DIAMETER ) {
				// magic +1 is because we have inserted the column for favorites above.
				foreDiameterColumnIndex = i+1;
			}
		}
		
		/*
		 * perhaps there is a better way for this.
		 * 
		 * This check basically says that if a component does not have a fore diameter, use the
		 * outer_diameter when filtering.  The problem this introduced is when this dialog is
		 * created for nose cones (which are aft of a body tube), you will be given the option
		 * to filter based on matching fore diameter.
		 */
		if ( foreDiameterColumnIndex < 0 ) {
			foreDiameterColumnIndex = aftDiameterColumnIndex;
		}
		
		/*
		 * Add filter by text.
		 */
		JPanel panel = new JPanel(new MigLayout("fill"));
		JLabel filterLabel = new JLabel(trans.get("ComponentPresetChooserDialog.filter.label"));
		panel.add(filterLabel);
		filterText = new JTextField(15);
		panel.add(filterText,"growx, growy 0, wrap");
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

		/*
		 * Add show all compatible check box.
		 */
		final List<ComponentPreset.Type> compatibleTypes = component.getPresetType().getCompatibleTypes();
		final ComponentPreset.Type nativeType = component.getPresetType();
		if ( compatibleTypes != null && compatibleTypes.size() >  0 ) {
			JCheckBox showAll = new JCheckBox();
			showAll.setText(trans.get("ComponentPresetChooserDialog.checkbox.showAllCompatible"));
			panel.add(showAll, "skip, span 2");
			showAll.addItemListener( new ItemListener () {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if ( ((JCheckBox)e.getItem()).isSelected()  ) {
						presets = Application.getComponentPresetDao().listForTypes(compatibleTypes);
					} else {
						presets = Application.getComponentPresetDao().listForType(nativeType);
					}
					componentSelectionTable.updateData( presets );
				}
			});
			
			
		}
		
		/*
		 * Add filter by fore diameter
		 */
		foreDiameterFilterCheckBox = new JCheckBox();
		foreDiameterFilterCheckBox.setText(trans.get("ComponentPresetChooserDialog.checkbox.filterForeDiameter"));
		panel.add(foreDiameterFilterCheckBox, "skip, span 2");
		foreDiameterFilterCheckBox.addItemListener( new ItemListener () {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateFilters();
			}
		});

		RocketComponent previousComponent = component.getPreviousComponent(); 
		/* hide the fore diameter filter if it is not applicable */
		if ( foreDiameterColumnIndex < 0 || previousComponent == null ) {
			if ( !(previousComponent instanceof ExternalComponent) && !(previousComponent instanceof InternalComponent) )
			foreDiameterFilterCheckBox.setVisible(false);
		}
		
		/*
		 * Add filter by aft diameter
		 */
		aftDiameterFilterCheckBox = new JCheckBox();
		aftDiameterFilterCheckBox.setText(trans.get("ComponentPresetChooserDialog.checkbox.filterAftDiameter"));
		panel.add(aftDiameterFilterCheckBox, "skip, span 2, wrap");
		aftDiameterFilterCheckBox.addItemListener( new ItemListener () {
			@Override
			public void itemStateChanged(ItemEvent e) {
				updateFilters();
			}
		});

		/* hide the aft diameter filter if it is not applicable */
		if ( aftDiameterColumnIndex < 0 || component.getNextComponent() == null ) {
			aftDiameterFilterCheckBox.setVisible(false);
		}
		
		componentSelectionTable = new ComponentPresetTable( presets, Arrays.<TypedKey<?>>asList(columnKeys) );
		
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(componentSelectionTable);
		panel.add(scrollpane, "grow, width :500:, height :300:, spanx, wrap para");

		
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
		
		this.setModal(true);
		this.pack();
		this.setLocationByPlatform(true);
		GUIUtil.setDisposableDialogOptions(this, okButton);
		
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
		row = componentSelectionTable.convertRowIndexToModel(row);
		return presets.get(row);
	}
	
	public void close(boolean ok) {
		okClicked = ok;
		this.setVisible(false);
	}
	
	private void updateFilters() {
		List<RowFilter<TableModel,Object>> filters = new ArrayList<RowFilter<TableModel,Object>> (2);
		String filterTextRegex = filterText.getText();
		if ( filterTextRegex != null ) {
			try {
				// The "(?iu)" magic turns on case insensitivity with unicode chars
				RowFilter<TableModel,Object> regexFilter = RowFilter.regexFilter("(?iu)"+filterTextRegex);
				filters.add(regexFilter);
			} catch ( java.util.regex.PatternSyntaxException e ) {
			}
		}
		if ( aftDiameterFilterCheckBox.isSelected() ) {
			// FIXME - please verify this logic looks correct.
			// Grab the next component.
			// If this.component is an InternalComponent, then we want to filter the outer diameter field
			// against the next component's inner diameter.
			RocketComponent nextComponent = component.getNextComponent();
			if ( nextComponent != null && nextComponent instanceof SymmetricComponent ) {
				SymmetricComponent parent = (SymmetricComponent) nextComponent;
				double nextDiameter;
				if ( this.component instanceof InternalComponent ) {
					nextDiameter = parent.getInnerRadius(0) * 2.0;
				} else {
					nextDiameter = parent.getForeRadius() * 2.0;
				}
				RowFilter<TableModel,Object> outerDiameterFilter = new ComponentPresetRowFilter( nextDiameter, aftDiameterColumnIndex);
				filters.add(outerDiameterFilter);
			}
		}
		if ( foreDiameterFilterCheckBox.isSelected() ) {
			// FIXME - please verify this logic looks correct.
			// Grab the previous component.
			// If this.component is an InternalComponent, then we want to filter the outer diameter field
			// against the previous component's inner diameter.
			RocketComponent previousComponent = component.getPreviousComponent();
			if ( previousComponent != null && previousComponent instanceof SymmetricComponent ) {
				SymmetricComponent parent = (SymmetricComponent) previousComponent;
				double previousDaimeter;
				if ( this.component instanceof InternalComponent ) {
					previousDaimeter = parent.getInnerRadius(parent.getLength()) * 2.0;
				} else {
					previousDaimeter = parent.getAftRadius() * 2.0;
				}
				RowFilter<TableModel,Object> outerDiameterFilter = new ComponentPresetRowFilter( previousDaimeter, foreDiameterColumnIndex);
				filters.add(outerDiameterFilter);
			}
		}
		
		componentSelectionTable.setRowFilter( RowFilter.andFilter(filters) );
	}
}
