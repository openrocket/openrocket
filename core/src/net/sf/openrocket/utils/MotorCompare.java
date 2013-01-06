package net.sf.openrocket.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.file.motor.MotorLoader;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;

public class MotorCompare {
	
	/** Maximum allowed difference in maximum thrust */
	private static final double MAX_THRUST_MARGIN = 0.30;
	/** Maximum allowed difference in total impulse */
	private static final double TOTAL_IMPULSE_MARGIN = 0.20;
	/** Maximum allowed difference in mass values */
	private static final double MASS_MARGIN = 0.20;
	
	/** Number of time points in thrust curve to compare */
	@SuppressWarnings("unused")
	private static final int DIVISIONS = 100;
	/** Maximum difference in thrust for a time point to be considered invalid */
	@SuppressWarnings("unused")
	private static final double THRUST_MARGIN = 0.20;
	/** Number of invalid time points allowed */
	@SuppressWarnings("unused")
	private static final int ALLOWED_INVALID_POINTS = 20;
	
	/** Minimum number of thrust curve points allowed (incl. start and end points) */
	private static final int MIN_POINTS = 7;
	
	
	public static void main(String[] args) throws IOException {
		@SuppressWarnings("unused")
		final double maxThrust;
		@SuppressWarnings("unused")
		final double maxTime;
		@SuppressWarnings("unused")
		int maxDelays;
		@SuppressWarnings("unused")
		int maxPoints;
		@SuppressWarnings("unused")
		int maxCommentLen;
		
		@SuppressWarnings("unused")
		double min, max, diff;
		
		@SuppressWarnings("unused")
		int[] goodness;
		
		@SuppressWarnings("unused")
		boolean bad = false;
		@SuppressWarnings("unused")
		List<String> cause = new ArrayList<String>();
		
		MotorLoader loader = new GeneralMotorLoader();
		List<ThrustCurveMotor> motors = new ArrayList<ThrustCurveMotor>();
		List<String> files = new ArrayList<String>();
		
		// Load files
		System.out.printf("Files      :");
		for (String file : args) {
			System.out.printf("\t%s", file);
			List<Motor> m = null;
			try {
				InputStream stream = new FileInputStream(file);
				m = loader.load(stream, file);
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.print("(ERR:" + e.getMessage() + ")");
			}
			if (m != null) {
				motors.addAll((List) m);
				for (int i = 0; i < m.size(); i++)
					files.add(file);
			}
		}
		System.out.println();
		
		compare(motors, files);
	}
	
	



	public static void compare(List<ThrustCurveMotor> motors, List<String> files) {
		@SuppressWarnings("unused")
		final double maxThrust, maxTime;
		int maxDelays;
		int maxPoints;
		int maxCommentLen;
		
		double min, max;
		double diff;
		
		int[] goodness;
		
		boolean bad = false;
		List<String> cause = new ArrayList<String>();
		

		if (motors.size() == 0) {
			System.err.println("No motors loaded.");
			System.out.println("ERROR: No motors loaded.\n");
			return;
			
		}
		
		if (motors.size() == 1) {
			System.out.println("Best (ONLY): " + files.get(0));
			System.out.println();
			return;
		}
		
		final int n = motors.size();
		goodness = new int[n];
		

		for (String s : files) {
			System.out.print("\t" + s);
		}
		System.out.println();
		

		// Designations
		System.out.printf("Designation:");
		String des = motors.get(0).getDesignation();
		for (Motor m : motors) {
			System.out.printf("\t%s", m.getDesignation());
			if (!m.getDesignation().equals(des)) {
				cause.add("Designation");
				bad = true;
			}
		}
		System.out.println();
		
		// Manufacturers
		System.out.printf("Manufacture:");
		Manufacturer mfg = motors.get(0).getManufacturer();
		for (ThrustCurveMotor m : motors) {
			System.out.printf("\t%s", m.getManufacturer());
			if (m.getManufacturer() != mfg) {
				cause.add("Manufacturer");
				bad = true;
			}
		}
		System.out.println();
		

		// Max. thrust
		max = 0;
		min = Double.MAX_VALUE;
		System.out.printf("Max.thrust :");
		for (Motor m : motors) {
			double f = m.getMaxThrustEstimate();
			System.out.printf("\t%.2f", f);
			max = Math.max(max, f);
			min = Math.min(min, f);
		}
		diff = (max - min) / min;
		if (diff > MAX_THRUST_MARGIN) {
			bad = true;
			cause.add("Max thrust");
		}
		System.out.printf("\t(discrepancy %.1f%%)\n", 100.0 * diff);
		maxThrust = (min + max) / 2;
		

		// Total time
		max = 0;
		min = Double.MAX_VALUE;
		System.out.printf("Burn time :");
		for (Motor m : motors) {
			double t = m.getBurnTimeEstimate();
			System.out.printf("\t%.2f", t);
			max = Math.max(max, t);
			min = Math.min(min, t);
		}
		diff = (max - min) / min;
		System.out.printf("\t(discrepancy %.1f%%)\n", 100.0 * diff);
		maxTime = max;
		

		// Total impulse
		max = 0;
		min = Double.MAX_VALUE;
		System.out.printf("Impulse    :");
		for (Motor m : motors) {
			double f = m.getTotalImpulseEstimate();
			System.out.printf("\t%.2f", f);
			max = Math.max(max, f);
			min = Math.min(min, f);
		}
		diff = (max - min) / min;
		if (diff > TOTAL_IMPULSE_MARGIN) {
			bad = true;
			cause.add("Total impulse");
		}
		System.out.printf("\t(discrepancy %.1f%%)\n", 100.0 * diff);
		

		// Initial mass
		max = 0;
		min = Double.MAX_VALUE;
		System.out.printf("Init mass  :");
		for (Motor m : motors) {
			double f = m.getLaunchCG().weight;
			System.out.printf("\t%.2f", f * 1000);
			max = Math.max(max, f);
			min = Math.min(min, f);
		}
		diff = (max - min) / min;
		if (diff > MASS_MARGIN) {
			bad = true;
			cause.add("Initial mass");
		}
		System.out.printf("\t(discrepancy %.1f%%)\n", 100.0 * diff);
		

		// Empty mass
		max = 0;
		min = Double.MAX_VALUE;
		System.out.printf("Empty mass :");
		for (Motor m : motors) {
			double f = m.getEmptyCG().weight;
			System.out.printf("\t%.2f", f * 1000);
			max = Math.max(max, f);
			min = Math.min(min, f);
		}
		diff = (max - min) / min;
		if (diff > MASS_MARGIN) {
			bad = true;
			cause.add("Empty mass");
		}
		System.out.printf("\t(discrepancy %.1f%%)\n", 100.0 * diff);
		

		// Delays
		maxDelays = 0;
		System.out.printf("Delays     :");
		for (ThrustCurveMotor m : motors) {
			System.out.printf("\t%d", m.getStandardDelays().length);
			maxDelays = Math.max(maxDelays, m.getStandardDelays().length);
		}
		System.out.println();
		

		// Data points
		maxPoints = 0;
		System.out.printf("Points     :");
		for (Motor m : motors) {
			System.out.printf("\t%d", ((ThrustCurveMotor) m).getTimePoints().length);
			maxPoints = Math.max(maxPoints, ((ThrustCurveMotor) m).getTimePoints().length);
		}
		System.out.println();
		

		// Comment length
		maxCommentLen = 0;
		System.out.printf("Comment len:");
		for (Motor m : motors) {
			System.out.printf("\t%d", m.getDescription().length());
			maxCommentLen = Math.max(maxCommentLen, m.getDescription().length());
		}
		System.out.println();
		

		if (bad) {
			String str = "ERROR: ";
			for (int i = 0; i < cause.size(); i++) {
				str += cause.get(i);
				if (i < cause.size() - 1)
					str += ", ";
			}
			str += " differs";
			System.out.println(str);
			System.out.println();
			return;
		}
		
		// Check consistency
		// TODO: Does not check consistency
		//		int invalidPoints = 0;
		//		for (int i = 0; i < DIVISIONS; i++) {
		//			double t = maxTime * i / (DIVISIONS - 1);
		//			min = Double.MAX_VALUE;
		//			max = 0;
		//			//				System.out.printf("%.2f:", t);
		//			for (Motor m : motors) {
		//				double f = m.getThrust(t);
		//				//					System.out.printf("\t%.2f", f);
		//				min = Math.min(min, f);
		//				max = Math.max(max, f);
		//			}
		//			diff = (max - min) / maxThrust;
		//			//				System.out.printf("\t(diff %.1f%%)\n", diff*100);
		//			if (diff > THRUST_MARGIN)
		//				invalidPoints++;
		//		}
		//		
		//		if (invalidPoints > ALLOWED_INVALID_POINTS) {
		//			System.out.println("ERROR: " + invalidPoints + "/" + DIVISIONS
		//					+ " points have thrust differing over " + (THRUST_MARGIN * 100) + "%");
		//			System.out.println();
		//			return;
		//		}
		

		// Check goodness
		for (int i = 0; i < n; i++) {
			ThrustCurveMotor m = motors.get(i);
			if (m.getStandardDelays().length == maxDelays)
				goodness[i] += 1000;
			if (((ThrustCurveMotor) m).getTimePoints().length == maxPoints)
				goodness[i] += 100;
			if (m.getDescription().length() == maxCommentLen)
				goodness[i] += 10;
			if (files.get(i).matches(".*\\.[rR][sS][eE]$"))
				goodness[i] += 1;
		}
		int best = 0;
		for (int i = 1; i < n; i++) {
			if (goodness[i] > goodness[best])
				best = i;
		}
		

		// Verify enough points
		int pts = ((ThrustCurveMotor) motors.get(best)).getTimePoints().length;
		if (pts < MIN_POINTS) {
			System.out.println("WARNING: Best has only " + pts + " data points");
		}
		
		System.out.println("Best (" + goodness[best] + "): " + files.get(best));
		System.out.println();
		

	}
	
}
