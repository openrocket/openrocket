package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;

import jakarta.xml.bind.annotation.XmlTransient;

public interface BodyTubeDTOAdapter {
    @XmlTransient
    Translator trans = Application.getTranslator();

    default void applyBodyTubeSettings(BodyTube bodyTube, WarningSet warnings, ErrorSet errors)
            throws RASAeroExportException {
        for (RocketComponent child : bodyTube.getChildren()) {
            if (child instanceof TrapezoidFinSet) {
                setFin(new FinDTO((TrapezoidFinSet) child, warnings, errors));
            } else if (child instanceof LaunchLug) {
                if (!MathUtil.equals(getRailGuideDiameter(), 0) || !MathUtil.equals(getRailGuideHeight(), 0)) { // only
                                                                                                                // one
                                                                                                                // check
                                                                                                                // on
                                                                                                                // diameter
                                                                                                                // or
                                                                                                                // length
                                                                                                                // should
                                                                                                                // be
                                                                                                                // sufficient,
                                                                                                                // but
                                                                                                                // just
                                                                                                                // to be
                                                                                                                // safe
                    warnings.add(String.format(trans.get("RASAeroExport.warning3"), child.getName()));
                    continue;
                }
                if (!MathUtil.equals(getLaunchShoeArea(), 0)) {
                    warnings.add(String.format(trans.get("RASAeroExport.warning4"), child.getName()));
                    continue;
                }

                LaunchLug lug = (LaunchLug) child;
                setLaunchLugDiameter(lug.getOuterRadius() * 2 * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                if (lug.getInstanceCount() == 2) {
                    setLaunchLugLength(lug.getLength() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                } else {
                    warnings.add(String.format(trans.get("RASAeroExport.warning5"), lug.getName()));
                    setLaunchLugLength(lug.getLength() * lug.getInstanceCount() / 2
                            * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                }
            } else if (child instanceof RailButton) {
                if (!MathUtil.equals(getLaunchLugDiameter(), 0) || !MathUtil.equals(getLaunchLugLength(), 0)) { // only
                                                                                                                // one
                                                                                                                // check
                                                                                                                // on
                                                                                                                // diameter
                                                                                                                // or
                                                                                                                // length
                                                                                                                // should
                                                                                                                // be
                                                                                                                // sufficient,
                                                                                                                // but
                                                                                                                // just
                                                                                                                // to be
                                                                                                                // safe
                    warnings.add(String.format(trans.get("RASAeroExport.warning6"), child.getName()));
                    continue;
                }
                if (!MathUtil.equals(getLaunchShoeArea(), 0)) {
                    warnings.add(String.format(trans.get("RASAeroExport.warning7"), child.getName()));
                    continue;
                }

                RailButton button = (RailButton) child;
                setRailGuideDiameter(button.getOuterDiameter() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
                setRailGuideHeight(button.getTotalHeight() * RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);

                if (button.getInstanceCount() != 2) {
                    warnings.add(String.format(trans.get("RASAeroExport.warning8"), button.getName(),
                            button.getInstanceCount()));
                }
            } else if (child instanceof Parachute) {
                // Do nothing, is handled by RecoveryDTO
            } else {
                warnings.add(String.format(trans.get("RASAeroExport.warning9"), child.getComponentName()));
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

    void setBoattailLength(Double boattailLength) throws RASAeroExportException;

    Double getBoattailRearDiameter();

    void setBoattailRearDiameter(Double boattailRearDiameter) throws RASAeroExportException;

    FinDTO getFin();

    void setFin(FinDTO fin);
}
