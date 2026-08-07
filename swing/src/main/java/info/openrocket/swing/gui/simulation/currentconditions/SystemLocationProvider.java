package info.openrocket.swing.gui.simulation.currentconditions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.openrocket.core.arch.SystemInfo;

/**
 * Reads the current position from the native location service on macOS or Windows.
 */
public class SystemLocationProvider {
	private static final long LOCATION_TIMEOUT_SECONDS = 20;
	private static final String MAC_OS_HELPER =
			"/info/openrocket/swing/currentconditions/macos/OpenRocketLocationHelper.zip";
	private static final List<String> GEOCLUE_COMMANDS = List.of(
			"/usr/libexec/geoclue-2.0/demos/where-am-i",
			"/usr/lib/geoclue-2.0/demos/where-am-i",
			"/usr/lib64/geoclue-2.0/demos/where-am-i",
			"where-am-i");
	private static final Pattern GEOCLUE_LATITUDE = Pattern.compile("(?m)^Latitude:\\s*([+-]?\\d+(?:\\.\\d+)?)");
	private static final Pattern GEOCLUE_LONGITUDE = Pattern.compile("(?m)^Longitude:\\s*([+-]?\\d+(?:\\.\\d+)?)");
	private static final Pattern GEOCLUE_ALTITUDE = Pattern.compile("(?m)^Altitude:\\s*([+-]?\\d+(?:\\.\\d+)?)");
	private static final Pattern GEOCLUE_ACCURACY = Pattern.compile("(?m)^Accuracy:\\s*([+-]?\\d+(?:\\.\\d+)?)");

	private static final String WINDOWS_SCRIPT = """
			$ErrorActionPreference = 'Stop'
			Add-Type -AssemblyName System.Runtime.WindowsRuntime
			[Windows.Devices.Geolocation.Geolocator, Windows.Devices.Geolocation, ContentType=WindowsRuntime] | Out-Null
			[Windows.Devices.Geolocation.Geoposition, Windows.Devices.Geolocation, ContentType=WindowsRuntime] | Out-Null
			$locator = [Windows.Devices.Geolocation.Geolocator]::new()
			$locator.DesiredAccuracyInMeters = 10
			$operation = $locator.GetGeopositionAsync()
			$asTask = ([System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object {
			  $_.Name -eq 'AsTask' -and $_.IsGenericMethod -and $_.GetParameters().Count -eq 1
			})[0]
			$task = $asTask.MakeGenericMethod([Windows.Devices.Geolocation.Geoposition]).Invoke($null, @($operation))
			if (-not $task.Wait(15000)) { throw 'Windows Location Services timed out.' }
			$coordinate = $task.Result.Coordinate
			$position = $coordinate.Point.Position
			$culture = [System.Globalization.CultureInfo]::InvariantCulture
			$position.Latitude.ToString($culture) + "`t" + $position.Longitude.ToString($culture) + "`t" +
			  $position.Altitude.ToString($culture) + "`t" + $coordinate.Accuracy.ToString($culture)
			""";

	public DeviceLocation locate() throws LocationException {
		return switch (SystemInfo.getPlatform()) {
			case MAC_OS -> locateMacOs();
			case WINDOWS -> run(List.of("powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy",
					"Bypass", "-Command", WINDOWS_SCRIPT), "Windows Location Services");
			case UNIX -> locateLinux();
		};
	}

	private static DeviceLocation locateMacOs() throws LocationException {
		Path directory = null;
		try {
			directory = Files.createTempDirectory("openrocket-location-");
			Path archive = directory.resolve("OpenRocketLocationHelper.zip");
			try (InputStream input = SystemLocationProvider.class.getResourceAsStream(MAC_OS_HELPER)) {
				if (input == null) {
					throw new LocationException("The macOS location helper is missing from this OpenRocket build.");
				}
				Files.copy(input, archive);
			}

			Process extract = new ProcessBuilder("/usr/bin/ditto", "-x", "-k", archive.toString(),
					directory.toString()).redirectErrorStream(true).start();
			if (!extract.waitFor(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS) || extract.exitValue() != 0) {
				throw new LocationException("Could not prepare the macOS location helper.");
			}

			Path output = directory.resolve("location.txt");
			Path error = directory.resolve("location-error.txt");
			Path application = directory.resolve("OpenRocketLocationHelper.app");
			Process process = new ProcessBuilder("/usr/bin/open", "-W", "-n", application.toString(),
					"--stdout", output.toString(), "--stderr", error.toString()).start();
			if (!process.waitFor(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				throw new LocationException("macOS Location Services timed out.");
			}

			String location = Files.exists(output) ? Files.readString(output, StandardCharsets.UTF_8).trim() : "";
			if (location.isBlank()) {
				String message = Files.exists(error) ? Files.readString(error, StandardCharsets.UTF_8).trim() : "";
				throw new LocationException(cleanError("macOS Location Services", message));
			}
			return parse(location, "macOS Location Services");
		} catch (IOException e) {
			throw new LocationException("Could not start macOS Location Services.", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LocationException("Location request was interrupted.", e);
		} finally {
			deleteDirectory(directory);
		}
	}

	private static void deleteDirectory(Path directory) {
		if (directory == null) {
			return;
		}
		try (var paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
					// Best-effort cleanup of a temporary helper bundle.
				}
			});
		} catch (IOException ignored) {
			// Best-effort cleanup of a temporary helper bundle.
		}
	}

	private static DeviceLocation locateLinux() throws LocationException {
		IOException unavailable = null;
		for (String command : GEOCLUE_COMMANDS) {
			try {
				Process process = new ProcessBuilder(command, "--timeout=8", "--accuracy-level=8")
						.redirectErrorStream(true).start();
				if (!process.waitFor(12, TimeUnit.SECONDS)) {
					process.destroyForcibly();
					throw new LocationException("Linux GeoClue timed out.");
				}
				String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
				return parseGeoClue(output);
			} catch (IOException e) {
				unavailable = e;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new LocationException("Location request was interrupted.", e);
			}
		}
		throw new LocationException("GeoClue's where-am-i helper is unavailable. Install the geoclue demo package "
				+ "(for example, geoclue-2-demo on Debian or Ubuntu).", unavailable);
	}

	private static DeviceLocation parseGeoClue(String output) throws LocationException {
		double latitude = findGeoClueValue(GEOCLUE_LATITUDE, output, Double.NaN);
		double longitude = findGeoClueValue(GEOCLUE_LONGITUDE, output, Double.NaN);
		double altitude = findGeoClueValue(GEOCLUE_ALTITUDE, output, 0);
		double accuracy = findGeoClueValue(GEOCLUE_ACCURACY, output, Double.NaN);
		if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
				|| !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
			throw new LocationException(cleanError("Linux GeoClue", output));
		}
		return new DeviceLocation(latitude, longitude, altitude, accuracy, "Linux GeoClue");
	}

	private static double findGeoClueValue(Pattern pattern, String output, double fallback) {
		Matcher matcher = pattern.matcher(output);
		double value = fallback;
		while (matcher.find()) {
			value = Double.parseDouble(matcher.group(1));
		}
		return value;
	}

	private static DeviceLocation run(List<String> command, String source) throws LocationException {
		Process process;
		try {
			process = new ProcessBuilder(command).redirectErrorStream(true).start();
			if (!process.waitFor(LOCATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				process.destroyForcibly();
				throw new LocationException(source + " timed out.");
			}
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
			if (process.exitValue() != 0) {
				throw new LocationException(cleanError(source, output));
			}
			return parse(output, source);
		} catch (IOException e) {
			throw new LocationException("Could not start " + source + ".", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new LocationException("Location request was interrupted.", e);
		}
	}

	static DeviceLocation parse(String output, String source) throws LocationException {
		String[] values = output.lines().reduce((first, second) -> second).orElse("").split("\\t");
		if (values.length != 4) {
			throw new LocationException(source + " returned an unreadable location.");
		}
		try {
			double latitude = Double.parseDouble(values[0]);
			double longitude = Double.parseDouble(values[1]);
			double altitude = Double.parseDouble(values[2]);
			double accuracy = Double.parseDouble(values[3]);
			if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90
					|| !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
				throw new NumberFormatException("coordinates out of range");
			}
			return new DeviceLocation(latitude, longitude, altitude, accuracy, source);
		} catch (NumberFormatException e) {
			throw new LocationException(source + " returned an invalid location.", e);
		}
	}

	private static String cleanError(String source, String output) {
		if (output.isBlank()) {
			return source + " could not determine the current location.";
		}
		String oneLine = output.replaceAll("\\s+", " ").trim();
		return String.format(Locale.ROOT, "%s failed: %s", source,
				oneLine.length() > 300 ? oneLine.substring(0, 300) + "..." : oneLine);
	}
}
