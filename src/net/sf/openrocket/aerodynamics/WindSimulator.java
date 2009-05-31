package net.sf.openrocket.aerodynamics;

import java.util.Random;

import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.PinkNoise;


public class WindSimulator {
	
	/** Source for seed numbers. */
	private static final Random seedSource = new Random();

    /** Pink noise alpha parameter. */
    private static final double ALPHA = 5.0/3.0;
    
    /** Number of poles to use in the pink noise IIR filter. */
    private static final int POLES = 2;
    
    /** The standard deviation of the generated pink noise with the specified number of poles. */
    private static final double STDDEV = 2.252;
    
    /** Time difference between random samples. */
    private static final double DELTA_T = 0.05;

    
    private double average = 0;
    private double standardDeviation = 0;
    
    private int seed;
    
    private PinkNoise randomSource = null;
    private double time1;
    private double value1, value2;
    

    /**
     * Construct a new wind simulator with a random starting seed value.
     */
    public WindSimulator() {
    	synchronized(seedSource) {
        	seed = seedSource.nextInt();
    	}
    }
    
    
    
    /**
     * Return the average wind speed.
     * 
     * @return the average wind speed.
     */
    public double getAverage() {
        return average;
    }
    /**
     * Set the average wind speed.  This method will also modify the
     * standard deviation such that the turbulence intensity remains constant.
     * 
     * @param average the average wind speed to set
     */
    public void setAverage(double average) {
        double intensity = getTurbulenceIntensity();
        this.average = Math.max(average, 0);
        setTurbulenceIntensity(intensity);
    }
    
    
    
    /**
     * Return the standard deviation from the average wind speed.
     * 
     * @return the standard deviation of the wind speed
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }
    
    /**
     * Set the standard deviation of the average wind speed.
     * 
     * @param standardDeviation the standardDeviation to set
     */
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = Math.max(standardDeviation, 0);
    }
    
    
    /**
     * Return the turbulence intensity (standard deviation / average).
     * 
     * @return  the turbulence intensity
     */
    public double getTurbulenceIntensity() {
        if (MathUtil.equals(average, 0)) {
            if (MathUtil.equals(standardDeviation, 0))
                return 0;
            else
                return 1000;
        }
        return standardDeviation / average;
    }

    /**
     * Set the standard deviation to match the turbulence intensity.
     * 
     * @param intensity   the turbulence intensity
     */
    public void setTurbulenceIntensity(double intensity) {
        setStandardDeviation(intensity * average);
    }
    
    
    
    
    
    public int getSeed() {
        return seed;
    }
    
    public void setSeed(int seed) {
        if (this.seed == seed)
            return;
        this.seed = seed;
    }
    
    
    
    public double getWindSpeed(double time) {
    	if (time < 0) {
    		throw new IllegalArgumentException("Requesting wind speed at t="+time);
    	}
    	
        if (randomSource == null) {
        	randomSource = new PinkNoise(ALPHA, POLES, new Random(seed));
            time1 = 0;
            value1 = randomSource.nextValue();
            value2 = randomSource.nextValue();
        }
        
        if (time < time1) {
        	reset();
        	return getWindSpeed(time);
        }
        
        while (time1 + DELTA_T < time) {
            value1 = value2;
            value2 = randomSource.nextValue();
            time1 += DELTA_T;
        }
        
        double a = (time - time1)/DELTA_T;

        
        return average + (value1 * (1-a) + value2 * a) * standardDeviation / STDDEV;
    }

    
    private void reset() {
        randomSource = null;
    }
    
    
    public static void main(String[] str) {
        
        WindSimulator sim = new WindSimulator();
        
        sim.setAverage(2);
        sim.setStandardDeviation(0.5);
        
        for (int i=0; i < 10000; i++) {
            double t = 0.01*i;
            double v = sim.getWindSpeed(t);
            System.out.printf("%d.%03d  %d.%03d\n", (int)t,((int)(t*1000))%1000, (int)v, ((int)(v*1000))%1000);
//            if ((i % 5) == 0)
//                System.out.println(" ***");
//            else
//                System.out.println("");
        }
        
    }
    
}
