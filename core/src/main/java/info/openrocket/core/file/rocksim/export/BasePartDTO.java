package info.openrocket.core.file.rocksim.export;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.rocksim.RockSimDensityType;
import info.openrocket.core.file.rocksim.RockSimFinishCode;
import info.openrocket.core.file.rocksim.RockSimLocationMode;
import info.openrocket.core.file.rocksim.importt.BaseHandler;
import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InternalComponent;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.StructuralComponent;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.ORColor;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The base class for all OpenRocket to RockSim conversions.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BasePartDTO {

    /**
     * The very important RockSim serial number. Each component needs one. This is
     * not multi-thread safe. Trying
     * to save multiple files at the same time will have unpredictable results with
     * respect to the serial numbering.
     */
    private static int currentSerialNumber = 1;

    @XmlElement(name = RockSimCommonConstants.KNOWN_MASS)
    private double knownMass = 0.0d;
    @XmlElement(name = RockSimCommonConstants.DENSITY)
    private double density = 0.0d;
    @XmlElement(name = RockSimCommonConstants.MATERIAL)
    private String material = "";
    @XmlElement(name = RockSimCommonConstants.NAME)
    private String name = "";
    @XmlElement(name = RockSimCommonConstants.KNOWN_CG)
    private double knownCG = 0;
    @XmlElement(name = RockSimCommonConstants.USE_KNOWN_CG)
    private int useKnownCG = 1;
    @XmlElement(name = RockSimCommonConstants.XB)
    private double xb = 0;
    @XmlElement(name = RockSimCommonConstants.CALC_MASS)
    private double calcMass = 0.0d;
    @XmlElement(name = RockSimCommonConstants.CALC_CG)
    private double calcCG = 0.0d;
    @XmlElement(name = RockSimCommonConstants.DENSITY_TYPE)
    private int densityType = 0;
    @XmlElement(name = RockSimCommonConstants.RADIAL_LOC)
    private double radialLoc = 0;
    @XmlElement(name = RockSimCommonConstants.RADIAL_ANGLE)
    private double radialAngle = 0;
    @XmlElement(name = RockSimCommonConstants.OPACITY)
    private double opacity = 1;
    @XmlElement(name = RockSimCommonConstants.DIFFUSE_COLOR)
    private String diffuseColor;
    @XmlElement(name = RockSimCommonConstants.AMBIENT_COLOR)
    private String ambientColor;
    @XmlElement(name = RockSimCommonConstants.USE_SINGLE_COLOR)
    private int useSingleColor = 1;
    @XmlElement(name = RockSimCommonConstants.LOCATION_MODE)
    private int locationMode = 0;
    @XmlElement(name = RockSimCommonConstants.LEN, required = false, nillable = false)
    private double len = 0.0d;
    @XmlElement(name = RockSimCommonConstants.FINISH_CODE)
    private int finishCode = 0;
    @XmlElement(name = RockSimCommonConstants.SERIAL_NUMBER)
    private int serialNumber = -1;
    @XmlElement(name = RockSimCommonConstants.COLOR)
    private String color;

    private static final Map<Class<? extends RocketComponent>, ORColor> DEFAULT_FIGURE_COLORS = new LinkedHashMap<>();

    static {
        DEFAULT_FIGURE_COLORS.put(BodyComponent.class, parseDefaultColor("0,0,240"));
        DEFAULT_FIGURE_COLORS.put(TubeFinSet.class, parseDefaultColor("0,0,200"));
        DEFAULT_FIGURE_COLORS.put(FinSet.class, parseDefaultColor("0,0,200"));
        DEFAULT_FIGURE_COLORS.put(LaunchLug.class, parseDefaultColor("0,0,180"));
        DEFAULT_FIGURE_COLORS.put(RailButton.class, parseDefaultColor("0,0,180"));
        DEFAULT_FIGURE_COLORS.put(InternalComponent.class, parseDefaultColor("170,0,100"));
        DEFAULT_FIGURE_COLORS.put(MassObject.class, parseDefaultColor("0,0,0"));
        DEFAULT_FIGURE_COLORS.put(RecoveryDevice.class, parseDefaultColor("255,0,0"));
        DEFAULT_FIGURE_COLORS.put(PodSet.class, parseDefaultColor("160,160,215"));
        DEFAULT_FIGURE_COLORS.put(ParallelStage.class, parseDefaultColor("198,163,184"));
    }

    /**
     * Default constructor.
     */
    protected BasePartDTO() {
        serialNumber = currentSerialNumber++;
    }

    /**
     * Copy constructor of sorts, that performs all common conversions for
     * components.
     *
     * @param ec
     */
    protected BasePartDTO(RocketComponent ec) {
        serialNumber = currentSerialNumber++;
        setCalcCG(ec.getCG().getX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setCalcMass(ec.getComponentMass() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
        setKnownCG(ec.getOverrideCGX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        setKnownMass(ec.getMass() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);

        if (!(ec instanceof FinSet)) {
            setLen(ec.getLength() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }
        setUseKnownCG(ec.isCGOverridden() || ec.isMassOverridden() ? 1 : 0);
        setName(ec.getName());

        setXb(ec.getAxialOffset() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);

        RocketComponent parent = ec.getParent();
        // When the relative position is BOTTOM, the position location of the bottom
        // edge of the component is +
        // to the right of the bottom of the parent, and - to the left.
        // But in RockSim, it's + to the left and - to the right
        if (ec.getAxialMethod().equals(AxialMethod.BOTTOM)) {
            setXb((-1 * ec.getAxialOffset()) * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        } else if (ec.getAxialMethod().equals(AxialMethod.MIDDLE)) {
            if (parent != null) {
                setXb((ec.getAxialOffset() + (parent.getLength() - ec.getLength()) / 2)
                        * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            } else {
                // Detached components (e.g., clustered clones) keep a valid position; use it to map MIDDLE to TOP.
                setXb(ec.getPosition().getX() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
            }
        }

        if (ec instanceof ExternalComponent) {
            ExternalComponent comp = (ExternalComponent) ec;
            setLocationMode(RockSimLocationMode.toCode(comp.getAxialMethod()));

            setDensity(comp.getMaterial().getDensity() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
            setDensityType(RockSimDensityType.toCode(comp.getMaterial().getType()));
            String compMaterial = comp.getMaterial().getName();
            if (compMaterial.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                compMaterial = compMaterial.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(compMaterial);

            setFinishCode(RockSimFinishCode.toCode(comp.getFinish()));
        } else if (ec instanceof StructuralComponent) {
            StructuralComponent comp = (StructuralComponent) ec;

            setLocationMode(RockSimLocationMode.toCode(comp.getAxialMethod()));
            setDensity(comp.getMaterial().getDensity() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
            setDensityType(RockSimDensityType.toCode(comp.getMaterial().getType()));
            String compMaterial = comp.getMaterial().getName();
            if (compMaterial.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                compMaterial = compMaterial.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(compMaterial);
        } else if (ec instanceof RecoveryDevice) {
            RecoveryDevice comp = (RecoveryDevice) ec;

            setLocationMode(RockSimLocationMode.toCode(comp.getAxialMethod()));
            setDensity(comp.getMaterial().getDensity() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_SURFACE_DENSITY);
            setDensityType(RockSimDensityType.toCode(comp.getMaterial().getType()));
            String compMaterial = comp.getMaterial().getName();
            if (compMaterial.startsWith(BaseHandler.ROCKSIM_MATERIAL_PREFIX)) {
                compMaterial = compMaterial.substring(BaseHandler.ROCKSIM_MATERIAL_PREFIX.length());
            }
            setMaterial(compMaterial);
        }

        if (ec instanceof RingComponent) {
            RingComponent rc = (RingComponent) ec;
            setRadialAngle(rc.getRadialDirection());
            setRadialLoc(rc.getRadialPosition() * RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
        }

        ORColor figureColor = ec.getColor();
        if (figureColor == null) {
            figureColor = getDefaultFigureColor(ec);
        }
        if (figureColor != null) {
            setColor(formatRgb(figureColor));
        }

        Appearance appearance = ec.getAppearance();
        ORColor appearanceColor = null;
        if (appearance != null) {
            appearanceColor = appearance.getPaint();
        }
        if (appearanceColor != null) {
            setDiffuseColor(formatRgb(appearanceColor));
            setAmbientColor(formatRgb(appearanceColor));
            setOpacity(appearanceColor.getAlpha() / 255.0);
        } else if (figureColor != null) {
            setDiffuseColor(formatRgb(figureColor));
            setOpacity(figureColor.getAlpha() / 255.0);
        } else {
            setOpacity(1.0);
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

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double theOpacity) {
        opacity = theOpacity;
    }

    public String getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(String theDiffuseColor) {
        diffuseColor = theDiffuseColor;
    }

    public String getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(String theAmbientColor) {
        ambientColor = theAmbientColor;
    }

    public int getUseSingleColor() {
        return useSingleColor;
    }

    public void setUseSingleColor(int theUseSingleColor) {
        useSingleColor = theUseSingleColor;
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

    public String getColor() {
        return color;
    }

    public void setColor(String theColor) {
        color = theColor;
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

    private static String formatRgb(ORColor color) {
        return String.format(Locale.US, "rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
    }

    private static ORColor getDefaultFigureColor(RocketComponent component) {
        for (Map.Entry<Class<? extends RocketComponent>, ORColor> entry : DEFAULT_FIGURE_COLORS.entrySet()) {
            if (entry.getKey().isInstance(component)) {
                return entry.getValue();
            }
        }
        return ORColor.BLACK;
    }

    private static ORColor parseDefaultColor(String rgb) {
        String[] parts = rgb.split(",");
        return new ORColor(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}
