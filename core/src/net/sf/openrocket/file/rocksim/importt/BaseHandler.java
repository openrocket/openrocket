/*
 * BaseHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimDensityType;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;

import org.xml.sax.SAXException;

/**
 * An abstract base class that handles common parsing.  All Rocksim component handlers are subclassed from here.
 *
 * @param <C> the specific RocketComponent subtype for which the concrete handler can create
 */
public abstract class BaseHandler<C extends RocketComponent> extends AbstractElementHandler {
	
	/**
	 * Prepend rocksim materials.
	 */
	public static final String ROCKSIM_MATERIAL_PREFIX = "RS: ";
	/**
	 * The overridden mass.
	 */
	private Double mass = 0d;
	/**
	 * The overridden Cg.
	 */
	private Double cg = 0d;
	/**
	 * The density of the material in the component.
	 */
	private Double density = 0d;
	/**
	 * The internal Rocksim density type.
	 */
	private RocksimDensityType densityType = RocksimDensityType.ROCKSIM_BULK;
	
	/**
	 * The material name.
	 */
	private String materialName = "";
	
	protected final DocumentLoadingContext context;
	private final RockSimAppearanceBuilder appearanceBuilder;
	
	public BaseHandler(DocumentLoadingContext context) {
		this.context = context;
		appearanceBuilder = new RockSimAppearanceBuilder(context);
	}
	
	/**
	 * The SAX method called when the closing element tag is reached.
	 *
	 * @param element    the element name.
	 * @param attributes attributes of the element.
	 * @param content    the textual content of the element.
	 * @param warnings   the warning set to store warnings in.
	 *
	 * @throws SAXException
	 */
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		final C component = getComponent();
		try {
			if (RocksimCommonConstants.NAME.equals(element)) {
				component.setName(content);
			}
			if (RocksimCommonConstants.KNOWN_MASS.equals(element)) {
				mass = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
			}
			if (RocksimCommonConstants.DENSITY.equals(element)) {
				density = Math.max(0d, Double.parseDouble(content));
			}
			if (RocksimCommonConstants.KNOWN_CG.equals(element)) {
				cg = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
			}
			if (RocksimCommonConstants.USE_KNOWN_CG.equals(element)) { //Rocksim sets UseKnownCG to true to control the override of both cg and mass
				boolean override = "1".equals(content);
				setOverride(component, override, mass, cg);
			}
			if (RocksimCommonConstants.DENSITY_TYPE.equals(element)) {
				densityType = RocksimDensityType.fromCode(Integer.parseInt(content));
			}
			
			appearanceBuilder.processElement(element, content, warnings);
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		/* Because of the order of XML elements in Rocksim, not all information is known at the time it really needs
		   to be acted upon.  So we keep temporary instance variables to be used here at the end of the parsing.
		 */
		density = computeDensity(densityType, density);
		RocketComponent component = getComponent();
		
		//TODO - What RockSim components can have Appearances?
		if (component instanceof ExternalComponent) {
			//If a symmetric component is set to PreventSeam then it is repeated
			//twice as many times around the rocket.
			if (component instanceof SymmetricComponent && appearanceBuilder.isPreventSeam()) {
				appearanceBuilder.setScaleU(appearanceBuilder.getScaleU() * 2);
			}
			component.setAppearance(appearanceBuilder.getAppearance());
		}
		
		updateComponentMaterial(component, materialName, getMaterialType(), density);
	}
	
	/**
	 * Compute the density.  Rocksim does strange things with densities.  For some streamer material it's in cubic,
	 * rather than square, units.  In those cases it needs to be converted to an appropriate SURFACE material density.
	 * Some G10 fiberglass materials are in cubic units, other G10 fiberglass is in square units.  And due to a Rocksim
	 * bug, some densities are 0 when they clearly should not be.
	 * <p/>
	 * This may be overridden for specific component density computations.
	 *
	 * @param type       the rocksim density
	 * @param rawDensity the density as specified in the Rocksim design file
	 *
	 * @return a value in OpenRocket SURFACE density units
	 */
	protected double computeDensity(RocksimDensityType type, double rawDensity) {
		return rawDensity / type.asOpenRocket();
	}
	
	/**
	 * If the Rocksim component does not override the mass, then create a Material based upon the density defined for
	 * that component.  This *should* result in a consistent representation of Cg between Rocksim and OpenRocket.
	 *
	 * @param component       the component
	 * @param type            the type of the material
	 * @param density         the density in g/cm^3
	 * @param definedMaterial the material that is currently defined on the component; used only to get the name as it
	 *                        appears in Rocksim
	 */
	public static void updateComponentMaterial(RocketComponent component, String definedMaterial, Material.Type type,
			double density) {
		if (definedMaterial != null) {
			Material custom = createCustomMaterial(type, definedMaterial, density);
			setMaterial(component, custom);
		}
	}
	
	/**
	 * Override the mass and Cg of the component.
	 *
	 * @param component the component
	 * @param override  true if any override should happen
	 * @param mass      the override mass
	 * @param cg        the override cg
	 */
	public static void setOverride(RocketComponent component, boolean override, double mass, double cg) {
		if (override) {
			component.setCGOverridden(override);
			component.setMassOverridden(override);
			component.setOverrideSubcomponents(false); //Rocksim does not support this type of override
			component.setOverrideMass(mass);
			component.setOverrideCGX(cg);
		}
	}
	
	/**
	 * Get the component this handler is working upon.
	 *
	 * @return a component
	 */
	protected abstract C getComponent();
	
	/**
	 * Get the required type of material for this component.
	 *
	 * @return the required material type
	 */
	protected abstract Material.Type getMaterialType();
	
	/**
	 * Some CG positions in Rocksim do not correspond to the CG position reference in OpenRocket.
	 *
	 * @param theCG the CG value to really use when overriding CG on the OpenRocket component
	 */
	protected void setCG(double theCG) {
		cg = theCG;
	}
	
	/**
	 * Set the material name as specified in the Rocksim design file.
	 *
	 * @param content the material name
	 */
	protected void setMaterialName(String content) {
		materialName = content;
	}
	
	/**
	 * Get the Rocksim enum of the component's density type.
	 *
	 * @return a Rocksim density type
	 */
	protected RocksimDensityType getDensityType() {
		return densityType;
	}
	
	/**
	 * Add child to parent only if the child is compatible.  Otherwise add to warning set.
	 *
	 * @param parent   the parent component
	 * @param child    the child component
	 * @param warnings the warning set
	 *
	 * @return true if the child is compatible with parent
	 */
	protected static boolean isCompatible(RocketComponent parent, Class<? extends RocketComponent> child, WarningSet warnings) {
		return isCompatible(parent, child, warnings, false);
	}
	
	/**
	 * Add child to parent only if the child is compatible.  Otherwise add to warning set.
	 *
	 * @param parent   the parent component
	 * @param child    the child component
	 * @param warnings the warning set
	 * @param suppressWarnings suppress warnings, just return the boolean
	 *
	 * @return true if the child is compatible with parent
	 */
	protected static boolean isCompatible(RocketComponent parent, Class<? extends RocketComponent> child,
			WarningSet warnings,
			boolean suppressWarnings) {
		if (!parent.isCompatible(child)) {
			if (!suppressWarnings) {
				warnings.add(child.getName() + " can not be attached to "
						+ parent.getComponentName() + ", ignoring component.");
			}
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Create a custom material based on the density.
	 *
	 * @param type    the type of the material
	 * @param name    the name of the component
	 * @param density the density
	 *
	 * @return a Material instance
	 */
	public static Material createCustomMaterial(Material.Type type, String name, double density) {
		return Databases.findMaterial(type, name, density);
	}
	
	/**
	 * Set the material onto an instance of RocketComponent.  This is done because only some subtypes of RocketComponent
	 * have the setMaterial method.  Unfortunately the supertype cannot be used.
	 *
	 * @param component the component who's material is to be set
	 * @param material  the material to be set on the component (defined by getComponent())
	 */
	private static void setMaterial(RocketComponent component, Material material) {
		try {
			final Method method = getMethod(component, "setMaterial", new Class[] { Material.class });
			if (method != null) {
				method.invoke(component, material);
			}
		} catch (IllegalAccessException ignored) {
		} catch (InvocationTargetException ignored) {
		}
	}
	
	/**
	 * Find a method by name and argument list.
	 *
	 * @param component the component who's material is to be set
	 * @param name      the method name
	 * @param args      the class types of the parameters
	 *
	 * @return the Method instance, or null
	 */
	private static Method getMethod(RocketComponent component, String name, Class[] args) {
		Method method = null;
		try {
			method = component.getClass().getMethod(name, args);
		} catch (NoSuchMethodException ignored) {
		}
		return method;
	}
	
}
