package info.openrocket.core.file.rocksim.export;

import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.util.CoordinateIF;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 */
@JacksonXmlRootElement(localName = RockSimCommonConstants.CUSTOM_FIN_SET)
public class CustomFinSetDTO extends FinSetDTO {

    @JacksonXmlProperty(localName = RockSimCommonConstants.POINT_LIST)
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

    private String convertFreeFormPoints(CoordinateIF[] points) {
        StringBuilder sb = new StringBuilder();

        // Reverse the order for RockSim
        for (int i = points.length - 1; i >= 0; i--) {
            CoordinateIF point = points[i];
            sb.append(point.getX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH).append(",")
                    .append(point.getY() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH).append("|");
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
