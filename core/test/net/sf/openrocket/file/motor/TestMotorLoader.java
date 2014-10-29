package net.sf.openrocket.file.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

import org.junit.Test;

public class TestMotorLoader {
	
	private static final String DIGEST1 = "e523030bc96d5e63313b5723aaea267d";
	private static final String DIGEST2 = "6a41f0f10b7283793eb0e6b389753729";
	private static final String DIGEST3 = "e3164a735f9a50500f2725f0a33d246b";
	
	
	@Test
	public void testGeneralMotorLoader() throws IOException {
		MotorLoader loader = new GeneralMotorLoader();
		
		test(loader, "test1.eng", DIGEST1);
		test(loader, "test2.rse", DIGEST2);
		test(loader, "test.zip", DIGEST2, DIGEST1);
		test(loader, "test3.rse", DIGEST3);
	}
	
	@Test
	public void testRASPMotorLoader() throws IOException {
		test(new RASPMotorLoader(), "test1.eng", DIGEST1);
	}
	
	@Test
	public void testRocksimMotorLoader() throws IOException {
		test(new RockSimMotorLoader(), "test2.rse", DIGEST2);
	}
	
	@Test
	public void testRocksimMotorLoader3() throws IOException {
		test(new RockSimMotorLoader(), "test3.rse", DIGEST3);
	}
	
	@Test
	public void testZipMotorLoader() throws IOException {
		test(new ZipFileMotorLoader(), "test.zip", DIGEST2, DIGEST1);
	}
	
	
	private void test(MotorLoader loader, String file, String... digests) throws IOException {
		List<Motor> motors;
		
		InputStream is = this.getClass().getResourceAsStream(file);
		assertNotNull("File " + file + " not found", is);
		motors = loader.load(is, file);
		is.close();
		assertEquals(digests.length, motors.size());
		
		String[] d = new String[digests.length];
		for (int i = 0; i < motors.size(); i++) {
			d[i] = ((ThrustCurveMotor) motors.get(i)).getDigest();
		}
		
		Arrays.sort(digests);
		Arrays.sort(d);
		assertTrue("d = " + Arrays.toString(d) + " digests = " + Arrays.toString(digests), Arrays.equals(d, digests));
	}
	
}
