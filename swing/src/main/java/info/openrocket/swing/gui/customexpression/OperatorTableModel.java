package info.openrocket.swing.gui.customexpression;

import javax.swing.table.AbstractTableModel;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.customexpression.Functions;
import info.openrocket.core.startup.Application;

public class OperatorTableModel extends AbstractTableModel {

	private static final Translator trans = Application.getTranslator();
	
	private static final String[] columnNames = {trans.get("customExpression.Operator"), trans.get("customExpression.Description")};
	
	private final Object[] operators = Functions.AVAILABLE_OPERATORS.keySet().toArray();
	private final Object[] descriptions = Functions.AVAILABLE_OPERATORS.values().toArray();
	
	public OperatorTableModel(){
		
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return Functions.AVAILABLE_OPERATORS.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0){
			return operators[row].toString();
		}
		else if (col == 1){
			return descriptions[row].toString();
		}
		return null;
	}
	
	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }
	
	public String getOperatorAt(int row) {
		return operators[row].toString();
	}

}
