package net.sf.openrocket.preset.loader;

import java.util.Locale;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.Transition.Shape;
import net.sf.openrocket.util.BugException;

public class ShapeColumnParser extends BaseColumnParser {

	public ShapeColumnParser() {
		super("Shape");
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		Transition.Shape shape = null;
		String lc = columnData.toLowerCase(Locale.US);
		if ( "ogive".equals(lc) ) {
			shape = Shape.OGIVE;
		}
		if ( "conical".equals(lc) ) {
			shape = Shape.CONICAL;
		}
		if ( "cone".equals(lc) ) {
			shape = Shape.CONICAL;
		}
		if ( "elliptical".equals(lc) ) {
			shape = Shape.ELLIPSOID;
		}
		if ( "parabolic".equals(lc) ) {
			shape = Shape.PARABOLIC;
		}
		if ( "sears-haack".equals(lc) ) {
			shape = Shape.HAACK;
		}
		if ( "power-series".equals(lc) ) {
			shape = Shape.POWER;
		}
		// guessing at what "ps" means.  I think it might be power series.
		if ( "ps".equals(lc) ) {
			shape = Shape.POWER;
		}
		if ( "1".equals(lc) ) {
			shape = Shape.OGIVE;
		}
		if( "0". equals(lc) ) {
			shape = Shape.CONICAL;
		}
		if( "". equals(lc) ) {
			shape = Shape.CONICAL;
		}
		if ( "3".equals(lc) ) {
			shape = Shape.ELLIPSOID;
		}
		if ( shape == null ) {
			throw new BugException("Invalid shape parameter: " + columnData);
		}
		props.put(ComponentPreset.SHAPE, shape);
	}

}
