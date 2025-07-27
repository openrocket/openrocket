package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.util.Coordinate;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement(name = RockSimCommonConstants.CUSTOM_FIN_SET)
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomFinSetDTO extends FinSetDTO {

    @XmlElement(name = RockSimCommonConstants.POINT_LIST)
    private String pointList = "";

    /**
     * Default constructor.
     */
    public CustomFinSetDTO() {
    }

    /**
     * Copy constructor of sorts.
     *
     * @param ec a free form finset
     */
    public CustomFinSetDTO(FreeformFinSet ec) {
        super(ec);
        setPointList(convertFreeFormPoints(ec.getFinPoints()));
    }

    private String convertFreeFormPoints(Coordinate[] points) {
        StringBuilder sb = new StringBuilder();

        // Reverse the order for RockSim
        for (int i = points.length - 1; i >= 0; i--) {
            Coordinate point = points[i];
            sb.append(point.x * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH).append(",")
                    .append(point.y * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH).append("|");
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
