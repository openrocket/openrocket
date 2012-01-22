package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.Streamer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class models a Rocksim XML element for a streamer.
 */
@XmlRootElement(name = RocksimCommonConstants.STREAMER)
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamerDTO extends BasePartDTO {

    @XmlElement(name = RocksimCommonConstants.WIDTH)
        private double width = 0d;
        @XmlElement(name = RocksimCommonConstants.DRAG_COEFFICIENT)
        private double dragCoefficient = 0.75d;

    /**
     * The default constructor.
     */
    public StreamerDTO() {
    }

    /**
     * Copy constructor.  This constructor fully populates this instance with values taken from the OR component.
     *
     * @param theORStreamer  the OR streamer component
     */
    public StreamerDTO(Streamer theORStreamer) {
        super(theORStreamer);
        setWidth(theORStreamer.getStripWidth() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
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
