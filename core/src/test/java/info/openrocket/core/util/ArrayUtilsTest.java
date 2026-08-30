package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ArrayUtilsTest {

	@Test
	public void testCopyOfRange_NullArg() {
		assertThrows(NullPointerException.class, () -> {
			ArrayUtils.copyOfRange((Integer[]) null, 0, 14);
		});
	}

	@Test
	public void testCopyOfRange_StartTooBig() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			Integer[] original = new Integer[5];
			ArrayUtils.copyOfRange(original, 8, 14);
		});
	}

	@Test
	public void testCopyOfRange_StartTooSmall() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
			Integer[] original = new Integer[5];
			ArrayUtils.copyOfRange(original, -1, 14);
		});
	}

	@Test
	public void testCopyOfRange_IllegalRange() {
		assertThrows(IllegalArgumentException.class, () -> {
			Integer[] original = new Integer[5];
			ArrayUtils.copyOfRange(original, 3, 2);
		});
	}

	@Test
	public void testCopyOfRange() {
		Integer[] original = new Integer[5];
		for (int i = 0; i < 5; i++) {
			original[i] = i;
		}
		Integer[] copy = ArrayUtils.copyOfRange(original, 0, 0);
		assertEquals(0, copy.length);

		copy = ArrayUtils.copyOfRange(original, 2, 2);
		assertEquals(0, copy.length);

		copy = ArrayUtils.copyOfRange(original, 0, 2);
		assertEquals(2, copy.length);
		for (int i = 0; i < 2; i++) {
			assertEquals(original[i], copy[i]);
		}

		copy = ArrayUtils.copyOfRange(original, 2, 5);
		assertEquals(3, copy.length);
		for (int i = 0; i < 3; i++) {
			assertEquals(original[i + 2], copy[i]);
		}

		copy = ArrayUtils.copyOfRange(original, 2, 15);
		assertEquals(13, copy.length);
		for (int i = 0; i < 3; i++) {
			assertEquals(original[i + 2], copy[i]);
		}
		for (int i = 3; i < 13; i++) {
			assertNull(copy[i]);
		}

	}

	@Test
	public void testCopyOfRange_ZeroSize() {
		Integer[] original = new Integer[0];

		Integer[] copy = ArrayUtils.copyOfRange(original, 0, 0);
		assertEquals(0, copy.length);

		copy = ArrayUtils.copyOfRange(original, 0, 2);
		assertEquals(2, copy.length);
		for (int i = 0; i < 2; i++) {
			assertEquals(null, copy[i]);
		}

	}

	@Test
	public void testRante0() {
		double[] ary = ArrayUtils.range(0.0, 0.0, 1.0);
		assertEquals(1, ary.length);
		assertEquals(0.0, ary[0], 0.0);
	}

	@Test
	public void testRange1() {
		double[] ary = ArrayUtils.range(0.0, 0.5, 1.0);
		assertEquals(1, ary.length);
		assertEquals(0.0, ary[0], 0.0);
	}

	@Test
	public void testRange2() {
		double[] ary = ArrayUtils.range(0.0, 1.0, 0.5);
		assertEquals(3, ary.length);
		assertEquals(0.0, ary[0], 0.0);
		assertEquals(0.5, ary[1], 0.0);
		assertEquals(1.0, ary[2], 0.0);
	}

	@Test
	public void testRange3() {
		double[] ary = ArrayUtils.range(0.0, 1.0, 0.4);
		assertEquals(3, ary.length);
		assertEquals(0.0, ary[0], 0.0);
		assertEquals(0.4, ary[1], 0.0);
		assertEquals(0.8, ary[2], 0.0);
	}

	@Test
	public void testRange4() {
		double[] ary = ArrayUtils.range(0.0, 10.0, 0.5);
		assertEquals(21, ary.length);
		int i = 0;
		for (double d = 0.0; d < 10.2; d += 0.5) {
			assertEquals(d, ary[i++], 0.0);
		}
		assertEquals(i, ary.length);
	}

	@Test
	public void testCopyOfRangeForPrimitiveDoubles() {
		double[] source = new double[] { 1.0, 2.0, 3.5 };

		double[] exact = ArrayUtils.copyOf(source, 3);
		assertArrayEquals(new double[] { 1.0, 2.0, 3.5 }, exact, 0.0);

		double[] shortened = ArrayUtils.copyOfRange(source, 1, 3);
		assertArrayEquals(new double[] { 2.0, 3.5 }, shortened, 0.0);

		double[] extended = ArrayUtils.copyOfRange(source, 1, 5);
		assertEquals(4, extended.length);
		assertEquals(2.0, extended[0], 0.0);
		assertEquals(3.5, extended[1], 0.0);
		assertEquals(0.0, extended[2], 0.0);
		assertEquals(0.0, extended[3], 0.0);
	}

	@Test
	public void testCopyOfRangeForPrimitiveBytes() {
		byte[] data = new byte[] { 5, 6, 7 };

		byte[] prefix = ArrayUtils.copyOfRange(data, 0, 2);
		assertArrayEquals(new byte[] { 5, 6 }, prefix);

		byte[] padded = ArrayUtils.copyOfRange(data, 2, 5);
		assertEquals(3, padded.length);
		assertEquals(7, padded[0]);
		assertEquals(0, padded[1]);
		assertEquals(0, padded[2]);
	}

	@Test
	public void testMeanVarianceDerivedStatistics() {
		double[] values = new double[] { 1.0, 2.0, 3.0, 4.0 };

		assertEquals(2.5, ArrayUtils.mean(values), 1.0e-12);
		assertEquals(4.0, ArrayUtils.max(values), 0.0);
		assertEquals(1.0, ArrayUtils.min(values), 0.0);
		assertEquals(1.25, ArrayUtils.variance(values), 1.0e-12);
		assertEquals(Math.sqrt(1.25), ArrayUtils.stdev(values), 1.0e-12);
		assertEquals(Math.sqrt(2.5 * 2.5 + 1.25), ArrayUtils.rms(values), 1.0e-12);
	}

	@Test
	public void testTrapzMatchesExpectedArea() {
		double[] samples = new double[] { 0.0, 1.0, 2.0, 3.0 };

		assertEquals(0.0, ArrayUtils.trapz(samples, 0.0), 0.0);
		assertEquals(4.5, ArrayUtils.trapz(samples, 1.0), 1.0e-12);
	}

	@Test
	public void testTrapzSkipsNaNSegments() {
		double[] samples = new double[] { 0.0, Double.NaN, 2.0 };

		assertEquals(0.0, ArrayUtils.trapz(samples, 1.0), 0.0);
	}

	@Test
	public void testTrapzWithDegenerateInputsReturnsZero() {
		assertEquals(0.0, ArrayUtils.trapz(new double[] { 1.0 }, 1.0), 0.0);
		assertEquals(0.0, ArrayUtils.trapz(new double[] { 1.0, 2.0 }, -1.0), 0.0);
	}

	@Test
	public void testTnear() {
		double[] range = ArrayUtils.range(0.0, 10.0, 20.0);
		double nearest = ArrayUtils.tnear(range, 3.1, 0.0, 2.0);
		assertEquals(0.0, nearest, 0.0);

		nearest = ArrayUtils.tnear(range, -1.0, 0.0, 2.0);
		assertEquals(0.0, nearest, 0.0);

		/*nearest = ArrayUtils.tnear(range, 9.9, 0.0, 2.0);
		assertEquals(10.0, nearest, 0.0);

		nearest = ArrayUtils.tnear(range, 100, 0.0, 2.0);
		assertEquals(20.0, nearest, 0.0);*/
	}

	@Test
	public void testRangeWithDegenerateInputsReturnsEmpty() {
		// stop before start would previously compute a negative array size
		assertEquals(0, ArrayUtils.range(5.0, 0.0, 0.05).length);
		assertEquals(0, ArrayUtils.range(1.0e9, 5.0, 0.05).length);
		assertEquals(0, ArrayUtils.range(0.0, 10.0, 0.0).length);
		assertEquals(0, ArrayUtils.range(0.0, 10.0, -1.0).length);
		assertEquals(0, ArrayUtils.range(Double.NaN, 10.0, 1.0).length);
		assertEquals(0, ArrayUtils.range(0.0, Double.NaN, 1.0).length);
	}

	@Test
	public void testStatisticsIgnoreNaNValues() {
		double[] values = new double[] { 1.0, Double.NaN, 3.0 };

		assertEquals(2.0, ArrayUtils.mean(values), 1.0e-12);
		assertEquals(1.0, ArrayUtils.variance(values), 1.0e-12);
	}

	@Test
	public void testStatisticsOfEmptyArray() {
		double[] empty = new double[0];

		assertTrue(Double.isNaN(ArrayUtils.mean(empty)));
		assertTrue(Double.isNaN(ArrayUtils.max(empty)));
		assertTrue(Double.isNaN(ArrayUtils.min(empty)));
		assertTrue(Double.isNaN(ArrayUtils.variance(empty)));
	}

	@Test
	public void testRangeProducesMonotonicSequence() {
		double[] ary = ArrayUtils.range(-1.0, 1.0, 0.25);
		for (int i = 1; i < ary.length; i++) {
			assertTrue(ary[i] - ary[i - 1] >= 0.25 - 1.0e-12);
		}
	}

}
