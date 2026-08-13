package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.Streamer;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This class models a Rocksim XML element for a streamer.
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.STREAMER)
public class StreamerDTO extends BasePartDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.WIDTH)
    private double width = 0.0d;
    @JacksonXmlProperty(localName = RockSimCommonConstants.DRAG_COEFFICIENT)
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
