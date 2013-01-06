package net.sf.openrocket.unit;

/*
 * This class provides a 'dumb' version of UnitGroup
 * It allows any arbitrary unit to be created. It doesn't store any value and can't be converted into anything else.
 * This is useful for custom expression units.
 * 
 * @author Richard Graham
 */

public class FixedUnitGroup extends UnitGroup {
	
	String unitString;
	
	public FixedUnitGroup( String unitString ){
		this.unitString = unitString; 
	}
	
	@Override
	public int getUnitCount(){
		return 1;
	}
	
	@Override
	public Unit getDefaultUnit(){
		return new GeneralUnit(1, unitString);
	}
	
	@Override
	public Unit getSIUnit(){
		return new GeneralUnit(1, unitString);
	}
	
	@Override
	public boolean contains(Unit u){
		return true;
	}
	
}
