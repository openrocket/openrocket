package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.CustomDoubleAdapter;
import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.util.MathUtil;

@XmlRootElement(name = RASAeroCommonConstants.BOOSTER)
@XmlAccessorType(XmlAccessType.FIELD)
public class BoosterDTO implements BodyTubeDTOAdapter {

    @XmlElement(name = RASAeroCommonConstants.PART_TYPE)
    private String partType;
    @XmlElement(name = RASAeroCommonConstants.LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double length;
    @XmlElement(name = RASAeroCommonConstants.DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double diameter;
    @XmlElement(name = RASAeroCommonConstants.INSIDE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double insideDiameter;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_LUG_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double launchLugDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_LUG_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double launchLugLength = 0d;
    @XmlElement(name = RASAeroCommonConstants.RAIL_GUIDE_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double railGuideDiameter = 0d;
    @XmlElement(name = RASAeroCommonConstants.RAIL_GUIDE_HEIGHT)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double railGuideHeight = 0d;
    @XmlElement(name = RASAeroCommonConstants.LAUNCH_SHOE_AREA)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double launchShoeArea = 0d;      // Currently not available in OR
    @XmlElement(name = RASAeroCommonConstants.LOCATION)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double location;
    @XmlElement(name = RASAeroCommonConstants.COLOR)
    private String color;
    @XmlElement(name = RASAeroCommonConstants.SHOULDER_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double shoulderLength;
    @XmlElement(name = RASAeroCommonConstants.NOZZLE_EXIT_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double nozzleExitDiameter;
    @XmlElement(name = RASAeroCommonConstants.BOATTAIL_LENGTH)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double boattailLength;
    @XmlElement(name = RASAeroCommonConstants.BOATTAIL_REAR_DIAMETER)
    @XmlJavaTypeAdapter(CustomDoubleAdapter.class)
    private Double boattailRearDiameter;

    @XmlElementRef(name = RASAeroCommonConstants.FIN, type = FinDTO.class)
    private FinDTO fin;


    @XmlTransient
    private final RocketComponent component;
    @XmlTransient
    private final WarningSet warnings;
    @XmlTransient
    private final ErrorSet errors;


    /**
     * We need a default, no-args constructor.
     */
    public BoosterDTO() {
        this.component = null;
        this.warnings = null;
        this.errors = null;
    }

    protected BoosterDTO(Rocket rocket, AxialStage stage, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        this.component = stage;
        this.warnings = warnings;
        this.errors = errors;

        int stageNr = rocket.getChildPosition(stage);       // Use this instead of stage.getStageNumber() in case there are parallel stages in the design
        if (stageNr != 1 && stageNr != 2) {
            throw new RASAeroExportException(String.format("Invalid stage number '%d' for booster stage '%s'.", stageNr, stage.getName()));
        }

        if (stage.getChildCount() == 0) {
            throw new RASAeroExportException(String.format("Stage '%s' may not be empty.", stage.getName()));
        }

        RocketComponent firstChild = stage.getChild(0);
        if (!(firstChild instanceof BodyTube) &&
                !(firstChild instanceof Transition && !(firstChild instanceof NoseCone))) {
            throw new RASAeroExportException(String.format("First component of stage '%s' must be a body tube or transition.", stage.getName()));
        }
        final BodyTube firstTube;
        if (firstChild instanceof Transition) {
            if (stage.getChildCount() == 1 || !(stage.getChild(1) instanceof BodyTube)) {
                throw new RASAeroExportException(
                        String.format("When the first component of stage '%s' is a transition, the second one must be a body tube.",
                                stage.getName()));
            }

            Transition transition = (Transition) firstChild;
            SymmetricComponent previousComponent = transition.getPreviousSymmetricComponent();
            if (previousComponent == null) {
                throw new RASAeroExportException(String.format("No previous component for '%s' in stage '%s'.",
                        firstChild.getName(), stage.getName()));
            }

            if (!MathUtil.equals(transition.getForeRadius(), previousComponent.getAftRadius())) {
                throw new RASAeroExportException(
                        String.format("Transition '%s' in stage '%s' must have the same fore radius as the aft radius of its previous component '%s'.",
                                transition.getName(), stage.getName(), previousComponent.getName()));
            }

            firstTube = (BodyTube) stage.getChild(1);
            if (!MathUtil.equals(firstTube.getOuterRadius(), transition.getAftRadius())) {
                throw new RASAeroExportException(
                        String.format("Radius of '%s' in stage '%s' must be the same as the aft radius of '%s'.",
                                firstTube.getName(), stage.getName(), transition.getName()));
            }

            setShoulderLength(firstChild.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setDiameter(firstTube.getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setInsideDiameter(transition.getForeRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

            if (stage.getChildCount() > 2) {
                warnings.add(String.format("Stage '%s' can only contain a body tube and transition shoulder, ignoring other %d components.",
                        stage.getName(), stage.getChildCount() - 2));
            }
        } else {
            firstTube = (BodyTube) stage.getChild(0);
            if (stage.getChildCount() > 1) {
                warnings.add(String.format("Stage '%s' can only contain a body tube, ignoring other %d components.",
                        stage.getName(), stage.getChildCount() - 1));
            }
        }

        applyBodyTubeSettings(firstTube, warnings, errors);

        TrapezoidFinSet finSet = getFinSetFromBodyTube(firstTube);
        if (finSet == null) {
            throw new RASAeroExportException(
                    String.format("Body tube '%s' in stage '%s' must have a TrapezoidFinSet.",
                            firstTube.getName(), stage.getName()));
        }
        setFin(new FinDTO(finSet, warnings, errors));

        setPartType(RASAeroCommonConstants.BOOSTER);
        setLength(firstTube.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setDiameter(firstTube.getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setLocation(firstChild.getAxialOffset(AxialMethod.ABSOLUTE) * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setColor(RASAeroCommonConstants.OPENROCKET_TO_RASAERO_COLOR(firstTube.getColor()));
    }

    private TrapezoidFinSet getFinSetFromBodyTube(BodyTube bodyTube) {
        for (RocketComponent child : bodyTube.getChildren()) {
            if (child instanceof TrapezoidFinSet) {
                return (TrapezoidFinSet) child;
            }
        }
        return null;
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        if (MathUtil.equals(length, 0)) {
            errors.add(String.format("Length of '%s' must be greater than 0.", component.getName()));
            return;
        }
        this.length = length;
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) throws RASAeroExportException {
        if (MathUtil.equals(diameter, 0)) {
            throw new RASAeroExportException(String.format("Diameter of '%s' must be greater than 0.", component.getName()));
        }
        this.diameter = diameter;
    }

    public Double getInsideDiameter() {
        return insideDiameter;
    }

    public void setInsideDiameter(Double insideDiameter) throws RASAeroExportException {
        if (MathUtil.equals(insideDiameter, 0)) {
            throw new RASAeroExportException(String.format("Inside diameter of '%s' must be greater than 0.", component.getName()));
        }
        this.insideDiameter = insideDiameter;
    }

    public Double getLaunchLugDiameter() {
        return launchLugDiameter;
    }

    public void setLaunchLugDiameter(Double launchLugDiameter) {
        this.launchLugDiameter = launchLugDiameter;
    }

    public Double getLaunchLugLength() {
        return launchLugLength;
    }

    public void setLaunchLugLength(Double launchLugLength) {
        this.launchLugLength = launchLugLength;
    }

    public Double getRailGuideDiameter() {
        return railGuideDiameter;
    }

    public void setRailGuideDiameter(Double railGuideDiameter) {
        this.railGuideDiameter = railGuideDiameter;
    }

    public Double getRailGuideHeight() {
        return railGuideHeight;
    }

    public void setRailGuideHeight(Double railGuideHeight) {
        this.railGuideHeight = railGuideHeight;
    }

    public Double getLaunchShoeArea() {
        return launchShoeArea;
    }

    public void setLaunchShoeArea(Double launchShoeArea) {
        this.launchShoeArea = launchShoeArea;
    }

    public Double getLocation() {
        return location;
    }

    public void setLocation(Double location) {
        this.location = location;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getShoulderLength() {
        return shoulderLength;
    }

    public void setShoulderLength(Double shoulderLength) {
        this.shoulderLength = shoulderLength;
    }

    public Double getNozzleExitDiameter() {
        return nozzleExitDiameter;
    }

    public void setNozzleExitDiameter(Double nozzleExitDiameter) {
        this.nozzleExitDiameter = nozzleExitDiameter;
    }

    public Double getBoattailLength() {
        return boattailLength;
    }

    public void setBoattailLength(Double boattailLength) {
        this.boattailLength = boattailLength;
    }

    public Double getBoattailRearDiameter() {
        return boattailRearDiameter;
    }

    public void setBoattailRearDiameter(Double boattailRearDiameter) {
        this.boattailRearDiameter = boattailRearDiameter;
    }

    public FinDTO getFin() {
        return fin;
    }

    public void setFin(FinDTO fin) {
        this.fin = fin;
    }
}
