package net.sf.openrocket.simulation.extension;

import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.l10n.Translator;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * An abstract implementation of a SimulationExtensionProvider.  The constructor is
 * provided by the class of the SimulationExtension and the name of the extension.
 */
public abstract class AbstractSimulationExtensionProvider implements SimulationExtensionProvider {
	
	@Inject
	private Injector injector;
	
	@Inject
	protected Translator trans;
	
	protected final Class<? extends SimulationExtension> extensionClass;
	private final String[] name;
	
	/**
	 * Sole constructor.
	 * 
	 * @param extensionClass	the simulation extension class
	 * @param name				the name returned by getName
	 */
	protected AbstractSimulationExtensionProvider(Class<? extends SimulationExtension> extensionClass, String... name) {
		this.extensionClass = extensionClass;
		this.name = name;
	}
	
	/**
	 * By default returns the canonical name of the simulation extension class.
	 */
	@Override
	public List<String> getIds() {
		return Arrays.asList(extensionClass.getCanonicalName());
	}
	
	/**
	 * By default returns the provided extension name for the first ID that getIds returns.
	 */
	@Override
	public List<String> getName(String id) {
		if (id.equals(getIds().get(0))) {
			return Arrays.asList(name);
		}
		return null;
	}
	
	/**
	 * By default returns a new instance of the simulation extension class instantiated by
	 * Class.newInstance.
	 */
	@Override
	public SimulationExtension getInstance(String id) {
		return injector.getInstance(extensionClass);
	}
	
}
