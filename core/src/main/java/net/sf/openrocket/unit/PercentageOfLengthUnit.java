package net.sf.openrocket.unit;

import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.util.BugException;

public class PercentageOfLengthUnit extends GeneralUnit {

    private final FlightConfiguration configuration;
    private final Rocket rocket;

    private int rocketModId = -1;
    private int configurationModId = -1;

    private double referenceLength = -1;

    public PercentageOfLengthUnit(FlightConfiguration configuration) {
        super(0.01, "%");
        this.configuration = configuration;

        if (configuration == null) {
            this.rocket = null;
        } else {
            this.rocket = configuration.getRocket();
        }
    }

    public PercentageOfLengthUnit(Rocket rocket) {
        super(0.01, "%");
        this.configuration = null;
        this.rocket = rocket;
    }

    public PercentageOfLengthUnit(double reference) {
        super(0.01, "%");
        this.configuration = null;
        this.rocket = null;
        this.referenceLength = reference;

        if (reference <= 0) {
            throw new IllegalArgumentException("Illegal reference = " + reference);
        }
    }

    @Override
    public double fromUnit(double value) {
        checkLength();

        return value * referenceLength * multiplier;
    }

    @Override
    public double toUnit(double value) {
        checkLength();

        return value / referenceLength / multiplier;
    }

    private void checkLength() {
        if (configuration != null && configuration.getModID() != configurationModId) {
            referenceLength = -1;
            configurationModId = configuration.getModID();
        }
        if (rocket != null && rocket.getModID() != rocketModId) {
            referenceLength = -1;
            rocketModId = rocket.getModID();
        }
        if (referenceLength < 0) {
            if (configuration != null) {
                referenceLength = getReferenceLength(configuration);
            } else if (rocket != null) {
                referenceLength = getReferenceLength(rocket);
            } else {
                throw new BugException("Both rocket and configuration are null");
            }
        }
    }

    /**
     * Get the reference length of a rocket configuration.
     *
     * @param config	the rocket configuration
     * @return			the reference length of the rocket
     */
    public static double getReferenceLength(FlightConfiguration config) {
        return config.getLengthAerodynamic();
    }

    /**
     * Get the reference length of a rocket.
     *
     * @param rocket	the rocket
     * @return			the reference length of the rocket
     */
    public static double getReferenceLength(Rocket rocket) {
        return getReferenceLength(rocket.getSelectedConfiguration());
    }

}