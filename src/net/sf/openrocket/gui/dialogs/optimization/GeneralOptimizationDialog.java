package net.sf.openrocket.gui.dialogs.optimization;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.optimization.rocketoptimization.OptimizableParameter;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.optimization.services.OptimizationServiceHelper;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.GUIUtil;

public class GeneralOptimizationDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	
	private final List<OptimizableParameter> optimizationParameters = new ArrayList<OptimizableParameter>();
	private final Map<Object, List<SimulationModifier>> simulationModifiers =
			new HashMap<Object, List<SimulationModifier>>();
	

	private final OpenRocketDocument baseDocument;
	private final Rocket rocketCopy;
	
	
	public GeneralOptimizationDialog(OpenRocketDocument document, Window parent) {
		super(parent, "Rocket optimization");
		
		this.baseDocument = document;
		this.rocketCopy = document.getRocket().copyWithOriginalID();
		
		loadOptimizationParameters();
		loadSimulationModifiers();
		

		JPanel panel = new JPanel(new MigLayout("fill"));
		

		JTree tree = new SimulationModifierTree(rocketCopy, simulationModifiers);
		JScrollPane scroll = new JScrollPane(tree);
		panel.add(scroll, "width 300lp, height 300lp");
		

		this.add(panel);
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	
	private void loadOptimizationParameters() {
		optimizationParameters.addAll(OptimizationServiceHelper.getOptimizableParameters(baseDocument));
		
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
		
		for (SimulationModifier m : OptimizationServiceHelper.getSimulationModifiers(baseDocument)) {
			Object key = m.getRelatedObject();
			List<SimulationModifier> list = simulationModifiers.get(key);
			if (list == null) {
				list = new ArrayList<SimulationModifier>();
				simulationModifiers.put(key, list);
			}
			list.add(m);
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
