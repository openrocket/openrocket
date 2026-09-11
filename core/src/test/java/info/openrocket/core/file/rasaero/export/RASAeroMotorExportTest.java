package info.openrocket.core.file.rasaero.export;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader;
import info.openrocket.core.file.rasaero.RASAeroMotorsLoader.RASAeroMotor;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the motor configuration fields written to RASAero simulations.
 */
public class RASAeroMotorExportTest {
    private static final ThrustCurveMotor SUSTAINER_MOTOR = createMotor("H148R", 0.152, 0.210);
    private static Injector originalInjector;

    @BeforeAll
    public static void setUpApplicationServices() {
        originalInjector = Application.getInjector();
        Injector injector = Guice.createInjector(
                Modules.override(new ServicesForTesting()).with(new PluginModule()));
        Application.setInjector(injector);
    }

    @AfterAll
    public static void restoreApplicationServices() {
        Application.setInjector(originalInjector);
    }

    /**
     * Motor mounts may be nested inside structural inner tubes.  The exporter must
     * still find the populated mount anywhere in the stage subtree.
     */
    @Test
    public void findsMotorFromNestedSustainerMount() {
        AxialStage sustainer = new AxialStage();
        BodyTube sustainerBody = new BodyTube(0.5, 0.05, 0.001);
        sustainer.addChild(sustainerBody);

        // This structural tube reproduces mounts nested more than one level below a body tube.
        InnerTube structuralTube = new InnerTube();
        structuralTube.setLength(0.2);
        structuralTube.setOuterRadius(0.025);
        structuralTube.setInnerRadius(0.021);
        sustainerBody.addChild(structuralTube);
        InnerTube sustainerMount = createMotorMount("Sustainer mount", 0.16);
        structuralTube.addChild(sustainerMount);

        FlightConfigurationId configurationId = new FlightConfigurationId();
        setMotor(sustainerMount, configurationId, SUSTAINER_MOTOR);

        assertSame(sustainerMount, SimulationListDTO.findMotorMount(sustainer));
    }

    /**
     * RASAero includes this plugged motor as I59WN-P, while OpenRocket stores
     * the motor designation separately from its selected delay.
     */
    @Test
    public void mapsOpenRocketDesignationToPluggedRASAeroDesignation() {
        WarningSet warnings = new WarningSet();
        List<RASAeroMotor> motors = RASAeroMotorsLoader.loadAllRASAeroMotors(warnings);
        ThrustCurveMotor openRocketMotor = createMotor("i59WN", 0.232, 0.487);

        String result = RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, openRocketMotor, warnings);

        assertEquals("I59WN-P  (AT)", result);
        assertEquals(0, warnings.size());
    }

    /**
     * A motor absent from rasp.eng must not be written as if RASAero supports it.
     */
    @Test
    public void warnsWhenMotorIsUnavailableInRASAeroCatalog() {
        WarningSet warnings = new WarningSet();
        List<RASAeroMotor> motors = RASAeroMotorsLoader.loadAllRASAeroMotors(warnings);
        ThrustCurveMotor unavailableMotor = createMotor("I999ZZ", 0.232, 0.487);

        String result = RASAeroCommonConstants.OPENROCKET_TO_RASAERO_MOTOR(motors, unavailableMotor, warnings);

        assertNull(result);
        assertEquals(1, warnings.size());
        assertTrue(containsWarning(warnings, "not available in RASAero II's rasp.eng catalog"));
    }

    private static InnerTube createMotorMount(String name, double length) {
        InnerTube mount = new InnerTube();
        mount.setName(name);
        mount.setLength(length);
        mount.setOuterRadius(0.0205);
        mount.setInnerRadius(0.019);
        mount.setMotorMount(true);
        return mount;
    }

    private static void setMotor(InnerTube mount, FlightConfigurationId configurationId, Motor motor) {
        MotorConfiguration configuration = new MotorConfiguration(mount, configurationId);
        configuration.setMotor(motor);
        mount.setMotorConfig(configuration, configurationId);
    }

    private static ThrustCurveMotor createMotor(String designation, double length, double launchMass) {
        CoordinateIF[] cgPoints = {
                new Coordinate(length / 2, 0, 0, launchMass),
                new Coordinate(length / 2, 0, 0, launchMass * 0.8),
                new Coordinate(length / 2, 0, 0, launchMass * 0.6)
        };
        return new ThrustCurveMotor.Builder()
                .setManufacturer(Manufacturer.getManufacturer("AeroTech"))
                .setDesignation(designation)
                .setCommonName(designation)
                .setDescription("RASAero export test motor")
                .setCaseInfo("Test case")
                .setMotorType(Motor.Type.RELOAD)
                .setStandardDelays(new double[] { Motor.PLUGGED_DELAY })
                .setDiameter(0.038)
                .setLength(length)
                .setTimePoints(new double[] { 0, 1, 2 })
                .setThrustPoints(new double[] { 0, 100, 0 })
                .setCGPoints(cgPoints)
                .setDigest("rasaero-export-" + designation)
                .build();
    }

    private static boolean containsWarning(WarningSet warnings, String text) {
        for (Warning warning : warnings) {
            if (warning.toString().contains(text)) {
                return true;
            }
        }
        return false;
    }
}
