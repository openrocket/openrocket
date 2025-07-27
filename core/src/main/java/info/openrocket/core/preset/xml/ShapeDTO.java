
package info.openrocket.core.preset.xml;

import info.openrocket.core.rocketcomponent.Transition;

import jakarta.xml.bind.annotation.XmlEnum;

/**
 * A mirror class to Transition.Shape to adapt that class to/from XML.
 */
@XmlEnum(String.class)
public enum ShapeDTO {

    CONICAL(Transition.Shape.CONICAL),
    OGIVE(Transition.Shape.OGIVE),
    ELLIPSOID(Transition.Shape.ELLIPSOID),
    POWER(Transition.Shape.POWER),
    PARABOLIC(Transition.Shape.PARABOLIC),
    HAACK(Transition.Shape.HAACK);

    private final Transition.Shape corollary;

    private ShapeDTO(Transition.Shape theShape) {
        corollary = theShape;
    }

    public static ShapeDTO asDTO(Transition.Shape targetShape) {
        ShapeDTO[] values = values();
		for (ShapeDTO value : values) {
			if (value.corollary.equals(targetShape)) {
				return value;
			}
		}
        return ELLIPSOID; // default
    }

    public Transition.Shape getORShape() {
        return corollary;
    }
}
