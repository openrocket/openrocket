package net.sf.openrocket.preset.xml;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.startup.Application;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * The active manager class that is the entry point for writing *.orc files.
 */
public class OpenRocketComponentSaver {

    /**
     * The logger.
     */
    private static final LogHelper log = Application.getLogger();

    /**
     * This method marshals an OpenRocketDocument (OR design) to Rocksim-compliant XML.
     *
     * @param theMaterialList the list of materials to be included
     * @param thePresetList   the list of presets to be included
     *
     * @return ORC-compliant XML
     */
    public String marshalToOpenRocketComponent(List<Material> theMaterialList, List<ComponentPreset> thePresetList) {

        try {
            JAXBContext binder = JAXBContext.newInstance(OpenRocketComponentDTO.class);
            Marshaller marshaller = binder.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();

            marshaller.marshal(toOpenRocketComponentDTO(theMaterialList, thePresetList), sw);
            return sw.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Write an XML representation of a list of presets.
     *
     * @param dest  the stream to write the data to
     * @param theMaterialList the list of materials to be included
     * @param thePresetList   the list of presets to be included
     *
     * @throws IOException thrown if the stream could not be written
     */
    public void save(OutputStream dest, List<Material> theMaterialList, List<ComponentPreset> thePresetList) throws IOException {
        log.info("Saving .orc file");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, "UTF-8"));
        writer.write(marshalToOpenRocketComponent(theMaterialList, thePresetList));
        writer.flush();
        writer.close();
    }

    /**
     * Root conversion method.  It iterates over all subcomponents.
     *
     * @return a corresponding ORC representation
     */
    private OpenRocketComponentDTO toOpenRocketComponentDTO(List<Material> theMaterialList, List<ComponentPreset> thePresetList) {
        OpenRocketComponentDTO rsd = new OpenRocketComponentDTO();

        if (theMaterialList != null) {
            for (Material material : theMaterialList) {
                rsd.addMaterial(new MaterialDTO(material));
            }
        }

        if (thePresetList != null) {
            for (ComponentPreset componentPreset : thePresetList) {
                rsd.addComponent(toComponentDTO(componentPreset));
            }
        }
        return rsd;
    }

    /**
     * Factory method that maps a preset to the corresponding DTO handler.
     *
     * @param thePreset  the preset for which a handler will be found
     *
     * @return a subclass of BaseComponentDTO that can be used for marshalling/unmarshalling a preset; null if not found
     * for the preset type
     */
    private static BaseComponentDTO toComponentDTO(ComponentPreset thePreset) {
        switch (thePreset.getType()) {
            case BODY_TUBE:
                return new BodyTubeDTO(thePreset);
            case TUBE_COUPLER:
                return new TubeCouplerDTO(thePreset);
            case NOSE_CONE:
                return new NoseConeDTO(thePreset);
            case TRANSITION:
                return new TransitionDTO(thePreset);
            case BULK_HEAD:
                return new BulkHeadDTO(thePreset);
            case CENTERING_RING:
                return new CenteringRingDTO(thePreset);
            case ENGINE_BLOCK:
                return new EngineBlockDTO(thePreset);
        }

        return null;
    }
}
