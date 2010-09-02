package net.sf.openrocket.file.motor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorDigest;
import net.sf.openrocket.motor.ThrustCurveMotor;

import org.junit.Test;

public class TestMotorLoader {
	
	@Test
	public void testGeneralMotorLoader() throws IOException {
		MotorLoader loader = new GeneralMotorLoader();
		
		test(loader, "test1.eng", "c056cf25df6751f7bb8a94bc4f64750f");
		test(loader, "test2.rse", "b2fe203ee319ae28b9ccdad26a8f21de");
		test(loader, "test.zip", "b2fe203ee319ae28b9ccdad26a8f21de", "c056cf25df6751f7bb8a94bc4f64750f");
		
	}
	
	@Test
	public void testRASPMotorLoader() throws IOException {
		test(new RASPMotorLoader(), "test1.eng", "c056cf25df6751f7bb8a94bc4f64750f");
	}
	
	@Test
	public void testRocksimMotorLoader() throws IOException {
		test(new RockSimMotorLoader(), "test2.rse", "b2fe203ee319ae28b9ccdad26a8f21de");
	}
	
	@Test
	public void testZipMotorLoader() throws IOException {
		test(new ZipFileMotorLoader(), "test.zip", "b2fe203ee319ae28b9ccdad26a8f21de", "c056cf25df6751f7bb8a94bc4f64750f");
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
			d[i] = MotorDigest.digestMotor((ThrustCurveMotor) motors.get(i));
		}
		
		Arrays.sort(digests);
		Arrays.sort(d);
		assertTrue(Arrays.equals(d, digests));
	}
	
}
