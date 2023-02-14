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
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Chars;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin
public class MotorManufacturerSubstitutor implements RocketSubstitutor {
    public static final String SUBSTITUTION = "{manufacturers}";

    @Inject
    private Translator trans;

    @Override
    public boolean containsSubstitution(String str) {
        return str.contains(SUBSTITUTION);
    }

    @Override
    public String substitute(String str, Rocket rocket, FlightConfigurationId configId) {
        String description = getMotorConfigurationManufacturer(rocket, configId);
        return str.replace(SUBSTITUTION, description);
    }

    @Override
    public Map<String, String> getDescriptions() {
        Map<String, String> desc = new HashMap<>();
        desc.put(SUBSTITUTION, trans.get("MotorManufacturerSubstitutor.description"));
        return null;
    }



    public String getMotorConfigurationManufacturer(Rocket rocket, FlightConfigurationId fcid) {
        String manufacturers;
        int motorCount = 0;

        // Generate the description

        // First iterate over each stage and store the manufacturer of each motor
        List<List<String>> list = new ArrayList<>();
        List<String> currentList = Collections.emptyList();

        FlightConfiguration config = rocket.getFlightConfiguration(fcid);
        for (RocketComponent c : rocket) {
            if (c instanceof AxialStage) {
                currentList = new ArrayList<>();
                list.add(currentList);

            } else if (c instanceof MotorMount) {
                MotorMount mount = (MotorMount) c;
                MotorConfiguration inst = mount.getMotorConfig(fcid);
                Motor motor = inst.getMotor();

                if (mount.isMotorMount() && config.isComponentActive(mount) && motor instanceof ThrustCurveMotor) {
                    String manufacturer = ((ThrustCurveMotor) motor).getManufacturer().getDisplayName();

                    for (int i = 0; i < mount.getMotorCount(); i++) {
                        currentList.add(manufacturer);
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
                        String s = "";
                        if (count > 1) {
                            s = "" + count + Chars.TIMES + previous;
                        } else {
                            s = previous;
                        }

                        if (stageName.equals(""))
                            stageName = s;
                        else
                            stageName = stageName + "," + s;
                    }

                    previous = current;
                    count = 1;
                }
            }
            if (previous != null) {
                String s = "";
                if (count > 1) {
                    s = "" + count + Chars.TIMES + previous;
                } else {
                    s = previous;
                }

                if (stageName.equals(""))
                    stageName = s;
                else
                    stageName = stageName + "," + s;
            }
            stages.add(stageName);
        }

        manufacturers = "";
        for (int i = 0; i < stages.size(); i++) {
            String s = stages.get(i);
            if (s.equals("") && config.isStageActive(i))
                s = trans.get("Rocket.motorCount.noStageMotors");
            if (i == 0)
                manufacturers = manufacturers + s;
            else
                manufacturers = manufacturers + "; " + s;
        }
        return manufacturers;
    }
}

