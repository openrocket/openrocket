package info.openrocket.core.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import info.openrocket.core.motor.MotorDigest.DataType;
import info.openrocket.core.util.TextUtil;

import org.junit.jupiter.api.Test;

public class MotorDigestTest {

	private static final double[] timeArray = {
			0.0, 0.123456789, 0.4115, Math.nextAfter(Math.nextAfter(1.4445, 0), 0)
	};

	private static final double[] massArray = {
			0.54321, 0.43211
	};

	private static final double[] thrustArray = {
			0.0, 0.2345678, 9999.3335, 0.0
	};

	private static final int[] intData = {
			// Time (ms)
			0, 4, 0, 123, 412, 1445,
			// Mass specific (0.1g)
			1, 2, 5432, 4321,
			// Thrust (mN)
			5, 4, 0, 235, 9999334, 0
	};

	@Test
	public void testMotorDigest() throws NoSuchAlgorithmException {

		MessageDigest correct = MessageDigest.getInstance("MD5");
		for (int value : intData) {
			correct.update((byte) ((value >>> 24) & 0xFF));
			correct.update((byte) ((value >>> 16) & 0xFF));
			correct.update((byte) ((value >>> 8) & 0xFF));
			correct.update((byte) (value & 0xFF));
		}

		MotorDigest motor = new MotorDigest();
		motor.update(DataType.TIME_ARRAY, timeArray);
		motor.update(DataType.MASS_SPECIFIC, massArray);
		motor.update(DataType.FORCE_PER_TIME, thrustArray);

		assertEquals(TextUtil.hexString(correct.digest()), motor.getDigest());
	}

	@Test
	public void testCommentDigest() throws NoSuchAlgorithmException, UnsupportedEncodingException {

		assertEquals(md5("Hello world!"), MotorDigest.digestComment("Hello  world! "));
		assertEquals(md5("Hello world!"), MotorDigest.digestComment("\nHello\tworld!\n\r"));
		assertEquals(md5("Hello world!"), MotorDigest.digestComment("Hello\r\r\r\nworld!"));
		assertEquals(md5("Hello\u00e4 world!"), MotorDigest.digestComment("Hello\u00e4\r\r\nworld!"));

	}

	private static String md5(String source)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest digest = MessageDigest.getInstance("MD5");
		return TextUtil.hexString(digest.digest(source.getBytes(StandardCharsets.UTF_8)));
	}
}
