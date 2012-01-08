package net.sf.openrocket.file.rocksim.export;

import net.sf.openrocket.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import java.util.List;

/**
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class StageDTO {

    @XmlElementRefs({
            @XmlElementRef(name = "BodyTube", type = BodyTubeDTO.class),
            @XmlElementRef(name = "NoseCone", type = NoseConeDTO.class),
            @XmlElementRef(name = "Transition", type = TransitionDTO.class)
    })
    private List<BasePartDTO> externalPart = new ArrayList<BasePartDTO>();

    public StageDTO() {
    }

    public List<BasePartDTO> getExternalPart() {
        return externalPart;
    }

    public void addExternalPart(BasePartDTO theExternalPartDTO) {
        externalPart.add(theExternalPartDTO);
    }
}
