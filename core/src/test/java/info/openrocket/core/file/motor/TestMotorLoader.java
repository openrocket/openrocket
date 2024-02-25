package info.openrocket.core.file.motor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import info.openrocket.core.motor.ThrustCurveMotor;

import org.junit.jupiter.api.Test;

public class TestMotorLoader {

	private static final String DIGEST1 = "e523030bc96d5e63313b5723aaea267d";
	private static final String DIGEST2 = "6a41f0f10b7283793eb0e6b389753729";
	private static final String DIGEST3 = "e3164a735f9a50500f2725f0a33d246b";

	@Test
	public void testGeneralMotorLoader() throws IOException {
		MotorLoader loader = new GeneralMotorLoader();

		test(loader, "/file/motor/test1.eng", DIGEST1);
		test(loader, "/file/motor/test2.rse", DIGEST2);
		test(loader, "/file/motor/test.zip", DIGEST2, DIGEST1);
		test(loader, "/file/motor/test3.rse", DIGEST3);
	}

	@Test
	public void testRASPMotorLoader() throws IOException {
		test(new RASPMotorLoader(), "/file/motor/test1.eng", DIGEST1);
	}

	@Test
	public void testRockSimMotorLoader() throws IOException {
		test(new RockSimMotorLoader(), "/file/motor/test2.rse", DIGEST2);
	}

	@Test
	public void testRockSimMotorLoader3() throws IOException {
		test(new RockSimMotorLoader(), "/file/motor/test3.rse", DIGEST3);
	}

	@Test
	public void testZipMotorLoader() throws IOException {
		test(new ZipFileMotorLoader(), "/file/motor/test.zip", DIGEST2, DIGEST1);
	}

	private void test(MotorLoader loader, String file, String... digests) throws IOException {
		List<ThrustCurveMotor.Builder> motors;

		InputStream is = this.getClass().getResourceAsStream(file);
		assertNotNull(is, "File " + file + " not found");
		motors = loader.load(is, file);
		is.close();
		assertEquals(digests.length, motors.size());

		String[] d = new String[digests.length];
		for (int i = 0; i < motors.size(); i++) {
			d[i] = motors.get(i).build().getDigest();
		}

		Arrays.sort(digests);
		Arrays.sort(d);
		assertArrayEquals(d, digests, "d = " + Arrays.toString(d) + " digests = " + Arrays.toString(digests));
	}

}
