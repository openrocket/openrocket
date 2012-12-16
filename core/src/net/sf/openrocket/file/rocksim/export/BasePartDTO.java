package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimDensityType;
import net.sf.openrocket.file.rocksim.RocksimFinishCode;
import net.sf.openrocket.file.rocksim.RocksimLocationMode;
import net.sf.openrocket.file.rocksim.importt.BaseHandler;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.StructuralComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The base class for all OpenRocket to Rocksim conversions.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BasePartDTO {

    /**
     * The very important Rocksim serial number.  Each component needs one.  This is not multi-thread safe.  Trying
     * to save multiple files at the same time will have unpredictable results with respect to the serial numbering.
     */
    private static int currentSerialNumber = 1;

    @XmlElement(name = RocksimCommonConstants.KNOWN_MASS)
    private double knownMass = 0d;
    @XmlElement(name = RocksimCommonConstants.DENSITY)
    private double density = 0d;
    @XmlElement(name = RocksimCommonConstants.MATERIAL)
    private String material = "";
    @XmlElement(name = RocksimCommonConstants.NAME)
    private String name = "";
    @XmlElement(name = RocksimCommonConstants.KNOWN_CG)
    private double knownCG = 0;
    @XmlElement(name = RocksimCommonConstants.USE_KNOWN_CG)
    private int useKnownCG = 1;
    @XmlElement(name = RocksimCommonConstants.XB)
    private double xb = 0;
    @XmlElement(name = RocksimCommonConstants.CALC_MASS)
    private double calcMass = 0d;
    @XmlElement(name = RocksimCommonConstants.CALC_CG)
    private double calcCG = 0d;
    @XmlElement(name = RocksimCommonConstants.DENSITY_TYPE)
    private int densityType = 0;
    @XmlElement(name = RocksimCommonConstants.RADIAL_LOC)
    private double radialLoc = 0;
    @XmlElement(name = RocksimCommonConstants.RADIAL_ANGLE)
    private double radialAngle = 0;
    @XmlElement(name = RocksimCommonConstants.LOCATION_MODE)
    private int locationMode = 0;
    @XmlElement(name = RocksimCommonConstants.LEN, required = false, nillable = false)
    private double len = 0d;
    @XmlElement(name = RocksimCommonConstants.FINISH_CODE)
    private int finishCode = 0;
    @XmlElement(name = RocksimCommonConstants.SERIAL_NUMBER)
    private int serialNumber = -1;

    /**
     * Default constructor.
     */
    protected BasePartDTO() {
        serialNumber = currentSerialNumber++;
    }

    /**
     * Copy constructor of sorts, that performs all common conversions for components.
     *
     * @param ec
     */
    protected BasePartDTO(RocketComponent ec) {
        serialNumber = currentSerialNumber++;
        setCalcCG(ec.getCG().x * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setCalcMass(ec.getComponentMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
        setKnownCG(ec.getOverrideCGX() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setKnownMass(ec.getMass() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);

        if (! (ec instanceof FinSet)) {
            setLen(ec.getLength() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        setUseKnownCG(ec.isCGOverridden() || ec.isMassOverridden() ? 1 : 0);
        setName(ec.getName());

        setXb(ec.getPositionValue() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        //When the relative position is BOTTOM, the position location of the bottom edge of the component is +
        //to the right of the bottom of the parent, and - to the left.
        //But in Rocksim, it's + to the left and - to the right
        if (ec.getRelativePosition().equals(RocketComponent.Position.BOTTOM)) {
            setXb((-1 * ec.getPositionValue()) * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        else if (ec.getRelativePosition().equals(RocketComponent.Position.MIDDLE)) {
            //Mapped to TOP, so adjust accordingly
            setXb((ec.getPositionValue() + (ec.getParent().getLength() - ec.getLength()) /2)* RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }

        if (ec instanceof ExternalComponent) {
            ExternalComponent comp = (ExternalComponent) ec;
            setLocationMode(RocksimLocationMode.toCode(comp.getRelativePosition()));

            setDensity(comp.getMaterial().getDensity() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
            setDensityType(RocksimDensityType.toCode(comp.getMaterial().getType()));
            String compMaterial = comp.getMaterial().getName();
            if (compMaterial.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                compMaterial = compMaterial.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(compMaterial);

            setFinishCode(RocksimFinishCode.toCode(comp.getFinish()));
        }
        else if (ec instanceof StructuralComponent) {
            StructuralComponent comp = (StructuralComponent) ec;

            setLocationMode(RocksimLocationMode.toCode(comp.getRelativePosition()));
            setDensity(comp.getMaterial().getDensity() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
            setDensityType(RocksimDensityType.toCode(comp.getMaterial().getType()));
            String compMaterial = comp.getMaterial().getName();
            if (compMaterial.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                compMaterial = compMaterial.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(compMaterial);
        }
        else if (ec instanceof RecoveryDevice) {
            RecoveryDevice comp = (RecoveryDevice) ec;

            setLocationMode(RocksimLocationMode.toCode(comp.getRelativePosition()));
            setDensity(comp.getMaterial().getDensity() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_SURFACE_DENSITY);
            setDensityType(RocksimDensityType.toCode(comp.getMaterial().getType()));
            String compMaterial = comp.getMaterial().getName();
            if (compMaterial.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                compMaterial = compMaterial.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(compMaterial);
        }

        if (ec instanceof RingComponent) {
            RingComponent rc = (RingComponent)ec;
            setRadialAngle(rc.getRadialDirection());
            setRadialLoc(rc.getRadialPosition() * RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
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

    public double getRadialLoc() {
        return radialLoc;
    }

    public void setRadialLoc(double theRadialLoc) {
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

    public static int getCurrentSerialNumber() {
        return currentSerialNumber - 1;
    }

    /**
     * Reset the serial number, which needs to happen after each file save.
     */
    public static void resetCurrentSerialNumber() {
        currentSerialNumber = 0;
    }
}
