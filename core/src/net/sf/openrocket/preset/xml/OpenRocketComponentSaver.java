package net.sf.openrocket.preset.xml;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The active manager class that is the entry point for reading and writing *.orc files.
 */
public class OpenRocketComponentSaver {

	private static final Logger log = LoggerFactory.getLogger(OpenRocketComponentSaver.class);
    /**
     * The JAXBContext.  JAXBContext is thread-safe.
     */
    private static JAXBContext context = null;

    static {
        try {
            context = JAXBContext.newInstance(OpenRocketComponentDTO.class);
        }
        catch (JAXBException jaxb) {
           log.error("Unable to create JAXBContext for loading of *.orc files.", jaxb);
        }
    }

    public boolean save(File file, List<Material> theMaterialList, List<ComponentPreset> thePresetList) throws
                                                                                                     JAXBException,
                                                                                                     IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        writer.write(marshalToOpenRocketComponent(theMaterialList, thePresetList));
        writer.flush();
        writer.close();
        return true;
    }

    /**
     * This method marshals a list of materials and ComponentPresets into an .orc formatted XML string.
     *
     * @param theMaterialList the list of materials to be included
     * @param thePresetList   the list of presets to be included
     *
     * @return ORC-compliant XML
     *
     * @throws JAXBException
     */
    public String marshalToOpenRocketComponent(List<Material> theMaterialList, List<ComponentPreset> thePresetList) throws
                                                                                                                    JAXBException {
        /** The context is thread-safe, but marshallers are not.  Create a local one. */
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter sw = new StringWriter();

        // We're going to sort the initial data since that makes the output much easier on the eyes.

        Collections.sort(theMaterialList, new Comparator<Material>() {

			@Override
			public int compare(Material o1, Material o2) {
				return o1.getName().compareTo( o2.getName() );
			}

        });

        Collections.sort(thePresetList, new Comparator<ComponentPreset>() {

			@Override
			public int compare(ComponentPreset o1, ComponentPreset o2) {
				int manucmp = o1.getManufacturer().getSimpleName().compareTo( o2.getManufacturer().getSimpleName() );

				if ( manucmp != 0 ) {
					return manucmp;
				}

				return o1.getPartNo().compareTo( o2.getPartNo());
			}

        });

        marshaller.marshal(toOpenRocketComponentDTO(theMaterialList, thePresetList), sw);
        return sw.toString();

    }

    /**
     * This method unmarshals from a Reader that is presumed to be open on an XML file in .orc format.
     *
     * @param is an open reader; StringBufferInputStream could not be used because it's deprecated and does not handle
     *           UTF characters correctly
     *
     * @return a list of ComponentPresets
     *
     * @throws InvalidComponentPresetException
     *
     */
    public OpenRocketComponentDTO unmarshalFromOpenRocketComponent(Reader is) throws JAXBException,
                                                                                    InvalidComponentPresetException {
        return fromOpenRocketComponent(is);
    }

    /**
     * Write an XML representation of a list of presets.
     *
     * @param dest            the stream to write the data to
     * @param theMaterialList the list of materials to be included
     * @param thePresetList   the list of presets to be included
     *
     * @throws JAXBException
     * @throws IOException   thrown if the stream could not be written
     */
    public void save(OutputStream dest, List<Material> theMaterialList, List<ComponentPreset> thePresetList) throws
                                                                                                             IOException,
                                                                                                             JAXBException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, "UTF-8"));
        writer.write(marshalToOpenRocketComponent(theMaterialList, thePresetList));
        writer.flush();
        writer.close();
    }

    /**
     * Read from an open Reader instance XML in .orc format and reconstruct an OpenRocketComponentDTO instance.
     *
     * @param is an open Reader; assumed to be opened on a file of XML in .orc format
     *
     * @return the OpenRocketComponentDTO that is a POJO representation of the XML; null if the data could not be read
     *         or was in an invalid format
     */
    private OpenRocketComponentDTO fromOpenRocketComponent(Reader is) throws JAXBException {
        /** The context is thread-safe, but unmarshallers are not.  Create a local one. */
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (OpenRocketComponentDTO) unmarshaller.unmarshal(is); //new StreamSource(is));
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
     * @param thePreset the preset for which a handler will be found
     *
     * @return a subclass of BaseComponentDTO that can be used for marshalling/unmarshalling a preset; null if not found
     *         for the preset type
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
            case LAUNCH_LUG:
                return new LaunchLugDTO(thePreset);
            case STREAMER:
                return new StreamerDTO(thePreset);
            case PARACHUTE:
                return new ParachuteDTO(thePreset);
        }

        return null;
    }
}
