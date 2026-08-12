package info.openrocket.swing.gui.figure3d.ui;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GLScenePanelImageConversionTest {

	@Test
	void convertsPremultipliedLinearFramebufferRgbToStraightSrgb() {
		ByteBuffer buffer = ByteBuffer.allocateDirect(3 * 4);
		// 137 sRGB decodes to approximately 0.25 linear. At 50% alpha that
		// represents an unassociated value of 0.5 linear, or approximately 187 sRGB
		// after the eight-bit alpha value is taken into account.
		putRgba(buffer, 137, 137, 137, 128);
		putRgba(buffer, 42, 84, 126, 0);
		putRgba(buffer, 10, 20, 30, 255);
		buffer.flip();

		BufferedImage image = GLScenePanel.bufferToImage(buffer, 3, 1);

		assertEquals(0x80BBBBBB, image.getRGB(0, 0));
		assertEquals(0x00000000, image.getRGB(1, 0));
		assertEquals(0xFF0A141E, image.getRGB(2, 0));
	}

	private static void putRgba(ByteBuffer buffer, int red, int green, int blue, int alpha) {
		buffer.put((byte) red);
		buffer.put((byte) green);
		buffer.put((byte) blue);
		buffer.put((byte) alpha);
	}
}
