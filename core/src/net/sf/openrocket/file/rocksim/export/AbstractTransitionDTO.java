package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.file.rocksim.importt.RocksimNoseConeCode;
import net.sf.openrocket.rocketcomponent.Transition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AbstractTransitionDTO extends BasePartDTO {
    @XmlElement(name = "ShapeCode")
    private int shapeCode = 1;
    @XmlElement(name = "ConstructionType")
    private int constructionType = 1;
    @XmlElement(name = "WallThickness")
    private double wallThickness = 0d;
    @XmlElement(name = "ShapeParameter")
    private double shapeParameter = 0d;

    protected AbstractTransitionDTO() {

    }

    protected AbstractTransitionDTO(Transition nc) {
        super(nc);
        setConstructionType(nc.isFilled() ? 0 : 1);
        setShapeCode(RocksimNoseConeCode.toCode(nc.getType()));

        if (Transition.Shape.POWER.equals(nc.getType()) ||
                Transition.Shape.HAACK.equals(nc.getType()) ||
                Transition.Shape.PARABOLIC.equals(nc.getType())) {
            setShapeParameter(nc.getShapeParameter());
        }

        setWallThickness(nc.getThickness() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);

    }

    public int getShapeCode() {
        return shapeCode;
    }

    public void setShapeCode(int theShapeCode) {
        shapeCode = theShapeCode;
    }

    public int getConstructionType() {
        return constructionType;
    }

    public void setConstructionType(int theConstructionType) {
        constructionType = theConstructionType;
    }

    public double getWallThickness() {
        return wallThickness;
    }

    public void setWallThickness(double theWallThickness) {
        wallThickness = theWallThickness;
    }

    public double getShapeParameter() {
        return shapeParameter;
    }

    public void setShapeParameter(double theShapeParameter) {
        shapeParameter = theShapeParameter;
    }
}
