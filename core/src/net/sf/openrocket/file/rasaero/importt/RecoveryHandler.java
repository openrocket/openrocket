package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles importing the RASAero recovery settings to an OpenRocket recovery device.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class RecoveryHandler extends AbstractElementHandler {
    private final Rocket rocket;

    // Recovery parameters
    private final int NR_OF_RECOVERY_DEVICES = 2;                               // Number of recovery devices supported by RASAero
    private final Double[] altitude = new Double[NR_OF_RECOVERY_DEVICES];       // Altitude in meters
    private final String[] deviceType = new String[NR_OF_RECOVERY_DEVICES];     // None or Parachute
    private final Boolean[] event = new Boolean[NR_OF_RECOVERY_DEVICES];        // True if recovery device is enabled
    private final Double[] size = new Double[NR_OF_RECOVERY_DEVICES];           // Parachute diameter in meters
    private final String[] eventType = new String[NR_OF_RECOVERY_DEVICES];      // When to deploy
    private final Double[] CD = new Double[NR_OF_RECOVERY_DEVICES];             // Coefficient of drag

    public RecoveryHandler(Rocket rocket) {
        this.rocket = rocket;
    }
    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        String[] elements = new String[] {RASAeroCommonConstants.RECOVERY_ALTITUDE, RASAeroCommonConstants.RECOVERY_DEVICE_TYPE,
                RASAeroCommonConstants.RECOVERY_EVENT, RASAeroCommonConstants.RECOVERY_SIZE, RASAeroCommonConstants.RECOVERY_EVENT_TYPE,
                RASAeroCommonConstants.RECOVERY_CD};
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
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
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
                        vars[i-1] = Double.parseDouble(content);
                    } else if (vars instanceof Boolean[]) {
                        vars[i-1] = Boolean.parseBoolean(content);
                    } else if (vars instanceof String[]) {
                        vars[i-1] = content;
                    } else {
                        throw new IllegalArgumentException("Unknown recovery var type " + vars.getClass().getComponentType());
                    }
                }
            }
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        // Recovery device 1
        if (event[0]) {
            addRecoveryDevice(0, warnings);
        }

        // Recovery device 2 (Don't add if device 1 is disabled, or its event type is set to none)
        if (event[1] && event[0] && !"None".equals(eventType[0])) {
            addRecoveryDevice(1, warnings);
        }
    }

    /**
     * Create a new recovery device with the parameters from RASAero, and add it to the rocket.
     * There are some specific rules to importing the device type:
     *  - Event unchecked => ignore the recovery event
     *  - Event checked and event type set to None => ignore
     *  - Device set to None => use a parachute with size 0
     *  - Device set to Parachute => use a parachute with the given size
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
            recoveryDevice = createParachute(deviceNr, size[deviceNr], CD[deviceNr], altitude[deviceNr], eventType[deviceNr], warnings);
        }
        // This is a bit strange from RASAero, but if the device type is None, use a parachute with 0 size, but with a CD
        // (= Ballistic/tumble decent for that device)
        else if (deviceType[deviceNr].equals("None")) {
            recoveryDevice = createParachute(deviceNr, 0, CD[deviceNr], altitude[deviceNr], eventType[deviceNr], warnings);
        }
        // Unknown device type
        else {
            warnings.add("Unknown recovery device type " + deviceType[deviceNr] + " for recovery device " + (deviceNr + 1) + ". Ignoring.");
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
     * @param recoveryDeviceNr The recovery device number (0 or 1)
     * @param size The size of the parachute
     * @param CD The drag coefficient of the parachute
     * @param altitude The altitude of the parachute deployment
     * @param eventType The event type of the parachute deployment
     * @param warnings The warning set to add import warnings to
     * @return The parachute with the parameters from RASAero
     */
    private Parachute createParachute(int recoveryDeviceNr, double size, double CD, double altitude, String eventType, WarningSet warnings) {
        Parachute recoveryDevice = new Parachute();
        recoveryDevice.setName("Recovery Event " + (recoveryDeviceNr+1));
        DeploymentConfiguration config = recoveryDevice.getDeploymentConfigurations().getDefault();

        recoveryDevice.setDiameter(size / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH);
        recoveryDevice.setLineLength(recoveryDevice.getDiameter());
        recoveryDevice.setCD(CD);
        config.setDeployAltitude(altitude / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_ALTITUDE);

        // There is a special RASAero rule: if event 1 AND event 2 are set to apogee, then set event 2 to altitude
        if (recoveryDeviceNr == 1 && eventType.equals("Apogee") && this.eventType[0].equals("Apogee")) {
            eventType = "Altitude";
            warnings.add("Recovery device 2 is set to apogee, but recovery device 1 is also set to apogee. Setting recovery device 2 to altitude.");
        }
        config.setDeployEvent(RASAeroCommonConstants.getDeployEventFromRASAero(eventType, warnings));

        // Shroud line count = diameter / 6 inches. 6 inches = 0.1524 meters. Minimum is 6 lines.
        recoveryDevice.setLineCount(Math.max(6, (int) Math.round(recoveryDevice.getDiameter() / 0.1524)));

        return recoveryDevice;
    }

    /**
     * RASAero does not specify where recovery devices are located.
     * We will use the following (arbitrary, but logical) rule to add the recovery device to the rocket:
     *      put recovery device 1 in the first sustainer body tube just below the nosecone (about 1.125 calibers below
     *      the top of the body tube).
     * @param recoveryDevice the recovery device to add
     * @param warnings the warning set to add import warnings to
     */
    private void addRecoveryDevice1ToRocket(RecoveryDevice recoveryDevice, WarningSet warnings) {
        AxialStage sustainer = rocket.getStage(0);
        if (sustainer == null || sustainer.getChildCount() < 2 || !(sustainer.getChild(1) instanceof BodyTube)) {
            warnings.add("No sustainer body tube found." + recoveryDevice.getName() + " will not be added to the rocket.");
            return;
        }
        BodyTube bodyTube = (BodyTube) sustainer.getChild(1);
        bodyTube.addChild(recoveryDevice);
        recoveryDevice.setAxialMethod(AxialMethod.TOP);
        double offset = bodyTube.getOuterRadius() * 1.125;
        if (offset + recoveryDevice.getLength() > bodyTube.getLength()) {
            offset = 0;
        }
        recoveryDevice.setAxialOffset(offset);
    }

    /**
     * RASAero does not specify where recovery devices are located.
     * We will use the following (arbitrary, but logical) rules to add the recovery device to the rocket:
     *  1.  If the airframe has only 1 body tube:
     *      put recovery device 2 in the first body tube just below recovery device 1.
     *  2.  If the airframe has two body tubes:
     *      put recovery device 2 in the second body tube (about 1.125 calibers below the top of the second body tube).
     *  3.  If the airframe has three or more body tubes:
     *      put recovery device 2 in the third body tube (about 1.125 calibers below the top of the third body tube).
     * @param recoveryDevice the recovery device to add
     * @param warnings the warning set to add import warnings to
     */
    private void addRecoveryDevice2ToRocket(RecoveryDevice recoveryDevice, WarningSet warnings) {
        final BodyTube bodyTube;
        double offset;

        AxialStage sustainer = rocket.getStage(0);

        // Get all body tubes
        List<BodyTube> bodyTubes = new ArrayList<>();
        for (int i = 0; i < sustainer.getChildCount(); i++) {
            if (sustainer.getChild(i) instanceof BodyTube) {
                bodyTubes.add((BodyTube) sustainer.getChild(i));
            }
        }

        switch (bodyTubes.size()) {
            case 0:
                warnings.add("No sustainer body tube found." + recoveryDevice.getName() + " will not be added to the rocket.");
                return;
            case 1:
                // If there is only one body tube, add the recovery device to the first body tube, after recovery device 1
                bodyTube = bodyTubes.get(0);
                offset = bodyTube.getOuterRadius() * 1.125;
                offset += recoveryDevice.getLength() * 1.05;        // = equivalent to adding after recovery device 1
                break;
            case 2:
                // If there are two body tubes, add the recovery device to the second body tube
                bodyTube = bodyTubes.get(1);
                offset = bodyTube.getOuterRadius() * 1.125;
                break;
            default:
                // If there are three or more body tubes, add the recovery device to the third body tube
                bodyTube = bodyTubes.get(2);
                offset = bodyTube.getOuterRadius() * 1.125;
                break;
        }

        // Add the recovery device to the rocket
        bodyTube.addChild(recoveryDevice);
        recoveryDevice.setAxialMethod(AxialMethod.TOP);
        if (offset + recoveryDevice.getLength() > bodyTube.getLength()) {
            offset = 0;
        }
        recoveryDevice.setAxialOffset(offset);
    }
}
