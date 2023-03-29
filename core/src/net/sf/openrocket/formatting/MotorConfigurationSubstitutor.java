package net.sf.openrocket.formatting;

import com.google.inject.Inject;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Chars;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General substitutor for motor configurations. This currently includes substitutions for
 *  - {motors} - the motor designation (e.g. "M1350-0")
 *  - {manufacturers} - the motor manufacturer (e.g. "AeroTech")
 *  - a combination of motors and manufacturers, e.g. {motors | manufacturers} -> "M1350-0 | AeroTech"
 *      You can choose which comes first and what the separator is. E.g. {manufacturers, motors} -> "AeroTech, M1350-0".
 *
 * <p>
 * This substitutor is added through injection. All substitutors with the "@Plugin" tag in the formatting package will
 * be included automatically.
 */
@Plugin
public class MotorConfigurationSubstitutor implements RocketSubstitutor {
    public static final String SUBSTITUTION_START = "{";
    public static final String SUBSTITUTION_END = "}";
    public static final String SUBSTITUTION_MOTORS = "motors";
    public static final String SUBSTITUTION_MANUFACTURERS = "manufacturers";

    // Substitutions for combinations of motors and manufacturers
    private static final String SUBSTITUTION_PATTERN = "\\" + SUBSTITUTION_START +
            "(" + SUBSTITUTION_MOTORS + "|" + SUBSTITUTION_MANUFACTURERS + ")" +
            "(.*?)" +
            "(" + SUBSTITUTION_MOTORS + "|" + SUBSTITUTION_MANUFACTURERS + ")" +
            "\\" + SUBSTITUTION_END;

    @Inject
    private Translator trans;

    @Override
    public boolean containsSubstitution(String input) {
        return getSubstitutionContent(input) != null;
    }

    @Override
    public String substitute(String input, Rocket rocket, FlightConfigurationId configId) {
        String description = getConfigurationSubstitution(input, rocket, configId);
        String substitutionString = getSubstiutionString(input);
        if (substitutionString != null) {
            return input.replace(substitutionString, description);
        }
        return input;
    }

    @Override
    public Map<String, String> getDescriptions() {
        return null;
    }

    public String getConfigurationSubstitution(String input, Rocket rocket, FlightConfigurationId fcid) {
        StringBuilder configurations = new StringBuilder();
        int motorCount = 0;

        // Iterate over each stage and store the manufacturer of each motor
        List<List<String>> list = new ArrayList<>();
        List<String> currentList = new ArrayList<>();

        String[] content = getSubstitutionContent(input);
        if (content == null) {
            return "";
        }

        FlightConfiguration config = rocket.getFlightConfiguration(fcid);
        for (RocketComponent c : rocket) {
            if (c instanceof AxialStage) {
                currentList = new ArrayList<>();
                list.add(currentList);
            } else if (c instanceof MotorMount) {
                MotorMount mount = (MotorMount) c;
                MotorConfiguration inst = mount.getMotorConfig(fcid);
                Motor motor = inst.getMotor();

                if (mount.isMotorMount() && config.isComponentActive(mount) && (motor != null)) {
                    String motorDesignation = motor.getMotorName(inst.getEjectionDelay());
                    String manufacturer = "";
                    if (motor instanceof ThrustCurveMotor) {
                        manufacturer = ((ThrustCurveMotor) motor).getManufacturer().getDisplayName();
                    }

                    for (int i = 0; i < mount.getMotorCount(); i++) {
                        if (content.length == 2) {
                            if (SUBSTITUTION_MOTORS.equals(content[1])) {
                                currentList.add(motorDesignation);
                            } else if (SUBSTITUTION_MANUFACTURERS.equals(content[1])) {
                                currentList.add(manufacturer);
                            } else {
                                continue;
                            }
                        } else if (content.length == 4) {
                            String configString;
                            if (content[1].equals(SUBSTITUTION_MOTORS)) {
                                configString = motorDesignation;
                            } else if (content[1].equals(SUBSTITUTION_MANUFACTURERS)) {
                                configString = manufacturer;
                            } else {
                                continue;
                            }
                            configString += content[2];
                            if (content[3].equals(SUBSTITUTION_MOTORS)) {
                                configString += motorDesignation;
                            } else if (content[3].equals(SUBSTITUTION_MANUFACTURERS)) {
                                configString += manufacturer;
                            } else {
                                continue;
                            }
                            currentList.add(configString);
                        } else {
                            continue;
                        }
                        motorCount++;
                    }
                }
            }
        }

        if (motorCount == 0) {
            return trans.get("Rocket.motorCount.Nomotor");
        }

        // Change multiple occurrences of a motor to n x motor
        List<String> stages = new ArrayList<>();
        for (List<String> stage : list) {
            String stageName = "";
            String previous = null;
            int count = 0;

            Collections.sort(stage);
            for (String current : stage) {
                if (current.equals(previous)) {
                    count++;
                } else {
                    if (previous != null) {
                        String s = count > 1 ? count + Chars.TIMES + previous : previous;
                        stageName = stageName.equals("") ? s : stageName + "," + s;
                    }

                    previous = current;
                    count = 1;
                }
            }

            if (previous != null) {
                String s = count > 1 ? "" + count + Chars.TIMES + previous : previous;
                stageName = stageName.equals("") ? s : stageName + "," + s;
            }

            stages.add(stageName);
        }

        for (int i = 0; i < stages.size(); i++) {
            String s = stages.get(i);
            if (s.equals("") && config.isStageActive(i)) {
                s = trans.get("Rocket.motorCount.noStageMotors");
            }

            configurations.append(i == 0 ? s : "; " + s);
        }

        return configurations.toString();
    }

    /**
     * Returns which string in input should be replaced, or null if no text needs to be replaced.
     * @param input The input string
     * @return The string to replace, or null if no text needs to be replaced.
     */
    private static String getSubstiutionString(String input) {
        String[] content = getSubstitutionContent(input);
        if (content != null) {
            return content[0];
        }
        return null;
    }

    /**
     * Fills in the content of the substitution tag and the separator.
     * If there are both a motor and a manufacturer substitution tag, the array will contain the following:
     *  [0] = The full tag, including substitution start and end
     *  [1] = The motor/manufacturer substitution tag, depending on which one was found first.
     *  if there are two substitution tags, the array will also contain the following:
     *  ([2] = The separator)
     *  ([3] = The motor/manufacturer substitution tag, depending on which one was found first.)
     * @param input The input string
     * @return The content of the substitution tag and the separator, or null if no text needs to be replaced.
     */
    private static String[] getSubstitutionContent(String input) {
        // First try with only the motors tag
        String pattern = "\\" + SUBSTITUTION_START + "(" + SUBSTITUTION_MOTORS + ")" + "\\" + SUBSTITUTION_END;
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(input);
        if (matcher.find()) {
            String[] content = new String[2];
            content[0] = matcher.group(0);
            content[1] = matcher.group(1);
            return content;
        }
        // First try with only the manufacturers tag
        pattern = "\\" + SUBSTITUTION_START + "(" + SUBSTITUTION_MANUFACTURERS + ")" + "\\" + SUBSTITUTION_END;
        regexPattern = Pattern.compile(pattern);
        matcher = regexPattern.matcher(input);
        if (matcher.find()) {
            String[] content = new String[2];
            content[0] = matcher.group(0);
            content[1] = matcher.group(1);
            return content;
        }

        // Then try combined patterns
        pattern = SUBSTITUTION_PATTERN;
        regexPattern = Pattern.compile(pattern);
        matcher = regexPattern.matcher(input);
        if (matcher.find()) {
            String[] content = new String[4];
            content[0] = matcher.group(0);
            content[1] = matcher.group(1);
            if (matcher.groupCount() >= 3) {
                content[2] = matcher.group(2);
                content[3] = matcher.group(3);
                for (int i = 4; i < matcher.groupCount(); i++) {
                    content[3] += matcher.group(i);
                }
            }
            return content;
        }
        return null;
    }
}

