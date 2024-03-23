package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.file.rasaero.export.RASAeroSaver.RASAeroExportException;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = RASAeroCommonConstants.BOATTAIL)
@XmlAccessorType(XmlAccessType.FIELD)
public class BoattailDTO extends TransitionDTO {
    /**
     * We need a default no-args constructor.
     */
    public BoattailDTO() {
        super();
    }

    public BoattailDTO(Transition boattail, WarningSet warnings, ErrorSet errors) throws RASAeroExportException {
        super(boattail, warnings, errors);

        setPartType(RASAeroCommonConstants.BOATTAIL);
    }
}
