package info.openrocket.core.file.rasaero.export;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.RocketSaver;
import info.openrocket.core.logging.ErrorSet;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.Rocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

/**
 * This class is responsible for marshalling an OpenRocketDocument (OR design)
 * to RASAero-compliant XML.
 * Big thanks to hcraigmiller for testing and providing feedback.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
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
     * This method marshals an OpenRocketDocument (OR design) to RASAero-compliant
     * XML.
     *
     * @param doc the OR design
     * @return RASAero-compliant XML
     */
    public String marshalToRASAero(OpenRocketDocument doc, WarningSet warnings, ErrorSet errors) {
        try {
            JAXBContext binder = JAXBContext.newInstance(RASAeroDocumentDTO.class);
            Marshaller marshaller = binder.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();

            marshaller.marshal(toRASAeroDocumentDTO(doc, warnings, errors), sw);
            return sw.toString();
        } catch (RASAeroExportException e) {
            errors.add(e.getMessage());
        } catch (Exception e) {
            log.error("Could not marshall a design to RASAero format. " + e.getMessage());
            throw new RuntimeException("Could not marshall a design to RASAero format. " + e.getMessage());
        }

        throw new RuntimeException("Could not marshall a design to RASAero format.");
    }

    @Override
    public void save(OutputStream dest, OpenRocketDocument doc, StorageOptions options, WarningSet warnings,
            ErrorSet errors) throws IOException {
        log.info("Saving .CDX1 file");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dest, StandardCharsets.UTF_8));
        writer.write(marshalToRASAero(doc, warnings, errors));
        writer.flush();
    }

    @Override
    public long estimateFileSize(OpenRocketDocument doc, StorageOptions options) {
        return marshalToRASAero(doc, new WarningSet(), new ErrorSet()).length();
    }

    /**
     * Root conversion method. It iterates over all subcomponents.
     *
     * @param doc      the OR design
     * @param warnings list to add export warnings to
     * @param errors   list to add export errors to
     * @return a corresponding RASAero representation
     */
    private RASAeroDocumentDTO toRASAeroDocumentDTO(OpenRocketDocument doc, WarningSet warnings, ErrorSet errors)
            throws RASAeroExportException {
        RASAeroDocumentDTO rad = new RASAeroDocumentDTO();
        rad.setDesign(toRocketDesignDTO(doc.getRocket(), warnings, errors));
        rad.setLaunchSite(toLaunchSiteDTO(doc, warnings, errors));
        rad.setRecovery(toRecoveryDTO(doc.getRocket(), warnings, errors));
        rad.setSimulationList(toSimulationListDTO(doc, warnings, errors));

        return rad;
    }

    /**
     * Create the RASAero rocket design (containing all the actual rocket
     * components).
     * 
     * @param rocket the OR rocket to export the components from
     * @return the RASAero rocket design
     */
    private RocketDesignDTO toRocketDesignDTO(Rocket rocket, WarningSet warnings, ErrorSet errors)
            throws RASAeroExportException {
        return new RocketDesignDTO(rocket, warnings, errors);
    }

    /**
     * Create RASAero launch site settings.
     * 
     * @param document document that contains simulations to take the launch site
     *                 settings from
     * @param warnings list to add export warnings to
     * @param errors   list to add export errors to
     * @return the RASAero launch site settings
     */
    private LaunchSiteDTO toLaunchSiteDTO(OpenRocketDocument document, WarningSet warnings, ErrorSet errors) {
        return new LaunchSiteDTO(document, warnings, errors);
    }

    /**
     * Create RASAero recovery settings.
     * 
     * @param rocket   rocket to fetch the recovery devices from
     * @param warnings list to add export warnings to
     * @param errors   list to add export errors to
     * @return the RASAero launch site settings
     */
    private RecoveryDTO toRecoveryDTO(Rocket rocket, WarningSet warnings, ErrorSet errors) {
        return new RecoveryDTO(rocket, warnings, errors);
    }

    /**
     * Create a list of simulations.
     * 
     * @param document document that contains simulations
     * @param warnings list to add export warnings to
     * @param errors   list to add export errors to
     * @return the RASAero simulation list
     */
    private SimulationListDTO toSimulationListDTO(OpenRocketDocument document, WarningSet warnings, ErrorSet errors) {
        return new SimulationListDTO(document, warnings, errors);
    }
}
