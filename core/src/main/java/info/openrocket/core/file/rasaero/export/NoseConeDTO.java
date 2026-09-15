package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.NoseCone;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import info.openrocket.core.file.rasaero.CustomDoubleAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants.NoseConeShapeSettings;

@JacksonXmlRootElement(localName = RASAeroCommonConstants.NOSE_CONE)
public class NoseConeDTO extends BasePartDTO {

    @JacksonXmlProperty(localName = RASAeroCommonConstants.SHAPE)
    private String shape;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.BLUNT_RADIUS)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double bluntRadius = 0.0d;
    @JacksonXmlProperty(localName = RASAeroCommonConstants.POWER_LAW)
    @JsonSerialize(using = CustomDoubleAdapter.Serializer.class)
    private Double powerLaw;

    /**
     * We need a default no-args constructor.
     */
    public NoseConeDTO() {
    }

    public NoseConeDTO(NoseCone noseCone, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(noseCone, warnings, errors);

        NoseConeShapeSettings shapeSettings = RASAeroCommonConstants
                .OPENROCKET_TO_RASAERO_SHAPE(noseCone.getShapeType(), noseCone.getShapeParameter());

        setShape(shapeSettings.getShape());
        Double shapeParameter = shapeSettings.getShapeParameter();
        if (shapeParameter != null) {
            setPowerLaw(shapeParameter);
        }
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public Double getPowerLaw() {
        return powerLaw;
    }

    public void setPowerLaw(Double powerLaw) {
        this.powerLaw = powerLaw;
    }

    public double getBluntRadius() {
        return bluntRadius;
    }

    public void setBluntRadius(double bluntRadius) {
        this.bluntRadius = bluntRadius;
    }
}
