package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JLabel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.ServicesForTesting;

public class MotorInformationPanelTest {

	@BeforeAll
	public static void setUp() {
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(pluginModule));
		Application.setInjector(injector);
	}

	@Test
	public void testUserThrustCurveDisplayedInMotorDetailsSourceAndTooltip() throws Exception {
		ThrustCurveMotor dbMotor = createMotor("M1550R", "thrustcurve.org", false, "");
		ThrustCurveMotor userMotor = createMotor("M1550R", "", true,
				"/Users/example/OpenRocket/ThrustCurves/AeroTech_M1550R.eng");
		ThrustCurveMotor userMotorWindowsPath = createMotor("M1550R", "", true,
				"C:\\Users\\example\\ThrustCurves\\custom_motor.rse");
		ThrustCurveMotor userMotorNoPath = createMotor("M1550R", "", true, "");
		ThrustCurveMotor userDbMotor = createMotor("M1550R", "thrustcurve.org", true,
				"/Users/example/OpenRocket/ThrustCurves/user_motors.db");

		MotorInformationPanel panel = new MotorInformationPanel();
		JLabel sourceLabel = getSourceLabel(panel);

		panel.updateData(List.of(dbMotor), dbMotor);
		assertEquals("ThrustCurve.org", sourceLabel.getText());
		assertNull(sourceLabel.getToolTipText());

		panel.updateData(List.of(userMotor), userMotor);
		assertEquals("User thrust curve (AeroTech_M1550R.eng)", sourceLabel.getText());
		assertEquals("/Users/example/OpenRocket/ThrustCurves/AeroTech_M1550R.eng", sourceLabel.getToolTipText());

		panel.updateData(List.of(userMotorWindowsPath), userMotorWindowsPath);
		assertEquals("User thrust curve (custom_motor.rse)", sourceLabel.getText());
		assertEquals("C:\\Users\\example\\ThrustCurves\\custom_motor.rse", sourceLabel.getToolTipText());

		panel.updateData(List.of(userMotorNoPath), userMotorNoPath);
		assertEquals("User thrust curve", sourceLabel.getText());
		assertNull(sourceLabel.getToolTipText());

		panel.updateData(List.of(userDbMotor), userDbMotor);
		assertEquals("User thrust curve (user_motors.db)", sourceLabel.getText());
		assertEquals("/Users/example/OpenRocket/ThrustCurves/user_motors.db", sourceLabel.getToolTipText());

		panel.clearData();
		assertEquals("", sourceLabel.getText());
		assertNull(sourceLabel.getToolTipText());
	}

	@Test
	public void testLongUserSourceFileDoesNotExpandDetailsPanelPreferredWidth() {
		ThrustCurveMotor dbMotor = createMotor("M1550R", "thrustcurve.org", false, "");
		ThrustCurveMotor longUserMotor = createMotor("M1550R", "", true,
				"/Users/example/ThrustCurves/very_long_custom_experimental_user_thrustcurve_filename_2026_v2.eng");

		MotorInformationPanel panel = new MotorInformationPanel();
		panel.updateData(List.of(dbMotor), dbMotor);
		int baselineWidth = panel.getPreferredSize().width;

		panel.updateData(List.of(longUserMotor), longUserMotor);
		int userMotorWidth = panel.getPreferredSize().width;

		assertTrue(userMotorWidth <= baselineWidth,
				"Long user thrust curve filename should not expand details panel width (baseline="
						+ baselineWidth + ", actual=" + userMotorWidth + ")");
	}

	@Test
	public void testMotorHolderIndicatesUserThrustCurve() {
		ThrustCurveMotor dbMotor = createMotor("M1550R", "thrustcurve.org", false, "");
		ThrustCurveMotor userMotor = createMotor("M1550R", "", true,
				"/Users/example/ThrustCurves/AeroTech_M1550R.eng");

		assertEquals(dbMotor.getMotorName(), new MotorHolder(dbMotor, 0).toString());
		assertEquals(userMotor.getMotorName() + " (user)", new MotorHolder(userMotor, 1).toString());
	}

	private static ThrustCurveMotor createMotor(String designation, String dataSource, boolean userDefined,
			String sourceFile) {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setCode(designation)
				.setDesignation(designation)
				.setDataSource(dataSource)
				.setUserDefined(userDefined)
				.setSourceFile(sourceFile)
				.setMotorType(Motor.Type.RELOAD)
				.setStandardDelays(new double[] { Motor.PLUGGED_DELAY })
				.setDiameter(0.075)
				.setLength(0.560)
				.setTimePoints(new double[] { 0.0, 1.0, 2.0 })
				.setThrustPoints(new double[] { 0.0, 1550.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.28, 0, 0, 4.5),
						new Coordinate(0.28, 0, 0, 3.2),
						new Coordinate(0.28, 0, 0, 2.0)
				})
				.setInitialMass(4.5)
				.setDigest("digest-" + designation + "-" + userDefined)
				.build();
	}

	private static JLabel getSourceLabel(MotorInformationPanel panel) throws Exception {
		Field field = MotorInformationPanel.class.getDeclaredField("sourceLabel");
		field.setAccessible(true);
		return (JLabel) field.get(panel);
	}
}
