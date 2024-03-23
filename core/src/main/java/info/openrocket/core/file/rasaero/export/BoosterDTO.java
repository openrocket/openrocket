package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.CustomDoubleAdapter;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

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
    private Double launchShoeArea = 0d; // Currently not available in OR
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
    private Double nozzleExitDiameter = 0d;
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
    @XmlTransient
    private static final Translator trans = Application.getTranslator();

    /**
     * We need a default, no-args constructor.
     */
    public BoosterDTO() {
        this.component = null;
        this.warnings = null;
        this.errors = null;
    }

    protected BoosterDTO(Rocket rocket, AxialStage stage, WarningSet warnings, ErrorSet errors)
            throws RASAeroExportException {
        this.component = stage;
        this.warnings = warnings;
        this.errors = errors;

        int stageNr = rocket.getChildPosition(stage); // Use this instead of stage.getStageNumber() in case there are
                                                      // parallel stages in the design
        if (stageNr != 1 && stageNr != 2) {
            throw new RASAeroExportException(
                    String.format(trans.get("RASAeroExport.error9"), stageNr, stage.getName()));
        }

        if (stage.getChildCount() == 0) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error10"), stage.getName()));
        }

        RocketComponent firstChild = stage.getChild(0);
        if (!(firstChild instanceof BodyTube) &&
                !(firstChild instanceof Transition && !(firstChild instanceof NoseCone))) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error11"), stage.getName()));
        }

        final BodyTube firstTube;
        if (firstChild instanceof Transition) {
            if (stage.getChildCount() == 1 || !(stage.getChild(1) instanceof BodyTube)) {
                throw new RASAeroExportException(
                        String.format(trans.get("RASAeroExport.error12"), stage.getName()));
            }

            Transition transition = (Transition) firstChild;
            SymmetricComponent previousComponent = transition.getPreviousSymmetricComponent();
            if (previousComponent == null) {
                throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error13"),
                        firstChild.getName(), stage.getName()));
            }

            if (!MathUtil.equals(transition.getForeRadius(), previousComponent.getAftRadius())) {
                throw new RASAeroExportException(
                        String.format(trans.get("RASAeroExport.error14"),
                                transition.getName(), stage.getName(), previousComponent.getName()));
            }

            firstTube = (BodyTube) stage.getChild(1);
            if (!MathUtil.equals(firstTube.getOuterRadius(), transition.getAftRadius())) {
                throw new RASAeroExportException(
                        String.format(trans.get("RASAeroExport.error15"),
                                firstTube.getName(), stage.getName(), transition.getName()));
            }

            setShoulderLength(firstChild.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setDiameter(firstTube.getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
            setInsideDiameter(transition.getForeRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        } else {
            firstTube = (BodyTube) stage.getChild(0);
        }

        TrapezoidFinSet finSet = getFinSetFromBodyTube(firstTube);

        double tubeLength = firstTube.getLength();
        double finLocationOffset = 0;
        // Aggregate same-sized body tubes
        for (int i = stage.getChildPosition(firstTube) + 1; i < stage.getChildCount(); i++) {
            RocketComponent comp = stage.getChild(i);
            if (comp instanceof BodyTube &&
                    MathUtil.equals(((BodyTube) comp).getOuterRadius(), firstTube.getOuterRadius())) {
                // Aggregate the tubes by combining the lengths
                tubeLength += comp.getLength();

                // If no fin set in firstTube, add fin from new tube
                if (finSet == null) {
                    finSet = getFinSetFromBodyTube((BodyTube) comp);
                }
                // We need an offset to the fin location, since the fin axial offset is
                // referenced to its parent tube,
                // which can be different from the bottom of the aggregate tubes
                else {
                    finLocationOffset += comp.getLength();
                }
            } else {
                // If this booster is the last stage, and the last component is a transition, it
                // could be a boattail
                boolean isBoattail = (comp instanceof Transition && !(comp instanceof NoseCone))
                        && i == stage.getChildCount() - 1;
                if (stageNr == rocket.getChildCount() - 1 && isBoattail) {
                    Transition transition = (Transition) comp;
                    setBoattailLength(transition.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                    setBoattailRearDiameter(
                            transition.getAftRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                    break;
                }

                String msg = String.format(trans.get("RASAeroExport.error31"), stage.getName());

                if (isBoattail) {
                    msg = "<html>" + msg + "<br>&nbsp;" + trans.get("RASAeroExport.error32") + "</html>";
                }

                errors.add(msg);

                break;
            }
        }

        applyBodyTubeSettings(firstTube, warnings, errors);

        if (finSet == null) {
            throw new RASAeroExportException(
                    String.format(trans.get("RASAeroExport.error16"),
                            firstTube.getName(), stage.getName()));
        }
        FinDTO finDTO = new FinDTO(finSet, warnings, errors);
        double finLocation = finDTO.getLocation();
        finDTO.setLocation(finLocation + finLocationOffset * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setFin(finDTO);

        setPartType(RASAeroCommonConstants.BOOSTER);
        setLength(tubeLength * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setDiameter(firstTube.getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        setLocation(
                firstChild.getAxialOffset(AxialMethod.ABSOLUTE) * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
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
            errors.add(String.format(trans.get("RASAeroExport.error17"), component.getName()));
            return;
        }
        this.length = length;
    }

    public Double getDiameter() {
        return diameter;
    }

    public void setDiameter(Double diameter) throws RASAeroExportException {
        if (MathUtil.equals(diameter, 0)) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error18"), component.getName()));
        }
        this.diameter = diameter;
    }

    public Double getInsideDiameter() {
        return insideDiameter;
    }

    public void setInsideDiameter(Double insideDiameter) throws RASAeroExportException {
        if (MathUtil.equals(insideDiameter, 0)) {
            throw new RASAeroExportException(String.format(trans.get("RASAeroExport.error19"), component.getName()));
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

    public void setBoattailLength(Double boattailLength) throws RASAeroExportException {
        if (boattailLength == 0) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error29"));
        }
        this.boattailLength = boattailLength;
    }

    public Double getBoattailRearDiameter() {
        return boattailRearDiameter;
    }

    public void setBoattailRearDiameter(Double boattailRearDiameter) throws RASAeroExportException {
        if (boattailRearDiameter == 0) {
            throw new RASAeroExportException(trans.get("RASAeroExport.error30"));
        }
        this.boattailRearDiameter = boattailRearDiameter;
    }

    public FinDTO getFin() {
        return fin;
    }

    public void setFin(FinDTO fin) {
        this.fin = fin;
    }
}
