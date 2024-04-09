package info.openrocket.swing.gui.configdialog;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.ClusterConfiguration;
import info.openrocket.core.rocketcomponent.Clusterable;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.StateChangeListener;

import info.openrocket.swing.gui.Resettable;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SelectColorButton;



public class InnerTubeConfig extends RocketComponentConfig {
	private static final long serialVersionUID = 7900041420864324470L;
	private static final Translator trans = Application.getTranslator();
	private static final Preferences prefs = Application.getPreferences();

	private static final String PREF_SEPARATION_RELATIVE = "InnerTubeSeparationRelative";


	public InnerTubeConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);

		JPanel mainPanel = new JPanel(new MigLayout());

		//// Left panel
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]"));

		DoubleModel m;
		JSpinner spin;
		DoubleModel od;

		//// ---------------------------- Attributes ----------------------------

		////  Length
		panel.add(new JLabel(trans.get("ThicknessRingCompCfg.tab.Length")));
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		register(m);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		focusElement = spin;
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 100lp, wrap");

		//// Outer diameter
		panel.add(new JLabel(trans.get("ThicknessRingCompCfg.tab.Outerdiam")));

		//// OuterRadius
		od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		register(od);

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");

		if (od.isAutomaticAvailable()) {
			JCheckBox check = new JCheckBox(od.getAutomaticAction());
			//// Automatic
			check.setText(trans.get("ringcompcfg.Automatic"));
			panel.add(check, "skip, span 2, wrap");
		}

		////  Inner diameter
		panel.add(new JLabel(trans.get("ThicknessRingCompCfg.tab.Innerdiam")));

		//// InnerRadius
		m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		register(m);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap");

		if (m.isAutomaticAvailable()) {
			JCheckBox check = new JCheckBox(m.getAutomaticAction());
			//// Automatic
			check.setText(trans.get("ringcompcfg.Automatic"));
			panel.add(check, "skip, span 2, wrap");
		}


		////  Wall thickness
		panel.add(new JLabel(trans.get("ThicknessRingCompCfg.tab.Wallthickness")));

		//// Thickness
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		register(m);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap");

		mainPanel.add(panel, "aligny 0, gapright 40lp");

		//// Right side of panel ----
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]"));

		//// ---------------------------- Placement ----------------------------
		PlacementPanel pp = new PlacementPanel(component, order);
		register(pp);
		panel.add(pp, "span, grow");

		//// Material
		MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
		register(materialPanel);
		panel.add(materialPanel, "span, grow, wrap");

		mainPanel.add(panel, "aligny 0");

		tabbedPane.insertTab(trans.get("ThicknessRingCompCfg.tab.General"), null, mainPanel,
				trans.get("ThicknessRingCompCfg.tab.Generalprop"), 0);

		MotorConfig motorConfig = new MotorConfig((MotorMount)c, order);
		register(motorConfig);

		tabbedPane.insertTab(trans.get("InnerTubeCfg.tab.Motor"), null, motorConfig,
				trans.get("InnerTubeCfg.tab.ttip.Motor"), 1);

		JPanel tab = clusterTab();
		//// Cluster and Cluster configuration
		tabbedPane.insertTab(trans.get("InnerTubeCfg.tab.Cluster"), null, tab,
				trans.get("InnerTubeCfg.tab.ttip.Cluster"), 2);

		tab = positionTab();
		//// Radial position
		tabbedPane.insertTab(trans.get("InnerTubeCfg.tab.Radialpos"), null, tab,
				trans.get("InnerTubeCfg.tab.ttip.Radialpos"), 3);

		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this config dialog
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}

	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 35",
				"[][65lp::][30lp::]", ""));

		////  Radial position
		JLabel l = new JLabel(trans.get("ringcompcfg.Radialdistance"));
		//// Distance from the rocket centerline
		l.setToolTipText(trans.get("ringcompcfg.Distancefrom"));
		panel.add(l);

		DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		register(m);

		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		//// Distance from the rocket centerline
		spin.setToolTipText(trans.get("ringcompcfg.Distancefrom"));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		BasicSlider bs = new BasicSlider(m.getSliderModel(0, 0.1, 1.0));
		//// Distance from the rocket centerline
		bs.setToolTipText(trans.get("ringcompcfg.Distancefrom"));
		panel.add(bs, "w 130lp, wrap");


		//// Radial direction
		l = new JLabel(trans.get("ringcompcfg.Radialdirection"));
		//// The radial direction from the rocket centerline
		l.setToolTipText(trans.get("ringcompcfg.radialdirectionfrom"));
		panel.add(l);

		m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
		register(m);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		//// The radial direction from the rocket centerline
		spin.setToolTipText(trans.get("ringcompcfg.radialdirectionfrom"));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		panel.add(new UnitSelector(m), "growx");
		bs = new BasicSlider(m.getSliderModel(-Math.PI, Math.PI));
		//// The radial direction from the rocket centerline
		bs.setToolTipText(trans.get("ringcompcfg.radialdirectionfrom"));
		panel.add(bs, "w 130lp, wrap");


		//// Reset button
		JButton button = new SelectColorButton(trans.get("ringcompcfg.but.Reset"));
		//// Reset the component to the rocket centerline
		button.setToolTipText(trans.get("ringcompcfg.but.Resetcomponant"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((RingComponent) component).setRadialDirection(0.0);
				((RingComponent) component).setRadialPosition(0.0);
			}
		});
		panel.add(button, "spanx, right, wrap para");
		order.add(button);


		DescriptionArea note = new DescriptionArea(4);
		//// Note: An inner tube will not affect the aerodynamics of the rocket even if located outside the body tube.
		note.setText(trans.get("ringcompcfg.note.desc"));
		panel.add(note, "spanx, growx");

		return panel;
	}


	private JPanel clusterTab() {
		JPanel panel = new JPanel(new MigLayout());

		JPanel subPanel = new JPanel(new MigLayout());

		// Cluster type selection
		//// Select cluster configuration:
		subPanel.add(new JLabel(trans.get("InnerTubeCfg.lbl.Selectclustercfg")), "spanx, wrap");
		subPanel.add(new ClusterSelectionPanel((InnerTube) component), "spanx, wrap");
		//		JPanel clusterSelection = new ClusterSelectionPanel((InnerTube)component);
		//		clusterSelection.setBackground(ORColor.blue);
		//		subPanel.add(clusterSelection);

		panel.add(subPanel);


		subPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]"));

		// Tube separation scale
		//// Tube separation:
		JLabel l = new JLabel(trans.get("InnerTubeCfg.lbl.TubeSep"));
		//// The separation of the tubes, 1.0 = touching each other
		l.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.TubeSep"));
		subPanel.add(l);

		//// Models
		final boolean useRelativeSeparation = prefs.getBoolean(PREF_SEPARATION_RELATIVE, true);
		final DoubleModel clusterScaleModelRel = new DoubleModel(component, "ClusterScale", 1, UnitGroup.UNITS_NONE, 0);
		final DoubleModel clusterScaleModelAbs = new DoubleModel(component, "ClusterScaleAbsolute", 1, UnitGroup.UNITS_LENGTH);
		final DoubleModel clusterScaleModel = useRelativeSeparation ? clusterScaleModelRel : clusterScaleModelAbs;

		register(clusterScaleModelRel);
		register(clusterScaleModelAbs);
		register(clusterScaleModel);

		final String clusterScaleTtipRel = trans.get("InnerTubeCfg.lbl.ttip.TubeSep");
		final String clusterScaleTtipAbs = trans.get("InnerTubeCfg.lbl.ttip.TubeSepAbs");
		final String clusterScaleTtip = useRelativeSeparation ? clusterScaleTtipRel : clusterScaleTtipAbs;

		JSpinner clusterScaleSpin = new JSpinner(clusterScaleModel.getSpinnerModel());
		clusterScaleSpin.setEditor(new SpinnerEditor(clusterScaleSpin));
		clusterScaleSpin.setToolTipText(clusterScaleTtip);
		subPanel.add(clusterScaleSpin, "growx");
		order.add(((SpinnerEditor) clusterScaleSpin.getEditor()).getTextField());

		UnitSelector clusterScaleUnit = new UnitSelector(clusterScaleModel);
		subPanel.add(clusterScaleUnit, "growx");

		BasicSlider clusterScaleBs = new BasicSlider(clusterScaleModel.getSliderModel(0, 1, 4));
		subPanel.add(clusterScaleBs, "w 100lp, wrap");

		// Relative/absolute separation
		JRadioButton rbRel = new JRadioButton(trans.get("InnerTubeCfg.radioBut.Relative"));
		JRadioButton rbAbs = new JRadioButton(trans.get("InnerTubeCfg.radioBut.Absolute"));
		rbRel.setToolTipText(trans.get("InnerTubeCfg.radioBut.Relative.ttip"));
		rbAbs.setToolTipText(trans.get("InnerTubeCfg.radioBut.Absolute.ttip"));
		ButtonGroup bg = new ButtonGroup();
		bg.add(rbRel);
		bg.add(rbAbs);
		subPanel.add(rbRel, "skip, spanx, split 2");
		subPanel.add(rbAbs, "wrap");

		rbRel.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED)
					return;
				clusterScaleSpin.setModel(clusterScaleModelRel.getSpinnerModel());
				clusterScaleSpin.setEditor(new SpinnerEditor(clusterScaleSpin));
				clusterScaleUnit.setModel(clusterScaleModelRel);
				clusterScaleBs.setModel(clusterScaleModelRel.getSliderModel(0, 1, 4));
				clusterScaleSpin.setToolTipText(clusterScaleTtipRel);

				prefs.putBoolean(PREF_SEPARATION_RELATIVE, false);
			}
		});
		rbAbs.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.DESELECTED)
					return;
				DoubleModel radiusModelMin = new DoubleModel(component, "OuterRadius", -2, UnitGroup.UNITS_LENGTH);
				DoubleModel radiusModelMax = new DoubleModel(component, "OuterRadius", 6, UnitGroup.UNITS_LENGTH);

				clusterScaleSpin.setModel(clusterScaleModelAbs.getSpinnerModel());
				clusterScaleSpin.setEditor(new SpinnerEditor(clusterScaleSpin));
				clusterScaleUnit.setModel(clusterScaleModelAbs);
				clusterScaleBs.setModel(clusterScaleModelAbs.getSliderModel(radiusModelMin, radiusModelMax));
				clusterScaleSpin.setToolTipText(clusterScaleTtipAbs);

				prefs.putBoolean(PREF_SEPARATION_RELATIVE, false);
			}
		});

		// Select the button by default
		if (prefs.getBoolean(PREF_SEPARATION_RELATIVE, true)) {
			rbRel.setSelected(true);
		} else {
			rbAbs.setSelected(true);
		}

		// Rotation:
		l = new JLabel(trans.get("InnerTubeCfg.lbl.Rotation"));
		//// Rotation angle of the cluster configuration
		l.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.Rotation"));
		subPanel.add(l);
		DoubleModel dm = new DoubleModel(component, "ClusterRotation", 1, UnitGroup.UNITS_ANGLE,
				-Math.PI, Math.PI);
		register(dm);

		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		//// Rotation angle of the cluster configuration
		spin.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.Rotation"));
		subPanel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

		subPanel.add(new UnitSelector(dm), "growx");
		BasicSlider bs = new BasicSlider(dm.getSliderModel());
		//// Rotation angle of the cluster configuration
		bs.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.Rotation"));
		subPanel.add(bs, "w 100lp, wrap para");



		// Split button
		//// Split cluster
		JButton split = new SelectColorButton(trans.get("InnerTubeCfg.but.Splitcluster"));
		//// <html>Split the cluster into separate components.<br>
		//// This also duplicates all components attached to this inner tube.
		split.setToolTipText(trans.get("InnerTubeCfg.lbl.longA1") +
				trans.get("InnerTubeCfg.lbl.longA2"));
		split.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Do change in future for overall safety
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						document.addUndoPosition("Split cluster");

						List<RocketComponent> listeners = new ArrayList<>(component.getConfigListeners());
						splitAction(component);
						for (RocketComponent listener : listeners) {
							if (listener instanceof InnerTube) {
								splitAction(listener);
							}
						}
					}

					private void splitAction(RocketComponent component) {
						RocketComponent parent = component.getParent();
						int index = parent.getChildPosition(component);
						if (index < 0) {
							throw new BugException("Inconsistent state: component=" + component +
									" parent=" + parent + " parent.children=" + parent.getChildren());
						}

						InnerTube tube = (InnerTube) component;
						if (tube.getInstanceCount() <= 1)
							return;

						Coordinate[] coords = component.getComponentLocations();
						parent.removeChild(index);
						for (int i = 0; i < coords.length; i++) {
							InnerTube copy = InnerTube.makeIndividualClusterComponent(coords[i], component.getName() + " #" + (i + 1), component);

							parent.addChild(copy, index + i);
						}
					}
				});
			}
		});
		subPanel.add(split, "spanx, split 2, gapright para, sizegroup buttons, right");
		order.add(split);

		// Reset button
		///// Reset settings
		JButton reset = new SelectColorButton(trans.get("InnerTubeCfg.but.Resetsettings"));
		//// Reset the separation and rotation to the default values
		reset.setToolTipText(trans.get("InnerTubeCfg.but.ttip.Resetsettings"));
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				((InnerTube) component).setClusterScale(1.0);
				((InnerTube) component).setClusterRotation(0.0);
			}
		});
		subPanel.add(reset, "sizegroup buttons, right");
		order.add(reset);

		panel.add(subPanel, "grow");

		return panel;
	}

}


class ClusterSelectionPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1804786106133398810L;
	private static final int BUTTON_SIZE = 50;
	private static final int MOTOR_DIAMETER = 10;

	private static final Color SELECTED_COLOR;
	private static Color UNSELECTED_COLOR;
	private static final Color MOTOR_FILL_COLOR;
	private static final Color MOTOR_BORDER_COLOR;

	static {
		SELECTED_COLOR = Color.RED;
		MOTOR_FILL_COLOR = Color.GREEN;
		MOTOR_BORDER_COLOR = Color.BLACK;
		initColors();
	}

	public ClusterSelectionPanel(Clusterable component) {
		super(new MigLayout("gap 0 0",
				"[" + BUTTON_SIZE + "!][" + BUTTON_SIZE + "!][" + BUTTON_SIZE + "!][" + BUTTON_SIZE + "!]",
				"[" + BUTTON_SIZE + "!][" + BUTTON_SIZE + "!][" + BUTTON_SIZE + "!]"));

		for (int i = 0; i < ClusterConfiguration.CONFIGURATIONS.length; i++) {
			ClusterConfiguration config = ClusterConfiguration.CONFIGURATIONS[i];

			JComponent button = new ClusterButton(component, config);
			if (i % 4 == 3)
				add(button, "wrap");
			else
				add(button);
		}

	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(ClusterSelectionPanel::updateColors);
	}

	private static void updateColors() {
		UNSELECTED_COLOR = GUIUtil.getUITheme().getBackgroundColor();
	}


	private class ClusterButton extends JPanel implements StateChangeListener, MouseListener,
	Resettable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3626386642481889629L;
		private Clusterable component;
		private ClusterConfiguration config;

		public ClusterButton(Clusterable c, ClusterConfiguration config) {
			component = c;
			this.config = config;
			setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			//			setBorder(BorderFactory.createLineBorder(ORColor.BLACK, 1));
			setToolTipText(config.getXMLName());
			component.addChangeListener(this);
			addMouseListener(this);
		}


		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			Rectangle area = g2.getClipBounds();

			if (component.getClusterConfiguration() == config)
				g2.setColor(SELECTED_COLOR);
			else
				g2.setColor(UNSELECTED_COLOR);

			g2.fillRect(area.x, area.y, area.width, area.height);

			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_NORMALIZE);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			List<Double> points = config.getPoints();
			Ellipse2D.Float circle = new Ellipse2D.Float();
			for (int i = 0; i < points.size() / 2; i++) {
				double x = points.get(i * 2);
				double y = points.get(i * 2 + 1);

				double px = BUTTON_SIZE / 2 + x * MOTOR_DIAMETER;
				double py = BUTTON_SIZE / 2 - y * MOTOR_DIAMETER;
				circle.setFrameFromCenter(px, py, px + MOTOR_DIAMETER / 2, py + MOTOR_DIAMETER / 2);

				g2.setColor(MOTOR_FILL_COLOR);
				g2.fill(circle);
				g2.setColor(MOTOR_BORDER_COLOR);
				g2.draw(circle);
			}
		}


		@Override
		public void stateChanged(EventObject e) {
			repaint();
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				component.setClusterConfiguration(config);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}


		@Override
		public void resetModel() {
			component.removeChangeListener(this);
			removeMouseListener(this);
		}
	}

}
