/*
 * RockSimHandler.java
 *
 */
package info.openrocket.core.file.rocksim.importt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.DocumentLoadingContext;
import info.openrocket.core.file.rocksim.RockSimCommonConstants;
import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.file.simplesax.ElementHandler;
import info.openrocket.core.file.simplesax.PlainTextHandler;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.AxialStage;

import org.xml.sax.SAXException;

/**
 * This class is a Sax element handler for Rocksim version 9 design files. It
 * parses the Rocksim file (typically
 * a .rkt extension) and creates corresponding OpenRocket components. This is a
 * best effort approach and may not
 * be an exact replica.
 * <p/>
 * Limitations: tube fins are not supported; Rocksim 'pods' are not supported.
 */
public class RockSimHandler extends AbstractElementHandler {

	/**
	 * The main content handler.
	 */
	private RockSimContentHandler handler = null;

	private final DocumentLoadingContext context;

	public RockSimHandler(DocumentLoadingContext context) {
		super();
		this.context = context;
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
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {

		// Check for unknown elements
		if (!RockSimCommonConstants.ROCK_SIM_DOCUMENT.equals(element)) {
			warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
			return null;
		}

		// Check for first call
		if (handler != null) {
			warnings.add(Warning.fromString("Multiple document elements found, ignoring later "
					+ "ones."));
			return null;
		}

		handler = new RockSimContentHandler(context);
		return handler;
	}

}

/**
 * Handles the content of the <DesignInformation> tag.
 */
class RockSimContentHandler extends AbstractElementHandler {
	/**
	 * The DocumentLoadingContext
	 */
	private final DocumentLoadingContext context;

	/**
	 * The top-level component, from which all child components are added.
	 */
	private final Rocket rocket;

	/**
	 * The rocksim file version.
	 */
	private String version;

	/**
	 * The newest RockSim file version, as written by RockSim 11.
	 */
	private static final int NEWEST_FILE_VERSION = 4;

	public RockSimContentHandler(DocumentLoadingContext context) {
		super();
		this.context = context;
		this.rocket = context.getOpenRocketDocument().getRocket();
	}

	/**
	 * Get the OpenRocket document that has been created from parsing the Rocksim
	 * design file.
	 *
	 * @return the instantiated OpenRocketDocument
	 */
	public OpenRocketDocument getDocument() {
		return context.getOpenRocketDocument();
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		if (RockSimCommonConstants.DESIGN_INFORMATION.equals(element)) {
			// The next sub-element is "RocketDesign", which is really the only thing that
			// matters. Rather than
			// create another handler just for that element, handle it here.
			return this;
		}
		if (RockSimCommonConstants.FILE_VERSION.equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if (RockSimCommonConstants.ROCKET_DESIGN.equals(element)) {
			return new RocketDesignHandler(context, rocket);
		}
		if (RockSimCommonConstants.SIMULATION_RESULTS_LIST.equals(element)) {
			return new SimulationResultsListHandler(context, rocket);
		}
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		/**
		 * SAX handler for Rocksim file version number. The value is not used currently,
		 * but could be used in the future
		 * for backward/forward compatibility reasons (different lower level handlers
		 * could be called via a strategy pattern).
		 */
		if (RockSimCommonConstants.FILE_VERSION.equals(element)) {
			version = content;
			try {
				if (Integer.parseInt(content.trim()) > NEWEST_FILE_VERSION) {
					warnings.add("RockSim file version " + content.trim() + " is newer than the supported version "
							+ NEWEST_FILE_VERSION + "; some data may not be imported.");
				}
			} catch (NumberFormatException nfe) {
				warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
			}
		}
	}

	/**
	 * Answer the file version.
	 *
	 * @return the version of the Rocksim design file
	 */
	public String getVersion() {
		return version;
	}
}

/**
 * A SAX handler for the high level Rocksim design. This structure includes
 * sub-structures for each of the stages.
 * Correct functioning of this handler is predicated on the stage count element
 * appearing before the actual stage parts
 * structures. If that invariant is not true, then behavior will be
 * unpredictable.
 */
class RocketDesignHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	/**
	 * The parent component.
	 */
	private final RocketComponent component;
	/**
	 * The parsed stage count. Defaults to 1.
	 */
	private int stageCount = 1;
	/**
	 * The overridden stage 1 mass.
	 */
	private double stage1Mass = 0.0d;
	/**
	 * The overridden stage 2 mass.
	 */
	private double stage2Mass = 0.0d;
	/**
	 * The overridden stage 3 mass.
	 */
	private double stage3Mass = 0.0d;
	/**
	 * The overridden CG of stage 3 (the sustainer) alone, measured from the nose tip.
	 */
	private double stage3CG = 0.0d;
	/**
	 * The overridden CG of stages 3 and 2 combined, measured from the nose tip.
	 */
	private double stage32CG = 0.0d;
	/**
	 * The overridden CG of stages 3, 2 and 1 combined, measured from the nose tip.
	 */
	private double stage321CG = 0.0d;
	/**
	 * Whether RockSim uses the stage mass and CG overrides.
	 */
	private boolean useKnownMass = false;
	/**
	 * Whether RockSim calculates the Cd (true) or uses the fixed Cd (false).
	 */
	private boolean calculateCD = true;
	/**
	 * RockSim's fixed Cd of the sustainer.
	 */
	private double fixedCD = 0.0d;
	/**
	 * The imported stages, from the top (RockSim stage 3) down.
	 */
	private final List<AxialStage> stages = new ArrayList<>();

	/**
	 * Constructor.
	 *
	 * @param c the parent component
	 */
	public RocketDesignHandler(DocumentLoadingContext context, RocketComponent c) {
		this.context = context;
		component = c;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		/**
		 * In Rocksim stages are from the top down, so a single stage rocket is actually
		 * stage '3'. A 2-stage
		 * rocket defines stage '2' as the initial booster with stage '3' sitting atop
		 * it. And so on.
		 */
		if ("Stage3Parts".equals(element)
				|| ("Stage2Parts".equals(element) && stageCount >= 2)
				|| ("Stage1Parts".equals(element) && stageCount == 3)) {
			final AxialStage stage = new AxialStage();
			component.addChild(stage);
			stages.add(stage);
			return new StageHandler(context, stage);
		}
		if (RockSimCommonConstants.NAME.equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("StageCount".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage3Mass".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage2Mass".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage1Mass".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage3CG".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage32CG".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage321CG".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("UseKnownMass".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if (RockSimCommonConstants.CALCULATE_CD.equals(element) || RockSimCommonConstants.FIXED_CD.equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if (RockSimCommonConstants.COMMENTS.equals(element) || RockSimCommonConstants.DESIGNER.equals(element)
				|| RockSimCommonConstants.REVISIONS.equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		return null;
	}

	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		try {
			if (RockSimCommonConstants.NAME.equals(element)) {
				component.setName(content);
			}
			if (RockSimCommonConstants.CALCULATE_CD.equals(element)) {
				calculateCD = Integer.parseInt(content.trim()) != 0;
			}
			if (RockSimCommonConstants.FIXED_CD.equals(element)) {
				fixedCD = Double.parseDouble(content);
			}
			if (RockSimCommonConstants.COMMENTS.equals(element)) {
				component.setComment(content);
			}
			if (RockSimCommonConstants.DESIGNER.equals(element) && component instanceof Rocket) {
				((Rocket) component).setDesigner(content);
			}
			if (RockSimCommonConstants.REVISIONS.equals(element) && component instanceof Rocket) {
				((Rocket) component).setRevision(content);
			}
			if ("StageCount".equals(element)) {
				stageCount = Integer.parseInt(content);
			}
			if ("Stage3Mass".equals(element)) {
				stage3Mass = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if ("Stage2Mass".equals(element)) {
				stage2Mass = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if ("Stage1Mass".equals(element)) {
				stage1Mass = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if ("Stage3CG".equals(element)) {
				stage3CG = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if ("Stage32CG".equals(element)) {
				stage32CG = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if ("Stage321CG".equals(element)) {
				stage321CG = Double.parseDouble(content) / RockSimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if ("UseKnownMass".equals(element)) {
				useKnownMass = Integer.parseInt(content.trim()) != 0;
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}

	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		applyStageOverrides();
		applyFixedCD(warnings);
	}

	/**
	 * When RockSim does not calculate the Cd, it uses the fixed Cd of the design instead. For a single-stage design,
	 * this is the sustainer's Cd, which becomes the stage's Cd override.
	 */
	private void applyFixedCD(WarningSet warnings) {
		if (calculateCD || fixedCD <= 0 || stages.isEmpty()) {
			return;
		}
		if (stages.size() > 1) {
			warnings.add("The fixed Cd of multi-stage RockSim designs is not imported; OpenRocket calculates the Cd.");
			return;
		}
		AxialStage sustainer = stages.get(0);
		sustainer.setCDOverridden(true);
		sustainer.setSubcomponentsOverriddenCD(true);
		sustainer.setOverrideCD(fixedCD);
	}

	/**
	 * RockSim only uses the stage mass and CG overrides when UseKnownMass is set. The mass overrides are per stage,
	 * but the CG overrides are those of the stacked stages (sustainer alone, sustainer and first booster, all
	 * stages), measured from the nose tip. The CG of each stage is derived from the CGs of the stacks.
	 */
	private void applyStageOverrides() {
		if (!useKnownMass) {
			return;
		}

		final double[] stageMasses = { stage3Mass, stage2Mass, stage1Mass };
		final double[] stackCGs = { stage3CG, stage32CG, stage321CG };
		double stackMass = 0;
		double stackMoment = 0;
		boolean stackCGKnown = true;
		for (int i = 0; i < stages.size(); i++) {
			final AxialStage stage = stages.get(i);
			final double mass = stageMasses[i];
			if (mass <= 0) {
				// Without the stage mass, the CGs of this and the lower stages cannot be derived
				stackCGKnown = false;
				continue;
			}
			stage.setMassOverridden(true);
			stage.setSubcomponentsOverriddenMass(true); // Rocksim does not support this type of override
			stage.setOverrideMass(mass);

			stackCGKnown &= stackCGs[i] > 0;
			if (stackCGKnown) {
				double stageCG = (stackCGs[i] * (stackMass + mass) - stackMoment) / mass;
				stage.setCGOverridden(true);
				stage.setSubcomponentsOverriddenCG(true); // Rocksim does not support this type of override
				stage.setOverrideCGX(stageCG - stage.getComponentLocations()[0].getX());
			}
			stackMass += mass;
			stackMoment = stackCGs[i] * stackMass;
		}
	}

}

/**
 * A SAX handler for a Rocksim stage.
 */
class StageHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	/**
	 * The parent OpenRocket component.
	 */
	private final RocketComponent component;

	/**
	 * Constructor.
	 *
	 * @param c the parent component
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public StageHandler(DocumentLoadingContext context, RocketComponent c) throws IllegalArgumentException {
		if (c == null) {
			throw new IllegalArgumentException("The stage component may not be null.");
		}
		this.context = context;
		component = c;
	}

	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		if (RockSimCommonConstants.NOSE_CONE.equals(element)) {
			return new NoseConeHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.BODY_TUBE.equals(element)) {
			return new BodyTubeHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.TRANSITION.equals(element)) {
			return new TransitionHandler(context, component, warnings);
		}
		if (RockSimCommonConstants.SUBASSEMBLY.equals(element)) {
			return new SubAssemblyHandler(context, component);
		}
		return null;
	}
}
