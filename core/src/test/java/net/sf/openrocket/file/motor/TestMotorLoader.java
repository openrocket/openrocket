// thzero
package net.sf.openrocket.file.motor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.motor.ThrustCurveMotor;

import org.junit.Test;

public class TestMotorLoader {
	
	private static final String DIGEST1 = "e523030bc96d5e63313b5723aaea267d";
	private static final String DIGEST2 = "6a41f0f10b7283793eb0e6b389753729";
	private static final String DIGEST3 = "e3164a735f9a50500f2725f0a33d246b";
	
	
	@Test
	public void testGeneralMotorLoader() throws IOException {
		MotorLoader loader = new GeneralMotorLoader();
		
// thzero - begin
		test(loader, "/file/motor/test1.eng", DIGEST1);
		test(loader, "/file/motor/test2.rse", DIGEST2);
		test(loader, "/file/motor/test.zip", DIGEST2, DIGEST1);
		test(loader, "/file/motor/test3.rse", DIGEST3);
// thzero - end
	}
	
	@Test
	public void testRASPMotorLoader() throws IOException {
// thzero - begin
		test(new RASPMotorLoader(), "/file/motor/test1.eng", DIGEST1);
// thzero - end
	}
	
	@Test
	public void testRockSimMotorLoader() throws IOException {
// thzero - begin
		test(new RockSimMotorLoader(), "/file/motor/test2.rse", DIGEST2);
// thzero - end
	}
	
	@Test
	public void testRockSimMotorLoader3() throws IOException {
// thzero - begin
		test(new RockSimMotorLoader(), "/file/motor/test3.rse", DIGEST3);
// thzero - end
	}
	
	@Test
	public void testZipMotorLoader() throws IOException {
// thzero - begin
		test(new ZipFileMotorLoader(), "/file/motor/test.zip", DIGEST2, DIGEST1);
// thzero - end
	}
	
	
	private void test(MotorLoader loader, String file, String... digests) throws IOException {
		List<ThrustCurveMotor.Builder> motors;
		
		InputStream is = this.getClass().getResourceAsStream(file);
		assertNotNull("File " + file + " not found", is);
		motors = loader.load(is, file);
		is.close();
		assertEquals(digests.length, motors.size());
		
		String[] d = new String[digests.length];
		for (int i = 0; i < motors.size(); i++) {
			d[i] = motors.get(i).build().getDigest();
		}
		
		Arrays.sort(digests);
		Arrays.sort(d);
		assertTrue("d = " + Arrays.toString(d) + " digests = " + Arrays.toString(digests), Arrays.equals(d, digests));
	}
	
}
