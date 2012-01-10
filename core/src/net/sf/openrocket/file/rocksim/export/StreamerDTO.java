package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.Streamer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = RocksimCommonConstants.STREAMER)
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamerDTO extends BasePartDTO {

    @XmlElement(name = RocksimCommonConstants.WIDTH)
        private double width = 0d;
        @XmlElement(name = RocksimCommonConstants.DRAG_COEFFICIENT)
        private double dragCoefficient = 0.75d;

    public StreamerDTO() {
    }

    public StreamerDTO(Streamer ec) {
        super(ec);
        setWidth(ec.getStripWidth() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setDragCoefficient(ec.getCD());
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
