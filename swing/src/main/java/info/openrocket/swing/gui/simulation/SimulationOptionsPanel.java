package info.openrocket.swing.gui.simulation;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.RK4SimulationStepper;
import info.openrocket.core.simulation.SimulationOptions;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.simulation.extension.SimulationExtensionProvider;
import info.openrocket.core.startup.Application;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.GeodeticComputationStrategy;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.simulation.extension.SwingSimulationExtensionConfigurator;

import com.google.inject.Key;

class SimulationOptionsPanel extends JPanel {

	private static final long serialVersionUID = -5251458539346201239L;

	private static final Translator trans = Application.getTranslator();
	
	private OpenRocketDocument document;
	final Simulation simulation;
	
	private JPanel currentExtensions;
	final JPopupMenu extensionMenu;
	JMenu extensionMenuCopyExtension;

	private static Color textColor;
	private static Color dimTextColor;

	static {
		initColors();
	}
	
	SimulationOptionsPanel(OpenRocketDocument document, final Simulation simulation) {
		super(new MigLayout("fill"));
		this.document = document;
		this.simulation = simulation;

		final SimulationOptions conditions = simulation.getOptions();
		
		JPanel sub, subsub;
		String tip;
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		UnitSelector unit;
		BasicSlider slider;
		
		// // Simulation options
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		// // Simulator options
		sub.setBorder(BorderFactory.createTitledBorder(trans
				.get("simedtdlg.border.Simopt")));
		this.add(sub, "growx, growy, aligny 0");
		
		// Separate panel for computation methods, as they use a different
		// layout
		subsub = new JPanel(new MigLayout("insets 0, fill", "[grow][min!][min!][]"));
		
		// // Calculation method:
		tip = trans.get("simedtdlg.lbl.ttip.Calcmethod");
		label = new JLabel(trans.get("simedtdlg.lbl.Calcmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		// // Extended Barrowman
		label = new JLabel(trans.get("simedtdlg.lbl.ExtBarrowman"));
		label.setToolTipText(tip);
		subsub.add(label, "growx, span 3, wrap");
		
		// Simulation method
		tip = trans.get("simedtdlg.lbl.ttip.Simmethod1")
				+ trans.get("simedtdlg.lbl.ttip.Simmethod2");
		label = new JLabel(trans.get("simedtdlg.lbl.Simmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		label = new JLabel("6-DOF Runge-Kutta 4");
		label.setToolTipText(tip);
		subsub.add(label, "growx, span 3, wrap");
		
		// // Geodetic calculation method:
		label = new JLabel(trans.get("simedtdlg.lbl.GeodeticMethod"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.GeodeticMethodTip"));
		subsub.add(label, "gapright para");
		
		EnumModel<GeodeticComputationStrategy> gcsModel = new EnumModel<>(
				conditions, "GeodeticComputation");
		final JComboBox<GeodeticComputationStrategy> gcsCombo = new JComboBox<>(gcsModel);
		ActionListener gcsTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeodeticComputationStrategy gcs = (GeodeticComputationStrategy) gcsCombo
						.getSelectedItem();
				gcsCombo.setToolTipText(gcs.getDescription());
			}
		};
		gcsCombo.addActionListener(gcsTTipListener);
		gcsTTipListener.actionPerformed(null);
		subsub.add(gcsCombo, "span 3, wrap para");
		
		
		// // Time step:
		label = new JLabel(trans.get("simedtdlg.lbl.Timestep"));
		tip = trans.get("simedtdlg.lbl.ttip.Timestep1")
				+ trans.get("simedtdlg.lbl.ttip.Timestep2")
				+ " "
				+ UnitGroup.UNITS_TIME_STEP
						.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP)
				+ ".";
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		m = new DoubleModel(conditions, "TimeStep", UnitGroup.UNITS_TIME_STEP,
				0.01, 1);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		subsub.add(spin, "");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		subsub.add(unit, "");
		slider = new BasicSlider(m.getSliderModel(0.01, 0.2));
		slider.setToolTipText(tip);
		subsub.add(slider, "w 100, wrap");

		// // Maximum simulation time:
		label = new JLabel(trans.get("simedtdlg.lbl.MaxSimTime"));
		tip = trans.get("simedtdlg.lbl.ttip.MaxSimTime");
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");

		m = new DoubleModel(conditions, "MaxSimulationTime",
				UnitGroup.UNITS_LONG_TIME, 1);

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		subsub.add(spin, "");

		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		subsub.add(unit, "wrap");

		
		sub.add(subsub, "spanx, wrap para");
		
		// Reset to default button
		JButton resetBtn = new JButton(trans.get("simedtdlg.but.resettodefault"));
		// Reset the time step to its default value (
		resetBtn.setToolTipText(trans.get("simedtdlg.but.ttip.resettodefault")
				+ UnitGroup.UNITS_SHORT_TIME
						.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP)
				+ ").");
		resetBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ApplicationPreferences preferences = Application.getPreferences();
				conditions.setTimeStep(preferences.getDouble(
						ApplicationPreferences.SIMULATION_TIME_STEP,
						RK4SimulationStepper.RECOMMENDED_TIME_STEP));
				conditions.setMaxSimulationTime(preferences.getDouble(
						ApplicationPreferences.SIMULATION_MAX_TIME,
						RK4SimulationStepper.RECOMMENDED_MAX_TIME));
				conditions.setGeodeticComputation(preferences.getEnum(
						ApplicationPreferences.GEODETIC_COMPUTATION,
						GeodeticComputationStrategy.SPHERICAL));
			}
		});

		// Save as default button
		JButton saveBtn = new JButton(trans.get("simedtdlg.but.savedefault"));
		saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ApplicationPreferences preferences = Application.getPreferences();
				preferences.setTimeStep(conditions.getTimeStep());
				preferences.setMaxSimulationTime(conditions.getMaxSimulationTime());
				preferences.setGeodeticComputation(conditions.getGeodeticComputation());
			}
		});
		
		sub.add(resetBtn, "align left, split 2");
		sub.add(saveBtn, "wrap");
		
		
		
		//// Simulation extensions
		sub = new JPanel(new MigLayout("fill, gap 0 0"));
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.SimExt")));
		this.add(sub, "wmin 300lp, growx, growy");
		
		
		DescriptionArea desc = new DescriptionArea(5);
		desc.setText(trans.get("simedtdlg.SimExt.desc"));
		sub.add(desc, "aligny 0, hmin 100lp, growx, wrap para");
		
		
		final JButton addExtension = new JButton(trans.get("simedtdlg.SimExt.add"));
		extensionMenu = getExtensionMenu();
		addExtension.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					extensionMenu.show(addExtension, 5, addExtension.getBounds().height);
				}
			});
		sub.add(addExtension, "growx, wrap 0");
		
		currentExtensions = new JPanel(new MigLayout("fillx, gap 0 0, ins 0"));
		JScrollPane scroll = new JScrollPane(currentExtensions);
		scroll.setForeground(textColor);
		//  &#$%! scroll pane will not honor "growy"...
		sub.add(scroll, "growx, growy, h 100%");
		
		updateCurrentExtensions();
		
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationOptionsPanel::updateColors);
	}

	public static void updateColors() {
		textColor = GUIUtil.getUITheme().getTextColor();
		dimTextColor = GUIUtil.getUITheme().getDimTextColor();
	}
	
	private JPopupMenu getExtensionMenu() {
		Set<SimulationExtensionProvider> extensions = Application.getInjector().getInstance(new Key<>() {
		});
		
		JPopupMenu basemenu = new JPopupMenu();

		//// Use code / Launch conditions
		for (final SimulationExtensionProvider provider : extensions) {
			List<String> ids = provider.getIds();
			for (final String id : ids) {
				List<String> menuItems = provider.getName(id);
				if (menuItems != null) {
					JComponent menu = findMenu(basemenu, menuItems);
					JMenuItem item = new JMenuItem(menuItems.get(menuItems.size() - 1));
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							SimulationExtension e = provider.getInstance(id);
							simulation.getSimulationExtensions().add(e);
							updateCurrentExtensions();
							SwingSimulationExtensionConfigurator configurator = findConfigurator(e);
							if (configurator != null) {
								configurator.configure(e, simulation, SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
								updateCurrentExtensions();
							}
						}
					});
					menu.add(item);
				}
			}
		}

		//// Copy extension
		updateExtensionMenuCopyExtension(basemenu);
		
		return basemenu;
	}

	/**
	 * Updates the contents of the "Copy extension" menu item in the extension menu.
	 * @param extensionMenu extension menu to add the "Copy extension" menu item to
	 */
	private void updateExtensionMenuCopyExtension(JPopupMenu extensionMenu) {
		if (extensionMenu == null) {
			return;
		}
		if (this.extensionMenuCopyExtension != null) {
			extensionMenu.remove(this.extensionMenuCopyExtension);
		}

		this.extensionMenuCopyExtension = null;
		for (Simulation sim : document.getSimulations()) {
			if (sim.getSimulationExtensions().isEmpty()) {
				continue;
			}

			JMenu menu = new JMenu(sim.getName());
			for (final SimulationExtension ext : sim.getSimulationExtensions()) {
				JMenuItem item = new JMenuItem(ext.getName());
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						SimulationExtension e = ext.clone();
						simulation.getSimulationExtensions().add(e);
						updateCurrentExtensions();
						SwingSimulationExtensionConfigurator configurator = findConfigurator(e);
						if (configurator != null) {
							configurator.configure(e, simulation, SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
              updateCurrentExtensions();
						}
					}
				});
				menu.add(item);
			}

			if (this.extensionMenuCopyExtension == null) {
				this.extensionMenuCopyExtension = new JMenu(trans.get("simedtdlg.SimExt.copyExtension"));
			}
			this.extensionMenuCopyExtension.add(menu);
		}
		if (this.extensionMenuCopyExtension != null) {
			extensionMenu.add(this.extensionMenuCopyExtension);
		}
	}
	
	private JComponent findMenu(MenuElement menu, List<String> menuItems) {
		for (int i = 0; i < menuItems.size() - 1; i++) {
			String menuItem = menuItems.get(i);
			
			MenuElement found = null;
			for (MenuElement e : menu.getSubElements()) {
				if (e instanceof JMenu && ((JMenu) e).getText().equals(menuItem)) {
					found = e;
					break;
				}
			}
			
			if (found != null) {
				menu = found;
			} else {
				JMenu m = new JMenu(menuItem);
				((JComponent) menu).add(m);
				menu = m;
			}
		}
		return (JComponent) menu;
	}
	
	
	private void updateCurrentExtensions() {
		currentExtensions.removeAll();
		
		if (simulation.getSimulationExtensions().isEmpty()) {
			StyledLabel l = new StyledLabel(trans.get("simedtdlg.SimExt.noExtensions"), Style.ITALIC);
			l.setForeground(dimTextColor);
			currentExtensions.add(l, "growx, pad 5 5 5 5, wrap");
		} else {
			for (SimulationExtension e : simulation.getSimulationExtensions()) {
				currentExtensions.add(new SimulationExtensionPanel(e), "growx, wrap");
			}
		}

		updateExtensionMenuCopyExtension(this.extensionMenu);

		// Both needed:
		this.revalidate();
		this.repaint();
	}
	
	
	private class SimulationExtensionPanel extends JPanel {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3296795614810745035L;

		public SimulationExtensionPanel(final SimulationExtension extension) {
			super(new MigLayout("fillx, gapx 0"));
			
			this.setBorder(BorderFactory.createLineBorder(dimTextColor));
			this.add(new JLabel(extension.getName()), "spanx, growx, wrap");
			
			JButton button;
			
			this.add(new JPanel(), "spanx, split, growx, right");

			// Configure
			if (findConfigurator(extension) != null) {
				button = new JButton(Icons.CONFIGURE);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						findConfigurator(extension).configure(extension, simulation,
								SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
						updateCurrentExtensions();
					}
				});
				this.add(button, "right");
			}

			// Help
			if (extension.getDescription() != null) {
				button = new JButton(Icons.HELP);
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						final JDialog dialog = new JDialog(SwingUtilities.windowForComponent(SimulationOptionsPanel.this),
								extension.getName(), ModalityType.APPLICATION_MODAL);
						JPanel panel = new JPanel(new MigLayout("fill"));
						DescriptionArea area = new DescriptionArea(extension.getDescription(), 10, 0);
						panel.add(area, "width 400lp, wrap para");
						JButton close = new JButton(trans.get("button.close"));
						close.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								dialog.setVisible(false);
							}
						});
						panel.add(close, "right");
						dialog.add(panel);
						GUIUtil.setDisposableDialogOptions(dialog, close);
						dialog.setLocationRelativeTo(SwingUtilities.windowForComponent(SimulationOptionsPanel.this));
						dialog.setVisible(true);
					}
				});
				this.add(button, "right");
			}

			// Delete
			button = new JButton(Icons.EDIT_DELETE);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Iterator<SimulationExtension> iter = simulation.getSimulationExtensions().iterator();
					while (iter.hasNext()) {
						// Compare with identity
						if (iter.next() == extension) {
							iter.remove();
							break;
						}
					}
					updateCurrentExtensions();
				}
			});
			this.add(button, "right");
			
		}
	}
	
	private SwingSimulationExtensionConfigurator findConfigurator(SimulationExtension extension) {
		Set<SwingSimulationExtensionConfigurator> configurators = Application.getInjector().getInstance(new Key<>() {
		});
		for (SwingSimulationExtensionConfigurator c : configurators) {
			if (c.support(extension)) {
				return c;
			}
		}
		return null;
	}
	
}
