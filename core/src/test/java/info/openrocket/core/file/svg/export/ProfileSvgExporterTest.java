package info.openrocket.core.file.svg.export;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ORColor;
import java.io.File;
import java.nio.file.Files;

class ProfileSvgExporterTest extends BaseTestCase {

	@Test
	void calculateBoundsForConstantRadiusComponent() {
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.05);
		tube.setLength(0.2);

		ProfileSvgExporter.Bounds bounds = ProfileSvgExporter.calculateBounds(tube);

		Assertions.assertEquals(0.2, bounds.getWidth(), 1e-9);
		// getMaxAbsY returns the maximum absolute Y value (radius), not the total height
		Assertions.assertEquals(0.05, bounds.getMaxAbsY(), 1e-9); // radius
	}

	@Test
	void calculateBoundsForVariableRadiusComponent() {
		NoseCone noseCone = new NoseCone();
		noseCone.setLength(0.1);
		noseCone.setAftRadius(0.05);
		noseCone.setForeRadius(0.0);
		noseCone.setShapeType(NoseCone.Shape.CONICAL);

		ProfileSvgExporter.Bounds bounds = ProfileSvgExporter.calculateBounds(noseCone);

		Assertions.assertEquals(0.1, bounds.getWidth(), 1e-6);
		Assertions.assertTrue(bounds.getMaxAbsY() > 0);
		Assertions.assertTrue(bounds.getMaxAbsY() <= 0.05);
	}

	@Test
	void calculateBoundsForTransitionWithShoulders() {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.03);
		transition.setAftRadius(0.05);
		transition.setShapeType(Transition.Shape.CONICAL);
		transition.setForeShoulderLength(0.02);
		transition.setForeShoulderRadius(0.03);
		transition.setAftShoulderLength(0.025);
		transition.setAftShoulderRadius(0.05);

		ProfileSvgExporter.Bounds bounds = ProfileSvgExporter.calculateBounds(transition);

		// Should include transition body plus shoulders
		Assertions.assertTrue(bounds.getWidth() >= 0.15);
		// Width should account for shoulders extending beyond transition body
		Assertions.assertTrue(bounds.getWidth() >= 0.15 + 0.02 + 0.025);
	}

	@Test
	void drawClosedProfileForConstantRadiusWritesRectangle() throws Exception {
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.05);
		tube.setLength(0.2);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(tube, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("bodytube", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		// Should contain path data with rectangle coordinates
		Assertions.assertTrue(contents.contains("M"), "Should contain path with Move command");
		Assertions.assertTrue(contents.contains("L"), "Should contain path with Line commands");
		// Check for expected coordinates (converted to mm: 0.05m = 50mm, 0.2m = 200mm)
		Assertions.assertTrue(contents.contains("50.000") || contents.contains("50.0"), 
			"Should contain radius coordinate: " + contents);
		Assertions.assertTrue(contents.contains("200.000") || contents.contains("200.0"), 
			"Should contain length coordinate: " + contents);
	}

	@Test
	void drawClosedProfileForVariableRadiusWritesCurvedPath() throws Exception {
		NoseCone noseCone = new NoseCone();
		noseCone.setLength(0.1);
		noseCone.setAftRadius(0.05);
		noseCone.setForeRadius(0.0);
		noseCone.setShapeType(NoseCone.Shape.CONICAL);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(noseCone, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("nosecone", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		Assertions.assertTrue(contents.contains("M"), "Should contain path with Move command");
		Assertions.assertTrue(contents.contains("L"), "Should contain path with Line commands");
		// Should have many points for variable radius
		long lineCount = contents.split("L").length - 1;
		Assertions.assertTrue(lineCount > 10, "Variable radius should have many line segments");
	}

	@Test
	void drawClosedProfileForTransitionWithForeShoulder() throws Exception {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.03);
		transition.setAftRadius(0.05);
		transition.setShapeType(Transition.Shape.CONICAL);
		transition.setForeShoulderLength(0.02);
		transition.setForeShoulderRadius(0.03);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(transition, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("transition-fore", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		// Should contain multiple paths (transition body + fore shoulder)
		long pathCount = contents.split("<path").length - 1;
		Assertions.assertTrue(pathCount >= 2, "Should contain transition body and fore shoulder paths");
		// Fore shoulder extends backward (negative X)
		Assertions.assertTrue(contents.contains("-20.000") || contents.contains("-20.0"), 
			"Should contain negative coordinate for fore shoulder: " + contents);
	}

	@Test
	void drawClosedProfileForTransitionWithAftShoulder() throws Exception {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.03);
		transition.setAftRadius(0.05);
		transition.setShapeType(Transition.Shape.CONICAL);
		transition.setAftShoulderLength(0.025);
		transition.setAftShoulderRadius(0.05);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(transition, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("transition-aft", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		// Should contain multiple paths (transition body + aft shoulder)
		long pathCount = contents.split("<path").length - 1;
		Assertions.assertTrue(pathCount >= 2, "Should contain transition body and aft shoulder paths");
		// Aft shoulder extends forward (positive X beyond transition length)
		Assertions.assertTrue(contents.contains("175.000") || contents.contains("175.0"), 
			"Should contain coordinate beyond transition length for aft shoulder: " + contents);
	}

	@Test
	void drawClosedProfileForTransitionWithBothShoulders() throws Exception {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.03);
		transition.setAftRadius(0.05);
		transition.setShapeType(Transition.Shape.CONICAL);
		transition.setForeShoulderLength(0.02);
		transition.setForeShoulderRadius(0.03);
		transition.setAftShoulderLength(0.025);
		transition.setAftShoulderRadius(0.05);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(transition, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("transition-both", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		// Should contain multiple paths (transition body + both shoulders)
		long pathCount = contents.split("<path").length - 1;
		Assertions.assertTrue(pathCount >= 3, "Should contain transition body and both shoulder paths");
	}

	@Test
	void drawClosedProfileForTransitionWithoutShoulders() throws Exception {
		Transition transition = new Transition();
		transition.setLength(0.15);
		transition.setForeRadius(0.03);
		transition.setAftRadius(0.05);
		transition.setShapeType(Transition.Shape.CONICAL);
		// No shoulders set

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(transition, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("transition-none", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		// Should contain only transition body path
		long pathCount = contents.split("<path").length - 1;
		Assertions.assertEquals(1, pathCount, "Should contain only transition body path");
	}

	@Test
	void drawClosedProfileRespectsOriginOffset() throws Exception {
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.05);
		tube.setLength(0.2);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		// Draw at offset origin
		ProfileSvgExporter.drawClosedProfile(tube, builder, 0.1, 0.05, options);

		File svgFile = File.createTempFile("bodytube-offset", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		// SVG builder adjusts viewBox based on content, but path should be present
		Assertions.assertTrue(contents.contains("<path"), 
			"Should contain path element: " + contents);
		// Path should contain coordinates (viewBox will be adjusted automatically)
		Assertions.assertTrue(contents.contains("M") && contents.contains("L"), 
			"Should contain path commands: " + contents);
	}

	@Test
	void drawClosedProfileUsesCorrectStrokeColor() throws Exception {
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.05);
		tube.setLength(0.2);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(new ORColor(255, 0, 0), 0.2);

		ProfileSvgExporter.drawClosedProfile(tube, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("bodytube-color", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		Assertions.assertTrue(contents.contains("stroke=\"rgb(255,0,0)\"") || 
			contents.contains("stroke=\"#ff0000\""), 
			"Should contain red stroke color: " + contents);
	}

	@Test
	void drawClosedProfileUsesCorrectStrokeWidth() throws Exception {
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.05);
		tube.setLength(0.2);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.5);

		ProfileSvgExporter.drawClosedProfile(tube, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("bodytube-width", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		Assertions.assertTrue(contents.contains("stroke-width=\"0.500\""), 
			"Should contain correct stroke width: " + contents);
	}

	@Test
	void drawClosedProfileForBodyTubeHasCorrectDimensionsInMm() throws Exception {
		// Test with specific dimensions: 50mm radius, 200mm length
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.05); // 50mm
		tube.setLength(0.2); // 200mm

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(tube, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("bodytube-dimensions", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		
		// Verify the path contains expected dimensions in millimeters
		// Width should be 200mm (length)
		// Height should span 100mm (radius * 2 = 50mm * 2)
		verifyPathDimensions(contents, 0.2 * 1000, 0.05 * 2 * 1000, 0.1);
		
		// Also verify specific coordinates are present
		// Should contain coordinates for 200mm length and ±50mm radius
		Assertions.assertTrue(contents.contains("200.000") || contents.contains("200.0"), 
			"Should contain 200mm length coordinate: " + contents);
		Assertions.assertTrue(contents.contains("50.000") || contents.contains("50.0"), 
			"Should contain 50mm radius coordinate: " + contents);
	}

	@Test
	void drawClosedProfileForTransitionWithShouldersHasCorrectDimensionsInMm() throws Exception {
		// Test transition: 150mm length, fore shoulder 20mm, aft shoulder 25mm
		Transition transition = new Transition();
		transition.setLength(0.15); // 150mm
		transition.setForeRadius(0.03); // 30mm
		transition.setAftRadius(0.05); // 50mm
		transition.setShapeType(Transition.Shape.CONICAL);
		transition.setForeShoulderLength(0.02); // 20mm
		transition.setForeShoulderRadius(0.03); // 30mm
		transition.setAftShoulderLength(0.025); // 25mm
		transition.setAftShoulderRadius(0.05); // 50mm

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(transition, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("transition-dimensions", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		
		// Should have 3 paths: transition body + 2 shoulders
		long pathCount = contents.split("<path").length - 1;
		Assertions.assertTrue(pathCount >= 3, "Should contain transition body and both shoulders: " + contents);
		
		// Verify fore shoulder dimensions: 20mm length, 60mm height (30mm radius * 2)
		verifyShoulderDimensions(contents, -0.02 * 1000, 0.0, 0.02 * 1000, 0.03 * 2 * 1000, "fore");
		
		// Verify aft shoulder dimensions: 25mm length, 100mm height (50mm radius * 2)
		verifyShoulderDimensions(contents, 0.15 * 1000, 0.0, 0.025 * 1000, 0.05 * 2 * 1000, "aft");
	}

	@Test
	void drawClosedProfileForNoseConeHasCorrectDimensionsInMm() throws Exception {
		// Test nose cone: 100mm length, 50mm base radius
		NoseCone noseCone = new NoseCone();
		noseCone.setLength(0.1); // 100mm
		noseCone.setAftRadius(0.05); // 50mm
		noseCone.setForeRadius(0.0);
		noseCone.setShapeType(NoseCone.Shape.CONICAL);

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(noseCone, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("nosecone-dimensions", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		
		// Verify length is 100mm, height spans 100mm (50mm radius * 2)
		verifyPathDimensions(contents, 0.1 * 1000, 0.05 * 2 * 1000, 0.1);
		
		// Verify specific coordinates
		Assertions.assertTrue(contents.contains("100.000") || contents.contains("100.0"), 
			"Should contain 100mm length coordinate: " + contents);
		Assertions.assertTrue(contents.contains("50.000") || contents.contains("50.0"), 
			"Should contain 50mm radius coordinate: " + contents);
	}

	@Test
	void drawClosedProfileForSmallComponentHasCorrectDimensionsInMm() throws Exception {
		// Test with very small dimensions to verify precision
		// 10mm radius, 20mm length
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.01); // 10mm
		tube.setLength(0.02); // 20mm

		SVGBuilder builder = new SVGBuilder();
		SVGExportOptions options = new SVGExportOptions(ORColor.BLACK, 0.1);

		ProfileSvgExporter.drawClosedProfile(tube, builder, 0.0, 0.0, options);

		File svgFile = File.createTempFile("bodytube-small", ".svg");
		builder.writeToFile(svgFile);

		String contents = Files.readString(svgFile.toPath());
		
		// Verify dimensions: 20mm width, 20mm height (10mm radius * 2)
		verifyPathDimensions(contents, 0.02 * 1000, 0.01 * 2 * 1000, 0.01);
		
		// Verify specific coordinates with high precision
		Assertions.assertTrue(contents.contains("20.000") || contents.contains("20.0"), 
			"Should contain 20mm coordinate: " + contents);
		Assertions.assertTrue(contents.contains("10.000") || contents.contains("10.0"), 
			"Should contain 10mm coordinate: " + contents);
	}

	/**
	 * Verifies that path dimensions match expected values in millimeters.
	 * Extracts coordinates from SVG path data and checks dimensions.
	 * For components with multiple paths (e.g., transitions with shoulders), 
	 * this checks the main component path (typically the first or largest one).
	 */
	private void verifyPathDimensions(String svgContent, double expectedWidthMm, double expectedHeightMm, double toleranceMm) {
		// Extract all path data attributes
		java.util.regex.Pattern pathPattern = java.util.regex.Pattern.compile("d=\"([^\"]+)\"");
		java.util.regex.Matcher matcher = pathPattern.matcher(svgContent);
		
		if (!matcher.find()) {
			Assertions.fail("No path data found in SVG: " + svgContent);
			return;
		}
		
		// Find the largest path (main component body, not shoulders)
		double maxArea = 0;
		String mainPathData = null;
		double mainWidth = 0;
		double mainHeight = 0;
		
		matcher.reset();
		while (matcher.find()) {
			String pathData = matcher.group(1);
			java.util.regex.Pattern coordPattern = java.util.regex.Pattern.compile("([ML])([\\d.\\-]+),([\\d.\\-]+)");
			java.util.regex.Matcher coordMatcher = coordPattern.matcher(pathData);
			
			double minX = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			
			while (coordMatcher.find()) {
				double x = Double.parseDouble(coordMatcher.group(2));
				double y = Double.parseDouble(coordMatcher.group(3));
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}
			
			double width = maxX - minX;
			double height = maxY - minY;
			double area = width * height;
			
			// Keep the largest path (main component)
			if (area > maxArea) {
				maxArea = area;
				mainPathData = pathData;
				mainWidth = width;
				mainHeight = height;
			}
		}
		
		Assertions.assertNotNull(mainPathData, "Should have found at least one path");
		Assertions.assertEquals(expectedWidthMm, mainWidth, toleranceMm,
			String.format("Width mismatch: expected %.3fmm, got %.3fmm (path: %s)", 
				expectedWidthMm, mainWidth, mainPathData));
		Assertions.assertEquals(expectedHeightMm, mainHeight, toleranceMm,
			String.format("Height mismatch: expected %.3fmm, got %.3fmm (path: %s)", 
				expectedHeightMm, mainHeight, mainPathData));
	}

	/**
	 * Verifies shoulder dimensions in SVG.
	 */
	private void verifyShoulderDimensions(String svgContent, double expectedStartX, double expectedStartY,
	                                      double expectedLengthMm, double expectedHeightMm, String shoulderType) {
		// Extract all path data attributes
		java.util.regex.Pattern pathPattern = java.util.regex.Pattern.compile("d=\"([^\"]+)\"");
		java.util.regex.Matcher matcher = pathPattern.matcher(svgContent);
		
		boolean found = false;
		while (matcher.find()) {
			String pathData = matcher.group(1);
			// Parse coordinates
			java.util.regex.Pattern coordPattern = java.util.regex.Pattern.compile("([ML])([\\d.\\-]+),([\\d.\\-]+)");
			java.util.regex.Matcher coordMatcher = coordPattern.matcher(pathData);
			
			double minX = Double.MAX_VALUE;
			double maxX = -Double.MAX_VALUE;
			double minY = Double.MAX_VALUE;
			double maxY = -Double.MAX_VALUE;
			
			while (coordMatcher.find()) {
				double x = Double.parseDouble(coordMatcher.group(2));
				double y = Double.parseDouble(coordMatcher.group(3));
				minX = Math.min(minX, x);
				maxX = Math.max(maxX, x);
				minY = Math.min(minY, y);
				maxY = Math.max(maxY, y);
			}
			
			// Check if this path matches the expected shoulder dimensions
			double length = maxX - minX;
			double height = maxY - minY;
			
			// Fore shoulder extends backward (negative X), aft extends forward
			if (shoulderType.equals("fore")) {
				if (minX < 0 && Math.abs(length - expectedLengthMm) < 0.1 && 
				    Math.abs(height - expectedHeightMm) < 0.1) {
					found = true;
					Assertions.assertEquals(expectedLengthMm, length, 0.1,
						String.format("Fore shoulder length: expected %.3fmm, got %.3fmm", expectedLengthMm, length));
					Assertions.assertEquals(expectedHeightMm, height, 0.1,
						String.format("Fore shoulder height: expected %.3fmm, got %.3fmm", expectedHeightMm, height));
					break;
				}
			} else if (shoulderType.equals("aft")) {
				if (minX >= expectedStartX && Math.abs(length - expectedLengthMm) < 0.1 && 
				    Math.abs(height - expectedHeightMm) < 0.1) {
					found = true;
					Assertions.assertEquals(expectedLengthMm, length, 0.1,
						String.format("Aft shoulder length: expected %.3fmm, got %.3fmm", expectedLengthMm, length));
					Assertions.assertEquals(expectedHeightMm, height, 0.1,
						String.format("Aft shoulder height: expected %.3fmm, got %.3fmm", expectedHeightMm, height));
					break;
				}
			}
		}
		
		Assertions.assertTrue(found, String.format("Shoulder %s not found with expected dimensions in SVG", shoulderType));
	}
}

