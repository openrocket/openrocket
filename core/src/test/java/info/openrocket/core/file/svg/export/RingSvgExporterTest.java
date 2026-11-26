package info.openrocket.core.file.svg.export;

import info.openrocket.core.rocketcomponent.InnerTube;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ORColor;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

class RingSvgExporterTest {

	@Test
	void renderRingWritesOuterAndInnerCircles() throws Exception {
		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.2, true);

		RingSvgExporter.renderRing(builder, 0, 0, 0.05, 0.02,
				Collections.emptyList(), options);

		File svgFile = File.createTempFile("ring", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		Assertions.assertTrue(contents.contains("r=\"50.000\""), contents);
		Assertions.assertTrue(contents.contains("r=\"20.000\""), contents);
	}

	@Test
	void holesFromMotorMountsFallsBackToRadialShift() {
		InnerTube tube = new InnerTube();
		tube.setOuterRadius(0.01);
		tube.setRadialShift(0.02, -0.01);

		List<RingSvgExporter.Hole> holes = RingSvgExporter.holesFromMotorMounts(Collections.singletonList(tube));
		Assertions.assertEquals(1, holes.size());
		RingSvgExporter.Hole hole = holes.get(0);
		Assertions.assertEquals(0.02, hole.offsetY(), 1e-9);
		Assertions.assertEquals(-0.01, hole.offsetZ(), 1e-9);
		Assertions.assertEquals(0.01, hole.radius(), 1e-9);
	}
}

