package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.Streamer;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * This class models a Rocksim XML element for a streamer.
 */
@XmlRootElement(name = RockSimCommonConstants.STREAMER)
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamerDTO extends BasePartDTO {

    @XmlElement(name = RockSimCommonConstants.WIDTH)
    private double width = 0d;
    @XmlElement(name = RockSimCommonConstants.DRAG_COEFFICIENT)
    private double dragCoefficient = 0.75d;

    /**
     * The default constructor.
     */
    public StreamerDTO() {
    }

    /**
     * Copy constructor. This constructor fully populates this instance with values
     * taken from the OR component.
     *
     * @param theORStreamer the OR streamer component
     */
    public StreamerDTO(Streamer theORStreamer) {
        super(theORStreamer);
        setWidth(theORStreamer.getStripWidth() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setDragCoefficient(theORStreamer.getCD());
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double theWidth) {
        width = theWidth;
    }

    public double getDragCoefficient() {
        return dragCoefficient;
    }

    public void setDragCoefficient(double theDragCoefficient) {
        dragCoefficient = theDragCoefficient;
    }
}
