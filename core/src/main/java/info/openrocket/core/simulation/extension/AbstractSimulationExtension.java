package info.openrocket.core.simulation.extension;

import java.util.Collections;
import java.util.List;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.util.AbstractChangeSource;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Config;

import com.google.inject.Inject;

/**
 * An abstract implementation of a SimulationExtension.
 */
public abstract class AbstractSimulationExtension extends AbstractChangeSource
		implements SimulationExtension, Cloneable {

	@Inject
	protected Translator trans;

	protected Config config = new Config();
	private final String name;

	/**
	 * Use the current class name as the extension name. You should override
	 * getName if you use this constructor.
	 */
	protected AbstractSimulationExtension() {
		this(null);
	}

	/**
	 * Use the provided name as a static name for this extension.
	 */
	protected AbstractSimulationExtension(String name) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = this.getClass().getSimpleName();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default, this method returns the canonical name of this class.
	 */
	@Override
	public String getId() {
		return this.getClass().getCanonicalName();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default, this method returns the name provided to the constructor.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default, this method returns null.
	 */
	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default, this method returns an empty list.
	 */
	@Override
	public List<FlightDataType> getFlightDataTypes() {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * By default, this method does nothing.
	 */
	@Override
	public void documentLoaded(OpenRocketDocument document, Simulation simulation, WarningSet warnings) {

	}

	/**
	 * By default, returns a new object obtained by calling Object.clone() and
	 * cloning the config object.
	 */
	@Override
	public SimulationExtension clone() {
		try {
			AbstractSimulationExtension copy = (AbstractSimulationExtension) super.clone();
			copy.config = this.config.clone();
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new BugException(e);
		}
	}

	@Override
	public Config getConfig() {
		return config.clone();
	}

	@Override
	public void setConfig(Config config) {
		this.config = config.clone();
		fireChangeEvent();
	}
}
