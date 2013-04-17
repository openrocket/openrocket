package net.sf.openrocket.formatting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.plugin.Plugin;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Chars;

import com.google.inject.Inject;

@Plugin
public class MotorDescriptionSubstitutor implements RocketSubstitutor {
	public static final String SUBSTITUTION = "{motors}";
	
	@Inject
	private Translator trans;
	
	@Override
	public boolean containsSubstitution(String str) {
		return str.contains(SUBSTITUTION);
	}
	
	@Override
	public String substitute(String str, Rocket rocket, String configId) {
		String description = getMotorConfigurationDescription(rocket, configId);
		return str.replace(SUBSTITUTION, description);
	}
	
	@Override
	public Map<String, String> getDescriptions() {
		Map<String, String> desc = new HashMap<String, String>();
		desc.put(SUBSTITUTION, trans.get("MotorDescriptionSubstitutor.description"));
		return null;
	}
	
	
	
	public String getMotorConfigurationDescription(Rocket rocket, String id) {
		String name;
		int motorCount = 0;
		
		// Generate the description
		
		// First iterate over each stage and store the designations of each motor
		List<List<String>> list = new ArrayList<List<String>>();
		List<String> currentList = Collections.emptyList();
		
		Iterator<RocketComponent> iterator = rocket.iterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			
			if (c instanceof Stage) {
				
				currentList = new ArrayList<String>();
				list.add(currentList);
				
			} else if (c instanceof MotorMount) {
				
				MotorMount mount = (MotorMount) c;
				Motor motor = mount.getMotor(id);
				
				if (mount.isMotorMount() && motor != null) {
					String designation = motor.getDesignation(mount.getMotorDelay(id));
					
					for (int i = 0; i < mount.getMotorCount(); i++) {
						currentList.add(designation);
						motorCount++;
					}
				}
				
			}
		}
		
		if (motorCount == 0) {
			return trans.get("Rocket.motorCount.Nomotor");
		}
		
		// Change multiple occurrences of a motor to n x motor
		List<String> stages = new ArrayList<String>();
		
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
		
		name = "";
		for (int i = 0; i < stages.size(); i++) {
			String s = stages.get(i);
			if (s.equals(""))
				s = trans.get("Rocket.motorCount.noStageMotors");
			if (i == 0)
				name = name + s;
			else
				name = name + "; " + s;
		}
		return name;
	}
	
	
	
}
