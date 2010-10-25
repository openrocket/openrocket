package net.sf.openrocket.gui.dialogs.optimization;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.swing.JDialog;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameterService;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifierService;
import net.sf.openrocket.util.BugException;

public class GeneralOptimizationDialog extends JDialog {
	
	private final List<OptimizableParameter> optimizationParameters = new ArrayList<OptimizableParameter>();
	private final Map<Object, List<SimulationModifier>> simulationModifiers =
			new HashMap<Object, List<SimulationModifier>>();
	

	private final OpenRocketDocument document;
	
	public GeneralOptimizationDialog(OpenRocketDocument document, Window parent) {
		this.document = document;
		
		loadOptimizationParameters();
		loadSimulationModifiers();
	}
	
	
	private void loadOptimizationParameters() {
		ServiceLoader<OptimizableParameterService> loader =
				ServiceLoader.load(OptimizableParameterService.class);
		
		for (OptimizableParameterService g : loader) {
			optimizationParameters.addAll(g.getParameters(document));
		}
		
		if (optimizationParameters.isEmpty()) {
			throw new BugException("No rocket optimization parameters found, distribution built wrong.");
		}
		
		Collections.sort(optimizationParameters, new Comparator<OptimizableParameter>() {
			@Override
			public int compare(OptimizableParameter o1, OptimizableParameter o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
	
	private void loadSimulationModifiers() {
		ServiceLoader<SimulationModifierService> loader = ServiceLoader.load(SimulationModifierService.class);
		
		for (SimulationModifierService g : loader) {
			for (SimulationModifier m : g.getModifiers(document)) {
				Object key = m.getRelatedObject();
				List<SimulationModifier> list = simulationModifiers.get(key);
				if (list == null) {
					list = new ArrayList<SimulationModifier>();
					simulationModifiers.put(key, list);
				}
				list.add(m);
			}
		}
		
		for (Object key : simulationModifiers.keySet()) {
			List<SimulationModifier> list = simulationModifiers.get(key);
			Collections.sort(list, new Comparator<SimulationModifier>() {
				@Override
				public int compare(SimulationModifier o1, SimulationModifier o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
		}
		
	}
	
}
