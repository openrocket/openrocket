package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handles importing the RASAero recovery settings to an OpenRocket recovery
 * device.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RecoveryHandler extends AbstractElementHandler {
    private final Rocket rocket;

    // Recovery parameters
    private final int NR_OF_RECOVERY_DEVICES = 2; // Number of recovery devices supported by RASAero
    private final Double[] altitude = new Double[NR_OF_RECOVERY_DEVICES]; // Altitude in meters
    private final String[] deviceType = new String[NR_OF_RECOVERY_DEVICES]; // None or Parachute
    private final Boolean[] event = new Boolean[NR_OF_RECOVERY_DEVICES]; // True if recovery device is enabled
    private final Double[] size = new Double[NR_OF_RECOVERY_DEVICES]; // Parachute diameter in meters
    private final String[] eventType = new String[NR_OF_RECOVERY_DEVICES]; // When to deploy
    private final Double[] CD = new Double[NR_OF_RECOVERY_DEVICES]; // Coefficient of drag

    public RecoveryHandler(Rocket rocket) {
        this.rocket = rocket;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings)
            throws SAXException {
        String[] elements = new String[] { RASAeroCommonConstants.RECOVERY_ALTITUDE,
                RASAeroCommonConstants.RECOVERY_DEVICE_TYPE,
                RASAeroCommonConstants.RECOVERY_EVENT, RASAeroCommonConstants.RECOVERY_SIZE,
                RASAeroCommonConstants.RECOVERY_EVENT_TYPE,
                RASAeroCommonConstants.RECOVERY_CD };
        for (int i = 1; i <= NR_OF_RECOVERY_DEVICES; i++) {
            for (String e : elements) {
                String key = e + i;
                if (key.equals(element)) {
                    return PlainTextHandler.INSTANCE;
                }
            }
        }

        warnings.add("Unknown element " + element + " in recovery settings.");
        return null;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        Map<String, Object[]> mapParametersToVars = new HashMap<>();
        mapParametersToVars.put(RASAeroCommonConstants.RECOVERY_ALTITUDE, altitude);
        mapParametersToVars.put(RASAeroCommonConstants.RECOVERY_DEVICE_TYPE, deviceType);
        mapParametersToVars.put(RASAeroCommonConstants.RECOVERY_EVENT, event);
        mapParametersToVars.put(RASAeroCommonConstants.RECOVERY_SIZE, size);
        mapParametersToVars.put(RASAeroCommonConstants.RECOVERY_EVENT_TYPE, eventType);
        mapParametersToVars.put(RASAeroCommonConstants.RECOVERY_CD, CD);

        // Set the values of the recovery parameters
        for (int i = 1; i <= NR_OF_RECOVERY_DEVICES; i++) {
            for (String e : mapParametersToVars.keySet()) {
                String key = e + i;
                if (key.equals(element)) {
                    Object[] vars = mapParametersToVars.get(e);
                    if (vars.length != NR_OF_RECOVERY_DEVICES) {
                        throw new IllegalArgumentException("Recovery var array length is not 2");
                    }
                    if (vars instanceof Double[]) {
                        vars[i - 1] = Double.parseDouble(content);
                    } else if (vars instanceof Boolean[]) {
                        vars[i - 1] = Boolean.parseBoolean(content);
                    } else if (vars instanceof String[]) {
                        vars[i - 1] = content;
                    } else {
                        throw new IllegalArgumentException(
                                "Unknown recovery var type " + vars.getClass().getComponentType());
                    }
                }
            }
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        // Recovery device 1
        if (event[0]) {
            addRecoveryDevice(0, warnings);
        }

        // Recovery device 2 (Don't add if device 1 is disabled, or its event type is
        // set to none)
        if (event[1] && event[0] && !"None".equals(eventType[0])) {
            addRecoveryDevice(1, warnings);
        }
    }

    /**
     * Create a new recovery device with the parameters from RASAero, and add it to
     * the rocket.
     * There are some specific rules to importing the device type:
     * - Event unchecked => ignore the recovery event
     * - Event checked and event type set to None => ignore
     * - Device set to None => use a parachute with size 0
     * - Device set to Parachute => use a parachute with the given size
     * 
     * @param deviceNr Recovery device number (0 or 1)
     * @param warnings Warning set to add import warnings to
     */
    private void addRecoveryDevice(int deviceNr, WarningSet warnings) {
        if (deviceNr > NR_OF_RECOVERY_DEVICES - 1) {
            throw new IllegalArgumentException("Invalid recovery device number " + deviceNr);
        }

        // No event => ignore
        if (eventType[deviceNr].equals("None")) {
            return;
        }

        final RecoveryDevice recoveryDevice;

        // Parachutes are explicitly supported by RASAero
        if (deviceType[deviceNr].equals("Parachute")) {
            recoveryDevice = createParachute(deviceNr, size[deviceNr], CD[deviceNr], altitude[deviceNr],
                    eventType[deviceNr], warnings);
        }
        // This is a bit strange from RASAero, but if the device type is None, use a
        // parachute with 0 size, but with a CD
        // (= Ballistic/tumble decent for that device)
        else if (deviceType[deviceNr].equals("None")) {
            recoveryDevice = createParachute(deviceNr, 0, CD[deviceNr], altitude[deviceNr], eventType[deviceNr],
                    warnings);
        }
        // Unknown device type
        else {
            warnings.add("Unknown recovery device type " + deviceType[deviceNr] + " for recovery device "
                    + (deviceNr + 1) + ". Ignoring.");
            return;
        }

        // Add the recovery device to the rocket
        if (deviceNr == 0) {
            addRecoveryDevice1ToRocket(recoveryDevice, warnings);
        } else if (deviceNr == 1) {
            addRecoveryDevice2ToRocket(recoveryDevice, warnings);
        }
    }

    /**
     * Create a parachute with the parameters from RASAero.
     * 
     * @param recoveryDeviceNr The recovery device number (0 or 1)
     * @param size             The size of the parachute
     * @param CD               The drag coefficient of the parachute
     * @param altitude         The altitude of the parachute deployment
     * @param eventType        The event type of the parachute deployment
     * @param warnings         The warning set to add import warnings to
     * @return The parachute with the parameters from RASAero
     */
    private Parachute createParachute(int recoveryDeviceNr, double size, double CD, double altitude, String eventType,
            WarningSet warnings) {
        Parachute recoveryDevice = new Parachute();
        recoveryDevice.setName("Recovery Event " + (recoveryDeviceNr + 1));
        DeploymentConfiguration config = recoveryDevice.getDeploymentConfigurations().getDefault();

        recoveryDevice.setDiameter(size / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH);
        recoveryDevice.setLineLength(recoveryDevice.getDiameter());
        recoveryDevice.setCD(CD);
        config.setDeployAltitude(altitude / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_ALTITUDE);

        // There is a special RASAero rule: if event 1 AND event 2 are set to apogee,
        // then set event 2 to altitude
        if (recoveryDeviceNr == 1 && eventType.equals("Apogee") && this.eventType[0].equals("Apogee")) {
            eventType = "Altitude";
            warnings.add(
                    "Recovery device 2 is set to apogee, but recovery device 1 is also set to apogee. Setting recovery device 2 to altitude.");
        }
        config.setDeployEvent(RASAeroCommonConstants.RASAERO_TO_OPENROCKET_DEPLOY_EVENT(eventType, warnings));

        // Shroud line count = diameter / 6 inches. 6 inches = 0.1524 meters. Minimum is
        // 6 lines.
        recoveryDevice.setLineCount(Math.max(6, (int) Math.round(recoveryDevice.getDiameter() / 0.1524)));

        return recoveryDevice;
    }

    /**
     * RASAero does not specify where recovery devices are located.
     * We will use the following (arbitrary, but logical) rule to add the recovery
     * device to the rocket:
     * If only Recovery Device 1 (deployment at apogee) is active, but Recovery
     * Device 2 (deployment at altitude) is not:
     * 1. If the sustainer has 1, 2 or 3 body tubes (with no vent band - a body tube
     * with length <= .8 calibers),
     * Recovery Device 1 (deployment at apogee) is positioned near the top (1.125
     * calibers) of the first body tube.
     * 2. If the sustainer has 3 or more body tubes and one vent band (a body tube
     * with length <= .8 calibers),
     * position Recovery Device 1 in the body tube above the vent band, near the top
     * (1.125 calibers).
     * 3. If the sustainer has 4 or more body tubes (with no vent bands), position
     * Recovery Device 1 in the
     * second body tube, near the top (1.125 calibers).
     * If there is a Recovery Device 2:
     * 4. If the sustainer has 1 body tube, position Recovery Device 1 near the top
     * (1.125 calibers) of the first body tube.
     * 5. If the sustainer has 2 body tubes, position Recovery Device 1 near the top
     * (1.125 calibers) of the second body tube.
     * 6. If the sustainer has 3 body tubes, position Recovery Device 1 near the top
     * (1.125 calibers) of the second body tube.
     * 7. If the sustainer has 3 or more body tubes and one vent band (a body tube
     * with length <= .8 calibers), position
     * Recovery Device 1 near the top (1.125 calibers) of the tube below the vent
     * band.
     * 8. If the sustainer has 4 or more body tubes, position Recovery Device 1 near
     * the top (1.125 calibers) of the third body tube.
     * 
     * @param recoveryDevice the recovery device to add
     * @param warnings       the warning set to add import warnings to
     */
    private void addRecoveryDevice1ToRocket(RecoveryDevice recoveryDevice, WarningSet warnings) {
        AxialStage sustainer = rocket.getStage(0);
        if (sustainer == null || sustainer.getChildCount() < 2 || !(sustainer.getChild(1) instanceof BodyTube)) {
            warnings.add(
                    "No sustainer body tube found." + recoveryDevice.getName() + " will not be added to the rocket.");
            return;
        }

        final BodyTube parentBodyTube;
        List<BodyTube> bodyTubes = getBodyTubesInStage(sustainer);
        int nrOfTubes = bodyTubes.size();

        // If there is no Recovery Device 2
        if (!event[1] || "None".equals(eventType[1])) {
            switch (nrOfTubes) {
                case 0:
                    warnings.add("No sustainer body tube found." + recoveryDevice.getName()
                            + " will not be added to the rocket.");
                    return;
                case 1:
                case 2:
                    // Rule 1: Add to the first tube, 1.125 calibers from the top
                    parentBodyTube = bodyTubes.get(0);
                    break;
                default:
                    // Check if there is a vent band
                    BodyTube ventBand = null;
                    for (BodyTube tube : bodyTubes) {
                        if (tube.getLength() <= 1.6 * tube.getOuterRadius()) { // 0.8 calibers
                            ventBand = tube;
                            break;
                        }
                    }

                    if (ventBand != null) {
                        // Rule 2: Add to the tube above the vent band, 1.125 calibers from the top
                        int index = bodyTubes.indexOf(ventBand);
                        int parentIndex = Math.max(index - 1, 0);
                        parentBodyTube = bodyTubes.get(parentIndex);
                    } else if (nrOfTubes == 3) {
                        // Rule 1: Add to the first tube, 1.125 calibers from the top
                        parentBodyTube = bodyTubes.get(0);
                    } else {
                        // Rule 3: Add to the second tube, 1.125 calibers from the top
                        parentBodyTube = bodyTubes.get(1);
                    }

                    break;
            }
        }
        // If there is a Recovery Device 2
        else {
            switch (nrOfTubes) {
                case 0:
                    warnings.add("No sustainer body tube found." + recoveryDevice.getName()
                            + " will not be added to the rocket.");
                    return;
                case 1:
                    // Rule 4: Add to the first tube, 1.125 calibers from the top
                    parentBodyTube = bodyTubes.get(0);
                    break;
                case 2:
                    // Rule 5: Add to the second tube, 1.125 calibers from the top
                    parentBodyTube = bodyTubes.get(1);
                    break;
                default:
                    // Check if there is a vent band
                    BodyTube ventBand = null;
                    for (BodyTube tube : bodyTubes) {
                        if (tube.getLength() <= 1.6 * tube.getOuterRadius()) { // 0.8 calibers
                            ventBand = tube;
                            break;
                        }
                    }

                    if (ventBand != null) {
                        // Rule 7: Add to the tube below the vent band, 1.125 calibers from the top
                        int index = bodyTubes.indexOf(ventBand);
                        int parentIndex = Math.min(index + 1, bodyTubes.size() - 1);
                        parentBodyTube = bodyTubes.get(parentIndex);
                    } else if (nrOfTubes == 3) {
                        // Rule 6: Add to the second tube, 1.125 calibers from the top
                        parentBodyTube = bodyTubes.get(1);
                    } else {
                        // Rule 8: Add to the third tube, 1.125 calibers from the top
                        parentBodyTube = bodyTubes.get(2);
                    }

                    break;
            }
        }

        // Add the recovery device to the rocket
        parentBodyTube.addChild(recoveryDevice);

        // Position the recovery device
        recoveryDevice.setAxialMethod(AxialMethod.TOP);
        double offset = parentBodyTube.getOuterRadius() * 2.25; // 1.125 calibers
        if (offset + recoveryDevice.getLength() > parentBodyTube.getLength()) {
            offset = 0;
        }
        recoveryDevice.setAxialOffset(offset);
    }

    /**
     * RASAero does not specify where recovery devices are located.
     * We will use the following (arbitrary, but logical) rules to add Recovery
     * Device 2 (deployment at altitude <->
     * Recovery Device 1, with deployment at apogee) to the rocket:
     * 1. If the sustainer has 1 body tube, position Recovery Device 2 in the first
     * body tube, just below Recovery Device 1.
     * 2. If the sustainer has 2 or 3 body tubes, position Recovery Device 2 near
     * the top (1.125 calibers) of the first body tube.
     * 3. If the sustainer has 3 or more body tubes and one vent band (a body tube
     * with length <= .8 calibers), position
     * Recovery Device 2 near the top (1.125 calibers) of the tube above the vent
     * band.
     * 4. If the sustainer has 4 or more body tubes, position Recovery Device 1 near
     * the top (1.125 calibers) of the second body tube.
     * 
     * @param recoveryDevice the recovery device to add
     * @param warnings       the warning set to add import warnings to
     */
    private void addRecoveryDevice2ToRocket(RecoveryDevice recoveryDevice, WarningSet warnings) {
        double offset = 0;
        final BodyTube parentBodyTube;
        AxialStage sustainer = rocket.getStage(0);
        List<BodyTube> bodyTubes = getBodyTubesInStage(sustainer);

        switch (bodyTubes.size()) {
            case 0:
                warnings.add("No sustainer body tube found." + recoveryDevice.getName()
                        + " will not be added to the rocket.");
                return;
            case 1:
                // Rule 1: Add to the first tube, just below Recovery Device 1
                parentBodyTube = bodyTubes.get(0);
                offset += recoveryDevice.getLength() * 1.05; // = equivalent to adding after recovery device 1
                break;
            case 2:
                // Rule 2: Add to first body tube, 1.125 calibers from the top
                parentBodyTube = bodyTubes.get(0);
                break;
            default:
                // Check if there is a vent band
                BodyTube ventBand = null;
                for (BodyTube tube : bodyTubes) {
                    if (tube.getLength() <= 1.6 * tube.getOuterRadius()) { // 0.8 calibers
                        ventBand = tube;
                        break;
                    }
                }

                if (ventBand != null) {
                    // Rule 3: Add to the tube above the vent band, 1.125 calibers from the top
                    int index = bodyTubes.indexOf(ventBand);
                    int parentIndex = Math.max(index - 1, 0);
                    parentBodyTube = bodyTubes.get(parentIndex);
                } else if (bodyTubes.size() == 3) {
                    // Rule 2: Add to first body tube, 1.125 calibers from the top
                    parentBodyTube = bodyTubes.get(0);
                } else {
                    // Rule 4: Add to the second tube, 1.125 calibers from the top
                    parentBodyTube = bodyTubes.get(1);
                }

                break;
        }

        // Add the recovery device to the rocket
        parentBodyTube.addChild(recoveryDevice);

        // Position the recovery device
        offset += parentBodyTube.getOuterRadius() * 2.25; // 1.125 calibers
        recoveryDevice.setAxialMethod(AxialMethod.TOP);
        if (offset + recoveryDevice.getLength() > parentBodyTube.getLength()) {
            // For rule 1, device 2 should be below device 1, so just in case, put this at
            // the bottom instead of at the top
            if (bodyTubes.size() == 1) {
                recoveryDevice.setAxialMethod(AxialMethod.BOTTOM);
            }
            offset = 0;
        }
        recoveryDevice.setAxialOffset(offset);
    }

    private List<BodyTube> getBodyTubesInStage(AxialStage stage) {
        // Get all body tubes
        List<BodyTube> bodyTubes = new LinkedList<>();
        for (int i = 0; i < stage.getChildCount(); i++) {
            if (stage.getChild(i) instanceof BodyTube) {
                bodyTubes.add((BodyTube) stage.getChild(i));
            }
        }

        return bodyTubes;
    }
}
