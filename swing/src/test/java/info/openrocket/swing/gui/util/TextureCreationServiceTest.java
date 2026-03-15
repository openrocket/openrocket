package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.inject.Injector;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.util.TextureCreationService.TextureGenerationException;
import info.openrocket.swing.gui.util.TextureCreationService.TextureGenerationResult;
import info.openrocket.swing.utils.BasicApplication;

class TextureCreationServiceTest {

	private static final double DPI = 300d;
	private static final double SCALE = DPI / 0.0254d;
	private static Injector originalInjector;

	@BeforeAll
	static void initializeApplication() {
		originalInjector = Application.getInjector();
		if (originalInjector == null) {
			new BasicApplication().initializeApplication();
		}
	}

	@AfterAll
	static void restoreApplication() {
		Application.setInjector(originalInjector);
	}

	@Test
	void generatesExpectedDimensionsForBodyTubeOutsideSurface() throws TextureGenerationException {
		BodyTube tube = new BodyTube();
		tube.setLength(0.5);
		tube.setOuterRadius(0.05);

		TextureCreationService service = new TextureCreationService();
		TextureGenerationResult result = service.generateTextureImage(tube, false, DPI);
		BufferedImage image = result.getImage();

		double circumference = 2 * Math.PI * 0.05;
		int expectedWidth = (int) Math.round(circumference * SCALE);
		int expectedHeight = (int) Math.round(0.5 * SCALE);

		assertEquals(expectedWidth, image.getWidth());
		assertEquals(expectedHeight, image.getHeight());
	}

	@Test
	void usesInnerRadiusForInsideSurface() throws TextureGenerationException {
		BodyTube tube = new BodyTube();
		tube.setLength(0.4);
		tube.setOuterRadius(0.06);
		tube.setThickness(0.005);

		TextureCreationService service = new TextureCreationService();
		TextureGenerationResult result = service.generateTextureImage(tube, true, DPI);
		BufferedImage image = result.getImage();

		double innerRadius = 0.06 - 0.005;
		int expectedWidth = (int) Math.round(2 * Math.PI * innerRadius * SCALE);
		int expectedHeight = (int) Math.round(0.4 * SCALE);

		assertEquals(expectedWidth, image.getWidth());
		assertEquals(expectedHeight, image.getHeight());
	}

	@Test
	void drawsOutlineForFinSet() throws TextureGenerationException {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setHeight(0.15);
		finSet.setRootChord(0.2);
		finSet.setTipChord(0.12);
		finSet.setSweep(0.05);

		TextureCreationService service = new TextureCreationService();
		TextureGenerationResult result = service.generateTextureImage(finSet, false, DPI);

		CoordinateIF[] points = finSet.getFinPoints();
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (CoordinateIF point : points) {
			minX = Math.min(minX, point.getX());
			maxX = Math.max(maxX, point.getX());
			minY = Math.min(minY, point.getY());
			maxY = Math.max(maxY, point.getY());
		}

		int expectedWidth = (int) Math.round((maxX - minX) * SCALE);
		int expectedHeight = (int) Math.round((maxY - minY) * SCALE);

		assertEquals(expectedWidth, result.getImage().getWidth());
		assertEquals(expectedHeight, result.getImage().getHeight());

		assertTrue(hasOpaquePixels(result.getImage()), "Outline should place opaque pixels into the image");
	}

	@Test
	void skipsOutlineWhenDisabled() throws TextureGenerationException {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setHeight(0.15);
		finSet.setRootChord(0.2);
		finSet.setTipChord(0.12);
		finSet.setSweep(0.05);

		TextureCreationService service = new TextureCreationService();
		TextureGenerationResult result = service.generateTextureImage(finSet, false, DPI, false);

		assertFalse(hasOpaquePixels(result.getImage()), "Outline should be omitted when disabled");
	}

	@Test
	void respectsCustomOutlineWidth() throws TextureGenerationException {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setHeight(0.15);
		finSet.setRootChord(0.2);
		finSet.setTipChord(0.12);
		finSet.setSweep(0.05);

		TextureCreationService service = new TextureCreationService();
		TextureGenerationResult thin = service.generateTextureImage(finSet, false, DPI, true, 1f);
		TextureGenerationResult thick = service.generateTextureImage(finSet, false, DPI, true, 5f);

		assertTrue(hasOpaquePixels(thin.getImage()));
		assertTrue(hasOpaquePixels(thick.getImage()));
		assertTrue(countOpaquePixels(thick.getImage()) > countOpaquePixels(thin.getImage()),
				"Thicker outlines should cover more pixels");
	}

	@Test
	void mirrorsFinTextureWhenRequested() throws TextureGenerationException {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setHeight(0.15);
		finSet.setRootChord(0.2);
		finSet.setTipChord(0.12);
		finSet.setSweep(0.05);

		TextureCreationService service = new TextureCreationService();
		TextureCreationService.TextureGenerationResult normal = service.generateTextureImage(finSet, false, DPI, true, 2f, false);
		TextureCreationService.TextureGenerationResult mirrored = service.generateTextureImage(finSet, false, DPI, true, 2f, true);

		assertTrue(hasOpaquePixels(normal.getImage()));
		assertTrue(hasOpaquePixels(mirrored.getImage()));
		assertTrue(isHorizontallyMirror(normal.getImage(), mirrored.getImage()),
				"Mirrored texture should be a vertical reflection of the original");
	}

	@Test
	void appliesCustomOutlineColor() throws TextureGenerationException {
		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setHeight(0.15);
		finSet.setRootChord(0.2);
		finSet.setTipChord(0.12);
		finSet.setSweep(0.05);

		Color outlineColor = new Color(120, 10, 180, 255);
		TextureCreationService service = new TextureCreationService();
		TextureGenerationResult colored = service.generateTextureImage(finSet, false, DPI, true, 2f, false, outlineColor);

		int observed = findFirstOpaquePixelColor(colored.getImage());
		assertEquals(outlineColor.getRGB(), observed, "Outline color should match the requested color");
	}

	@Test
	void rejectsUnsupportedComponentTypes() {
		Parachute parachute = new Parachute();
		TextureCreationService service = new TextureCreationService();

		assertThrows(TextureGenerationException.class,
				() -> service.generateTextureImage(parachute, false, DPI));
	}

	private boolean hasOpaquePixels(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					return true;
				}
			}
		}
		return false;
	}

	private int countOpaquePixels(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int count = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if ((image.getRGB(x, y) >>> 24) != 0) {
					count++;
				}
			}
		}
		return count;
	}

	private boolean isHorizontallyMirror(BufferedImage original, BufferedImage mirrored) {
		if (original.getWidth() != mirrored.getWidth() || original.getHeight() != mirrored.getHeight()) {
			return false;
		}
		int width = original.getWidth();
		int height = original.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int origPixel = original.getRGB(x, y);
				int mirrorPixel = mirrored.getRGB(width - 1 - x, y);
				if (origPixel != mirrorPixel) {
					return false;
				}
			}
		}
		return true;
	}

	private int findFirstOpaquePixelColor(BufferedImage image) {
		int width = image.getWidth();
		int height = image.getHeight();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int pixel = image.getRGB(x, y);
				if ((pixel >>> 24) != 0) {
					return pixel;
				}
			}
		}
		return 0;
	}
}

