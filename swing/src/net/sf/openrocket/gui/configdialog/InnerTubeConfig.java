package net.sf.openrocket.gui.configdialog;


import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.Resettable;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.Clusterable;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.StateChangeListener;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.EventObject;
import java.util.List;


public class InnerTubeConfig extends ThicknessRingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	
	public InnerTubeConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		JPanel tab;
		
		tab = new MotorConfig((MotorMount) c);
		//// Motor and Motor mount configuration
		tabbedPane.insertTab(trans.get("InnerTubeCfg.tab.Motor"), null, tab,
				trans.get("InnerTubeCfg.tab.ttip.Motor"), 1);
		
		tab = clusterTab();
		//// Cluster and Cluster configuration
		tabbedPane.insertTab(trans.get("InnerTubeCfg.tab.Cluster"), null, tab,
				trans.get("InnerTubeCfg.tab.ttip.Cluster"), 2);
		
		tab = positionTab();
		//// Radial position
		tabbedPane.insertTab(trans.get("InnerTubeCfg.tab.Radialpos"), null, tab,
				trans.get("InnerTubeCfg.tab.ttip.Radialpos"), 3);
		
		tabbedPane.setSelectedIndex(0);
	}
	
	
	private JPanel clusterTab() {
		JPanel panel = new JPanel(new MigLayout());
		
		JPanel subPanel = new JPanel(new MigLayout());
		
		// Cluster type selection
		//// Select cluster configuration:
		subPanel.add(new JLabel(trans.get("InnerTubeCfg.lbl.Selectclustercfg")), "spanx, wrap");
		subPanel.add(new ClusterSelectionPanel((InnerTube) component), "spanx, wrap");
		//		JPanel clusterSelection = new ClusterSelectionPanel((InnerTube)component);
		//		clusterSelection.setBackground(Color.blue);
		//		subPanel.add(clusterSelection);
		
		panel.add(subPanel);
		

		subPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]"));
		
		// Tube separation scale
		//// Tube separation:
		JLabel l = new JLabel(trans.get("InnerTubeCfg.lbl.TubeSep"));
		//// The separation of the tubes, 1.0 = touching each other
		l.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.TubeSep"));
		subPanel.add(l);
		DoubleModel dm = new DoubleModel(component, "ClusterScale", 1, UnitGroup.UNITS_NONE, 0);
		
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		//// The separation of the tubes, 1.0 = touching each other
		spin.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.TubeSep"));
		subPanel.add(spin, "growx");
		
		BasicSlider bs = new BasicSlider(dm.getSliderModel(0, 1, 4));
		//// The separation of the tubes, 1.0 = touching each other
		bs.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.TubeSep"));
		subPanel.add(bs, "skip,w 100lp, wrap");
		
		// Rotation:
		l = new JLabel(trans.get("InnerTubeCfg.lbl.Rotation"));
		//// Rotation angle of the cluster configuration
		l.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.Rotation"));
		subPanel.add(l);
		dm = new DoubleModel(component, "ClusterRotation", 1, UnitGroup.UNITS_ANGLE,
				-Math.PI, Math.PI);
		
		spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		//// Rotation angle of the cluster configuration
		spin.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.Rotation"));
		subPanel.add(spin, "growx");
		
		subPanel.add(new UnitSelector(dm), "growx");
		bs = new BasicSlider(dm.getSliderModel(-Math.PI, 0, Math.PI));
		//// Rotation angle of the cluster configuration
		bs.setToolTipText(trans.get("InnerTubeCfg.lbl.ttip.Rotation"));
		subPanel.add(bs, "w 100lp, wrap para");
		


		// Split button
		//// Split cluster
		JButton split = new JButton(trans.get("InnerTubeCfg.but.Splitcluster"));
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
						RocketComponent parent = component.getParent();
						int index = parent.getChildPosition(component);
						if (index < 0) {
							throw new BugException("Inconsistent state: component=" + component +
									" parent=" + parent + " parent.children=" + parent.getChildren());
						}
						
						InnerTube tube = (InnerTube) component;
						if (tube.getClusterCount() <= 1)
							return;
						
						document.addUndoPosition("Split cluster");
						
						Coordinate[] coords = { Coordinate.NUL };
						coords = component.shiftCoordinates(coords);
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
		

		// Reset button
		///// Reset settings
		JButton reset = new JButton(trans.get("InnerTubeCfg.but.Resetsettings"));
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
		
		panel.add(subPanel, "grow");
		

		return panel;
	}


}


class ClusterSelectionPanel extends JPanel {
	private static final int BUTTON_SIZE = 50;
	private static final int MOTOR_DIAMETER = 10;
	
	private static final Color SELECTED_COLOR = Color.RED;
	private static final Color UNSELECTED_COLOR = Color.WHITE;
	private static final Color MOTOR_FILL_COLOR = Color.GREEN;
	private static final Color MOTOR_BORDER_COLOR = Color.BLACK;
	
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
	
	
	private class ClusterButton extends JPanel implements StateChangeListener, MouseListener,
															Resettable {
		private Clusterable component;
		private ClusterConfiguration config;
		
		public ClusterButton(Clusterable c, ClusterConfiguration config) {
			component = c;
			this.config = config;
			setMinimumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setMaximumSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			//			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
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
