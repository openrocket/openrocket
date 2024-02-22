package info.openrocket.swing.gui.rocketfigure;


import java.awt.Shape;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.LineStyle;
import info.openrocket.core.util.Transformation;


/**
 * A catch-all, no-operation drawing component.
 */
public class RocketComponentShape {

	final public boolean hasShape;
	final public Shape shape;
	public ORColor color;
	final public LineStyle lineStyle;
	final public RocketComponent component;
	
	//fillColor);
	//borderColor);

	protected RocketComponentShape(){
		this.hasShape = false;
		this.shape = null;
		this.color = null;
		this.lineStyle = null;
		this.component=null;
	}
	
	public RocketComponentShape( final Shape _shape, final RocketComponent _comp){
		this.shape = _shape;
		this.color = _comp.getColor();
		this.lineStyle = _comp.getLineStyle();
		this.component = _comp;
		
		if( null == _shape ){
			this.hasShape = false;
		}else{
			this.hasShape = true;
		}
	}
	
	public RocketComponent getComponent(){
		return this.component;
	}

	
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {
		// no-op
		Application.getExceptionHandler().handleErrorCondition("ERROR:  RocketComponent.getShapesSide called with "
				+ component);
		return new RocketComponentShape[0];
	}
	
	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
		// no-op
		Application.getExceptionHandler().handleErrorCondition("ERROR:  RocketComponent.getShapesBack called with "
				+component);
		return new RocketComponentShape[0];
	}

	public ORColor getColor() {
		return color;
	}

	public void setColor(ORColor color) {
		this.color = color;
	}

	public static RocketComponentShape[] toArray(final Shape[] shapeArray, final RocketComponent rc){
		RocketComponentShape[] toReturn = new RocketComponentShape[ shapeArray.length];
		for ( int curShapeIndex=0;curShapeIndex<shapeArray.length; curShapeIndex++){
			Shape curShape = shapeArray[curShapeIndex ];
			toReturn[curShapeIndex] = new RocketComponentShape( curShape, rc);
		}
		return toReturn;
	}
}
