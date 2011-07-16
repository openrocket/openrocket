package net.sf.openrocket.gui.optimization;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.Function;
import net.sf.openrocket.optimization.general.FunctionOptimizer;
import net.sf.openrocket.optimization.general.OptimizationController;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.general.ParallelExecutorCache;
import net.sf.openrocket.optimization.general.ParallelFunctionCache;
import net.sf.openrocket.optimization.general.Point;
import net.sf.openrocket.optimization.general.multidim.MultidirectionalSearchOptimizer;
import net.sf.openrocket.optimization.rocketoptimization.RocketOptimizationFunction;
import net.sf.openrocket.optimization.rocketoptimization.SimulationDomain;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.optimization.rocketoptimization.domains.StabilityDomain;
import net.sf.openrocket.optimization.rocketoptimization.goals.MaximizationGoal;
import net.sf.openrocket.optimization.rocketoptimization.modifiers.GenericComponentModifier;
import net.sf.openrocket.optimization.rocketoptimization.parameters.MaximumAltitudeParameter;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.GUIUtil;

public class OptimizationTestDialog extends JDialog {
	
	private final OpenRocketDocument document;
	
	public OptimizationTestDialog(Window parent, OpenRocketDocument document) {
		super(parent, "Optimization", ModalityType.APPLICATION_MODAL);
		
		this.document = document;
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		this.add(panel);
		
		JButton button = new JButton("Test optimize");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					doOptimize();
				} catch (OptimizationException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(button, "wrap para");
		


		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptimizationTestDialog.this.dispose();
			}
		});
		panel.add(close);
		

		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
	
	private void doOptimize() throws OptimizationException {
		Simulation sim = document.getSimulation(0);
		Rocket rocket = sim.getRocket();
		
		RocketComponent body = null;
		
		for (RocketComponent c : rocket) {
			if (c instanceof BodyTube) {
				body = c;
				break;
			}
		}
		
		Point initial;
		
		SimulationDomain domain;
		//		domain= new IdentitySimulationDomain();
		domain = new StabilityDomain(2, false);
		
		SimulationModifier mod1 = new GenericComponentModifier("Test", body,
				UnitGroup.UNITS_LENGTH, 1.0, BodyTube.class, body.getID(), "Length");
		mod1.setMinValue(0.1);
		mod1.setMaxValue(0.7);
		
		SimulationModifier mod2 = new GenericComponentModifier("Test", body,
				UnitGroup.UNITS_LENGTH, 2.0, BodyTube.class, body.getID(), "OuterRadius");
		mod2.setMinValue(0.01);
		mod2.setMaxValue(0.10);
		
		OptimizationController controller = new OptimizationController() {
			int step = 0;
			
			@Override
			public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
				step++;
				System.out.println("STEP " + step + " oldValue=" + oldValue + " newValue=" + newValue +
						" oldPoint=" + oldPoint + " newPoint=" + newPoint +
						" stepSize=" + stepSize);
				return step < 20;
			}
		};
		

		initial = new Point(mod1.getCurrentScaledValue(sim), mod2.getCurrentScaledValue(sim));
		

		Function function = new RocketOptimizationFunction(sim, new MaximumAltitudeParameter(),
				new MaximizationGoal(), domain, mod1, mod2);
		/*
		function = new Function() {
			@Override
			public double evaluate(Point point) throws InterruptedException, OptimizationException {
				// y = ax^2 + bx + c
				// y' = 2ax + b
				// 2a * pi/4 + b = 0
				// b = -a*pi/2
				// a=-1 -> b = pi/2
				

				double x = point.get(0);
				double y = -x * x + Math.PI / 2 * x;
				System.out.println("Evaluating at x=" + x + " value=" + y);
				return y;
			}
		};
		*/

		ParallelFunctionCache cache = new ParallelExecutorCache(1);
		cache.setFunction(function);
		
		FunctionOptimizer optimizer = new MultidirectionalSearchOptimizer(cache);
		
		optimizer.optimize(initial, controller);
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	private static int evalCount = 0;
	
	public static void main(String[] args) throws OptimizationException {
		Point initial;
		


		OptimizationController controller = new OptimizationController() {
			int step = 0;
			
			@Override
			public boolean stepTaken(Point oldPoint, double oldValue, Point newPoint, double newValue, double stepSize) {
				step++;
				System.out.println("STEP " + step + " oldValue=" + oldValue + " newValue=" + newValue +
						" oldPoint=" + oldPoint + " newPoint=" + newPoint +
						" stepSize=" + stepSize);
				return step < 20;
			}
		};
		

		initial = new Point(0.5, 0.5);
		

		Function function = new Function() {
			@Override
			public double evaluate(Point point) throws InterruptedException, OptimizationException {
				// y = ax^2 + bx + c
				// y' = 2ax + b
				// 2a * pi/4 + b = 0
				// b = -a*pi/2
				// a=-1 -> b = pi/2
				
				evalCount++;
				
				//				double x = point.get(0);
				//				double y = x * x - Math.PI / 2 * x;
				//				System.out.println("Evaluating at x=" + x + " value=" + y);
				//				return y;
				
				double x = point.get(0);
				double y = point.get(1);
				double z = 4 * pow2((x - 0.3231)) + 2 * pow2(y - 0.8923);
				
				System.out.println("Evaluation " + evalCount + ":  x=" + x + " y=" + y + " z=" + z);
				
				return z;
			}
		};
		

		ParallelFunctionCache cache = new ParallelExecutorCache();
		cache.setFunction(function);
		
		FunctionOptimizer optimizer = new MultidirectionalSearchOptimizer(cache);
		
		optimizer.optimize(initial, controller);
		

		System.out.println("Total evaluation count: " + evalCount);
	}
}
