package net.sf.openrocket.file.rasaero.export;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;
import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.rasaero.importt.RASAeroLoader;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class RASAeroSaverTest {
    // TODO: export a complex design
    // TODO: check recovery
    // TODO: check sims (including weights and CG)

    @BeforeClass
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

        Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator), pluginModule, dbOverrides);
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

            assertEquals(" incorrect amount of RASAero export warnings", 3, warnings.size());
            assertEquals(" incorrect amount of RASAero export errors", 0, errors.size());

            // Write to .CDX1 file
            Path output = Files.createTempFile("01.One-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assert.assertNotNull("Could not open 01.One-stage.CDX1", stream);
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> originalChildren = originalDocument.getRocket().getAllChildren();
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(" Number of total children doesn't match",
                    originalChildren.size(), importedChildren.size());

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

            assertEquals(" incorrect amount of RASAero export warnings", 2, warnings.size());
            assertEquals(" incorrect amount of RASAero export errors", 0, errors.size());

            // Write to .CDX1 file
            Path output = Files.createTempFile("02.Two-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assert.assertNotNull("Could not open 02.Two-stage.CDX1", stream);
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(" Number of total children doesn't match",
                    18, importedChildren.size());
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

            assertEquals(" incorrect amount of RASAero export warnings", 2, warnings.size());
            assertEquals(" incorrect amount of RASAero export errors", 0, errors.size());

            // Write to .CDX1 file
            Path output = Files.createTempFile("03.Three-stage", ".CDX1");
            Files.write(output, result.getBytes(StandardCharsets.UTF_8));

            // Read the file
            RASAeroLoader loader = new RASAeroLoader();
            InputStream stream = new FileInputStream(output.toFile());
            Assert.assertNotNull("Could not open 03.Three-stage.CDX1", stream);
            OpenRocketDocument importedDocument = OpenRocketDocumentFactory.createEmptyRocket();
            DocumentLoadingContext context = new DocumentLoadingContext();
            context.setOpenRocketDocument(importedDocument);
            context.setMotorFinder(new DatabaseMotorFinder());
            loader.loadFromStream(context, new BufferedInputStream(stream), null);
            Rocket importedRocket = importedDocument.getRocket();

            // Test children counts
            List<RocketComponent> importedChildren = importedRocket.getAllChildren();
            assertEquals(" Number of total children doesn't match",
                    21, importedChildren.size());
        } catch (IllegalStateException ise) {
            fail(ise.getMessage());
        } catch (RocketLoadException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OpenRocketDocument loadRocket(String fileName) {
        GeneralRocketLoader loader = new GeneralRocketLoader(new File(fileName));
        InputStream is = this.getClass().getResourceAsStream(fileName);
        String failMsg = String.format("Problem in unit test, cannot find %s", fileName);
        assertNotNull(failMsg, is);

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
