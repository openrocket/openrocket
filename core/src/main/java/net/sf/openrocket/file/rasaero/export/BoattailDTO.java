package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.file.rasaero.RASAeroCommonConstants;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.file.rasaero.export.RASAeroSaver.RASAeroExportException;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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
