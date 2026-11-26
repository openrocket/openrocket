package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.file.DatabaseMotorFinder;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the import of RASAero recovery devices, including RASAero's special
 * cases: disabled events, devices without a parachute, two apogee deployments,
 * and unknown device or event types.
 */
public class RASAeroRecoveryImportTest extends BaseTestCase {
    private static final double EPSILON = 0.0001;

    /**
     * The Complex.Two-Stage.CDX1 design has two parachutes: a 12" one deploying at apogee and
     * a 24" one deploying at 600 ft. RASAero does not specify their position, so the importer
     * places device 1 in the tube below the vent band and device 2 in the tube above it,
     * both 1.125 calibers from the top of their tube.
     */
    @Test
    public void testImportsParachuteParameters() throws Exception {
        Rocket rocket = load(design()).getRocket();

        Parachute device1 = getRecoveryDevice(rocket, "Recovery Event 1");
        assertEquals(12 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, device1.getDiameter(), EPSILON);
        assertEquals(0.8, device1.getCD(), EPSILON);
        assertEquals(device1.getDiameter(), device1.getLineLength(), EPSILON);
        // 12" / 6" = 2 lines, rounded up to the minimum of 6
        assertEquals(6, device1.getLineCount());
        DeploymentConfiguration config1 = device1.getDeploymentConfigurations().getDefault();
        assertEquals(1000 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE, config1.getDeployAltitude(),
                EPSILON);
        assertEquals(DeploymentConfiguration.DeployEvent.APOGEE, config1.getDeployEvent());
        BodyTube parent1 = assertInstanceOf(BodyTube.class, device1.getParent());
        assertEquals(36 * 0.0254, parent1.getLength(), EPSILON);
        assertEquals(parent1.getOuterRadius() * 2.25, device1.getAxialOffset(), EPSILON);

        Parachute device2 = getRecoveryDevice(rocket, "Recovery Event 2");
        assertEquals(24 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, device2.getDiameter(), EPSILON);
        assertEquals(0.8, device2.getCD(), EPSILON);
        // 24" / 6" = 4 lines, rounded up to the minimum of 6
        assertEquals(6, device2.getLineCount());
        DeploymentConfiguration config2 = device2.getDeploymentConfigurations().getDefault();
        assertEquals(600 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE, config2.getDeployAltitude(),
                EPSILON);
        assertEquals(DeploymentConfiguration.DeployEvent.ALTITUDE, config2.getDeployEvent());
        BodyTube parent2 = assertInstanceOf(BodyTube.class, device2.getParent());
        assertEquals(6 * 0.0254, parent2.getLength(), EPSILON);
        assertEquals(parent2.getOuterRadius() * 2.25, device2.getAxialOffset(), EPSILON);
    }

    /**
     * Recovery device 2 is only imported when device 1 is enabled, so disabling
     * device 1's event must remove both devices.
     */
    @Test
    public void testSkipsAllDevicesWhenFirstEventDisabled() throws Exception {
        Rocket rocket = load(design().replaceFirst("<Event1>True</Event1>", "<Event1>False</Event1>")).getRocket();
        assertEquals(0, getRecoveryDevices(rocket).size());
    }

    /**
     * A device with event type None is ignored, and device 2 requires device 1's
     * event type to not be None, so a None event type on device 1 removes both.
     */
    @Test
    public void testSkipsDevicesWithNoneEventType() throws Exception {
        RASAeroLoader loader = new RASAeroLoader();
        Rocket rocket = load(loader, design().replaceFirst("<EventType1>Apogee</EventType1>",
                "<EventType1>None</EventType1>")).getRocket();
        assertEquals(0, getRecoveryDevices(rocket).size());
    }

    /**
     * RASAero's device type None means ballistic/tumble descent: the importer uses
     * a zero-diameter parachute that keeps the device's CD and deployment settings.
     */
    @Test
    public void testImportsDeviceWithoutParachuteAsZeroDiameterParachute() throws Exception {
        Rocket rocket = load(design().replaceFirst("<DeviceType1>Parachute</DeviceType1>",
                "<DeviceType1>None</DeviceType1>")).getRocket();

        Parachute device1 = getRecoveryDevice(rocket, "Recovery Event 1");
        assertEquals(0.0, device1.getDiameter(), EPSILON);
        assertEquals(0.8, device1.getCD(), EPSILON);
        DeploymentConfiguration config1 = device1.getDeploymentConfigurations().getDefault();
        assertEquals(1000 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE, config1.getDeployAltitude(),
                EPSILON);
        assertEquals(DeploymentConfiguration.DeployEvent.APOGEE, config1.getDeployEvent());

        // Device 2 is still imported, as device 1's event type is not None
        Parachute device2 = getRecoveryDevice(rocket, "Recovery Event 2");
        assertEquals(24 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, device2.getDiameter(), EPSILON);
    }

    /**
     * Two apogee deployments are not allowed: if device 2 is also set to apogee,
     * it must be changed to an altitude deployment, with a warning.
     */
    @Test
    public void testConvertsSecondApogeeDeviceToAltitudeDeployment() throws Exception {
        RASAeroLoader loader = new RASAeroLoader();
        Rocket rocket = load(loader, design().replaceFirst("<EventType2>Altitude</EventType2>",
                "<EventType2>Apogee</EventType2>")).getRocket();

        Parachute device2 = getRecoveryDevice(rocket, "Recovery Event 2");
        assertEquals(DeploymentConfiguration.DeployEvent.ALTITUDE,
                device2.getDeploymentConfigurations().getDefault().getDeployEvent());
        assertTrue(loader.getWarnings().stream()
                .anyMatch(w -> w.toString().contains("Setting recovery device 2 to altitude")));
    }

    /**
     * An unknown device type is ignored with a warning; the other device is still imported.
     */
    @Test
    public void testWarnsOnUnknownDeviceType() throws Exception {
        RASAeroLoader loader = new RASAeroLoader();
        Rocket rocket = load(loader, design().replaceFirst("<DeviceType1>Parachute</DeviceType1>",
                "<DeviceType1>Blah</DeviceType1>")).getRocket();

        assertEquals(1, getRecoveryDevices(rocket).size());
        Parachute device2 = getRecoveryDevice(rocket, "Recovery Event 2");
        assertEquals(24 / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH, device2.getDiameter(), EPSILON);
        assertTrue(loader.getWarnings().stream()
                .anyMatch(w -> w.toString().contains("Unknown recovery device type Blah")));
    }

    /**
     * The shroud line count is the parachute diameter divided by 6", with a minimum
     * of 6 lines. A 48" parachute therefore gets 8 lines.
     */
    @Test
    public void testComputesShroudLineCountFromDiameter() throws Exception {
        Rocket rocket = load(design().replaceFirst("<Size1>12</Size1>", "<Size1>48</Size1>")).getRocket();

        assertEquals(8, getRecoveryDevice(rocket, "Recovery Event 1").getLineCount());
        assertEquals(6, getRecoveryDevice(rocket, "Recovery Event 2").getLineCount());
    }

    /**
     * An unknown deployment event defaults to apogee with a warning.
     */
    @Test
    public void testWarnsOnUnknownDeployEventType() throws Exception {
        RASAeroLoader loader = new RASAeroLoader();
        Rocket rocket = load(loader, design().replaceFirst("<EventType1>Apogee</EventType1>",
                "<EventType1>Foo</EventType1>")).getRocket();

        Parachute device1 = getRecoveryDevice(rocket, "Recovery Event 1");
        assertEquals(DeploymentConfiguration.DeployEvent.APOGEE,
                device1.getDeploymentConfigurations().getDefault().getDeployEvent());
        assertTrue(loader.getWarnings().stream()
                .anyMatch(w -> w.toString().contains("Unknown deployment event: Foo")));
    }

    private List<Parachute> getRecoveryDevices(Rocket rocket) {
        List<Parachute> devices = new ArrayList<>();
        for (RocketComponent component : rocket.getAllChildren()) {
            if (component instanceof Parachute) {
                devices.add((Parachute) component);
            }
        }
        return devices;
    }

    private Parachute getRecoveryDevice(Rocket rocket, String name) {
        for (Parachute device : getRecoveryDevices(rocket)) {
            if (device.getName().equals(name)) {
                return device;
            }
        }
        assertNotNull(null, "No recovery device named " + name);
        return null;
    }

    private String design() throws IOException {
        try (InputStream stream = RASAeroRecoveryImportTest.class
                .getResourceAsStream("/file/rasaero/importt/Complex.Two-Stage.CDX1")) {
            assertNotNull(stream, "Could not open Complex.Two-Stage.CDX1");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private OpenRocketDocument load(RASAeroLoader loader, String design) throws RocketLoadException, IOException {
        OpenRocketDocument doc = OpenRocketDocumentFactory.createEmptyRocket();
        DocumentLoadingContext context = new DocumentLoadingContext();
        context.setOpenRocketDocument(doc);
        context.setMotorFinder(new DatabaseMotorFinder());
        loader.loadFromStream(context, new ByteArrayInputStream(design.getBytes(StandardCharsets.UTF_8)), null);
        return doc;
    }

    private OpenRocketDocument load(String design) throws RocketLoadException, IOException {
        return load(new RASAeroLoader(), design);
    }
}
