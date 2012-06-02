/**
 * 
 */
package net.sf.openrocket.gui.customexpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.CustomExpression;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.startup.Application;

/**
 * @author Richard Graham
 *
 */
public class VariableTableModel extends AbstractTableModel {

	private static final Translator trans = Application.getTranslator();

	private ArrayList<FlightDataType> types = new ArrayList<FlightDataType>();
	private static final String[] columnNames = {trans.get("customExpression.Name"), trans.get("customExpression.Symbol"), trans.get("customExpression.Units")};
	
	/*
	 * Table model will be constructed with all the built in variables and any custom variables defined
	 */
	public VariableTableModel(Simulation sim){
		
		Collections.addAll(types, FlightDataType.ALL_TYPES);
		
		for (CustomExpression expression : sim.getCustomExpressions()){
			types.add(expression.getType());
		}
		
	}
	
	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return types.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return types.get(row).getName();
		else if (col == 1)
			return types.get(row).getSymbol();
		else if (col == 2)
			return types.get(row).getUnitGroup().getDefaultUnit().toString();
		
		return null;
	}
	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	public String getSymbolAt(int row) {
		if (row < 0 || row > types.size()){
			return "";
		}
		else { 
			return types.get(row).getSymbol();
		}
	}
}
