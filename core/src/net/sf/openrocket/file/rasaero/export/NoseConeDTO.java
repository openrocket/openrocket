package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.CustomDoubleAdapter;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.NoseCone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants.NoseConeShapeSettings;

@XmlRootElement(name = RASAeroCommonConstants.NOSE_CONE)
@XmlAccessorType(XmlAccessType.FIELD)
public class NoseConeDTO extends BasePartDTO {

    @XmlElement(name = RASAeroCommonConstants.SHAPE)
    private String shape;
    @XmlElement(name = RASAeroCommonConstants.BLUNT_RADIUS)
    private double bluntRadius = 0;
    @XmlElement(name = RASAeroCommonConstants.POWER_LAW)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double powerLaw;

    /**
     * We need a default no-args constructor.
     */
    public NoseConeDTO() {
    }

    public NoseConeDTO(NoseCone noseCone, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(noseCone, warnings, errors);

        NoseConeShapeSettings shapeSettings =
                RASAeroCommonConstants.OPENROCKET_TO_RASAERO_SHAPE(noseCone.getShapeType(), noseCone.getShapeParameter());

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
