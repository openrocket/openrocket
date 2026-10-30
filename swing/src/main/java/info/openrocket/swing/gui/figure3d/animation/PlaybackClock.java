package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.core.util.MathUtil;

/**
 * Playback time for an animation: advanced by wall-clock frame time at an adjustable rate (0 is
 * paused), clamped to its range, optionally looping. Thread-safe: the Swing thread seeks and
 * changes the rate while the render thread advances it.
 */
public final class PlaybackClock {
	private final double start;
	private final double end;
	private double time;
	private double rate = 1.0;
	private boolean looping;
	private boolean discardElapsedTimeOnNextUpdate;

	public PlaybackClock(double start, double end) {
		this.start = start;
		this.end = Math.max(end, start);
		this.time = start;
	}

	/** Advances by the given wall-clock seconds times the rate, wrapping when looping. */
	public synchronized void update(double dtRealSeconds) {
		if (discardElapsedTimeOnNextUpdate) {
			discardElapsedTimeOnNextUpdate = false;
			return;
		}
		time += rate * dtRealSeconds;
		if (looping && rate != 0.0 && end > start && (time >= end || time < start)) {
			double duration = end - start;
			time = start + ((time - start) % duration + duration) % duration;
		}
		time = MathUtil.clamp(time, start, end);
	}

	public synchronized double getTime() {
		return time;
	}

	/** Seeks to the given time, clamped to the range. */
	public synchronized void setTime(double t) {
		time = MathUtil.clamp(t, start, end);
	}

	public synchronized double getRate() {
		return rate;
	}

	/** Sets playback speed relative to real time; 0 pauses. */
	public synchronized void setRate(double r) {
		if (rate == 0.0 && r != 0.0) {
			// A paused replay renders on demand, so the next frame's delta may span the entire
			// pause. Start timing from that frame instead of charging the pause to the animation.
			discardElapsedTimeOnNextUpdate = true;
		}
		rate = r;
	}

	public synchronized boolean isLooping() {
		return looping;
	}

	public synchronized void setLooping(boolean looping) {
		this.looping = looping;
	}

	public double getStart() {
		return start;
	}

	public double getEnd() {
		return end;
	}
}
