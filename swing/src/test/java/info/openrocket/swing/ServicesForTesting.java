package info.openrocket.swing;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.ResourceBundleTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.swing.gui.util.SwingPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.Preferences;

public class ServicesForTesting extends AbstractModule {

	@Override
	protected void configure() {
		bind(ApplicationPreferences.class).to(PreferencesForTesting.class);
		bind(Translator.class).toProvider(TranslatorProviderForTesting.class);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
	}

	public static class TranslatorProviderForTesting implements Provider<Translator> {

		private final AtomicReference<Translator> translator = new AtomicReference<>();

		@Override
		public Translator get() {

			Translator oldTranslator = translator.get();

			if (oldTranslator != null) {
				return oldTranslator;
			}

			Locale.setDefault(Locale.US);

			// Setup the translator
			Translator newTranslator;
			newTranslator = new ResourceBundleTranslator("l10n.messages");
			if (Locale.getDefault().getLanguage().equals("xx")) {
				newTranslator = new DebugTranslator(newTranslator);
			}

			if (translator.compareAndSet(null, newTranslator)) {
				return newTranslator;
			} else {
				return translator.get();
			}

		}

	}

	@com.google.inject.Singleton
	public static class PreferencesForTesting extends SwingPreferences {

		private static final Preferences ROOT = new InMemoryPreferences(null, "");

		@Override
		public boolean getBoolean(String key, boolean defaultValue) {
			return getBaseNode().getBoolean(key, defaultValue);
		}

		@Override
		public void putBoolean(String key, boolean value) {
			getBaseNode().putBoolean(key, value);
		}

		@Override
		public int getInt(String key, int defaultValue) {
			return getBaseNode().getInt(key, defaultValue);
		}

		@Override
		public void putInt(String key, int value) {
			getBaseNode().putInt(key, value);
		}

		@Override
		public double getDouble(String key, double defaultValue) {
			return getBaseNode().getDouble(key, defaultValue);
		}

		@Override
		public void putDouble(String key, double value) {
			getBaseNode().putDouble(key, value);
		}

		@Override
		public String getString(String key, String defaultValue) {
			return getBaseNode().get(key, defaultValue);
		}

		@Override
		public void putString(String key, String value) {
			if (value == null) {
				getBaseNode().remove(key);
			} else {
				getBaseNode().put(key, value);
			}
		}

		@Override
		public String getString(String directory, String key, String defaultValue) {
			return getBaseNode().node(directory).get(key, defaultValue);
		}

		@Override
		public void putString(String directory, String key, String value) {
			if (value == null) {
				getBaseNode().node(directory).remove(key);
			} else {
				getBaseNode().node(directory).put(key, value);
			}
		}

		@Override
		public void addUserMaterial(Material m) {
			// TODO Auto-generated method stub

		}

		@Override
		public Set<Material> getUserMaterials() {
			return Collections.emptySet();
		}

		@Override
		public void removeUserMaterial(Material m) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setComponentFavorite(ComponentPreset preset, Type type, boolean favorite) {
			// TODO Auto-generated method stub

		}

		@Override
		public Set<String> getComponentFavorites(Type type) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Preferences getNode(String nodeName) {
			return getBaseNode().node(nodeName);
		}

		@Override
		public Preferences getPreferences() {
			return getBaseNode();
		}

		private Preferences getBaseNode() {
			return ROOT;
		}

	}

	/**
	 * A {@link Preferences} implementation that keeps its values in memory.
	 * <p>
	 * The tests deliberately do not use {@link Preferences#userRoot()}: that
	 * store is shared with the core test preferences, and the core and swing test JVMs run
	 * concurrently, so writes of one project can overwrite those of the other. Keeping the
	 * testing preferences in memory isolates them per JVM and makes clearing them cheap.
	 */
	private static final class InMemoryPreferences extends AbstractPreferences {
		private final Map<String, String> values = new HashMap<>();
		private final Map<String, InMemoryPreferences> children = new HashMap<>();

		private InMemoryPreferences(InMemoryPreferences parent, String name) {
			super(parent, name);
		}

		@Override
		protected void putSpi(String key, String value) {
			values.put(key, value);
		}

		@Override
		protected String getSpi(String key) {
			return values.get(key);
		}

		@Override
		protected void removeSpi(String key) {
			values.remove(key);
		}

		@Override
		protected void removeNodeSpi() {
			((InMemoryPreferences) parent()).children.remove(name());
		}

		@Override
		protected String[] keysSpi() {
			return values.keySet().toArray(new String[0]);
		}

		@Override
		protected String[] childrenNamesSpi() {
			return children.keySet().toArray(new String[0]);
		}

		@Override
		protected AbstractPreferences childSpi(String name) {
			return children.computeIfAbsent(name, n -> new InMemoryPreferences(this, n));
		}

		@Override
		protected void syncSpi() {
			// Nothing to synchronise, the values never leave the JVM
		}

		@Override
		protected void flushSpi() {
			// Nothing to flush, the values never leave the JVM
		}
	}
}
