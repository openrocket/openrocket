package net.sf.openrocket.gui.configdialog;


import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.adaptors.PresetModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Invalidatable;

public class RocketComponentConfig extends JPanel {
	private static final long serialVersionUID = -2925484062132243982L;

	private static final Translator trans = Application.getTranslator();
	
	protected final OpenRocketDocument document;
	protected final RocketComponent component;
	protected final JTabbedPane tabbedPane;
	
	private final List<Invalidatable> invalidatables = new ArrayList<Invalidatable>();
	
	private JComboBox<?> presetComboBox;
	private PresetModel presetModel;
	
	protected final JTextField componentNameField;
	protected JTextArea commentTextArea;
	private final TextFieldListener textFieldListener;
	
	private JPanel buttonPanel;
	
	private JLabel infoLabel;
	
	
	public RocketComponentConfig(OpenRocketDocument document, RocketComponent component) {
		setLayout(new MigLayout("fill, gap 4!, ins panel", "[]:5[]", "[growprio 5]5![fill, grow, growprio 500]5![growprio 5]"));

		this.document = document;
		this.component = component;
		
		//// Component name:
		JLabel label = new JLabel(trans.get("RocketCompCfg.lbl.Componentname"));
		//// The component name.
		label.setToolTipText(trans.get("RocketCompCfg.ttip.Thecomponentname"));
		this.add(label, "spanx, height 32!, split");
		
		componentNameField = new JTextField(15);
		textFieldListener = new TextFieldListener();
		componentNameField.addActionListener(textFieldListener);
		componentNameField.addFocusListener(textFieldListener);
		//// The component name.
		componentNameField.setToolTipText(trans.get("RocketCompCfg.ttip.Thecomponentname"));
		this.add(componentNameField, "growx");
		
		if (component.getPresetType() != null) {
			// If the component supports a preset, show the preset selection box.
			presetModel = new PresetModel(this, document, component);
			presetComboBox = new JComboBox(presetModel);
			presetComboBox.setEditable(false);
			this.add(presetComboBox, "");
		}
		
		
		tabbedPane = new JTabbedPane();
		this.add(tabbedPane, "newline, span, growx, growy 100, wrap");
		
		//// Override and Mass and CG override options
		tabbedPane.addTab(trans.get("RocketCompCfg.tab.Override"), null, overrideTab(),
				trans.get("RocketCompCfg.tab.MassandCGoverride"));
		if (component.isMassive()) {
			//// Appearance options
			tabbedPane.addTab(trans.get("RocketCompCfg.tab.Appearance"), null, new AppearancePanel(document, component),
					"Appearance Tool Tip");
		}
		
		//// Comment and Specify a comment for the component
		tabbedPane.addTab(trans.get("RocketCompCfg.tab.Comment"), null, commentTab(),
				trans.get("RocketCompCfg.tab.Specifyacomment"));
		
		addButtons();
		
		updateFields();
	}
	
	
	protected void addButtons(JButton... buttons) {
		if (buttonPanel != null) {
			this.remove(buttonPanel);
		}
		
		buttonPanel = new JPanel(new MigLayout("fillx, ins 5"));

		//// Mass:
		infoLabel = new StyledLabel(" ", -1);
		buttonPanel.add(infoLabel, "growx");
		
		for (JButton b : buttons) {
			buttonPanel.add(b, "right, gap para");
		}
		
		//// Close button
		JButton closeButton = new SelectColorButton(trans.get("dlg.but.close"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ComponentConfigDialog.disposeDialog();
			}
		});
		buttonPanel.add(closeButton, "right, gap 30lp");
		
		updateFields();
		
		this.add(buttonPanel, "newline, spanx, growx");
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
		
		if (component.getPresetComponent() != null) {
			ComponentPreset preset = component.getPresetComponent();
			sb.append(preset.getManufacturer() + " " + preset.getPartNo() + "      ");
		}
		
		if (component.isMassive()) {
			sb.append(trans.get("RocketCompCfg.lbl.Componentmass") + " ");
			sb.append(UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(
					component.getComponentMass()));
			
			String overridetext = null;
			if (component.isMassOverridden()) {
				overridetext = trans.get("RocketCompCfg.lbl.overriddento") + " " + UnitGroup.UNITS_MASS.getDefaultUnit().
						toStringUnit(component.getOverrideMass()) + ")";
			}
			
			for (RocketComponent c = component.getParent(); c != null; c = c.getParent()) {
				if (c.isMassOverridden() && c.getOverrideSubcomponents()) {
					overridetext = trans.get("RocketCompCfg.lbl.overriddenby") + " " + c.getName() + ")";
				}
			}
			
			if (overridetext != null) {
				sb.append(" " + overridetext);
			}
			
			infoLabel.setText(sb.toString());
		} else {
			infoLabel.setText("");
		}
	}
	
	
	protected JPanel materialPanel(Material.Type type) {
		////Component material: and Component finish:
		return materialPanel(type, 
				trans.get("RocketCompCfg.lbl.Componentmaterial"),
				trans.get("RocketCompCfg.lbl.Componentfinish"), 
				"Material");
	}
	
	protected JPanel materialPanel(Material.Type type, String partName){
		return materialPanel(type, trans.get("RocketCompCfg.lbl.Componentmaterial"),
			trans.get("RocketCompCfg.lbl.Componentfinish"), partName);
	    	}
	
	protected JPanel materialPanel(Material.Type type,
					String materialString, 
					String finishString, 
					String partName) {
		
	    JPanel subPanel = new JPanel(new MigLayout());
	    	JLabel label = new JLabel(materialString);
		//// The component material affects the weight of the component.
		label.setToolTipText(trans.get("RocketCompCfg.lbl.ttip.componentmaterialaffects"));
		subPanel.add(label, "spanx 4, wrap rel");
		
		JComboBox<Material> materialCombo = new JComboBox<Material>(new MaterialModel(subPanel, component, type, partName));
		//// The component material affects the weight of the component.
		materialCombo.setToolTipText(trans.get("RocketCompCfg.combo.ttip.componentmaterialaffects"));
		subPanel.add(materialCombo, "spanx 4, growx, wrap paragraph");
		
		
		if (component instanceof ExternalComponent) {
			label = new JLabel(finishString);
			////<html>The component finish affects the aerodynamic drag of the component.<br>
			String tip = trans.get("RocketCompCfg.lbl.longA1")
					//// The value indicated is the average roughness height of the surface.
					+ trans.get("RocketCompCfg.lbl.longA2");
			label.setToolTipText(tip);
			subPanel.add(label, "spanx 4, wmin 220lp, wrap rel");
			
			JComboBox<ExternalComponent.Finish> finishCombo = new JComboBox<ExternalComponent.Finish>(
					new EnumModel<ExternalComponent.Finish>(component, "Finish"));
			finishCombo.setToolTipText(tip);
			subPanel.add( finishCombo, "spanx 4, growx, split");
			
			//// Set for all
			JButton button = new SelectColorButton(trans.get("RocketCompCfg.but.Setforall"));
			//// Set this finish for all components of the rocket.
			button.setToolTipText(trans.get("RocketCompCfg.but.ttip.Setforall"));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Finish f = ((ExternalComponent) component).getFinish();
					try {
						document.startUndo("Set rocket finish");
						
						// Do changes
						Iterator<RocketComponent> iter = component.getRoot().iterator();
						while (iter.hasNext()) {
							RocketComponent c = iter.next();
							if (c instanceof ExternalComponent) {
								((ExternalComponent) c).setFinish(f);
							}
						}
					} finally {
						document.stopUndo();
					}
				}
			});
			subPanel.add(button, "wrap paragraph");
		}
		return subPanel;
	}
	
	protected JPanel instanceablePanel( Instanceable inst ){
		JPanel panel = new JPanel( new MigLayout("fill"));
		{ // Instance Count
			panel.add(new JLabel(trans.get("RocketCompCfg.lbl.InstanceCount")));
			IntegerModel countModel = new IntegerModel(component, "InstanceCount", 1);
			JSpinner countSpinner = new JSpinner( countModel.getSpinnerModel());
			countSpinner.setEditor(new SpinnerEditor(countSpinner));
			panel.add(countSpinner, "w 100lp, wrap rel");
		}
		
		{ // Instance separation
			panel.add(new JLabel(trans.get("RocketCompCfg.lbl.InstanceSeparation")));
			DoubleModel separationModel = new DoubleModel(component, "InstanceSeparation", UnitGroup.UNITS_LENGTH);
			JSpinner separationSpinner = new JSpinner( separationModel.getSpinnerModel());
			separationSpinner.setEditor(new SpinnerEditor(separationSpinner));
			panel.add(separationSpinner, "growx");
			panel.add(new UnitSelector(separationModel), "growx");
			panel.add(new BasicSlider(separationModel.getSliderModel(0, 0.001, 0.02)), "w 100lp, wrap para");
		}
		return panel;
	}
	
	private JPanel overrideTab() {
		JPanel panel = new JPanel(new MigLayout("align 50% 20%, fillx, gap rel unrel",
				"[][65lp::][30lp::][]", ""));
		//// Override the mass or center of gravity of the
		panel.add(new StyledLabel(trans.get("RocketCompCfg.lbl.Overridemassorcenter") + " " +
				component.getComponentName() + ":", Style.BOLD), "spanx, wrap 20lp");
		
		JCheckBox check;
		BooleanModel bm;
		UnitSelector us;
		BasicSlider bs;
		
		////  Mass
		bm = new BooleanModel(component, "MassOverridden");
		check = new JCheckBox(bm);
		//// Override mass:
		check.setText(trans.get("RocketCompCfg.checkbox.Overridemass"));
		panel.add(check, "growx 1, gapright 20lp");
		
		DoubleModel m = new DoubleModel(component, "OverrideMass", UnitGroup.UNITS_MASS, 0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		bm.addEnableComponent(spin, true);
		panel.add(spin, "growx 1");
		
		us = new UnitSelector(m);
		bm.addEnableComponent(us, true);
		panel.add(us, "growx 1");
		
		bs = new BasicSlider(m.getSliderModel(0, 0.03, 1.0));
		bm.addEnableComponent(bs);
		panel.add(bs, "growx 5, w 100lp, wrap");
		
	
		//OVERRIDES CG ----------------------------------
	
		////  CG override
		bm = new BooleanModel(component, "CGOverridden");
		check = new JCheckBox(bm);
		//// Override center of gravity:"
		check.setText(trans.get("RocketCompCfg.checkbox.Overridecenterofgrav"));
		panel.add(check, "growx 1, gapright 20lp");
		
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
		panel.add(spin, "growx 1");
		
		us = new UnitSelector(m);
		bm.addEnableComponent(us, true);
		panel.add(us, "growx 1");
		
		bs = new BasicSlider(m.getSliderModel(new DoubleModel(0), length));
		bm.addEnableComponent(bs);
		panel.add(bs, "growx 5, w 100lp, wrap");
		

		//END OVERRIDES CG ---------------------------------------------------

		// Override subcomponents checkbox
		bm = new BooleanModel(component, "OverrideSubcomponents");
		check = new JCheckBox(bm);
		//// Override mass and CG of all subcomponents
		check.setText(trans.get("RocketCompCfg.checkbox.OverridemassandCG"));
		panel.add(check, "spanx, wrap 35lp");


        //BEGIN OVERRIDES CD ---------------------------------------------------


		bm = new BooleanModel(component, "CDOverridden");
		check = new JCheckBox(bm);
		//// Override coefficient of drag:
		check.setText(trans.get("RocketCompCfg.checkbox.SetDragCoeff"));
		panel.add(check, "growx 1, gapright 20lp");
		
		m = new DoubleModel(component, "OverrideCD", UnitGroup.UNITS_COEFFICIENT, 0);
		spin = new JSpinner(m.getSpinnerModel());

		spin.setEditor(new SpinnerEditor(spin));
		bm.addEnableComponent(spin, true);
		panel.add(spin, "growx 1");
		
		
		bs = new BasicSlider(m.getSliderModel(0, 1.0));
		bm.addEnableComponent(bs);
		panel.add(bs, "growx 5, w 100lp, wrap");


		//END OVERRIDES CP --------------------------------------------------

		//// The overridden mass does not include motors.
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
		
		panel.add(new JScrollPane(commentTextArea), "grow");
		
		return panel;
	}
	
	
	protected JPanel shoulderTab() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub;
		DoubleModel m, m2;
		DoubleModel m0 = new DoubleModel(0);
		BooleanModel bm;
		JCheckBox check;
		JSpinner spin;
		
		
		////  Fore shoulder, not for NoseCone
		
		if (!(component instanceof NoseCone)) {
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
			
			sub.add(new UnitSelector(m), "growx");
			sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");
			
			
			////  Length:
			sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Length")));
			
			m = new DoubleModel(component, "ForeShoulderLength", UnitGroup.UNITS_LENGTH, 0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			sub.add(spin, "growx");
			
			sub.add(new UnitSelector(m), "growx");
			sub.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.2)), "w 100lp, wrap");
			
			
			////  Thickness:
			sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Thickness")));
			
			m = new DoubleModel(component, "ForeShoulderThickness", UnitGroup.UNITS_LENGTH, 0);
			m2 = new DoubleModel(component, "ForeShoulderRadius", UnitGroup.UNITS_LENGTH);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			sub.add(spin, "growx");
			
			sub.add(new UnitSelector(m), "growx");
			sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");
			
			
			////  Capped
			bm = new BooleanModel(component, "ForeShoulderCapped");
			check = new JCheckBox(bm);
			//// End capped
			check.setText(trans.get("RocketCompCfg.checkbox.Endcapped"));
			//// Whether the end of the shoulder is capped.
			check.setToolTipText(trans.get("RocketCompCfg.ttip.Endcapped"));
			sub.add(check, "spanx");
			
			
			panel.add(sub);
		}
		
		
		////  Aft shoulder
		sub = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		if (component instanceof NoseCone)
			//// Nose cone shoulder
			sub.setBorder(BorderFactory.createTitledBorder(trans.get("RocketCompCfg.title.Noseconeshoulder")));
		else
			//// Aft shoulder
			sub.setBorder(BorderFactory.createTitledBorder(trans.get("RocketCompCfg.title.Aftshoulder")));
		
		
		////  Radius
		//// Diameter:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Diameter")));
		
		m = new DoubleModel(component, "AftShoulderRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, "AftRadius", 2, UnitGroup.UNITS_LENGTH);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		
		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");
		
		
		////  Length:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Length")));
		
		m = new DoubleModel(component, "AftShoulderLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		
		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.2)), "w 100lp, wrap");
		
		
		////  Thickness:
		sub.add(new JLabel(trans.get("RocketCompCfg.lbl.Thickness")));
		
		m = new DoubleModel(component, "AftShoulderThickness", UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, "AftShoulderRadius", UnitGroup.UNITS_LENGTH);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		
		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");
		
		
		////  Capped
		bm = new BooleanModel(component, "AftShoulderCapped");
		check = new JCheckBox(bm);
		//// End capped
		check.setText(trans.get("RocketCompCfg.checkbox.Endcapped"));
		//// Whether the end of the shoulder is capped.
		check.setToolTipText(trans.get("RocketCompCfg.ttip.Endcapped"));
		sub.add(check, "spanx");
		
		
		panel.add(sub);
		
		
		return panel;
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
