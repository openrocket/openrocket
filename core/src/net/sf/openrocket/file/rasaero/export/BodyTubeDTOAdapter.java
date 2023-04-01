package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;

public interface BodyTubeDTOAdapter {
    default void applyBodyTubeSettings(BodyTube bodyTube, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        for (RocketComponent child : bodyTube.getChildren()) {
            if (child instanceof TrapezoidFinSet) {
                setFin(new FinDTO((TrapezoidFinSet) child, warnings, errors));
            } else if (child instanceof LaunchLug) {
                if (!MathUtil.equals(getRailGuideDiameter(), 0) || !MathUtil.equals(getRailGuideHeight(), 0)) {     // only one check on diameter or length should be sufficient, but just to be safe
                    warnings.add(String.format("Already added a rail button, ignoring launch lug '%s'.", child.getName()));
                    continue;
                }
                if (!MathUtil.equals(getLaunchShoeArea(), 0)) {
                    warnings.add(String.format("Already added a launch shoe, ignoring launch lug '%s'.", child.getName()));
                    continue;
                }

                LaunchLug lug = (LaunchLug) child;
                setLaunchLugDiameter(lug.getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                if (lug.getInstanceCount() == 2) {
                    setLaunchLugLength(lug.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                } else {
                    warnings.add(String.format(
                            "Instance count of '%s' not set to 2, defaulting to 2 and adjusting launch lug length accordingly.",
                            lug.getName()));
                    setLaunchLugLength(lug.getLength() * lug.getInstanceCount() / 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                }
            } else if (child instanceof RailButton) {
                if (!MathUtil.equals(getLaunchLugDiameter(), 0) || !MathUtil.equals(getLaunchLugLength(), 0)) {     // only one check on diameter or length should be sufficient, but just to be safe
                    warnings.add(String.format("Already added a launch lug, ignoring rail button '%s'.", child.getName()));
                    continue;
                }
                if (!MathUtil.equals(getLaunchShoeArea(), 0)) {
                    warnings.add(String.format("Already added a launch shoe, ignoring rail button '%s'.", child.getName()));
                    continue;
                }

                RailButton button = (RailButton) child;
                setRailGuideDiameter(button.getOuterDiameter() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                setRailGuideHeight(button.getTotalHeight() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                if (button.getInstanceCount() != 2) {
                    warnings.add(String.format("Instance count of '%s' equals %d, defaulting to 2.",
                            button.getName(), button.getInstanceCount()));
                }
            } else if (child instanceof Parachute) {
                // Do nothing, is handled by RecoveryDTO
            } else {
                warnings.add(String.format("Unsupported component '%s', ignoring.", child.getComponentName()));
            }
        }
    }

    Double getLaunchLugDiameter();

    void setLaunchLugDiameter(Double launchLugDiameter) throws RASAeroExportException;

    Double getLaunchLugLength();

    void setLaunchLugLength(Double launchLugLength) throws RASAeroExportException;

    Double getRailGuideDiameter();

    void setRailGuideDiameter(Double railGuideDiameter) throws RASAeroExportException;

    Double getRailGuideHeight();

    void setRailGuideHeight(Double railGuideHeight) throws RASAeroExportException;

    Double getLaunchShoeArea();

    void setLaunchShoeArea(Double launchShoeArea) throws RASAeroExportException;

    Double getBoattailLength();

    void setBoattailLength(Double boattailLength);

    Double getBoattailRearDiameter();

    void setBoattailRearDiameter(Double boattailRearDiameter);

    FinDTO getFin();

    void setFin(FinDTO fin);
}
