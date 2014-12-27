package net.sf.openrocket.simulation.extension.impl;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.SimulationListener;

public class ScriptingExtension extends AbstractSimulationExtension {
	
	private static final String JS = "JavaScript";
	
	public ScriptingExtension() {
		setLanguage(JS);
	}
	
	@Override
	public String getName() {
		String name = trans.get("SimulationExtension.scripting.name");
		name = L10N.replace(name, "{language}", getLanguage());
		return name;
	}
	
	@Override
	public String getDescription() {
		return trans.get("SimulationExtension.scripting.desc");
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		conditions.getSimulationListenerList().add(getListener());
	}
	
	
	public String getScript() {
		return config.getString("script", "");
	}
	
	public void setScript(String script) {
		config.put("script", script);
	}
	
	public String getLanguage() {
		// TODO: Support other languages
		return JS;
	}
	
	public void setLanguage(String language) {
		config.put("language", language);
	}
	
	
	SimulationListener getListener() throws SimulationException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		try {
			engine.eval(getScript());
		} catch (ScriptException e) {
			throw new SimulationException("Invalid script: " + e.getMessage());
		}
		
		// TODO: Check for implementation first
		return new ScriptingSimulationListener((Invocable) engine);
	}
	
}
