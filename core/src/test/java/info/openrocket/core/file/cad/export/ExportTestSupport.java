package info.openrocket.core.file.cad.export;

import info.openrocket.core.file.cad.export.CADDimensionVerifier.Check;
import info.openrocket.core.file.cad.export.CADDimensionVerifier.Status;
import info.openrocket.core.file.cad.export.CADPartsExporter.ExportedPart;
import info.openrocket.core.file.cad.export.CADPartsExporter.Format;
import info.openrocket.core.file.cad.export.FlatPart.Dimension;
import info.openrocket.core.file.svg.export.RingSvgExporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static info.openrocket.core.file.cad.export.PartFixtures.createDisc;
import static info.openrocket.core.file.cad.export.PartFixtures.createFinSet;
import static info.openrocket.core.file.cad.export.PartFixtures.createRing;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parts, files and assertions shared by the export tests.
 */
final class ExportTestSupport {
	private ExportTestSupport() {
	}

	static CADPartsExporter createAllParts() {
		CADPartsExporter exporter = new CADPartsExporter();
		exporter.addFinSet(createFinSet());
		exporter.addCenteringRing(createRing(0.025, 0.009), List.of(new RingSvgExporter.Hole(0.012, 0.005, 0.003)));
		exporter.addCenteringRing(createDisc(), List.of());
		return exporter;
	}

	/**
	 * A file written by the exporter, together with the parts that were written.
	 */
	record Written(byte[] bytes, List<ExportedPart> parts) {
		String text() {
			return new String(bytes, StandardCharsets.ISO_8859_1);
		}

		CADDimensionVerifier.Result verify(Format format) throws IOException {
			return CADDimensionVerifier.verify(format, new ByteArrayInputStream(bytes), parts);
		}
	}

	static Written write(CADPartsExporter exporter, Format format) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		List<ExportedPart> parts = exporter.write(format, "test", out);
		return new Written(out.toByteArray(), parts);
	}

	static void assertMeasured(CADDimensionVerifier.Result result, String part, Dimension dimension,
									   double expected) {
		Check check = find(result, part, dimension);
		assertEquals(Status.MATCH, check.status(), describe(result));
		assertEquals(expected, check.measured(), 1e-6);
	}

	static double find(FlatPart part, Dimension dimension) {
		return part.getDimensions().stream().filter(d -> d.dimension() == dimension).findFirst().orElseThrow().value();
	}

	static String describe(CADDimensionVerifier.Result result) {
		StringBuilder sb = new StringBuilder("units=" + result.units());
		for (Check check : result.checks()) {
			sb.append(String.format(Locale.ROOT, "%n%s %s#%d design=%.9g measured=%.9g %s", check.part().name(),
					check.dimension().dimension(), check.dimension().index(), check.dimension().value(),
					check.measured(), check.status()));
		}
		return sb.toString();
	}

	static int count(String text, String token) {
		int count = 0;
		for (int i = text.indexOf(token); i >= 0; i = text.indexOf(token, i + 1)) {
			count++;
		}
		return count;
	}

	static void assertHasPoint(List<double[]> points, double x, double y) {
		for (double[] p : points) {
			if (Math.abs(p[0] - x) < 1e-6 && Math.abs(p[1] - y) < 1e-6) {
				return;
			}
		}
		throw new AssertionError(String.format(Locale.ROOT, "No point at (%.6f, %.6f)", x, y));
	}

	static void assertHasPoint3D(List<double[]> points, double x, double y, double z) {
		for (double[] p : points) {
			if (Math.abs(p[0] - x) < 1e-6 && Math.abs(p[1] - y) < 1e-6 && Math.abs(p[2] - z) < 1e-6) {
				return;
			}
		}
		throw new AssertionError(String.format(Locale.ROOT, "No point at (%.6f, %.6f, %.6f)", x, y, z));
	}

	static void assertHasCircle(List<double[]> circles, double x, double y, double r) {
		for (double[] c : circles) {
			if (Math.abs(c[0] - x) < 1e-6 && Math.abs(c[1] - y) < 1e-6 && Math.abs(c[2] - r) < 1e-6) {
				return;
			}
		}
		throw new AssertionError(String.format(Locale.ROOT, "No circle at (%.6f, %.6f) r=%.6f", x, y, r));
	}

	/**
	 * Returns the 10/20/40 group values of all DXF entities of the given type.
	 */
	static List<double[]> dxfEntities(String dxf, String type) {
		String[] lines = dxf.split("\n");
		List<double[]> result = new ArrayList<>();
		for (int i = 0; i + 1 < lines.length; i += 2) {
			if (!lines[i].equals("0") || !lines[i + 1].equals(type)) {
				continue;
			}
			double[] values = new double[3];
			for (int j = i + 2; j + 1 < lines.length && !lines[j].equals("0"); j += 2) {
				switch (lines[j]) {
					case "10" -> values[0] = Double.parseDouble(lines[j + 1]);
					case "20" -> values[1] = Double.parseDouble(lines[j + 1]);
					case "40" -> values[2] = Double.parseDouble(lines[j + 1]);
					default -> {
					}
				}
			}
			result.add(values);
		}
		return result;
	}

	static Check find(CADDimensionVerifier.Result result, String part, Dimension dimension) {
		for (Check check : result.checks()) {
			if (check.part().name().equals(part) && check.dimension().dimension() == dimension) {
				return check;
			}
		}
		throw new AssertionError("No check for " + part + " " + dimension);
	}
}
