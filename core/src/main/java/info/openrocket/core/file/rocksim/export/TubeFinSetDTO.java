package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.TubeFinSet;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This class models an XML element for a Rocksim TubeFinSet.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.TUBE_FIN_SET)
public class TubeFinSetDTO extends BasePartDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.OD)
    private double od = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.ID)
    private double id = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.TUBE_COUNT)
    private int tubeCount = 0;
    @JacksonXmlProperty(localName = RockSimCommonConstants.MAX_TUBES_ALLOWED)
    private int maxTubeCount = 0;

    /**
     * Default constructor.
     */
    public TubeFinSetDTO() {
    }

    /**
     * Copy constructor. Fully populates this instance with values taken from the OR
     * TubeFinSet.
     *
     * @param theORTubeFinSet The OR TubeFinSet component to be serialized in
     *                        Rocksim format
     */
    public TubeFinSetDTO(TubeFinSet theORTubeFinSet) {
        super(theORTubeFinSet);
        setId(theORTubeFinSet.getInnerRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setOd(theORTubeFinSet.getOuterRadius() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_RADIUS);
        setRadialAngle(theORTubeFinSet.getBaseRotation());
        setTubeCount(theORTubeFinSet.getFinCount());
    }

    /**
     * Set the outer diameter of the tube fin(s).
     *
     * @return diameter in meters
     */
    public double getOd() {
        return od;
    }

    /**
     * Set the outer diameter of the tube fin(s).
     *
     * @param theOd diameter in meters
     */
    public void setOd(double theOd) {
        od = theOd;
    }

    /**
     * Get the inner diameter of the tube fin(s).
     *
     * @return diameter in meters
     */
    public double getId() {
        return id;
    }

    /**
     * Set the inner diameter of the tube fin(s).
     *
     * @param theId diameter in meters
     */
    public void setId(double theId) {
        id = theId;
    }

    /**
     * Get the tube fin count.
     *
     * @return # tube fins
     */
    public int getTubeCount() {
        return tubeCount;
    }

    /**
     * Set the tube fin count.
     *
     * @param theTubeCount # tube fins
     */
    public void setTubeCount(final int theTubeCount) {
        tubeCount = theTubeCount;
        maxTubeCount = tubeCount;
    }

    /**
     * Get the max tube fin count. Since OR doesn't have this concept, just set it
     * to the actual count.
     *
     * @return # tube fins
     */
    public int getMaxTubeCount() {
        return maxTubeCount;
    }
}
