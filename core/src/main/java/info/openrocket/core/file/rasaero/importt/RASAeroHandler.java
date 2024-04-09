package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.SimulationOptions;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for a RASAeroDocument document.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RASAeroHandler extends AbstractElementHandler {
    /**
     * The main content handler.
     */
    private RocketDocumentHandler handler = null;

    private final DocumentLoadingContext context;
    private final String rocketName;

    public RASAeroHandler(DocumentLoadingContext context, String rocketName) {
        super();
        this.context = context;
        this.rocketName = rocketName;
    }

    /**
     * Return the OpenRocketDocument read from the file, or <code>null</code> if a
     * document
     * has not been read yet.
     *
     * @return the document read, or null.
     */
    public OpenRocketDocument getDocument() {
        return context.getOpenRocketDocument();
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        // Check for unknown elements
        if (!RASAeroCommonConstants.RASAERO_DOCUMENT.equals(element)) {
            warnings.add("Unknown element " + element + " in RASAeroDocument, ignoring.");
            return null;
        }

        // Check for first call
        if (handler != null) {
            warnings.add("Multiple document elements found, ignoring later ones.");
            return null;
        }

        handler = new RocketDocumentHandler(context, rocketName);
        return handler;
    }

    /**
     * A SAX handler for the RASAeroDocument element.
     */
    private static class RocketDocumentHandler extends AbstractElementHandler {
        /**
         * The DocumentLoadingContext
         */
        private final DocumentLoadingContext context;

        /**
         * The top-level component, from which all child components are added.
         */
        private final Rocket rocket;

        /**
         * The RASAero launch site settings to be used for all the OpenRocket
         * simulations.
         */
        private final SimulationOptions launchSiteSettings = new SimulationOptions();

        /**
         * The RASAero file version.
         */
        private String version;

        public RocketDocumentHandler(DocumentLoadingContext context, String rocketName) {
            super();
            this.context = context;
            this.rocket = context.getOpenRocketDocument().getRocket();
            this.rocket.setName(rocketName);
            final AxialStage stage = new AxialStage(); // The first stage in RASAero is not explicitly defined, so add
                                                       // it here
            stage.setName("Sustainer");
            this.rocket.addChild(stage);
        }

        @Override
        public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
                throws SAXException {
            // File version
            if (RASAeroCommonConstants.FILE_VERSION.equals(element)) {
                return PlainTextHandler.INSTANCE;
            }
            // Rocket design
            else if (RASAeroCommonConstants.ROCKET_DESIGN.equals(element)) {
                return new RocketDesignHandler(context, rocket.getChild(0));
            }
            // LaunchSite
            else if (RASAeroCommonConstants.LAUNCH_SITE.equals(element)) {
                return new LaunchSiteHandler(launchSiteSettings);
            }
            // Recovery
            else if (RASAeroCommonConstants.RECOVERY.equals(element)) {
                return new RecoveryHandler(rocket);
            }
            // SimulationList
            else if (RASAeroCommonConstants.SIMULATION_LIST.equals(element)) {
                return new SimulationListHandler(context, rocket, launchSiteSettings);
            }

            return null;
        }

        @Override
        public void closeElement(String element, HashMap<String, String> attributes, String content,
                WarningSet warnings) throws SAXException {
            /*
             * SAX handler for RASAero file version number. The value is not used currently,
             * but could be used in the future
             * for backward/forward compatibility reasons (different lower level handlers
             * could be called via a strategy pattern).
             */
            if (RASAeroCommonConstants.FILE_VERSION.equals(element)) {
                this.version = content;
            }
        }
    }

    /**
     * A SAX handler for the RocketDesign element.
     */
    private static class RocketDesignHandler extends AbstractElementHandler {
        /**
         * The DocumentLoadingContext
         */
        private final DocumentLoadingContext context;

        /**
         * The top-level component, from which all child components are added.
         */
        private final RocketComponent component;

        public RocketDesignHandler(DocumentLoadingContext context, RocketComponent component) {
            super();
            this.context = context;
            this.component = component;
        }

        @Override
        public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
                throws SAXException {
            // Nose cone
            if (RASAeroCommonConstants.NOSE_CONE.equals(element)) {
                return new NoseConeHandler(context, component, warnings);
            }
            // Body tube
            else if (RASAeroCommonConstants.BODY_TUBE.equals(element)) {
                return new BodyTubeHandler(context, component, warnings);
            }
            // Transition
            else if (RASAeroCommonConstants.TRANSITION.equals(element)) {
                return new TransitionHandler(context, component, warnings);
            }
            // Fin can
            else if (RASAeroCommonConstants.FIN_CAN.equals(element)) {
                return new FinCanHandler(context, component);
            }
            // Booster
            else if (RASAeroCommonConstants.BOOSTER.equals(element)) {
                return new BoosterHandler(context, component);
            }
            // BoatTail
            else if (RASAeroCommonConstants.BOATTAIL.equals(element)) {
                return new BoattailHandler(context, component, warnings);
            }

            // Surface finish
            else if (RASAeroCommonConstants.SURFACE_FINISH.equals(element)) {
                return PlainTextHandler.INSTANCE;
            }

            // Comments
            else if (RASAeroCommonConstants.COMMENTS.equals(element)) {
                return PlainTextHandler.INSTANCE;
            }

            // warnings.add("Unknown element " + element + " in RocketDesign, ignoring.");
            return null;
        }

        @Override
        public void closeElement(String element, HashMap<String, String> attributes, String content,
                WarningSet warnings) throws SAXException {
            // Surface finish
            if (RASAeroCommonConstants.SURFACE_FINISH.equals(element)) {
                SurfaceFinishHandler.setSurfaceFinishes(component.getRocket(), content, warnings);
            }
            // Comments
            else if (RASAeroCommonConstants.COMMENTS.equals(element)) {
                component.getRocket().setComment(content);
            }
        }
    }
}
