package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.core.util.MathUtil;

/** Small playback clock with play/pause/speed and clamping. */
public final class PlaybackClock {
	private double time;
	private double rate = 1.0;
	private final double start, end;

	public PlaybackClock(double start, double end) {
		this.start = start;
		this.end = Math.max(end, start);
		this.time = start;
	}

	public void update(double dtRealSeconds) {
		time += rate * dtRealSeconds;
		if (time < start) time = start;
		if (time > end)   time = end;
	}

	public double getTime()        { return time; }
	public void   setTime(double t){ time = MathUtil.clamp(t, start, end); }
	public double getRate()        { return rate; }
	public void   setRate(double r){ rate = r; }
	public double getStart()       { return start; }
	public double getEnd()         { return end; }
}
