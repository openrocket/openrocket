/**
 * 
 */
package net.sf.openrocket.gui.customexpression;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.simulation.customexpression.CustomExpression;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.startup.Application;

/**
 * @author Richard Graham
 *
 */
public class VariableTableModel extends AbstractTableModel {

	private static final Translator trans = Application.getTranslator();

	private List<FlightDataType> types; // = new ArrayList<FlightDataType>();
	private static final String[] columnNames = {trans.get("customExpression.Name"), trans.get("customExpression.Symbol"), trans.get("customExpression.Units")};
	
	/*
	 * Table model will be constructed with all the built in variables and any custom variables defined
	 */
	public VariableTableModel(OpenRocketDocument doc){
		
		types = new ArrayList<FlightDataType>( doc.getFlightDataTypes() );
		
		//Collections.addAll(types, FlightDataType.ALL_TYPES);
		//for (CustomExpression expression : doc.getCustomExpressions()){
		//	types.add(expression.getType());
		//}
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
			return types.get(row).getUnitGroup().getSIUnit().toString();
		
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
