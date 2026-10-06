package info.openrocket.distribution;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.Invocable;

import com.formdev.flatlaf.ui.FlatNativeLinuxLibrary;
import com.formdev.flatlaf.ui.FlatNativeMacLibrary;
import com.formdev.flatlaf.ui.FlatNativeWindowsLibrary;
import com.formdev.flatlaf.util.SystemInfo;
import com.sun.jna.Native;
import info.openrocket.core.scripting.GraalJSScriptEngineFactory;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;

/** Checks the packaged application without the development runtime classpath. */
public final class DistributionSmokeTest {

	private DistributionSmokeTest() {
	}

	public static void main(String[] args) throws Exception {
		String jar = GraalJSScriptEngineFactory.class.getProtectionDomain().getCodeSource()
				.getLocation().toString();
		require(jar.endsWith(".jar"), "Application classes must come from the distribution JAR");

		// Driver discovery exercises the merged JDBC service descriptor as well as SQLite JNI.
		try (var connection = DriverManager.getConnection("jdbc:sqlite::memory:");
				var statement = connection.createStatement();
				var result = statement.executeQuery("SELECT 6 * 7")) {
			require(result.next() && result.getInt(1) == 42, "SQLite query failed");
		}

		Path database = Files.createTempFile("openrocket-distribution-motors-", ".db");
		try {
			try (InputStream input = DistributionSmokeTest.class.getResourceAsStream(
					"/datafiles/thrustcurves/initial_motors.db")) {
				require(input != null, "Bundled motor database is missing");
				Files.copy(input, database, StandardCopyOption.REPLACE_EXISTING);
			}
			try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
					var statement = connection.createStatement();
					var result = statement.executeQuery("SELECT COUNT(*) FROM motors")) {
				require(result.next() && result.getInt(1) > 0, "Bundled motor database is empty");
			}
		} finally {
			Files.deleteIfExists(database);
		}

		var engine = new GraalJSScriptEngineFactory().getScriptEngine();
		try {
			engine.eval("function preStep(status) { return status.get() > 0; }"
					+ "function postSimpleThrustCalculation(status, thrust) { return thrust * 2; }");
			var invocable = (Invocable) engine;
			require(Boolean.TRUE.equals(invocable.invokeFunction("preStep", new AtomicInteger(1))),
					"JavaScript callback or Java interop failed");
			require(Double.valueOf(6.5).equals(invocable.invokeFunction(
					"postSimpleThrustCalculation", null, Double.valueOf(3.25))),
					"JavaScript thrust callback returned an unexpected value or type");
			require("1.234,5".equals(engine.eval("new Intl.NumberFormat('de-DE').format(1234.5)")),
					"JavaScript internationalization data failed to load");
		} finally {
			((AutoCloseable) engine).close();
		}

		require(Native.POINTER_SIZE == 8, "JNA native library failed to initialize");
		boolean flatlafLoaded = SystemInfo.isWindows ? FlatNativeWindowsLibrary.isLoaded()
				: SystemInfo.isMacOS ? FlatNativeMacLibrary.isLoaded() : FlatNativeLinuxLibrary.isLoaded();
		require(flatlafLoaded, "FlatLaf native library failed to initialize");
		require(GL.getFunctionProvider() != null, "LWJGL OpenGL native library failed to initialize");
		STBImage.stbi_failure_reason();
		System.out.println("Distribution smoke test passed: " + jar);
	}

	private static void require(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
