/*
 * RocksimHandler.java
 *
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.HashMap;

import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;

import org.xml.sax.SAXException;

/**
 * This class is a Sax element handler for Rocksim version 9 design files.  It parses the Rocksim file (typically
 * a .rkt extension) and creates corresponding OpenRocket components.  This is a best effort approach and may not
 * be an exact replica.
 * <p/>
 * Limitations: Rocksim flight simulations are not imported; tube fins are not supported; Rocksim 'pods' are not supported.
 */
public class RocksimHandler extends AbstractElementHandler {
	
	/**
	 * The main content handler.
	 */
	private RocksimContentHandler handler = null;
	
	private final DocumentLoadingContext context;
	
	public RocksimHandler(DocumentLoadingContext context) {
		super();
		this.context = context;
	}
	
	/**
	 * Return the OpenRocketDocument read from the file, or <code>null</code> if a document
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
		if (!element.equals("RockSimDocument")) {
			warnings.add(Warning.fromString("Unknown element " + element + ", ignoring."));
			return null;
		}
		
		// Check for first call
		if (handler != null) {
			warnings.add(Warning.fromString("Multiple document elements found, ignoring later "
					+ "ones."));
			return null;
		}
		
		handler = new RocksimContentHandler(context);
		return handler;
	}
	
}

/**
 * Handles the content of the <DesignInformation> tag.
 */
class RocksimContentHandler extends AbstractElementHandler {
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
	
	public RocksimContentHandler(DocumentLoadingContext context) {
		super();
		this.context = context;
		this.rocket = context.getOpenRocketDocument().getRocket();
	}
	
	/**
	 * Get the OpenRocket document that has been created from parsing the Rocksim design file.
	 *
	 * @return the instantiated OpenRocketDocument
	 */
	public OpenRocketDocument getDocument() {
		return context.getOpenRocketDocument();
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes,
			WarningSet warnings) {
		if (RocksimCommonConstants.DESIGN_INFORMATION.equals(element)) {
			//The next sub-element is "RocketDesign", which is really the only thing that matters.  Rather than
			//create another handler just for that element, handle it here.
			return this;
		}
		if (RocksimCommonConstants.FILE_VERSION.equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if (RocksimCommonConstants.ROCKET_DESIGN.equals(element)) {
			return new RocketDesignHandler(context, rocket);
		}
		return null;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		/**
		 * SAX handler for Rocksim file version number.  The value is not used currently, but could be used in the future
		 * for backward/forward compatibility reasons (different lower level handlers could be called via a strategy pattern).
		 */
		if (RocksimCommonConstants.FILE_VERSION.equals(element)) {
			version = content;
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
 * A SAX handler for the high level Rocksim design.  This structure includes sub-structures for each of the stages.
 * Correct functioning of this handler is predicated on the stage count element appearing before the actual stage parts
 * structures.  If that invariant is not true, then behavior will be unpredictable.
 */
class RocketDesignHandler extends AbstractElementHandler {
	private final DocumentLoadingContext context;
	/**
	 * The parent component.
	 */
	private final RocketComponent component;
	/**
	 * The parsed stage count.  Defaults to 1.
	 */
	private int stageCount = 1;
	/**
	 * The overridden stage 1 mass.
	 */
	private double stage1Mass = 0d;
	/**
	 * The overridden stage 2 mass.
	 */
	private double stage2Mass = 0d;
	/**
	 * The overridden stage 3 mass.
	 */
	private double stage3Mass = 0d;
	/**
	 * The overridden stage 1 Cg.
	 */
	private double stage1CG = 0d;
	/**
	 * The overridden stage 2 Cg.
	 */
	private double stage2CG = 0d;
	/**
	 * The overridden stage 3 Cg.
	 */
	private double stage3CG = 0d;
	
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
		 * In Rocksim stages are from the top down, so a single stage rocket is actually stage '3'.  A 2-stage
		 * rocket defines stage '2' as the initial booster with stage '3' sitting atop it.  And so on.
		 */
		if ("Stage3Parts".equals(element)) {
			final Stage stage = new Stage();
			if (stage3Mass > 0.0d) {
				stage.setMassOverridden(true);
				stage.setOverrideSubcomponents(true); //Rocksim does not support this type of override
				stage.setOverrideMass(stage3Mass);
			}
			if (stage3CG > 0.0d) {
				stage.setCGOverridden(true);
				stage.setOverrideSubcomponents(true); //Rocksim does not support this type of override
				stage.setOverrideCGX(stage3CG);
			}
			component.addChild(stage);
			return new StageHandler(context, stage);
		}
		if ("Stage2Parts".equals(element)) {
			if (stageCount >= 2) {
				final Stage stage = new Stage();
				if (stage2Mass > 0.0d) {
					stage.setMassOverridden(true);
					stage.setOverrideSubcomponents(true); //Rocksim does not support this type of override
					stage.setOverrideMass(stage2Mass);
				}
				if (stage2CG > 0.0d) {
					stage.setCGOverridden(true);
					stage.setOverrideSubcomponents(true); //Rocksim does not support this type of override
					stage.setOverrideCGX(stage2CG);
				}
				component.addChild(stage);
				return new StageHandler(context, stage);
			}
		}
		if ("Stage1Parts".equals(element)) {
			if (stageCount == 3) {
				final Stage stage = new Stage();
				if (stage1Mass > 0.0d) {
					stage.setMassOverridden(true);
					stage.setOverrideSubcomponents(true); //Rocksim does not support this type of override
					stage.setOverrideMass(stage1Mass);
				}
				if (stage1CG > 0.0d) {
					stage.setCGOverridden(true);
					stage.setOverrideSubcomponents(true); //Rocksim does not support this type of override
					stage.setOverrideCGX(stage1CG);
				}
				component.addChild(stage);
				return new StageHandler(context, stage);
			}
		}
		if (RocksimCommonConstants.NAME.equals(element)) {
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
		if ("Stage2CGAlone".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		if ("Stage1CGAlone".equals(element)) {
			return PlainTextHandler.INSTANCE;
		}
		return null;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		try {
			if (RocksimCommonConstants.NAME.equals(element)) {
				component.setName(content);
			}
			if ("StageCount".equals(element)) {
				stageCount = Integer.parseInt(content);
			}
			if ("Stage3Mass".equals(element)) {
				stage3Mass = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if ("Stage2Mass".equals(element)) {
				stage2Mass = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if ("Stage1Mass".equals(element)) {
				stage1Mass = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if ("Stage3CG".equals(element)) {
				stage3CG = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if ("Stage2CGAlone".equals(element)) {
				stage2CG = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if ("Stage1CGAlone".equals(element)) {
				stage1CG = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
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
		if (RocksimCommonConstants.NOSE_CONE.equals(element)) {
			return new NoseConeHandler(context, component, warnings);
		}
		if (RocksimCommonConstants.BODY_TUBE.equals(element)) {
			return new BodyTubeHandler(context, component, warnings);
		}
		if (RocksimCommonConstants.TRANSITION.equals(element)) {
			return new TransitionHandler(context, component, warnings);
		}
		return null;
	}
}
