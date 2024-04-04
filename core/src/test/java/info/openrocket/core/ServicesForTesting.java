package info.openrocket.core;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.prefs.BackingStoreException;

import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.ResourceBundleTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPreset.Type;
import info.openrocket.core.startup.Preferences;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

public class ServicesForTesting extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(Preferences.class).to(PreferencesForTesting.class);
		bind(Translator.class).toProvider(TranslatorProviderForTesting.class);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
	}
	
	public static class TranslatorProviderForTesting implements Provider<Translator> {
		
		private final AtomicReference<Translator> translator = new AtomicReference<Translator>();
		
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
	
	public static class PreferencesForTesting extends Preferences {
		
		private static java.util.prefs.Preferences root = null;
		
		@Override
		public boolean getBoolean(String key, boolean defaultValue) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public void putBoolean(String key, boolean value) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public int getInt(String key, int defaultValue) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void putInt(String key, int value) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public double getDouble(String key, double defaultValue) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void putDouble(String key, double value) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public String getString(String key, String defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void putString(String key, String value) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public String getString(String directory, String key, String defaultValue) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void putString(String directory, String key, String value) {
			// TODO Auto-generated method stub
			
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
		public java.util.prefs.Preferences getNode(String nodeName) {
			return getBaseNode().node(nodeName);
		}

		@Override
		public java.util.prefs.Preferences getPreferences() {
			return getBaseNode();
		}

		private java.util.prefs.Preferences getBaseNode() {
			if (root == null) {
				final String name = "OpenRocket-unittest-" + System.currentTimeMillis();
				root = java.util.prefs.Preferences.userRoot().node(name);
				Runtime.getRuntime().addShutdownHook(new Thread() {
					@Override
					public void run() {
						try {
							root.removeNode();
						} catch (BackingStoreException e) {
							e.printStackTrace();
						}
					}
				});
			}
			return root;
		}
		
	}
}
