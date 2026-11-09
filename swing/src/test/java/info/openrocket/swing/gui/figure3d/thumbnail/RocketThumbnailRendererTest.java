package info.openrocket.swing.gui.figure3d.thumbnail;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.util.BaseTestCase;

class RocketThumbnailRendererTest extends BaseTestCase {
	private static final int SIZE = 256;

	private static final ORColor RED = new ORColor(255, 0, 0);
	private static final ORColor BLUE = new ORColor(0, 0, 255);
	private static final ORColor GREEN = new ORColor(0, 255, 0);
	private static final ORColor GREY = new ORColor(160, 160, 160);

	// Shading darkens surfaces towards their silhouette, so the predicates accept darker shades of each colour.
	private static final IntPredicate IS_RED = rgb -> r(rgb) > 150 && g(rgb) < 70 && b(rgb) < 70;
	private static final IntPredicate IS_BLUE = rgb -> b(rgb) > 150 && r(rgb) < 70 && g(rgb) < 70;
	private static final IntPredicate IS_GREEN = rgb -> g(rgb) > 150 && r(rgb) < 70 && b(rgb) < 70;
	/** Body tube paint, excluding the darker outline and its blend with neighbouring colours. */
	private static final IntPredicate IS_GREY = rgb -> r(rgb) >= 105 && r(rgb) <= 170
			&& Math.abs(r(rgb) - g(rgb)) < 6 && Math.abs(g(rgb) - b(rgb)) < 6;
	private static final IntPredicate IS_REDDISH = rgb -> r(rgb) > g(rgb) + 60 && r(rgb) > b(rgb) + 60;

	@Test
	void rocketIsDrawnDiagonallyWithNoseUpLeft() {
		BufferedImage image = RocketThumbnailRenderer.render(createRocket(RED, GREY), SIZE);

		assertNotNull(image);
		assertEquals(SIZE, image.getWidth());
		assertEquals(SIZE, image.getHeight());
		double[] nose = centroid(image, IS_RED);
		double[] fins = centroid(image, IS_BLUE);
		assertTrue(nose[0] < SIZE / 2.0 && nose[1] < SIZE / 2.0, "nose cone should be in the top-left quadrant");
		assertTrue(fins[0] > SIZE / 2.0 && fins[1] > SIZE / 2.0, "fins should be in the bottom-right quadrant");
	}

	@Test
	void decalIsDrawnOverThePaint() throws Exception {
		Rocket plain = createRocket(RED, GREY);
		Rocket decorated = createRocket(RED, GREY);
		setBodyDecal(decorated, Decal.EdgeMode.REPEAT, 1);

		assertEquals(0, count(RocketThumbnailRenderer.render(plain, SIZE), IS_GREEN));
		BufferedImage image = RocketThumbnailRenderer.render(decorated, SIZE);
		assertTrue(count(image, IS_GREEN) > 0, "decal should be drawn on the body tube");
		assertEquals(0, count(image, IS_GREY), "a repeating decal should cover the whole body tube");
	}

	@Test
	void stickerShowsPaintOutsideTheDecalWhileClampStretchesIt() throws Exception {
		// Scaling the texture coordinates by 2 leaves part of the body tube outside the decal image.
		Rocket sticker = createRocket(RED, GREY);
		setBodyDecal(sticker, Decal.EdgeMode.STICKER, 2);
		Rocket clamp = createRocket(RED, GREY);
		setBodyDecal(clamp, Decal.EdgeMode.CLAMP, 2);

		BufferedImage stickerImage = RocketThumbnailRenderer.render(sticker, SIZE);
		assertTrue(count(stickerImage, IS_GREEN) > 0, "sticker should be drawn");
		assertTrue(count(stickerImage, IS_GREY) > 0, "paint should show outside the sticker");

		BufferedImage clampImage = RocketThumbnailRenderer.render(clamp, SIZE);
		assertTrue(count(clampImage, IS_GREEN) > 0, "clamped decal should be drawn");
		assertEquals(0, count(clampImage, IS_GREY), "clamped decal should stretch its edge over the paint");
	}

	@Test
	void texelsAreWrappedAccordingToEdgeMode() {
		int[] indices = IntStream.rangeClosed(-4, 7).toArray();
		assertArrayEquals(new int[] { 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3 }, wrapAll(indices, Decal.EdgeMode.REPEAT));
		assertArrayEquals(new int[] { 3, 2, 1, 0, 0, 1, 2, 3, 3, 2, 1, 0 }, wrapAll(indices, Decal.EdgeMode.MIRROR));
		assertArrayEquals(new int[] { 0, 0, 0, 0, 0, 1, 2, 3, 3, 3, 3, 3 }, wrapAll(indices, Decal.EdgeMode.CLAMP));
	}

	@Test
	void translucentBodyShowsWhatIsInside() {
		Rocket opaque = createRocket(GREY, GREY);
		addRedInnerTube(opaque);
		Rocket translucent = createRocket(GREY, new ORColor(160, 160, 160, 60));
		addRedInnerTube(translucent);

		assertEquals(0, count(RocketThumbnailRenderer.render(opaque, SIZE), IS_REDDISH),
				"an opaque body tube should hide the inner tube");
		assertTrue(count(RocketThumbnailRenderer.render(translucent, SIZE), IS_REDDISH) > 0,
				"the inner tube should show through a translucent body tube");
	}

	@Test
	void pngIsEncodedAtRequestedSize() throws Exception {
		byte[] png = RocketThumbnailRenderer.renderPng(createRocket(RED, GREY), SIZE);

		assertNotNull(png);
		BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(png));
		assertEquals(SIZE, decoded.getWidth());
		assertEquals(SIZE, decoded.getHeight());
	}

	@Test
	void rocketWithoutComponentsHasNoThumbnail() {
		Rocket rocket = new Rocket();
		rocket.addChild(new AxialStage());
		rocket.enableEvents();

		assertNull(RocketThumbnailRenderer.renderPng(rocket, SIZE));
	}

	/**
	 * A nose cone on a body tube with blue fins.
	 */
	private static Rocket createRocket(ORColor noseColor, ORColor bodyColor) {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);

		NoseCone nose = new NoseCone(Transition.Shape.OGIVE, 0.1, 0.02);
		nose.setAppearance(new Appearance(noseColor, 0));
		stage.addChild(nose);

		BodyTube body = new BodyTube(0.3, 0.02);
		body.setAppearance(new Appearance(bodyColor, 0));
		stage.addChild(body);

		TrapezoidFinSet fins = new TrapezoidFinSet(4, 0.06, 0.03, 0.03, 0.04);
		fins.setAppearance(new Appearance(BLUE, 0));
		body.addChild(fins);
		fins.setAxialMethod(AxialMethod.BOTTOM);
		fins.setAxialOffset(0);

		rocket.getSelectedConfiguration().setAllStages();
		rocket.enableEvents();
		return rocket;
	}

	private static void addRedInnerTube(Rocket rocket) {
		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
		InnerTube inner = new InnerTube();
		inner.setAppearance(new Appearance(RED, 0));
		body.addChild(inner);
		inner.setOuterRadius(0.015);
		inner.setLength(0.2);
	}

	/** Gives the body tube a solid green decal, with its texture coordinates multiplied by {@code scale}. */
	private static void setBodyDecal(Rocket rocket, Decal.EdgeMode edgeMode, double scale) throws Exception {
		BufferedImage green = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				green.setRGB(x, y, 0xFF00FF00);
			}
		}
		ByteArrayOutputStream png = new ByteArrayOutputStream();
		ImageIO.write(green, "png", png);

		Decal decal = new Decal(new Coordinate(0, 0), new Coordinate(0, 0), new Coordinate(scale, scale), 0,
				new InMemoryDecalImage(png.toByteArray()), edgeMode);
		BodyTube body = (BodyTube) rocket.getChild(0).getChild(1);
		body.setAppearance(new Appearance(GREY, 0, decal, false));
	}

	private static int[] wrapAll(int[] indices, Decal.EdgeMode mode) {
		return IntStream.of(indices).map(i -> RocketThumbnailRenderer.wrap(i, 4, mode)).toArray();
	}

	private static double[] centroid(BufferedImage image, IntPredicate matches) {
		double sumX = 0, sumY = 0;
		int count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (matches.test(image.getRGB(x, y))) {
					sumX += x;
					sumY += y;
					count++;
				}
			}
		}
		assertTrue(count > 0, "expected colour not found in thumbnail");
		return new double[] { sumX / count, sumY / count };
	}

	private static int count(BufferedImage image, IntPredicate matches) {
		int count = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				if (matches.test(image.getRGB(x, y))) {
					count++;
				}
			}
		}
		return count;
	}

	private static int r(int rgb) {
		return (rgb >> 16) & 0xFF;
	}

	private static int g(int rgb) {
		return (rgb >> 8) & 0xFF;
	}

	private static int b(int rgb) {
		return rgb & 0xFF;
	}

	/** Decal image backed by PNG data in memory. */
	private record InMemoryDecalImage(byte[] png) implements DecalImage {
		@Override
		public String getName() {
			return "decals/test.png";
		}

		@Override
		public InputStream getBytes() {
			return new ByteArrayInputStream(png);
		}

		@Override
		public void exportImage(File file) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void fireChangeEvent(Object source) {
		}

		@Override
		public File getDecalFile() {
			return null;
		}

		@Override
		public void setDecalFile(File file) {
		}

		@Override
		public boolean isIgnored() {
			return false;
		}

		@Override
		public void setIgnored(boolean ignored) {
		}

		@Override
		public void addChangeListener(StateChangeListener listener) {
		}

		@Override
		public void removeChangeListener(StateChangeListener listener) {
		}

		@Override
		public int compareTo(DecalImage other) {
			return getName().compareTo(other.getName());
		}
	}
}
