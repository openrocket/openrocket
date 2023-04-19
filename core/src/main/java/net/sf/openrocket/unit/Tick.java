package net.sf.openrocket.unit;

public final class Tick {
	public final double value;
	public final double unitValue;
	public final boolean major;
	public final boolean notable;
	
	public Tick(double value, double unitValue, boolean major, boolean notable) {
		this.value = value;
		this.unitValue = unitValue;
		this.major = major;
		this.notable = notable;
	}
	
	@Override
	public String toString() {
		String s = "Tick[value="+value;
		if (major)
			s += ",major";
		else
			s += ",minor";
		if (notable)
			s += ",notable";
		s+= "]";
		return s;
	}
}
