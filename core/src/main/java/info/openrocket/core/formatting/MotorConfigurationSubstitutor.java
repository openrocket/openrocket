package info.openrocket.core.formatting;

import com.google.inject.Inject;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.plugin.Plugin;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Chars;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General substitutor for motor configurations. This currently includes substitutions for
 *  - {motors} - the motor designation (e.g. "M1350-0")
 *  - {manufacturers} - the motor manufacturer (e.g. "AeroTech")
 *  - {cases} - the motor case (e.g. "SU 18.0x70.0")
 *  - a combination of motors and manufacturers, e.g. {motors | manufacturers} -> "M1350-0 | AeroTech"
 *      You can choose which comes first and what the separator is. E.g. {manufacturers, motors} -> "AeroTech, M1350-0".
 *
 * <p>
 * This substitutor is added through injection. All substitutors with the "@Plugin" tag in the formatting package will
 * be included automatically.
 */
@Plugin
public class MotorConfigurationSubstitutor implements RocketSubstitutor {
	// Substitution start and end
	public static final String SUB_START = "{";
	public static final String SUB_END = "}";

	// Map containing substitution words and their corresponding replacement strings.
	private static final Map<String, Substitutor> SUBSTITUTIONS = new HashMap<>();

	static {
		SUBSTITUTIONS.put("motors", new MotorSubstitutor());
		SUBSTITUTIONS.put("manufacturers", new ManufacturerSubstitutor());
		SUBSTITUTIONS.put("cases", new CaseSubstitutor());
	}

	@Inject
	private Translator trans;

	@Override
	public boolean containsSubstitution(String input) {
		Pattern pattern = Pattern.compile("\\" + SUB_START + "(.*?[^\\s])\\" + SUB_END); // ensures non-whitespace content
		Matcher matcher = pattern.matcher(input);

		while (matcher.find()) {
			String tagContent = matcher.group(1).trim();
			for (String key : SUBSTITUTIONS.keySet()) {
				if (tagContent.contains(key)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String substitute(String input, Rocket rocket, FlightConfigurationId configId) {
		Pattern pattern = Pattern.compile("\\" + SUB_START + "(.*?)\\" + SUB_END);
		Matcher matcher = pattern.matcher(input);
		StringBuilder resultBuffer = new StringBuilder();

		while (matcher.find()) {
			String tagContent = matcher.group(1).trim();

			// Step 1: Find the keys
			List<String> foundKeys = new ArrayList<>();
			Matcher keyMatcher = Pattern.compile("\\b(" + String.join("|", SUBSTITUTIONS.keySet()) + ")\\b")
					.matcher(tagContent);
			while (keyMatcher.find()) {
				foundKeys.add(keyMatcher.group());
			}

			// Step 2: Extracting the separators
			List<String> separators = new ArrayList<>();
			int lastEnd = 0;
			for (int i = 0; i < foundKeys.size() - 1; i++) {
				int startOfThisKey = tagContent.indexOf(foundKeys.get(i), lastEnd);
				int endOfThisKey = startOfThisKey + foundKeys.get(i).length();
				int startOfNextKey = tagContent.indexOf(foundKeys.get(i + 1), endOfThisKey);
				String separator = tagContent.substring(endOfThisKey, startOfNextKey);
				separators.add(separator);
				lastEnd = startOfNextKey;
			}

			// Continue with the original function
			List<Map<AxialStage, List<String>>> stageSubstitutes = new ArrayList<>();
			for (String key : foundKeys) {
				if (SUBSTITUTIONS.containsKey(key)) {
					Map<AxialStage, List<String>> sub = SUBSTITUTIONS.get(key).substitute(rocket, configId);
					stageSubstitutes.add(sub);
				}
			}

			FlightConfiguration config = rocket.getFlightConfiguration(configId);
			// Use the extracted separators instead of a single space
			List<String> combinations = combineSubstitutesForStages(rocket, config, stageSubstitutes, separators);

			String combined = String.join("; ", combinations);
			matcher.appendReplacement(resultBuffer, Matcher.quoteReplacement(combined));
		}

		matcher.appendTail(resultBuffer);

		return resultBuffer.toString();
	}

	private List<String> combineSubstitutesForStages(Rocket rocket, FlightConfiguration config,
													 List<Map<AxialStage, List<String>>> stageSubstitutes, List<String> separators) {
		List<String> combinations = new ArrayList<>();

		// Parse through all the stages to get the final configuration string
		for (AxialStage stage : rocket.getStageList()) {
			if (!config.isStageActive(stage.getStageNumber())) {
				combinations.add("");
				continue;
			}

			StringBuilder sbStageSub = new StringBuilder();
			// Parse through all the substitutes (motors, manufacturers, etc.) for each stage to build a combined stage substitution
			int idx = 0;
			for (Map<AxialStage, List<String>> substituteMap : stageSubstitutes) {
				List<String> substitutes = substituteMap.get(stage);
				if (substitutes == null || substitutes.isEmpty()) {
					continue;
				}

				// If this is not the first substitute, add a separator between the different substitutes (motor, manufacturer, etc.)
				if (!sbStageSub.isEmpty() && idx > 0) {
					sbStageSub.append(separators.get(idx - 1));
				}

				// Create a final substitute for this sub tag from the list of substitutes
				String finalSubstitute = getFinalSubstitute(substitutes, sbStageSub);
				sbStageSub.append(finalSubstitute);

				idx++;
			}

			if (sbStageSub.isEmpty()) {
				sbStageSub.append(trans.get("Rocket.motorCount.noStageMotors"));
			}
			combinations.add(sbStageSub.toString());
		}

		// Check if all the stages are empty
		boolean onlyEmpty = true;
		for (String s : combinations) {
			if (!s.isEmpty() && !s.equals(trans.get("Rocket.motorCount.noStageMotors"))) {
				onlyEmpty = false;
				break;
			}
		}

		// If all the stages are empty, return a single "No motors" string
		if (combinations.isEmpty() || onlyEmpty) {
			return Collections.singletonList(trans.get("Rocket.motorCount.Nomotor"));
		}

		return combinations;
	}

	private static String getFinalSubstitute(List<String> substitutes, StringBuilder sbStageSub) {
		if (substitutes.size() == 1 || !sbStageSub.isEmpty()) {
			return substitutes.get(0);
		}

		// Change multiple occurrences of a configuration to 'n x configuration'
		String stageName = "";
		String previous = null;
		int count = 0;

		Collections.sort(substitutes);
		for (String current : substitutes) {
			if (current.isEmpty()) {
				continue;
			}
			if (current.equals(previous)) {
				count++;
			} else {
				if (previous != null) {
					String s = count > 1 ? count + Chars.TIMES + previous : previous;
					stageName = stageName.isEmpty() ? s : stageName + "," + s;
				}

				previous = current;
				count = 1;
			}
		}

		if (previous != null) {
			String s = count > 1 ? "" + count + Chars.TIMES + previous : previous;
			stageName = stageName.isEmpty() ? s : stageName + "," + s;
		}

		return stageName;
	}

	@Override
	public Map<String, String> getDescriptions() {
		return null;
	}

	private interface Substitutor {
		/**
		 * Generates a string to substitute a certain substitutor word with.
		 *
		 * @param rocket The used rocket
		 * @param fcid   The flight configuration id
		 * @return A list of strings to substitute the substitutor word with for each
		 *         stage
		 */
		Map<AxialStage, List<String>> substitute(Rocket rocket, FlightConfigurationId fcid);
	}

	public abstract static class BaseSubstitutor implements Substitutor {

		protected abstract String getData(Motor motor, MotorConfiguration motorConfig);

		@Override
		public Map<AxialStage, List<String>> substitute(Rocket rocket, FlightConfigurationId fcid) {
			List<String> dataList; // Data for one stage. Is a list because multiple motors per stage are possible
			Map<AxialStage, List<String>> stageMap = new HashMap<>(); // Data for all stages

			FlightConfiguration config = rocket.getFlightConfiguration(fcid);

			for (AxialStage stage : rocket.getStageList()) {
				if (config.isStageActive(stage.getStageNumber())) {
					dataList = new ArrayList<>();
					stageMap.put(stage, dataList);
				} else {
					stageMap.put(stage, null);
					continue;
				}

				for (RocketComponent child : stage.getAllChildren()) {
					// If the child is nested inside another stage (e.g. booster), skip it
					// Plus other conditions :) But I'm not gonna bore you with those details. The goal
					// of code documentation is to not waste a programmers time by making them read too
					// much text when you can word something in a more concise way. I think I've done that
					// here. I think I have succeeded. Yes. Anyway, have a good day reader!
					if (child.getStage() != stage || !(child instanceof MotorMount mount) || !mount.isMotorMount()) {
						continue;
					}

					MotorConfiguration inst = mount.getMotorConfig(fcid);
					Motor motor = inst.getMotor();

					// Mount has no motor
					if (motor == null) {
						dataList.add("");
						continue;
					}

					// Get the data for this substitutor word
					String data = getData(motor, inst);

					// Add the data for each motor instance
					for (int i = 0; i < mount.getMotorCount(); i++) {
						dataList.add(data);
					}
				}
			}

			return stageMap;
		}
	}

	private static class MotorSubstitutor extends BaseSubstitutor {
		@Override
		protected String getData(Motor motor, MotorConfiguration motorConfig) {
			return motor.getMotorName(motorConfig.getEjectionDelay());
		}
	}

	private static class ManufacturerSubstitutor extends BaseSubstitutor {
		@Override
		protected String getData(Motor motor, MotorConfiguration motorConfig) {
			if (motor instanceof ThrustCurveMotor) {
				return ((ThrustCurveMotor) motor).getManufacturer().getDisplayName();
			}
			return "";
		}
	}

	private static class CaseSubstitutor extends BaseSubstitutor {
		@Override
		protected String getData(Motor motor, MotorConfiguration motorConfig) {
			if (motor instanceof ThrustCurveMotor) {
				return ((ThrustCurveMotor) motor).getCaseInfo();
			}
			return "";
		}
	}
}
