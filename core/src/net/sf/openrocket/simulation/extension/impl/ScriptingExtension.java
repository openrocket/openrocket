package net.sf.openrocket.simulation.extension.impl;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.extension.AbstractSimulationExtension;
import net.sf.openrocket.simulation.listeners.SimulationListener;

import com.google.inject.Inject;

public class ScriptingExtension extends AbstractSimulationExtension {
	
	private static final String DEFAULT_LANGUAGE = "JavaScript";
	
	@Inject
	private ScriptingUtil util;
	
	
	public ScriptingExtension() {
		setLanguage(DEFAULT_LANGUAGE);
		setScript("");
		setEnabled(true);
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
	public void documentLoaded(OpenRocketDocument document, Simulation simulation, WarningSet warnings) {
		/*
		 * Scripts that the user has not explicitly indicated as trusted are disabled
		 * when loading from a file.  This is to prevent trojans.
		 */
		if (isEnabled()) {
			if (!util.isTrustedScript(getLanguage(), getScript())) {
				setEnabled(false);
				warnings.add(Warning.fromString(trans.get("SimulationExtension.scripting.warning.disabled")));
			}
		}
	}
	
	@Override
	public void initialize(SimulationConditions conditions) throws SimulationException {
		if (isEnabled()) {
			conditions.getSimulationListenerList().add(getListener());
		}
	}
	
	
	public String getScript() {
		return config.getString("script", "");
	}
	
	public void setScript(String script) {
		config.put("script", script);
	}
	
	public String getLanguage() {
		return config.getString("language", DEFAULT_LANGUAGE);
	}
	
	public void setLanguage(String language) {
		config.put("language", language);
	}
	
	public boolean isEnabled() {
		return config.getBoolean("enabled", false);
	}
	
	public void setEnabled(boolean enabled) {
		config.put("enabled", enabled);
	}
	
	
	SimulationListener getListener() throws SimulationException {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName(getLanguage());
		if (engine == null) {
			throw new SimulationException("Your JRE does not support the scripting language '" + getLanguage() + "'");
		}
		
		try {
			engine.eval(getScript());
		} catch (ScriptException e) {
			throw new SimulationException("Invalid script: " + e.getMessage());
		}
		
		if (!(engine instanceof Invocable)) {
			throw new SimulationException("The scripting language '" + getLanguage() + "' does not implement the Invocable interface");
		}
		return new ScriptingSimulationListener((Invocable) engine);
	}
	
}
