package net.sf.openrocket.util;

import static net.sf.openrocket.aerodynamics.AtmosphericConditions.GAMMA;
import static net.sf.openrocket.aerodynamics.AtmosphericConditions.R;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.AtmosphericConditions;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.ExactAtmosphericConditions;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.RocketLoader;
import net.sf.openrocket.rocketcomponent.Configuration;

public class Analysis {
	
	private static final double MACH_MIN = 0.01;
	private static final double MACH_MAX = 5.00001;
	private static final double MACH_STEP = 0.02;
	
	private static final double AOA_MACH = 0.6;
	private static final double AOA_MIN = 0;
	private static final double AOA_MAX = 15.00001*Math.PI/180;
	private static final double AOA_STEP = 0.5*Math.PI/180;
	
	private static final double REYNOLDS = 9.8e6;
	private static final double STAG_TEMP = 330;
	
	
	private final RocketLoader loader = new GeneralRocketLoader();
	private final AerodynamicCalculator calculator = new BarrowmanCalculator();
	
	private final FlightConditions conditions;
	private final double length;
	
	private final Configuration config;
	
	private final AtmosphericConditions atmosphere;
	
	
	
	private Analysis(String filename) throws RocketLoadException {

		OpenRocketDocument doc = loader.load(new File(filename));
		config = doc.getRocket().getDefaultConfiguration();
		
		calculator.setConfiguration(config);
		
		conditions = new FlightConditions(config);
		System.out.println("Children: " + Arrays.toString(config.getRocket().getChildren()));
		System.out.println("Children: " + Arrays.toString(config.getRocket().getChild(0).getChildren()));
		length = config.getLength();
		System.out.println("Rocket length: " + (length*1000)+"mm");
		
		atmosphere = new ExactAtmosphericConditions();
		
	}
	
	
	private double computeVelocityAndAtmosphere(double mach, double reynolds, double stagTemp) {
		final double temperature;
		final double pressure;
		
		
		temperature = stagTemp / (1 + (GAMMA-1)/2 * MathUtil.pow2(mach));
		
		// Speed of sound
		double c = 331.3 * Math.sqrt(1 + (temperature - 273.15)/273.15);
		
		// Free-stream velocity
		double v0 = c * mach;
		
//		kin.visc. = (3.7291e-06 + 4.9944e-08 * temperature) / density
		pressure = reynolds * (3.7291e-06 + 4.9944e-08 * temperature) * R * temperature / 
					(v0 * length);
		
		atmosphere.pressure = pressure;
		atmosphere.temperature = temperature;
		conditions.setAtmosphericConditions(atmosphere);
		conditions.setVelocity(v0);
		
		if (Math.abs(conditions.getMach() - mach) > 0.001) {
			System.err.println("Computed mach: "+conditions.getMach() + " requested "+mach);
//			System.exit(1);
		}
		
		return v0;
	}
	
	
	
	private void computeVsMach(PrintStream stream) {
		
		conditions.setAOA(0);
		conditions.setTheta(45*Math.PI/180);
		stream.println("% Mach, Caxial, CP, , CNa, Croll");
		
		for (double mach = MACH_MIN; mach <= MACH_MAX; mach += MACH_STEP) {
			
			computeVelocityAndAtmosphere(mach, REYNOLDS, STAG_TEMP);
//			conditions.setMach(mach);
			
			
			AerodynamicForces forces = calculator.getAerodynamicForces(0, conditions, null);
			

			double Re = conditions.getVelocity() * 
					calculator.getConfiguration().getLength() / 
					conditions.getAtmosphericConditions().getKinematicViscosity();
			if (Math.abs(Re - REYNOLDS) > 1) {
				throw new RuntimeException("Re="+Re);
			}
			stream.printf("%f, %f, %f, %f, %f\n", mach, forces.Caxial, forces.cp.x, forces.CNa, 
					forces.Croll);
		}
		
	}

	
	
	private void computeVsAOA(PrintStream stream, double thetaDeg) {
		
		computeVelocityAndAtmosphere(AOA_MACH, REYNOLDS, STAG_TEMP);
		conditions.setTheta(thetaDeg * Math.PI/180);
		stream.println("% AOA, CP, CN, Cm   at theta = "+thetaDeg);
		
		for (double aoa = AOA_MIN; aoa <= AOA_MAX; aoa += AOA_STEP) {

			conditions.setAOA(aoa);
			AerodynamicForces forces = calculator.getAerodynamicForces(0, conditions, null);
			

			double Re = conditions.getVelocity() * 
					calculator.getConfiguration().getLength() / 
					conditions.getAtmosphericConditions().getKinematicViscosity();
			if (Math.abs(Re - REYNOLDS) > 1) {
				throw new RuntimeException("Re="+Re);
			}
			stream.printf("%f, %f, %f, %f\n", aoa*180/Math.PI, forces.cp.x, forces.CN, forces.Cm);
		}
		
	}
	
	
	

	public static void main(String arg[]) throws Exception {
		
		if (arg.length != 2) {
			System.err.println("Arguments:  <rocket file> <output prefix>");
			System.exit(1);
		}

		Analysis a = new Analysis(arg[0]);
		final String prefix = arg[1];
		

		String name;
		double v0 = a.computeVelocityAndAtmosphere(0.6, 9.8e6, 322);
		System.out.printf("Sanity test: mach = %.1f v=%.1f temp=%.1f pres=%.0f c=%.1f " +
				"ref.length=%.1fmm\n",
				a.conditions.getMach(), v0, a.atmosphere.temperature, a.atmosphere.pressure, 
				a.atmosphere.getMachSpeed(), a.conditions.getRefLength()*1000);
		System.out.println();
		
		
		// CA, CP, Croll vs. Mach  at AOA=0
		name = prefix + "-CA-CP-CNa-Croll-vs-Mach.csv";
		System.out.println("Computing CA, CP, CNa, Croll vs. Mach to file "+name);
		a.computeVsMach(new PrintStream(name));

		
		// CN & Cm vs. AOA  at M=0.6
		name = prefix + "-CP-CN-Cm-vs-AOA-0.csv";
		System.out.println("Computing CP, CN, Cm vs. AOA at theta=0 to file "+name);
		a.computeVsAOA(new PrintStream(name), 0);

		// CN & Cm vs. AOA  at M=0.6
		name = prefix + "-CP-CN-Cm-vs-AOA-22.5.csv";
		System.out.println("Computing CP, CN, Cm vs. AOA at theta=22.5 to file "+name);
		a.computeVsAOA(new PrintStream(name), 0);

		// CN & Cm vs. AOA  at M=0.6
		name = prefix + "-CP-CN-Cm-vs-AOA-45.csv";
		System.out.println("Computing CP, CN, Cm vs. AOA at theta=45 to file "+name);
		a.computeVsAOA(new PrintStream(name), 0);

		
		System.out.println("Done.");
	}
	
	
	
	
	
}
