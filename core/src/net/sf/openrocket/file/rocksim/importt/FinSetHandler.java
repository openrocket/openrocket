/*
 * FinSetHandler.java
 */
package net.sf.openrocket.file.rocksim.importt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.file.rocksim.RocksimFinishCode;
import net.sf.openrocket.file.rocksim.RocksimLocationMode;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.util.Coordinate;

import org.xml.sax.SAXException;

/**
 * A SAX handler for Rocksim fin sets.  Because the type of fin may not be known first (in Rocksim file format, the fin
 * shape type is in the middle of the XML structure), and because we're using SAX not DOM, all of the fin
 * characteristics are kept here until the closing FinSet tag. At that point, <code>asOpenRocket</code> method is called
 * to construct the corresponding OpenRocket FinSet.
 */
class FinSetHandler extends AbstractElementHandler {
	/**
	 * The parent component.
	 */
	private final RocketComponent component;
	
	/**
	 * The name of the fin.
	 */
	private String name;
	/**
	 * The Rocksim fin shape code.
	 */
	private int shapeCode;
	/**
	 * The location of the fin on its parent.
	 */
	private double location = 0.0d;
	/**
	 * The OpenRocket Position which gives the absolute/relative positioning for location.
	 */
	private RocketComponent.Position position;
	/**
	 * The number of fins in this fin set.
	 */
	private int finCount;
	/**
	 * The length of the root chord.
	 */
	private double rootChord = 0.0d;
	/**
	 * The length of the tip chord.
	 */
	private double tipChord = 0.0d;
	/**
	 * The length of the mid-chord (aka height).
	 */
	private double midChordLen = 0.0d;
	/**
	 * The distance of the leading edge from root to top.
	 */
	private double sweepDistance = 0.0d;
	/**
	 * The angle the fins have been rotated from the y-axis, if looking down the tube, in radians.
	 */
	private double radialAngle = 0.0d;
	/**
	 * The thickness of the fins.
	 */
	private double thickness;
	/**
	 * The finish of the fins.
	 */
	private ExternalComponent.Finish finish;
	/**
	 * The shape of the tip.
	 */
	private int tipShapeCode;
	/**
	 * The length of the TTW tab.
	 */
	private double tabLength = 0.0d;
	/**
	 * The depth of the TTW tab.
	 */
	private double tabDepth = 0.0d;
	/**
	 * The offset of the tab, from the front of the fin.
	 */
	private double taboffset = 0.0d;
	/**
	 * The elliptical semi-span (height).
	 */
	private double semiSpan;
	/**
	 * The list of custom points.
	 */
	private String pointList;
	/**
	 * Override the Cg and mass.
	 */
	private boolean override = false;
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
	 * The material name.
	 */
	private String materialName = "";
	/**
	 * The Rocksim calculated mass.
	 */
	private Double calcMass = 0d;
	/**
	 * The Rocksim calculated cg.
	 */
	private Double calcCg = 0d;
	
	private final RockSimAppearanceBuilder appearanceBuilder;
	
	/**
	 * Constructor.
	 *
	 * @param c the parent
	 *
	 * @throws IllegalArgumentException thrown if <code>c</code> is null
	 */
	public FinSetHandler(DocumentLoadingContext context, RocketComponent c) throws IllegalArgumentException {
		if (c == null) {
			throw new IllegalArgumentException("The parent component of a fin set may not be null.");
		}
		appearanceBuilder = new RockSimAppearanceBuilder(context);
		component = c;
	}
	
	@Override
	public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) {
		return PlainTextHandler.INSTANCE;
	}
	
	@Override
	public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
			throws SAXException {
		try {
			if (RocksimCommonConstants.NAME.equals(element)) {
				name = content;
			}
			if (RocksimCommonConstants.MATERIAL.equals(element)) {
				materialName = content;
			}
			if (RocksimCommonConstants.FINISH_CODE.equals(element)) {
				finish = RocksimFinishCode.fromCode(Integer.parseInt(content)).asOpenRocket();
			}
			if (RocksimCommonConstants.XB.equals(element)) {
				location = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.LOCATION_MODE.equals(element)) {
				position = RocksimLocationMode.fromCode(Integer.parseInt(content)).asOpenRocket();
			}
			if (RocksimCommonConstants.FIN_COUNT.equals(element)) {
				finCount = Integer.parseInt(content);
			}
			if (RocksimCommonConstants.ROOT_CHORD.equals(element)) {
				rootChord = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.TIP_CHORD.equals(element)) {
				tipChord = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.SEMI_SPAN.equals(element)) {
				semiSpan = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if ("MidChordLen".equals(element)) {
				midChordLen = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.SWEEP_DISTANCE.equals(element)) {
				sweepDistance = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.THICKNESS.equals(element)) {
				thickness = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.TIP_SHAPE_CODE.equals(element)) {
				tipShapeCode = Integer.parseInt(content);
			}
			if (RocksimCommonConstants.TAB_LENGTH.equals(element)) {
				tabLength = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.TAB_DEPTH.equals(element)) {
				tabDepth = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.TAB_OFFSET.equals(element)) {
				taboffset = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			if (RocksimCommonConstants.RADIAL_ANGLE.equals(element)) {
				radialAngle = Double.parseDouble(content);
			}
			if (RocksimCommonConstants.SHAPE_CODE.equals(element)) {
				shapeCode = Integer.parseInt(content);
			}
			if (RocksimCommonConstants.POINT_LIST.equals(element)) {
				pointList = content;
			}
			if (RocksimCommonConstants.KNOWN_MASS.equals(element)) {
				mass = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
			}
			if (RocksimCommonConstants.DENSITY.equals(element)) {
				density = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_BULK_DENSITY);
			}
			if (RocksimCommonConstants.KNOWN_CG.equals(element)) {
				cg = Math.max(0d, Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS);
			}
			if (RocksimCommonConstants.USE_KNOWN_CG.equals(element)) {
				override = "1".equals(content);
			}
			if (RocksimCommonConstants.CALC_MASS.equals(element)) {
				calcMass = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_MASS;
			}
			if (RocksimCommonConstants.CALC_CG.equals(element)) {
				calcCg = Double.parseDouble(content) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH;
			}
			
			appearanceBuilder.processElement(element, content, warnings);
		} catch (NumberFormatException nfe) {
			warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
		}
	}
	
	@Override
	public void endHandler(String element, HashMap<String, String> attributes,
			String content, WarningSet warnings) throws SAXException {
		//Create the fin set and correct for overrides and actual material densities
		final FinSet finSet = asOpenRocket(warnings);
		
		finSet.setAppearance(appearanceBuilder.getAppearance());
		
		if (component.isCompatible(finSet)) {
			BaseHandler.setOverride(finSet, override, mass, cg);
			if (!override && finSet.getCrossSection().equals(FinSet.CrossSection.AIRFOIL)) {
				//Override mass anyway.  This is done only for AIRFOIL because Rocksim does not compute different
				//mass/cg for different cross sections, but OpenRocket does.  This can lead to drastic differences
				//in mass.  To counteract that, the cross section value is retained but the mass/cg is overridden
				//with the calculated values from Rocksim.  This will best approximate the Rocksim design in OpenRocket.
				BaseHandler.setOverride(finSet, true, calcMass, calcCg);
			}
			BaseHandler.updateComponentMaterial(finSet, materialName, Material.Type.BULK, density);
			component.addChild(finSet);
		}
		else {
			warnings.add(finSet.getComponentName() + " can not be attached to "
					+ component.getComponentName() + ", ignoring component.");
		}
	}
	
	
	/**
	 * Convert the parsed Rocksim data values in this object to an instance of OpenRocket's FinSet.
	 *
	 * @param warnings the warning set to convey incompatibilities to the user
	 *
	 * @return a FinSet instance
	 */
	public FinSet asOpenRocket(WarningSet warnings) {
		FinSet result;
		
		if (shapeCode == 0) {
			//Trapezoidal
			result = new TrapezoidFinSet();
			((TrapezoidFinSet) result).setFinShape(rootChord, tipChord, sweepDistance, semiSpan, thickness);
		}
		else if (shapeCode == 1) {
			//Elliptical
			result = new EllipticalFinSet();
			((EllipticalFinSet) result).setHeight(semiSpan);
			((EllipticalFinSet) result).setLength(rootChord);
		}
		else if (shapeCode == 2) {
			
			result = new FreeformFinSet();
			try {
				((FreeformFinSet) result).setPoints(toCoordinates(pointList, warnings));
			} catch (IllegalFinPointException e) {
				warnings.add("Illegal fin point set. " + e.getMessage() + " Ignoring.");
			}
		}
		else {
			return null;
		}
		result.setThickness(thickness);
		result.setName(name);
		result.setFinCount(finCount);
		result.setFinish(finish);
		//All TTW tabs in Rocksim are relative to the front of the fin.
		result.setTabRelativePosition(FinSet.TabRelativePosition.FRONT);
		result.setTabHeight(tabDepth);
		result.setTabLength(tabLength);
		result.setTabShift(taboffset);
		result.setBaseRotation(radialAngle);
		result.setCrossSection(convertTipShapeCode(tipShapeCode));
		result.setRelativePosition(position);
		PositionDependentHandler.setLocation(result, position, location);
		return result;
		
	}
	
	/**
	 * Convert a Rocksim string that represents fin plan points into an array of OpenRocket coordinates.
	 *
	 * @param pointList a comma and pipe delimited string of X,Y coordinates from Rocksim.  This is of the format:
	 *                  <pre>x0,y0|x1,y1|x2,y2|... </pre>
	 * @param warnings  the warning set to convey incompatibilities to the user
	 *
	 * @return an array of OpenRocket Coordinates
	 */
	private Coordinate[] toCoordinates(String pointList, WarningSet warnings) {
		List<Coordinate> result = new ArrayList<Coordinate>();
		if (pointList != null && pointList.length() > 0) {
			String[] points = pointList.split("\\Q|\\E");
			for (String point : points) {
				String[] aPoint = point.split(",");
				try {
					if (aPoint.length > 1) {
						Coordinate c = new Coordinate(
								Double.parseDouble(aPoint[0]) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH,
								Double.parseDouble(aPoint[1]) / RocksimCommonConstants.ROCKSIM_TO_OPENROCKET_LENGTH);
						result.add(c);
					}
					else {
						warnings.add("Invalid fin point pair.");
					}
				} catch (NumberFormatException nfe) {
					warnings.add("Fin point not in numeric format.");
				}
			}
			if (!result.isEmpty()) {
				//OpenRocket requires fin plan points be ordered from leading root chord to trailing root chord in the
				//Coordinate array.
				Coordinate last = result.get(result.size() - 1);
				if (last.x == 0 && last.y == 0) {
					Collections.reverse(result);
				}
			}
		}
		final Coordinate[] coords = new Coordinate[result.size()];
		return result.toArray(coords);
	}
	
	
	/**
	 * Convert a Rocksim tip shape to an OpenRocket CrossSection.
	 *
	 * @param tipShape the tip shape code from Rocksim
	 *
	 * @return a CrossSection instance
	 */
	public static FinSet.CrossSection convertTipShapeCode(int tipShape) {
		switch (tipShape) {
		case 0:
			return FinSet.CrossSection.SQUARE;
		case 1:
			return FinSet.CrossSection.ROUNDED;
		case 2:
			return FinSet.CrossSection.AIRFOIL;
		default:
			return FinSet.CrossSection.SQUARE;
		}
	}
	
	public static int convertTipShapeCode(FinSet.CrossSection cs) {
		if (FinSet.CrossSection.ROUNDED.equals(cs)) {
			return 1;
		}
		if (FinSet.CrossSection.AIRFOIL.equals(cs)) {
			return 2;
		}
		return 0;
	}
	
}
