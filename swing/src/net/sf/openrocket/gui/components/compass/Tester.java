package net.sf.openrocket.gui.components.compass;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.utils.BasicApplication;

public class Tester {
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		
		BasicApplication baseApp = new BasicApplication();
		baseApp.initializeApplication();
		
		GUIUtil.setBestLAF();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame();
				
				JPanel panel = new JPanel(new MigLayout("fill"));
				DoubleModel model = new DoubleModel(Math.toRadians(45), UnitGroup.UNITS_ANGLE);
				DoubleModel second = new DoubleModel(Math.toRadians(30), UnitGroup.UNITS_ANGLE);
				
				
				CompassPointer rose = new CompassSelector(model);
				rose.setPreferredSize(new Dimension(300, 300));
				rose.setSecondaryModel(second);
				panel.add(rose);
				
				rose = new CompassPointer(model);
				rose.setPreferredSize(new Dimension(24, 24));
				panel.add(rose);
				rose.setMarkerFont(null);
				rose.setPointerArrow(false);
				rose.setPointerWidth(0.45f);
				rose.setScaler(1.0f);
				
				JSpinner spin = new JSpinner(model.getSpinnerModel());
				spin.setPreferredSize(new Dimension(50, 20));
				panel.add(spin, "wrap para");
				
				
				CompassSelectionButton button = new CompassSelectionButton(model);
				panel.add(button);
				
				
				frame.add(panel);
				frame.pack();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}
