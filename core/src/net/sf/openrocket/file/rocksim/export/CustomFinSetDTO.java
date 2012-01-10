package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.util.Coordinate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = RocksimCommonConstants.CUSTOM_FIN_SET)
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomFinSetDTO extends FinSetDTO {

    @XmlElement(name = RocksimCommonConstants.POINT_LIST)
    private String pointList = "";

    /**
     * Default constructor.
     */
    public CustomFinSetDTO() {
    }

    /**
     * Copy constructor of sorts.
     *
     * @param ec  a free form finset
     */
    public CustomFinSetDTO(FreeformFinSet ec) {
        super(ec);
        setPointList(convertFreeFormPoints(ec.getFinPoints()));
    }


    private String convertFreeFormPoints(Coordinate[] points) {
        StringBuilder sb = new StringBuilder();

        //Reverse the order for Rocksim
        for (int i = points.length - 1; i >= 0; i--) {
            Coordinate point = points[i];
            sb.append(point.x * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH).append(",")
                    .append(point.y * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH).append("|");
        }
        return sb.toString();
    }

    public String getPointList() {
        return pointList;
    }

    public void setPointList(String thePointList) {
        pointList = thePointList;
    }
}

