package net.sf.openrocket.gui.rocketfigure;


import java.awt.Shape;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.Transformation;


/**
 * A catch-all, no-operation drawing component.
 */
public class RocketComponentShape {

	final public boolean hasShape;
	final public Shape shape;
	final public net.sf.openrocket.util.Color color;
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
	
	public static RocketComponentShape[] toArray( final Shape[] shapeArray, final RocketComponent rc){
		RocketComponentShape[] toReturn = new RocketComponentShape[ shapeArray.length];
		for ( int curShapeIndex=0;curShapeIndex<shapeArray.length; curShapeIndex++){
			Shape curShape = shapeArray[curShapeIndex ];
			toReturn[curShapeIndex] = new RocketComponentShape( curShape, rc);
		}
		return toReturn;
	}
	
	
}
