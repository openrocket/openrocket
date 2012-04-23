package net.sf.openrocket.preset.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The real 'root' element in an XML document.
 */
@XmlRootElement(name = "OpenRocketComponent")
@XmlAccessorType(XmlAccessType.FIELD)
public class OpenRocketComponentDTO {

    @XmlElement(name = "Version")
    private final String version = "0.1";

    @XmlElementWrapper(name = "Materials")
            @XmlElement(name = "Material")
    List<MaterialDTO> materials = new ArrayList<MaterialDTO>();

    @XmlElementWrapper(name = "Components")
    @XmlElementRefs({
            @XmlElementRef(name = "BodyTubes", type = BodyTubeDTO.class),
            @XmlElementRef(name = "TubeCouplers", type = TubeCouplerDTO.class),
            @XmlElementRef(name = "NoseCones", type = NoseConeDTO.class),
            @XmlElementRef(name = "Transitions", type = TransitionDTO.class),
            @XmlElementRef(name = "BulkHeads", type = BulkHeadDTO.class),
            @XmlElementRef(name = "CenteringRings", type = CenteringRingDTO.class),
            @XmlElementRef(name = "EngineBlocks", type = EngineBlockDTO.class)})
    private List<BaseComponentDTO> components = new ArrayList<BaseComponentDTO>();

    public OpenRocketComponentDTO() {
    }

    public OpenRocketComponentDTO(final List<MaterialDTO> theMaterials, final List<BaseComponentDTO> theComponents) {
        materials = theMaterials;
        components = theComponents;
    }

    public List<MaterialDTO> getMaterials() {
        return materials;
    }

    public void addMaterial(final MaterialDTO theMaterial) {
        materials.add(theMaterial);
    }

    public void setMaterials(final List<MaterialDTO> theMaterials) {
        materials = theMaterials;
    }

    public List<BaseComponentDTO> getComponents() {
        return components;
    }

    public void addComponent(final BaseComponentDTO theComponent) {
        components.add(theComponent);
    }

    public void setComponents(final List<BaseComponentDTO> theComponents) {
        components = theComponents;
    }
}
