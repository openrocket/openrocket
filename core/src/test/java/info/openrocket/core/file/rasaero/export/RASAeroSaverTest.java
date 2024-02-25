package info.openrocket.core.file.rasaero.export;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.database.ComponentPresetDao;
import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.rasaero.importt.RASAeroLoader;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class RASAeroSaverTest {
    // TODO: export a complex design
    // TODO: check recovery
    // TODO: check sims (including weights and CG)

    @BeforeAll
    public static void setup() {
        Module applicationModule = new ServicesForTesting();

        Module pluginModule = new PluginModule();

        Module debugTranslator = new AbstractModule() {
            @Override
            protected void configure() {
                bind(Translator.class).toInstance(new DebugTranslator(null));
            }
        };

        Module dbOverrides = new AbstractModule() {
            @Override
            protected void configure() {
                bind(ComponentPresetDao.class).toProvider(new EmptyComponentDbProvider());
                bind(MotorDatabase.class).toProvider(new MotorDbProvider());
            }
        };

        Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator),
                pluginModule, dbOverrides);
        Application.setInjector(injector);
    }

    @Test
    public void testSingleStage() {
        OpenRocketDocument originalDocument = loadRocket("01.One-stage.ork");
        try {
            // Convert to RASAero XML
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);

            assertEquals(3, warnings.size(), " incorrect amount of RASAero export warnings");
            assertEquals(0, errors.size(), " incorrect amount of RASAero export errors");

            // Write to .CDX1 file
            Path output = Files.createTempFile("01.One-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assertions.assertNotNull(stream, "Could not open 01.One-stage.CDX1");
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> originalChildren = originalDocument.getRocket().getAllChildren();
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(originalChildren.size(), importedChildren.size(), " Number of total children doesn't match");

            // TODO: check all components
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTwoStage() {
        OpenRocketDocument originalDocument = loadRocket("02.Two-stage.ork");
        try {
            // Convert to RASAero XML
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);

            assertEquals(2, warnings.size(), " incorrect amount of RASAero export warnings");
            assertEquals(0, errors.size(), " incorrect amount of RASAero export errors");

            // Write to .CDX1 file
            Path output = Files.createTempFile("02.Two-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assertions.assertNotNull(stream, "Could not open 02.Two-stage.CDX1");
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(18, importedChildren.size(), " Number of total children doesn't match");
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testThreeStage() {
        OpenRocketDocument originalDocument = loadRocket("03.Three-stage.ork");
        try {
            // Convert to RASAero XML
            WarningSet warnings = new WarningSet();
            ErrorSet errors = new ErrorSet();
            String result = new RASAeroSaver().marshalToRASAero(originalDocument, warnings, errors);

            assertEquals(2, warnings.size(), " incorrect amount of RASAero export warnings");
            assertEquals(0, errors.size(), " incorrect amount of RASAero export errors");

            // Write to .CDX1 file
            Path output = Files.createTempFile("03.Three-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assertions.assertNotNull(stream, "Could not open 03.Three-stage.CDX1");
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(21, importedChildren.size(), " Number of total children doesn't match");
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OpenRocketDocument loadRocket(String fileName) {
        GeneralRocketLoader loader = new GeneralRocketLoader(new File(fileName));
        InputStream is = this.getClass().getResourceAsStream("/file/rasaero/export/" + fileName);
        String failMsg = String.format("Problem in unit test, cannot find %s", fileName);
        assertNotNull(is, failMsg);

        OpenRocketDocument rocketDoc = null;
        try {
            rocketDoc = loader.load(is, fileName);
        } catch (RocketLoadException e) {
            fail("RocketLoadException while loading file " + fileName + " : " + e.getMessage());
        }

        try {
            is.close();
        } catch (IOException e) {
            fail("Unable to close input stream for file " + fileName + ": " + e.getMessage());
        }

        return rocketDoc;
    }

    private static class EmptyComponentDbProvider implements Provider<ComponentPresetDao> {

        final ComponentPresetDao db = new ComponentPresetDatabase();

        @Override
        public ComponentPresetDao get() {
            return db;
        }
    }

    private static class MotorDbProvider implements Provider<ThrustCurveMotorSetDatabase> {

        final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();

        public MotorDbProvider() {
        }

        @Override
        public ThrustCurveMotorSetDatabase get() {
            return db;
        }
    }
}
