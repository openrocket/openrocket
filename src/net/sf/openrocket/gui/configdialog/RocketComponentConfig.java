package net.sf.openrocket.gui.configdialog;


import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.ColorIcon;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.ExternalComponent.Finish;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.Invalidatable;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.Prefs;

public class RocketComponentConfig extends JPanel {
	
	protected final RocketComponent component;
	protected final JTabbedPane tabbedPane;
	
	private final List<Invalidatable> invalidatables = new ArrayList<Invalidatable>();
	

	protected final JTextField componentNameField;
	protected JTextArea commentTextArea;
	private final TextFieldListener textFieldListener;
	private JButton colorButton;
	private JCheckBox colorDefault;
	private JPanel buttonPanel;
	
	private JLabel massLabel;
	
	
	public RocketComponentConfig(RocketComponent component) {
		setLayout(new MigLayout("fill", "[grow, fill]"));
		this.component = component;
		
		JLabel label = new JLabel("Component name:");
		label.setToolTipText("The component name.");
		this.add(label, "split, gapright 10");
		
		componentNameField = new JTextField(15);
		textFieldListener = new TextFieldListener();
		componentNameField.addActionListener(textFieldListener);
		componentNameField.addFocusListener(textFieldListener);
		componentNameField.setToolTipText("The component name.");
		this.add(componentNameField, "growx, growy 0, wrap");
		

		tabbedPane = new JTabbedPane();
		this.add(tabbedPane, "growx, growy 1, wrap");
		
		tabbedPane.addTab("Override", null, overrideTab(), "Mass and CG override options");
		if (component.isMassive())
			tabbedPane.addTab("Figure", null, figureTab(), "Figure style options");
		tabbedPane.addTab("Comment", null, commentTab(), "Specify a comment for the component");
		
		addButtons();
		
		updateFields();
	}
	
	
	protected void addButtons(JButton... buttons) {
		if (buttonPanel != null) {
			this.remove(buttonPanel);
		}
		
		buttonPanel = new JPanel(new MigLayout("fill, ins 0"));
		
		massLabel = new StyledLabel("Mass: ", -1);
		buttonPanel.add(massLabel, "growx");
		
		for (JButton b : buttons) {
			buttonPanel.add(b, "right, gap para");
		}
		
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ComponentConfigDialog.hideDialog();
			}
		});
		buttonPanel.add(closeButton, "right, gap 30lp");
		
		updateFields();
		
		this.add(buttonPanel, "spanx, growx");
	}
	
	
	/**
	 * Called when a change occurs, so that the fields can be updated if necessary.
	 * When overriding this method, the supermethod must always be called.
	 */
	public void updateFields() {
		// Component name
		componentNameField.setText(component.getName());
		
		// Component color and "Use default color" checkbox
		if (colorButton != null && colorDefault != null) {
			colorButton.setIcon(new ColorIcon(getColor()));
			
			if ((component.getColor() == null) != colorDefault.isSelected())
				colorDefault.setSelected(component.getColor() == null);
		}
		
		// Mass label
		if (component.isMassive()) {
			String text = "Component mass: ";
			text += UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(
					component.getComponentMass());
			
			String overridetext = null;
			if (component.isMassOverridden()) {
				overridetext = "(overridden to " + UnitGroup.UNITS_MASS.getDefaultUnit().
						toStringUnit(component.getOverrideMass()) + ")";
			}
			
			for (RocketComponent c = component.getParent(); c != null; c = c.getParent()) {
				if (c.isMassOverridden() && c.getOverrideSubcomponents()) {
					overridetext = "(overridden by " + c.getName() + ")";
				}
			}
			
			if (overridetext != null)
				text = text + " " + overridetext;
			
			massLabel.setText(text);
		} else {
			massLabel.setText("");
		}
	}
	
	
	protected JPanel materialPanel(JPanel panel, Material.Type type) {
		return materialPanel(panel, type, "Component material:", "Component finish:");
	}
	
	protected JPanel materialPanel(JPanel panel, Material.Type type,
			String materialString, String finishString) {
		JLabel label = new JLabel(materialString);
		label.setToolTipText("The component material affects the weight of the component.");
		panel.add(label, "spanx 4, wrap rel");
		
		JComboBox combo = new JComboBox(new MaterialModel(panel, component, type));
		combo.setToolTipText("The component material affects the weight of the component.");
		panel.add(combo, "spanx 4, growx, wrap paragraph");
		

		if (component instanceof ExternalComponent) {
			label = new JLabel(finishString);
			String tip = "<html>The component finish affects the aerodynamic drag of the "
					+ "component.<br>"
					+ "The value indicated is the average roughness height of the surface.";
			label.setToolTipText(tip);
			panel.add(label, "spanx 4, wmin 220lp, wrap rel");
			
			combo = new JComboBox(new EnumModel<ExternalComponent.Finish>(component, "Finish"));
			combo.setToolTipText(tip);
			panel.add(combo, "spanx 4, growx, split");
			
			JButton button = new JButton("Set for all");
			button.setToolTipText("Set this finish for all components of the rocket.");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Finish f = ((ExternalComponent) component).getFinish();
					Rocket rocket = component.getRocket();
					try {
						rocket.freeze();
						// Store previous undo description
						String desc = ComponentConfigDialog.getUndoDescription();
						ComponentConfigDialog.addUndoPosition("Set rocket finish");
						// Do changes
						Iterator<RocketComponent> iter = rocket.iterator();
						while (iter.hasNext()) {
							RocketComponent c = iter.next();
							if (c instanceof ExternalComponent) {
								((ExternalComponent) c).setFinish(f);
							}
						}
						// Restore undo description
						ComponentConfigDialog.addUndoPosition(desc);
					} finally {
						rocket.thaw();
					}
				}
			});
			panel.add(button, "wrap paragraph");
		}
		
		return panel;
	}
	
	
	private JPanel overrideTab() {
		JPanel panel = new JPanel(new MigLayout("align 50% 20%, fillx, gap rel unrel",
				"[][65lp::][30lp::][]", ""));
		
		panel.add(new StyledLabel("Override the mass or center of gravity of the " +
				component.getComponentName() + ":", Style.BOLD), "spanx, wrap 20lp");
		
		JCheckBox check;
		BooleanModel bm;
		UnitSelector us;
		BasicSlider bs;
		
		////  Mass
		bm = new BooleanModel(component, "MassOverridden");
		check = new JCheckBox(bm);
		check.setText("Override mass:");
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
		

		////  CG override
		bm = new BooleanModel(component, "CGOverridden");
		check = new JCheckBox(bm);
		check.setText("Override center of gravity:");
		panel.add(check, "growx 1, gapright 20lp");
		
		m = new DoubleModel(component, "OverrideCGX", UnitGroup.UNITS_LENGTH, 0);
		// Calculate suitable length for slider
		DoubleModel length;
		if (component instanceof ComponentAssembly) {
			double l = 0;
			
			Iterator<RocketComponent> iterator = component.iterator(false);
			while (iterator.hasNext()) {
				RocketComponent c = iterator.next();
				if (c.getRelativePosition() == RocketComponent.Position.AFTER)
					l += c.getLength();
			}
			length = new DoubleModel(l);
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
		panel.add(bs, "growx 5, w 100lp, wrap 35lp");
		

		// Override subcomponents checkbox
		bm = new BooleanModel(component, "OverrideSubcomponents");
		check = new JCheckBox(bm);
		check.setText("Override mass and CG of all subcomponents");
		panel.add(check, "gap para, spanx, wrap para");
		

		panel.add(new StyledLabel("<html>The overridden mass does not include motors.<br>" +
				"The center of gravity is measured from the front end of the " +
				component.getComponentName().toLowerCase() + ".", -1),
				"spanx, wrap, gap para, height 0::30lp");
		
		return panel;
	}
	
	
	private JPanel commentTab() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new StyledLabel("Comments on the " + component.getComponentName() + ":",
				Style.BOLD), "wrap");
		
		// TODO: LOW:  Changes in comment from other sources not reflected in component
		commentTextArea = new JTextArea(component.getComment());
		commentTextArea.setLineWrap(true);
		commentTextArea.setWrapStyleWord(true);
		commentTextArea.setEditable(true);
		GUIUtil.setTabToFocusing(commentTextArea);
		commentTextArea.addFocusListener(textFieldListener);
		
		panel.add(new JScrollPane(commentTextArea), "width 10px, height 10px, growx, growy");
		
		return panel;
	}
	
	

	private JPanel figureTab() {
		JPanel panel = new JPanel(new MigLayout("align 20% 20%"));
		
		panel.add(new StyledLabel("Figure style:", Style.BOLD), "wrap para");
		

		panel.add(new JLabel("Component color:"), "gapleft para, gapright 10lp");
		
		colorButton = new JButton(new ColorIcon(getColor()));
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = component.getColor();
				if (c == null) {
					c = Prefs.getDefaultColor(component.getClass());
				}
				
				c = JColorChooser.showDialog(tabbedPane, "Choose color", c);
				if (c != null) {
					component.setColor(c);
				}
			}
		});
		panel.add(colorButton, "gapright 10lp");
		
		colorDefault = new JCheckBox("Use default color");
		if (component.getColor() == null)
			colorDefault.setSelected(true);
		colorDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (colorDefault.isSelected())
					component.setColor(null);
				else
					component.setColor(Prefs.getDefaultColor(component.getClass()));
			}
		});
		panel.add(colorDefault, "wrap para");
		

		panel.add(new JLabel("Component line style:"), "gapleft para, gapright 10lp");
		
		LineStyle[] list = new LineStyle[LineStyle.values().length + 1];
		System.arraycopy(LineStyle.values(), 0, list, 1, LineStyle.values().length);
		
		JComboBox combo = new JComboBox(new EnumModel<LineStyle>(component, "LineStyle",
				list, "Default style"));
		panel.add(combo, "spanx 2, growx, wrap 50lp");
		

		JButton button = new JButton("Save as default style");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (component.getColor() != null) {
					Prefs.setDefaultColor(component.getClass(), component.getColor());
					component.setColor(null);
				}
				if (component.getLineStyle() != null) {
					Prefs.setDefaultLineStyle(component.getClass(), component.getLineStyle());
					component.setLineStyle(null);
				}
			}
		});
		panel.add(button, "gapleft para, spanx 3, growx, wrap");
		
		return panel;
	}
	
	
	private Color getColor() {
		Color c = component.getColor();
		if (c == null) {
			c = Prefs.getDefaultColor(component.getClass());
		}
		return c;
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
			
			sub.setBorder(BorderFactory.createTitledBorder("Fore shoulder"));
			

			////  Radius
			sub.add(new JLabel("Diameter:"));
			
			m = new DoubleModel(component, "ForeShoulderRadius", 2, UnitGroup.UNITS_LENGTH, 0);
			m2 = new DoubleModel(component, "ForeRadius", 2, UnitGroup.UNITS_LENGTH);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			sub.add(spin, "growx");
			
			sub.add(new UnitSelector(m), "growx");
			sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");
			

			////  Length
			sub.add(new JLabel("Length:"));
			
			m = new DoubleModel(component, "ForeShoulderLength", UnitGroup.UNITS_LENGTH, 0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			sub.add(spin, "growx");
			
			sub.add(new UnitSelector(m), "growx");
			sub.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.2)), "w 100lp, wrap");
			

			////  Thickness
			sub.add(new JLabel("Thickness:"));
			
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
			check.setText("End capped");
			check.setToolTipText("Whether the end of the shoulder is capped.");
			sub.add(check, "spanx");
			

			panel.add(sub);
		}
		

		////  Aft shoulder
		sub = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		
		if (component instanceof NoseCone)
			sub.setBorder(BorderFactory.createTitledBorder("Nose cone shoulder"));
		else
			sub.setBorder(BorderFactory.createTitledBorder("Aft shoulder"));
		

		////  Radius
		sub.add(new JLabel("Diameter:"));
		
		m = new DoubleModel(component, "AftShoulderRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		m2 = new DoubleModel(component, "AftRadius", 2, UnitGroup.UNITS_LENGTH);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		
		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(m0, m2)), "w 100lp, wrap");
		

		////  Length
		sub.add(new JLabel("Length:"));
		
		m = new DoubleModel(component, "AftShoulderLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		sub.add(spin, "growx");
		
		sub.add(new UnitSelector(m), "growx");
		sub.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.2)), "w 100lp, wrap");
		

		////  Thickness
		sub.add(new JLabel("Thickness:"));
		
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
		check.setText("End capped");
		check.setToolTipText("Whether the end of the shoulder is capped.");
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
	
	public void invalidateModels() {
		for (Invalidatable i : invalidatables) {
			i.invalidate();
		}
	}
	
}