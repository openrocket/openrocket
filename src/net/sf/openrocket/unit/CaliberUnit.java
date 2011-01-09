package net.sf.openrocket.unit;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.util.MathUtil;


public class CaliberUnit extends GeneralUnit {
	
	public static final double DEFAULT_CALIBER = 0.01;
	
	private final Configuration configuration;
	private final Rocket rocket;
	
	private double caliber = -1;
	

	/* Listener for rocket and configuration, resets the caliber to -1. */
	private final ChangeListener listener = new ChangeListener() {
		@Override
		public void stateChanged(ChangeEvent e) {
			caliber = -1;
		}
	};
	
	

	public CaliberUnit(Configuration configuration) {
		super(1.0, "cal");
		this.configuration = configuration;
		
		if (configuration == null) {
			this.rocket = null;
		} else {
			this.rocket = configuration.getRocket();
			configuration.addChangeListener(listener);
		}
	}
	
	public CaliberUnit(Rocket rocket) {
		super(1.0, "cal");
		this.configuration = null;
		this.rocket = rocket;
		if (rocket != null) {
			rocket.addChangeListener(listener);
		}
	}
	
	
	@Override
	public double fromUnit(double value) {
		if (caliber < 0)
			calculateCaliber();
		
		return value * caliber;
	}
	
	@Override
	public double toUnit(double value) {
		if (caliber < 0)
			calculateCaliber();
		
		return value / caliber;
	}
	
	
	// TODO: HIGH:  Check caliber calculation method...
	private void calculateCaliber() {
		caliber = 0;
		
		Iterator<RocketComponent> iterator;
		if (configuration != null) {
			iterator = configuration.iterator();
		} else if (rocket != null) {
			iterator = rocket.iterator(false);
		} else {
			Collection<RocketComponent> set = Collections.emptyList();
			iterator = set.iterator();
		}
		
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			if (c instanceof SymmetricComponent) {
				double r1 = ((SymmetricComponent) c).getForeRadius() * 2;
				double r2 = ((SymmetricComponent) c).getAftRadius() * 2;
				caliber = MathUtil.max(caliber, r1, r2);
			}
		}
		
		if (caliber <= 0)
			caliber = DEFAULT_CALIBER;
	}
}
