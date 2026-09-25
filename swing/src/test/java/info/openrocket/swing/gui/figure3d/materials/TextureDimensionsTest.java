package info.openrocket.swing.gui.figure3d.materials;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TextureDimensionsTest {

	@Test
	void acceptsTextureWithinGpuAndPixelLimits() {
		assertDoesNotThrow(() -> Texture.validateCompressedTextureDimensions(4096, 4096, 8192));
	}

	@Test
	void rejectsTextureBeyondGpuDimensionLimit() {
		assertThrows(IllegalArgumentException.class,
				() -> Texture.validateCompressedTextureDimensions(8193, 1, 8192));
	}

	@Test
	void rejectsTextureBeyondDecodedPixelBudgetWithoutOverflowing() {
		assertThrows(IllegalArgumentException.class,
				() -> Texture.validateCompressedTextureDimensions(Integer.MAX_VALUE, Integer.MAX_VALUE,
						Integer.MAX_VALUE));
	}

	@Test
	void bleedsOpaqueNeighborColorIntoTransparentPixelInPlace() {
		ByteBuffer image = ByteBuffer.allocateDirect(8);
		image.put(0, (byte) 120);
		image.put(1, (byte) 80);
		image.put(2, (byte) 40);
		image.put(3, (byte) 255);

		Texture.bleedTransparentRgb(image, 2, 1);

		assertEquals(120, Byte.toUnsignedInt(image.get(4)));
		assertEquals(80, Byte.toUnsignedInt(image.get(5)));
		assertEquals(40, Byte.toUnsignedInt(image.get(6)));
		assertEquals(0, Byte.toUnsignedInt(image.get(7)));
	}
}
