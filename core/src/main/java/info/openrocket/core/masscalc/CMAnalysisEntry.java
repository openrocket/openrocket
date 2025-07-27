package info.openrocket.core.masscalc;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;

public class CMAnalysisEntry {

    public CMAnalysisEntry(final Object _src) {
        source = _src;
        if (source instanceof RocketComponent) {
            name = ((RocketComponent) source).getName();
        } else if (source instanceof Motor) {
            name = ((Motor) source).getDesignation();
        } else {
            name = null;
        }
        eachMass = Double.NaN;
        totalCM = Coordinate.NaN;
    }

    public String name;
    public Object source;
    public double eachMass;
    public Coordinate totalCM;

    public void updateEachMass(final double newMass) {
        if (Double.isNaN(eachMass)) {
            eachMass = newMass;
        }
    }

    public void updateAverageCM(final Coordinate newCM) {
        if (this.totalCM.isNaN()) {
            this.totalCM = newCM;
        } else {
            this.totalCM = totalCM.average(newCM);
        }
    }
}
