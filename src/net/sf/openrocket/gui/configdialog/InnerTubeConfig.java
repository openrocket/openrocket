package net.sf.openrocket.gui.configdialog;



import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.Resettable;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.rocketcomponent.ClusterConfiguration;
import net.sf.openrocket.rocketcomponent.Clusterable;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;


public class InnerTubeConfig extends ThicknessRingComponentConfig {


	public InnerTubeConfig(RocketComponent c) {
		super(c);
		
		JPanel tab;
		
		tab = positionTab();
		tabbedPane.insertTab("Radial position", null, tab, "Radial position", 1);
		
		tab = new MotorConfig((MotorMount)c);
		tabbedPane.insertTab("Motor", null, tab, "Motor mount configuration", 2);

		tab = clusterTab();
		tabbedPane.insertTab("Cluster", null, tab, "Cluster configuration", 3);
		
		tabbedPane.setSelectedIndex(0);
	}
	
	
	private JPanel clusterTab() {
		JPanel panel = new JPanel(new MigLayout());
		
		JPanel subPanel = new JPanel(new MigLayout());
		
		// Cluster type selection
		subPanel.add(new JLabel("Select cluster configuration:"),"spanx, wrap");
		subPanel.add(new ClusterSelectionPanel((InnerTube)component),"spanx, wrap");
//		JPanel clusterSelection = new ClusterSelectionPanel((InnerTube)component);
//		clusterSelection.setBackground(Color.blue);
//		subPanel.add(clusterSelection);
		
		panel.add(subPanel);
		
		
		subPanel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]"));

		// Tube separation scale
		JLabel l = new JLabel("Tube separation:");
		l.setToolTipText("The separation of the tubes, 1.0 = touching each other");
		subPanel.add(l);
		DoubleModel dm  = new DoubleModel(component,"ClusterScale",1,UnitGroup.UNITS_NONE,0);

		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText("The separation of the tubes, 1.0 = touching each other");
		subPanel.add(spin,"growx");
		
		BasicSlider bs = new BasicSlider(dm.getSliderModel(0, 1, 4));
		bs.setToolTipText("The separation of the tubes, 1.0 = touching each other");
		subPanel.add(bs,"skip,w 100lp, wrap");

		// Rotation
		l = new JLabel("Rotation:");
		l.setToolTipText("Rotation angle of the cluster configuration");
		subPanel.add(l);
		dm  = new DoubleModel(component,"ClusterRotation",1,UnitGroup.UNITS_ANGLE,0);

		spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText("Rotation angle of the cluster configuration");
		subPanel.add(spin,"growx");
		
		subPanel.add(new UnitSelector(dm),"growx");
		bs = new BasicSlider(dm.getSliderModel(-Math.PI, 0, Math.PI));
		bs.setToolTipText("Rotation angle of the cluster configuration");
		subPanel.add(bs,"w 100lp, wrap");

		// Reset button
		JButton reset = new JButton("Reset");
		reset.setToolTipText("Reset the separation and rotation to the default values");
		reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				((InnerTube)component).setClusterScale(1.0);
				((InnerTube)component).setClusterRotation(0.0);
			}
		});
		subPanel.add(reset,"spanx,right");
		
		panel.add(subPanel,"grow");
		
		
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
				"["+BUTTON_SIZE+"!]["+BUTTON_SIZE+"!]["+BUTTON_SIZE+"!]["+BUTTON_SIZE+"!]",
				"["+BUTTON_SIZE+"!]["+BUTTON_SIZE+"!]["+BUTTON_SIZE+"!]"));
		
		for (int i=0; i<ClusterConfiguration.CONFIGURATIONS.length; i++) {
			ClusterConfiguration config = ClusterConfiguration.CONFIGURATIONS[i];
			
			JComponent button = new ClusterButton(component,config);
			if (i%4 == 3) 
				add(button,"wrap");
			else
				add(button);
		}

	}
	
	
	private class ClusterButton extends JPanel implements ChangeListener, MouseListener,
														  Resettable {
		private Clusterable component;
		private ClusterConfiguration config;
		
		public ClusterButton(Clusterable c, ClusterConfiguration config) {
			component = c;
			this.config = config;
			setMinimumSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
			setPreferredSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
			setMaximumSize(new Dimension(BUTTON_SIZE,BUTTON_SIZE));
			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			component.addChangeListener(this);
			addMouseListener(this);
		}
		

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
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
			for (int i=0; i < points.size()/2; i++) {
				double x = points.get(i*2);
				double y = points.get(i*2+1);
				
				double px = BUTTON_SIZE/2 + x*MOTOR_DIAMETER;
				double py = BUTTON_SIZE/2 - y*MOTOR_DIAMETER;
				circle.setFrameFromCenter(px,py,px+MOTOR_DIAMETER/2,py+MOTOR_DIAMETER/2);
				
				g2.setColor(MOTOR_FILL_COLOR);
				g2.fill(circle);
				g2.setColor(MOTOR_BORDER_COLOR);
				g2.draw(circle);
			}
		}


		public void stateChanged(ChangeEvent e) {
			repaint();
		}


		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				component.setClusterConfiguration(config);
			}
		}
		
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) {	}
		public void mousePressed(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) { }


		public void resetModel() {
			component.removeChangeListener(this);
			removeMouseListener(this);
		}
	}
	
}
