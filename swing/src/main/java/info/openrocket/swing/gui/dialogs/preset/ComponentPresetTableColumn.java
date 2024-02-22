package info.openrocket.swing.gui.dialogs.preset;

import java.util.Set;

import javax.swing.table.TableColumn;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.unit.Value;

@SuppressWarnings("serial")
public abstract class ComponentPresetTableColumn extends TableColumn {

	private static final Translator trans = Application.getTranslator();

	protected ComponentPresetTableColumn( String header, int modelIndex ) {
		this.setHeaderValue(header);
		this.setModelIndex(modelIndex);
	
	}
	
	public abstract Object getValueFromPreset( Set<String> favorites, ComponentPreset preset );
	
	public static class Favorite extends ComponentPresetTableColumn {

		public Favorite(int modelIndex) {
			super(trans.get("table.column.Favorite"), modelIndex);
		}
		
		@Override
		public Object getValueFromPreset( Set<String> favorites, ComponentPreset preset ) {
			return favorites.contains(preset.preferenceKey());
		}

	}

	public static class Parameter extends ComponentPresetTableColumn {

		protected final TypedKey<?> key;
		
		public Parameter( TypedKey<?> key, int modelIndex ) {
			super( trans.get("table.column." + key.getName()), modelIndex );
			this.key = key;
		}

		@Override
		public Object getValueFromPreset(Set<String> favorites, ComponentPreset preset) {
			return preset.has(key) ? preset.get(key) : null;
		}
		
	}


	public static class DoubleWithUnit extends Parameter {

		UnitGroup unitGroup;
		Unit selectedUnit;
		
		public DoubleWithUnit( TypedKey<Double> key, int modelIndex ) {
			super(key,modelIndex);
			this.unitGroup = key.getUnitGroup();
			this.selectedUnit = unitGroup.getDefaultUnit();
		}

		@Override
		public Object getValueFromPreset(Set<String> favorites, ComponentPreset preset) {
			Double value = (Double) super.getValueFromPreset(favorites, preset);
			if ( value != null ) {
				return new Value((Double)super.getValueFromPreset(favorites, preset),selectedUnit);
			} else {
				return null;
			}
		}
		
		
	}

}



