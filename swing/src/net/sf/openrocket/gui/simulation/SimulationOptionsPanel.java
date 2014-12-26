package net.sf.openrocket.gui.simulation;

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

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.RK4SimulationStepper;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.simulation.extension.SimulationExtensionProvider;
import net.sf.openrocket.simulation.extension.SwingSimulationExtensionConfigurator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GeodeticComputationStrategy;

import com.google.inject.Key;

class SimulationOptionsPanel extends JPanel {
	
	private static final Translator trans = Application.getTranslator();
	
	final Simulation simulation;
	
	private JPanel currentExtensions;
	
	SimulationOptionsPanel(final Simulation simulation) {
		super(new MigLayout("fill"));
		this.simulation = simulation;
		
		final SimulationOptions conditions = simulation.getOptions();
		
		JPanel sub, subsub;
		String tip;
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		UnitSelector unit;
		BasicSlider slider;
		
		
		//// Simulation options
		sub = new JPanel(new MigLayout("fill, gap rel unrel",
				"[grow][65lp!][30lp!][75lp!]", ""));
		//// Simulator options
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.Simopt")));
		this.add(sub, "growx, growy, aligny 0");
		
		
		// Separate panel for computation methods, as they use a different layout
		subsub = new JPanel(new MigLayout("insets 0, fill"));
		
		
		//// Calculation method:
		tip = trans.get("simedtdlg.lbl.ttip.Calcmethod");
		label = new JLabel(trans.get("simedtdlg.lbl.Calcmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		//// Extended Barrowman
		label = new JLabel(trans.get("simedtdlg.lbl.ExtBarrowman"));
		label.setToolTipText(tip);
		subsub.add(label, "growx, wrap para");
		
		
		//  Simulation method
		tip = trans.get("simedtdlg.lbl.ttip.Simmethod1") +
				trans.get("simedtdlg.lbl.ttip.Simmethod2");
		label = new JLabel(trans.get("simedtdlg.lbl.Simmethod"));
		label.setToolTipText(tip);
		subsub.add(label, "gapright para");
		
		label = new JLabel("6-DOF Runge-Kutta 4");
		label.setToolTipText(tip);
		subsub.add(label, "growx, wrap para");
		
		
		//// Geodetic calculation method:
		label = new JLabel(trans.get("simedtdlg.lbl.GeodeticMethod"));
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.GeodeticMethodTip"));
		subsub.add(label, "gapright para");
		
		EnumModel<GeodeticComputationStrategy> gcsModel = new EnumModel<GeodeticComputationStrategy>(conditions, "GeodeticComputation");
		final JComboBox gcsCombo = new JComboBox(gcsModel);
		ActionListener gcsTTipListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GeodeticComputationStrategy gcs = (GeodeticComputationStrategy) gcsCombo.getSelectedItem();
				gcsCombo.setToolTipText(gcs.getDescription());
			}
		};
		gcsCombo.addActionListener(gcsTTipListener);
		gcsTTipListener.actionPerformed(null);
		subsub.add(gcsCombo, "growx, wrap para");
		
		sub.add(subsub, "spanx, wrap para");
		
		
		//// Time step:
		label = new JLabel(trans.get("simedtdlg.lbl.Timestep"));
		tip = trans.get("simedtdlg.lbl.ttip.Timestep1") +
				trans.get("simedtdlg.lbl.ttip.Timestep2") + " " +
				UnitGroup.UNITS_TIME_STEP.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP) +
				".";
		label.setToolTipText(tip);
		sub.add(label);
		
		m = new DoubleModel(conditions, "TimeStep", UnitGroup.UNITS_TIME_STEP, 0, 1);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText(tip);
		sub.add(spin, "w 65lp!");
		//sub.add(spin, "nogrid");
		
		unit = new UnitSelector(m);
		unit.setToolTipText(tip);
		sub.add(unit, "w 25");
		//sub.add(unit, "nogrid");
		slider = new BasicSlider(m.getSliderModel(0, 0.2));
		slider.setToolTipText(tip);
		sub.add(slider, "w 75lp, wrap");
		//sub.add(slider,"wrap");
		
		
		
		
		//// Reset to default button
		JButton button = new JButton(trans.get("simedtdlg.but.resettodefault"));
		//// Reset the time step to its default value (
		button.setToolTipText(trans.get("simedtdlg.but.ttip.resettodefault") +
				UnitGroup.UNITS_SHORT_TIME.toStringUnit(RK4SimulationStepper.RECOMMENDED_TIME_STEP) +
				").");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				conditions.setTimeStep(RK4SimulationStepper.RECOMMENDED_TIME_STEP);
				conditions.setGeodeticComputation(GeodeticComputationStrategy.SPHERICAL);
			}
		});
		
		sub.add(button, "align left");
		
		
		
		
		//// Simulation extensions
		sub = new JPanel(new MigLayout("fill, gap 0 0"));
		sub.setBorder(BorderFactory.createTitledBorder(trans.get("simedtdlg.border.SimExt")));
		this.add(sub, "wmin 300lp, growx, growy");
		
		
		DescriptionArea desc = new DescriptionArea(5);
		desc.setText(trans.get("simedtdlg.SimExt.desc"));
		sub.add(desc, "aligny 0, hmin 100lp, growx, wrap para");
		
		
		final JButton addExtension = new JButton("Add extension");
		final JPopupMenu menu = getExtensionMenu();
		addExtension.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				menu.show(addExtension, 5, addExtension.getBounds().height);
			}
		});
		sub.add(addExtension, "growx, wrap 0");
		
		currentExtensions = new JPanel(new MigLayout("fillx, gap 0 0, ins 0"));
		JScrollPane scroll = new JScrollPane(currentExtensions);
		//  &#$%! scroll pane will not honor "growy"...
		sub.add(scroll, "growx, growy, h 100%");
		
		updateCurrentExtensions();
		
	}
	
	private JPopupMenu getExtensionMenu() {
		Set<SimulationExtensionProvider> extensions = Application.getInjector().getInstance(new Key<Set<SimulationExtensionProvider>>() {
		});
		
		JPopupMenu basemenu = new JPopupMenu();
		
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
						}
					});
					menu.add(item);
				}
			}
		}
		return basemenu;
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
			l.setForeground(Color.DARK_GRAY);
			currentExtensions.add(l, "growx, pad 5 5 5 5, wrap");
		} else {
			for (SimulationExtension e : simulation.getSimulationExtensions()) {
				currentExtensions.add(new SimulationExtensionPanel(e), "growx, wrap");
			}
		}
		// Both needed:
		this.revalidate();
		this.repaint();
	}
	
	
	private class SimulationExtensionPanel extends JPanel {
		
		public SimulationExtensionPanel(final SimulationExtension extension) {
			super(new MigLayout("fillx, gapx 0"));
			
			this.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
			this.add(new JLabel(extension.getName()), "spanx, growx, wrap");
			
			JButton button;
			
			this.add(new JPanel(), "spanx, split, growx, right");
			
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
			
			button = new JButton(Icons.DELETE);
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
		
		private SwingSimulationExtensionConfigurator findConfigurator(SimulationExtension extension) {
			Set<SwingSimulationExtensionConfigurator> configurators = Application.getInjector().getInstance(new Key<Set<SwingSimulationExtensionConfigurator>>() {
			});
			for (SwingSimulationExtensionConfigurator c : configurators) {
				if (c.support(extension)) {
					return c;
				}
			}
			return null;
		}
	}
	
	//	
	//	
	//	private class ExtensionListModel extends AbstractListModel {
	//		@Override
	//		public SimulationExtensionConfiguration getElementAt(int index) {
	//			if (index < 0 || index >= getSize())
	//				return null;
	//			return simulation.getSimulationExtensions().get(index);
	//		}
	//		
	//		@Override
	//		public int getSize() {
	//			return simulation.getSimulationExtensions().size();
	//		}
	//	}
	//	
	//	
	//	private class ExtensionCellRenderer extends JPanel implements ListCellRenderer {
	//		private JLabel label;
	//		
	//		public ExtensionCellRenderer() {
	//			super(new MigLayout("fill"));
	//			label = new JLabel();
	//			
	//		}
	//		
	//		@Override
	//		public Component getListCellRendererComponent(JList list, Object value,
	//				int index, boolean isSelected, boolean cellHasFocus) {
	//			SimulationExtensionConfiguration config = (SimulationExtensionConfiguration) value;
	//			
	//			
	//			
	//			String s = value.toString();
	//			setText(s);
	//			
	//			// Attempt instantiating, catch any exceptions
	//			Exception ex = null;
	//			try {
	//				Class<?> c = Class.forName(s);
	//				@SuppressWarnings("unused")
	//				SimulationListener l = (SimulationListener) c.newInstance();
	//			} catch (Exception e) {
	//				ex = e;
	//			}
	//			
	//			if (ex == null) {
	//				setIcon(Icons.SIMULATION_LISTENER_OK);
	//				//// Listener instantiated successfully.
	//				setToolTipText("Listener instantiated successfully.");
	//			} else {
	//				setIcon(Icons.SIMULATION_LISTENER_ERROR);
	//				//// <html>Unable to instantiate listener due to exception:<br>
	//				setToolTipText("<html>Unable to instantiate listener due to exception:<br>" +
	//						ex.toString());
	//			}
	//			
	//			if (isSelected) {
	//				setBackground(list.getSelectionBackground());
	//				setForeground(list.getSelectionForeground());
	//			} else {
	//				setBackground(list.getBackground());
	//				setForeground(list.getForeground());
	//			}
	//			setOpaque(true);
	//			return this;
	//		}
	//	}
	
}
