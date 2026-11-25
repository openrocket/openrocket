package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.ServicesForTesting;
import info.openrocket.swing.gui.components.CollapsiblePanel;
import info.openrocket.swing.gui.util.CheckList;

public class MotorInformationPanelTest {

	private static ThrustCurveMotorSetDatabase testDatabase;

	@BeforeAll
	public static void setUp() {
		testDatabase = new ThrustCurveMotorSetDatabase();
		Module applicationModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Module motorDbModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ThrustCurveMotorSetDatabase.class).toProvider(() -> testDatabase);
			}
		};
		Injector injector = Guice.createInjector(
				Modules.override(applicationModule).with(pluginModule, motorDbModule));
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
		JLabel sourceLabel = getField(panel, "sourceLabel", JLabel.class);
		JLabel sourceFileLabel = getField(panel, "sourceFileLabel", JLabel.class);

		panel.updateData(List.of(dbMotor), dbMotor);
		assertEquals("ThrustCurve.org", sourceLabel.getText());
		assertNull(sourceLabel.getToolTipText());
		assertFalse(sourceFileLabel.isVisible());
		assertEquals("", sourceFileLabel.getText());
		assertNull(sourceFileLabel.getToolTipText());

		panel.updateData(List.of(userMotor), userMotor);
		assertEquals("User thrust curve", sourceLabel.getText());
		assertEquals("/Users/example/OpenRocket/ThrustCurves/AeroTech_M1550R.eng", sourceLabel.getToolTipText());
		assertTrue(sourceFileLabel.isVisible());
		assertEquals("AeroTech_M1550R.eng", sourceFileLabel.getText());
		assertEquals("/Users/example/OpenRocket/ThrustCurves/AeroTech_M1550R.eng", sourceFileLabel.getToolTipText());

		panel.updateData(List.of(userMotorWindowsPath), userMotorWindowsPath);
		assertEquals("User thrust curve", sourceLabel.getText());
		assertEquals("C:\\Users\\example\\ThrustCurves\\custom_motor.rse", sourceLabel.getToolTipText());
		assertTrue(sourceFileLabel.isVisible());
		assertEquals("custom_motor.rse", sourceFileLabel.getText());
		assertEquals("C:\\Users\\example\\ThrustCurves\\custom_motor.rse", sourceFileLabel.getToolTipText());

		panel.updateData(List.of(userMotorNoPath), userMotorNoPath);
		assertEquals("User thrust curve", sourceLabel.getText());
		assertNull(sourceLabel.getToolTipText());
		assertFalse(sourceFileLabel.isVisible());
		assertEquals("", sourceFileLabel.getText());

		panel.updateData(List.of(userDbMotor), userDbMotor);
		assertEquals("User thrust curve", sourceLabel.getText());
		assertEquals("/Users/example/OpenRocket/ThrustCurves/user_motors.db", sourceLabel.getToolTipText());
		assertTrue(sourceFileLabel.isVisible());
		assertEquals("user_motors.db", sourceFileLabel.getText());
		assertEquals("/Users/example/OpenRocket/ThrustCurves/user_motors.db", sourceFileLabel.getToolTipText());

		panel.clearData();
		assertEquals("", sourceLabel.getText());
		assertNull(sourceLabel.getToolTipText());
		assertFalse(sourceFileLabel.isVisible());
		assertEquals("", sourceFileLabel.getText());
		assertNull(sourceFileLabel.getToolTipText());
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

	@Test
	@SuppressWarnings("unchecked")
	public void testCollapsibleMotorSourceChecklistFiltering() throws Exception {
		MotorSource tcSource = MotorSource.forDatabaseSource("thrustcurve.org");
		MotorSource rasaeroSource = MotorSource.forDatabaseSource("RASAero");
		MotorSource manualSource = MotorSource.forDatabaseSource("manual");

		assertEquals("ThrustCurve.org", tcSource.getDisplayName());
		assertEquals("RASAero", rasaeroSource.getDisplayName());
		assertEquals("OpenRocket", manualSource.getDisplayName());
		assertEquals("User thrust curve", MotorSource.USER_DEFINED.getDisplayName());
		assertEquals("Unknown", MotorSource.UNKNOWN.getDisplayName());
		assertEquals(tcSource, MotorSource.forDatabaseSource("ThrustCurve.org"));

		ThrustCurveMotor tcOnly = createMotor("H123", "thrustcurve.org", false, "");
		ThrustCurveMotor rasaeroOnly = createMotor("Dart", "RASAero", false, "");
		ThrustCurveMotor manualOnly = createMotor("A6", "manual", false, "");
		ThrustCurveMotor userOnly = createMotor("M1550R", "", true,
				"/Users/example/ThrustCurves/AeroTech_M1550R.eng");
		ThrustCurveMotor mixedDb = createMotor("K550W", "thrustcurve.org", false, "",
				new double[] { 0.0, 0.5, 2.0 }, new double[] { 0.0, 550.0, 0.0 }, "mixed-db");
		ThrustCurveMotor mixedUser = createMotor("K550W", "", true,
				"/Users/example/ThrustCurves/custom_k550.eng",
				new double[] { 0.0, 1.5, 2.0 }, new double[] { 0.0, 900.0, 0.0 }, "mixed-user");

		testDatabase = new ThrustCurveMotorSetDatabase();
		testDatabase.addMotor(tcOnly);
		testDatabase.addMotor(rasaeroOnly);
		testDatabase.addMotor(manualOnly);
		testDatabase.addMotor(userOnly);
		testDatabase.addMotor(mixedDb);
		testDatabase.addMotor(mixedUser);

		ThrustCurveMotorSelectionPanel selectionPanel = new ThrustCurveMotorSelectionPanel();
		JTable table = getField(selectionPanel, "table", JTable.class);
		MotorFilterPanel filterPanel = getField(selectionPanel, "motorFilterPanel", MotorFilterPanel.class);
		CollapsiblePanel sourceCollapsible = getField(filterPanel, "sourceCollapsiblePanel", CollapsiblePanel.class);
		CheckList<MotorSource> sourceCheckList = getField(filterPanel, "sourceCheckList", CheckList.class);

		assertNotNull(sourceCollapsible);
		assertFalse(sourceCollapsible.isExpanded(), "Motor source section should be collapsed by default");
		assertEquals("Motor Source", sourceCollapsible.getHeaderButton().getText());
		assertEquals(4, sourceCheckList.getCheckedItems().size(),
				"All motor sources should be checked initially");
		assertTrue(sourceCheckList.getUncheckedItems().isEmpty(),
				"No motor sources should be unchecked initially");
		assertEquals(5, table.getRowCount());

		// Uncheck RASAero only: Dart disappears while ThrustCurve.org, manual, user, and mixed stay.
		sourceCheckList.setUncheckedItems(List.of(rasaeroSource));
		assertEquals(4, table.getRowCount());
		assertEquals("Motor Source (3/4)", sourceCollapsible.getHeaderButton().getText());
		assertEquals(-1, findTableRowByDesignation(table, "Dart"));

		// Show only user-defined curves: userOnly and mixedSet remain visible.
		sourceCheckList.setCheckedItems(List.of(MotorSource.USER_DEFINED));
		assertEquals(2, table.getRowCount());
		assertEquals("Motor Source (1/4)", sourceCollapsible.getHeaderButton().getText());

		// Select the mixed K550W row while only user-defined is checked: must select the user curve.
		int mixedRow = findTableRowByDesignation(table, "K550W");
		assertTrue(mixedRow >= 0, "Expected K550W row to be visible when user-defined is checked");
		table.setRowSelectionInterval(mixedRow, mixedRow);
		assertEquals(mixedUser, selectionPanel.getSelectedMotor());
		assertEquals(List.of(mixedUser), selectionPanel.getFilteredCurves());

		// Switching to ThrustCurve.org only while mixedUser is selected must switch to mixedDb.
		sourceCheckList.setCheckedItems(List.of(tcSource));
		assertEquals(2, table.getRowCount());
		assertEquals(mixedDb, selectionPanel.getSelectedMotor());
		assertEquals(List.of(mixedDb), selectionPanel.getFilteredCurves());

		// Selecting userOnly, then excluding user-defined clears selection.
		sourceCheckList.setCheckedItems(List.of(MotorSource.USER_DEFINED));
		int userOnlyRow = findTableRowByDesignation(table, "M1550R");
		assertTrue(userOnlyRow >= 0, "Expected M1550R row to be visible when user-defined is checked");
		table.setRowSelectionInterval(userOnlyRow, userOnlyRow);
		assertEquals(userOnly, selectionPanel.getSelectedMotor());

		sourceCheckList.setCheckedItems(List.of(tcSource));
		assertNull(selectionPanel.getSelectedMotor());

		// Checking all restores all 5 rows and resets the header title.
		sourceCheckList.checkAll();
		assertEquals(5, table.getRowCount());
		assertEquals("Motor Source", sourceCollapsible.getHeaderButton().getText());
	}

	private static ThrustCurveMotor createMotor(String designation, String dataSource, boolean userDefined,
			String sourceFile) {
		return createMotor(designation, dataSource, userDefined, sourceFile,
				new double[] { 0.0, 1.0, 2.0 }, new double[] { 0.0, 1550.0, 0.0 },
				"digest-" + designation + "-" + userDefined);
	}

	private static ThrustCurveMotor createMotor(String designation, String dataSource, boolean userDefined,
			String sourceFile, double[] timePoints, double[] thrustPoints, String digest) {
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
				.setTimePoints(timePoints)
				.setThrustPoints(thrustPoints)
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.28, 0, 0, 4.5),
						new Coordinate(0.28, 0, 0, 3.2),
						new Coordinate(0.28, 0, 0, 2.0)
				})
				.setInitialMass(4.5)
				.setDigest(digest)
				.build();
	}

	private static <T> T getField(Object target, String name, Class<T> type) throws Exception {
		Class<?> cls = target.getClass();
		while (cls != null) {
			try {
				Field field = cls.getDeclaredField(name);
				field.setAccessible(true);
				return type.cast(field.get(target));
			} catch (NoSuchFieldException ignored) {
				cls = cls.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	private static int findTableRowByDesignation(JTable table, String designation) {
		ThrustCurveMotorDatabaseModel model = (ThrustCurveMotorDatabaseModel) table.getModel();
		for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
			int modelRow = table.convertRowIndexToModel(viewRow);
			if (designation.equals(model.getMotorSet(modelRow).getDesignation())) {
				return viewRow;
			}
		}
		return -1;
	}
}
