package net.sf.openrocket.gui.configdialog;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.adaptors.PresetModel;
import net.sf.openrocket.gui.adaptors.TextComponentSelectionKeyListener;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.dialogs.preset.ComponentPresetChooserDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.widgets.IconToggleButton;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Invalidatable;

public class RocketComponentConfig extends JPanel {
	private static final long serialVersionUID = -2925484062132243982L;

	private static final Translator trans = Application.getTranslator();
	private static final Preferences preferences = Application.getPreferences();
	
	protected final OpenRocketDocument document;
	protected final RocketComponent component;
	protected final JTabbedPane tabbedPane;
	protected final ComponentConfigDialog parent;
	protected boolean isNewComponent = false;		// Checks whether this config dialog is editing an existing component, or a new one
	
	private final List<Invalidatable> invalidatables = new ArrayList<Invalidatable>();
	protected final List<Component> order = new ArrayList<>();		// Component traversal order
	
	private JComboBox<?> presetComboBox;
	private PresetModel presetModel;
	protected Component focusElement = null;	// Element that will be focused on after a preset is selected
	
	protected final JTextField componentNameField;
	protected JTextArea commentTextArea;
	private final TextFieldListener textFieldListener;

	private DescriptionArea componentInfo;
	private IconToggleButton infoBtn;

	private JPanel buttonPanel;
	protected JButton okButton;
	protected JButton cancelButton;
	private AppearancePanel appearancePanel = null;
	
	private JLabel infoLabel;
	private StyledLabel multiCompEditLabel;

	private boolean allSameType;		// Checks whether all listener components are of the same type as <component>
	private boolean allMassive;			// Checks whether all listener components, and this component, are massive

	public RocketComponentConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
		setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[growprio 5]5![fill, grow, growprio 500]5![growprio 5]"));

		this.document = document;
		this.component = component;
		if (parent instanceof ComponentConfigDialog) {
			this.parent = (ComponentConfigDialog) parent;
		} else {
			this.parent = null;
		}

		// Check the listeners for the same type and massive status
		allSameType = true;
		allMassive = component.isMassive();
		List<RocketComponent> listeners = component.getConfigListeners();
		if (listeners != null && listeners.size() > 0) {
			allSameType = component.checkAllClassesEqual(listeners);
			if (allMassive) {	// Only check if <component> is already massive
				for (RocketComponent listener : listeners) {
					if (!listener.isMassive()) {
						allMassive = false;
						break;
					}
				}
			}
		}

		//// Component name:
		JLabel label = new JLabel(trans.get("RocketCompCfg.lbl.Componentname"));
		//// The component name.
		label.setToolTipText(trans.get("RocketCompCfg.lbl.Componentname.ttip"));
		this.add(label, "spanx, height 32!, split");

		componentNameField = new JTextField(15);
		textFieldListener = new TextFieldListener();
		componentNameField.addActionListener(textFieldListener);
		componentNameField.addFocusListener(textFieldListener);
		componentNameField.addKeyListener(new TextComponentSelectionKeyListener(componentNameField));
		//// The component name.
		componentNameField.setToolTipText(trans.get("RocketCompCfg.lbl.Componentname.ttip"));
		this.add(componentNameField, "growx");
		order.add(componentNameField);

		if (allSameType && component.getPresetType() != null) {
			// If the component supports a preset, show the preset selection box.
			presetModel = new PresetModel(this, document, component);
			presetComboBox = new JComboBox(presetModel);
			presetComboBox.setMaximumRowCount(25);
			presetComboBox.setEditable(false);
			presetComboBox.setToolTipText(trans.get("PresetModel.combo.ttip"));
			this.add(presetComboBox, "growx 110");
			order.add(presetComboBox);

			final JButton selectPreset = new SelectColorButton(trans.get("PresetModel.lbl.partsLib"));
			selectPreset.setToolTipText(trans.get("PresetModel.lbl.partsLib.ttip"));
			selectPreset.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectPreset();
				}
			});
			this.add(selectPreset);
			order.add(selectPreset);
		}

		tabbedPane = new JTabbedPane();
		this.add(tabbedPane, "newline, span, growx, growy 100, wrap");
		order.add(tabbedPane);
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				tabbedPane.requestFocusInWindow();
			}
		});

		//// Override and Mass and CG override options
		tabbedPane.addTab(trans.get("RocketCompCfg.tab.Override"), null, overrideTab(),
				trans.get("RocketCompCfg.tab.Override.ttip"));
		if (allMassive) {
			//// Appearance options
			appearancePanel = new AppearancePanel(document, component, parent, order);
			tabbedPane.addTab(trans.get("RocketCompCfg.tab.Appearance"), null, appearancePanel,
					trans.get("RocketCompCfg.tab.Appearance.ttip"));
		}

		//// Comment and Specify a comment for the component
		tabbedPane.addTab(trans.get("RocketCompCfg.tab.Comment"), null, commentTab(),
				trans.get("RocketCompCfg.tab.Comment.ttip"));

		addButtons();

		updateFields();
	}

	/**
	 * Add a section to the component configuration dialog that displays information about the component.
	 */
	private void addComponentInfo(JPanel buttonPanel) {
		// Don't add the info panel if this is a multi-comp edit
		List<RocketComponent> listeners = component.getConfigListeners();
		if (listeners != null && listeners.size() > 0) {
			return;
		}

		final String helpText;
		final String key = "ComponentInfo." + component.getClass().getSimpleName();
		if (!trans.checkIfKeyExists(key)) {
			return;
		}
		helpText = "<html>" + trans.get(key) + "</html>";

		// Component info
		componentInfo = new DescriptionArea(helpText, 5);
		componentInfo.setTextFont(null);
		componentInfo.setVisible(false);
		this.add(componentInfo, "gapleft 10, gapright 10, growx, spanx, wrap");

		// Component info toggle button
		infoBtn = new IconToggleButton();
		infoBtn.setToolTipText(trans.get("RocketCompCfg.btn.ComponentInfo.ttip"));
		infoBtn.setIconScale(1.2f);
		infoBtn.setSelectedIcon(Icons.HELP_ABOUT);
		buttonPanel.add(infoBtn, "split 3, gapright para");

		infoBtn.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				// Note: we will display the help text if the infoButton is not selected, and hide it if it is selected.
				// This way, the info button is displayed as "enabled" to draw attention to it.
				componentInfo.setVisible(e.getStateChange() != ItemEvent.SELECTED);

				// Repaint to fit to the new size
				if (parent != null) {
					parent.pack();
				} else {
					updateUI();
				}
			}
		});

		infoBtn.setSelected(true);
	}
	
	protected void addButtons(JButton... buttons) {
		if (componentInfo != null) {
			this.remove(componentInfo);
		}
		if (buttonPanel != null) {
			this.remove(buttonPanel);
		}
		
		buttonPanel = new JPanel(new MigLayout("fill, ins 5, hidemode 3"));

		//// Component info
		addComponentInfo(buttonPanel);

		//// Multi-comp edit label
		multiCompEditLabel = new StyledLabel(" ", -1, Style.BOLD);
		multiCompEditLabel.setFontColor(new Color(170, 0, 100));
		buttonPanel.add(multiCompEditLabel, "split 2");

		//// Mass:
		infoLabel = new StyledLabel(" ", -1);
		buttonPanel.add(infoLabel, "growx");
		
		for (JButton b : buttons) {
			buttonPanel.add(b, "right, gap para");
		}

		//// Cancel button
		this.cancelButton = new SelectColorButton(trans.get("dlg.but.cancel"));
		this.cancelButton.setToolTipText(trans.get("RocketCompCfg.btn.Cancel.ttip"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Don't do anything on cancel if you are editing an existing component, and it is not modified
				if (!isNewComponent && parent != null && !parent.isModified()) {
					ComponentConfigDialog.disposeDialog();
					return;
				}
				// Apply the cancel operation if set to auto discard in preferences
				if (!preferences.isShowDiscardConfirmation()) {
					ComponentConfigDialog.clearConfigListeners = false;		// Undo action => config listeners of new component will be cleared
					ComponentConfigDialog.disposeDialog();
					document.undo();
					return;
				}

				// Yes/No dialog: Are you sure you want to discard your changes?
				JPanel msg = createCancelOperationContent();
				int resultYesNo = JOptionPane.showConfirmDialog(RocketComponentConfig.this, msg,
						trans.get("RocketCompCfg.CancelOperation.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (resultYesNo == JOptionPane.YES_OPTION) {
					ComponentConfigDialog.clearConfigListeners = false;		// Undo action => config listeners of new component will be cleared
					ComponentConfigDialog.disposeDialog();
					document.undo();
				}
			}
		});
		buttonPanel.add(cancelButton, "split 2, right, gapleft 30lp");

		//// Ok button
		this.okButton = new SelectColorButton(trans.get("dlg.but.ok"));
		this.okButton.setToolTipText(trans.get("RocketCompCfg.btn.OK.ttip"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ComponentConfigDialog.disposeDialog();
			}
		});
		buttonPanel.add(okButton);

		updateFields();
		
		this.add(buttonPanel, "newline, spanx, growx");
	}

	private JPanel createCancelOperationContent() {
		JPanel panel = new JPanel(new MigLayout());
		String msg = isNewComponent ? trans.get("RocketCompCfg.CancelOperation.msg.undoAdd") :
				trans.get("RocketCompCfg.CancelOperation.msg.discardChanges");
		JLabel msgLabel = new JLabel(msg);
		JCheckBox dontAskAgain = new JCheckBox(trans.get("RocketCompCfg.CancelOperation.checkbox.dontAskAgain"));
		dontAskAgain.setSelected(false);
		dontAskAgain.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					preferences.setShowDiscardConfirmation(false);
				}
				// Unselected state should be not be possible and thus not be handled
			}
		});

		panel.add(msgLabel, "left, wrap");
		panel.add(dontAskAgain, "left, gaptop para");

		return panel;
	}
	
	
	/**
	 * Called when a change occurs, so that the fields can be updated if necessary.
	 * When overriding this method, the supermethod must always be called.
	 */
	public void updateFields() {
		// Component name
		componentNameField.setText(component.getName());

		// Info label
		StringBuilder sb = new StringBuilder();

		if (allSameType && component.getPresetComponent() != null) {
			ComponentPreset preset = component.getPresetComponent();
			sb.append(preset.getManufacturer() + " " + preset.getPartNo() + "      ");
		}

		List<RocketComponent> listeners = component.getConfigListeners();
		if (allMassive && (listeners == null || listeners.size() == 0)) {	// TODO: support aggregate mass display for current component and listeners?
			sb.append(trans.get("RocketCompCfg.lbl.Componentmass") + " ");
			sb.append(UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(
					component.getComponentMass()));
			
			String overridetext = null;
			if (component.isMassOverridden()) {
				overridetext = trans.get("RocketCompCfg.lbl.overriddento") + " " + UnitGroup.UNITS_MASS.getDefaultUnit().
						toStringUnit(component.getOverrideMass()) + ")";
			}

			if (component.getMassOverriddenBy() != null) {
				overridetext = trans.get("RocketCompCfg.lbl.overriddenby") + " " + component.getMassOverriddenBy().getName() + ")";
			}
			
			if (overridetext != null) {
				sb.append(" " + overridetext);
			}
			
			infoLabel.setText(sb.toString());
		} else {
			infoLabel.setText("");
		}

		// Multi-comp edit label
		if (listeners != null && listeners.size() > 0) {
			if (infoBtn != null) {
				componentInfo.setVisible(false);
				infoBtn.setVisible(false);
			}

			multiCompEditLabel.setText(trans.get("ComponentCfgDlg.MultiComponentEdit"));

			StringBuilder components = new StringBuilder(trans.get("ComponentCfgDlg.MultiComponentEdit.ttip"));
			components.append(component.getName()).append(", ");
			for (int i = 0; i < listeners.size(); i++) {
				if (i < listeners.size() - 1) {
					components.append(listeners.get(i).getName()).append(", ");
				} else {
					components.append(listeners.get(i).getName());
				}
			}
			multiCompEditLabel.setToolTipText(components.toString());
		} else {
			if (infoBtn != null) {
				componentInfo.setVisible(false);
				infoBtn.setSelected(true);
				infoBtn.setVisible(true);
			}
			multiCompEditLabel.setText("");
		}
	}

	/**
	 * Open the component preset dialog.
	 */
	public void selectPreset() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (presetComboBox == null || presetModel == null) return;
				((ComponentPresetDatabase) Application.getComponentPresetDao()).addDatabaseListener(presetModel);
				ComponentPresetChooserDialog dialog =
						new ComponentPresetChooserDialog(SwingUtilities.getWindowAncestor(RocketComponentConfig.this),
								component, presetModel);
				dialog.setVisible(true);
				((ComponentPresetDatabase) Application.getComponentPresetDao()).removeChangeListener(presetModel);
			}
		});
	}

	public void clearConfigListeners() {
		if (appearancePanel != null) {
			appearancePanel.clearConfigListeners();
		}
	}

	public int getSelectedTabIndex() {
		return tabbedPane.getSelectedIndex();
	}

	public String getSelectedTabName() {
		if (tabbedPane != null) {
			return tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
		} else {
			return "";
		}
	}

	public void setSelectedTabIndex(int index) {
		if (tabbedPane != null) {
			tabbedPane.setSelectedIndex(index);
		}
	}

	public void setSelectedTab(String tabName) {
		if (tabbedPane != null) {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				if (tabbedPane.getTitleAt(i).equals(tabName)) {
					tabbedPane.setSelectedIndex(i);
					return;
				}
			}
			tabbedPane.setSelectedIndex(0);
		}
	}
	
	private JPanel overrideTab() {
		JPanel panel = new JPanel(new MigLayout("align 0% 20%, gap rel unrel",
				"[][65lp::80lp][::20lp][]", ""));
		//// Override the mass, center of gravity, or drag coeficient of the component

		JCheckBox check;
		JCheckBox checkSub;
		BooleanModel bm;
		UnitSelector us;
		BasicSlider bs;
		
		// OVERRIDE MASS ----------------------------------
		JPanel checkboxes = new JPanel(new MigLayout("inset 0"));
		bm = new BooleanModel(component, "MassOverridden");
		check = new JCheckBox(bm);
		//// Override mass:
		check.setText(trans.get("RocketCompCfg.checkbox.Overridemass"));
		check.setToolTipText(trans.get("RocketCompCfg.checkbox.Overridemass.ttip"));
		checkboxes.add(check, "wrap");
		order.add(check);

		////// Override subcomponents
		BooleanModel bmSubcomp = new BooleanModel(component, "SubcomponentsOverriddenMass");
		checkSub = new JCheckBox(bmSubcomp);
		checkSub.setText(trans.get("RocketCompCfg.checkbox.OverrideSubcomponents"));
		Font smallFont = checkSub.getFont();
		smallFont = smallFont.deriveFont(smallFont.getSize2D() - 1);
		checkSub.setFont(smallFont);
		checkSub.setToolTipText(trans.get("RocketCompCfg.checkbox.OverrideSubcomponents.Mass.ttip"));
		bm.addEnableComponent(checkSub, true);
		checkboxes.add(checkSub, "gapleft 25lp, wrap");
		order.add(checkSub);

		////// Mass overridden by
		if (component.getMassOverriddenBy() != null) {
			StyledLabel labelMassOverriddenBy = new StyledLabel(
					String.format(trans.get("RocketCompCfg.lbl.MassOverriddenBy"), component.getMassOverriddenBy().getName()),
					0, StyledLabel.Style.BOLD);
			labelMassOverriddenBy.setFontColor(net.sf.openrocket.util.Color.DARK_RED.toAWTColor());
			labelMassOverriddenBy.setToolTipText(
					String.format(trans.get("RocketCompCfg.lbl.MassOverriddenBy.ttip"), component.getMassOverriddenBy().getName()));
			checkboxes.add(labelMassOverriddenBy, "gapleft 25lp, wrap");
		}

		panel.add(checkboxes, "growx 1, gapright 20lp");
		
		DoubleModel m = new DoubleModel(component, "OverrideMass", UnitGroup.UNITS_MASS, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		bm.addEnableComponent(spin, true);
		panel.add(spin, "growx 2");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		us = new UnitSelector(m);
		bm.addEnableComponent(us, true);
		panel.add(us, "growx 1");
		
		bs = new BasicSlider(m.getSliderModel(0, 0.03, 1.0));
		bm.addEnableComponent(bs);
		panel.add(bs, "w 150lp, wrap");

		if (component.getMassOverriddenBy() != null) {
			check.setEnabled(false);
			bm.removeEnableComponent(checkSub);
			bm.removeEnableComponent(spin);
			bm.removeEnableComponent(us);
			bm.removeEnableComponent(bs);
			checkSub.setEnabled(false);
			spin.setEnabled(false);
			us.setEnabled(false);
			bs.setEnabled(false);
		}

		// END OVERRIDE MASS ----------------------------------
	
		// OVERRIDE CG ----------------------------------
		checkboxes = new JPanel(new MigLayout("inset 0"));
		bm = new BooleanModel(component, "CGOverridden");
		check = new JCheckBox(bm);
		//// Override center of gravity:"
		check.setText(trans.get("RocketCompCfg.checkbox.Overridecenterofgrav"));
		check.setToolTipText(trans.get("RocketCompCfg.checkbox.Overridecenterofgrav.ttip"));
		checkboxes.add(check, "wrap");
		order.add(check);

		////// Override subcomponents
		bmSubcomp = new BooleanModel(component, "SubcomponentsOverriddenCG");
		checkSub = new JCheckBox(bmSubcomp);
		checkSub.setText(trans.get("RocketCompCfg.checkbox.OverrideSubcomponents"));
		checkSub.setFont(smallFont);
		checkSub.setToolTipText(trans.get("RocketCompCfg.checkbox.OverrideSubcomponents.CG.ttip"));
		bm.addEnableComponent(checkSub, true);
		checkboxes.add(checkSub, "gapleft 25lp, wrap");
		order.add(checkSub);

		////// CG overridden by
		if (component.getCGOverriddenBy() != null) {
			StyledLabel labelCGOverriddenBy = new StyledLabel(
					String.format(trans.get("RocketCompCfg.lbl.CGOverriddenBy"), component.getCGOverriddenBy().getName()),
					0, StyledLabel.Style.BOLD);
			labelCGOverriddenBy.setFontColor(net.sf.openrocket.util.Color.DARK_RED.toAWTColor());
			labelCGOverriddenBy.setToolTipText(
					String.format(trans.get("RocketCompCfg.lbl.CGOverriddenBy.ttip"), component.getCGOverriddenBy().getName()));
			checkboxes.add(labelCGOverriddenBy, "gapleft 25lp, wrap");
		}

		panel.add(checkboxes, "growx 1, gapright 20lp");
		
		m = new DoubleModel(component, "OverrideCGX", UnitGroup.UNITS_LENGTH, 0);
		// Calculate suitable length for slider
		DoubleModel length;
		if (component.getChildCount() > 0) {
			Iterator<RocketComponent> iterator = component.iterator(true);
			double minL = Double.MAX_VALUE;
			double maxL = Double.MIN_VALUE;

			while (iterator.hasNext()) {
				RocketComponent c = iterator.next();

				double compPos = c.getAxialOffset(AxialMethod.ABSOLUTE);
				if (compPos < minL) {
					minL = compPos;
				}

				double compLen = c.getLength();
				if (c instanceof FinSet) {
					compLen = ((FinSet) c).getInstanceBoundingBox().span().x;
				}
				if (compPos + compLen > maxL) {
					maxL = compPos + compLen;
				}
			}
			length = new DoubleModel(maxL - minL);
		} else if (component instanceof FinSet) {
			double compLen = ((FinSet) component).getInstanceBoundingBox().span().x;
			length = new DoubleModel(compLen);
		} else {
			length = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		}
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		bm.addEnableComponent(spin, true);
		panel.add(spin, "growx 2");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		us = new UnitSelector(m);
		bm.addEnableComponent(us, true);
		panel.add(us, "growx 1");
		
		bs = new BasicSlider(m.getSliderModel(new DoubleModel(0), length));
		bm.addEnableComponent(bs);
		panel.add(bs, "w 150lp, wrap");

		if (component.getCGOverriddenBy() != null) {
			check.setEnabled(false);
			bm.removeEnableComponent(checkSub);
			bm.removeEnableComponent(spin);
			bm.removeEnableComponent(us);
			bm.removeEnableComponent(bs);
			checkSub.setEnabled(false);
			spin.setEnabled(false);
			us.setEnabled(false);
			bs.setEnabled(false);
		}

		// END OVERRIDE CG ---------------------------------------------------


    	// BEGIN OVERRIDE CD ------------------------------------------
		checkboxes = new JPanel(new MigLayout("inset 0"));
		bm = new BooleanModel(component, "CDOverridden");
		check = new JCheckBox(bm);
		//// Override coefficient of drag:
		check.setText(trans.get("RocketCompCfg.checkbox.SetDragCoeff"));
		check.setToolTipText(trans.get("RocketCompCfg.checkbox.SetDragCoeff.ttip"));
		checkboxes.add(check, "wrap");
		order.add(check);

		////// Override subcomponents
		bmSubcomp = new BooleanModel(component, "SubcomponentsOverriddenCD");
		checkSub = new JCheckBox(bmSubcomp);
		checkSub.setText(trans.get("RocketCompCfg.checkbox.OverrideSubcomponents"));
		checkSub.setFont(smallFont);
		checkSub.setToolTipText(trans.get("RocketCompCfg.checkbox.OverrideSubcomponents.CD.ttip"));
		bm.addEnableComponent(checkSub, true);
		checkboxes.add(checkSub, "gapleft 25lp, wrap");
		order.add(checkSub);

		////// CD overridden by
		if (component.getCDOverriddenBy() != null) {
			StyledLabel labelCDOverriddenBy = new StyledLabel(
					String.format(trans.get("RocketCompCfg.lbl.CDOverriddenBy"), component.getCDOverriddenBy().getName()),
					0, StyledLabel.Style.BOLD);
			labelCDOverriddenBy.setFontColor(net.sf.openrocket.util.Color.DARK_RED.toAWTColor());
			labelCDOverriddenBy.setToolTipText(
					String.format(trans.get("RocketCompCfg.lbl.CDOverriddenBy"), component.getCDOverriddenBy().getName()));
			checkboxes.add(labelCDOverriddenBy, "gapleft 25lp, wrap");
		}

		panel.add(checkboxes, "growx 1, gapright 20lp");
		
		m = new DoubleModel(component, "OverrideCD", UnitGroup.UNITS_COEFFICIENT, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		spin = new JSpinner(m.getSpinnerModel());

		spin.setEditor(new SpinnerEditor(spin));
		bm.addEnableComponent(spin, true);
		panel.add(spin, "top, growx 1");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		
		bs = new BasicSlider(m.getSliderModel(-1.0, 1.0));
		bm.addEnableComponent(bs);
		panel.add(bs, "top, skip, w 150lp, wrap");

		if (component.getCDOverriddenBy() != null) {
			check.setEnabled(false);
			bm.removeEnableComponent(checkSub);
			bm.removeEnableComponent(spin);
			bm.removeEnableComponent(bs);
			checkSub.setEnabled(false);
			spin.setEnabled(false);
			bs.setEnabled(false);
		}

		// END OVERRIDE CD --------------------------------------------------

		// OVERRIDE MASS, CG DOESN'T INCLUDE MOTORS  --------------------------------------------------
		panel.add(new StyledLabel(trans.get("RocketCompCfg.lbl.longB1") +
						//// The center of gravity is measured from the front end of the
						trans.get("RocketCompCfg.lbl.longB2") + " " +
						component.getComponentName().toLowerCase(Locale.getDefault()) + ".", -1),
				"spanx, pushy, aligny bottom");
		
		return panel;
	}
	
	
	private JPanel commentTab() {
		JPanel panel = new JPanel(new MigLayout("fill","[]","[][grow]"));
		
		//// Comments on the
		panel.add(new StyledLabel(trans.get("RocketCompCfg.lbl.Commentsonthe") + " " + component.getComponentName() + ":",
				Style.BOLD), "wrap");
		
		// TODO: LOW:  Changes in comment from other sources not reflected in component
		commentTextArea = new JTextArea(component.getComment());
		commentTextArea.setLineWrap(true);
		commentTextArea.setWrapStyleWord(true);
		commentTextArea.setEditable(true);
		GUIUtil.setTabToFocusing(commentTextArea);
		commentTextArea.addFocusListener(textFieldListener);
		commentTextArea.addKeyListener(new TextComponentSelectionKeyListener(commentTextArea));
		
		panel.add(new JScrollPane(commentTextArea), "grow");
		order.add(commentTextArea);
		
		return panel;
	}
	
	
	protected JPanel shoulderTab() {
		JPanel panel = new JPanel(new MigLayout("fillx"));
		DoubleModel m0 = new DoubleModel(0);
		
		////  Fore shoulder, not for NoseCone
		if (!(component instanceof NoseCone)) {
			addForeShoulderSection(panel, m0);
		}

		////  Aft shoulder
		addAftShoulderSection(panel, m0);

		return panel;
	}

	private void addForeShoulderSection(JPanel panel, DoubleModel m0) {
		DoubleModel m;
		JCheckBox check;
		JPanel sub;
		DoubleModel m2;
		JSpinner spin;
		BooleanModel bm;
		sub = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		//// Fore shoulder
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("RocketCompCfg.border.Foreshoulder")));


		////  Radius
		//// Diameter:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Diameter")));

		m = new DoubleModel(component, "ForeShoulderRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, "ForeRadius", 2, UnitGroup.UNITS_LENGTH);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");


		////  Length:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Length")));

		m = new DoubleModel(component, "ForeShoulderLength", UnitGroup.UNITS_LENGTH, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.2)), "w 100lp, wrap");


		////  Thickness:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Thickness")));

		m = new DoubleModel(component, "ForeShoulderThickness", UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, "ForeShoulderRadius", UnitGroup.UNITS_LENGTH);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");


		////  Capped
		bm = new BooleanModel(component, "ForeShoulderCapped");
		check = new JCheckBox(bm);
		//// End capped
		check.setText(trans.get("RocketCompCfg.checkbox.Endcapped"));
		check.setToolTipText(trans.get("RocketCompCfg.checkbox.Endcapped.ttip"));
		sub.add(check, "spanx");
		order.add(check);

		panel.add(sub);
	}

	private void addAftShoulderSection(JPanel panel, DoubleModel m0) {
		JSpinner spin;
		JCheckBox check;
		DoubleModel m;
		DoubleModel m2;
		JPanel sub;
		BooleanModel bm;
		sub = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		String valueNameShoulder = "AftShoulder";
		String valueNameRadius = "AftRadius";

		if (component instanceof NoseCone) {
			// Nose cones have a special shoulder method to cope with flipped nose cones
			valueNameShoulder = "Shoulder";
			valueNameRadius = "BaseRadius";
			//// Nose cone shoulder
			sub.setBorder(null);
		} else {
			//// Aft shoulder
			sub.setBorder(BorderFactory.createTitledBorder(trans.get("RocketCompCfg.title.Aftshoulder")));
		}


		////  Radius
		//// Diameter:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Diameter")));

		m = new DoubleModel(component, valueNameShoulder+"Radius", 2, UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, valueNameRadius, 2, UnitGroup.UNITS_LENGTH);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");


		////  Length:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Length")));

		m = new DoubleModel(component, valueNameShoulder+"Length", UnitGroup.UNITS_LENGTH, 0);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.2)), "w 100lp, wrap");


		////  Thickness:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Thickness")));

		m = new DoubleModel(component, valueNameShoulder+"Thickness", UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, valueNameShoulder+"Radius", UnitGroup.UNITS_LENGTH);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");


		////  Capped
		bm = new BooleanModel(component, valueNameShoulder+"Capped");
		check = new JCheckBox(bm);
		//// End capped
		check.setText(trans.get("RocketCompCfg.checkbox.Endcapped"));
		check.setToolTipText(trans.get("RocketCompCfg.checkbox.Endcapped.ttip"));
		sub.add(check, "spanx");
		order.add(check);

		panel.add(sub);
	}

	/**
	 * Sets whether this dialog is editing a new component (true), or an existing one (false).
	 * @param newComponent true if this dialog is editing a new component.
	 */
	public void setNewComponent(boolean newComponent) {
		isNewComponent = newComponent;
	}

	/*
	 * Private inner class to handle events in componentNameField.
	 */
	private class TextFieldListener implements ActionListener, FocusListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			setName();
		}
		
		@Override
		public void focusGained(FocusEvent e) {
		}
		
		@Override
		public void focusLost(FocusEvent e) {
			setName();
		}
		
		private void setName() {
			if (!component.getName().equals(componentNameField.getText())) {
				component.setName(componentNameField.getText());
			}
			if (!component.getComment().equals(commentTextArea.getText())) {
				component.setComment(commentTextArea.getText());
			}
		}
	}

	/**
	 * Requests focus for the focus element that should be active after a preset is selected.
	 */
	public void setFocusElement() {
		if (focusElement != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (focusElement instanceof JSpinner) {
						SpinnerEditor ed = (SpinnerEditor) ((JSpinner)focusElement).getEditor();
						ed.getTextField().requestFocusInWindow();
					} else {
						focusElement.requestFocusInWindow();
					}
				}
			});
		}
	}
	
	
	protected void register(Invalidatable model) {
		this.invalidatables.add(model);
	}
	
	public void invalidate() {
		super.invalidate();
		for (Invalidatable i : invalidatables) {
			i.invalidate();
		}
	}
	

	protected static void setDeepEnabled(Component component, boolean enabled) {
		component.setEnabled(enabled);
		if (component instanceof Container) {
			for (Component c : ((Container) component).getComponents()) {
				setDeepEnabled(c, enabled);
			}
		}
	}
}
