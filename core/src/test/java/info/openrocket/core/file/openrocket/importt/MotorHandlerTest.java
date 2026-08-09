package info.openrocket.core.file.openrocket.importt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import info.openrocket.core.document.Attachment;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.motor.RockSimMotorWriter;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorDigest;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.DecalNotFoundException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

/**
 * Tests embedded motor attachment resolution independently of the installed
 * motor database.
 */
public class MotorHandlerTest {
	private static final int DIGEST_FILE_VERSION = 104;

	@Test
	public void testMatchingAttachmentPreservesRequestedDigest() throws IOException, SAXException {
		ThrustCurveMotor prototype = createMotor("F12X", 12.0, "");
		String requestedDigest = createRaspStyleDigest(prototype);
		ThrustCurveMotor original = createMotor("F12X", 12.0, requestedDigest);
		Attachment attachment = createRseAttachment(original);
		MotorHandler handler = createHandler(requestedDigest, attachment);
		WarningSet warnings = new WarningSet();

		Motor loaded = handler.getMotor(warnings);

		assertNotNull(loaded);
		assertEquals(requestedDigest, loaded.getDigest());
		assertEquals(Motor.Type.UNKNOWN, loaded.getMotorType());
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testMissingAttachmentDoesNotAddCorruptionWarning() throws SAXException {
		String requestedDigest = "missing-digest";
		Attachment attachment = new MissingAttachment("thrustcurves/" + requestedDigest + ".rse");
		MotorHandler handler = createHandler(requestedDigest, attachment);
		WarningSet warnings = new WarningSet();

		assertNull(handler.getMotor(warnings));
		assertTrue(warnings.isEmpty());
	}

	@Test
	public void testCorruptAttachmentAddsWarning() throws SAXException {
		String requestedDigest = "corrupt-digest";
		Attachment attachment = new ByteArrayAttachment("thrustcurves/" + requestedDigest + ".rse",
				"<engine-database>".getBytes(StandardCharsets.UTF_8));
		MotorHandler handler = createHandler(requestedDigest, attachment);
		WarningSet warnings = new WarningSet();

		assertNull(handler.getMotor(warnings));
		assertTrue(containsWarning(warnings, "Unable to load embedded motor attachment"));
		assertTrue(containsWarning(warnings, requestedDigest));
	}

	@Test
	public void testMismatchedAttachmentAddsWarning() throws IOException, SAXException {
		ThrustCurveMotor expected = createMotor("F12X", 12.0, "");
		String requestedDigest = createRaspStyleDigest(expected);
		ThrustCurveMotor differentMotor = createMotor("F12X", 20.0, "different-digest");
		MotorHandler handler = createHandler(requestedDigest, createRseAttachment(differentMotor));
		WarningSet warnings = new WarningSet();

		assertNull(handler.getMotor(warnings));
		assertTrue(containsWarning(warnings, "contains no motor matching digest"));
		assertTrue(containsWarning(warnings, requestedDigest));
	}

	/**
	 * Create a handler whose database lookup always misses so attachment behavior is
	 * exercised directly.
	 */
	private static MotorHandler createHandler(String digest, Attachment attachment) throws SAXException {
		DocumentLoadingContext context = new DocumentLoadingContext();
		context.setFileVersion(DIGEST_FILE_VERSION);
		context.setMotorFinder((type, manufacturer, designation, diameter, length, motorDigest, warnings) -> null);
		context.setAttachmentFactory(name -> attachment);

		MotorHandler handler = new MotorHandler(context);
		handler.closeElement("digest", new HashMap<>(), digest, new WarningSet());
		return handler;
	}

	private static Attachment createRseAttachment(ThrustCurveMotor motor) {
		String contents = new RockSimMotorWriter().write(motor);
		return new ByteArrayAttachment("motor.rse", contents.getBytes(StandardCharsets.UTF_8));
	}

	private static ThrustCurveMotor createMotor(String designation, double peakThrust, String digest) {
		return new ThrustCurveMotor.Builder()
				// A known manufacturer default proves that explicit UNKNOWN is preserved.
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation(designation)
				.setDescription("Embedded motor test")
				.setMotorType(Motor.Type.UNKNOWN)
				.setStandardDelays(new double[] { 3, Motor.PLUGGED_DELAY })
				.setDiameter(0.024)
				.setLength(0.070)
				.setTimePoints(new double[] { 0.0, 0.5, 1.0 })
				.setThrustPoints(new double[] { 0.0, peakThrust, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.035, 0, 0, 0.100),
						new Coordinate(0.033, 0, 0, 0.070),
						new Coordinate(0.031, 0, 0, 0.040)
				})
				.setDigest(digest)
				.build();
	}

	/**
	 * Use the digest produced for a RASP motor to verify that compatible historical
	 * digests survive conversion through explicit RSE mass and CG samples.
	 */
	private static String createRaspStyleDigest(ThrustCurveMotor motor) {
		MotorDigest digest = new MotorDigest();
		digest.update(MotorDigest.DataType.TIME_ARRAY, motor.getTimePoints());
		digest.update(MotorDigest.DataType.MASS_SPECIFIC, motor.getLaunchMass(), motor.getBurnoutMass());
		digest.update(MotorDigest.DataType.FORCE_PER_TIME, motor.getThrustPoints());
		return digest.getDigest();
	}

	private static boolean containsWarning(WarningSet warnings, String text) {
		return warnings.stream().anyMatch(warning -> warning.toString().contains(text));
	}

	/** In-memory attachment used for valid and corrupt RSE content. */
	private static class ByteArrayAttachment extends Attachment {
		private final byte[] contents;

		private ByteArrayAttachment(String name, byte[] contents) {
			super(name);
			this.contents = contents;
		}

		@Override
		public InputStream getBytes() {
			return new ByteArrayInputStream(contents);
		}
	}

	/** Attachment implementation that models an absent zip entry. */
	private static class MissingAttachment extends Attachment {
		private MissingAttachment(String name) {
			super(name);
		}

		@Override
		public InputStream getBytes() throws DecalNotFoundException {
			throw new DecalNotFoundException(getName(), null);
		}
	}
}
