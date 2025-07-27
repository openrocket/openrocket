package info.openrocket.core.simulation.extension.impl;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.L10N;
import info.openrocket.core.simulation.SimulationConditions;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.simulation.extension.AbstractSimulationExtension;
import info.openrocket.core.simulation.listeners.SimulationListener;

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
		 * when loading from a file. This is to prevent trojans.
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
		ScriptEngine engine = util.getEngineByName(getLanguage());
		if (engine == null) {
			throw new SimulationException("Your JRE does not support the scripting language '" + getLanguage() + "'");
		}

		try {
			engine.eval(getScript());
		} catch (ScriptException e) {
			throw new SimulationException("Invalid script: " + e.getMessage());
		}

		if (!(engine instanceof Invocable)) {
			throw new SimulationException(
					"The scripting language '" + getLanguage() + "' does not implement the Invocable interface");
		}
		return new ScriptingSimulationListener((Invocable) engine);
	}

}
