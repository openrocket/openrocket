package info.openrocket.swing.gui.figure3d.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaybackClockTest {

	@Test
	void loopingPreservesOvershootAndSupportsNonzeroStartAndReversePlayback() {
		PlaybackClock clock = new PlaybackClock(2.0, 5.0);
		clock.setLooping(true);
		clock.setTime(4.5);
		clock.update(1.0);
		assertEquals(2.5, clock.getTime(), 1e-9);
		clock.update(9.25);
		assertEquals(2.75, clock.getTime(), 1e-9);
		clock.setRate(-1.0);
		clock.update(1.0);
		assertEquals(4.75, clock.getTime(), 1e-9);
		clock.setLooping(false);
		clock.setRate(1.0);
		clock.update(1.0);
		assertEquals(5.0, clock.getTime(), 1e-9);
	}

	@Test
	void loopingAnEmptyReplayDoesNotProduceAnInvalidTime() {
		PlaybackClock clock = new PlaybackClock(4.0, 4.0);
		clock.setLooping(true);
		clock.update(100.0);
		assertEquals(4.0, clock.getTime());
	}

	@Test
	void aPausedLoopCanBeScrubbedToItsExactEnd() {
		PlaybackClock clock = new PlaybackClock(2.0, 5.0);
		clock.setLooping(true);
		clock.setRate(0.0);
		clock.setTime(5.0);
		clock.update(0.5);
		assertEquals(5.0, clock.getTime());
	}

	@Test
	void clampsSetTimeToRange() {
		PlaybackClock clock = new PlaybackClock(2.0, 5.0);

		clock.setTime(-1.0);
		assertEquals(2.0, clock.getTime());

		clock.setTime(7.0);
		assertEquals(5.0, clock.getTime());
	}

	@Test
	void pausesWhenRateIsZero() {
		PlaybackClock clock = new PlaybackClock(0.0, 10.0);

		clock.setTime(3.0);
		clock.setRate(0.0);
		clock.update(5.0);

		assertEquals(3.0, clock.getTime());
	}

	@Test
	void resumeDoesNotChargeTheTimeSpentWaitingForTheNextFrame() {
		PlaybackClock clock = new PlaybackClock(0.0, 10.0);
		clock.setRate(0.0);

		clock.setRate(1.0);
		clock.update(1.0);
		assertEquals(0.0, clock.getTime());

		clock.update(0.25);
		assertEquals(0.25, clock.getTime());

		clock.setRate(0.0);
		clock.update(1.0);
		clock.setRate(1.0);
		clock.update(1.0);
		assertEquals(0.25, clock.getTime());

		clock.update(0.25);
		assertEquals(0.5, clock.getTime());
	}

	@Test
	void accumulatesAtPlaybackRateAndClampsAtEnds() {
		PlaybackClock clock = new PlaybackClock(0.0, 3.0);

		clock.setRate(2.0);
		clock.update(0.5);
		assertEquals(1.0, clock.getTime());

		clock.update(2.0);
		assertEquals(3.0, clock.getTime());

		clock.setRate(-4.0);
		clock.update(1.0);
		assertEquals(0.0, clock.getTime());
	}

	@Test
	void endCannotPrecedeStart() {
		PlaybackClock clock = new PlaybackClock(4.0, 1.0);

		assertEquals(4.0, clock.getStart());
		assertEquals(4.0, clock.getEnd());
		assertEquals(4.0, clock.getTime());
	}
}
