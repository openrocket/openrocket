package net.sf.openrocket.file.rasaero.export;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.rocketcomponent.Rocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class RASAeroSaver extends RocketSaver {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RASAeroSaver.class);

    public static class RASAeroExportException extends Exception {
        public RASAeroExportException(String errorMessage) {
            super(errorMessage);
        }
    }

    /**
     * This method marshals an OpenRocketDocument (OR design) to RASAero-compliant XML.
     *
     * @param doc the OR design
     * @return RASAero-compliant XML
     */
    public String marshalToRASAero(OpenRocketDocument doc) {
        try {
            JAXBContext binder = JAXBContext.newInstance(RASAeroDocumentDTO.class);
            Marshaller marshaller = binder.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();

            marshaller.marshal(toRASAeroDocumentDTO(doc), sw);
            return sw.toString();
        } catch (RASAeroExportException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Could not marshall a design to RASAero format. " + e.getMessage());
        }

        return null;
    }

    @Override
    public void save(OutputStream dest, OpenRocketDocument doc, StorageOptions options) throws IOException {
        log.info("Saving .CDX1 file");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, StandardCharsets.UTF_8));
        writer.write(marshalToRASAero(doc));
        writer.flush();
    }

    @Override
    public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
        return marshalToRASAero(doc).length();
    }

    /**
     * Root conversion method.  It iterates over all subcomponents.
     *
     * @param doc the OR design
     * @return a corresponding RASAero representation
     */
    private RASAeroDocumentDTO toRASAeroDocumentDTO(OpenRocketDocument doc) throws RASAeroExportException {
        RASAeroDocumentDTO rad = new RASAeroDocumentDTO();
        rad.setDesign(toRocketDesignDTO(doc.getRocket()));

        return rad;
    }

    /**
     * Create the RASAero rocket design (containing all the actual rocket components).
     * @param rocket the OR rocket to export the components from
     * @return the RASAero rocket design
     */
    private RocketDesignDTO toRocketDesignDTO(Rocket rocket) throws RASAeroExportException {
        RocketDesignDTO result = new RocketDesignDTO(rocket);
        return result;
    }
}
