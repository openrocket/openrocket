package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.rocketcomponent.Streamer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = "Streamer")
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamerDTO extends BasePartDTO {

        @XmlElement(name = "Width")
        private double width = 0d;
        @XmlElement(name = "DragCoefficient")
        private double dragCoefficient = 0.75d;

    public StreamerDTO() {
    }

    public StreamerDTO(Streamer ec) {
        super(ec);
        setWidth(ec.getStripWidth() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
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
