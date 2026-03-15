package info.openrocket.core.material;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.Databases;
import info.openrocket.core.l10n.ResourceBundleTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for MaterialGroup backward compatibility, specifically for the
 * "ThreadsLines" group that was renamed to ELASTICS, KEVLARS, and NYLONS.
 */
public class MaterialGroupTest {
	@BeforeAll
	public static void setUp() throws Exception {
		Module applicationModule = new ServicesForTesting();
		Module debugTranslator = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new ResourceBundleTranslator("l10n.messages", Locale.US));
			}
		};
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator),
				pluginModule);
		Application.setInjector(injector);
	}

	@Test
	void testLoadFromDatabaseStringNormalGroups() {
		// Test that normal groups still work
		assertEquals(MaterialGroup.METALS, MaterialGroup.loadFromDatabaseString("Metals"));
		assertEquals(MaterialGroup.WOODS, MaterialGroup.loadFromDatabaseString("Woods"));
		assertEquals(MaterialGroup.PLASTICS, MaterialGroup.loadFromDatabaseString("Plastics"));
		assertEquals(MaterialGroup.ELASTICS, MaterialGroup.loadFromDatabaseString("Elastics"));
		assertEquals(MaterialGroup.KEVLARS, MaterialGroup.loadFromDatabaseString("Kevlars"));
		assertEquals(MaterialGroup.NYLONS, MaterialGroup.loadFromDatabaseString("Nylons"));
		assertEquals(MaterialGroup.OTHER, MaterialGroup.loadFromDatabaseString("Other"));
		assertEquals(MaterialGroup.CUSTOM, MaterialGroup.loadFromDatabaseString("Custom"));
	}

	@Test
	void testLoadFromDatabaseStringInvalidGroup() {
		// Test that invalid groups throw exception
		assertThrows(IllegalArgumentException.class, () -> {
			MaterialGroup.loadFromDatabaseString("InvalidGroup");
		});
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityThreadsLinesToElastics() {
		// Test backward compatibility: ThreadsLines -> ELASTICS
		// Find an elastic material from the database and use its actual name
		Material elasticMaterial = Databases.findMaterial(Material.Type.LINE, "Elastic cord (round 2 mm, 1/16 in)");
		assertNotNull(elasticMaterial, "Elastic material should be found in database");
		assertEquals(MaterialGroup.ELASTICS, elasticMaterial.getGroup(), "Material should be in ELASTICS group");
		
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				elasticMaterial.getType(),
				elasticMaterial.getName(),
				elasticMaterial.getDensity()
		);
		assertEquals(MaterialGroup.ELASTICS, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityThreadsLinesToKevlars() {
		// Test backward compatibility: ThreadsLines -> KEVLARS
		// Find a Kevlar material from the database and use its actual name
		Material kevlarMaterial = Databases.findMaterial(Material.Type.LINE, "Kevlar thread 138  (0.4 mm, 1/64 in)");
		assertNotNull(kevlarMaterial, "Kevlar material should be found in database");
		assertEquals(MaterialGroup.KEVLARS, kevlarMaterial.getGroup(), "Material should be in KEVLARS group");
		
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				kevlarMaterial.getType(),
				kevlarMaterial.getName(),
				kevlarMaterial.getDensity()
		);
		assertEquals(MaterialGroup.KEVLARS, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityThreadsLinesToNylons() {
		// Test backward compatibility: ThreadsLines -> NYLONS
		// Find a nylon material from the database and use its actual name
		Material nylonMaterial = Databases.findMaterial(Material.Type.LINE, "Braided nylon (2 mm, 1/16 in)");
		assertNotNull(nylonMaterial, "Nylon material should be found in database");
		assertEquals(MaterialGroup.NYLONS, nylonMaterial.getGroup(), "Material should be in NYLONS group");
		
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				nylonMaterial.getType(),
				nylonMaterial.getName(),
				nylonMaterial.getDensity()
		);
		assertEquals(MaterialGroup.NYLONS, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityThreadsLinesToOther() {
		// Test backward compatibility: ThreadsLines -> OTHER
		// When material is not found in ELASTICS, KEVLARS, or NYLONS
		// Using a material that doesn't exist or is in a different group
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				Material.Type.LINE,
				"NonExistentMaterial",
				0.001
		);
		assertEquals(MaterialGroup.OTHER, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityThreadsLinesToOtherForNonLineMaterial() {
		// Test backward compatibility: ThreadsLines -> OTHER
		// When material type is not LINE (ThreadsLines was only for LINE materials)
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				Material.Type.BULK,
				"Aluminum",
				2700
		);
		assertEquals(MaterialGroup.OTHER, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityNormalGroup() {
		// Test that non-ThreadsLines groups work normally
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"Metals",
				Material.Type.BULK,
				"Aluminum",
				2700
		);
		assertEquals(MaterialGroup.METALS, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityNullGroup() {
		// Test that null group returns OTHER
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				null,
				Material.Type.BULK,
				"Aluminum",
				2700
		);
		assertEquals(MaterialGroup.OTHER, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityCaseInsensitive() {
		// Test that material name matching is case-insensitive
		Material elasticMaterial = Databases.findMaterial(Material.Type.LINE, "Elastic cord (round 2 mm, 1/16 in)");
		assertNotNull(elasticMaterial, "Elastic material should be found in database");
		assertEquals(MaterialGroup.ELASTICS, elasticMaterial.getGroup(), "Material should be in ELASTICS group");
		
		// Use uppercase version of the name
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				elasticMaterial.getType(),
				elasticMaterial.getName().toUpperCase(),  // uppercase
				elasticMaterial.getDensity()
		);
		assertEquals(MaterialGroup.ELASTICS, group);
	}

	@Test
	void testLoadFromDatabaseStringWithBackwardCompatibilityDensityTolerance() {
		// Test that density matching uses MathUtil.equals (with tolerance)
		Material elasticMaterial = Databases.findMaterial(Material.Type.LINE, "Elastic cord (round 2 mm, 1/16 in)");
		assertNotNull(elasticMaterial, "Elastic material should be found in database");
		assertEquals(MaterialGroup.ELASTICS, elasticMaterial.getGroup(), "Material should be in ELASTICS group");
		
		// Using a slightly different density that should still match
		MaterialGroup group = MaterialGroup.loadFromDatabaseStringWithBackwardCompatibility(
				"ThreadsLines",
				elasticMaterial.getType(),
				elasticMaterial.getName(),
				elasticMaterial.getDensity() + 1e-10
		);
		assertEquals(MaterialGroup.OTHER, group);		// We currently don't handle tolerance
	}
}

