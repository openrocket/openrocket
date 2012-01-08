package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.importt.BaseHandler;
import net.sf.openrocket.file.rocksim.importt.RocksimDensityType;
import net.sf.openrocket.file.rocksim.importt.RocksimFinishCode;
import net.sf.openrocket.file.rocksim.importt.RocksimHandler;
import net.sf.openrocket.file.rocksim.importt.RocksimLocationMode;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.StructuralComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BasePartDTO {

    @XmlElement(name = "KnownMass")
    private Double knownMass = 0d;
    @XmlElement(name = "Density")
    private double density = 0d;
    @XmlElement(name = "Material")
    private String material = "";
    @XmlElement(name = "Name")
    private String name = "";
    @XmlElement(name = "KnownCG")
    private Double knownCG = null;
    @XmlElement(name = "UseKnownCG")
    private int useKnownCG = 1;
    @XmlElement(name = "Xb")
    private double xb = 0;
    @XmlElement(name = "CalcMass")
    private double calcMass = 0d;
    @XmlElement(name = "CalcCG")
    private double calcCG = 0d;
    @XmlElement(name = "DensityType")
    private int densityType = 0;
    @XmlElement(name = "RadialLoc")
    private String radialLoc = "0.";
    @XmlElement(name = "RadialAngle")
    private double radialAngle = 0;
    @XmlElement(name = "LocationMode")
    private int locationMode = 0;
    @XmlElement(name = "Len")
    private double len = 0d;
    @XmlElement(name = "FinishCode")
    private int finishCode = 0;

    protected BasePartDTO() {
    }

    protected BasePartDTO(RocketComponent ec) {
        setCalcCG(ec.getCG().x * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setCalcMass(ec.getComponentMass() * RocksimHandler.ROCKSIM_TO_OPENROCKET_MASS);
        setKnownCG(ec.getOverrideCGX() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setKnownMass(ec.getOverrideMass() * RocksimHandler.ROCKSIM_TO_OPENROCKET_MASS);
        setLen(ec.getLength() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        setUseKnownCG(ec.isCGOverridden() || ec.isMassOverridden() ? 1 : 0);
        setName(ec.getName());

        setXb(ec.getPositionValue() * RocksimHandler.ROCKSIM_TO_OPENROCKET_LENGTH);
        if (ec instanceof ExternalComponent) {
            ExternalComponent comp = (ExternalComponent) ec;
            setLocationMode(RocksimLocationMode.toCode(comp.getRelativePosition()));

            if (comp.getRelativePosition().equals(RocketComponent.Position.BOTTOM)) {
                setXb(-1 * getXb());
            }
            setDensity(comp.getMaterial().getDensity() * RocksimHandler.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
            setDensityType(RocksimDensityType.toCode(comp.getMaterial().getType()));
            String material = comp.getMaterial().getName();
            if (material.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                material = material.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(material);

            setFinishCode(RocksimFinishCode.toCode(comp.getFinish()));
        }
        else if (ec instanceof StructuralComponent) {
            StructuralComponent comp = (StructuralComponent) ec;

            setLocationMode(RocksimLocationMode.toCode(comp.getRelativePosition()));
            if (comp.getRelativePosition().equals(RocketComponent.Position.BOTTOM)) {
                setXb(-1 * getXb());
            }
            setDensity(comp.getMaterial().getDensity() * RocksimHandler.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
            setDensityType(RocksimDensityType.toCode(comp.getMaterial().getType()));
            String material = comp.getMaterial().getName();
            if (material.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                material = material.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(material);
        }
        else if (ec instanceof RecoveryDevice) {
            RecoveryDevice comp = (RecoveryDevice) ec;

            setLocationMode(RocksimLocationMode.toCode(comp.getRelativePosition()));
            if (comp.getRelativePosition().equals(RocketComponent.Position.BOTTOM)) {
                setXb(-1 * getXb());
            }
            setDensity(comp.getMaterial().getDensity() * RocksimHandler.ROCKSIM_TO_OPENROCKET_SURFACE_DENSITY);
            setDensityType(RocksimDensityType.toCode(comp.getMaterial().getType()));
            String material = comp.getMaterial().getName();
            if (material.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                material = material.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(material);

        }
    }

    public Double getKnownMass() {
        return knownMass;
    }

    public void setKnownMass(Double theKnownMass) {
        knownMass = theKnownMass;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double theDensity) {
        density = theDensity;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String theMaterial) {
        material = theMaterial;
    }

    public String getName() {
        return name;
    }

    public void setName(String theName) {
        name = theName;
    }

    public Double getKnownCG() {
        return knownCG;
    }

    public void setKnownCG(Double theKnownCG) {
        knownCG = theKnownCG;
    }

    public int getUseKnownCG() {
        return useKnownCG;
    }

    public void setUseKnownCG(int theUseKnownCG) {
        useKnownCG = theUseKnownCG;
    }

    public double getXb() {
        return xb;
    }

    public void setXb(double theXb) {
        xb = theXb;
    }

    public double getCalcMass() {
        return calcMass;
    }

    public void setCalcMass(double theCalcMass) {
        calcMass = theCalcMass;
    }

    public double getCalcCG() {
        return calcCG;
    }

    public void setCalcCG(double theCalcCG) {
        calcCG = theCalcCG;
    }

    public int getDensityType() {
        return densityType;
    }

    public void setDensityType(int theDensityType) {
        densityType = theDensityType;
    }

    public String getRadialLoc() {
        return radialLoc;
    }

    public void setRadialLoc(String theRadialLoc) {
        radialLoc = theRadialLoc;
    }

    public double getRadialAngle() {
        return radialAngle;
    }

    public void setRadialAngle(double theRadialAngle) {
        radialAngle = theRadialAngle;
    }

    public int getLocationMode() {
        return locationMode;
    }

    public void setLocationMode(int theLocationMode) {
        locationMode = theLocationMode;
    }

    public double getLen() {
        return len;
    }

    public void setLen(double theLen) {
        len = theLen;
    }

    public int getFinishCode() {
        return finishCode;
    }

    public void setFinishCode(int theFinishCode) {
        finishCode = theFinishCode;
    }

}
